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

@UaDataType("CloseSecureChannelResponse")
public class CloseSecureChannelResponse implements UaResponseMessage {

    public static final NodeId TypeId = Identifiers.CloseSecureChannelResponse;
    public static final NodeId BinaryEncodingId = Identifiers.CloseSecureChannelResponse_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.CloseSecureChannelResponse_Encoding_DefaultXml;

    protected final ResponseHeader _responseHeader;

    public CloseSecureChannelResponse() {
        this._responseHeader = null;
    }

    public CloseSecureChannelResponse(ResponseHeader _responseHeader) {
        this._responseHeader = _responseHeader;
    }

    public ResponseHeader getResponseHeader() { return _responseHeader; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(CloseSecureChannelResponse closeSecureChannelResponse, UaEncoder encoder) {
        encoder.encodeSerializable("ResponseHeader", closeSecureChannelResponse._responseHeader != null ? closeSecureChannelResponse._responseHeader : new ResponseHeader());
    }

    public static CloseSecureChannelResponse decode(UaDecoder decoder) {
        ResponseHeader _responseHeader = decoder.decodeSerializable("ResponseHeader", ResponseHeader.class);

        return new CloseSecureChannelResponse(_responseHeader);
    }

    static {
        DelegateRegistry.registerEncoder(CloseSecureChannelResponse::encode, CloseSecureChannelResponse.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(CloseSecureChannelResponse::decode, CloseSecureChannelResponse.class, BinaryEncodingId, XmlEncodingId);
    }

}
