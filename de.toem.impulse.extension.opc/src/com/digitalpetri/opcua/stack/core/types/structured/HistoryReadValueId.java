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
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;

@UaDataType("HistoryReadValueId")
public class HistoryReadValueId implements UaStructure {

    public static final NodeId TypeId = Identifiers.HistoryReadValueId;
    public static final NodeId BinaryEncodingId = Identifiers.HistoryReadValueId_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.HistoryReadValueId_Encoding_DefaultXml;

    protected final NodeId _nodeId;
    protected final String _indexRange;
    protected final QualifiedName _dataEncoding;
    protected final ByteString _continuationPoint;

    public HistoryReadValueId() {
        this._nodeId = null;
        this._indexRange = null;
        this._dataEncoding = null;
        this._continuationPoint = null;
    }

    public HistoryReadValueId(NodeId _nodeId, String _indexRange, QualifiedName _dataEncoding, ByteString _continuationPoint) {
        this._nodeId = _nodeId;
        this._indexRange = _indexRange;
        this._dataEncoding = _dataEncoding;
        this._continuationPoint = _continuationPoint;
    }

    public NodeId getNodeId() { return _nodeId; }

    public String getIndexRange() { return _indexRange; }

    public QualifiedName getDataEncoding() { return _dataEncoding; }

    public ByteString getContinuationPoint() { return _continuationPoint; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(HistoryReadValueId historyReadValueId, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", historyReadValueId._nodeId);
        encoder.encodeString("IndexRange", historyReadValueId._indexRange);
        encoder.encodeQualifiedName("DataEncoding", historyReadValueId._dataEncoding);
        encoder.encodeByteString("ContinuationPoint", historyReadValueId._continuationPoint);
    }

    public static HistoryReadValueId decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        String _indexRange = decoder.decodeString("IndexRange");
        QualifiedName _dataEncoding = decoder.decodeQualifiedName("DataEncoding");
        ByteString _continuationPoint = decoder.decodeByteString("ContinuationPoint");

        return new HistoryReadValueId(_nodeId, _indexRange, _dataEncoding, _continuationPoint);
    }

    static {
        DelegateRegistry.registerEncoder(HistoryReadValueId::encode, HistoryReadValueId.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(HistoryReadValueId::decode, HistoryReadValueId.class, BinaryEncodingId, XmlEncodingId);
    }

}
