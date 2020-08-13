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
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.PerformUpdateType;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryEventFieldList;

public interface AuditHistoryEventUpdateEventType extends AuditHistoryUpdateEventType {
    QualifiedProperty<NodeId> UPDATED_NODE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "UpdatedNode",
        NodeId.parse("ns=0;i=17"),
        ValueRanks.Scalar,
        NodeId.class
    );

    QualifiedProperty<PerformUpdateType> PERFORM_INSERT_REPLACE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "PerformInsertReplace",
        NodeId.parse("ns=0;i=11293"),
        ValueRanks.Scalar,
        PerformUpdateType.class
    );

    QualifiedProperty<EventFilter> FILTER = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Filter",
        NodeId.parse("ns=0;i=725"),
        ValueRanks.Scalar,
        EventFilter.class
    );

    QualifiedProperty<HistoryEventFieldList[]> NEW_VALUES = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "NewValues",
        NodeId.parse("ns=0;i=920"),
        ValueRanks.OneDimension,
        HistoryEventFieldList[].class
    );

    QualifiedProperty<HistoryEventFieldList[]> OLD_VALUES = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "OldValues",
        NodeId.parse("ns=0;i=920"),
        ValueRanks.OneDimension,
        HistoryEventFieldList[].class
    );

    CompletableFuture<? extends PropertyType> getUpdatedNodeNode();

    CompletableFuture<NodeId> getUpdatedNode();

    CompletableFuture<StatusCode> setUpdatedNode(NodeId value);

    CompletableFuture<? extends PropertyType> getPerformInsertReplaceNode();

    CompletableFuture<PerformUpdateType> getPerformInsertReplace();

    CompletableFuture<StatusCode> setPerformInsertReplace(PerformUpdateType value);

    CompletableFuture<? extends PropertyType> getFilterNode();

    CompletableFuture<EventFilter> getFilter();

    CompletableFuture<StatusCode> setFilter(EventFilter value);

    CompletableFuture<? extends PropertyType> getNewValuesNode();

    CompletableFuture<HistoryEventFieldList[]> getNewValues();

    CompletableFuture<StatusCode> setNewValues(HistoryEventFieldList[] value);

    CompletableFuture<? extends PropertyType> getOldValuesNode();

    CompletableFuture<HistoryEventFieldList[]> getOldValues();

    CompletableFuture<StatusCode> setOldValues(HistoryEventFieldList[] value);
}
