package org.apache.kafka.common.security.authenticator;

import java.security.cert.Certificate;

import javax.security.auth.callback.Callback;

public class X509Callback implements Callback {

    private Certificate[] peerCertificates;

    public Certificate[] getPeerCertificates() {
        return peerCertificates;
    }

    public void setPeerCertificates(Certificate[] peerCertificates) {
        this.peerCertificates = peerCertificates;
    }

}
