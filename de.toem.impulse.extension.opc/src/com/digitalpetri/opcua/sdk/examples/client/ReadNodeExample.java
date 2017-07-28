package com.digitalpetri.opcua.sdk.examples.client;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.BrowseDirection;
import com.digitalpetri.opcua.stack.core.types.structured.BrowseDescription;
import com.digitalpetri.opcua.stack.core.types.structured.BrowseResult;

import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class ReadNodeExample implements ClientExample {

    public static void main(String[] args) throws Exception {
//        String endpointUrl = "opc.tcp://localhost:12685";
        String endpointUrl = "opc.tcp://opcua.demo-this.com:51210/UA/SampleServer";

        ReadNodeExample example = new ReadNodeExample();

        new ClientExampleRunner(endpointUrl, example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        NodeId root = Identifiers.RootFolder;
        BrowseDescription bd = new BrowseDescription(Identifiers.RootFolder, BrowseDirection.Forward, Identifiers.HierarchicalReferences, true, UInteger.valueOf(0),UInteger.valueOf(-1));
        
        CompletableFuture<BrowseResult> result = client.browse(bd);
        
        // read the value of the current time node
        UaVariableNode currentTimeNode = client.getAddressSpace()
                .getVariableNode(Identifiers.Server_ServerStatus_CurrentTime);

        DataValue value = currentTimeNode.readValue().get();

        logger.info("currentTime value={}", value);

        future.complete(client);
    }
    
    

}
