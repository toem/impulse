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
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

@UaDataType("ObjectTypeAttributes")
public class ObjectTypeAttributes extends NodeAttributes {

    public static final NodeId TypeId = Identifiers.ObjectTypeAttributes;
    public static final NodeId BinaryEncodingId = Identifiers.ObjectTypeAttributes_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ObjectTypeAttributes_Encoding_DefaultXml;

    protected final Boolean _isAbstract;

    public ObjectTypeAttributes() {
        super(null, null, null, null, null);
        this._isAbstract = null;
    }

    public ObjectTypeAttributes(UInteger _specifiedAttributes, LocalizedText _displayName, LocalizedText _description, UInteger _writeMask, UInteger _userWriteMask, Boolean _isAbstract) {
        super(_specifiedAttributes, _displayName, _description, _writeMask, _userWriteMask);
        this._isAbstract = _isAbstract;
    }

    public Boolean getIsAbstract() { return _isAbstract; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(ObjectTypeAttributes objectTypeAttributes, UaEncoder encoder) {
        encoder.encodeUInt32("SpecifiedAttributes", objectTypeAttributes._specifiedAttributes);
        encoder.encodeLocalizedText("DisplayName", objectTypeAttributes._displayName);
        encoder.encodeLocalizedText("Description", objectTypeAttributes._description);
        encoder.encodeUInt32("WriteMask", objectTypeAttributes._writeMask);
        encoder.encodeUInt32("UserWriteMask", objectTypeAttributes._userWriteMask);
        encoder.encodeBoolean("IsAbstract", objectTypeAttributes._isAbstract);
    }

    public static ObjectTypeAttributes decode(UaDecoder decoder) {
        UInteger _specifiedAttributes = decoder.decodeUInt32("SpecifiedAttributes");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        UInteger _writeMask = decoder.decodeUInt32("WriteMask");
        UInteger _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        Boolean _isAbstract = decoder.decodeBoolean("IsAbstract");

        return new ObjectTypeAttributes(_specifiedAttributes, _displayName, _description, _writeMask, _userWriteMask, _isAbstract);
    }

    static {
        DelegateRegistry.registerEncoder(ObjectTypeAttributes::encode, ObjectTypeAttributes.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ObjectTypeAttributes::decode, ObjectTypeAttributes.class, BinaryEncodingId, XmlEncodingId);
    }

}
