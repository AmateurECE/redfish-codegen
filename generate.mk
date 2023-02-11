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
# LAST EDITED:	    02/08/2023
#
####

RELEASE_LINK=https://www.dmtf.org/sites/default/files/standards/documents
RELEASE_FILE=DSP8010_2022.3.zip

OPENAPI_DOCUMENT=api/openapi/openapi.yaml

all: api/openapi/openapi.yaml

$(RELEASE_FILE):
	curl -L -O $(RELEASE_LINK)/$(RELEASE_FILE)

api/openapi/openapi.yaml: $(RELEASE_FILE)
	unzip -o -DD -d api $(RELEASE_FILE)

JAR_FILE=redfish-codegen/target/redfish-codegen-1.0-SNAPSHOT.jar
JVM_ARGS=-DmaxYamlCodePoints=6291456 -Dfile.encoding=UTF-8

$(JAR_FILE): redfish-codegen/pom.xml
	(cd redfish-codegen && mvn clean package)

src/models: api/openapi/openapi.yaml $(JAR_FILE)
	java $(JVM_ARGS) -jar $(JAR_FILE) -apiDirectory api -crateDirectory .

###############################################################################
