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

public class AnonymousIdentityToken extends UserIdentityToken {

    public static final NodeId TypeId = Identifiers.AnonymousIdentityToken;
    public static final NodeId BinaryEncodingId = Identifiers.AnonymousIdentityToken_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.AnonymousIdentityToken_Encoding_DefaultXml;


    public AnonymousIdentityToken() {
        super(null);
    }

    public AnonymousIdentityToken(String policyId) {
        super(policyId);
    }


    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("PolicyId", policyId)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<AnonymousIdentityToken> {

        @Override
        public Class<AnonymousIdentityToken> getType() {
            return AnonymousIdentityToken.class;
        }

        @Override
        public AnonymousIdentityToken decode(UaDecoder decoder) throws UaSerializationException {
            String policyId = decoder.readString("PolicyId");

            return new AnonymousIdentityToken(policyId);
        }

        @Override
        public void encode(AnonymousIdentityToken value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeString("PolicyId", value.policyId);
        }
    }

}
