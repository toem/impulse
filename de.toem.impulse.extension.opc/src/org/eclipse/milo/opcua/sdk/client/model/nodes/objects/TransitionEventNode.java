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
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.StateVariableNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.TransitionVariableNode;
import org.eclipse.milo.opcua.sdk.client.model.types.objects.TransitionEventType;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public class TransitionEventNode extends BaseEventNode implements TransitionEventType {
    public TransitionEventNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    public CompletableFuture<TransitionVariableNode> getTransitionNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "Transition").thenApply(TransitionVariableNode.class::cast);
    }

    public CompletableFuture<LocalizedText> getTransition() {
        return getTransitionNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, LocalizedText.class));
    }

    public CompletableFuture<StatusCode> setTransition(LocalizedText value) {
        return getTransitionNode().thenCompose(node -> node.setValue(value));
    }

    public CompletableFuture<StateVariableNode> getFromStateNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "FromState").thenApply(StateVariableNode.class::cast);
    }

    public CompletableFuture<LocalizedText> getFromState() {
        return getFromStateNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, LocalizedText.class));
    }

    public CompletableFuture<StatusCode> setFromState(LocalizedText value) {
        return getFromStateNode().thenCompose(node -> node.setValue(value));
    }

    public CompletableFuture<StateVariableNode> getToStateNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "ToState").thenApply(StateVariableNode.class::cast);
    }

    public CompletableFuture<LocalizedText> getToState() {
        return getToStateNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, LocalizedText.class));
    }

    public CompletableFuture<StatusCode> setToState(LocalizedText value) {
        return getToStateNode().thenCompose(node -> node.setValue(value));
    }
}
