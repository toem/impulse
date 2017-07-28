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
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("CallRequest")
public class CallRequest implements UaRequestMessage {

    public static final NodeId TypeId = Identifiers.CallRequest;
    public static final NodeId BinaryEncodingId = Identifiers.CallRequest_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.CallRequest_Encoding_DefaultXml;

    protected final RequestHeader _requestHeader;
    protected final CallMethodRequest[] _methodsToCall;

    public CallRequest() {
        this._requestHeader = null;
        this._methodsToCall = null;
    }

    public CallRequest(RequestHeader _requestHeader, CallMethodRequest[] _methodsToCall) {
        this._requestHeader = _requestHeader;
        this._methodsToCall = _methodsToCall;
    }

    public RequestHeader getRequestHeader() { return _requestHeader; }

    public CallMethodRequest[] getMethodsToCall() { return _methodsToCall; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(CallRequest callRequest, UaEncoder encoder) {
        encoder.encodeSerializable("RequestHeader", callRequest._requestHeader != null ? callRequest._requestHeader : new RequestHeader());
        encoder.encodeArray("MethodsToCall", callRequest._methodsToCall, encoder::encodeSerializable);
    }

    public static CallRequest decode(UaDecoder decoder) {
        RequestHeader _requestHeader = decoder.decodeSerializable("RequestHeader", RequestHeader.class);
        CallMethodRequest[] _methodsToCall = decoder.decodeArray("MethodsToCall", decoder::decodeSerializable, CallMethodRequest.class);

        return new CallRequest(_requestHeader, _methodsToCall);
    }

    static {
        DelegateRegistry.registerEncoder(CallRequest::encode, CallRequest.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(CallRequest::decode, CallRequest.class, BinaryEncodingId, XmlEncodingId);
    }

}
