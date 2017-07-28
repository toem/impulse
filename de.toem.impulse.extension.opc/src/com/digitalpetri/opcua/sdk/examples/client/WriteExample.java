package com.digitalpetri.opcua.sdk.examples.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;

public class WriteExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        String endpointUrl = "opc.tcp://localhost:12685";

        WriteExample example = new WriteExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        List<NodeId> nodeIds = newArrayList(
                new NodeId(2, "/Static/AllProfiles/Scalar/Int32"));

        for (int i = 0; i < 10; i++) {
            Variant v = new Variant(i);

            // don't write status or timestamps
            DataValue dv = new DataValue(v, null, null);

            // write asynchronously....
            CompletableFuture<List<StatusCode>> f =
                    client.writeValues(nodeIds, newArrayList(dv));

            // ...but block for the results so we write in order
            List<StatusCode> statusCodes = f.get();
            StatusCode status = statusCodes.get(0);

            if (status.isGood()) {
                logger.info("Wrote '{}' to nodeId={}", v, nodeIds.get(0));
            }
        }

        future.complete(client);
    }

}
