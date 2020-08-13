//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.birt;

import java.util.List;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.PropertyTableController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.dialog.ControlProviderElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.extension.birt.bar.BarChart;
import de.toem.impulse.extension.birt.line.LineChart;
import de.toem.impulse.extension.birt.pie.PieChart;
import de.toem.impulse.i18n.I18n;
import de.toem.impulse.scripting.ScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.properties.IPropertyModel;

public class BirtChartDialog extends ControlProviderElementDialog {

    public BirtChartDialog(ITlkEditor parent, IController controller) {
        super(parent, controller,getControls());

    }

    public BirtChartDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public BirtChartDialog(ITlkEditor parent, IElement element) {
        super(parent, element, getControls());

    }

    public BirtChartDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public BirtChartDialog(ITlkEditor parent) {
        super(parent, getControls());

    }

    public BirtChartDialog() {
        super(getControls());

    }

    public static  IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public String getHelpContext() {
                return ImpulseBirtExtension.PLUGIN_ID + "." + "birt_dialog";
            }
            
            @Override
            public boolean fillThis() {
                try {
                    tlk().addButtonSet(container(), new RadioSetController(editor(), AbstractBirtChartCell.class.getField("dimensions")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart||getCell() instanceof PieChart;
                        }
                        
                    }, 3, cols(),
                            TLK.LABEL | TLK.RADIO, "Dimensions:", AbstractBirtChartCell.dimensionOptions, null);
                    tlk().addButtonSet(container(), new RadioSetController(editor(), AbstractBirtChartCell.class.getField("showTitle")), 4, cols(),
                            TLK.LABEL | TLK.RADIO, "Title:", AbstractBirtChartCell.titleOptions, null);
                    Object options =tlk().addGroup(container(), null, cols(), cols(), TLK.NONE, "Options", null);
                    addOptions(options,this);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("log10")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart;
                        }
                        
                    }, 1, TLK.CHECK, "Log10",
                            null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("stacked")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof BarChart;
                        }
                        
                    }, 1, TLK.CHECK, "Stacked",
                            null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("transposed")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof LineChart||getCell() instanceof BarChart;
                        }
                        
                    }, 1, TLK.CHECK,
                            "Transposed", null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("inner")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof PieChart;
                        }
                        
                    }, 1, TLK.CHECK,
                            "Inner", null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("explode")){

                        @Override
                        public boolean enabled() {
                            return getCell() instanceof PieChart;
                        }
                        
                    }, 1, TLK.CHECK,
                            "Explode", null);
                    tlk().addButton(options, new CheckController(editor(), AbstractBirtChartCell.class.getField("showLegend")), 1, TLK.CHECK,
                            "Show legend", null);
                    Object limitation = tlk().addGroup(container(), null, cols(), cols(), TLK.NULL, "Limitations", null);
                    tlk().addText(limitation, new TextController(editor(), AbstractBirtChartCell.class.getField("maxSeries")), cols(), TLK.LABEL |TLK.BORDER,
                            "Max series:");
                    tlk().addText(limitation, new TextController(editor(), AbstractBirtChartCell.class.getField("maxCategories")), cols(),TLK.LABEL | TLK.BORDER,
                            "Max categories:");
                                       
                    // script
                    Object script = ScriptControls.fillScriptControls(tlk() , container(), editor(), clazz().getField("script"), tlk().ld(cols(),TLK.FILL, 450, TLK.FILL, 250),"Birt Script");                 
                    tlk().addButton(script, new CheckController(editor(), AbstractBirtChartCell.class.getField("enableScript")), cols(), TLK.CHECK, 
                            "Apply script", null);

                    // Default plot parameter
                    tlk().addLabel(container(), null, cols(), TLK.NULL, "Default Plot Parameter:", null);
                    tlk().addTable(container(),
                            new PropertyTableController(editor(), AbstractChartCell.class.getField("parameters")) {
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



                    }, tlk().ld(cols(), TLK.FILL, TLK.DEFAULT, TLK.FILL, 100),
                            TLK.BORDER | TLK.LEAD, null, new String[] { "Parameter", "Value"});
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.insertBefore(new NameDescriptionEnableProvider());
        provider.setCellClass(AbstractBirtChartCell.class);
        return provider;
    }
    

    protected static void addOptions(Object options, AbstractControlProvider provider) {

    }
}
