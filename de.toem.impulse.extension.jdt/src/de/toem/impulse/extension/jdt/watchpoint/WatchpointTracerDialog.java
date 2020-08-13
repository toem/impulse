//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.jdt.watchpoint;

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
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.TcpAdapter;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class WatchpointTracerDialog extends AbstractElementDialog {

    public WatchpointTracerDialog(ITlkEditor parent, IController controller) {
        super(parent, controller);
    }

    public WatchpointTracerDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell);
    }

    public WatchpointTracerDialog(ITlkEditor parent, IElement element) {
        super(parent, element);
    }

    public WatchpointTracerDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements);
    }

    public WatchpointTracerDialog(ITlkEditor parent) {
        super(parent);
    }

    public WatchpointTracerDialog() {
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
                    tlk().addButton(container(), new CheckController(editor(), WatchpointTracer.class.getField("enableWatchpoints")), cols(), TLK.CHECK,
                            "Enable Watchpoint Tracing", null);
                    Object watchGroup = tlk().addGroup(container(), null, cols(), cols(), TLK.NULL, "Watchpoints", null);
                    tlk().addCombo(
                            watchGroup,
                            new ComboSelectController(editor(), WatchpointTracer.class.getField("watchpointSignal"),
                                    WatchpointTracer.WATCHPOINT_SIGNAL_LABELS, WatchpointTracer.WATCHPOINT_SIGNAL_VALUES), cols(), TLK.READ_ONLY | TLK.LABEL,
                            "Signal:");
                    tlk().addButtonSet(watchGroup, new RadioSetController(editor(), WatchpointTracer.class.getField("watchpointAction")), 2, cols(),
                            TLK.RADIO | TLK.LABEL, "Action:", WatchpointTracer.ACTION_LABELS, null);
                    tlk().addButton(watchGroup, new CheckController(editor(), WatchpointTracer.class.getField("enableWatchpointExpressions")), cols(), TLK.CHECK,
                            "Enable Expression Tracing", null);
                    tlk().addText(watchGroup, new TextController(editor(),WatchpointTracer.class.getField("watchpointExpressionFilter")).setNullText("comma separated expr. fragments"), COLS, TLK.BORDER|TLK.LABEL, "Expression Filter:");

                    
                    tlk().addButton(container(), new CheckController(editor(), WatchpointTracer.class.getField("enableBreakpoints")), cols(), TLK.CHECK,
                            "Enable Breakpoint Tracing", null);
                    Object breakGroup = tlk().addGroup(container(), null, cols(), cols(), TLK.NULL, "Breakpoints", null);
                    tlk().addCombo(
                            breakGroup,
                            new ComboSelectController(editor(), WatchpointTracer.class.getField("breakpointSignal"),
                                    WatchpointTracer.BREAKPOINT_SIGNAL_LABELS, WatchpointTracer.BREAKPOINT_SIGNAL_VALUES), cols(), TLK.READ_ONLY | TLK.LABEL,
                            "Signal:");
                    tlk().addButtonSet(breakGroup, new RadioSetController(editor(), WatchpointTracer.class.getField("breakpointAction")), 2, cols(),
                            TLK.RADIO | TLK.LABEL, "Action:", WatchpointTracer.ACTION_LABELS, null);
                    tlk().addButton(breakGroup, new CheckController(editor(), WatchpointTracer.class.getField("enableBreakpointExpressions")), cols(), TLK.CHECK,
                            "Enable Expression Tracing", null);
                    tlk().addText(breakGroup, new TextController(editor(),WatchpointTracer.class.getField("breakpointExpressionFilter")).setNullText("comma separated expr. fragments"), COLS, TLK.BORDER|TLK.LABEL, "Expression Filter:");

                    tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")) {

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort) getCell()).isPort());
                        }
                    }, tlk().ld(cols(), TLK.RIGHT, TLK.DEFAULT), TLK.CHECK, "Insert at Root", null);
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
