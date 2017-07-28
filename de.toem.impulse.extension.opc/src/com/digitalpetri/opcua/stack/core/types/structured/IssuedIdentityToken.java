/*
 * Copyright 2015 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.opcua.stack.core.types.structured;

import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.serialization.DelegateRegistry;
import com.digitalpetri.opcua.stack.core.serialization.UaDecoder;
import com.digitalpetri.opcua.stack.core.serialization.UaEncoder;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("IssuedIdentityToken")
public class IssuedIdentityToken extends UserIdentityToken {

    public static final NodeId TypeId = Identifiers.IssuedIdentityToken;
    public static final NodeId BinaryEncodingId = Identifiers.IssuedIdentityToken_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.IssuedIdentityToken_Encoding_DefaultXml;

    protected final ByteString _tokenData;
    protected final String _encryptionAlgorithm;

    public IssuedIdentityToken() {
        super(null);
        this._tokenData = null;
        this._encryptionAlgorithm = null;
    }

    public IssuedIdentityToken(String _policyId, ByteString _tokenData, String _encryptionAlgorithm) {
        super(_policyId);
        this._tokenData = _tokenData;
        this._encryptionAlgorithm = _encryptionAlgorithm;
    }

    public ByteString getTokenData() { return _tokenData; }

    public String getEncryptionAlgorithm() { return _encryptionAlgorithm; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(IssuedIdentityToken issuedIdentityToken, UaEncoder encoder) {
        encoder.encodeString("PolicyId", issuedIdentityToken._policyId);
        encoder.encodeByteString("TokenData", issuedIdentityToken._tokenData);
        encoder.encodeString("EncryptionAlgorithm", issuedIdentityToken._encryptionAlgorithm);
    }

    public static IssuedIdentityToken decode(UaDecoder decoder) {
        String _policyId = decoder.decodeString("PolicyId");
        ByteString _tokenData = decoder.decodeByteString("TokenData");
        String _encryptionAlgorithm = decoder.decodeString("EncryptionAlgorithm");

        return new IssuedIdentityToken(_policyId, _tokenData, _encryptionAlgorithm);
    }

    static {
        DelegateRegistry.registerEncoder(IssuedIdentityToken::encode, IssuedIdentityToken.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(IssuedIdentityToken::decode, IssuedIdentityToken.class, BinaryEncodingId, XmlEncodingId);
    }

}
