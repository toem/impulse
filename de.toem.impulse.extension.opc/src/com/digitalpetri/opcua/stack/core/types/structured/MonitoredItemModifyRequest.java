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
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

@UaDataType("MonitoredItemModifyRequest")
public class MonitoredItemModifyRequest implements UaStructure {

    public static final NodeId TypeId = Identifiers.MonitoredItemModifyRequest;
    public static final NodeId BinaryEncodingId = Identifiers.MonitoredItemModifyRequest_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.MonitoredItemModifyRequest_Encoding_DefaultXml;

    protected final UInteger _monitoredItemId;
    protected final MonitoringParameters _requestedParameters;

    public MonitoredItemModifyRequest() {
        this._monitoredItemId = null;
        this._requestedParameters = null;
    }

    public MonitoredItemModifyRequest(UInteger _monitoredItemId, MonitoringParameters _requestedParameters) {
        this._monitoredItemId = _monitoredItemId;
        this._requestedParameters = _requestedParameters;
    }

    public UInteger getMonitoredItemId() { return _monitoredItemId; }

    public MonitoringParameters getRequestedParameters() { return _requestedParameters; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(MonitoredItemModifyRequest monitoredItemModifyRequest, UaEncoder encoder) {
        encoder.encodeUInt32("MonitoredItemId", monitoredItemModifyRequest._monitoredItemId);
        encoder.encodeSerializable("RequestedParameters", monitoredItemModifyRequest._requestedParameters != null ? monitoredItemModifyRequest._requestedParameters : new MonitoringParameters());
    }

    public static MonitoredItemModifyRequest decode(UaDecoder decoder) {
        UInteger _monitoredItemId = decoder.decodeUInt32("MonitoredItemId");
        MonitoringParameters _requestedParameters = decoder.decodeSerializable("RequestedParameters", MonitoringParameters.class);

        return new MonitoredItemModifyRequest(_monitoredItemId, _requestedParameters);
    }

    static {
        DelegateRegistry.registerEncoder(MonitoredItemModifyRequest::encode, MonitoredItemModifyRequest.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(MonitoredItemModifyRequest::decode, MonitoredItemModifyRequest.class, BinaryEncodingId, XmlEncodingId);
    }

}
