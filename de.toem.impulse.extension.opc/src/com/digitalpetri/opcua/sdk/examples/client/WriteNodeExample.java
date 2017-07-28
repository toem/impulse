package com.digitalpetri.opcua.sdk.examples.client;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class WriteNodeExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        String endpointUrl = "opc.tcp://localhost:12685";

        WriteNodeExample example = new WriteNodeExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        NodeId nodeId = new NodeId(2, "/Static/AllProfiles/Scalar/Int32");

        UaVariableNode variableNode = client.getAddressSpace().getVariableNode(nodeId);

        // read the existing value
        Object valueBefore = variableNode.readValueAttribute().get();
        logger.info("valueBefore={}", valueBefore);

        // write a new random value (status and timestamps not included)
        DataValue newValue = new DataValue(new Variant(new Random().nextInt()), null, null);
        StatusCode writeStatus = variableNode.writeValue(newValue).get();
        logger.info("writeStatus={}", writeStatus);

        // read the value again
        Object valueAfter = variableNode.readValueAttribute().get();
        logger.info("valueAfter={}", valueAfter);

        future.complete(client);
    }

}
