/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.TwoStateVariableType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface AlarmConditionType extends AcknowledgeableConditionType {
    QualifiedProperty<NodeId> INPUT_NODE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "InputNode",
        NodeId.parse("ns=0;i=17"),
        ValueRanks.Scalar,
        NodeId.class
    );

    QualifiedProperty<Boolean> SUPPRESSED_OR_SHELVED = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "SuppressedOrShelved",
        NodeId.parse("ns=0;i=1"),
        ValueRanks.Scalar,
        Boolean.class
    );

    QualifiedProperty<Double> MAX_TIME_SHELVED = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "MaxTimeShelved",
        NodeId.parse("ns=0;i=290"),
        ValueRanks.Scalar,
        Double.class
    );

    CompletableFuture<? extends PropertyType> getInputNodeNode();

    CompletableFuture<NodeId> getInputNode();

    CompletableFuture<StatusCode> setInputNode(NodeId value);

    CompletableFuture<? extends PropertyType> getSuppressedOrShelvedNode();

    CompletableFuture<Boolean> getSuppressedOrShelved();

    CompletableFuture<StatusCode> setSuppressedOrShelved(Boolean value);

    CompletableFuture<? extends PropertyType> getMaxTimeShelvedNode();

    CompletableFuture<Double> getMaxTimeShelved();

    CompletableFuture<StatusCode> setMaxTimeShelved(Double value);

    CompletableFuture<? extends TwoStateVariableType> getEnabledStateNode();

    CompletableFuture<LocalizedText> getEnabledState();

    CompletableFuture<StatusCode> setEnabledState(LocalizedText value);

    CompletableFuture<? extends TwoStateVariableType> getActiveStateNode();

    CompletableFuture<LocalizedText> getActiveState();

    CompletableFuture<StatusCode> setActiveState(LocalizedText value);

    CompletableFuture<? extends TwoStateVariableType> getSuppressedStateNode();

    CompletableFuture<LocalizedText> getSuppressedState();

    CompletableFuture<StatusCode> setSuppressedState(LocalizedText value);

    CompletableFuture<? extends ShelvedStateMachineType> getShelvingStateNode();
}
