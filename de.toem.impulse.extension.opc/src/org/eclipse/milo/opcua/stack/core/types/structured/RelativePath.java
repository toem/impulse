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

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class RelativePath implements UaStructure {

    public static final NodeId TypeId = Identifiers.RelativePath;
    public static final NodeId BinaryEncodingId = Identifiers.RelativePath_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.RelativePath_Encoding_DefaultXml;

    protected final RelativePathElement[] elements;

    public RelativePath() {
        this.elements = null;
    }

    public RelativePath(RelativePathElement[] elements) {
        this.elements = elements;
    }

    @Nullable
    public RelativePathElement[] getElements() { return elements; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Elements", elements)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<RelativePath> {

        @Override
        public Class<RelativePath> getType() {
            return RelativePath.class;
        }

        @Override
        public RelativePath decode(UaDecoder decoder) throws UaSerializationException {
            RelativePathElement[] elements =
                decoder.readBuiltinStructArray(
                    "Elements",
                    RelativePathElement.class
                );

            return new RelativePath(elements);
        }

        @Override
        public void encode(RelativePath value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeBuiltinStructArray(
                "Elements",
                value.elements,
                RelativePathElement.class
            );
        }
    }

}
