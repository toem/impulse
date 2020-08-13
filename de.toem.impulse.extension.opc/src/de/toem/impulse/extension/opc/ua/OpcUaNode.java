package de.toem.impulse.extension.opc.ua;

import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

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
	public static final String[] TRIGGER_OPTIONS = { "Default", "Status", "Status/Value", "Status/Value/Timestamp" };
	public int trigger = OpcUaAdapter.TRIGGER_DEFAULT;
	
	public String additionalAttributes;
	
	
	public String imageExtension() {
		if (isObject())
			return ".object";
		else if (isVariable())
			return ".variable";
		else if (isMethod())
			return ".method";
		return null;
	}
	public boolean isObject(){
		return NodeClass.Object.toString().equals(nodeClass);
	}
	public boolean isVariable(){
		return NodeClass.Variable.toString().equals(nodeClass);
	}
	public boolean isMethod(){
		return NodeClass.Method.toString().equals(nodeClass);
	}
}
