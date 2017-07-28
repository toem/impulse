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

import java.util.UUID;

import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.serialization.DelegateRegistry;
import com.digitalpetri.opcua.stack.core.serialization.UaDecoder;
import com.digitalpetri.opcua.stack.core.serialization.UaEncoder;
import com.digitalpetri.opcua.stack.core.serialization.UaStructure;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.DiagnosticInfo;
import com.digitalpetri.opcua.stack.core.types.builtin.ExpandedNodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.XmlElement;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.ULong;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.enumerated.EnumeratedTestType;

@UaDataType("ScalarTestType")
public class ScalarTestType implements UaStructure {

    public static final NodeId TypeId = Identifiers.ScalarTestType;
    public static final NodeId BinaryEncodingId = Identifiers.ScalarTestType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ScalarTestType_Encoding_DefaultXml;

    protected final Boolean _boolean;
    protected final Byte _sByte;
    protected final UByte _byte;
    protected final Short _int16;
    protected final UShort _uInt16;
    protected final Integer _int32;
    protected final UInteger _uInt32;
    protected final Long _int64;
    protected final ULong _uInt64;
    protected final Float _float;
    protected final Double _double;
    protected final String _string;
    protected final DateTime _dateTime;
    protected final UUID _guid;
    protected final ByteString _byteString;
    protected final XmlElement _xmlElement;
    protected final NodeId _nodeId;
    protected final ExpandedNodeId _expandedNodeId;
    protected final StatusCode _statusCode;
    protected final DiagnosticInfo _diagnosticInfo;
    protected final QualifiedName _qualifiedName;
    protected final LocalizedText _localizedText;
    protected final ExtensionObject _extensionObject;
    protected final DataValue _dataValue;
    protected final EnumeratedTestType _enumeratedValue;

    public ScalarTestType() {
        this._boolean = null;
        this._sByte = null;
        this._byte = null;
        this._int16 = null;
        this._uInt16 = null;
        this._int32 = null;
        this._uInt32 = null;
        this._int64 = null;
        this._uInt64 = null;
        this._float = null;
        this._double = null;
        this._string = null;
        this._dateTime = null;
        this._guid = null;
        this._byteString = null;
        this._xmlElement = null;
        this._nodeId = null;
        this._expandedNodeId = null;
        this._statusCode = null;
        this._diagnosticInfo = null;
        this._qualifiedName = null;
        this._localizedText = null;
        this._extensionObject = null;
        this._dataValue = null;
        this._enumeratedValue = null;
    }

    public ScalarTestType(Boolean _boolean, Byte _sByte, UByte _byte, Short _int16, UShort _uInt16, Integer _int32, UInteger _uInt32, Long _int64, ULong _uInt64, Float _float, Double _double, String _string, DateTime _dateTime, UUID _guid, ByteString _byteString, XmlElement _xmlElement, NodeId _nodeId, ExpandedNodeId _expandedNodeId, StatusCode _statusCode, DiagnosticInfo _diagnosticInfo, QualifiedName _qualifiedName, LocalizedText _localizedText, ExtensionObject _extensionObject, DataValue _dataValue, EnumeratedTestType _enumeratedValue) {
        this._boolean = _boolean;
        this._sByte = _sByte;
        this._byte = _byte;
        this._int16 = _int16;
        this._uInt16 = _uInt16;
        this._int32 = _int32;
        this._uInt32 = _uInt32;
        this._int64 = _int64;
        this._uInt64 = _uInt64;
        this._float = _float;
        this._double = _double;
        this._string = _string;
        this._dateTime = _dateTime;
        this._guid = _guid;
        this._byteString = _byteString;
        this._xmlElement = _xmlElement;
        this._nodeId = _nodeId;
        this._expandedNodeId = _expandedNodeId;
        this._statusCode = _statusCode;
        this._diagnosticInfo = _diagnosticInfo;
        this._qualifiedName = _qualifiedName;
        this._localizedText = _localizedText;
        this._extensionObject = _extensionObject;
        this._dataValue = _dataValue;
        this._enumeratedValue = _enumeratedValue;
    }

    public Boolean getBoolean() {
        return _boolean;
    }

    public Byte getSByte() {
        return _sByte;
    }

    public UByte getByte() {
        return _byte;
    }

    public Short getInt16() {
        return _int16;
    }

    public UShort getUInt16() {
        return _uInt16;
    }

    public Integer getInt32() {
        return _int32;
    }

    public UInteger getUInt32() {
        return _uInt32;
    }

    public Long getInt64() {
        return _int64;
    }

    public ULong getUInt64() {
        return _uInt64;
    }

    public Float getFloat() {
        return _float;
    }

    public Double getDouble() {
        return _double;
    }

    public String getString() {
        return _string;
    }

    public DateTime getDateTime() {
        return _dateTime;
    }

    public UUID getGuid() {
        return _guid;
    }

    public ByteString getByteString() {
        return _byteString;
    }

    public XmlElement getXmlElement() {
        return _xmlElement;
    }

    public NodeId getNodeId() {
        return _nodeId;
    }

    public ExpandedNodeId getExpandedNodeId() {
        return _expandedNodeId;
    }

    public StatusCode getStatusCode() {
        return _statusCode;
    }

    public DiagnosticInfo getDiagnosticInfo() {
        return _diagnosticInfo;
    }

    public QualifiedName getQualifiedName() {
        return _qualifiedName;
    }

    public LocalizedText getLocalizedText() {
        return _localizedText;
    }

    public ExtensionObject getExtensionObject() {
        return _extensionObject;
    }

    public DataValue getDataValue() {
        return _dataValue;
    }

    public EnumeratedTestType getEnumeratedValue() {
        return _enumeratedValue;
    }

    @Override
    public NodeId getTypeId() {
        return TypeId;
    }

    @Override
    public NodeId getBinaryEncodingId() {
        return BinaryEncodingId;
    }

    @Override
    public NodeId getXmlEncodingId() {
        return XmlEncodingId;
    }


    public static void encode(ScalarTestType scalarTestType, UaEncoder encoder) {
        encoder.encodeBoolean("Boolean", scalarTestType._boolean);
        encoder.encodeSByte("SByte", scalarTestType._sByte);
        encoder.encodeByte("Byte", scalarTestType._byte);
        encoder.encodeInt16("Int16", scalarTestType._int16);
        encoder.encodeUInt16("UInt16", scalarTestType._uInt16);
        encoder.encodeInt32("Int32", scalarTestType._int32);
        encoder.encodeUInt32("UInt32", scalarTestType._uInt32);
        encoder.encodeInt64("Int64", scalarTestType._int64);
        encoder.encodeUInt64("UInt64", scalarTestType._uInt64);
        encoder.encodeFloat("Float", scalarTestType._float);
        encoder.encodeDouble("Double", scalarTestType._double);
        encoder.encodeString("String", scalarTestType._string);
        encoder.encodeDateTime("DateTime", scalarTestType._dateTime);
        encoder.encodeGuid("Guid", scalarTestType._guid);
        encoder.encodeByteString("ByteString", scalarTestType._byteString);
        encoder.encodeXmlElement("XmlElement", scalarTestType._xmlElement);
        encoder.encodeNodeId("NodeId", scalarTestType._nodeId);
        encoder.encodeExpandedNodeId("ExpandedNodeId", scalarTestType._expandedNodeId);
        encoder.encodeStatusCode("StatusCode", scalarTestType._statusCode);
        encoder.encodeDiagnosticInfo("DiagnosticInfo", scalarTestType._diagnosticInfo);
        encoder.encodeQualifiedName("QualifiedName", scalarTestType._qualifiedName);
        encoder.encodeLocalizedText("LocalizedText", scalarTestType._localizedText);
        encoder.encodeExtensionObject("ExtensionObject", scalarTestType._extensionObject);
        encoder.encodeDataValue("DataValue", scalarTestType._dataValue);
        encoder.encodeEnumeration("EnumeratedValue", scalarTestType._enumeratedValue);
    }

    public static ScalarTestType decode(UaDecoder decoder) {
        Boolean _boolean = decoder.decodeBoolean("Boolean");
        Byte _sByte = decoder.decodeSByte("SByte");
        UByte _byte = decoder.decodeByte("Byte");
        Short _int16 = decoder.decodeInt16("Int16");
        UShort _uInt16 = decoder.decodeUInt16("UInt16");
        Integer _int32 = decoder.decodeInt32("Int32");
        UInteger _uInt32 = decoder.decodeUInt32("UInt32");
        Long _int64 = decoder.decodeInt64("Int64");
        ULong _uInt64 = decoder.decodeUInt64("UInt64");
        Float _float = decoder.decodeFloat("Float");
        Double _double = decoder.decodeDouble("Double");
        String _string = decoder.decodeString("String");
        DateTime _dateTime = decoder.decodeDateTime("DateTime");
        UUID _guid = decoder.decodeGuid("Guid");
        ByteString _byteString = decoder.decodeByteString("ByteString");
        XmlElement _xmlElement = decoder.decodeXmlElement("XmlElement");
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        ExpandedNodeId _expandedNodeId = decoder.decodeExpandedNodeId("ExpandedNodeId");
        StatusCode _statusCode = decoder.decodeStatusCode("StatusCode");
        DiagnosticInfo _diagnosticInfo = decoder.decodeDiagnosticInfo("DiagnosticInfo");
        QualifiedName _qualifiedName = decoder.decodeQualifiedName("QualifiedName");
        LocalizedText _localizedText = decoder.decodeLocalizedText("LocalizedText");
        ExtensionObject _extensionObject = decoder.decodeExtensionObject("ExtensionObject");
        DataValue _dataValue = decoder.decodeDataValue("DataValue");
        EnumeratedTestType _enumeratedValue = decoder.decodeEnumeration("EnumeratedValue", EnumeratedTestType.class);

        return new ScalarTestType(_boolean, _sByte, _byte, _int16, _uInt16, _int32, _uInt32, _int64, _uInt64, _float, _double, _string, _dateTime, _guid, _byteString, _xmlElement, _nodeId, _expandedNodeId, _statusCode, _diagnosticInfo, _qualifiedName, _localizedText, _extensionObject, _dataValue, _enumeratedValue);
    }

    static {
        DelegateRegistry.registerEncoder(ScalarTestType::encode, ScalarTestType.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ScalarTestType::decode, ScalarTestType.class, BinaryEncodingId, XmlEncodingId);
    }

}
