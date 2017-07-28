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
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

@UaDataType("SetTriggeringRequest")
public class SetTriggeringRequest implements UaRequestMessage {

    public static final NodeId TypeId = Identifiers.SetTriggeringRequest;
    public static final NodeId BinaryEncodingId = Identifiers.SetTriggeringRequest_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.SetTriggeringRequest_Encoding_DefaultXml;

    protected final RequestHeader _requestHeader;
    protected final UInteger _subscriptionId;
    protected final UInteger _triggeringItemId;
    protected final UInteger[] _linksToAdd;
    protected final UInteger[] _linksToRemove;

    public SetTriggeringRequest() {
        this._requestHeader = null;
        this._subscriptionId = null;
        this._triggeringItemId = null;
        this._linksToAdd = null;
        this._linksToRemove = null;
    }

    public SetTriggeringRequest(RequestHeader _requestHeader, UInteger _subscriptionId, UInteger _triggeringItemId, UInteger[] _linksToAdd, UInteger[] _linksToRemove) {
        this._requestHeader = _requestHeader;
        this._subscriptionId = _subscriptionId;
        this._triggeringItemId = _triggeringItemId;
        this._linksToAdd = _linksToAdd;
        this._linksToRemove = _linksToRemove;
    }

    public RequestHeader getRequestHeader() { return _requestHeader; }

    public UInteger getSubscriptionId() { return _subscriptionId; }

    public UInteger getTriggeringItemId() { return _triggeringItemId; }

    public UInteger[] getLinksToAdd() { return _linksToAdd; }

    public UInteger[] getLinksToRemove() { return _linksToRemove; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(SetTriggeringRequest setTriggeringRequest, UaEncoder encoder) {
        encoder.encodeSerializable("RequestHeader", setTriggeringRequest._requestHeader != null ? setTriggeringRequest._requestHeader : new RequestHeader());
        encoder.encodeUInt32("SubscriptionId", setTriggeringRequest._subscriptionId);
        encoder.encodeUInt32("TriggeringItemId", setTriggeringRequest._triggeringItemId);
        encoder.encodeArray("LinksToAdd", setTriggeringRequest._linksToAdd, encoder::encodeUInt32);
        encoder.encodeArray("LinksToRemove", setTriggeringRequest._linksToRemove, encoder::encodeUInt32);
    }

    public static SetTriggeringRequest decode(UaDecoder decoder) {
        RequestHeader _requestHeader = decoder.decodeSerializable("RequestHeader", RequestHeader.class);
        UInteger _subscriptionId = decoder.decodeUInt32("SubscriptionId");
        UInteger _triggeringItemId = decoder.decodeUInt32("TriggeringItemId");
        UInteger[] _linksToAdd = decoder.decodeArray("LinksToAdd", decoder::decodeUInt32, UInteger.class);
        UInteger[] _linksToRemove = decoder.decodeArray("LinksToRemove", decoder::decodeUInt32, UInteger.class);

        return new SetTriggeringRequest(_requestHeader, _subscriptionId, _triggeringItemId, _linksToAdd, _linksToRemove);
    }

    static {
        DelegateRegistry.registerEncoder(SetTriggeringRequest::encode, SetTriggeringRequest.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(SetTriggeringRequest::decode, SetTriggeringRequest.class, BinaryEncodingId, XmlEncodingId);
    }

}
