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
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

@UaDataType("Node")
public class Node implements UaStructure {

    public static final NodeId TypeId = Identifiers.Node;
    public static final NodeId BinaryEncodingId = Identifiers.Node_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.Node_Encoding_DefaultXml;

    protected final NodeId _nodeId;
    protected final NodeClass _nodeClass;
    protected final QualifiedName _browseName;
    protected final LocalizedText _displayName;
    protected final LocalizedText _description;
    protected final UInteger _writeMask;
    protected final UInteger _userWriteMask;
    protected final ReferenceNode[] _references;

    public Node() {
        this._nodeId = null;
        this._nodeClass = null;
        this._browseName = null;
        this._displayName = null;
        this._description = null;
        this._writeMask = null;
        this._userWriteMask = null;
        this._references = null;
    }

    public Node(NodeId _nodeId, NodeClass _nodeClass, QualifiedName _browseName, LocalizedText _displayName, LocalizedText _description, UInteger _writeMask, UInteger _userWriteMask, ReferenceNode[] _references) {
        this._nodeId = _nodeId;
        this._nodeClass = _nodeClass;
        this._browseName = _browseName;
        this._displayName = _displayName;
        this._description = _description;
        this._writeMask = _writeMask;
        this._userWriteMask = _userWriteMask;
        this._references = _references;
    }

    public NodeId getNodeId() { return _nodeId; }

    public NodeClass getNodeClass() { return _nodeClass; }

    public QualifiedName getBrowseName() { return _browseName; }

    public LocalizedText getDisplayName() { return _displayName; }

    public LocalizedText getDescription() { return _description; }

    public UInteger getWriteMask() { return _writeMask; }

    public UInteger getUserWriteMask() { return _userWriteMask; }

    public ReferenceNode[] getReferences() { return _references; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(Node node, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", node._nodeId);
        encoder.encodeEnumeration("NodeClass", node._nodeClass);
        encoder.encodeQualifiedName("BrowseName", node._browseName);
        encoder.encodeLocalizedText("DisplayName", node._displayName);
        encoder.encodeLocalizedText("Description", node._description);
        encoder.encodeUInt32("WriteMask", node._writeMask);
        encoder.encodeUInt32("UserWriteMask", node._userWriteMask);
        encoder.encodeArray("References", node._references, encoder::encodeSerializable);
    }

    public static Node decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        NodeClass _nodeClass = decoder.decodeEnumeration("NodeClass", NodeClass.class);
        QualifiedName _browseName = decoder.decodeQualifiedName("BrowseName");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        UInteger _writeMask = decoder.decodeUInt32("WriteMask");
        UInteger _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        ReferenceNode[] _references = decoder.decodeArray("References", decoder::decodeSerializable, ReferenceNode.class);

        return new Node(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references);
    }

    static {
        DelegateRegistry.registerEncoder(Node::encode, Node.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(Node::decode, Node.class, BinaryEncodingId, XmlEncodingId);
    }

}
