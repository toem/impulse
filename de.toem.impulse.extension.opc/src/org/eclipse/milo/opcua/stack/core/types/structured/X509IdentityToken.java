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
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class X509IdentityToken extends UserIdentityToken {

    public static final NodeId TypeId = Identifiers.X509IdentityToken;
    public static final NodeId BinaryEncodingId = Identifiers.X509IdentityToken_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.X509IdentityToken_Encoding_DefaultXml;

    protected final ByteString certificateData;

    public X509IdentityToken() {
        super(null);
        this.certificateData = null;
    }

    public X509IdentityToken(String policyId, ByteString certificateData) {
        super(policyId);
        this.certificateData = certificateData;
    }

    public ByteString getCertificateData() { return certificateData; }

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
            .add("CertificateData", certificateData)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<X509IdentityToken> {

        @Override
        public Class<X509IdentityToken> getType() {
            return X509IdentityToken.class;
        }

        @Override
        public X509IdentityToken decode(UaDecoder decoder) throws UaSerializationException {
            String policyId = decoder.readString("PolicyId");
            ByteString certificateData = decoder.readByteString("CertificateData");

            return new X509IdentityToken(policyId, certificateData);
        }

        @Override
        public void encode(X509IdentityToken value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeString("PolicyId", value.policyId);
            encoder.writeByteString("CertificateData", value.certificateData);
        }
    }

}
