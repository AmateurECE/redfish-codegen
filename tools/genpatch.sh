#!/bin/bash -xe
# Generates some patches in the set that fixes the OpenAPI documents for code
# generation.

SCRIPT_DIR=$(dirname $0)

export QUILT_PC=api/.pc
export QUILT_PATCHES=schema-patches

PATCHES=(
  "0001-Translate-openapi-fragments.patch"
)

SCRIPTS=(
  "${SCRIPT_DIR}/fix_openapi_fragments.py"
)

if [ ! -d schema-patches ]; then
	printf >&2 "This script should be run from the root of the repository.\n"
	exit 1
fi

setup() {
  make -f generate.mk clean && make -f generate.mk unzip

  # Reset the patches
  for patch in $PATCHES; do
    rm -f schema-patches/$patch
    sed -i -e "/$patch/d" schema-patches/series
  done
}

teardown() {
  make -f generate.mk clean
}

patch_by_script() {
  local patch=$1
  local script=$2

  quilt new $patch
  quilt add api/openapi/*.yaml

  python3 $script api/openapi

  quilt refresh
}

setup
for ((i = 0; i < ${#PATCHES[@]}; i++)); do
  patch_by_script ${PATCHES[$i]} ${SCRIPTS[$i]}
done
teardown

