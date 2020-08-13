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
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class SignedSoftwareCertificate implements UaStructure {

    public static final NodeId TypeId = Identifiers.SignedSoftwareCertificate;
    public static final NodeId BinaryEncodingId = Identifiers.SignedSoftwareCertificate_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.SignedSoftwareCertificate_Encoding_DefaultXml;

    protected final ByteString certificateData;
    protected final ByteString signature;

    public SignedSoftwareCertificate() {
        this.certificateData = null;
        this.signature = null;
    }

    public SignedSoftwareCertificate(ByteString certificateData, ByteString signature) {
        this.certificateData = certificateData;
        this.signature = signature;
    }

    public ByteString getCertificateData() { return certificateData; }

    public ByteString getSignature() { return signature; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("CertificateData", certificateData)
            .add("Signature", signature)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<SignedSoftwareCertificate> {

        @Override
        public Class<SignedSoftwareCertificate> getType() {
            return SignedSoftwareCertificate.class;
        }

        @Override
        public SignedSoftwareCertificate decode(UaDecoder decoder) throws UaSerializationException {
            ByteString certificateData = decoder.readByteString("CertificateData");
            ByteString signature = decoder.readByteString("Signature");

            return new SignedSoftwareCertificate(certificateData, signature);
        }

        @Override
        public void encode(SignedSoftwareCertificate value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeByteString("CertificateData", value.certificateData);
            encoder.writeByteString("Signature", value.signature);
        }
    }

}
