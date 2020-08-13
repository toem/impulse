//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.ports.scope;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.AbstractElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.TcpAdapter;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class ExampleScopeAdapterDialog extends AbstractElementDialog {

    public ExampleScopeAdapterDialog(ITlkEditor parent, IController controller) {
        super(parent, controller);
    }

    public ExampleScopeAdapterDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell);
    }

    public ExampleScopeAdapterDialog(ITlkEditor parent, IElement element) {
        super(parent, element);
    }

    public ExampleScopeAdapterDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements);
    }

    public ExampleScopeAdapterDialog(ITlkEditor parent) {
        super(parent);
    }

    public ExampleScopeAdapterDialog() {
        super();
    }

    protected void createConcreteControl(Composite container) {
        IControlProvider provider = getControls();
        provider.setLayout(COLS, COLS);
        tlk().addControls(container, provider);
    }

    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public boolean fillThis() {
                try {
                    tlk().addText(container(), new TextController(editor(), ExampleScopeAdapter.class.getField("signalCount")), cols(), DialogToolkit.LABEL | TLK.BORDER,
                            "No of Signals:");   
                    tlk().addText(container(), new TextController(editor(), ExampleScopeAdapter.class.getField("signalRate")), cols(), DialogToolkit.LABEL | TLK.BORDER,
                            "Signal rate (us):");  
                    tlk().addText(container(), new TextController(editor(), ExampleScopeAdapter.class.getField("samples")), cols(), DialogToolkit.LABEL | TLK.BORDER,
                            "No of samples:"); 
                   tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")){

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort)getCell()).isPort());
                        } },
                            tlk().ld(cols(), TLK.RIGHT, TLK.DEFAULT), TLK.CHECK, "Insert at Root", null);
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.insertBefore(new NameDescriptionEnableProvider());
        provider.setCellClass(TcpAdapter.class);
        return provider;
    }
}
