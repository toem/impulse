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
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class NetworkGroupDataType implements UaStructure {

    public static final NodeId TypeId = Identifiers.NetworkGroupDataType;
    public static final NodeId BinaryEncodingId = Identifiers.NetworkGroupDataType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.NetworkGroupDataType_Encoding_DefaultXml;

    protected final String serverUri;
    protected final EndpointUrlListDataType[] networkPaths;

    public NetworkGroupDataType() {
        this.serverUri = null;
        this.networkPaths = null;
    }

    public NetworkGroupDataType(String serverUri, EndpointUrlListDataType[] networkPaths) {
        this.serverUri = serverUri;
        this.networkPaths = networkPaths;
    }

    public String getServerUri() { return serverUri; }

    @Nullable
    public EndpointUrlListDataType[] getNetworkPaths() { return networkPaths; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ServerUri", serverUri)
            .add("NetworkPaths", networkPaths)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<NetworkGroupDataType> {

        @Override
        public Class<NetworkGroupDataType> getType() {
            return NetworkGroupDataType.class;
        }

        @Override
        public NetworkGroupDataType decode(UaDecoder decoder) throws UaSerializationException {
            String serverUri = decoder.readString("ServerUri");
            EndpointUrlListDataType[] networkPaths =
                decoder.readBuiltinStructArray(
                    "NetworkPaths",
                    EndpointUrlListDataType.class
                );

            return new NetworkGroupDataType(serverUri, networkPaths);
        }

        @Override
        public void encode(NetworkGroupDataType value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeString("ServerUri", value.serverUri);
            encoder.writeBuiltinStructArray(
                "NetworkPaths",
                value.networkPaths,
                EndpointUrlListDataType.class
            );
        }
    }

}
