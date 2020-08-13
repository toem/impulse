//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.yakindu.chart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.ButtonController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.ComboEditController;
import de.toem.eclipse.toolkits.controller.base.CompositeComboController;
import de.toem.eclipse.toolkits.controller.base.ImageController;
import de.toem.eclipse.toolkits.controller.base.PropertyTableController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.cells.CellTableController;
import de.toem.eclipse.toolkits.controller.commands.CommandButtonController;
import de.toem.eclipse.toolkits.dialog.ControlProviderElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.ImpulsePreferences;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.extension.yakindu.ImpulseYakinduExtension;
import de.toem.impulse.extension.yakindu.StateChartLoader;
import de.toem.impulse.i18n.I18n;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ElementFieldModifier;
import de.toem.pattern.element.ElementHierarchyModifier;
import de.toem.pattern.element.ElementModifierEvent;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.element.IElementModifier;
import de.toem.pattern.element.Link;
import de.toem.pattern.messages.IMessages.IProgressMessage;
import de.toem.pattern.messages.Messages;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.registry.Registration;
import de.toem.pattern.threading.Progress;

public class StateChartDialog extends ControlProviderElementDialog {

    public StateChartDialog(ITlkEditor parent, IController controller) {
        super(parent, controller, getControls());

    }

    public StateChartDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public StateChartDialog(ITlkEditor parent, IElement element) {
        super(parent, element, getControls());

    }

    public StateChartDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public StateChartDialog(ITlkEditor parent) {
        super(parent, getControls());

    }

    public StateChartDialog() {
        super(getControls());

    }

    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public String getHelpContext() {
                return ImpulseYakinduExtension.PLUGIN_ID + "." + "statechart_dialog";
            }

            @Override
            public boolean fillThis() {

                // Default plot parameter
                tlk().addLabel(container(), null, cols(), TLK.NULL, "Default Plot Parameter:", null);
                tlk().addTable(container(), new PropertyTableController(editor(), source("parameters")) {
                    @Override
                    public boolean needsUpdate() {
                        return true;
                    }

                    @Override
                    protected void doUpdatePre() {
                        super.doUpdatePre();
                        setModel(null);
                        if (this.getCell() instanceof AbstractChartCell) {
                            IPropertyModel propertyModel = ((AbstractChartCell) this.getCell()).getPropertyModel(true);
                            if (propertyModel != null) {
                                setModel(propertyModel);
                            }
                        }
                    }

                }, tlk().ld(cols(), TLK.FILL, TLK.DEFAULT, TLK.FILL, 100), TLK.BORDER | TLK.LEAD, null, new String[] { "Parameter", "Value" });

                return true;
            }

        }.insertBefore(getStateControls(false)).insertBefore(new NameDescriptionEnableProvider());
        provider.setCellClass(StateChart.class);
        return provider;
    }

    public static IControlProvider getStateControls(boolean forProducer) {
        IControlProvider provider = new AbstractControlProvider() {

            IController modeController;
            IController contentController;
            IController resourceController;
            IController fileController;
            IController chartController;
            IController diagramController;
            Object synchonize;

            @Override
            public boolean fillThis() {
                try {
                    Object composite;
                    if (!forProducer) {
                        modeController = tlk().addButtonSet(container(), new RadioSetController(editor(), source("mode")) {

                            @Override
                            protected void doUpdateExternal() {
                                tlk().showControl(getValueAsInt() == StateChart.MODE_RESORUCE_REFERENCE, resourceController);
                                tlk().showControl(getValueAsInt() == StateChart.MODE_FILE_REFERENCE, fileController);
                                tlk().showControl(getValueAsInt() == StateChart.MODE_CHART_REFERENCE, chartController);
                                tlk().showControl(getValueAsInt() == StateChart.MODE_RESORUCE_REFERENCE || getValueAsInt() == StateChart.MODE_FILE_REFERENCE,
                                        synchonize);
                                super.doUpdateExternal();
                            }

                        }, 2, cols(), TLK.LABEL | TLK.RADIO, "Mode:", StateChart.modeLabels, null);

                        tlk().addCompositeCombo(container(), resourceController = new CompositeComboController(editor(), source("resourceReference")) {

                            @Override
                            public void opened() {
                                Object resource = Messages.openResource("Open resource", ResourcesPlugin.getWorkspace().getRoot(), null, IResource.FILE);
                                if (resource != null) {
                                    IElement element = Elements.getElement(resource);
                                    if (element.isBound()) {
                                        setValue(element.getLink(Elements.getElement(ResourcesPlugin.getWorkspace().getRoot())));
                                        syncDiagrams(getCell());
                                    }
                                }
                                super.opened();
                            }

                            @Override
                            public void closed() {
                                super.closed();
                            }
                            
                            protected void doUpdateText(Object value) {

                                if (value instanceof Link) {
                                    combo().setText(((Link) value).getPath());
                                    combo().setToolTipText(((Link) value).getPath());
                                } else super.doUpdateText(value);
                            }
                            
                            @Override
                            protected Object revert(Object value) {
                                if (value instanceof String)
                                return Link.fromPath((String) value);
                                return super.revert(value);
                            }

                        }, tlk().ld(cols() - 1, true, true, true, false), TLK.BORDER | TLK.DROP_DOWN | TLK.LABEL, I18n.General_Resource_,
                                Registration.images.get("de.toem.eclipse.toolkits.images.general.combo_edit"));

                        tlk().addCompositeCombo(container(), fileController = new CompositeComboController(editor(), source("fileReference")) {

                            @Override
                            public void opened() {
                                File file = Utils.isEmpty(getValueAsString()) ? null : new File(getValueAsString());
                                file = Messages.openFile("Open file", file, new String[] { "*.sct" });
                                if (file != null) {
                                    setValue(file.getPath());
                                    syncDiagrams(getCell());
                                }
                                super.opened();
                            }

                            @Override
                            public void closed() {
                                super.closed();
                            }

                        }, tlk().ld(cols() - 1, true, true, true, false), TLK.READ_ONLY | TLK.BORDER | TLK.DROP_DOWN | TLK.LABEL, I18n.General_Path_,
                                Registration.images.get("de.toem.eclipse.toolkits.images.general.combo_edit"));
                        synchonize = tlk().addGroup(container(), null, 2, cols(), TLK.NONE, I18n.General_Synchronisation, null);

                        tlk().addButton(synchonize, new CheckController(editor(), source("synchronize")) {
                        }, tlk().ld(1, TLK.GRAB, TLK.IGNORE_CONTROL), TLK.CHECK, "Synchornize on Refresh", null);
                        tlk().addButton(synchonize, new ButtonController(editor(), null) {

                            @Override
                            public void execute(String id, Object data) {
                                syncDiagrams(getCell());
                            }
                        }, 1, TLK.PUSH, "Synchronize now", null);
                    }

                    IController chartTableController;

                    chartTableController = new CellTableController(editor(), source("chartReference")) {

                        @Override
                        public void selectionChanged() {
                            tlk().updateEnable();
                            super.selectionChanged();
                        }

                    }.initCells(ImpulsePreferences.chartPreferences, AbstractChartCell.class).initColumnDataSources(true,
                            new Object[] { AbstractChartCell.class.getField("description") });

                    Object combo = tlk().addCompositeCombo(container(), chartController = new CompositeComboController(editor(), source("chartReference")) {

                        @Override
                        protected Object convert(Object value) {
                            if (value instanceof Link)
                                return ((Link) value).getPath();
                            return "";
                        }
                    }.initContentController(chartTableController), tlk().ld(cols() - 1, true, true, true, false),
                            TLK.READ_ONLY | TLK.BORDER | TLK.DROP_DOWN | TLK.LABEL, I18n.General_Chart_,
                            Registration.images.get("de.toem.eclipse.toolkits.images.general.combo_edit"));
                    composite = tlk().addGroup(combo, null, cols(), null, TLK.BORDER, I18n.General_Chart, null);

                    tlk().addNothing(composite, tlk().ld(cols(), SWT.FILL, tlk().wc(60)));
                    tlk().addTable(composite, chartTableController, tlk().ld(cols() - 1, TLK.GRAB, TLK.IGNORE_CONTROL, SWT.FILL, 250),
                            TLK.ICON | TLK.SORT | TLK.FILTER | TLK.FULL_SELECTION | TLK.MULTI, null,
                            new String[] { I18n.General_Name, I18n.General_Description });
                    Object buttonComp = tlk().addComposite(composite, null, 1, tlk().ld(1, SWT.FILL, SWT.DEFAULT, SWT.TOP, SWT.DEFAULT), 0, null, null);
                    CommandButtonController.addEditDefaultButtons(tlk(), buttonComp, editor(), chartTableController, true, true, true, false);

                    // contentController = new ValueController(editor(), source("content"));

                    if (!forProducer)
                        diagramController = tlk().addCombo(container(), new ComboEditController(editor(), source("diagram")) {

                            @Override
                            public boolean isAffected(ElementModifierEvent event) {
                                return true;
                            }

                            @Override
                            public boolean needsUpdate() {
                                return true;
                            }

                            @Override
                            public void populate() {
                                super.populate();
                                if (hasCell())
                                    for (ICell cell : getCell().getChildren())
                                        addItem(cell.getName());
                            }

                        }, tlk().ld(cols() - 1, true, true, true, false), TLK.LABEL, "Diagram:");

                    Object image = tlk().addComposite(container(), new ImageController(editor(), null) {

                        @Override
                        public boolean isAffected(ElementModifierEvent event) {
                            return true;
                        }

                        @Override
                        public boolean needsUpdate() {
                            return true;
                        }

                        @Override
                        public Object value() {
                            StateChartDiagram diagram = null;
                            if (forProducer)
                                diagram = StateChart.getDiagram(chartController.getValueAsLink());
                            else {
                                String name = diagramController.getValueAsString();
                                diagram = hasCell() ? (StateChartDiagram) getCell().getChildByName(name, StateChartDiagram.class) : null;
                            }
                            return diagram != null ? diagram.image : null;
                        }
                    }.initScale(true, true).initAlign(TLK.CENTER, TLK.CENTER), null, tlk().ld(cols(), TLK.GRAB, TLK.IGNORE_CONTROL, TLK.FILL, 100), TLK.BORDER,
                            null, null);

                    // TLK.applyBackground(image, TLK.COLOR_LIST_BACKGROUND, false);
                } catch (NoSuchFieldException |

                        SecurityException e) {
                }
                return true;
            }

            private void syncDiagrams(final ICell base) {

                if (base == null || !base.isBound())
                    return;

                // parameter
                int mode = modeController.getValueAsInt();
                Link resourceReference = resourceController.getValueAsLink();
                String fileReference = fileController.getValueAsString();

                // progress
                final IProgressMessage[] progressMessage = new IProgressMessage[1];
                Progress status = new Progress() {
                    protected void changed() {
                        if (progressMessage[0] != null)
                            progressMessage[0].update();
                    }
                };
                progressMessage[0] = Messages.openProgress("Load Statechart", status);
                status.start();
                
                StateChartLoader loader = StateChart.loadChart(this, mode,
                        mode == StateChart.MODE_RESORUCE_REFERENCE ? resourceReference : mode == StateChart.MODE_FILE_REFERENCE ? fileReference : null,true);

                // check loader
                if (loader == null)
                    Messages.openError("Open statechart", "Could not find valid statechart!");
                else {

                    // modifier
                    List<IElementModifier> modifiers = new ArrayList<IElementModifier>();

                    // remove first
                    if (base.hasChildren())
                        modifiers.add(ElementHierarchyModifier.removeAll(base.getElement()));

                    // create
                    List<StateChartDiagram> diagrams = StateChart.createDiagrams(loader, status);

                    // add new diagrams
                    for (StateChartDiagram diagram : diagrams) {
                        modifiers.add(ElementHierarchyModifier.add(base.getElement(), Elements.getElements(diagram), null));
                        status.done(1.0 / loader.getDiagramNames().size());
                    }

                    // change content
                    modifiers.add(new ElementFieldModifier(base.getElement(), "content", ElementFieldModifier.MODIFICATION_REPLACE, loader.getBytes(), false));

                    editor().apply("Sync state chart", null, modifiers.toArray(new IElementModifier[modifiers.size()]), true);
                }
                progressMessage[0].close();
                progressMessage[0]=null;
            }

        };
        provider.setCellClass(StateChart.class);
        return provider;
    }

    protected static void addOptions(Object options, AbstractControlProvider provider) {

    }
}
