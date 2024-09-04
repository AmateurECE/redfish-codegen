#!/usr/bin/make -f
###############################################################################
# NAME:             generate.mk
#
# AUTHOR:           Ethan D. Twardy <ethan.twardy@gmail.com>
#
# DESCRIPTION:      Generate the sources from an OpenAPI document.
#
# CREATED:          01/29/2023
#
# LAST EDITED:	    06/25/2023
#
####

RELEASE_LINK=https://www.dmtf.org/sites/default/files/standards/documents
REDFISH_VERSION=2024.2
SCHEMA_FILE=DSP8010_$(REDFISH_VERSION).zip
REGISTRY_VERSION=2024.1
REGISTRY_FILE=DSP8011_$(REGISTRY_VERSION).zip

B=DSP8010_$(REDFISH_VERSION)

SWORDFISH_VERSION=v1.2.7
SWORDFISH_SCHEMA_FILE=Swordfish_$(SWORDFISH_VERSION)_Schema.zip
SWORDFISH_LINK=https://www.snia.org/sites/default/files/technical-work/swordfish/release/$(SWORDFISH_VERSION)/zip/$(SWORDFISH_SCHEMA_FILE)

OPENAPI_DOCUMENT=$(B)/openapi/openapi.yaml

JAR_FILE=redfish-generator/target/redfish-codegen-0.3.1-SNAPSHOT.jar
JVM_ARGS=-DmaxYamlCodePoints=6291456 -Dfile.encoding=UTF-8

ifdef CARGO_FEATURE_CLIENT
JAR_ARGS += -clientMode
endif

define redfish_models
(cd $1 && java $(JVM_ARGS) -jar ../$(JAR_FILE) \
	-specDirectory ../$(B) \
	-specVersion $(REDFISH_VERSION) \
	-registryDirectory ../registry \
	-component $2 $(JAR_ARGS))
endef

CODEGEN_DEPENDENCIES += $(B)/openapi/openapi.yaml
CODEGEN_DEPENDENCIES += registry/DSP8011_$(REGISTRY_VERSION).pdf
CODEGEN_DEPENDENCIES += $(JAR_FILE)

models: redfish-models/src/lib.rs
routing: redfish-axum/src/lib.rs

redfish-models/src/lib.rs: $(CODEGEN_DEPENDENCIES)
	$(call redfish_models,redfish-models,models)

redfish-axum/src/lib.rs: $(CODEGEN_DEPENDENCIES)
	$(call redfish_models,redfish-axum,routing)

# Schema

$(SCHEMA_FILE):
	curl -LO $(RELEASE_LINK)/$(SCHEMA_FILE)

$(SWORDFISH_SCHEMA_FILE):
	curl -LO $(SWORDFISH_LINK)

SCHEMA_FILES += $(SCHEMA_FILE)
SCHEMA_FILES += $(SWORDFISH_SCHEMA_FILE)
SCHEMA_FILES += $(REGISTRY_FILE)

get-schema: $(SCHEMA_FILES)

$(B)/.unzip.lock: $(SCHEMA_FILES)
	unzip -o -DD $(SCHEMA_FILE)
	: # Unzip from the swordfish distribution only those files which are
	: # not included in the Redfish distribution.
	unzip -n -DD -d swordfish $(SWORDFISH_SCHEMA_FILE)
	mv --update=none swordfish/yaml/* $(B)/openapi/
	unzip -o -DD -d registry $(REGISTRY_FILE)
	touch $@

unzip: DSP8010_$(REDFISH_VERSION)/.unzip.lock

$(B)/openapi/openapi.yaml: $(B)/.unzip.lock
	-if [ -f schema-patches/series ]; then \
		QUILT_PC=$(B)/.pc QUILT_PATCHES=schema-patches quilt push -a --leave-rejects; \
	fi
	sed -i -e 's#http://redfish.dmtf.org/schemas/v1#.#' $(B)/openapi/*.yaml
	sed -i -e 's#http://redfish.dmtf.org/schemas/swordfish/v1#.#' \
		$(B)/openapi/*.yaml

clean:
	rm -rf $(B) registry swordfish

# Registry

$(REGISTRY_FILE):
	curl -L -O $(RELEASE_LINK)/$(REGISTRY_FILE)

registry/DSP8011_$(REGISTRY_VERSION).pdf: $(B)/.unzip.lock
	-QUILT_PC=registry/.pc QUILT_PATCHES=registry-patches quilt push -a
	touch $@

prepare: registry/DSP8011_$(REGISTRY_VERSION).pdf $(B)/openapi/openapi.yaml

# Jar

$(JAR_FILE): redfish-generator/pom.xml
	(cd redfish-generator && mvn clean package)

.PHONY: get-schema unzip clean prepare

###############################################################################
