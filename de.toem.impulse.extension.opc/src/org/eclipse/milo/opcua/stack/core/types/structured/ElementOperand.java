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
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

public class ElementOperand extends FilterOperand {

    public static final NodeId TypeId = Identifiers.ElementOperand;
    public static final NodeId BinaryEncodingId = Identifiers.ElementOperand_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ElementOperand_Encoding_DefaultXml;

    protected final UInteger index;

    public ElementOperand() {
        super();
        this.index = null;
    }

    public ElementOperand(UInteger index) {
        super();
        this.index = index;
    }

    public UInteger getIndex() { return index; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Index", index)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<ElementOperand> {

        @Override
        public Class<ElementOperand> getType() {
            return ElementOperand.class;
        }

        @Override
        public ElementOperand decode(UaDecoder decoder) throws UaSerializationException {
            UInteger index = decoder.readUInt32("Index");

            return new ElementOperand(index);
        }

        @Override
        public void encode(ElementOperand value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeUInt32("Index", value.index);
        }
    }

}
