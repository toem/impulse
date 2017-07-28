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
import com.digitalpetri.opcua.stack.core.serialization.UaStructure;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.enumerated.MessageSecurityMode;

@UaDataType("EndpointDescription")
public class EndpointDescription implements UaStructure {

    public static final NodeId TypeId = Identifiers.EndpointDescription;
    public static final NodeId BinaryEncodingId = Identifiers.EndpointDescription_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.EndpointDescription_Encoding_DefaultXml;

    protected final String _endpointUrl;
    protected final ApplicationDescription _server;
    protected final ByteString _serverCertificate;
    protected final MessageSecurityMode _securityMode;
    protected final String _securityPolicyUri;
    protected final UserTokenPolicy[] _userIdentityTokens;
    protected final String _transportProfileUri;
    protected final UByte _securityLevel;

    public EndpointDescription() {
        this._endpointUrl = null;
        this._server = null;
        this._serverCertificate = null;
        this._securityMode = null;
        this._securityPolicyUri = null;
        this._userIdentityTokens = null;
        this._transportProfileUri = null;
        this._securityLevel = null;
    }

    public EndpointDescription(String _endpointUrl, ApplicationDescription _server, ByteString _serverCertificate, MessageSecurityMode _securityMode, String _securityPolicyUri, UserTokenPolicy[] _userIdentityTokens, String _transportProfileUri, UByte _securityLevel) {
        this._endpointUrl = _endpointUrl;
        this._server = _server;
        this._serverCertificate = _serverCertificate;
        this._securityMode = _securityMode;
        this._securityPolicyUri = _securityPolicyUri;
        this._userIdentityTokens = _userIdentityTokens;
        this._transportProfileUri = _transportProfileUri;
        this._securityLevel = _securityLevel;
    }

    public String getEndpointUrl() { return _endpointUrl; }

    public ApplicationDescription getServer() { return _server; }

    public ByteString getServerCertificate() { return _serverCertificate; }

    public MessageSecurityMode getSecurityMode() { return _securityMode; }

    public String getSecurityPolicyUri() { return _securityPolicyUri; }

    public UserTokenPolicy[] getUserIdentityTokens() { return _userIdentityTokens; }

    public String getTransportProfileUri() { return _transportProfileUri; }

    public UByte getSecurityLevel() { return _securityLevel; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(EndpointDescription endpointDescription, UaEncoder encoder) {
        encoder.encodeString("EndpointUrl", endpointDescription._endpointUrl);
        encoder.encodeSerializable("Server", endpointDescription._server != null ? endpointDescription._server : new ApplicationDescription());
        encoder.encodeByteString("ServerCertificate", endpointDescription._serverCertificate);
        encoder.encodeEnumeration("SecurityMode", endpointDescription._securityMode);
        encoder.encodeString("SecurityPolicyUri", endpointDescription._securityPolicyUri);
        encoder.encodeArray("UserIdentityTokens", endpointDescription._userIdentityTokens, encoder::encodeSerializable);
        encoder.encodeString("TransportProfileUri", endpointDescription._transportProfileUri);
        encoder.encodeByte("SecurityLevel", endpointDescription._securityLevel);
    }

    public static EndpointDescription decode(UaDecoder decoder) {
        String _endpointUrl = decoder.decodeString("EndpointUrl");
        ApplicationDescription _server = decoder.decodeSerializable("Server", ApplicationDescription.class);
        ByteString _serverCertificate = decoder.decodeByteString("ServerCertificate");
        MessageSecurityMode _securityMode = decoder.decodeEnumeration("SecurityMode", MessageSecurityMode.class);
        String _securityPolicyUri = decoder.decodeString("SecurityPolicyUri");
        UserTokenPolicy[] _userIdentityTokens = decoder.decodeArray("UserIdentityTokens", decoder::decodeSerializable, UserTokenPolicy.class);
        String _transportProfileUri = decoder.decodeString("TransportProfileUri");
        UByte _securityLevel = decoder.decodeByte("SecurityLevel");

        return new EndpointDescription(_endpointUrl, _server, _serverCertificate, _securityMode, _securityPolicyUri, _userIdentityTokens, _transportProfileUri, _securityLevel);
    }

    static {
        DelegateRegistry.registerEncoder(EndpointDescription::encode, EndpointDescription.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(EndpointDescription::decode, EndpointDescription.class, BinaryEncodingId, XmlEncodingId);
    }

}
