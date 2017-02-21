/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.kafka.common.security.authenticator;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class TestSslLoginModule implements LoginModule {
    private X509Certificate x509;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        Callback[] callbacks = new Callback[1];
        X509Callback callback = new X509Callback();
        callbacks[0] = callback;
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            //handle exception
        }

        Certificate[] certs = callback.getPeerCertificates();
        if (certs != null) {
            x509 = (X509Certificate) certs[0];
           // System.out.println(x509.getSubjectX500Principal());
        }

    }

    @Override
    public boolean login() throws LoginException {
        //for testing just make sure not null
        //real module would do more advanced logic
        return x509 != null;
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }

}
