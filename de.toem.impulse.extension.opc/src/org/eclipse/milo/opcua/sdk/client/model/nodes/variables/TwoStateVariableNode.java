/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.model.nodes.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.TwoStateVariableType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public class TwoStateVariableNode extends StateVariableNode implements TwoStateVariableType {
    public TwoStateVariableNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    public CompletableFuture<PropertyNode> getIdNode() {
        return getPropertyNode(TwoStateVariableType.ID);
    }

    public CompletableFuture<Boolean> getId() {
        return getProperty(TwoStateVariableType.ID);
    }

    public CompletableFuture<StatusCode> setId(Boolean value) {
        return setProperty(TwoStateVariableType.ID, value);
    }

    public CompletableFuture<PropertyNode> getTransitionTimeNode() {
        return getPropertyNode(TwoStateVariableType.TRANSITION_TIME);
    }

    public CompletableFuture<DateTime> getTransitionTime() {
        return getProperty(TwoStateVariableType.TRANSITION_TIME);
    }

    public CompletableFuture<StatusCode> setTransitionTime(DateTime value) {
        return setProperty(TwoStateVariableType.TRANSITION_TIME, value);
    }

    public CompletableFuture<PropertyNode> getEffectiveTransitionTimeNode() {
        return getPropertyNode(TwoStateVariableType.EFFECTIVE_TRANSITION_TIME);
    }

    public CompletableFuture<DateTime> getEffectiveTransitionTime() {
        return getProperty(TwoStateVariableType.EFFECTIVE_TRANSITION_TIME);
    }

    public CompletableFuture<StatusCode> setEffectiveTransitionTime(DateTime value) {
        return setProperty(TwoStateVariableType.EFFECTIVE_TRANSITION_TIME, value);
    }

    public CompletableFuture<PropertyNode> getTrueStateNode() {
        return getPropertyNode(TwoStateVariableType.TRUE_STATE);
    }

    public CompletableFuture<LocalizedText> getTrueState() {
        return getProperty(TwoStateVariableType.TRUE_STATE);
    }

    public CompletableFuture<StatusCode> setTrueState(LocalizedText value) {
        return setProperty(TwoStateVariableType.TRUE_STATE, value);
    }

    public CompletableFuture<PropertyNode> getFalseStateNode() {
        return getPropertyNode(TwoStateVariableType.FALSE_STATE);
    }

    public CompletableFuture<LocalizedText> getFalseState() {
        return getProperty(TwoStateVariableType.FALSE_STATE);
    }

    public CompletableFuture<StatusCode> setFalseState(LocalizedText value) {
        return setProperty(TwoStateVariableType.FALSE_STATE, value);
    }
}
