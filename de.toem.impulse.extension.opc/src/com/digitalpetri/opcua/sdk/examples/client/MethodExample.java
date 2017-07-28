package com.digitalpetri.opcua.sdk.examples.client;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.structured.CallMethodRequest;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class MethodExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        String endpointUrl = "opc.tcp://localhost:12685";

        MethodExample example = new MethodExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        // call the sqrt(x) function provided by CttNamespace
        sqrt(client, 16.0).exceptionally(ex -> {
            logger.error("error invoking sqrt()", ex);
            return -1.0;
        }).thenAccept(v -> {
            logger.info("sqrt(16)={}", v);

            future.complete(client);
        });
    }

    private CompletableFuture<Double> sqrt(OpcUaClient client, Double input) {
        NodeId objectId = NodeId.parse("ns=2;s=/Methods");
        NodeId methodId = NodeId.parse("ns=2;s=/Methods/sqrt(x)");

        CallMethodRequest request = new CallMethodRequest(
                objectId, methodId, new Variant[]{new Variant(input)});

        return client.call(request).thenCompose(result -> {
            StatusCode statusCode = result.getStatusCode();

            if (statusCode.isGood()) {
                Double value = (Double) result.getOutputArguments()[0].getValue();
                return CompletableFuture.completedFuture(value);
            } else {
                CompletableFuture<Double> f = new CompletableFuture<>();
                f.completeExceptionally(new UaException(statusCode));
                return f;
            }
        });
    }

}
