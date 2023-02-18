// The ClientCertificatesCollection provides the following URLs:
//  /redfish/v1/UpdateService/ClientCertificates
//  /redfish/v1/UpdateService/ClientCertificates/{CertificateId}
//  /redfish/v1/UpdateService/ClientCertificates/{CertificateId}/Actions/Certificate.Rekey
//  /redfish/v1/UpdateService/ClientCertificates/{CertificateId}/Actions/Certificate.Renew

pub trait ClientCertificates {
    const URL: &'static str = "/redfish/v1/UpdateService/ClientCertificates";
}

// TODO: Sub-urls
