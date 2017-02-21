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

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.network.Authenticator;
import org.apache.kafka.common.network.SslTransportLayer;
import org.apache.kafka.common.network.TransportLayer;
import org.apache.kafka.common.security.JaasContext;
import org.apache.kafka.common.security.auth.PrincipalBuilder;

public class SslServerAuthenticator implements Authenticator {

    private PrincipalBuilder principalBuilder;
    private Subject subject;
    private JaasContext jaasContext;
    private SslTransportLayer transportLayer;
    private LoginManager loginManager;
    private Map<String, ?> configs;
    private boolean complete = false;

    public void configure(TransportLayer transportLayer, PrincipalBuilder principalBuilder, Map<String, ?> configs,
            JaasContext jaasContext) {
        this.configure(transportLayer, principalBuilder, configs);
        this.transportLayer = (SslTransportLayer) transportLayer;
        this.jaasContext = jaasContext;
        this.principalBuilder = principalBuilder;
        this.configs = configs;

    }

    @Override
    public void authenticate() throws IOException {
        if (jaasContext != null) {
            try {
                loginManager = LoginManager.acquireLoginManager(jaasContext, transportLayer, configs);
            } catch (LoginException e) {
                throw new IOException(e.getMessage(), e);
            }
            this.subject = loginManager.subject();
        }
        complete = true;
    }

    @Override
    public Principal principal() throws KafkaException {
        if (subject != null && subject.getPrincipals().size() > 0){
            return subject.getPrincipals().iterator().next();
        } else if (principalBuilder != null) {
            return principalBuilder.buildPrincipal(transportLayer, this);
        } else {
            throw new KafkaException("Could not build principal");
        }
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void close() throws IOException {
        if (this.loginManager != null) {
            this.loginManager.release();
        }
    }

    @Override
    public void configure(TransportLayer transportLayer, PrincipalBuilder principalBuilder, Map<String, ?> configs) {
        // TODO Auto-generated method stub

    }

}
