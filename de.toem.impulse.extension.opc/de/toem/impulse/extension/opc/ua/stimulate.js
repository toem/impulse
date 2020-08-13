// client      : OPC/UA client (org.eclipse.milo.opcua.sdk.client.OpcUaClient)
// progress    : Progress control (de.toem.impulse.cells.ports.IPortProgress)
// console     : Console output (de.toem.impulse.scripting.IScriptConsole)

// imports
var NodeId = Java.type("org.eclipse.milo.opcua.stack.core.types.builtin.NodeId");
var TimestampsToReturn = Java.type("org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn");
var DataValue = Java.type("org.eclipse.milo.opcua.stack.core.types.builtin.DataValue");
var Variant = Java.type("org.eclipse.milo.opcua.stack.core.types.builtin.Variant");
var Thread = Java.type("java.lang.Thread");

var count = 0;
while (!progress.isCanceled()) {

    if (progress.isStreaming()) {
    
        // define a node
        var nodeId /*:NodeId:*/ = NodeId.parse("ns=2;i=10223");

        // read the node
        var value = client.readValue(0, TimestampsToReturn.Both, nodeId).get();
        console.println(value);

        // write the node
        client.writeValue(nodeId, DataValue.valueOnly(new Variant(true))).get();
        
        // stop streaming after 10 cycles
        count++;
        if (count > 10)
        	progress.cancel();
    }
    // wait some time
    Thread.sleep(500);
}
