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
import com.digitalpetri.opcua.stack.core.serialization.UaRequestMessage;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("ActivateSessionRequest")
public class ActivateSessionRequest implements UaRequestMessage {

    public static final NodeId TypeId = Identifiers.ActivateSessionRequest;
    public static final NodeId BinaryEncodingId = Identifiers.ActivateSessionRequest_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ActivateSessionRequest_Encoding_DefaultXml;

    protected final RequestHeader _requestHeader;
    protected final SignatureData _clientSignature;
    protected final SignedSoftwareCertificate[] _clientSoftwareCertificates;
    protected final String[] _localeIds;
    protected final ExtensionObject _userIdentityToken;
    protected final SignatureData _userTokenSignature;

    public ActivateSessionRequest() {
        this._requestHeader = null;
        this._clientSignature = null;
        this._clientSoftwareCertificates = null;
        this._localeIds = null;
        this._userIdentityToken = null;
        this._userTokenSignature = null;
    }

    public ActivateSessionRequest(RequestHeader _requestHeader, SignatureData _clientSignature, SignedSoftwareCertificate[] _clientSoftwareCertificates, String[] _localeIds, ExtensionObject _userIdentityToken, SignatureData _userTokenSignature) {
        this._requestHeader = _requestHeader;
        this._clientSignature = _clientSignature;
        this._clientSoftwareCertificates = _clientSoftwareCertificates;
        this._localeIds = _localeIds;
        this._userIdentityToken = _userIdentityToken;
        this._userTokenSignature = _userTokenSignature;
    }

    public RequestHeader getRequestHeader() { return _requestHeader; }

    public SignatureData getClientSignature() { return _clientSignature; }

    public SignedSoftwareCertificate[] getClientSoftwareCertificates() { return _clientSoftwareCertificates; }

    public String[] getLocaleIds() { return _localeIds; }

    public ExtensionObject getUserIdentityToken() { return _userIdentityToken; }

    public SignatureData getUserTokenSignature() { return _userTokenSignature; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(ActivateSessionRequest activateSessionRequest, UaEncoder encoder) {
        encoder.encodeSerializable("RequestHeader", activateSessionRequest._requestHeader != null ? activateSessionRequest._requestHeader : new RequestHeader());
        encoder.encodeSerializable("ClientSignature", activateSessionRequest._clientSignature != null ? activateSessionRequest._clientSignature : new SignatureData());
        encoder.encodeArray("ClientSoftwareCertificates", activateSessionRequest._clientSoftwareCertificates, encoder::encodeSerializable);
        encoder.encodeArray("LocaleIds", activateSessionRequest._localeIds, encoder::encodeString);
        encoder.encodeExtensionObject("UserIdentityToken", activateSessionRequest._userIdentityToken);
        encoder.encodeSerializable("UserTokenSignature", activateSessionRequest._userTokenSignature != null ? activateSessionRequest._userTokenSignature : new SignatureData());
    }

    public static ActivateSessionRequest decode(UaDecoder decoder) {
        RequestHeader _requestHeader = decoder.decodeSerializable("RequestHeader", RequestHeader.class);
        SignatureData _clientSignature = decoder.decodeSerializable("ClientSignature", SignatureData.class);
        SignedSoftwareCertificate[] _clientSoftwareCertificates = decoder.decodeArray("ClientSoftwareCertificates", decoder::decodeSerializable, SignedSoftwareCertificate.class);
        String[] _localeIds = decoder.decodeArray("LocaleIds", decoder::decodeString, String.class);
        ExtensionObject _userIdentityToken = decoder.decodeExtensionObject("UserIdentityToken");
        SignatureData _userTokenSignature = decoder.decodeSerializable("UserTokenSignature", SignatureData.class);

        return new ActivateSessionRequest(_requestHeader, _clientSignature, _clientSoftwareCertificates, _localeIds, _userIdentityToken, _userTokenSignature);
    }

    static {
        DelegateRegistry.registerEncoder(ActivateSessionRequest::encode, ActivateSessionRequest.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ActivateSessionRequest::decode, ActivateSessionRequest.class, BinaryEncodingId, XmlEncodingId);
    }

}
