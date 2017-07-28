/*
 * Copyright 2016 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.opcua.sdk.client.api.config;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.digitalpetri.opcua.sdk.client.api.identity.AnonymousProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.stack.client.config.UaTcpStackClientConfig;
import com.digitalpetri.opcua.stack.client.config.UaTcpStackClientConfigBuilder;
import com.digitalpetri.opcua.stack.core.channel.ChannelConfig;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class OpcUaClientConfigBuilder extends UaTcpStackClientConfigBuilder {

    private Supplier<String> sessionName;

    private UInteger sessionTimeout = uint(120000);
    private UInteger maxResponseMessageSize = uint(0);
    private UInteger requestTimeout = uint(60000);
    private UInteger maxPendingPublishRequests = uint(UInteger.MAX_VALUE);
    private IdentityProvider identityProvider = new AnonymousProvider();

    public OpcUaClientConfigBuilder setSessionName(Supplier<String> sessionName) {
        this.sessionName = sessionName;
        return this;
    }

    public OpcUaClientConfigBuilder setSessionTimeout(UInteger sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public OpcUaClientConfigBuilder setMaxResponseMessageSize(UInteger maxResponseMessageSize) {
        this.maxResponseMessageSize = maxResponseMessageSize;
        return this;
    }

    public OpcUaClientConfigBuilder setMaxPendingPublishRequests(UInteger maxPendingPublishRequests) {
        this.maxPendingPublishRequests = maxPendingPublishRequests;
        return this;
    }

    public OpcUaClientConfigBuilder setRequestTimeout(UInteger requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public OpcUaClientConfigBuilder setIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setEndpointUrl(String endpointUrl) {
        super.setEndpointUrl(endpointUrl);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setEndpoint(EndpointDescription endpoint) {
        super.setEndpoint(endpoint);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setKeyPair(KeyPair keyPair) {
        super.setKeyPair(keyPair);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setCertificate(X509Certificate certificate) {
        super.setCertificate(certificate);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setApplicationName(LocalizedText applicationName) {
        super.setApplicationName(applicationName);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setApplicationUri(String applicationUri) {
        super.setApplicationUri(applicationUri);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setProductUri(String productUri) {
        super.setProductUri(productUri);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setChannelConfig(ChannelConfig channelConfig) {
        super.setChannelConfig(channelConfig);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setChannelLifetime(UInteger channelLifetime) {
        super.setChannelLifetime(channelLifetime);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setExecutor(ExecutorService executor) {
        super.setExecutor(executor);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setEventLoop(NioEventLoopGroup eventLoop) {
        super.setEventLoop(eventLoop);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setWheelTimer(HashedWheelTimer wheelTimer) {
        super.setWheelTimer(wheelTimer);
        return this;
    }

    @Override
    public OpcUaClientConfigBuilder setSecureChannelReauthenticationEnabled(boolean secureChannelReauthenticationEnabled) {
        super.setSecureChannelReauthenticationEnabled(secureChannelReauthenticationEnabled);
        return this;
    }

    public OpcUaClientConfig build() {
        UaTcpStackClientConfig stackClientConfig = super.build();

        if (sessionName == null) {
            sessionName = () -> String.format("UaSession:%s:%s",
                    stackClientConfig.getApplicationName().getText(),
                    System.currentTimeMillis());
        }

        return new OpcUaClientConfigImpl(
                stackClientConfig,
                sessionName,
                sessionTimeout,
                maxResponseMessageSize,
                maxPendingPublishRequests,
                requestTimeout,
                identityProvider);
    }

    public static class OpcUaClientConfigImpl implements OpcUaClientConfig {

        private final UaTcpStackClientConfig stackClientConfig;
        private final Supplier<String> sessionName;
        private final UInteger sessionTimeout;
        private final UInteger maxResponseMessageSize;
        private final UInteger maxPendingPublishRequests;
        private final UInteger requestTimeout;
        private final IdentityProvider identityProvider;

        public OpcUaClientConfigImpl(UaTcpStackClientConfig stackClientConfig,
                                     Supplier<String> sessionName,
                                     UInteger sessionTimeout,
                                     UInteger maxResponseMessageSize,
                                     UInteger maxPendingPublishRequests,
                                     UInteger requestTimeout,
                                     IdentityProvider identityProvider) {

            this.stackClientConfig = stackClientConfig;
            this.sessionName = sessionName;
            this.sessionTimeout = sessionTimeout;
            this.maxResponseMessageSize = maxResponseMessageSize;
            this.maxPendingPublishRequests = maxPendingPublishRequests;
            this.requestTimeout = requestTimeout;
            this.identityProvider = identityProvider;
        }

        @Override
        public Supplier<String> getSessionName() {
            return sessionName;
        }

        @Override
        public UInteger getSessionTimeout() {
            return sessionTimeout;
        }

        @Override
        public UInteger getMaxResponseMessageSize() {
            return maxResponseMessageSize;
        }

        @Override
        public UInteger getMaxPendingPublishRequests() {
            return maxPendingPublishRequests;
        }

        @Override
        public UInteger getRequestTimeout() {
            return requestTimeout;
        }

        @Override
        public IdentityProvider getIdentityProvider() {
            return identityProvider;
        }

        @Override
        public Optional<String> getEndpointUrl() {
            return stackClientConfig.getEndpointUrl();
        }

        @Override
        public Optional<EndpointDescription> getEndpoint() {
            return stackClientConfig.getEndpoint();
        }

        @Override
        public Optional<KeyPair> getKeyPair() {
            return stackClientConfig.getKeyPair();
        }

        @Override
        public Optional<X509Certificate> getCertificate() {
            return stackClientConfig.getCertificate();
        }

        @Override
        public LocalizedText getApplicationName() {
            return stackClientConfig.getApplicationName();
        }

        @Override
        public String getApplicationUri() {
            return stackClientConfig.getApplicationUri();
        }

        @Override
        public String getProductUri() {
            return stackClientConfig.getProductUri();
        }

        @Override
        public ChannelConfig getChannelConfig() {
            return stackClientConfig.getChannelConfig();
        }

        @Override
        public UInteger getChannelLifetime() {
            return stackClientConfig.getChannelLifetime();
        }

        @Override
        public ExecutorService getExecutor() {
            return stackClientConfig.getExecutor();
        }

        @Override
        public NioEventLoopGroup getEventLoop() {
            return stackClientConfig.getEventLoop();
        }

        @Override
        public HashedWheelTimer getWheelTimer() {
            return stackClientConfig.getWheelTimer();
        }

        @Override
        public boolean isSecureChannelReauthenticationEnabled() {
            return stackClientConfig.isSecureChannelReauthenticationEnabled();
        }

    }

}
