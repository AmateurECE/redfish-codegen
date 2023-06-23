#!/bin/sh
service_name=$1 && shift
crate_name=$(echo $service_name | sed -e 's/-/_/')
podman run \
       -it \
       --rm \
       -v ./target/debug/$service_name:/usr/bin/$service_name:ro \
       -v ./test/config.yaml:/etc/redfish/config.yaml:ro \
       -v ./test/manager-key.pem:/etc/redfish/twardyece-manager-key.pem:ro \
       -v ./test/manager-cert.pem:/etc/redfish/twardyece-manager-cert.pem:ro \
       -p 3000:3000 \
       -p 3001:3001 \
       --entrypoint=/usr/bin/$service_name \
       -e 'RUST_LOG=tower_http=debug,'$crate_name=debug \
       seuss-test:latest \
       --config /etc/redfish/config.yaml
