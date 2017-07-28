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
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("GetEndpointsResponse")
public class GetEndpointsResponse implements UaResponseMessage {

    public static final NodeId TypeId = Identifiers.GetEndpointsResponse;
    public static final NodeId BinaryEncodingId = Identifiers.GetEndpointsResponse_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.GetEndpointsResponse_Encoding_DefaultXml;

    protected final ResponseHeader _responseHeader;
    protected final EndpointDescription[] _endpoints;

    public GetEndpointsResponse() {
        this._responseHeader = null;
        this._endpoints = null;
    }

    public GetEndpointsResponse(ResponseHeader _responseHeader, EndpointDescription[] _endpoints) {
        this._responseHeader = _responseHeader;
        this._endpoints = _endpoints;
    }

    public ResponseHeader getResponseHeader() { return _responseHeader; }

    public EndpointDescription[] getEndpoints() { return _endpoints; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(GetEndpointsResponse getEndpointsResponse, UaEncoder encoder) {
        encoder.encodeSerializable("ResponseHeader", getEndpointsResponse._responseHeader != null ? getEndpointsResponse._responseHeader : new ResponseHeader());
        encoder.encodeArray("Endpoints", getEndpointsResponse._endpoints, encoder::encodeSerializable);
    }

    public static GetEndpointsResponse decode(UaDecoder decoder) {
        ResponseHeader _responseHeader = decoder.decodeSerializable("ResponseHeader", ResponseHeader.class);
        EndpointDescription[] _endpoints = decoder.decodeArray("Endpoints", decoder::decodeSerializable, EndpointDescription.class);

        return new GetEndpointsResponse(_responseHeader, _endpoints);
    }

    static {
        DelegateRegistry.registerEncoder(GetEndpointsResponse::encode, GetEndpointsResponse.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(GetEndpointsResponse::decode, GetEndpointsResponse.class, BinaryEncodingId, XmlEncodingId);
    }

}
