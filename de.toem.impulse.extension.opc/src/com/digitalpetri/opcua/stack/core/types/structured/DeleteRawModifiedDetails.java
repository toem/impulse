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
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("DeleteRawModifiedDetails")
public class DeleteRawModifiedDetails extends HistoryUpdateDetails {

    public static final NodeId TypeId = Identifiers.DeleteRawModifiedDetails;
    public static final NodeId BinaryEncodingId = Identifiers.DeleteRawModifiedDetails_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.DeleteRawModifiedDetails_Encoding_DefaultXml;

    protected final Boolean _isDeleteModified;
    protected final DateTime _startTime;
    protected final DateTime _endTime;

    public DeleteRawModifiedDetails() {
        super(null);
        this._isDeleteModified = null;
        this._startTime = null;
        this._endTime = null;
    }

    public DeleteRawModifiedDetails(NodeId _nodeId, Boolean _isDeleteModified, DateTime _startTime, DateTime _endTime) {
        super(_nodeId);
        this._isDeleteModified = _isDeleteModified;
        this._startTime = _startTime;
        this._endTime = _endTime;
    }

    public Boolean getIsDeleteModified() { return _isDeleteModified; }

    public DateTime getStartTime() { return _startTime; }

    public DateTime getEndTime() { return _endTime; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(DeleteRawModifiedDetails deleteRawModifiedDetails, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", deleteRawModifiedDetails._nodeId);
        encoder.encodeBoolean("IsDeleteModified", deleteRawModifiedDetails._isDeleteModified);
        encoder.encodeDateTime("StartTime", deleteRawModifiedDetails._startTime);
        encoder.encodeDateTime("EndTime", deleteRawModifiedDetails._endTime);
    }

    public static DeleteRawModifiedDetails decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        Boolean _isDeleteModified = decoder.decodeBoolean("IsDeleteModified");
        DateTime _startTime = decoder.decodeDateTime("StartTime");
        DateTime _endTime = decoder.decodeDateTime("EndTime");

        return new DeleteRawModifiedDetails(_nodeId, _isDeleteModified, _startTime, _endTime);
    }

    static {
        DelegateRegistry.registerEncoder(DeleteRawModifiedDetails::encode, DeleteRawModifiedDetails.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(DeleteRawModifiedDetails::decode, DeleteRawModifiedDetails.class, BinaryEncodingId, XmlEncodingId);
    }

}
