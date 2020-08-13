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

public class Range implements UaStructure {

    public static final NodeId TypeId = Identifiers.Range;
    public static final NodeId BinaryEncodingId = Identifiers.Range_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.Range_Encoding_DefaultXml;

    protected final Double low;
    protected final Double high;

    public Range() {
        this.low = null;
        this.high = null;
    }

    public Range(Double low, Double high) {
        this.low = low;
        this.high = high;
    }

    public Double getLow() { return low; }

    public Double getHigh() { return high; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Low", low)
            .add("High", high)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<Range> {

        @Override
        public Class<Range> getType() {
            return Range.class;
        }

        @Override
        public Range decode(UaDecoder decoder) throws UaSerializationException {
            Double low = decoder.readDouble("Low");
            Double high = decoder.readDouble("High");

            return new Range(low, high);
        }

        @Override
        public void encode(Range value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeDouble("Low", value.low);
            encoder.writeDouble("High", value.high);
        }
    }

}
