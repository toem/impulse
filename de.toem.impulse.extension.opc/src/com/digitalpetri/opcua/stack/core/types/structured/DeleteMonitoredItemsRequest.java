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

@UaDataType("DeleteMonitoredItemsRequest")
public class DeleteMonitoredItemsRequest implements UaRequestMessage {

    public static final NodeId TypeId = Identifiers.DeleteMonitoredItemsRequest;
    public static final NodeId BinaryEncodingId = Identifiers.DeleteMonitoredItemsRequest_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.DeleteMonitoredItemsRequest_Encoding_DefaultXml;

    protected final RequestHeader _requestHeader;
    protected final UInteger _subscriptionId;
    protected final UInteger[] _monitoredItemIds;

    public DeleteMonitoredItemsRequest() {
        this._requestHeader = null;
        this._subscriptionId = null;
        this._monitoredItemIds = null;
    }

    public DeleteMonitoredItemsRequest(RequestHeader _requestHeader, UInteger _subscriptionId, UInteger[] _monitoredItemIds) {
        this._requestHeader = _requestHeader;
        this._subscriptionId = _subscriptionId;
        this._monitoredItemIds = _monitoredItemIds;
    }

    public RequestHeader getRequestHeader() { return _requestHeader; }

    public UInteger getSubscriptionId() { return _subscriptionId; }

    public UInteger[] getMonitoredItemIds() { return _monitoredItemIds; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(DeleteMonitoredItemsRequest deleteMonitoredItemsRequest, UaEncoder encoder) {
        encoder.encodeSerializable("RequestHeader", deleteMonitoredItemsRequest._requestHeader != null ? deleteMonitoredItemsRequest._requestHeader : new RequestHeader());
        encoder.encodeUInt32("SubscriptionId", deleteMonitoredItemsRequest._subscriptionId);
        encoder.encodeArray("MonitoredItemIds", deleteMonitoredItemsRequest._monitoredItemIds, encoder::encodeUInt32);
    }

    public static DeleteMonitoredItemsRequest decode(UaDecoder decoder) {
        RequestHeader _requestHeader = decoder.decodeSerializable("RequestHeader", RequestHeader.class);
        UInteger _subscriptionId = decoder.decodeUInt32("SubscriptionId");
        UInteger[] _monitoredItemIds = decoder.decodeArray("MonitoredItemIds", decoder::decodeUInt32, UInteger.class);

        return new DeleteMonitoredItemsRequest(_requestHeader, _subscriptionId, _monitoredItemIds);
    }

    static {
        DelegateRegistry.registerEncoder(DeleteMonitoredItemsRequest::encode, DeleteMonitoredItemsRequest.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(DeleteMonitoredItemsRequest::decode, DeleteMonitoredItemsRequest.class, BinaryEncodingId, XmlEncodingId);
    }

}
