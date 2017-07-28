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

package com.digitalpetri.opcua.sdk.client.subscriptions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscription;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.ModifyMonitoredItemsResponse;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateResult;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemModifyRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemModifyResult;
import com.digitalpetri.opcua.stack.core.types.structured.SetMonitoringModeResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.google.common.collect.Lists.newArrayList;

public class OpcUaSubscription implements UaSubscription {

    private final Map<UInteger, OpcUaMonitoredItem> itemsByClientHandle = Maps.newConcurrentMap();
    private final Map<UInteger, OpcUaMonitoredItem> itemsByServerHandle = Maps.newConcurrentMap();

    private volatile long lastSequenceNumber = 0L;
    private volatile double revisedPublishingInterval = 0.0;

    private volatile UInteger revisedLifetimeCount = uint(0);

    private volatile UInteger revisedMaxKeepAliveCount = uint(0);
    private volatile UInteger maxNotificationsPerPublish;
    private volatile boolean publishingEnabled;
    private volatile UByte priority;

    private final OpcUaClient client;
    private final UInteger subscriptionId;

    public OpcUaSubscription(OpcUaClient client, UInteger subscriptionId,
                      double revisedPublishingInterval,
                      UInteger revisedLifetimeCount,
                      UInteger revisedMaxKeepAliveCount,
                      UInteger maxNotificationsPerPublish,
                      boolean publishingEnabled,
                      UByte priority) {

        this.client = client;
        this.subscriptionId = subscriptionId;
        this.revisedPublishingInterval = revisedPublishingInterval;
        this.revisedLifetimeCount = revisedLifetimeCount;
        this.revisedMaxKeepAliveCount = revisedMaxKeepAliveCount;
        this.maxNotificationsPerPublish = maxNotificationsPerPublish;
        this.publishingEnabled = publishingEnabled;
        this.priority = priority;
    }

    @Override
    public CompletableFuture<List<UaMonitoredItem>> createMonitoredItems(TimestampsToReturn timestampsToReturn,
                                                                         List<MonitoredItemCreateRequest> itemsToCreate) {

        return client.createMonitoredItems(
                subscriptionId,
                timestampsToReturn,
                itemsToCreate).thenApply(response -> {

            List<UaMonitoredItem> createdItems = newArrayList();

            MonitoredItemCreateResult[] results = response.getResults();

            for (int i = 0; i < itemsToCreate.size(); i++) {
                MonitoredItemCreateRequest request = itemsToCreate.get(i);
                MonitoredItemCreateResult result = results[i];

                OpcUaMonitoredItem item = new OpcUaMonitoredItem(
                        request.getRequestedParameters().getClientHandle(),
                        request.getItemToMonitor(),
                        result.getMonitoredItemId(),
                        result.getStatusCode(),
                        result.getRevisedSamplingInterval(),
                        result.getRevisedQueueSize(),
                        result.getFilterResult(),
                        request.getMonitoringMode());

                if (item.getStatusCode().isGood()) {
                    itemsByClientHandle.put(item.getClientHandle(), item);
                    itemsByServerHandle.put(item.getMonitoredItemId(), item);
                }

                createdItems.add(item);
            }

            return createdItems;
        });
    }

    @Override
    public CompletableFuture<List<StatusCode>> modifyMonitoredItems(TimestampsToReturn timestampsToReturn,
                                                                    List<MonitoredItemModifyRequest> itemsToModify) {

        CompletableFuture<ModifyMonitoredItemsResponse> future =
                client.modifyMonitoredItems(subscriptionId, timestampsToReturn, itemsToModify);

        return future.thenApply(response -> {
            List<StatusCode> statusCodes = newArrayList();

            MonitoredItemModifyResult[] results = response.getResults();

            for (int i = 0; i < results.length; i++) {
                MonitoredItemModifyRequest request = itemsToModify.get(i);
                MonitoredItemModifyResult result = results[i];
                StatusCode statusCode = result.getStatusCode();

                OpcUaMonitoredItem item = itemsByServerHandle.get(request.getMonitoredItemId());

                if (item != null) {
                    item.setStatusCode(statusCode);
                    item.setRevisedSamplingInterval(result.getRevisedSamplingInterval());
                    item.setRevisedQueueSize(result.getRevisedQueueSize());
                    item.setFilterResult(result.getFilterResult());
                }

                statusCodes.add(statusCode);
            }

            return statusCodes;
        });
    }

    @Override
    public CompletableFuture<List<StatusCode>> deleteMonitoredItems(List<UaMonitoredItem> itemsToDelete) {

        List<UInteger> monitoredItemIds = itemsToDelete.stream()
                .map(UaMonitoredItem::getMonitoredItemId)
                .collect(Collectors.toList());

        return client.deleteMonitoredItems(subscriptionId, monitoredItemIds).thenApply(response -> {
            StatusCode[] results = response.getResults();

            for (UaMonitoredItem item : itemsToDelete) {
                itemsByClientHandle.remove(item.getClientHandle());
                itemsByServerHandle.remove(item.getMonitoredItemId());
            }

            return Arrays.asList(results);
        });
    }


    @Override
    public CompletableFuture<List<StatusCode>> setMonitoringMode(MonitoringMode monitoringMode,
                                                                 List<UaMonitoredItem> items) {

        List<UInteger> monitoredItemIds = items.stream()
                .map(UaMonitoredItem::getMonitoredItemId)
                .collect(Collectors.toList());

        CompletableFuture<SetMonitoringModeResponse> future =
                client.setMonitoringMode(subscriptionId, monitoringMode, monitoredItemIds);

        return future.thenApply(response -> {
            StatusCode[] results = response.getResults();

            for (int i = 0; i < monitoredItemIds.size(); i++) {
                UInteger id = monitoredItemIds.get(i);
                OpcUaMonitoredItem item = itemsByServerHandle.get(id);

                StatusCode result = results[i];
                if (result.isGood() && item != null) {
                    item.setMonitoringMode(monitoringMode);
                }
            }

            return Arrays.asList(results);
        });
    }

    @Override
    public CompletableFuture<StatusCode> setPublishingMode(boolean publishingEnabled) {
        return client.setPublishingMode(publishingEnabled, newArrayList(subscriptionId))
                .thenApply(response -> {
                    StatusCode statusCode = response.getResults()[0];

                    if (statusCode.isGood()) {
                        setPublishingEnabled(publishingEnabled);
                    }

                    return statusCode;
                });
    }

    @Override
    public UInteger getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public double getRevisedPublishingInterval() {
        return revisedPublishingInterval;
    }

    @Override
    public UInteger getRevisedLifetimeCount() {
        return revisedLifetimeCount;
    }

    @Override
    public UInteger getRevisedMaxKeepAliveCount() {
        return revisedMaxKeepAliveCount;
    }

    @Override
    public UInteger getMaxNotificationsPerPublish() {
        return maxNotificationsPerPublish;
    }

    @Override
    public boolean isPublishingEnabled() {
        return publishingEnabled;
    }

    @Override
    public UByte getPriority() {
        return priority;
    }

    @Override
    public ImmutableList<UaMonitoredItem> getMonitoredItems() {
        return ImmutableList.copyOf(itemsByClientHandle.values());
    }

    Map<UInteger, OpcUaMonitoredItem> getItemsByClientHandle() {
        return itemsByClientHandle;
    }

    Map<UInteger, OpcUaMonitoredItem> getItemsByServerHandle() {
        return itemsByServerHandle;
    }

    long getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    void setRevisedPublishingInterval(double revisedPublishingInterval) {
        this.revisedPublishingInterval = revisedPublishingInterval;
    }

    void setRevisedLifetimeCount(UInteger revisedLifetimeCount) {
        this.revisedLifetimeCount = revisedLifetimeCount;
    }

    void setRevisedMaxKeepAliveCount(UInteger revisedMaxKeepAliveCount) {
        this.revisedMaxKeepAliveCount = revisedMaxKeepAliveCount;
    }

    void setMaxNotificationsPerPublish(UInteger maxNotificationsPerPublish) {
        this.maxNotificationsPerPublish = maxNotificationsPerPublish;
    }

    void setPublishingEnabled(boolean publishingEnabled) {
        this.publishingEnabled = publishingEnabled;
    }

    void setPriority(UByte priority) {
        this.priority = priority;
    }

    void setLastSequenceNumber(long lastSequenceNumber) {
        this.lastSequenceNumber = lastSequenceNumber;
    }

}
