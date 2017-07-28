package de.toem.impulse.extension.opc.ua;

import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

import de.toem.pattern.element.Cell;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = OpcUaNode.TYPE, dynamicChildOf = { OpcUaAdapter.TYPE }, properties = "imageExtension")
public class OpcUaNode extends Cell {
	public static final String TYPE = "opc.ua.node";

	public boolean enabled;

	public String nodeId;
	public String nodeClass;
	public String description;
	
	public int sampleRate;
	public int queueSize;

	public String additionalAttributes;
	
	
	public String imageExtension() {
		if (isObject())
			return ".object";
		else if (isVariable())
			return ".variable";
		return null;
	}
	public boolean isObject(){
		return NodeClass.Object.toString().equals(nodeClass);
	}
	public boolean isVariable(){
		return NodeClass.Variable.toString().equals(nodeClass);
	}
}
