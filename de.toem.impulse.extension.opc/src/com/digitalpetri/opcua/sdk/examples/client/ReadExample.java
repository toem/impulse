package com.digitalpetri.opcua.sdk.examples.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.enumerated.ServerState;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;

public class ReadExample implements ClientExample {

    public static void main(String[] args) throws Exception {
    	
        String endpointUrl = "opc.tcp://opcua.demo-this.com:51210/UA/SampleServer";
//        String endpointUrl = "opc.tcp://10.50.100.129:48401";
//        String endpointUrl = "opc.tcp://uademo.prosysopc.com:53530/OPCUA/SimulationServer";

        ReadExample example = new ReadExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        // asynchronous read request
        readServerStateAndTime(client).thenAccept(values -> {
            DataValue v0 = values.get(0);
            DataValue v1 = values.get(1);

            logger.info("State={}", ServerState.from((Integer) v0.getValue().getValue()));
            logger.info("CurrentTime={}", v1.getValue().getValue());

            future.complete(client);
        });
    }

    private CompletableFuture<List<DataValue>> readServerStateAndTime(OpcUaClient client) {
        List<NodeId> nodeIds = newArrayList(
                Identifiers.Server_ServerStatus_State,
                Identifiers.Server_ServerStatus_CurrentTime);

        return client.readValues(0.0, TimestampsToReturn.Both, nodeIds);
    }

}
