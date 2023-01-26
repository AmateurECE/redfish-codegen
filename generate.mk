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
# LAST EDITED:	    01/29/2023
#
####

RELEASE_LINK=https://www.dmtf.org/sites/default/files/standards/documents
RELEASE_FILE=DSP8010_2022.3.zip

OPENAPI_DOCUMENT=api/openapi/openapi.yaml

all: src/lib.rs

$(RELEASE_FILE):
	curl -L -O $(RELEASE_LINK)/$(RELEASE_FILE)

api/openapi/openapi.yaml: $(RELEASE_FILE)
	unzip -o -DD -d api $(RELEASE_FILE)

OPENAPI_MODULE=openapi-generator-cli
OPENAPI_VERSION=6.3.0-SNAPSHOT
REPOSITORY=$(HOME)/.m2/repository/org/openapitools/$(OPENAPI_MODULE)
JAR=$(REPOSITORY)/$(OPENAPI_VERSION)/$(OPENAPI_MODULE)-$(OPENAPI_VERSION).jar

JVM_ARGS=-DmaxYamlCodePoints=6291456 -Dfile.encoding=UTF-8

src/lib.rs: api/openapi/openapi.yaml
	java $(JVM_ARGS) -jar $(JAR) generate \
		-g rust-server \
		-i $< \
		-c config.yaml

###############################################################################
