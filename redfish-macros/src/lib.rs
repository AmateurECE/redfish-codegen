// Author: Ethan D. Twardy <ethan.twardy@gmail.com>

use lazy_static::lazy_static;
use proc_macro2::{Ident, TokenStream};
use quote::{quote, quote_spanned};
use regex::Regex;
use std::str::FromStr;
use syn::spanned::Spanned;
use syn::{Attribute, DeriveInput, Lit, Meta, MetaNameValue, NestedMeta};

const NOT_LIST_ERROR: &str = "attribute requires a list of key/value pairs";
const REGISTRY_IDENT: &str = "registry";

#[derive(Default)]
struct VariantContext {
    message: Option<syn::LitStr>,
    id: Option<syn::LitStr>,
    severity: Option<syn::Path>,
    resolution: Option<syn::LitStr>,
}

#[derive(Default)]
struct VariantExpressions {
    message: TokenStream,
    args: TokenStream,
    severity: TokenStream,
    id: TokenStream,
    resolution: TokenStream,
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

fn field_name_for_index(number: usize) -> Ident {
    quote::format_ident!("_{}", number)
}

fn destructure_fields(fields: &syn::Fields) -> TokenStream {
    match fields {
        syn::Fields::Unnamed(inner_fields) => {
            let field_names = (0..inner_fields.unnamed.len()).map(field_name_for_index);
            quote! {
                ( #(#field_names ,)* )
            }
        }
        syn::Fields::Unit => TokenStream::new(),
        _ => panic!("Enum variants must either be units or contain unnamed fields"),
    }
}

fn construct_format(literal: &syn::LitStr) -> TokenStream {
    lazy_static! {
        static ref PATTERN: Regex = Regex::new(r"(%\d+)").unwrap();
    }

    let message = literal.value();
    if PATTERN.is_match(&message) {
        let arguments = PATTERN.captures_iter(&message).map(|cap| {
            let digit = &mut cap[1].to_string();
            digit.remove(0);
            // The fields in the Redfish registries are 1-indexed.
            field_name_for_index(usize::from_str(digit).unwrap() - 1)
        });
        let format_string = PATTERN.replace_all(&message, "{}");

        quote! { format!(#format_string, #(& #arguments ,)*) }
    } else {
        quote! { #message.to_string() }
    }
}

fn get_variant_context(variant: &syn::Variant) -> VariantContext {
    let mut context = VariantContext {
        ..Default::default()
    };

    for attribute in attribute_name_value_list(variant.attrs.iter(), REGISTRY_IDENT) {
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
                    context.severity = Some(
                        value
                            .parse()
                            .expect("severity expects a path to a resource::Health variant"),
                    )
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

    context
}

fn args_variant(variant: &syn::Variant, destructured_fields: &TokenStream) -> TokenStream {
    let name = &variant.ident;
    let expression = match &variant.fields {
        syn::Fields::Unnamed(inner_fields) => {
            let field_names = (0..inner_fields.unnamed.len()).map(|i| {
                let name = field_name_for_index(i);
                quote!( ::std::string::ToString::to_string(& #name) )
            });
            quote! {
                Some(vec![ #(#field_names ,)* ])
            }
        }
        syn::Fields::Unit => quote!(None),
        _ => panic!("Enum variants must either be units or contain unnamed fields"),
    };

    quote_spanned! {
        variant.span() => Self::#name #destructured_fields => #expression
    }
}

fn get_variant_expressions(data: &syn::Data) -> Vec<VariantExpressions> {
    match *data {
        syn::Data::Enum(ref data) => {
            data.variants.iter().map(|variant| {
                let destructured_fields = destructure_fields(&variant.fields);

                let name = &variant.ident;
                let context = get_variant_context(variant);
                let severity = context.severity.expect("Missing \"severity\" property");
                let id = context.id.expect("Missing \"id\" property");
                let resolution = context.resolution.expect("Missing \"resolution\"property");

                let message = context.message.expect("Missing \"message\" property");
                let message_format = construct_format(&message);
                VariantExpressions {
                    message: quote_spanned!(variant.span() => Self::#name #destructured_fields => #message_format),
                    args: args_variant(variant, &destructured_fields),
                    severity: quote_spanned!(variant.span() => Self::#name #destructured_fields => #severity),
                    id: quote_spanned!(variant.span() => Self::#name #destructured_fields => #id),
                    resolution: quote_spanned!(variant.span() => Self::#name #destructured_fields => #resolution),
                }
            }).collect::<Vec<VariantExpressions>>()
        }

        syn::Data::Struct(_) | syn::Data::Union(_) => unimplemented!(),
    }
}

#[proc_macro_derive(Registry, attributes(registry))]
pub fn derive_into_redfish_message(input: proc_macro::TokenStream) -> proc_macro::TokenStream {
    let input = syn::parse_macro_input!(input as DeriveInput);
    let (impl_generics, ty_generics, where_clause) = input.generics.split_for_impl();

    let name = &input.ident;
    let variant_expressions = get_variant_expressions(&input.data);

    let message_variants = variant_expressions.iter().map(|e| &e.message);
    let args_variants = variant_expressions.iter().map(|e| &e.args);
    let severity_variants = variant_expressions.iter().map(|e| &e.severity);
    let id_variants = variant_expressions.iter().map(|e| &e.id);
    let resolution_variants = variant_expressions.iter().map(|e| &e.resolution);

    let expanded = quote! {
        // The generated impl
        impl #impl_generics crate::Registry<'static> for #name #ty_generics
           #where_clause
        {
            fn message(self) -> String {
                match self {
                    #(#message_variants ,)*
                }
            }

            fn args(&self) -> Option<Vec<String>> {
                match &self {
                    #(#args_variants ,)*
                }
            }

            fn severity(&self) -> crate::models::resource::Health {
                match &self {
                    #(#severity_variants ,)*
                }
            }

            fn id(&self) -> &'static str {
                match &self {
                    #(#id_variants ,)*
                }
            }

            fn resolution(&self) -> &'static str {
                match &self {
                    #(#resolution_variants ,)*
                }
            }
        }
    };

    proc_macro::TokenStream::from(expanded)
}
