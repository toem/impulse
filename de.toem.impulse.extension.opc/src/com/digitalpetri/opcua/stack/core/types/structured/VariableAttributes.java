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
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

@UaDataType("VariableAttributes")
public class VariableAttributes extends NodeAttributes {

    public static final NodeId TypeId = Identifiers.VariableAttributes;
    public static final NodeId BinaryEncodingId = Identifiers.VariableAttributes_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.VariableAttributes_Encoding_DefaultXml;

    protected final Variant _value;
    protected final NodeId _dataType;
    protected final Integer _valueRank;
    protected final UInteger[] _arrayDimensions;
    protected final UByte _accessLevel;
    protected final UByte _userAccessLevel;
    protected final Double _minimumSamplingInterval;
    protected final Boolean _historizing;

    public VariableAttributes() {
        super(null, null, null, null, null);
        this._value = null;
        this._dataType = null;
        this._valueRank = null;
        this._arrayDimensions = null;
        this._accessLevel = null;
        this._userAccessLevel = null;
        this._minimumSamplingInterval = null;
        this._historizing = null;
    }

    public VariableAttributes(UInteger _specifiedAttributes, LocalizedText _displayName, LocalizedText _description, UInteger _writeMask, UInteger _userWriteMask, Variant _value, NodeId _dataType, Integer _valueRank, UInteger[] _arrayDimensions, UByte _accessLevel, UByte _userAccessLevel, Double _minimumSamplingInterval, Boolean _historizing) {
        super(_specifiedAttributes, _displayName, _description, _writeMask, _userWriteMask);
        this._value = _value;
        this._dataType = _dataType;
        this._valueRank = _valueRank;
        this._arrayDimensions = _arrayDimensions;
        this._accessLevel = _accessLevel;
        this._userAccessLevel = _userAccessLevel;
        this._minimumSamplingInterval = _minimumSamplingInterval;
        this._historizing = _historizing;
    }

    public Variant getValue() { return _value; }

    public NodeId getDataType() { return _dataType; }

    public Integer getValueRank() { return _valueRank; }

    public UInteger[] getArrayDimensions() { return _arrayDimensions; }

    public UByte getAccessLevel() { return _accessLevel; }

    public UByte getUserAccessLevel() { return _userAccessLevel; }

    public Double getMinimumSamplingInterval() { return _minimumSamplingInterval; }

    public Boolean getHistorizing() { return _historizing; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(VariableAttributes variableAttributes, UaEncoder encoder) {
        encoder.encodeUInt32("SpecifiedAttributes", variableAttributes._specifiedAttributes);
        encoder.encodeLocalizedText("DisplayName", variableAttributes._displayName);
        encoder.encodeLocalizedText("Description", variableAttributes._description);
        encoder.encodeUInt32("WriteMask", variableAttributes._writeMask);
        encoder.encodeUInt32("UserWriteMask", variableAttributes._userWriteMask);
        encoder.encodeVariant("Value", variableAttributes._value);
        encoder.encodeNodeId("DataType", variableAttributes._dataType);
        encoder.encodeInt32("ValueRank", variableAttributes._valueRank);
        encoder.encodeArray("ArrayDimensions", variableAttributes._arrayDimensions, encoder::encodeUInt32);
        encoder.encodeByte("AccessLevel", variableAttributes._accessLevel);
        encoder.encodeByte("UserAccessLevel", variableAttributes._userAccessLevel);
        encoder.encodeDouble("MinimumSamplingInterval", variableAttributes._minimumSamplingInterval);
        encoder.encodeBoolean("Historizing", variableAttributes._historizing);
    }

    public static VariableAttributes decode(UaDecoder decoder) {
        UInteger _specifiedAttributes = decoder.decodeUInt32("SpecifiedAttributes");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        UInteger _writeMask = decoder.decodeUInt32("WriteMask");
        UInteger _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        Variant _value = decoder.decodeVariant("Value");
        NodeId _dataType = decoder.decodeNodeId("DataType");
        Integer _valueRank = decoder.decodeInt32("ValueRank");
        UInteger[] _arrayDimensions = decoder.decodeArray("ArrayDimensions", decoder::decodeUInt32, UInteger.class);
        UByte _accessLevel = decoder.decodeByte("AccessLevel");
        UByte _userAccessLevel = decoder.decodeByte("UserAccessLevel");
        Double _minimumSamplingInterval = decoder.decodeDouble("MinimumSamplingInterval");
        Boolean _historizing = decoder.decodeBoolean("Historizing");

        return new VariableAttributes(_specifiedAttributes, _displayName, _description, _writeMask, _userWriteMask, _value, _dataType, _valueRank, _arrayDimensions, _accessLevel, _userAccessLevel, _minimumSamplingInterval, _historizing);
    }

    static {
        DelegateRegistry.registerEncoder(VariableAttributes::encode, VariableAttributes.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(VariableAttributes::decode, VariableAttributes.class, BinaryEncodingId, XmlEncodingId);
    }

}
