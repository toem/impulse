/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.structured;

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

public class MonitoredItemNotification implements UaStructure {

    public static final NodeId TypeId = Identifiers.MonitoredItemNotification;
    public static final NodeId BinaryEncodingId = Identifiers.MonitoredItemNotification_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.MonitoredItemNotification_Encoding_DefaultXml;

    protected final UInteger clientHandle;
    protected final DataValue value;

    public MonitoredItemNotification() {
        this.clientHandle = null;
        this.value = null;
    }

    public MonitoredItemNotification(UInteger clientHandle, DataValue value) {
        this.clientHandle = clientHandle;
        this.value = value;
    }

    public UInteger getClientHandle() { return clientHandle; }

    public DataValue getValue() { return value; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ClientHandle", clientHandle)
            .add("Value", value)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<MonitoredItemNotification> {

        @Override
        public Class<MonitoredItemNotification> getType() {
            return MonitoredItemNotification.class;
        }

        @Override
        public MonitoredItemNotification decode(UaDecoder decoder) throws UaSerializationException {
            UInteger clientHandle = decoder.readUInt32("ClientHandle");
            DataValue value = decoder.readDataValue("Value");

            return new MonitoredItemNotification(clientHandle, value);
        }

        @Override
        public void encode(MonitoredItemNotification value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeUInt32("ClientHandle", value.clientHandle);
            encoder.writeDataValue("Value", value.value);
        }
    }

}
