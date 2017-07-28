//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.birt;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.kupzog.ktable.SWTX;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.GridController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.grid.GridPropertyModel;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.charts.birt.bar.BarChart;
import de.toem.impulse.charts.birt.line.LineChart;
import de.toem.impulse.charts.birt.pie.PieChart;
import de.toem.impulse.scripting.JsScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.properties.IPropertyModel;

public class BirtChartDialog extends ControlProviderDialog {

    public BirtChartDialog(IEditorParent parent, IController controller) {
        super(parent, controller,getControls());

    }

    public BirtChartDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public BirtChartDialog(IEditorParent parent, IElement element) {
        super(parent, element, getControls());

    }

    public BirtChartDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public BirtChartDialog(IEditorParent parent) {
        super(parent, getControls());

    }

    public BirtChartDialog() {
        super(getControls());

    }

    public static  IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public boolean fillThis() {
                try {
                    tlk().addButtonSet(container(), new RadioSetController(editor(), AbstractBirtChartCell.class.getField("dimensions")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart||getCell() instanceof PieChart;
                        }
                        
                    }, 3, cols(),
                            TLK.LABEL | SWT.RADIO, "Dimensions:", AbstractBirtChartCell.dimensionOptions, null);
                    tlk().addButtonSet(container(), new RadioSetController(editor(), AbstractBirtChartCell.class.getField("showTitle")), 4, cols(),
                            TLK.LABEL | SWT.RADIO, "Title:", AbstractBirtChartCell.titleOptions, null);
                    Composite options =tlk().addGroup(container(), null, cols(), cols(), SWT.NONE, "Options", null);
                    addOptions(options,this);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("log10")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart;
                        }
                        
                    }, 1, SWT.CHECK, "Log10",
                            null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("stacked")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof BarChart;
                        }
                        
                    }, 1, SWT.CHECK, "Stacked",
                            null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("transposed")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart;
                        }
                        
                    }, 1, SWT.CHECK,
                            "Transposed", null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("showLegend")), 1, SWT.CHECK,
                            "Show legend", null);
                    tlk().addText(container(), new TextController(editor(), AbstractBirtChartCell.class.getField("maxSeries")), cols(), TLK.LABEL |SWT.BORDER,
                            "Max series:");
                    tlk().addText(container(), new TextController(editor(), AbstractBirtChartCell.class.getField("maxCategories")), cols(),TLK.LABEL | SWT.BORDER,
                            "Max categories:");
                    
                    tlk().addGrid(container(),
                            new GridController(editor(), AbstractChartCell.class.getField("parameters"), new GridPropertyModel(null, false)) {
                        @Override
                        public boolean needsUpdate() {
                            return true;
                        }

                        @Override
                        protected void doUpdatePre() {
                            super.doUpdatePre();
                            ((GridPropertyModel) this.model).setProvider(null);
                            if (this.getCell() instanceof AbstractChartCell) {
                                IPropertyModel propertyModel = ((AbstractChartCell) this.getCell()).getPropertyModel(true);
                                if (propertyModel != null) {
                                    ((GridPropertyModel) this.model).setProvider(propertyModel);
                                }
                            }
                        }

                    }, tlk().ld(cols(), SWT.FILL, SWT.DEFAULT, SWT.FILL, 100),
                            SWT.BORDER | SWT.MULTI | SWTX.AUTO_SCROLL | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY, null, 2, 8,
                            new String[] { "Parameter", "Value"});
                    
                    tlk().addButton(container(), new CheckController(editor(), AbstractBirtChartCell.class.getField("enableScript")), cols(), SWT.CHECK,
                            "Use Script", null);

                    JsScriptControls.fillScriptControls(tlk(), container(), editor(), clazz().getField("script"), cols(),tlk().ld(cols(),SWT.FILL, 450, SWT.FILL, 350));
                    

                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.add(new NameDescriptionEnableProvider());
        provider.setCellClass(AbstractBirtChartCell.class);
        return provider;
    }
    

    protected static void addOptions(Composite options, AbstractControlProvider provider) {

    }
}
