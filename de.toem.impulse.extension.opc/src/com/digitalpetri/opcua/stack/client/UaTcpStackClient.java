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

package com.digitalpetri.opcua.stack.client;

import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.digitalpetri.opcua.stack.client.config.UaTcpStackClientConfig;
import com.digitalpetri.opcua.stack.client.handlers.UaRequestFuture;
import com.digitalpetri.opcua.stack.client.handlers.UaTcpClientAcknowledgeHandler;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.UaServiceFaultException;
import com.digitalpetri.opcua.stack.core.application.UaStackClient;
import com.digitalpetri.opcua.stack.core.channel.ChannelConfig;
import com.digitalpetri.opcua.stack.core.channel.ClientSecureChannel;
import com.digitalpetri.opcua.stack.core.serialization.UaRequestMessage;
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.ApplicationType;
import com.digitalpetri.opcua.stack.core.types.structured.ApplicationDescription;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.FindServersRequest;
import com.digitalpetri.opcua.stack.core.types.structured.FindServersResponse;
import com.digitalpetri.opcua.stack.core.types.structured.GetEndpointsRequest;
import com.digitalpetri.opcua.stack.core.types.structured.GetEndpointsResponse;
import com.digitalpetri.opcua.stack.core.types.structured.RequestHeader;
import com.digitalpetri.opcua.stack.core.types.structured.ResponseHeader;
import com.digitalpetri.opcua.stack.core.types.structured.ServiceFault;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class UaTcpStackClient implements UaStackClient {

    private static final long DEFAULT_TIMEOUT_MS = 60000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<UInteger, CompletableFuture<UaResponseMessage>> pending = Maps.newConcurrentMap();
    private final Map<UInteger, Timeout> timeouts = Maps.newConcurrentMap();

    private final HashedWheelTimer wheelTimer;

    private final ApplicationDescription application;

    private final ClientChannelManager channelManager;

    private final UaTcpStackClientConfig config;

    public UaTcpStackClient(UaTcpStackClientConfig config) {
        this.config = config;

        wheelTimer = config.getWheelTimer();

        application = new ApplicationDescription(
                config.getApplicationUri(),
                config.getProductUri(),
                config.getApplicationName(),
                ApplicationType.Client,
                null, null, null);

        channelManager = new ClientChannelManager(this);
    }

    public UaTcpStackClientConfig getConfig() {
        return config;
    }

    @Override
    public CompletableFuture<UaStackClient> connect() {
        CompletableFuture<UaStackClient> future = new CompletableFuture<>();

        channelManager.getChannel().whenComplete((ch, ex) -> {
            if (ch != null) future.complete(this);
            else future.completeExceptionally(ex);
        });

        return future;
    }

    @Override
    public CompletableFuture<UaStackClient> disconnect() {
        return channelManager.disconnect()
            .thenApply(v -> UaTcpStackClient.this);
    }

    public <T extends UaResponseMessage> CompletableFuture<T> sendRequest(UaRequestMessage request) {
        return channelManager.getChannel()
                .thenCompose(sc -> sendRequest(request, sc));
    }

    @SuppressWarnings("unchecked")
    private <T extends UaResponseMessage> CompletionStage<T> sendRequest(UaRequestMessage request, ClientSecureChannel sc) {
        Channel channel = sc.getChannel();

        CompletableFuture<T> future = new CompletableFuture<>();
        UaRequestFuture requestFuture = new UaRequestFuture(request);

        RequestHeader requestHeader = request.getRequestHeader();

        pending.put(requestHeader.getRequestHandle(), (CompletableFuture<UaResponseMessage>) future);

        scheduleRequestTimeout(requestHeader);

        requestFuture.getFuture().whenComplete((r, x) -> {
            if (r != null) {
                receiveResponse(r);
            } else {
                UInteger requestHandle = request.getRequestHeader().getRequestHandle();

                pending.remove(requestHandle);
                future.completeExceptionally(x);
            }
        });

        channel.writeAndFlush(requestFuture).addListener(f -> {
            if (!f.isSuccess()) {
                Throwable cause = f.cause();

                if (cause instanceof ClosedChannelException) {
                    logger.debug("Channel closed; retrying...");

                    sendRequest(request).whenComplete((r, ex) -> {
                        if (r != null) {
                            T t = (T) r;
                            future.complete(t);
                        } else {
                            future.completeExceptionally(ex);
                        }
                    });
                } else {
                    UInteger requestHandle = request.getRequestHeader().getRequestHandle();

                    pending.remove(requestHandle);
                    future.completeExceptionally(f.cause());

                    logger.debug("Write failed, requestHandle={}", requestHandle, cause);
                }
            }
        });

        return future;
    }

    public void sendRequests(List<? extends UaRequestMessage> requests,
                             List<CompletableFuture<? extends UaResponseMessage>> futures) {

        Preconditions.checkArgument(requests.size() == futures.size(),
                "requests and futures parameters must be same size");

        channelManager.getChannel().whenComplete((sc, ex) -> {
            if (sc != null) {
                sendRequests(requests, futures, sc);
            } else {
                futures.forEach(f -> f.completeExceptionally(ex));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void sendRequests(List<? extends UaRequestMessage> requests, List<CompletableFuture<? extends UaResponseMessage>> futures, ClientSecureChannel sc) {
        Channel channel = sc.getChannel();
        Iterator<? extends UaRequestMessage> requestIterator = requests.iterator();
        Iterator<CompletableFuture<? extends UaResponseMessage>> futureIterator = futures.iterator();

        List<UaRequestFuture> pendingRequests = new ArrayList<>(requests.size());

        while (requestIterator.hasNext() && futureIterator.hasNext()) {
            UaRequestMessage request = requestIterator.next();
            CompletableFuture<UaResponseMessage> future =
                    (CompletableFuture<UaResponseMessage>) futureIterator.next();

            UaRequestFuture pendingRequest = new UaRequestFuture(request, future);
            pendingRequests.add(pendingRequest);

            RequestHeader requestHeader = request.getRequestHeader();

            pending.put(requestHeader.getRequestHandle(), future);

            scheduleRequestTimeout(requestHeader);

            pendingRequest.getFuture().thenAccept(this::receiveResponse);
        }

        channel.eventLoop().execute(() -> {
            for (UaRequestFuture pendingRequest : pendingRequests) {
                channel.write(pendingRequest).addListener(f -> {
                    if (!f.isSuccess()) {
                        UInteger requestHandle = pendingRequest
                                .getRequest().getRequestHeader().getRequestHandle();

                        CompletableFuture<?> future = pending.remove(requestHandle);
                        if (future != null) future.completeExceptionally(f.cause());

                        logger.debug("Write failed, requestHandle={}", requestHandle, f.cause());
                    }
                });
            }

            channel.flush();
        });
    }

    public CompletableFuture<ClientSecureChannel> getChannelFuture() {
        return channelManager.getChannel();
    }

    private void scheduleRequestTimeout(RequestHeader requestHeader) {
        UInteger requestHandle = requestHeader.getRequestHandle();

        long timeoutHint = requestHeader.getTimeoutHint() != null ?
                requestHeader.getTimeoutHint().longValue() : DEFAULT_TIMEOUT_MS;

        Timeout timeout = wheelTimer.newTimeout(t -> {
            timeouts.remove(requestHandle);
            if (!t.isCancelled()) {
                CompletableFuture<UaResponseMessage> f = pending.remove(requestHandle);
                if (f != null) {
                    String message = "request timed out after " + timeoutHint + "ms";
                    f.completeExceptionally(new UaException(StatusCodes.Bad_Timeout, message));
                }
            }
        }, timeoutHint, TimeUnit.MILLISECONDS);

        timeouts.put(requestHandle, timeout);
    }

    private void receiveResponse(UaResponseMessage response) {
        ResponseHeader header = response.getResponseHeader();
        UInteger requestHandle = header.getRequestHandle();

        CompletableFuture<UaResponseMessage> future = pending.remove(requestHandle);

        if (future != null) {
            if (header.getServiceResult().isGood()) {
                future.complete(response);
            } else {
                ServiceFault serviceFault;

                if (response instanceof ServiceFault) {
                    serviceFault = (ServiceFault) response;
                } else {
                    serviceFault = new ServiceFault(header);
                }

                future.completeExceptionally(new UaServiceFaultException(serviceFault));
            }

            Timeout timeout = timeouts.remove(requestHandle);
            if (timeout != null) timeout.cancel();
        } else {
            logger.warn("Received {} for unknown requestHandle: {}",
                    response.getClass().getSimpleName(), requestHandle);
        }
    }

    @Override
    public Optional<X509Certificate> getCertificate() {
        return config.getCertificate();
    }

    @Override
    public Optional<KeyPair> getKeyPair() {
        return config.getKeyPair();
    }

    @Override
    public ChannelConfig getChannelConfig() {
        return config.getChannelConfig();
    }

    @Override
    public UInteger getChannelLifetime() {
        return config.getChannelLifetime();
    }

    @Override
    public ApplicationDescription getApplication() {
        return application;
    }

    @Override
    public Optional<EndpointDescription> getEndpoint() {
        return config.getEndpoint();
    }

    @Override
    public String getEndpointUrl() {
        return config.getEndpoint()
                .map(EndpointDescription::getEndpointUrl)
                .orElse(config.getEndpointUrl().orElse(""));
    }

    @Override
    public ExecutorService getExecutorService() {
        return config.getExecutor();
    }

    public static CompletableFuture<ClientSecureChannel> bootstrap(
            UaTcpStackClient client,
            Optional<ClientSecureChannel> existingChannel) {

        CompletableFuture<ClientSecureChannel> handshake = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(client.getConfig().getEventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        UaTcpClientAcknowledgeHandler acknowledgeHandler =
                                new UaTcpClientAcknowledgeHandler(client, existingChannel, handshake);

                        channel.pipeline().addLast(acknowledgeHandler);
                    }
                });

        try {
            URI uri = new URI(client.getEndpointUrl()).parseServerAuthority();

            bootstrap.connect(uri.getHost(), uri.getPort()).addListener((ChannelFuture f) -> {
                if (!f.isSuccess()) {
                    handshake.completeExceptionally(f.cause());
                }
            });
        } catch (Throwable e) {
            UaException failure = new UaException(
                    StatusCodes.Bad_TcpEndpointUrlInvalid, e);

            handshake.completeExceptionally(failure);
        }

        return handshake;
    }

    /**
     * Query the FindServers service at the given endpoint URL.
     * <p>
     * The endpoint URL(s) for each server {@link ApplicationDescription} returned can then be used in a
     * {@link #getEndpoints(String)} call to discover the endpoints for that server.
     *
     * @param endpointUrl the endpoint URL to find servers at.
     * @return the {@link ApplicationDescription}s returned by the FindServers service.
     */
    public static CompletableFuture<ApplicationDescription[]> findServers(String endpointUrl) {
        UaTcpStackClientConfig config = UaTcpStackClientConfig.builder()
                .setEndpointUrl(endpointUrl)
                .build();

        UaTcpStackClient client = new UaTcpStackClient(config);

        FindServersRequest request = new FindServersRequest(
                new RequestHeader(null, DateTime.now(), uint(1), uint(0), null, uint(5000), null),
                endpointUrl, null, null);

        return client.<FindServersResponse>sendRequest(request)
                .whenComplete((r, ex) -> client.disconnect())
                .thenApply(FindServersResponse::getServers);
    }

    /**
     * Query the GetEndpoints service at the given endpoint URL.
     *
     * @param endpointUrl the endpoint URL to get endpoints from.
     * @return the {@link EndpointDescription}s returned by the GetEndpoints service.
     */
    public static CompletableFuture<EndpointDescription[]> getEndpoints(String endpointUrl) {
        UaTcpStackClientConfig config = UaTcpStackClientConfig.builder()
                .setEndpointUrl(endpointUrl)
                .build();

        UaTcpStackClient client = new UaTcpStackClient(config);

        GetEndpointsRequest request = new GetEndpointsRequest(
                new RequestHeader(null, DateTime.now(), uint(1), uint(0), null, uint(5000), null),
                endpointUrl, null, new String[]{Stack.UA_TCP_BINARY_TRANSPORT_URI});

        return client.<GetEndpointsResponse>sendRequest(request)
                .whenComplete((r, ex) -> client.disconnect())
                .thenApply(GetEndpointsResponse::getEndpoints);
    }

}
