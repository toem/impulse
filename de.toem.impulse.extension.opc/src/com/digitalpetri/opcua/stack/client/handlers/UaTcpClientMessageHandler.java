/*
 * Copyright 2016 Kevin Herron
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.UaRuntimeException;
import com.digitalpetri.opcua.stack.core.UaServiceFaultException;
import com.digitalpetri.opcua.stack.core.channel.ChannelSecurity;
import com.digitalpetri.opcua.stack.core.channel.ClientSecureChannel;
import com.digitalpetri.opcua.stack.core.channel.MessageAbortedException;
import com.digitalpetri.opcua.stack.core.channel.SerializationQueue;
import com.digitalpetri.opcua.stack.core.channel.headers.AsymmetricSecurityHeader;
import com.digitalpetri.opcua.stack.core.channel.headers.HeaderDecoder;
import com.digitalpetri.opcua.stack.core.channel.messages.ErrorMessage;
import com.digitalpetri.opcua.stack.core.channel.messages.MessageType;
import com.digitalpetri.opcua.stack.core.channel.messages.TcpMessageDecoder;
import com.digitalpetri.opcua.stack.core.security.SecurityAlgorithm;
import com.digitalpetri.opcua.stack.core.serialization.UaRequestMessage;
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.SecurityTokenRequestType;
import com.digitalpetri.opcua.stack.core.types.structured.ChannelSecurityToken;
import com.digitalpetri.opcua.stack.core.types.structured.CloseSecureChannelRequest;
import com.digitalpetri.opcua.stack.core.types.structured.OpenSecureChannelRequest;
import com.digitalpetri.opcua.stack.core.types.structured.OpenSecureChannelResponse;
import com.digitalpetri.opcua.stack.core.types.structured.RequestHeader;
import com.digitalpetri.opcua.stack.core.types.structured.ServiceFault;
import com.digitalpetri.opcua.stack.core.util.BufferUtil;
import com.digitalpetri.opcua.stack.core.util.LongSequence;
import com.digitalpetri.opcua.stack.core.util.NonceUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.AttributeKey;
import io.netty.util.Timeout;
import org.jooq.lambda.tuple.Tuple2;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class UaTcpClientMessageHandler extends ByteToMessageCodec<UaRequestFuture> implements HeaderDecoder {

    public static final AttributeKey<Map<Long, UaRequestFuture>> KEY_PENDING_REQUEST_FUTURES =
            AttributeKey.valueOf("pending-request-futures");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<ByteBuf> chunkBuffers = new LinkedList<>();

    private final AtomicReference<AsymmetricSecurityHeader> headerRef = new AtomicReference<>();

    private ScheduledFuture renewFuture;
    private Timeout secureChannelTimeout;

    private final Map<Long, UaRequestFuture> pending;
    private final LongSequence requestIdSequence;

    private final UaTcpStackClient client;
    private final ClientSecureChannel secureChannel;
    private final SerializationQueue serializationQueue;
    private final CompletableFuture<ClientSecureChannel> handshakeFuture;

    public UaTcpClientMessageHandler(
            UaTcpStackClient client,
            ClientSecureChannel secureChannel,
            SerializationQueue serializationQueue,
            CompletableFuture<ClientSecureChannel> handshakeFuture) {

        this.client = client;
        this.secureChannel = secureChannel;
        this.serializationQueue = serializationQueue;
        this.handshakeFuture = handshakeFuture;

        secureChannel
                .attr(KEY_PENDING_REQUEST_FUTURES)
                .setIfAbsent(Maps.newConcurrentMap());

        pending = secureChannel.attr(KEY_PENDING_REQUEST_FUTURES).get();

        secureChannel
                .attr(ClientSecureChannel.KEY_REQUEST_ID_SEQUENCE)
                .setIfAbsent(new LongSequence(1L, UInteger.MAX_VALUE));

        requestIdSequence = secureChannel.attr(ClientSecureChannel.KEY_REQUEST_ID_SEQUENCE).get();

        handshakeFuture.thenAccept(sc -> {
            Channel channel = sc.getChannel();

            channel.eventLoop().execute(() -> {
                List<UaRequestFuture> awaitingHandshake = channel.attr(
                        UaTcpClientAcknowledgeHandler.KEY_AWAITING_HANDSHAKE).get();

                if (awaitingHandshake != null) {
                    channel.attr(UaTcpClientAcknowledgeHandler.KEY_AWAITING_HANDSHAKE).remove();

                    logger.debug("{} message(s) queued before handshake completed; sending now.", awaitingHandshake.size());
                    awaitingHandshake.forEach(channel::writeAndFlush);
                }
            });
        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        SecurityTokenRequestType requestType = secureChannel.getChannelId() == 0 ?
                SecurityTokenRequestType.Issue : SecurityTokenRequestType.Renew;

        secureChannelTimeout = client.getConfig().getWheelTimer().newTimeout(
                timeout -> {
                    if (!timeout.isCancelled()) {
                        handshakeFuture.completeExceptionally(
                                new UaException(
                                        StatusCodes.Bad_Timeout,
                                        "timed out waiting for secure channel"));
                        ctx.close();
                    }
                },
                5, TimeUnit.SECONDS
        );

        logger.debug("OpenSecureChannel timeout scheduled for +5s");

        sendOpenSecureChannelRequest(ctx, requestType);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (renewFuture != null) renewFuture.cancel(false);

        handshakeFuture.completeExceptionally(
                new UaException(StatusCodes.Bad_ConnectionClosed, "connection closed"));

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught: {}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof CloseSecureChannelRequest) {
            sendCloseSecureChannelRequest(ctx, (CloseSecureChannelRequest) evt);
        }
    }

    private void sendOpenSecureChannelRequest(ChannelHandlerContext ctx, SecurityTokenRequestType requestType) {
        SecurityAlgorithm algorithm = secureChannel.getSecurityPolicy().getSymmetricEncryptionAlgorithm();
        int nonceLength = NonceUtil.getNonceLength(algorithm);

        ByteString clientNonce = secureChannel.isSymmetricSigningEnabled() ?
                NonceUtil.generateNonce(nonceLength) :
                ByteString.NULL_VALUE;

        secureChannel.setLocalNonce(clientNonce);

        OpenSecureChannelRequest request = new OpenSecureChannelRequest(
                new RequestHeader(null, DateTime.now(), uint(0), uint(0), null, uint(0), null),
                uint(PROTOCOL_VERSION),
                requestType,
                secureChannel.getMessageSecurityMode(),
                secureChannel.getLocalNonce(),
                client.getChannelLifetime());

        encodeMessage(request, MessageType.OpenSecureChannel).whenComplete((t2, ex) -> {
            if (ex != null) {
                ctx.close();
                return;
            }

            List<ByteBuf> chunks = t2.v2();

            ctx.executor().execute(() -> {
                chunks.forEach(c -> ctx.write(c, ctx.voidPromise()));
                ctx.flush();
            });

            ChannelSecurity channelSecurity = secureChannel.getChannelSecurity();

            long currentTokenId = channelSecurity != null ?
                    channelSecurity.getCurrentToken().getTokenId().longValue() : -1L;

            long previousTokenId = channelSecurity != null ?
                    channelSecurity.getPreviousToken().map(token -> token.getTokenId().longValue()).orElse(-1L) : -1L;

            logger.debug(
                    "Sent OpenSecureChannelRequest ({}, id={}, currentToken={}, previousToken={}).",
                    request.getRequestType(), secureChannel.getChannelId(), currentTokenId, previousTokenId);
        });
    }

    private void sendCloseSecureChannelRequest(ChannelHandlerContext ctx, CloseSecureChannelRequest request) {
        encodeMessage(request, MessageType.CloseSecureChannel).whenComplete((t2, ex) -> {
            if (ex != null) {
                ctx.close();
                return;
            }

            List<ByteBuf> chunks = t2.v2();

            ctx.executor().execute(() -> {
                chunks.forEach(c -> ctx.write(c, ctx.voidPromise()));
                ctx.flush();
                ctx.close();
            });

            secureChannel.setChannelId(0);
        });
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, UaRequestFuture request, ByteBuf buffer) throws Exception {
        encodeMessage(request.getRequest(), MessageType.SecureMessage).whenComplete((t2, ex) -> {
            if (ex != null) {
                ctx.close();
                return;
            }

            long requestId = t2.v1();
            List<ByteBuf> chunks = t2.v2();

            pending.put(requestId, request);

            // No matter how we complete, make sure the entry in pending is removed.
            // This covers the case where the request fails due to a timeout in the
            // upper layers as well as normal completion.
            request.getFuture().whenComplete((r, x) -> pending.remove(requestId));

            ctx.executor().execute(() -> {
                chunks.forEach(c -> ctx.write(c, ctx.voidPromise()));
                ctx.flush();
            });
        });
    }

    private CompletableFuture<Tuple2<Long, List<ByteBuf>>> encodeMessage(
            UaRequestMessage request,
            MessageType messageType) {

        CompletableFuture<Tuple2<Long, List<ByteBuf>>> future = new CompletableFuture<>();

        serializationQueue.encode((binaryEncoder, chunkEncoder) -> {
            ByteBuf messageBuffer = null;

            try {
                messageBuffer = BufferUtil.buffer();
                binaryEncoder.setBuffer(messageBuffer);
                binaryEncoder.encodeMessage(null, request);

                List<ByteBuf> chunks;

                if (messageType == MessageType.OpenSecureChannel) {
                    chunks = chunkEncoder.encodeAsymmetric(
                            secureChannel,
                            messageType,
                            messageBuffer,
                            requestIdSequence.getAndIncrement()
                    );
                } else {
                    chunks = chunkEncoder.encodeSymmetric(
                            secureChannel,
                            messageType,
                            messageBuffer,
                            requestIdSequence.getAndIncrement()
                    );
                }

                future.complete(new Tuple2<>(chunkEncoder.getLastRequestId(), chunks));
            } catch (UaException ex) {
                logger.error("Error encoding {}: {}", request, ex.getMessage(), ex);

                future.completeExceptionally(ex);
            } finally {
                if (messageBuffer != null) {
                    messageBuffer.release();
                }
            }
        });

        return future;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readableBytes() >= HEADER_LENGTH) {
            buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

            if (buffer.readableBytes() >= getMessageLength(buffer)) {
                decodeMessage(ctx, buffer);
            }
        }
    }

    private void decodeMessage(ChannelHandlerContext ctx, ByteBuf buffer) throws UaException {
        int messageLength = getMessageLength(buffer);
        MessageType messageType = MessageType.fromMediumInt(buffer.getMedium(buffer.readerIndex()));

        switch (messageType) {
            case OpenSecureChannel:
                onOpenSecureChannel(ctx, buffer.readSlice(messageLength));
                break;

            case SecureMessage:
                onSecureMessage(ctx, buffer.readSlice(messageLength));
                break;

            case Error:
                onError(ctx, buffer.readSlice(messageLength));
                break;

            default:
                throw new UaException(
                        StatusCodes.Bad_TcpMessageTypeInvalid,
                        "unexpected MessageType: " + messageType);
        }
    }

    private boolean accumulateChunk(ByteBuf buffer) throws UaException {
        int maxChunkCount = serializationQueue.getParameters().getLocalMaxChunkCount();
        int maxChunkSize = serializationQueue.getParameters().getLocalReceiveBufferSize();

        int chunkSize = buffer.readerIndex(0).readableBytes();

        if (chunkSize > maxChunkSize) {
            throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                    String.format("max chunk size exceeded (%s)", maxChunkSize));
        }

        chunkBuffers.add(buffer.retain());

        if (chunkBuffers.size() > maxChunkCount) {
            throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                    String.format("max chunk count exceeded (%s)", maxChunkCount));
        }

        char chunkType = (char) buffer.getByte(3);

        return (chunkType == 'A' || chunkType == 'F');
    }

    private void onOpenSecureChannel(ChannelHandlerContext ctx, ByteBuf buffer) throws UaException {
        if (secureChannelTimeout != null) {
            if (secureChannelTimeout.cancel()) {
                logger.debug("OpenSecureChannel timeout canceled");

                secureChannelTimeout = null;
            } else {
                logger.warn("timed out waiting for secure channel");

                handshakeFuture.completeExceptionally(
                        new UaException(StatusCodes.Bad_Timeout,
                                "timed out waiting for secure channel"));
                ctx.close();
                return;
            }
        }

        buffer.skipBytes(3 + 1 + 4 + 4); // skip messageType, chunkType, messageSize, secureChannelId

        AsymmetricSecurityHeader securityHeader = AsymmetricSecurityHeader.decode(buffer);
        if (!headerRef.compareAndSet(null, securityHeader)) {
            if (!securityHeader.equals(headerRef.get())) {
                throw new UaException(StatusCodes.Bad_SecurityChecksFailed,
                        "subsequent AsymmetricSecurityHeader did not match");
            }
        }

        if (accumulateChunk(buffer)) {
            final List<ByteBuf> buffersToDecode = ImmutableList.copyOf(chunkBuffers);
            chunkBuffers = new LinkedList<>();

            serializationQueue.decode((binaryDecoder, chunkDecoder) -> {
                ByteBuf decodedBuffer = null;

                try {
                    decodedBuffer = chunkDecoder.decodeAsymmetric(secureChannel, buffersToDecode);

                    UaResponseMessage responseMessage = binaryDecoder
                            .setBuffer(decodedBuffer)
                            .decodeMessage(null);

                    StatusCode serviceResult = responseMessage.getResponseHeader().getServiceResult();

                    if (serviceResult.isGood()) {
                        OpenSecureChannelResponse response = (OpenSecureChannelResponse) responseMessage;

                        secureChannel.setChannelId(response.getSecurityToken().getChannelId().longValue());
                        logger.debug("Received OpenSecureChannelResponse.");

                        installSecurityToken(ctx, response);

                        handshakeFuture.complete(secureChannel);
                    } else {
                        ServiceFault serviceFault = (responseMessage instanceof ServiceFault) ?
                                (ServiceFault) responseMessage :
                                new ServiceFault(responseMessage.getResponseHeader());

                        throw new UaServiceFaultException(serviceFault);
                    }
                } catch (MessageAbortedException e) {
                    logger.error("Received message abort chunk; error={}, reason={}", e.getStatusCode(), e.getMessage());
                    ctx.close();
                } catch (Throwable t) {
                    logger.error("Error decoding OpenSecureChannelResponse: {}", t.getMessage(), t);
                    ctx.close();
                } finally {
                    if (decodedBuffer != null) {
                        decodedBuffer.release();
                    }
                }
            });
        }
    }

    private void installSecurityToken(ChannelHandlerContext ctx, OpenSecureChannelResponse response) {
        ChannelSecurity.SecuritySecrets newKeys = null;
        if (response.getServerProtocolVersion().longValue() < PROTOCOL_VERSION) {
            throw new UaRuntimeException(StatusCodes.Bad_ProtocolVersionUnsupported,
                    "server protocol version unsupported: " + response.getServerProtocolVersion());
        }

        ChannelSecurityToken newToken = response.getSecurityToken();

        if (secureChannel.isSymmetricSigningEnabled()) {
            secureChannel.setRemoteNonce(response.getServerNonce());

            newKeys = ChannelSecurity.generateKeyPair(
                    secureChannel,
                    secureChannel.getLocalNonce(),
                    secureChannel.getRemoteNonce()
            );
        }

        ChannelSecurity oldSecrets = secureChannel.getChannelSecurity();
        ChannelSecurity.SecuritySecrets oldKeys = oldSecrets != null ? oldSecrets.getCurrentKeys() : null;
        ChannelSecurityToken oldToken = oldSecrets != null ? oldSecrets.getCurrentToken() : null;

        secureChannel.setChannelSecurity(new ChannelSecurity(newKeys, newToken, oldKeys, oldToken));

        DateTime createdAt = response.getSecurityToken().getCreatedAt();
        long revisedLifetime = response.getSecurityToken().getRevisedLifetime().longValue();

        if (revisedLifetime > 0) {
            long renewAt = (long) (revisedLifetime * 0.75);
            renewFuture = ctx.executor().schedule(
                    () -> sendOpenSecureChannelRequest(ctx, SecurityTokenRequestType.Renew),
                    renewAt, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("Server revised secure channel lifetime to 0; renewal will not occur.");
        }

        ctx.executor().execute(() -> {
            // SecureChannel is ready; remove the acknowledge handler.
            if (ctx.pipeline().get(UaTcpClientAcknowledgeHandler.class) != null) {
                ctx.pipeline().remove(UaTcpClientAcknowledgeHandler.class);
            }
        });

        ChannelSecurity channelSecurity = secureChannel.getChannelSecurity();

        long currentTokenId = channelSecurity.getCurrentToken().getTokenId().longValue();

        long previousTokenId = channelSecurity.getPreviousToken()
                .map(t -> t.getTokenId().longValue()).orElse(-1L);

        logger.debug(
                "SecureChannel id={}, currentTokenId={}, previousTokenId={}, lifetime={}ms, createdAt={}",
                secureChannel.getChannelId(), currentTokenId, previousTokenId, revisedLifetime, createdAt);
    }

    private void onSecureMessage(ChannelHandlerContext ctx, ByteBuf buffer) throws UaException {
        buffer.skipBytes(3 + 1 + 4); // skip messageType, chunkType, messageSize

        long secureChannelId = buffer.readUnsignedInt();
        if (secureChannelId != secureChannel.getChannelId()) {
            throw new UaException(StatusCodes.Bad_SecureChannelIdInvalid,
                    "invalid secure channel id: " + secureChannelId);
        }

        if (accumulateChunk(buffer)) {
            final List<ByteBuf> buffersToDecode = ImmutableList.copyOf(chunkBuffers);
            chunkBuffers = new LinkedList<>();

            serializationQueue.decode((binaryDecoder, chunkDecoder) -> {
                ByteBuf decodedBuffer = null;

                try {
                    decodedBuffer = chunkDecoder.decodeSymmetric(secureChannel, buffersToDecode);

                    binaryDecoder.setBuffer(decodedBuffer);
                    UaResponseMessage response = binaryDecoder.decodeMessage(null);

                    UaRequestFuture request = pending.remove(chunkDecoder.getLastRequestId());

                    if (request != null) {
                        client.getExecutorService().execute(
                                () -> request.getFuture().complete(response));
                    } else {
                        logger.warn("No UaRequestFuture for requestId={}", chunkDecoder.getLastRequestId());
                    }
                } catch (MessageAbortedException e) {
                    logger.debug("Received message abort chunk; error={}, reason={}", e.getStatusCode(), e.getMessage());

                    UaRequestFuture request = pending.remove(chunkDecoder.getLastRequestId());

                    if (request != null) {
                        client.getExecutorService().execute(
                                () -> request.getFuture().completeExceptionally(e));
                    } else {
                        logger.warn("No UaRequestFuture for requestId={}", chunkDecoder.getLastRequestId());
                    }
                } catch (Throwable t) {
                    logger.error("Error decoding symmetric message: {}", t.getMessage(), t);
                    serializationQueue.pause();
                    ctx.close();
                } finally {
                    if (decodedBuffer != null) {
                        decodedBuffer.release();
                    }
                }
            });
        }
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
