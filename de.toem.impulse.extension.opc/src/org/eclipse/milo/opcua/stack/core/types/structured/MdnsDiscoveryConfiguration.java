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

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class MdnsDiscoveryConfiguration extends DiscoveryConfiguration {

    public static final NodeId TypeId = Identifiers.MdnsDiscoveryConfiguration;
    public static final NodeId BinaryEncodingId = Identifiers.MdnsDiscoveryConfiguration_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.MdnsDiscoveryConfiguration_Encoding_DefaultXml;

    protected final String mdnsServerName;
    protected final String[] serverCapabilities;

    public MdnsDiscoveryConfiguration() {
        super();
        this.mdnsServerName = null;
        this.serverCapabilities = null;
    }

    public MdnsDiscoveryConfiguration(String mdnsServerName, String[] serverCapabilities) {
        super();
        this.mdnsServerName = mdnsServerName;
        this.serverCapabilities = serverCapabilities;
    }

    public String getMdnsServerName() { return mdnsServerName; }

    @Nullable
    public String[] getServerCapabilities() { return serverCapabilities; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("MdnsServerName", mdnsServerName)
            .add("ServerCapabilities", serverCapabilities)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<MdnsDiscoveryConfiguration> {

        @Override
        public Class<MdnsDiscoveryConfiguration> getType() {
            return MdnsDiscoveryConfiguration.class;
        }

        @Override
        public MdnsDiscoveryConfiguration decode(UaDecoder decoder) throws UaSerializationException {
            String mdnsServerName = decoder.readString("MdnsServerName");
            String[] serverCapabilities = decoder.readArray("ServerCapabilities", decoder::readString, String.class);

            return new MdnsDiscoveryConfiguration(mdnsServerName, serverCapabilities);
        }

        @Override
        public void encode(MdnsDiscoveryConfiguration value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeString("MdnsServerName", value.mdnsServerName);
            encoder.writeArray("ServerCapabilities", value.serverCapabilities, encoder::writeString);
        }
    }

}
