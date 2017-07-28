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
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

@UaDataType("DataTypeNode")
public class DataTypeNode extends TypeNode {

    public static final NodeId TypeId = Identifiers.DataTypeNode;
    public static final NodeId BinaryEncodingId = Identifiers.DataTypeNode_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.DataTypeNode_Encoding_DefaultXml;

    protected final Boolean _isAbstract;

    public DataTypeNode() {
        super(null, null, null, null, null, null, null, null);
        this._isAbstract = null;
    }

    public DataTypeNode(NodeId _nodeId, NodeClass _nodeClass, QualifiedName _browseName, LocalizedText _displayName, LocalizedText _description, UInteger _writeMask, UInteger _userWriteMask, ReferenceNode[] _references, Boolean _isAbstract) {
        super(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references);
        this._isAbstract = _isAbstract;
    }

    public Boolean getIsAbstract() { return _isAbstract; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(DataTypeNode dataTypeNode, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", dataTypeNode._nodeId);
        encoder.encodeEnumeration("NodeClass", dataTypeNode._nodeClass);
        encoder.encodeQualifiedName("BrowseName", dataTypeNode._browseName);
        encoder.encodeLocalizedText("DisplayName", dataTypeNode._displayName);
        encoder.encodeLocalizedText("Description", dataTypeNode._description);
        encoder.encodeUInt32("WriteMask", dataTypeNode._writeMask);
        encoder.encodeUInt32("UserWriteMask", dataTypeNode._userWriteMask);
        encoder.encodeArray("References", dataTypeNode._references, encoder::encodeSerializable);
        encoder.encodeBoolean("IsAbstract", dataTypeNode._isAbstract);
    }

    public static DataTypeNode decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        NodeClass _nodeClass = decoder.decodeEnumeration("NodeClass", NodeClass.class);
        QualifiedName _browseName = decoder.decodeQualifiedName("BrowseName");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        UInteger _writeMask = decoder.decodeUInt32("WriteMask");
        UInteger _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        ReferenceNode[] _references = decoder.decodeArray("References", decoder::decodeSerializable, ReferenceNode.class);
        Boolean _isAbstract = decoder.decodeBoolean("IsAbstract");

        return new DataTypeNode(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references, _isAbstract);
    }

    static {
        DelegateRegistry.registerEncoder(DataTypeNode::encode, DataTypeNode.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(DataTypeNode::decode, DataTypeNode.class, BinaryEncodingId, XmlEncodingId);
    }

}
