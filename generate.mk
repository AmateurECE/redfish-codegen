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
RELEASE_VERSION=2022.3
SCHEMA_FILE=DSP8010_$(RELEASE_VERSION).zip
REGISTRY_FILE=DSP8011_$(RELEASE_VERSION).zip

SWORDFISH_VERSION=v1.2.4a
SWORDFISH_SCHEMA_FILE=Swordfish_$(SWORDFISH_VERSION)_Schema.zip
SWORDFISH_LINK=https://www.snia.org/sites/default/files/technical-work/swordfish/release/$(SWORDFISH_VERSION)/zip/$(SWORDFISH_SCHEMA_FILE)

OPENAPI_DOCUMENT=api/openapi/openapi.yaml

JAR_FILE=redfish-generator/target/redfish-codegen-1.0-SNAPSHOT.jar
JVM_ARGS=-DmaxYamlCodePoints=6291456 -Dfile.encoding=UTF-8

define redfish_codegen
(cd $1 && java $(JVM_ARGS) -jar ../$(JAR_FILE) \
	-specDirectory ../api \
	-specVersion $(RELEASE_VERSION) \
	-registryDirectory ../registry \
	-component $2)
endef

CODEGEN_DEPENDENCIES += api/openapi/openapi.yaml
CODEGEN_DEPENDENCIES += registry/DSP8011_2022.3.pdf
CODEGEN_DEPENDENCIES += $(JAR_FILE)

models: redfish-codegen/src/lib.rs
routing: redfish-axum/src/lib.rs

redfish-codegen/src/lib.rs: $(CODEGEN_DEPENDENCIES)
	$(call redfish_codegen,redfish-codegen,models)

redfish-axum/src/lib.rs: $(CODEGEN_DEPENDENCIES)
	$(call redfish_codegen,redfish-axum,routing)

# Schema

$(SCHEMA_FILE):
	curl -LO $(RELEASE_LINK)/$(SCHEMA_FILE)

$(SWORDFISH_SCHEMA_FILE):
	curl -LO $(SWORDFISH_LINK)

api/openapi/openapi.yaml: $(SCHEMA_FILE) $(SWORDFISH_SCHEMA_FILE)
	unzip -o -DD -d api $(SCHEMA_FILE)
	QUILT_PC=api/.pc QUILT_PATCHES=schema-patches quilt push -a
	unzip -o -DD -d api $(SWORDFISH_SCHEMA_FILE)
	mv -f api/openapi/openapi.yaml api/redfish.yaml
	mv -f api/yaml/* api/openapi/
	mv -f api/redfish.yaml api/openapi/openapi.yaml
	sed -i -e 's#http://redfish.dmtf.org/schemas/v1#./#' api/openapi/*.yaml
	sed -i -e 's#http://redfish.dmtf.org/schemas/swordfish/v1#./#' \
		api/openapi/*.yaml

# Registry

$(REGISTRY_FILE):
	curl -L -O $(RELEASE_LINK)/$(REGISTRY_FILE)

registry/DSP8011_2022.3.pdf: $(REGISTRY_FILE)
	unzip -o -DD -d registry $(REGISTRY_FILE)
	QUILT_PC=registry/.pc QUILT_PATCHES=registry-patches quilt push -a

# Jar

$(JAR_FILE): redfish-generator/pom.xml
	(cd redfish-generator && mvn clean package)

###############################################################################
