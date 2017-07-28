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

package com.digitalpetri.opcua.stack.core.channel.headers;

import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.channel.messages.MessageType;
import io.netty.buffer.ByteBuf;

public final class SecureMessageHeader {

    public static final int SECURE_MESSAGE_HEADER_SIZE = 12;

    private final MessageType messageType;
    private final char finalFlag;
    private final long messageSize;
    private final long secureChannelId;

    /**
     * @param messageType     the {@link MessageType} of the following message.
     * @param finalFlag       a one byte ASCII code that indicates whether the MessageChunk is the final chunk in a
     *                        Message. The following values are defined at this time: 'C', an intermediate chunk, 'F',
     *                        the final chunk, and  'A', the final chunk (used when an error occurred and the Message
     *                        is aborted).
     * @param messageSize     the length of the MessageChunk, in bytes. This value includes size of the Message header.
     * @param secureChannelId a unique identifier for the SecureChannel assigned by the Server.
     */
    public SecureMessageHeader(MessageType messageType, char finalFlag, long messageSize, long secureChannelId) {
        this.messageType = messageType;
        this.finalFlag = finalFlag;
        this.messageSize = messageSize;
        this.secureChannelId = secureChannelId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public char getChunkType() {
        return finalFlag;
    }

    public long getMessageSize() {
        return messageSize;
    }

    public long getSecureChannelId() {
        return secureChannelId;
    }

    public static void encode(SecureMessageHeader header, ByteBuf buffer) throws UaException {
        buffer.writeMedium(MessageType.toMediumInt(header.getMessageType()));
        buffer.writeByte(header.getChunkType());
        buffer.writeInt((int) header.getMessageSize());
        buffer.writeInt((int) header.getSecureChannelId());
    }

    public static SecureMessageHeader decode(ByteBuf buffer) throws UaException {
        return new SecureMessageHeader(
                MessageType.fromMediumInt(buffer.readMedium()),
                (char) buffer.readByte(),
                buffer.readUnsignedInt(),
                buffer.readUnsignedInt()
        );
    }

}
