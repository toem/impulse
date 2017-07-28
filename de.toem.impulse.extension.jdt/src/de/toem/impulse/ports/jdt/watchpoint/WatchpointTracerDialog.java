//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.jdt.watchpoint;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.ComboSelectController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.AbstractElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.SocketAdapter;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class WatchpointTracerDialog extends AbstractElementDialog {

    public WatchpointTracerDialog(IEditorParent parent, IController controller) {
        super(parent, controller);
    }

    public WatchpointTracerDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell);
    }

    public WatchpointTracerDialog(IEditorParent parent, IElement element) {
        super(parent, element);
    }

    public WatchpointTracerDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements);
    }

    public WatchpointTracerDialog(IEditorParent parent) {
        super(parent);
    }

    public WatchpointTracerDialog() {
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
                    tlk().addButton(container(), new CheckController(editor(), WatchpointTracer.class.getField("enableWatchpoints")), cols(), SWT.CHECK,
                            "Enable Watchpoint Tracing", null);
                    Composite watchGroup = tlk().addGroup(container(), null, cols(), cols(), SWT.NULL, "Watchpoints", null);
                    tlk().addCombo(
                            watchGroup,
                            new ComboSelectController(editor(), WatchpointTracer.class.getField("watchpointSignal"),
                                    WatchpointTracer.WATCHPOINT_SIGNAL_LABELS, WatchpointTracer.WATCHPOINT_SIGNAL_VALUES), cols(), SWT.READ_ONLY | TLK.LABEL,
                            "Signal:");
                    tlk().addButtonSet(watchGroup, new RadioSetController(editor(), WatchpointTracer.class.getField("watchpointAction")), 2, cols(),
                            SWT.RADIO | TLK.LABEL, "Action:", WatchpointTracer.ACTION_LABELS, null);
                    tlk().addButton(watchGroup, new CheckController(editor(), WatchpointTracer.class.getField("enableWatchpointExpressions")), cols(), SWT.CHECK,
                            "Enable Expression Tracing", null);
                    tlk().addText(watchGroup, new TextController(editor(),WatchpointTracer.class.getField("watchpointExpressionFilter")).setNullText("comma separated expr. fragments"), COLS, SWT.BORDER|TLK.LABEL, "Expression Filter:");

                    
                    tlk().addButton(container(), new CheckController(editor(), WatchpointTracer.class.getField("enableBreakpoints")), cols(), SWT.CHECK,
                            "Enable Breakpoint Tracing", null);
                    Composite breakGroup = tlk().addGroup(container(), null, cols(), cols(), SWT.NULL, "Breakpoints", null);
                    tlk().addCombo(
                            breakGroup,
                            new ComboSelectController(editor(), WatchpointTracer.class.getField("breakpointSignal"),
                                    WatchpointTracer.BREAKPOINT_SIGNAL_LABELS, WatchpointTracer.BREAKPOINT_SIGNAL_VALUES), cols(), SWT.READ_ONLY | TLK.LABEL,
                            "Signal:");
                    tlk().addButtonSet(breakGroup, new RadioSetController(editor(), WatchpointTracer.class.getField("breakpointAction")), 2, cols(),
                            SWT.RADIO | TLK.LABEL, "Action:", WatchpointTracer.ACTION_LABELS, null);
                    tlk().addButton(breakGroup, new CheckController(editor(), WatchpointTracer.class.getField("enableBreakpointExpressions")), cols(), SWT.CHECK,
                            "Enable Expression Tracing", null);
                    tlk().addText(breakGroup, new TextController(editor(),WatchpointTracer.class.getField("breakpointExpressionFilter")).setNullText("comma separated expr. fragments"), COLS, SWT.BORDER|TLK.LABEL, "Expression Filter:");

                    tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")) {

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort) getCell()).isPort());
                        }
                    }, tlk().ld(cols(), SWT.RIGHT, SWT.DEFAULT), SWT.CHECK, "Insert at Root", null);
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
