/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.eclipse.milo.opcua.stack.core.util.ManifestUtil;
import de.toem.impulse.extension.opc.LoggerFactory;

public final class Stack {

    public static final String VERSION =
        ManifestUtil.read("X-Stack-Version").orElse("dev");

    public static final String TCP_UASC_UABINARY_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/uatcp-uasc-uabinary";

    public static final String HTTPS_UABINARY_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/https-uabinary";

    public static final String HTTPS_UAXML_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/https-uasoapxml";

    public static final String HTTPS_UAJSON_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/https-uajson";

    public static final String WSS_UASC_UABINARY_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/wss-uasc-uabinary";

    public static final String WSS_UAJSON_TRANSPORT_URI =
        "http://opcfoundation.org/UA-Profile/Transport/wss-uajson";

    public static final String WSS_PROTOCOL_BINARY = "opcua+uacp";

    public static final String WSS_PROTOCOL_JSON = "opcua+uajson";

    public static final int DEFAULT_TCP_PORT = 12685;
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_HTTPS_PORT = 8443;

    private static NioEventLoopGroup EVENT_LOOP;
    private static ExecutorService EXECUTOR_SERVICE;
    private static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;
    private static HashedWheelTimer WHEEL_TIMER;
    private static ClassLoader CUSTOM_CLASS_LOADER;

    /**
     * @return a shared {@link NioEventLoopGroup}.
     */
    public static synchronized NioEventLoopGroup sharedEventLoop() {
        if (EVENT_LOOP == null) {
            ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicLong threadNumber = new AtomicLong(0L);

                @Override
                public Thread newThread(@Nonnull Runnable r) {
                    Thread thread = new Thread(r, "milo-netty-event-loop-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            };

            EVENT_LOOP = new NioEventLoopGroup(0, threadFactory);
        }

        return EVENT_LOOP;
    }

    /**
     * @return a shared {@link ExecutorService}.
     */
    public static synchronized ExecutorService sharedExecutor() {
        if (EXECUTOR_SERVICE == null) {
            ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicLong threadNumber = new AtomicLong(0L);

                @Override
                public Thread newThread(@Nonnull Runnable r) {
                    Thread thread = new Thread(r, "milo-shared-thread-pool-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(
                        (t, e) ->
                            LoggerFactory.getLogger(Stack.class)
                                .warn("Uncaught Exception on shared stack ExecutorService thread!", e)
                    );
                    return thread;
                }
            };

            EXECUTOR_SERVICE = Executors.newCachedThreadPool(threadFactory);
        }

        return EXECUTOR_SERVICE;
    }

    /**
     * @return a shared {@link ScheduledExecutorService}.
     */
    public static synchronized ScheduledExecutorService sharedScheduledExecutor() {
        if (SCHEDULED_EXECUTOR_SERVICE == null) {
            ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicLong threadNumber = new AtomicLong(0L);

                @Override
                public Thread newThread(@Nonnull Runnable r) {
                    Thread thread = new Thread(r, "milo-shared-scheduled-executor-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(
                        (t, e) ->
                            LoggerFactory.getLogger(Stack.class)
                                .warn("Uncaught Exception on shared stack ScheduledExecutorService thread!", e)
                    );
                    return thread;
                }
            };

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                threadFactory
            );

            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

            SCHEDULED_EXECUTOR_SERVICE = executor;
        }

        return SCHEDULED_EXECUTOR_SERVICE;
    }

    /**
     * @return a shared {@link HashedWheelTimer}.
     */
    public static synchronized HashedWheelTimer sharedWheelTimer() {
        if (WHEEL_TIMER == null) {
            ThreadFactory threadFactory = r -> {
                Thread thread = new Thread(r, "milo-netty-wheel-timer");
                thread.setDaemon(true);
                return thread;
            };

            WHEEL_TIMER = new HashedWheelTimer(threadFactory);
        }

        return WHEEL_TIMER;
    }

    /**
     * @return if configured, the {@link ClassLoader} to be used when reflectively loading classes.
     */
    public static synchronized Optional<ClassLoader> getCustomClassLoader() {
        return Optional.ofNullable(CUSTOM_CLASS_LOADER);
    }

    /**
     * Set the {@link ClassLoader} that will be used when reflectively loading classes.
     *
     * @param customClassLoader the {@link ClassLoader} that will used when reflectively loading classes.
     */
    public static synchronized void setCustomClassLoader(ClassLoader customClassLoader) {
        CUSTOM_CLASS_LOADER = customClassLoader;
    }

    /**
     * Release shared resources, waiting at most 5 seconds for the {@link NioEventLoopGroup} to shutdown gracefully.
     */
    public static synchronized void releaseSharedResources() {
        releaseSharedResources(5, TimeUnit.SECONDS);
    }

    /**
     * Release shared resources, waiting at most the specified timeout for the {@link NioEventLoopGroup} to shutdown
     * gracefully.
     *
     * @param timeout the duration of the timeout.
     * @param unit    the unit of the timeout duration.
     */
    public static synchronized void releaseSharedResources(long timeout, TimeUnit unit) {
        if (EVENT_LOOP != null) {
            try {
                EVENT_LOOP.shutdownGracefully().await(timeout, unit);
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(Stack.class)
                    .warn("Interrupted awaiting event loop shutdown.", e);
            }
            EVENT_LOOP = null;
        }

        if (SCHEDULED_EXECUTOR_SERVICE != null) {
            SCHEDULED_EXECUTOR_SERVICE.shutdown();
        }

        if (EXECUTOR_SERVICE != null) {
            EXECUTOR_SERVICE.shutdown();
        }

        if (SCHEDULED_EXECUTOR_SERVICE != null) {
            try {
                SCHEDULED_EXECUTOR_SERVICE.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(Stack.class)
                    .warn("Interrupted awaiting scheduled executor service shutdown.", e);
            }
            SCHEDULED_EXECUTOR_SERVICE = null;
        }

        if (EXECUTOR_SERVICE != null) {
            try {
                EXECUTOR_SERVICE.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(Stack.class)
                    .warn("Interrupted awaiting executor service shutdown.", e);
            }
            EXECUTOR_SERVICE = null;
        }

        if (WHEEL_TIMER != null) {
            WHEEL_TIMER.stop().forEach(Timeout::cancel);
            WHEEL_TIMER = null;
        }
    }

    public static final class ConnectionLimits {

        private ConnectionLimits() {}

        /**
         * Deadline, in milliseconds, that UA Hello message must be received within before the channel is closed.
         */
        public static volatile int HELLO_DEADLINE_MS = 10_000;

        /**
         * Allows rate limiting to be disabled stack-wide.
         */
        public static boolean RATE_LIMIT_ENABLED = true;

        /**
         * Maximum number of connect attempts per {@link #RATE_LIMIT_WINDOW_MS}.
         */
        public static int RATE_LIMIT_MAX_ATTEMPTS = 4;

        /**
         * The window of time over which connect attempts will be counted for rate limiting.
         */
        public static int RATE_LIMIT_WINDOW_MS = 1000;

        /**
         * The maximum number of connections allowed in total (any remote address, not including localhost).
         */
        public static int RATE_LIMIT_MAX_CONNECTIONS = 10_000;

        /**
         * The maximum number of connections allowed from any 1 remote address.
         */
        public static int RATE_LIMIT_MAX_CONNECTIONS_PER_ADDRESS = 100;

    }

}
