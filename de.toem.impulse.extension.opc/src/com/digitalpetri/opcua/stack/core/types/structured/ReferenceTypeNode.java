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

@UaDataType("ReferenceTypeNode")
public class ReferenceTypeNode extends TypeNode {

    public static final NodeId TypeId = Identifiers.ReferenceTypeNode;
    public static final NodeId BinaryEncodingId = Identifiers.ReferenceTypeNode_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ReferenceTypeNode_Encoding_DefaultXml;

    protected final ReferenceNode[] _references;
    protected final Boolean _isAbstract;
    protected final Boolean _symmetric;
    protected final LocalizedText _inverseName;

    public ReferenceTypeNode() {
        super(null, null, null, null, null, null, null, null);
        this._references = null;
        this._isAbstract = null;
        this._symmetric = null;
        this._inverseName = null;
    }

    public ReferenceTypeNode(NodeId _nodeId, NodeClass _nodeClass, QualifiedName _browseName, LocalizedText _displayName, LocalizedText _description, UInteger _writeMask, UInteger _userWriteMask, ReferenceNode[] _references, Boolean _isAbstract, Boolean _symmetric, LocalizedText _inverseName) {
        super(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references);
        this._references = _references;
        this._isAbstract = _isAbstract;
        this._symmetric = _symmetric;
        this._inverseName = _inverseName;
    }

    public ReferenceNode[] getReferences() { return _references; }

    public Boolean getIsAbstract() { return _isAbstract; }

    public Boolean getSymmetric() { return _symmetric; }

    public LocalizedText getInverseName() { return _inverseName; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(ReferenceTypeNode referenceTypeNode, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", referenceTypeNode._nodeId);
        encoder.encodeEnumeration("NodeClass", referenceTypeNode._nodeClass);
        encoder.encodeQualifiedName("BrowseName", referenceTypeNode._browseName);
        encoder.encodeLocalizedText("DisplayName", referenceTypeNode._displayName);
        encoder.encodeLocalizedText("Description", referenceTypeNode._description);
        encoder.encodeUInt32("WriteMask", referenceTypeNode._writeMask);
        encoder.encodeUInt32("UserWriteMask", referenceTypeNode._userWriteMask);
        encoder.encodeArray("References", referenceTypeNode._references, encoder::encodeSerializable);
        encoder.encodeBoolean("IsAbstract", referenceTypeNode._isAbstract);
        encoder.encodeBoolean("Symmetric", referenceTypeNode._symmetric);
        encoder.encodeLocalizedText("InverseName", referenceTypeNode._inverseName);
    }

    public static ReferenceTypeNode decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        NodeClass _nodeClass = decoder.decodeEnumeration("NodeClass", NodeClass.class);
        QualifiedName _browseName = decoder.decodeQualifiedName("BrowseName");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        UInteger _writeMask = decoder.decodeUInt32("WriteMask");
        UInteger _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        ReferenceNode[] _references = decoder.decodeArray("References", decoder::decodeSerializable, ReferenceNode.class);
        Boolean _isAbstract = decoder.decodeBoolean("IsAbstract");
        Boolean _symmetric = decoder.decodeBoolean("Symmetric");
        LocalizedText _inverseName = decoder.decodeLocalizedText("InverseName");

        return new ReferenceTypeNode(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references, _isAbstract, _symmetric, _inverseName);
    }

    static {
        DelegateRegistry.registerEncoder(ReferenceTypeNode::encode, ReferenceTypeNode.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ReferenceTypeNode::decode, ReferenceTypeNode.class, BinaryEncodingId, XmlEncodingId);
    }

}
