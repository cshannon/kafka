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
package org.apache.kafka.common.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.kafka.common.security.JaasContext;
import org.apache.kafka.common.security.auth.PrincipalBuilder;
import org.apache.kafka.common.security.authenticator.SslServerAuthenticator;
import org.apache.kafka.common.security.ssl.SslFactory;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslChannelBuilder implements ChannelBuilder {
    private static final Logger log = LoggerFactory.getLogger(SslChannelBuilder.class);
    private SslFactory sslFactory;
    private final JaasContext jaasContext;
    private PrincipalBuilder principalBuilder;
    private Mode mode;
    private Map<String, ?> configs;

    public SslChannelBuilder(Mode mode) {
        this.mode = mode;
        this.jaasContext = null;
    }

    public SslChannelBuilder(Mode mode, JaasContext jaasContext) {
        this.mode = mode;
        this.jaasContext = jaasContext;
    }

    @Override
    public void configure(Map<String, ?> configs) throws KafkaException {
        try {
            this.configs = configs;
            this.sslFactory = new SslFactory(mode);
            this.sslFactory.configure(this.configs);
            this.principalBuilder = ChannelBuilders.createPrincipalBuilder(configs);
        } catch (Exception e) {
            throw new KafkaException(e);
        }
    }

    @Override
    public KafkaChannel buildChannel(String id, SelectionKey key, int maxReceiveSize) throws KafkaException {
        try {
            SslTransportLayer transportLayer = buildTransportLayer(sslFactory, id, key);
            SslServerAuthenticator authenticator = new SslServerAuthenticator();
            authenticator.configure(transportLayer, this.principalBuilder, this.configs, jaasContext);
            return new KafkaChannel(id, transportLayer, authenticator, maxReceiveSize);
        } catch (Exception e) {
            log.info("Failed to create channel due to ", e);
            throw new KafkaException(e);
        }
    }

    @Override
    public void close() {
        this.principalBuilder.close();
    }

    protected SslTransportLayer buildTransportLayer(SslFactory sslFactory, String id, SelectionKey key)
            throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        return SslTransportLayer.create(id, key, sslFactory.createSslEngine(socketChannel.socket().getInetAddress().getHostName(), socketChannel.socket().getPort()));
    }
}
