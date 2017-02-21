package org.apache.kafka.common.security.authenticator;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.kafka.common.network.SslTransportLayer;

public class X509CallbackHandler implements CallbackHandler {
    private final SslTransportLayer transportLayer;

    public X509CallbackHandler(SslTransportLayer transportLayer) {
        super();
        this.transportLayer = transportLayer;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof X509Callback) {
                X509Callback x509Callback = (X509Callback) callback;
                x509Callback.setPeerCertificates(transportLayer.getPeerCertificates());
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized SSL Login callback");
            }
        }
    }

}
