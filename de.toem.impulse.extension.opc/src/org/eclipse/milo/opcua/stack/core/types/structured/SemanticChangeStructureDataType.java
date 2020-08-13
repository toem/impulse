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

public class SemanticChangeStructureDataType implements UaStructure {

    public static final NodeId TypeId = Identifiers.SemanticChangeStructureDataType;
    public static final NodeId BinaryEncodingId = Identifiers.SemanticChangeStructureDataType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.SemanticChangeStructureDataType_Encoding_DefaultXml;

    protected final NodeId affected;
    protected final NodeId affectedType;

    public SemanticChangeStructureDataType() {
        this.affected = null;
        this.affectedType = null;
    }

    public SemanticChangeStructureDataType(NodeId affected, NodeId affectedType) {
        this.affected = affected;
        this.affectedType = affectedType;
    }

    public NodeId getAffected() { return affected; }

    public NodeId getAffectedType() { return affectedType; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Affected", affected)
            .add("AffectedType", affectedType)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<SemanticChangeStructureDataType> {

        @Override
        public Class<SemanticChangeStructureDataType> getType() {
            return SemanticChangeStructureDataType.class;
        }

        @Override
        public SemanticChangeStructureDataType decode(UaDecoder decoder) throws UaSerializationException {
            NodeId affected = decoder.readNodeId("Affected");
            NodeId affectedType = decoder.readNodeId("AffectedType");

            return new SemanticChangeStructureDataType(affected, affectedType);
        }

        @Override
        public void encode(SemanticChangeStructureDataType value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeNodeId("Affected", value.affected);
            encoder.writeNodeId("AffectedType", value.affectedType);
        }
    }

}
