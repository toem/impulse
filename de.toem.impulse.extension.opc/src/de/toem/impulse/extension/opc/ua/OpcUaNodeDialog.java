//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import java.util.List;
import java.util.Set;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TableController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.ControlProviderElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.extension.opc.ImpulseOpcExtension;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.general.scan.TextScanResult;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

public class OpcUaNodeDialog extends ControlProviderElementDialog {

	public OpcUaNodeDialog(ITlkEditor parent, IController controller) {
		super(parent, controller, getControls());
	}

	public OpcUaNodeDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
		super(parent, parentElement, newCell, getControls());
	}

	public OpcUaNodeDialog(ITlkEditor parent, IElement element) {
		super(parent, element, getControls());
	}

	public OpcUaNodeDialog(ITlkEditor parent, List<IElement> elements) {
		super(parent, elements, getControls());
	}

	public OpcUaNodeDialog(ITlkEditor parent) {
		super(parent, getControls());
	}

	public OpcUaNodeDialog() {
		super(getControls());
	}

	public static IControlProvider getControls() {
		IControlProvider provider = new AbstractControlProvider() {

			private IController classController;
			Object object;
			Object variable;

			@Override
			public String getHelpContext() {
				return ImpulseOpcExtension.PLUGIN_ID + "." + "opc_dialog";
			}

			@Override
			public boolean fillThis() {
				try {

					IController nodeIdController = tlk().addText(container(), new TextController(editor(), OpcUaNode.class.getField("nodeId")), cols(),
							DialogToolkit.LABEL | TLK.READ_ONLY, "Node Id:");
					classController = tlk().addText(container(), new TextController(editor(), OpcUaNode.class.getField("nodeClass")) {

						@Override
						protected void doUpdateExternal() {
							tlk().showControl(NodeClass.Object.toString().equals(this.getValueAsString()), object);
							tlk().showControl(NodeClass.Variable.toString().equals(this.getValueAsString()), variable);
						}

					}, cols(), DialogToolkit.LABEL | TLK.READ_ONLY, "Node class:");

					object = tlk().addGroup(container(), null, cols(), cols(), TLK.NULL, "Event Subscription", null);
					tlk().addText(object, new TextController(editor(), OpcUaNode.class.getField("additionalAttributes")).setNullText("e.g. EventId,LocalTime"),
							tlk().ld(cols() - 1, TLK.FILL, 300), DialogToolkit.LABEL | TLK.BORDER,
							"Additional event attributes:");

					variable = tlk().addGroup(container(), null, cols(), cols(), TLK.NULL, "Value Subscription", null);
					tlk().addText(variable, new TextController(editor(), OpcUaNode.class.getField("sampleRate")) {


						@Override
						protected TextScanResult doCheck(String formatted, int options) {
							if (Utils.isEmpty(formatted) || Utils.parseInt(formatted, -1) >= 0)
								return TextScanResult.SCAN_OK;
							return TextScanResult.SCAN_ERROR;
						}

						@Override
						protected Object convert(Object value) {
							if (Utils.equals(value, (Integer) 0))
								return "";
							return value;
						}
					}.setNullText("Default"), cols(), DialogToolkit.LABEL | TLK.BORDER, "Sample rate[ms]:");
					tlk().addText(variable, new TextController(editor(), OpcUaNode.class.getField("queueSize")) {


						@Override
						protected TextScanResult doCheck(String formatted, int options) {
							if (Utils.isEmpty(formatted) || Utils.parseInt(formatted, -1) >= 0)
								return TextScanResult.SCAN_OK;
							return TextScanResult.SCAN_ERROR;
						}

						@Override
						protected Object convert(Object value) {
							if (Utils.equals(value, (Integer) 0))
								return "";
							return value;
						}
					}.setNullText("Default"), cols(), DialogToolkit.LABEL | TLK.BORDER, "Queue size:");
					tlk().addButtonSet(variable, new RadioSetController(editor(), OpcUaNode.class.getField("trigger")), 4, cols(), TLK.RADIO | TLK.LABEL,
							"Trigger:", OpcUaNode.TRIGGER_OPTIONS, null);

					IController attributes = tlk().addTable(container(), new TableController(editor(), null),
							tlk().ld(cols() , TLK.GRAB, TLK.IGNORE_CONTROL, TLK.FILL, 150), TLK.ICON|TLK.BORDER, null, new String[] { "Attribute        ", "Value" });

					Actives.schedule(new IExecutable() {

						@Override
						public void execute(IProgress p) {
							if (attributes.hasCell()) {
							ICell base = attributes.getCell().getParent(OpcUaAdapter.class);
							String nodeId = nodeIdController.getValueAsString();
							if (base instanceof OpcUaAdapter) {
								final OpcUaAdapter root = (OpcUaAdapter) base.clone();
								OpcUaClient client = null;
								try {
									
									Set<AttributeId> attributeIds = AttributeId.BASE_NODE_ATTRIBUTES;
									if (NodeClass.Object.toString().equals(classController.getValueAsString()))
											attributeIds = AttributeId.OBJECT_NODE_ATTRIBUTES;
									else if (NodeClass.Variable.toString().equals(classController.getValueAsString()))
										attributeIds = AttributeId.VARIABLE_NODE_ATTRIBUTES;
									client = OpcUa.createAndConnect(root);
									String[][] value = OpcUa.readAttributes(client, root,nodeId,attributeIds);
									Actives.runInMain(new IExecutable(){

										@Override
										public void execute(IProgress p) {
											attributes.setValue(value);
											
											}
										});
								} catch (Throwable e) {
								} finally {
									if (client != null)
										try {
											OpcUa.disconnect(client);
										} catch (Exception e) {
										}
								}
							}

							}
						}
					},100);

				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				}
				return true;
			}
		}.insertBefore(new NameDescriptionEnableProvider("Enable Subscription"));
		provider.setCellClass(OpcUaNode.class);
		return provider;
	}
}
