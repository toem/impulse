package com.digitalpetri.opcua.sdk.examples.client;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.identity.AnonymousProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.CompositeProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.UsernameProvider;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class UsernamePasswordExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        String endpointUrl = "opc.tcp://localhost:12685";

        UsernamePasswordExample example = new UsernamePasswordExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public IdentityProvider getIdentityProvider() {
        /*
         * Authentication is handled by setting an appropriate
         * IdentityProvider when building the UaTcpStackClientConfig.
         */
        return new CompositeProvider(
                new UsernameProvider("user", "password"),
                new AnonymousProvider());
    }

    @Override
    public SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.None;
    }

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        // read the value of the current time node
        UaVariableNode currentTimeNode = client.getAddressSpace()
                .getVariableNode(Identifiers.Server_ServerStatus_CurrentTime);

        DataValue value = currentTimeNode.readValue().get();

        logger.info("currentTime value={}", value);

        future.complete(client);
    }

}
