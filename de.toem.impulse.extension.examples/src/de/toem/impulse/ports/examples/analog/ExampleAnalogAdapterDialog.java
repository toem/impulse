//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.examples.analog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.AbstractElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.SocketAdapter;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class ExampleAnalogAdapterDialog extends AbstractElementDialog {

    public ExampleAnalogAdapterDialog(IEditorParent parent, IController controller) {
        super(parent, controller);
    }

    public ExampleAnalogAdapterDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell);
    }

    public ExampleAnalogAdapterDialog(IEditorParent parent, IElement element) {
        super(parent, element);
    }

    public ExampleAnalogAdapterDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements);
    }

    public ExampleAnalogAdapterDialog(IEditorParent parent) {
        super(parent);
    }

    public ExampleAnalogAdapterDialog() {
        super();
    }

    protected void createConcreteControl(Composite container) {
        IControlProvider provider = getControls();
        provider.setLayout(COLS, COLS);
        tlk.addControls(container, provider);
    }

    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public boolean fillThis() {
                try {
                    tlk().addText(container(), new TextController(editor(), ExampleAnalogAdapter.class.getField("signalCount")), cols(), DialogToolkit.LABEL | SWT.BORDER,
                            "No of Signals:");                      
                    tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")){

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort)getCell()).isPort());
                        } },
                            tlk().ld(cols(), SWT.RIGHT, SWT.DEFAULT), SWT.CHECK, "Insert at Root", null);
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.add(new NameDescriptionEnableProvider());
        provider.setCellClass(SocketAdapter.class);
        return provider;
    }
}
