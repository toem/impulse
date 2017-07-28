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

@UaDataType("QueryDataDescription")
public class QueryDataDescription implements UaStructure {

    public static final NodeId TypeId = Identifiers.QueryDataDescription;
    public static final NodeId BinaryEncodingId = Identifiers.QueryDataDescription_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.QueryDataDescription_Encoding_DefaultXml;

    protected final RelativePath _relativePath;
    protected final UInteger _attributeId;
    protected final String _indexRange;

    public QueryDataDescription() {
        this._relativePath = null;
        this._attributeId = null;
        this._indexRange = null;
    }

    public QueryDataDescription(RelativePath _relativePath, UInteger _attributeId, String _indexRange) {
        this._relativePath = _relativePath;
        this._attributeId = _attributeId;
        this._indexRange = _indexRange;
    }

    public RelativePath getRelativePath() { return _relativePath; }

    public UInteger getAttributeId() { return _attributeId; }

    public String getIndexRange() { return _indexRange; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(QueryDataDescription queryDataDescription, UaEncoder encoder) {
        encoder.encodeSerializable("RelativePath", queryDataDescription._relativePath != null ? queryDataDescription._relativePath : new RelativePath());
        encoder.encodeUInt32("AttributeId", queryDataDescription._attributeId);
        encoder.encodeString("IndexRange", queryDataDescription._indexRange);
    }

    public static QueryDataDescription decode(UaDecoder decoder) {
        RelativePath _relativePath = decoder.decodeSerializable("RelativePath", RelativePath.class);
        UInteger _attributeId = decoder.decodeUInt32("AttributeId");
        String _indexRange = decoder.decodeString("IndexRange");

        return new QueryDataDescription(_relativePath, _attributeId, _indexRange);
    }

    static {
        DelegateRegistry.registerEncoder(QueryDataDescription::encode, QueryDataDescription.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(QueryDataDescription::decode, QueryDataDescription.class, BinaryEncodingId, XmlEncodingId);
    }

}
