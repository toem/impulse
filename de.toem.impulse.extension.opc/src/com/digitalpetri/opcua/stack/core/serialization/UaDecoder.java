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

package com.digitalpetri.opcua.stack.core.serialization;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.digitalpetri.opcua.stack.core.UaSerializationException;
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
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.XmlElement;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.ULong;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;

public interface UaDecoder {

    Boolean decodeBoolean(String field) throws UaSerializationException;

    Byte decodeSByte(String field) throws UaSerializationException;

    Short decodeInt16(String field) throws UaSerializationException;

    Integer decodeInt32(String field) throws UaSerializationException;

    Long decodeInt64(String field) throws UaSerializationException;

    UByte decodeByte(String field) throws UaSerializationException;

    UShort decodeUInt16(String field) throws UaSerializationException;

    UInteger decodeUInt32(String field) throws UaSerializationException;

    ULong decodeUInt64(String field) throws UaSerializationException;

    Float decodeFloat(String field) throws UaSerializationException;

    Double decodeDouble(String field) throws UaSerializationException;

    String decodeString(String field) throws UaSerializationException;

    DateTime decodeDateTime(String field) throws UaSerializationException;

    UUID decodeGuid(String field) throws UaSerializationException;

    ByteString decodeByteString(String field) throws UaSerializationException;

    XmlElement decodeXmlElement(String field) throws UaSerializationException;

    NodeId decodeNodeId(String field) throws UaSerializationException;

    ExpandedNodeId decodeExpandedNodeId(String field) throws UaSerializationException;

    StatusCode decodeStatusCode(String field) throws UaSerializationException;

    QualifiedName decodeQualifiedName(String field) throws UaSerializationException;

    LocalizedText decodeLocalizedText(String field) throws UaSerializationException;

    ExtensionObject decodeExtensionObject(String field) throws UaSerializationException;

    DataValue decodeDataValue(String field) throws UaSerializationException;

    Variant decodeVariant(String field) throws UaSerializationException;

    DiagnosticInfo decodeDiagnosticInfo(String field) throws UaSerializationException;

    <T extends UaStructure> T decodeMessage(String field) throws UaSerializationException;

    <T extends UaEnumeration> T decodeEnumeration(String field, Class<T> clazz) throws UaSerializationException;

    <T extends UaSerializable> T decodeSerializable(String field, Class<T> clazz) throws UaSerializationException;

    <T> T[] decodeArray(String field, Function<String, T> decoder, Class<T> clazz) throws UaSerializationException;

    <T> T[] decodeArray(String field, BiFunction<String, Class<T>, T> decoder, Class<T> clazz) throws UaSerializationException;

}
