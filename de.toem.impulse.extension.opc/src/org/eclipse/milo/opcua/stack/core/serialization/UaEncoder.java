/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.serialization;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.DataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;

public interface UaEncoder {

    void writeBoolean(String field, Boolean value) throws UaSerializationException;

    void writeSByte(String field, Byte value) throws UaSerializationException;

    void writeInt16(String field, Short value) throws UaSerializationException;

    void writeInt32(String field, Integer value) throws UaSerializationException;

    void writeInt64(String field, Long value) throws UaSerializationException;

    void writeByte(String field, UByte value) throws UaSerializationException;

    void writeUInt16(String field, UShort value) throws UaSerializationException;

    void writeUInt32(String field, UInteger value) throws UaSerializationException;

    void writeUInt64(String field, ULong value) throws UaSerializationException;

    void writeFloat(String field, Float value) throws UaSerializationException;

    void writeDouble(String field, Double value) throws UaSerializationException;

    void writeString(String field, String value) throws UaSerializationException;

    void writeDateTime(String field, DateTime value) throws UaSerializationException;

    void writeGuid(String field, UUID value) throws UaSerializationException;

    void writeByteString(String field, ByteString value) throws UaSerializationException;

    void writeXmlElement(String field, XmlElement value) throws UaSerializationException;

    void writeNodeId(String field, NodeId value) throws UaSerializationException;

    void writeExpandedNodeId(String field, ExpandedNodeId value) throws UaSerializationException;

    void writeStatusCode(String field, StatusCode value) throws UaSerializationException;

    void writeQualifiedName(String field, QualifiedName value) throws UaSerializationException;

    void writeLocalizedText(String field, LocalizedText value) throws UaSerializationException;

    void writeExtensionObject(String field, ExtensionObject value) throws UaSerializationException;

    void writeDataValue(String field, DataValue value) throws UaSerializationException;

    void writeVariant(String field, Variant value) throws UaSerializationException;

    void writeDiagnosticInfo(String field, DiagnosticInfo value) throws UaSerializationException;

    void writeMessage(String field, UaMessage message) throws UaSerializationException;

    void writeStruct(String field, Object value, NodeId dataTypeId) throws UaSerializationException;

    void writeStruct(String field, Object value, ExpandedNodeId dataTypeId) throws UaSerializationException;

    void writeStruct(String field, Object value, DataTypeCodec codec) throws UaSerializationException;

    void writeBooleanArray(String field, Boolean[] value) throws UaSerializationException;

    void writeSByteArray(String field, Byte[] value) throws UaSerializationException;

    void writeInt16Array(String field, Short[] value) throws UaSerializationException;

    void writeInt32Array(String field, Integer[] value) throws UaSerializationException;

    void writeInt64Array(String field, Long[] value) throws UaSerializationException;

    void writeByteArray(String field, UByte[] value) throws UaSerializationException;

    void writeUInt16Array(String field, UShort[] value) throws UaSerializationException;

    void writeUInt32Array(String field, UInteger[] value) throws UaSerializationException;

    void writeUInt64Array(String field, ULong[] value) throws UaSerializationException;

    void writeFloatArray(String field, Float[] value) throws UaSerializationException;

    void writeDoubleArray(String field, Double[] value) throws UaSerializationException;

    void writeStringArray(String field, String[] value) throws UaSerializationException;

    void writeDateTimeArray(String field, DateTime[] value) throws UaSerializationException;

    void writeGuidArray(String field, UUID[] value) throws UaSerializationException;

    void writeByteStringArray(String field, ByteString[] value) throws UaSerializationException;

    void writeXmlElementArray(String field, XmlElement[] value) throws UaSerializationException;

    void writeNodeIdArray(String field, NodeId[] value) throws UaSerializationException;

    void writeExpandedNodeIdArray(String field, ExpandedNodeId[] value) throws UaSerializationException;

    void writeStatusCodeArray(String field, StatusCode[] value) throws UaSerializationException;

    void writeQualifiedNameArray(String field, QualifiedName[] value) throws UaSerializationException;

    void writeLocalizedTextArray(String field, LocalizedText[] value) throws UaSerializationException;

    void writeExtensionObjectArray(String field, ExtensionObject[] value) throws UaSerializationException;

    void writeDataValueArray(String field, DataValue[] value) throws UaSerializationException;

    void writeVariantArray(String field, Variant[] value) throws UaSerializationException;

    void writeDiagnosticInfoArray(String field, DiagnosticInfo[] value) throws UaSerializationException;

    void writeStructArray(String field, Object[] value, NodeId dataTypeId) throws UaSerializationException;

    void writeStructArray(String field, Object[] value, ExpandedNodeId dataTypeId) throws UaSerializationException;

    <T> void writeArray(String field, T[] values, BiConsumer<String, T> encoder) throws UaSerializationException;

    @Deprecated
    <T extends UaStructure> void writeBuiltinStruct(
        String field,
        T value,
        Class<T> clazz
    ) throws UaSerializationException;

    @Deprecated
    <T extends UaStructure> void writeBuiltinStructArray(
        String field,
        T[] values,
        Class<T> clazz
    ) throws UaSerializationException;

}
