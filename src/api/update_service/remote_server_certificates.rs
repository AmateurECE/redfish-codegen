// The RemoteServerCertificatesCollection provides the following URLs:
//  /redfish/v1/UpdateService/RemoteServerCertificates
//  /redfish/v1/UpdateService/RemoteServerCertificates/{CertificateId}
//  /redfish/v1/UpdateService/RemoteServerCertificates/{CertificateId}/Actions/Certificate.Rekey
//  /redfish/v1/UpdateService/RemoteServerCertificates/{CertificateId}/Actions/Certificate.Renew

pub trait UpdateServiceRemoteServerCertificates {
    const URL: &'static str = "/redfish/v1/UpdateService/RemoteServerCertificates";
}
