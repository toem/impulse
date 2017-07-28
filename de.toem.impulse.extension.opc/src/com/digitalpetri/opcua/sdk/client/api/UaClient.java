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

package com.digitalpetri.opcua.sdk.client.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.client.api.nodes.AddressSpace;
import com.digitalpetri.opcua.sdk.client.api.nodes.NodeCache;
import com.digitalpetri.opcua.sdk.client.api.services.AttributeServices;
import com.digitalpetri.opcua.sdk.client.api.services.MethodServices;
import com.digitalpetri.opcua.sdk.client.api.services.MonitoredItemServices;
import com.digitalpetri.opcua.sdk.client.api.services.SubscriptionServices;
import com.digitalpetri.opcua.sdk.client.api.services.ViewServices;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import com.digitalpetri.opcua.sdk.client.subscriptions.OpcUaSubscriptionManager;
import com.digitalpetri.opcua.stack.core.serialization.UaRequestMessage;
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;

public interface UaClient extends AttributeServices, MethodServices, MonitoredItemServices, SubscriptionServices, ViewServices {

    /**
     * @return the {@link OpcUaClientConfig} for this client.
     */
    OpcUaClientConfig getConfig();

    /**
     * Connect to the configured endpoint.
     *
     * @return a {@link CompletableFuture} holding this client instance.
     */
    CompletableFuture<UaClient> connect();

    /**
     * Disconnect from the configured endpoint.
     *
     * @return a {@link CompletableFuture} holding this client instance.
     */
    CompletableFuture<UaClient> disconnect();

    /**
     * @return a {@link CompletableFuture} holding the {@link UaSession}.
     */
    CompletableFuture<UaSession> getSession();

    /**
     * @return the {@link AddressSpace}.
     */
    AddressSpace getAddressSpace();

    /**
     * @return the {@link NodeCache}.
     */
    NodeCache getNodeCache();

    /**
     * @return the {@link OpcUaSubscriptionManager} for this client.
     */
    UaSubscriptionManager getSubscriptionManager();

    /**
     * Send a {@link UaRequestMessage}.
     *
     * @param request the request to send.
     * @return a {@link CompletableFuture} holding the response.
     */
    <T extends UaResponseMessage> CompletableFuture<T> sendRequest(UaRequestMessage request);

    /**
     * Send multiple {@link UaRequestMessage}s.
     *
     * @param requests the requests to send.
     * @param futures  the {@link CompletableFuture}s to complete when responses arrive.
     */
    void sendRequests(List<? extends UaRequestMessage> requests,
                      List<CompletableFuture<? extends UaResponseMessage>> futures);


}
