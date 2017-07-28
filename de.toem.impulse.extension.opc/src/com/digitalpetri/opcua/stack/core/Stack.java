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

package com.digitalpetri.opcua.stack.core;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.digitalpetri.opcua.stack.core.util.ManifestUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import de.toem.impulse.extension.opc.LoggerFactory;

public final class Stack {

    public static final String VERSION =
            ManifestUtil.read("X-Stack-Version").orElse("dev");

    public static final String UA_TCP_BINARY_TRANSPORT_URI =
            "http://opcfoundation.org/UA-Profile/Transport/uatcp-uasc-uabinary";

    public static final int DEFAULT_PORT = 12685;


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
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ua-netty-event-loop-" + threadNumber.getAndIncrement());
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
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ua-shared-pool-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
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
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ua-shared-scheduled-executor-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            };

            SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(threadFactory);
        }

        return SCHEDULED_EXECUTOR_SERVICE;
    }

    /**
     * @return a shared {@link HashedWheelTimer}.
     */
    public static synchronized HashedWheelTimer sharedWheelTimer() {
        if (WHEEL_TIMER == null) {
            ThreadFactory threadFactory = r -> {
                Thread thread = new Thread(r, "ua-netty-wheel-timer");
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
            SCHEDULED_EXECUTOR_SERVICE = null;
        }

        if (EXECUTOR_SERVICE != null) {
            EXECUTOR_SERVICE.shutdown();
            EXECUTOR_SERVICE = null;
        }

        if (WHEEL_TIMER != null) {
            WHEEL_TIMER.stop().forEach(Timeout::cancel);
            WHEEL_TIMER = null;
        }
    }

}
