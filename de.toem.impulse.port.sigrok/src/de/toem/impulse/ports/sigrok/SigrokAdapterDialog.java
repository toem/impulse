//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.sigrok;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.ButtonController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.ComboSelectController;
import de.toem.eclipse.toolkits.controller.base.ExpandableController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.dialog.TextPopupDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.SocketAdapter;
import de.toem.impulse.ports.sigrok.SigrokScan.ScanResult;
import de.toem.impulse.scripting.JsScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.messages.Messages;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

public class SigrokAdapterDialog extends ControlProviderDialog {

    public SigrokAdapterDialog(IEditorParent parent, IController controller) {
        super(parent, controller,getControls() );
    }

    public SigrokAdapterDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell,getControls() );
    }

    public SigrokAdapterDialog(IEditorParent parent, IElement element) {
        super(parent, element,getControls() );
    }

    public SigrokAdapterDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements,getControls() );
    }

    public SigrokAdapterDialog(IEditorParent parent) {
        super(parent,getControls() );
    }

    public SigrokAdapterDialog() {
        super(getControls() );
    }


    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            boolean initialScanDone = false;
            private ScanResult[] scan;

            @Override
            public String getHelpContext() {
                return SigrokPort.PLUGIN_ID + "." + "sigrok_dialog";
            }
            
            @Override
            public boolean fillThis() {
                try {
                    Composite buttons = tlk().addComposite(container(), null, 5, tlk().ld(cols() - 1, SWT.FILL, SWT.DEFAULT), TLK.LABEL, "sigrok-cli (0.8):", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String show = new SigrokScan().help();
                            if (show != null)
                                new TextPopupDialog(editor().getShell(), "Help (-h)", "", show) {
                                    @Override
                                    protected Point getDefaultSize() {
                                        return new Point(400, 500);
                                    }
                                }.open();
                            else
                                Messages.openError("Sigrok", "Could not execute command 'sigrok-cli' - Please check preferences");
                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Help (-h)", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String version = new SigrokScan().version();
                            if (version != null)
                                new TextPopupDialog(editor().getShell(), "Version (-V)", "", version) {
                                    @Override
                                    protected Point getDefaultSize() {
                                        return new Point(400, 500);
                                    }
                                }.open();
                            else
                                Messages.openError("Sigrok", "Could not execute command 'sigrok-cli' - Please check preferences");

                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Version (-V)", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String version = new SigrokScan().supported();
                            if (version != null)
                                new TextPopupDialog(editor().getShell(), "Supported (-L)", "", version) {
                                    @Override
                                    protected Point getDefaultSize() {
                                        return new Point(400, 500);
                                    }
                                }.open();
                            else
                                Messages.openError("Sigrok", "Could not execute command 'sigrok-cli' - Please check preferences");

                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Supported (-L)", null);                    
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String test = new SigrokScan().setOptions(getCell());
                            if (test != null)
                                new TextPopupDialog(editor().getShell(), "Set (--set)", "", test) {
                                    @Override
                                    protected Point getDefaultSize() {
                                        return new Point(400, 200);
                                    }
                                }.open();
                            else
                                Messages.openError("Sigrok", "Could not execute command 'sigrok-cli' - Please check preferences");

                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Set (--set)", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String test = new SigrokScan().getOptions(getCell());
                            if (test != null)
                                new TextPopupDialog(editor().getShell(), "Get (--get)", "", test) {
                                    @Override
                                    protected Point getDefaultSize() {
                                        return new Point(400, 500);
                                    }
                                }.open();
                            else
                                Messages.openError("Sigrok", "Could not execute command 'sigrok-cli' - Please check preferences");

                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Get (--get)", null);                    
                    final IController driver = tlk().addCombo(container(), new ComboSelectController(editor(), SigrokAdapter.class.getField("driver"), "") {

                        @Override
                        public void populate() {
                            if (scan == null)
                                addItem("Scanning...", "");
                            else {
                                addItem("", "");
                                for (ScanResult r : scan) {
                                    addItem(r.name, r.id);
                                }
                            }
                            if (!initialScanDone) {
                                initialScanDone = true;
                                Actives.run(new IExecutable() {

                                    @Override
                                    public void execute(IProgress p) {
                                        scan = new SigrokScan().scan();
                                        if (scan != null && scan.length > 0)
                                            Actives.runInMain(new IExecutable() {

                                                @Override
                                                public void execute(IProgress p) {
                                                    if (combo() != null && !combo().isDisposed())
                                                        updateControl(true);
                                                }
                                            });
                                    }
                                });
                            }

                        }

                    }, tlk().ld(cols() - 2, TLK.GRAB, SWT.DEFAULT), SWT.READ_ONLY | DialogToolkit.LABEL | SWT.BORDER, "Device:");

                    tlk().addButton(container(), new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            scan = new SigrokScan().scan();
                            driver.updateControl(true);
                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Re-Scan", null);
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("driver")),
                            tlk().ld(cols() - 2, true,true,true,false), DialogToolkit.LABEL | SWT.BORDER, "Driver (-d):");
                    tlk().addButton(container(), new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            super.execute(id, data);
                            String show = new SigrokScan().show(driver.getValueAsString());
                            new TextPopupDialog(editor().getShell(), "Show", "", show).open();
                            driver.updateControl(true);
                        }

                    }, tlk().ld(1, SWT.RIGHT, 100), SWT.NULL, "Show", null);
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("config")), cols(), DialogToolkit.LABEL | SWT.BORDER,
                            "Config (-c):");
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("channels")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Channels (-C):");
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("channelGroup")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Channel Group (-g):");
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("trigger")), cols(), DialogToolkit.LABEL | SWT.BORDER,
                            "Trigger (-t):");
                    tlk().addButton(container(), new CheckController(editor(), SigrokAdapter.class.getField("waitTrigger")), cols(),
                            DialogToolkit.LABEL | SWT.CHECK, "Wait Trigger (-w)", null);
                    Composite expandable = tlk().addExpandable(container(), new ExpandableController(editor(), "dialog.sigrok.protocol", false), cols(),
                            SWT.NULL, "Protocol Decoders");
                    Composite protocol = tlk().addComposite(expandable, null, cols(), null, SWT.NULL, null, null);
                    tlk().addText(protocol, new TextController(editor(), SigrokAdapter.class.getField("protocolDecoders")),
                            tlk().ld(cols() - 1, true,true,true,false), DialogToolkit.LABEL | SWT.BORDER, "Prot. Decoders (-P):");
                    tlk().addText(protocol, new TextController(editor(), SigrokAdapter.class.getField("protocolStack")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Prot. Stack (-S):");
                    tlk().addText(protocol, new TextController(editor(), SigrokAdapter.class.getField("protocolAnnotations")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Prot. Annotation (-A):");
                    tlk().addText(protocol, new TextController(editor(), SigrokAdapter.class.getField("protocolMeta")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Prot. Meta (-M):");
                    tlk().addText(protocol, new TextController(editor(), SigrokAdapter.class.getField("protocolBinary")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Prot. Binary (-B):");
                    tlk().addButtonSet(container(), new RadioSetController(editor(), SigrokAdapter.class.getField("aquisitionMode")), 2, cols(),
                            DialogToolkit.LABEL | SWT.RADIO, "Aquisition Mode:",
                            new String[] { "Samples (--samples)", "Time (--time)", "Frames (--frames)", "Continuous (--continuous)" }, null);
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("aquisitionAmount")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Aquisition Amount:");
                    tlk().addText(container(), new TextController(editor(), SigrokAdapter.class.getField("additionalOptions")), cols(),
                            DialogToolkit.LABEL | SWT.BORDER, "Additional Options:");
                    tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")) {

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort) getCell()).isPort());
                        }
                    }, tlk().ld(cols(), SWT.RIGHT, SWT.DEFAULT), SWT.CHECK, "Insert at Root", null);

                    expandable = tlk().addExpandable(container(), new ExpandableController(editor(), "dialog.port.sync", false),
                            tlk().ld(cols(), TLK.GRAB, SWT.DEFAULT), SWT.NULL, "Synchronisation");
                    Composite sync = tlk().addComposite(expandable, null, cols(), null, SWT.NULL, null, null);
                    tlk().addButton(sync, new CheckController(editor(), clazz().getField("enableSync")) {

                        @Override
                        public boolean enabled() {
                            return !getCellValueAsBoolean("insertAsRoot");
                        }
                    }, cols(), SWT.CHECK, "Enable Sync", null);
                    JsScriptControls.fillScriptControls(tlk(), sync, editor(), clazz().getField("syncScript"), cols());

                    
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
