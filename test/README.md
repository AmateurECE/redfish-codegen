# How to use

This directory contains configuration for testing a redfish service developed
with seuss inside of an OCI container. The steps are as follows:

1. Generate a TLS certificate/key pair for use with the service
2. Build the OCI image
3. Run using the convenience script provided

To test using the [Redfish Protocol Validator][1], these additional steps are
required:

1. Create a Python virtual environment and install the tools from the
   requirements.txt
2. Run the Redfish Protocol Validator

## Generating a certificate/key pair for testing

```bash
# Generate a private key
openssl genrsa -aes256 -out test/manager.key

# Convert to PEM format
openssl rsa -in test/manager.key -text > test/manager-key.pem

# Generate a certificate signing request
openssl req -key test/manager.key -new -out test/manager.csr

# Sign the certificate
openssl x509 -signkey test/manager.key -in test/manager.csr -req -days 365 -out test/manager-cert.pem
```

## Build the OCI image

```bash-session
$ podman build -t redfish-test:latest .
```

## Run using the convenience script

The conenience script will attempt to load the specified binary into the
container. For example, if my compiled service binary is at
`./target/debug/simple`, I would run:

```bash
$ ./test/run-container.sh simple
```

## Create a Python virtual environment

```bash-session
$ python3 -m venv ./test/venv
$ . ./test/venv/bin/activate
$ pip install -r test/requirements.txt
```

## Run the Redfish Protocol Validator

```
rf_protocol_validator -r https://localhost:3001 --no-cert-check -u administrator -p administrator
```

[1]: https://github.com/DMTF/Redfish-Protocol-Validator
