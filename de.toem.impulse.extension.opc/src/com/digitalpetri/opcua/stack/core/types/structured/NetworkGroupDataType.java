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
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("NetworkGroupDataType")
public class NetworkGroupDataType implements UaStructure {

    public static final NodeId TypeId = Identifiers.NetworkGroupDataType;
    public static final NodeId BinaryEncodingId = Identifiers.NetworkGroupDataType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.NetworkGroupDataType_Encoding_DefaultXml;

    protected final String _serverUri;
    protected final EndpointUrlListDataType[] _networkPaths;

    public NetworkGroupDataType() {
        this._serverUri = null;
        this._networkPaths = null;
    }

    public NetworkGroupDataType(String _serverUri, EndpointUrlListDataType[] _networkPaths) {
        this._serverUri = _serverUri;
        this._networkPaths = _networkPaths;
    }

    public String getServerUri() { return _serverUri; }

    public EndpointUrlListDataType[] getNetworkPaths() { return _networkPaths; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(NetworkGroupDataType networkGroupDataType, UaEncoder encoder) {
        encoder.encodeString("ServerUri", networkGroupDataType._serverUri);
        encoder.encodeArray("NetworkPaths", networkGroupDataType._networkPaths, encoder::encodeSerializable);
    }

    public static NetworkGroupDataType decode(UaDecoder decoder) {
        String _serverUri = decoder.decodeString("ServerUri");
        EndpointUrlListDataType[] _networkPaths = decoder.decodeArray("NetworkPaths", decoder::decodeSerializable, EndpointUrlListDataType.class);

        return new NetworkGroupDataType(_serverUri, _networkPaths);
    }

    static {
        DelegateRegistry.registerEncoder(NetworkGroupDataType::encode, NetworkGroupDataType.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(NetworkGroupDataType::decode, NetworkGroupDataType.class, BinaryEncodingId, XmlEncodingId);
    }

}
