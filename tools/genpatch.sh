#!/bin/sh -xe
# Generates some patches in the set that fixes the OpenAPI documents for code
# generation.

FIX_OPENAPI_FRAGMENTS=$(dirname $0)/fix_openapi_fragments.py

if [ ! -d schema-patches ]; then
	printf >&2 "This script should be run from the root of the repository.\n"
	exit 1
fi

make -f generate.mk clean && make -f generate.mk unzip

export QUILT_PC=api/.pc
export QUILT_PATCHES=schema-patches

PATCH_NAME=0001-Translate-openapi-fragments.patch

# Create a new patch that will convert the fragments to valid OpenAPI
# documents.
rm -f schema-patches/$PATCH_NAME
sed -i -e "/$PATCH_NAME/d" schema-patches/series
quilt new $PATCH_NAME
quilt add api/openapi/*.yaml

python3 $FIX_OPENAPI_FRAGMENTS api/openapi

quilt refresh
make -f generate.mk clean
