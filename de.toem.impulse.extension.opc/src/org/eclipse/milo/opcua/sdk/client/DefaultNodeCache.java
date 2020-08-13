/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.Maps;
import org.eclipse.milo.opcua.sdk.client.api.NodeCache;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class DefaultNodeCache implements NodeCache {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile long expireAfterNanos = Duration.ofMinutes(2).toNanos();
    private volatile long maximumSize = 1024;

    private volatile Cache<NodeId, Map<AttributeId, DataValue>> cache = buildCache();

    @Override
    public Optional<DataValue> getAttribute(NodeId nodeId, AttributeId attributeId) {
        Map<AttributeId, DataValue> attributes = cache.getIfPresent(nodeId);

        try {
            return attributes == null ?
                Optional.empty() :
                Optional.ofNullable(attributes.get(attributeId));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @Override
    public void putAttribute(NodeId nodeId, AttributeId attributeId, DataValue attribute) {
        try {
            Map<AttributeId, DataValue> attributes = cache.get(nodeId,
                () -> Collections.synchronizedMap(Maps.newEnumMap(AttributeId.class)));

            attributes.put(attributeId, attribute);
        } catch (ExecutionException e) {
            logger.error("Error loading value: {}", e.getMessage(), e);
        }
    }

    @Override
    public void invalidate(NodeId nodeId) {
        cache.invalidate(nodeId);
    }

    @Override
    public void invalidate(NodeId nodeId, AttributeId attributeId) {
        Optional.ofNullable(cache.getIfPresent(nodeId))
            .ifPresent(attributes -> attributes.remove(attributeId));
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    public CacheStats getStats() {
        return cache.stats();
    }

    public synchronized void setExpireAfter(long duration, TimeUnit unit) {
        this.expireAfterNanos = unit.toNanos(duration);

        Cache<NodeId, Map<AttributeId, DataValue>> newCache = buildCache();

        newCache.putAll(cache.asMap());

        cache = newCache;
    }

    public synchronized void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;

        Cache<NodeId, Map<AttributeId, DataValue>> newCache = buildCache();

        newCache.putAll(cache.asMap());

        cache = newCache;
    }

    private Cache<NodeId, Map<AttributeId, DataValue>> buildCache() {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(expireAfterNanos, TimeUnit.NANOSECONDS)
            .maximumSize(maximumSize)
            .recordStats()
            .build();
    }

}
