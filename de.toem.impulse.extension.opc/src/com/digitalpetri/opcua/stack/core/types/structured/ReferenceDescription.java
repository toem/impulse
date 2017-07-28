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
import com.digitalpetri.opcua.stack.core.types.builtin.ExpandedNodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

@UaDataType("ReferenceDescription")
public class ReferenceDescription implements UaStructure {

    public static final NodeId TypeId = Identifiers.ReferenceDescription;
    public static final NodeId BinaryEncodingId = Identifiers.ReferenceDescription_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ReferenceDescription_Encoding_DefaultXml;

    protected final NodeId _referenceTypeId;
    protected final Boolean _isForward;
    protected final ExpandedNodeId _nodeId;
    protected final QualifiedName _browseName;
    protected final LocalizedText _displayName;
    protected final NodeClass _nodeClass;
    protected final ExpandedNodeId _typeDefinition;

    public ReferenceDescription() {
        this._referenceTypeId = null;
        this._isForward = null;
        this._nodeId = null;
        this._browseName = null;
        this._displayName = null;
        this._nodeClass = null;
        this._typeDefinition = null;
    }

    public ReferenceDescription(NodeId _referenceTypeId, Boolean _isForward, ExpandedNodeId _nodeId, QualifiedName _browseName, LocalizedText _displayName, NodeClass _nodeClass, ExpandedNodeId _typeDefinition) {
        this._referenceTypeId = _referenceTypeId;
        this._isForward = _isForward;
        this._nodeId = _nodeId;
        this._browseName = _browseName;
        this._displayName = _displayName;
        this._nodeClass = _nodeClass;
        this._typeDefinition = _typeDefinition;
    }

    public NodeId getReferenceTypeId() { return _referenceTypeId; }

    public Boolean getIsForward() { return _isForward; }

    public ExpandedNodeId getNodeId() { return _nodeId; }

    public QualifiedName getBrowseName() { return _browseName; }

    public LocalizedText getDisplayName() { return _displayName; }

    public NodeClass getNodeClass() { return _nodeClass; }

    public ExpandedNodeId getTypeDefinition() { return _typeDefinition; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(ReferenceDescription referenceDescription, UaEncoder encoder) {
        encoder.encodeNodeId("ReferenceTypeId", referenceDescription._referenceTypeId);
        encoder.encodeBoolean("IsForward", referenceDescription._isForward);
        encoder.encodeExpandedNodeId("NodeId", referenceDescription._nodeId);
        encoder.encodeQualifiedName("BrowseName", referenceDescription._browseName);
        encoder.encodeLocalizedText("DisplayName", referenceDescription._displayName);
        encoder.encodeEnumeration("NodeClass", referenceDescription._nodeClass);
        encoder.encodeExpandedNodeId("TypeDefinition", referenceDescription._typeDefinition);
    }

    public static ReferenceDescription decode(UaDecoder decoder) {
        NodeId _referenceTypeId = decoder.decodeNodeId("ReferenceTypeId");
        Boolean _isForward = decoder.decodeBoolean("IsForward");
        ExpandedNodeId _nodeId = decoder.decodeExpandedNodeId("NodeId");
        QualifiedName _browseName = decoder.decodeQualifiedName("BrowseName");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        NodeClass _nodeClass = decoder.decodeEnumeration("NodeClass", NodeClass.class);
        ExpandedNodeId _typeDefinition = decoder.decodeExpandedNodeId("TypeDefinition");

        return new ReferenceDescription(_referenceTypeId, _isForward, _nodeId, _browseName, _displayName, _nodeClass, _typeDefinition);
    }

    static {
        DelegateRegistry.registerEncoder(ReferenceDescription::encode, ReferenceDescription.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ReferenceDescription::decode, ReferenceDescription.class, BinaryEncodingId, XmlEncodingId);
    }

}
