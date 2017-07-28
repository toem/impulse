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

import com.digitalpetri.opcua.stack.core.util.annotations.UInt32Primitive;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

public class AcknowledgeMessage {

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

    /**
     * @param protocolVersion   The latest version of the OPC UA TCP protocol supported by the Server.
     * @param receiveBufferSize The largest MessageChunk that the sender can receive. This value shall not be larger
     *                          than what the Client requested in the Hello Message. This value shall be greater than
     *                          8192 bytes.
     * @param sendBufferSize    The largest MessageChunk that the sender will send. This value shall not be larger than
     *                          what the Client requested in the Hello Message. This value shall be greater than 8192
     *                          bytes.
     * @param maxMessageSize    The maximum size for any request Message. The maximum size for any request Message. The
     *                          Client shall abort the Message with a Bad_RequestTooLarge StatusCode if a request
     *                          Message exceeds this value. The Message size is calculated using the unencrypted Message
     *                          body. A value of zero indicates that the Server has no limit.
     * @param maxChunkCount     The maximum number of chunks in any request Message. A value of zero indicates that the
     *                          Server has no limit.
     */
    public AcknowledgeMessage(@UInt32Primitive long protocolVersion,
                              @UInt32Primitive long receiveBufferSize,
                              @UInt32Primitive long sendBufferSize,
                              @UInt32Primitive long maxMessageSize,
                              @UInt32Primitive long maxChunkCount) {

        Preconditions.checkArgument(receiveBufferSize > 8192,
                "receiverBufferSize must be greater than 8192");

        Preconditions.checkArgument(sendBufferSize > 8192,
                "sendBufferSize must be greater than 8192");

        this.protocolVersion = protocolVersion;
        this.receiveBufferSize = receiveBufferSize;
        this.sendBufferSize = sendBufferSize;
        this.maxMessageSize = maxMessageSize;
        this.maxChunkCount = maxChunkCount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcknowledgeMessage that = (AcknowledgeMessage) o;

        return maxChunkCount == that.maxChunkCount &&
                maxMessageSize == that.maxMessageSize &&
                protocolVersion == that.protocolVersion &&
                receiveBufferSize == that.receiveBufferSize &&
                sendBufferSize == that.sendBufferSize;
    }

    @Override
    public int hashCode() {
        int result = (int) (protocolVersion ^ (protocolVersion >>> 32));
        result = 31 * result + (int) (receiveBufferSize ^ (receiveBufferSize >>> 32));
        result = 31 * result + (int) (sendBufferSize ^ (sendBufferSize >>> 32));
        result = 31 * result + (int) (maxMessageSize ^ (maxMessageSize >>> 32));
        result = 31 * result + (int) (maxChunkCount ^ (maxChunkCount >>> 32));
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
                .toString();
    }

    public static void encode(AcknowledgeMessage message, ByteBuf buffer) {
        buffer.writeInt((int) message.getProtocolVersion());
		buffer.writeInt((int) message.getReceiveBufferSize());
		buffer.writeInt((int) message.getSendBufferSize());
		buffer.writeInt((int) message.getMaxMessageSize());
		buffer.writeInt((int) message.getMaxChunkCount());
    }

    public static AcknowledgeMessage decode(ByteBuf buffer) {
        return new AcknowledgeMessage(
                buffer.readUnsignedInt(), /*    ProtocolVersion     */
                buffer.readUnsignedInt(), /*    ReceiveBufferSize   */
                buffer.readUnsignedInt(), /*    SendBufferSize      */
                buffer.readUnsignedInt(), /*    MaxMessageSize      */
                buffer.readUnsignedInt()  /*    MaxChunkCount       */
        );
    }

}
