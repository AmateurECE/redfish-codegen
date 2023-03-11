// Author: Ethan D. Twardy <ethan.twardy@gmail.com>
//
// Copyright 2023, Ethan Twardy. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the \"License\");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an \"AS IS\" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use proc_macro2::TokenStream;
use quote::{quote, quote_spanned};
use syn::spanned::Spanned;
use syn::{Attribute, DeriveInput, Lit, Meta, MetaNameValue, NestedMeta};

const NOT_LIST_ERROR: &str = "attribute requires a list of key/value pairs";
const MESSAGE_IDENT: &str = "message";

#[derive(Default)]
struct VariantContext {
    message: Option<syn::LitStr>,
    id: Option<syn::LitStr>,
    severity: Option<syn::Path>,
    resolution: Option<syn::LitStr>,
}

fn attribute_list<'a, I>(attributes: I, ident: &str) -> Vec<Meta>
where
    I: Iterator<Item = &'a Attribute>,
{
    attributes
        .filter(|a| a.path.is_ident(ident))
        .map(|a| {
            let meta = a.parse_meta().expect("Failed to parse attribute");
            if let Meta::List(ref list) = meta {
                Some(
                    list.nested
                        .iter()
                        .filter_map(|nested| match nested {
                            NestedMeta::Meta(ref value) => Some(value.clone()),
                            _ => {
                                panic!("Expecting a structured attribute")
                            }
                        })
                        .collect::<Vec<_>>(),
                )
            } else {
                panic!("Expecting a list of nested attributes")
            }
        })
        .collect::<Option<Vec<_>>>()
        .unwrap_or_else(|| panic!("No attribute matching identifier \"{}\"", ident))
        .concat()
}

fn attribute_path_list<'a, I>(attributes: I, ident: &str) -> Vec<syn::Path>
where
    I: Iterator<Item = &'a Attribute>,
{
    attribute_list(attributes, ident)
        .into_iter()
        .filter_map(|meta| match meta {
            Meta::Path(ref path) => Some(path.clone()),
            _ => {
                panic!("{}", NOT_LIST_ERROR)
            }
        })
        .collect::<Vec<_>>()
}

// Obtain an iterator over the list of nested Meta attributes of a field
fn attribute_name_value_list<'a, I>(attributes: I, ident: &str) -> Vec<MetaNameValue>
where
    I: Iterator<Item = &'a Attribute>,
{
    attribute_list(attributes, ident)
        .into_iter()
        .filter_map(|meta| match meta {
            Meta::NameValue(ref name_value) => Some(name_value.clone()),
            _ => {
                panic!("{}", NOT_LIST_ERROR)
            }
        })
        .collect::<Vec<_>>()
}

fn get_message_type(input: &DeriveInput) -> TokenStream {
    let attributes = attribute_path_list(input.attrs.iter(), MESSAGE_IDENT);
    let message_type = attributes.into_iter().last().unwrap();
    quote!(#message_type)
}

fn define_variant_coersion(data: &syn::Data, message_type: &TokenStream) -> TokenStream {
    match *data {
        syn::Data::Enum(ref data) => {
            let variants = data.variants.iter().map(|variant| {
                let name = &variant.ident;
                let mut context = VariantContext {
                    ..Default::default()
                };

                for attribute in attribute_name_value_list(variant.attrs.iter(), MESSAGE_IDENT) {
                    if attribute.path.is_ident("message") {
                        match attribute.lit {
                            Lit::Str(ref value) => context.message = Some(value.clone()),
                            _ => panic!("{}", NOT_LIST_ERROR),
                        }
                    } else if attribute.path.is_ident("id") {
                        match attribute.lit {
                            Lit::Str(ref value) => context.id = Some(value.clone()),
                            _ => panic!("{}", NOT_LIST_ERROR),
                        }
                    } else if attribute.path.is_ident("severity") {
                        match attribute.lit {
                            Lit::Str(ref value) => {
                                context.severity = Some(value.parse().expect(
                                    "severity expects a path to a resource::Health variant",
                                ))
                            }
                            _ => panic!("{}", NOT_LIST_ERROR),
                        }
                    } else if attribute.path.is_ident("resolution") {
                        match attribute.lit {
                            Lit::Str(ref value) => context.resolution = Some(value.clone()),
                            _ => panic!("{}", NOT_LIST_ERROR),
                        }
                    } else {
                        panic!("{}", NOT_LIST_ERROR)
                    }
                }

                let message = context.message.expect("Missing \"message\" property");
                let severity = context.severity.expect("Missing \"severity\" property");
                let id = context.id.expect("Missing \"id\" property");
                let resolution = context.resolution.expect("Missing \"resolution\"property");
                quote_spanned! {
                    variant.span() => Self::#name (_) => #message_type {
                        message: Some(#message.to_string()),
                        message_id: #id.to_string(),
                        message_severity: Some(#severity),
                        resolution: Some(#resolution.to_string()),
                        ..Default::default()
                    }
                }
            });

            quote! { #(#variants ,)* }
        }

        syn::Data::Struct(_) | syn::Data::Union(_) => unimplemented!(),
    }
}

#[proc_macro_derive(IntoRedfishMessage, attributes(message))]
pub fn derive_into_redfish_message(input: proc_macro::TokenStream) -> proc_macro::TokenStream {
    let input = syn::parse_macro_input!(input as DeriveInput);
    let (impl_generics, ty_generics, where_clause) = input.generics.split_for_impl();

    let name = &input.ident;
    let message_type = get_message_type(&input);
    let variant_coersion = define_variant_coersion(&input.data, &message_type);
    let expanded = quote! {
        // The generated impl
        impl #impl_generics Into< #message_type > for #name #ty_generics
           #where_clause
        {
           fn into(self) -> #message_type {
               match self {
                   #variant_coersion
               }
           }
        }
    };

    proc_macro::TokenStream::from(expanded)
}
