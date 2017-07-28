package com.digitalpetri.opcua.sdk.examples.client;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.examples.util.KeyStoreLoader;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ClientExampleRunner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    private final KeyStoreLoader loader = new KeyStoreLoader();

    private final String endpointUrl;
    private final ClientExample clientExample;

    public ClientExampleRunner(String endpointUrl,
                               ClientExample clientExample) {

        this.endpointUrl = endpointUrl;
        this.clientExample = clientExample;


    }

    private OpcUaClient createClient() throws Exception {
        SecurityPolicy securityPolicy = clientExample.getSecurityPolicy();

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(endpointUrl).get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        logger.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), securityPolicy);

        loader.load();

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
                .setApplicationUri("urn:digitalpetri:opcua:client")
                .setCertificate(loader.getClientCertificate())
                .setKeyPair(loader.getClientKeyPair())
                .setEndpoint(endpoint)
                .setIdentityProvider(clientExample.getIdentityProvider())
                .setRequestTimeout(uint(5000))
                .build();

        return new OpcUaClient(config);
    }

    public void run() {
        future.whenComplete((client, ex) -> {
            if (client != null) {
                try {
                    client.disconnect().get();
                    Stack.releaseSharedResources();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error disconnecting:", e.getMessage(), e);
                }
            } else {
                logger.error("Error running example: {}", ex.getMessage(), ex);
                Stack.releaseSharedResources();
            }

            try {
                Thread.sleep(1000);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        try {
            OpcUaClient client = createClient();

            try {
                clientExample.run(client, future);
                future.get(5, TimeUnit.SECONDS);
            } catch (Throwable t) {
                logger.error("Error running client example: {}", t.getMessage(), t);
                future.complete(client);
            }
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        try {
            Thread.sleep(999999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
