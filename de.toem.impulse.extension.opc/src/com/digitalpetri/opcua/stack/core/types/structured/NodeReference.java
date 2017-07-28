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

@UaDataType("NodeReference")
public class NodeReference implements UaStructure {

    public static final NodeId TypeId = Identifiers.NodeReference;
    public static final NodeId BinaryEncodingId = Identifiers.NodeReference_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.NodeReference_Encoding_DefaultXml;

    protected final NodeId _nodeId;
    protected final NodeId _referenceTypeId;
    protected final Boolean _isForward;
    protected final NodeId[] _referencedNodeIds;

    public NodeReference() {
        this._nodeId = null;
        this._referenceTypeId = null;
        this._isForward = null;
        this._referencedNodeIds = null;
    }

    public NodeReference(NodeId _nodeId, NodeId _referenceTypeId, Boolean _isForward, NodeId[] _referencedNodeIds) {
        this._nodeId = _nodeId;
        this._referenceTypeId = _referenceTypeId;
        this._isForward = _isForward;
        this._referencedNodeIds = _referencedNodeIds;
    }

    public NodeId getNodeId() { return _nodeId; }

    public NodeId getReferenceTypeId() { return _referenceTypeId; }

    public Boolean getIsForward() { return _isForward; }

    public NodeId[] getReferencedNodeIds() { return _referencedNodeIds; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(NodeReference nodeReference, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", nodeReference._nodeId);
        encoder.encodeNodeId("ReferenceTypeId", nodeReference._referenceTypeId);
        encoder.encodeBoolean("IsForward", nodeReference._isForward);
        encoder.encodeArray("ReferencedNodeIds", nodeReference._referencedNodeIds, encoder::encodeNodeId);
    }

    public static NodeReference decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        NodeId _referenceTypeId = decoder.decodeNodeId("ReferenceTypeId");
        Boolean _isForward = decoder.decodeBoolean("IsForward");
        NodeId[] _referencedNodeIds = decoder.decodeArray("ReferencedNodeIds", decoder::decodeNodeId, NodeId.class);

        return new NodeReference(_nodeId, _referenceTypeId, _isForward, _referencedNodeIds);
    }

    static {
        DelegateRegistry.registerEncoder(NodeReference::encode, NodeReference.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(NodeReference::decode, NodeReference.class, BinaryEncodingId, XmlEncodingId);
    }

}
