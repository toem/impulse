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
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

@UaDataType("MonitoredItemCreateResult")
public class MonitoredItemCreateResult implements UaStructure {

    public static final NodeId TypeId = Identifiers.MonitoredItemCreateResult;
    public static final NodeId BinaryEncodingId = Identifiers.MonitoredItemCreateResult_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.MonitoredItemCreateResult_Encoding_DefaultXml;

    protected final StatusCode _statusCode;
    protected final UInteger _monitoredItemId;
    protected final Double _revisedSamplingInterval;
    protected final UInteger _revisedQueueSize;
    protected final ExtensionObject _filterResult;

    public MonitoredItemCreateResult() {
        this._statusCode = null;
        this._monitoredItemId = null;
        this._revisedSamplingInterval = null;
        this._revisedQueueSize = null;
        this._filterResult = null;
    }

    public MonitoredItemCreateResult(StatusCode _statusCode, UInteger _monitoredItemId, Double _revisedSamplingInterval, UInteger _revisedQueueSize, ExtensionObject _filterResult) {
        this._statusCode = _statusCode;
        this._monitoredItemId = _monitoredItemId;
        this._revisedSamplingInterval = _revisedSamplingInterval;
        this._revisedQueueSize = _revisedQueueSize;
        this._filterResult = _filterResult;
    }

    public StatusCode getStatusCode() { return _statusCode; }

    public UInteger getMonitoredItemId() { return _monitoredItemId; }

    public Double getRevisedSamplingInterval() { return _revisedSamplingInterval; }

    public UInteger getRevisedQueueSize() { return _revisedQueueSize; }

    public ExtensionObject getFilterResult() { return _filterResult; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(MonitoredItemCreateResult monitoredItemCreateResult, UaEncoder encoder) {
        encoder.encodeStatusCode("StatusCode", monitoredItemCreateResult._statusCode);
        encoder.encodeUInt32("MonitoredItemId", monitoredItemCreateResult._monitoredItemId);
        encoder.encodeDouble("RevisedSamplingInterval", monitoredItemCreateResult._revisedSamplingInterval);
        encoder.encodeUInt32("RevisedQueueSize", monitoredItemCreateResult._revisedQueueSize);
        encoder.encodeExtensionObject("FilterResult", monitoredItemCreateResult._filterResult);
    }

    public static MonitoredItemCreateResult decode(UaDecoder decoder) {
        StatusCode _statusCode = decoder.decodeStatusCode("StatusCode");
        UInteger _monitoredItemId = decoder.decodeUInt32("MonitoredItemId");
        Double _revisedSamplingInterval = decoder.decodeDouble("RevisedSamplingInterval");
        UInteger _revisedQueueSize = decoder.decodeUInt32("RevisedQueueSize");
        ExtensionObject _filterResult = decoder.decodeExtensionObject("FilterResult");

        return new MonitoredItemCreateResult(_statusCode, _monitoredItemId, _revisedSamplingInterval, _revisedQueueSize, _filterResult);
    }

    static {
        DelegateRegistry.registerEncoder(MonitoredItemCreateResult::encode, MonitoredItemCreateResult.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(MonitoredItemCreateResult::decode, MonitoredItemCreateResult.class, BinaryEncodingId, XmlEncodingId);
    }

}
