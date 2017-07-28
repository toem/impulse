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

package com.digitalpetri.opcua.stack.core.channel.messages;

import javax.annotation.Nonnull;

import com.digitalpetri.opcua.stack.core.serialization.binary.BinaryDecoder;
import com.digitalpetri.opcua.stack.core.serialization.binary.BinaryEncoder;
import com.digitalpetri.opcua.stack.core.util.annotations.UInt32Primitive;
import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;

import static com.google.common.base.Preconditions.checkArgument;

public class HelloMessage {

    @UInt32Primitive
    private final long protocolVersion;

    @UInt32Primitive
    private final long receiveBufferSize;

    @UInt32Primitive
    private final long sendBufferSize;

    @UInt32Primitive
    private final long maxMessageSize;

    @UInt32Primitive
    private final long maxChunkCount;

    private final String endpointUrl;

    /**
     * @param protocolVersion   the latest version of the OPC UA TCP protocol supported by the Client.
     * @param receiveBufferSize the largest MessageChunk that the sender (Client) can receive. This value shall be
     *                          greater than 8192 bytes.
     * @param sendBufferSize    the largest MessageChunk that the sender (Client) will send. This value shall be
     *                          greater
     *                          than 8192 bytes.
     * @param maxMessageSize    the maximum size for any response Message. The Message size is calculated using the
     *                          unencrypted Message body. A value of zero indicates that the Client has no limit.
     * @param maxChunkCount     the maximum number of chunks in any response Message. A value of zero indicates that
     *                          the Client has no limit.
     * @param endpointUrl       the URL of the Endpoint which the Client wished to connect to. The encoded value shall
     *                          be less than 4096 bytes.
     */
    public HelloMessage(@UInt32Primitive long protocolVersion,
                        @UInt32Primitive long receiveBufferSize,
                        @UInt32Primitive long sendBufferSize,
                        @UInt32Primitive long maxMessageSize,
                        @UInt32Primitive long maxChunkCount,
                        @Nonnull String endpointUrl) {

        checkArgument(receiveBufferSize >= 8192, "receiverBufferSize must be at least 8192 bytes");
        checkArgument(sendBufferSize >= 8192, "sendBufferSize must be at least 8192 bytes");
        checkArgument(endpointUrl.length() <= 4096, "endpointUrl length cannot be greater than 4096 bytes");

        this.protocolVersion = protocolVersion;
        this.receiveBufferSize = receiveBufferSize;
        this.sendBufferSize = sendBufferSize;
        this.maxMessageSize = maxMessageSize;
        this.maxChunkCount = maxChunkCount;
        this.endpointUrl = endpointUrl;
    }

    @UInt32Primitive
    public long getProtocolVersion() {
        return protocolVersion;
    }

    @UInt32Primitive
    public long getReceiveBufferSize() {
        return receiveBufferSize;
    }

    @UInt32Primitive
    public long getSendBufferSize() {
        return sendBufferSize;
    }

    @UInt32Primitive
    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    @UInt32Primitive
    public long getMaxChunkCount() {
        return maxChunkCount;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HelloMessage that = (HelloMessage) o;

        return maxChunkCount == that.maxChunkCount &&
                maxMessageSize == that.maxMessageSize &&
                protocolVersion == that.protocolVersion &&
                receiveBufferSize == that.receiveBufferSize &&
                sendBufferSize == that.sendBufferSize &&
                endpointUrl.equals(that.endpointUrl);

    }

    @Override
    public int hashCode() {
        int result = (int) (protocolVersion ^ (protocolVersion >>> 32));
        result = 31 * result + (int) (receiveBufferSize ^ (receiveBufferSize >>> 32));
        result = 31 * result + (int) (sendBufferSize ^ (sendBufferSize >>> 32));
        result = 31 * result + (int) (maxMessageSize ^ (maxMessageSize >>> 32));
        result = 31 * result + (int) (maxChunkCount ^ (maxChunkCount >>> 32));
        result = 31 * result + endpointUrl.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("protocolVersion", protocolVersion)
                .add("receiverBufferSize", receiveBufferSize)
                .add("sendBufferSize", sendBufferSize)
                .add("maxMessageSize", maxMessageSize)
                .add("maxChunkCount", maxChunkCount)
                .add("endpointUrl", endpointUrl)
                .toString();
    }

    public static void encode(HelloMessage message, ByteBuf buffer) {
        buffer.writeInt((int) message.getProtocolVersion());
        buffer.writeInt((int) message.getReceiveBufferSize());
        buffer.writeInt((int) message.getSendBufferSize());
        buffer.writeInt((int) message.getMaxMessageSize());
        buffer.writeInt((int) message.getMaxChunkCount());
        encodeString(message.getEndpointUrl(), buffer);
    }

    public static HelloMessage decode(ByteBuf buffer) {
        return new HelloMessage(
                buffer.readUnsignedInt(), /*    ProtocolVersion    */
                buffer.readUnsignedInt(), /*    ReceiveBufferSize  */
                buffer.readUnsignedInt(), /*    SendBufferSize     */
                buffer.readUnsignedInt(), /*    MaxMessageSize     */
                buffer.readUnsignedInt(), /*    MaxChunkCount      */
                decodeString(buffer)      /*    EndpointUrl        */
        );
    }

    private static void encodeString(String s, ByteBuf buffer) {
        new BinaryEncoder().setBuffer(buffer).encodeString(null, s);
    }

    private static String decodeString(ByteBuf buffer) {
        return new BinaryDecoder().setBuffer(buffer).decodeString(null);
    }

}
