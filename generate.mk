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
# LAST EDITED:	    04/15/2023
#
####

RELEASE_LINK=https://www.dmtf.org/sites/default/files/standards/documents
RELEASE_VERSION=2022.3
SCHEMA_FILE=DSP8010_$(RELEASE_VERSION).zip
REGISTRY_FILE=DSP8011_$(RELEASE_VERSION).zip

OPENAPI_DOCUMENT=api/openapi/openapi.yaml

all: src/lib.rs

# Schema

$(SCHEMA_FILE):
	curl -L -O $(RELEASE_LINK)/$(SCHEMA_FILE)

api/openapi/openapi.yaml: $(SCHEMA_FILE)
	unzip -o -DD -d api $(SCHEMA_FILE)
	QUILT_PC=api/.pc QUILT_PATCHES=schema-patches quilt push -a

# Registry

$(REGISTRY_FILE):
	curl -L -O $(RELEASE_LINK)/$(REGISTRY_FILE)

registry/DSP8011_2022.3.pdf: $(REGISTRY_FILE)
	unzip -o -DD -d registry $(REGISTRY_FILE)
	QUILT_PC=registry/.pc QUILT_PATCHES=registry-patches quilt push -a

# Jar

JAR_FILE=redfish-generator/target/redfish-codegen-1.0-SNAPSHOT.jar
JVM_ARGS=-DmaxYamlCodePoints=6291456 -Dfile.encoding=UTF-8

$(JAR_FILE): redfish-generator/pom.xml
	(cd redfish-generator && mvn clean package)

# Code generation

src/lib.rs: api/openapi/openapi.yaml registry/DSP8011_2022.3.pdf $(JAR_FILE)
	(cd redfish-codegen && rm -rf src \
		&& java $(JVM_ARGS) -jar ../$(JAR_FILE) \
			-apiDirectory ../api/openapi \
			-specVersion $(RELEASE_VERSION) \
			-registryDirectory ../registry)

###############################################################################
