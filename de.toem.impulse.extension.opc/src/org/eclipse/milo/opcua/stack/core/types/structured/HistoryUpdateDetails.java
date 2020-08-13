/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.structured;

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class HistoryUpdateDetails implements UaStructure {

    public static final NodeId TypeId = Identifiers.HistoryUpdateDetails;
    public static final NodeId BinaryEncodingId = Identifiers.HistoryUpdateDetails_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.HistoryUpdateDetails_Encoding_DefaultXml;

    protected final NodeId nodeId;

    public HistoryUpdateDetails() {
        this.nodeId = null;
    }

    public HistoryUpdateDetails(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public NodeId getNodeId() { return nodeId; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("NodeId", nodeId)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<HistoryUpdateDetails> {

        @Override
        public Class<HistoryUpdateDetails> getType() {
            return HistoryUpdateDetails.class;
        }

        @Override
        public HistoryUpdateDetails decode(UaDecoder decoder) throws UaSerializationException {
            NodeId nodeId = decoder.readNodeId("NodeId");

            return new HistoryUpdateDetails(nodeId);
        }

        @Override
        public void encode(HistoryUpdateDetails value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeNodeId("NodeId", value.nodeId);
        }
    }

}
