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
import org.eclipse.milo.opcua.sdk.client.model.types.variables.SamplingIntervalDiagnosticsType;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

public class SamplingIntervalDiagnosticsNode extends BaseDataVariableNode implements SamplingIntervalDiagnosticsType {
    public SamplingIntervalDiagnosticsNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    public CompletableFuture<BaseDataVariableNode> getSamplingIntervalNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "SamplingInterval").thenApply(BaseDataVariableNode.class::cast);
    }

    public CompletableFuture<Double> getSamplingInterval() {
        return getSamplingIntervalNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, Double.class));
    }

    public CompletableFuture<StatusCode> setSamplingInterval(Double value) {
        return getSamplingIntervalNode().thenCompose(node -> node.setValue(value));
    }

    public CompletableFuture<BaseDataVariableNode> getSampledMonitoredItemsCountNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "SampledMonitoredItemsCount").thenApply(BaseDataVariableNode.class::cast);
    }

    public CompletableFuture<UInteger> getSampledMonitoredItemsCount() {
        return getSampledMonitoredItemsCountNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, UInteger.class));
    }

    public CompletableFuture<StatusCode> setSampledMonitoredItemsCount(UInteger value) {
        return getSampledMonitoredItemsCountNode().thenCompose(node -> node.setValue(value));
    }

    public CompletableFuture<BaseDataVariableNode> getMaxSampledMonitoredItemsCountNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "MaxSampledMonitoredItemsCount").thenApply(BaseDataVariableNode.class::cast);
    }

    public CompletableFuture<UInteger> getMaxSampledMonitoredItemsCount() {
        return getMaxSampledMonitoredItemsCountNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, UInteger.class));
    }

    public CompletableFuture<StatusCode> setMaxSampledMonitoredItemsCount(UInteger value) {
        return getMaxSampledMonitoredItemsCountNode().thenCompose(node -> node.setValue(value));
    }

    public CompletableFuture<BaseDataVariableNode> getDisabledMonitoredItemsSamplingCountNode() {
        return getVariableComponent("http://opcfoundation.org/UA/", "DisabledMonitoredItemsSamplingCount").thenApply(BaseDataVariableNode.class::cast);
    }

    public CompletableFuture<UInteger> getDisabledMonitoredItemsSamplingCount() {
        return getDisabledMonitoredItemsSamplingCountNode().thenCompose(UaVariableNode::getValue).thenApply(o -> cast(o, UInteger.class));
    }

    public CompletableFuture<StatusCode> setDisabledMonitoredItemsSamplingCount(UInteger value) {
        return getDisabledMonitoredItemsSamplingCountNode().thenCompose(node -> node.setValue(value));
    }
}
