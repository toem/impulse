//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.general.scan.TextScanResult;

public class OpcUaNodeDialog extends ControlProviderDialog {

	public OpcUaNodeDialog(IEditorParent parent, IController controller) {
		super(parent, controller, getControls());
	}

	public OpcUaNodeDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
		super(parent, parentElement, newCell, getControls());
	}

	public OpcUaNodeDialog(IEditorParent parent, IElement element) {
		super(parent, element, getControls());
	}

	public OpcUaNodeDialog(IEditorParent parent, List<IElement> elements) {
		super(parent, elements, getControls());
	}

	public OpcUaNodeDialog(IEditorParent parent) {
		super(parent, getControls());
	}

	public OpcUaNodeDialog() {
		super(getControls());
	}

	public static IControlProvider getControls() {
		IControlProvider provider = new AbstractControlProvider() {

			private IController classController;
			Composite object;
			Composite variable;

			@Override
			public boolean fillThis() {
				try {

					tlk().addText(container(), new TextController(editor(), OpcUaNode.class.getField("nodeId")), cols(), DialogToolkit.LABEL | SWT.READ_ONLY,
							"Node Id:");
					classController = tlk().addText(container(), new TextController(editor(), OpcUaNode.class.getField("nodeClass")) {

						@Override
						protected void doUpdateExternal() {
							tlk().showControl(object, NodeClass.Object.toString().equals(this.getValueAsString()));
							tlk().showControl(variable, NodeClass.Variable.toString().equals(this.getValueAsString()));
						}

					}, cols(), DialogToolkit.LABEL | SWT.READ_ONLY, "Node class:");

					object = tlk().addGroup(container(), null, cols(), cols(), SWT.NULL, "Objects", null);
					tlk().addText(object, new TextController(editor(), OpcUaNode.class.getField("additionalAttributes")),
							tlk().ld(cols() - 1, SWT.FILL, 300, SWT.FILL, 75), DialogToolkit.LABEL | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP,
							"Additional event attributes:");

					variable = tlk().addGroup(container(), null, cols(), cols(), SWT.NULL, "Variable", null);
					tlk().addText(variable, new TextController(editor(), OpcUaNode.class.getField("sampleRate")) {

						@Override
						public void setControl(Control control, int function) {
							super.setControl(control, function);
							text().setMessage("Publish rate");
						}

						@Override
						protected TextScanResult doCheck(String formatted, int options) {
							if (Utils.isEmpty(formatted) ||Utils.parseInt(formatted, -1) >= 0)
								return TextScanResult.SCAN_OK;
							return TextScanResult.SCAN_ERROR;
						}

						@Override
						protected Object convert(Object value) {
							if (Utils.equals(value, (Integer) 0))
								return "";
							return value;
						}
					}, cols(), DialogToolkit.LABEL | SWT.BORDER, "Sample rate[ms]:");
					tlk().addText(variable, new TextController(editor(), OpcUaNode.class.getField("queueSize")) {

						@Override
						public void setControl(Control control, int function) {
							super.setControl(control, function);
							text().setMessage("Default");
						}

						@Override
						protected TextScanResult doCheck(String formatted, int options) {
							if (Utils.isEmpty(formatted) ||Utils.parseInt(formatted, -1) >= 0)
								return TextScanResult.SCAN_OK;
							return TextScanResult.SCAN_ERROR;
						}
						
						@Override
						protected Object convert(Object value) {
							if (Utils.equals(value, (Integer) 0))
								return "";
							return value;
						}
					}, cols(), DialogToolkit.LABEL | SWT.BORDER, "Queue size:");
					tlk().addNothing(variable, tlk().ld(cols(), TLK.GRAB,SWT.DEFAULT));
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				}
				return true;
			}
		}.add(new NameDescriptionEnableProvider());
		provider.setCellClass(OpcUaNode.class);
		return provider;
	}
}
