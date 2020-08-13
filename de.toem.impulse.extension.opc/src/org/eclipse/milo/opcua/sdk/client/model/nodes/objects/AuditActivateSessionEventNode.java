/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.model.nodes.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.PropertyNode;
import org.eclipse.milo.opcua.sdk.client.model.types.objects.AuditActivateSessionEventType;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.SignedSoftwareCertificate;
import org.eclipse.milo.opcua.stack.core.types.structured.UserIdentityToken;

public class AuditActivateSessionEventNode extends AuditSessionEventNode implements AuditActivateSessionEventType {
    public AuditActivateSessionEventNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    public CompletableFuture<PropertyNode> getClientSoftwareCertificatesNode() {
        return getPropertyNode(AuditActivateSessionEventType.CLIENT_SOFTWARE_CERTIFICATES);
    }

    public CompletableFuture<SignedSoftwareCertificate[]> getClientSoftwareCertificates() {
        return getProperty(AuditActivateSessionEventType.CLIENT_SOFTWARE_CERTIFICATES);
    }

    public CompletableFuture<StatusCode> setClientSoftwareCertificates(SignedSoftwareCertificate[] value) {
        return setProperty(AuditActivateSessionEventType.CLIENT_SOFTWARE_CERTIFICATES, value);
    }

    public CompletableFuture<PropertyNode> getUserIdentityTokenNode() {
        return getPropertyNode(AuditActivateSessionEventType.USER_IDENTITY_TOKEN);
    }

    public CompletableFuture<UserIdentityToken> getUserIdentityToken() {
        return getProperty(AuditActivateSessionEventType.USER_IDENTITY_TOKEN);
    }

    public CompletableFuture<StatusCode> setUserIdentityToken(UserIdentityToken value) {
        return setProperty(AuditActivateSessionEventType.USER_IDENTITY_TOKEN, value);
    }

    public CompletableFuture<PropertyNode> getSecureChannelIdNode() {
        return getPropertyNode(AuditActivateSessionEventType.SECURE_CHANNEL_ID);
    }

    public CompletableFuture<String> getSecureChannelId() {
        return getProperty(AuditActivateSessionEventType.SECURE_CHANNEL_ID);
    }

    public CompletableFuture<StatusCode> setSecureChannelId(String value) {
        return setProperty(AuditActivateSessionEventType.SECURE_CHANNEL_ID, value);
    }
}
