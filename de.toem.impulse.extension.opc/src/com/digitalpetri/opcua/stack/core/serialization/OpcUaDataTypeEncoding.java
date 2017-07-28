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

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteOrder;
import javax.xml.stream.XMLStreamException;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaSerializationException;
import com.digitalpetri.opcua.stack.core.serialization.binary.BinaryDecoder;
import com.digitalpetri.opcua.stack.core.serialization.binary.BinaryEncoder;
import com.digitalpetri.opcua.stack.core.serialization.xml.XmlDecoder;
import com.digitalpetri.opcua.stack.core.serialization.xml.XmlEncoder;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.XmlElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

public class OpcUaDataTypeEncoding implements DataTypeEncoding {

    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    @Override
    public ByteString encodeToByteString(Object object, NodeId encodingTypeId) {
        EncoderDelegate<Object> delegate = DelegateRegistry.getEncoder(encodingTypeId);

        ByteBuf buffer = allocator.buffer().order(ByteOrder.LITTLE_ENDIAN);

        BinaryEncoder encoder = new BinaryEncoder();
        encoder.setBuffer(buffer);

        delegate.encode(object, encoder);

        byte[] bs = new byte[buffer.readableBytes()];
        buffer.readBytes(bs);
        buffer.release();

        return ByteString.of(bs);
    }

    @Override
    public Object decodeFromByteString(ByteString encoded, NodeId encodingTypeId) {
        DecoderDelegate<Object> delegate = DelegateRegistry.getDecoder(encodingTypeId);

        byte[] bs = encoded.bytes();
        if (bs == null) bs = new byte[0];

        ByteBuf buffer = Unpooled
                .wrappedBuffer(bs)
                .order(ByteOrder.LITTLE_ENDIAN);

        BinaryDecoder decoder = new BinaryDecoder();
        decoder.setBuffer(buffer);

        return delegate.decode(decoder);
    }

    @Override
    public XmlElement encodeToXmlElement(Object object, NodeId encodingTypeId) {
        try {
            EncoderDelegate<Object> delegate = DelegateRegistry.getEncoder(encodingTypeId);

            StringWriter stringWriter = new StringWriter();

            XmlEncoder encoder = new XmlEncoder();
            encoder.setOutput(stringWriter);

            delegate.encode(object, encoder);

            return new XmlElement(stringWriter.toString());
        } catch (XMLStreamException e) {
            throw new UaSerializationException(StatusCodes.Bad_EncodingError, e);
        }
    }

    @Override
    public Object decodeFromXmlElement(XmlElement encoded, NodeId encodingTypeId) {
        try {
            DecoderDelegate<Object> delegate = DelegateRegistry.getDecoder(encodingTypeId);

            XmlDecoder decoder = new XmlDecoder();
            decoder.setInput(new StringReader(encoded.getFragment()));

            return delegate.decode(decoder);
        } catch (XMLStreamException e) {
            throw new UaSerializationException(StatusCodes.Bad_DecodingError, e);
        }
    }

}
