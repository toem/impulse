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
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class LiteralOperand extends FilterOperand {

    public static final NodeId TypeId = Identifiers.LiteralOperand;
    public static final NodeId BinaryEncodingId = Identifiers.LiteralOperand_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.LiteralOperand_Encoding_DefaultXml;

    protected final Variant value;

    public LiteralOperand() {
        super();
        this.value = null;
    }

    public LiteralOperand(Variant value) {
        super();
        this.value = value;
    }

    public Variant getValue() { return value; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Value", value)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<LiteralOperand> {

        @Override
        public Class<LiteralOperand> getType() {
            return LiteralOperand.class;
        }

        @Override
        public LiteralOperand decode(UaDecoder decoder) throws UaSerializationException {
            Variant value = decoder.readVariant("Value");

            return new LiteralOperand(value);
        }

        @Override
        public void encode(LiteralOperand value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeVariant("Value", value.value);
        }
    }

}
