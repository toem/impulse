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

package com.digitalpetri.opcua.stack.client.handlers;

import java.nio.ByteOrder;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.client.config.UaTcpStackClientConfig;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.channel.ChannelConfig;
import com.digitalpetri.opcua.stack.core.channel.ChannelParameters;
import com.digitalpetri.opcua.stack.core.channel.ClientSecureChannel;
import com.digitalpetri.opcua.stack.core.channel.SerializationQueue;
import com.digitalpetri.opcua.stack.core.channel.headers.HeaderDecoder;
import com.digitalpetri.opcua.stack.core.channel.messages.AcknowledgeMessage;
import com.digitalpetri.opcua.stack.core.channel.messages.ErrorMessage;
import com.digitalpetri.opcua.stack.core.channel.messages.HelloMessage;
import com.digitalpetri.opcua.stack.core.channel.messages.MessageType;
import com.digitalpetri.opcua.stack.core.channel.messages.TcpMessageDecoder;
import com.digitalpetri.opcua.stack.core.channel.messages.TcpMessageEncoder;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.enumerated.MessageSecurityMode;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.util.CertificateUtil;
import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.AttributeKey;
import io.netty.util.Timeout;
import org.jooq.lambda.tuple.Tuple1;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class UaTcpClientAcknowledgeHandler extends ByteToMessageCodec<UaRequestFuture> implements HeaderDecoder {

    public static final AttributeKey<List<UaRequestFuture>> KEY_AWAITING_HANDSHAKE =
            AttributeKey.valueOf("awaiting-handshake");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<UaRequestFuture> awaitingHandshake = new CopyOnWriteArrayList<>();

    private volatile Timeout helloTimeout;

    private final ClientSecureChannel secureChannel;

    private final UaTcpStackClient client;
    private final CompletableFuture<ClientSecureChannel> handshakeFuture;

    public UaTcpClientAcknowledgeHandler(UaTcpStackClient client,
                                         Optional<ClientSecureChannel> existingChannel,
                                         CompletableFuture<ClientSecureChannel> handshakeFuture) {

        this.client = client;
        this.handshakeFuture = handshakeFuture;

        UaTcpStackClientConfig config = client.getConfig();

        if (existingChannel.isPresent()) {
            secureChannel = existingChannel.get();
        } else {
            secureChannel = config.getEndpoint()
                    .flatMap(e -> {
                        SecurityPolicy securityPolicy = SecurityPolicy
                                .fromUriSafe(e.getSecurityPolicyUri())
                                .orElse(SecurityPolicy.None);

                        if (securityPolicy == SecurityPolicy.None) {
                            return Optional.empty();
                        } else {
                            return Optional.of(new Tuple1<>(e));
                        }
                    })
                    .flatMap(t1 -> config.getKeyPair().map(t1::concat))
                    .flatMap(t2 -> config.getCertificate().map(t2::concat))
                    .flatMap(t3 -> {
                        EndpointDescription endpoint = t3.v1();
                        KeyPair keyPair = t3.v2();
                        X509Certificate localCertificate = t3.v3();

                        try {
                            X509Certificate remoteCertificate = CertificateUtil
                                    .decodeCertificate(endpoint.getServerCertificate().bytes());

                            List<X509Certificate> remoteCertificateChain = CertificateUtil
                                    .decodeCertificates(endpoint.getServerCertificate().bytes());

                            SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpoint.getSecurityPolicyUri());

                            ClientSecureChannel secureChannel = new ClientSecureChannel(
                                    keyPair,
                                    localCertificate,
                                    remoteCertificate,
                                    remoteCertificateChain,
                                    securityPolicy,
                                    endpoint.getSecurityMode()
                            );

                            return Optional.of(secureChannel);
                        } catch (Throwable t) {
                            return Optional.empty();
                        }
                    })
                    .orElse(new ClientSecureChannel(SecurityPolicy.None, MessageSecurityMode.None));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        helloTimeout = startHelloTimeout(ctx);

        secureChannel.setChannel(ctx.channel());

        HelloMessage hello = new HelloMessage(
                PROTOCOL_VERSION,
                client.getChannelConfig().getMaxChunkSize(),
                client.getChannelConfig().getMaxChunkSize(),
                client.getChannelConfig().getMaxMessageSize(),
                client.getChannelConfig().getMaxChunkCount(),
                client.getEndpointUrl());

        ByteBuf messageBuffer = TcpMessageEncoder.encode(hello);

        ctx.writeAndFlush(messageBuffer);

        logger.debug("Sent Hello message on channel={}.", ctx.channel());

        super.channelActive(ctx);
    }

    private Timeout startHelloTimeout(ChannelHandlerContext ctx) {
        return client.getConfig().getWheelTimer().newTimeout(
                timeout -> {
                    if (!timeout.isCancelled()) {
                        handshakeFuture.completeExceptionally(
                                new UaException(StatusCodes.Bad_Timeout,
                                        "timed out waiting for acknowledge"));
                        ctx.close();
                    }
                },
                5, TimeUnit.SECONDS);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, UaRequestFuture message, ByteBuf out) throws Exception {
        awaitingHandshake.add(message);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.readableBytes() >= HEADER_LENGTH &&
                buffer.readableBytes() >= getMessageLength(buffer)) {

            int messageLength = getMessageLength(buffer);
            MessageType messageType = MessageType.fromMediumInt(buffer.getMedium(buffer.readerIndex()));

            switch (messageType) {
                case Acknowledge:
                    onAcknowledge(ctx, buffer.readSlice(messageLength));
                    break;

                case Error:
                    onError(ctx, buffer.readSlice(messageLength));
                    break;

                default:
                    out.add(buffer.readSlice(messageLength).retain());
            }
        }
    }

    private void onAcknowledge(ChannelHandlerContext ctx, ByteBuf buffer) {
        if (helloTimeout != null && !helloTimeout.cancel()) {
            helloTimeout = null;
            handshakeFuture.completeExceptionally(
                    new UaException(StatusCodes.Bad_Timeout,
                            "timed out waiting for acknowledge"));
            ctx.close();
            return;
        }

        logger.debug("Received Acknowledge message on channel={}.", ctx.channel());

        buffer.skipBytes(3 + 1 + 4); // Skip messageType, chunkType, and messageSize

        AcknowledgeMessage acknowledge = AcknowledgeMessage.decode(buffer);

        long remoteProtocolVersion = acknowledge.getProtocolVersion();
        long remoteReceiveBufferSize = acknowledge.getReceiveBufferSize();
        long remoteSendBufferSize = acknowledge.getSendBufferSize();
        long remoteMaxMessageSize = acknowledge.getMaxMessageSize();
        long remoteMaxChunkCount = acknowledge.getMaxChunkCount();

        if (PROTOCOL_VERSION > remoteProtocolVersion) {
            logger.warn("Client protocol version ({}) does not match server protocol version ({}).",
                    PROTOCOL_VERSION, remoteProtocolVersion);
        }

        ChannelConfig config = client.getChannelConfig();

        /* Our receive buffer size is determined by the remote send buffer size. */
        long localReceiveBufferSize = Math.min(remoteSendBufferSize, config.getMaxChunkSize());

        /* Our send buffer size is determined by the remote receive buffer size. */
        long localSendBufferSize = Math.min(remoteReceiveBufferSize, config.getMaxChunkSize());

        /* Max message size the remote can send us; not influenced by remote configuration. */
        long localMaxMessageSize = config.getMaxMessageSize();

        /* Max chunk count the remote can send us; not influenced by remote configuration. */
        long localMaxChunkCount = config.getMaxChunkCount();

        ChannelParameters parameters = new ChannelParameters(
                Ints.saturatedCast(localMaxMessageSize),
                Ints.saturatedCast(localReceiveBufferSize),
                Ints.saturatedCast(localSendBufferSize),
                Ints.saturatedCast(localMaxChunkCount),
                Ints.saturatedCast(remoteMaxMessageSize),
                Ints.saturatedCast(remoteReceiveBufferSize),
                Ints.saturatedCast(remoteSendBufferSize),
                Ints.saturatedCast(remoteMaxChunkCount)
        );

        ctx.channel().attr(KEY_AWAITING_HANDSHAKE).set(awaitingHandshake);

        ctx.executor().execute(() -> {
            int maxArrayLength = client.getChannelConfig().getMaxArrayLength();
            int maxStringLength = client.getChannelConfig().getMaxStringLength();

            SerializationQueue serializationQueue = new SerializationQueue(
                    client.getConfig().getExecutor(),
                    parameters,
                    maxArrayLength,
                    maxStringLength
            );

            UaTcpClientMessageHandler handler = new UaTcpClientMessageHandler(
                    client,
                    secureChannel,
                    serializationQueue,
                    handshakeFuture
            );

            ctx.pipeline().addLast(handler);
        });
    }

    private void onError(ChannelHandlerContext ctx, ByteBuf buffer) {
        try {
            ErrorMessage errorMessage = TcpMessageDecoder.decodeError(buffer);
            StatusCode statusCode = errorMessage.getError();
            long errorCode = statusCode.getValue();

            boolean secureChannelError =
                    errorCode == StatusCodes.Bad_SecurityChecksFailed ||
                            errorCode == StatusCodes.Bad_TcpSecureChannelUnknown ||
                            errorCode == StatusCodes.Bad_SecureChannelIdInvalid;

            if (secureChannelError) {
                secureChannel.setChannelId(0);
            }

            logger.error("Received error message: " + errorMessage);

            handshakeFuture.completeExceptionally(new UaException(statusCode, errorMessage.getReason()));
        } catch (UaException e) {
            logger.error("An exception occurred while decoding an error message: {}", e.getMessage(), e);

            handshakeFuture.completeExceptionally(e);
        } finally {
            ctx.close();
        }
    }

}
