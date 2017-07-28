package com.digitalpetri.opcua.sdk.examples.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscription;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoringParameters;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.google.common.collect.Lists.newArrayList;

public class SubscriptionExample implements ClientExample {

    public static void main(String[] args) throws Exception {
//        String endpointUrl = "opc.tcp://localhost:12685";
        String endpointUrl = "opc.tcp://opcua.demo-this.com:51210/UA/SampleServer";

        SubscriptionExample example = new SubscriptionExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicLong clientHandles = new AtomicLong(1L);

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        // create a subscription and a monitored item
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        ReadValueId readValueId = new ReadValueId(
                Identifiers.Server_ServerStatus_CurrentTime,
                AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

        // client handle must be unique per item
        UInteger clientHandle = uint(clientHandles.getAndIncrement());

        MonitoringParameters parameters = new MonitoringParameters(
                clientHandle,
                1000.0,     // sampling interval
                null,       // filter, null means use default
                uint(10),   // queue size
                true);      // discard oldest

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                readValueId, MonitoringMode.Reporting, parameters);

        List<UaMonitoredItem> items = subscription
                .createMonitoredItems(TimestampsToReturn.Both, newArrayList(request)).get();

        // do something with the value updates
        UaMonitoredItem item = items.get(0);
        item.setValueConsumer(v -> {
            logger.info("value received: {}", v.getValue());

            future.complete(client);
        });
    }

}
