//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.nebula.eye;

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
import de.toem.impulse.extension.nebula.ImpulseNebulaExtension;
import de.toem.impulse.scripting.ScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.properties.IPropertyModel;

public class EyeChartDialog extends ControlProviderElementDialog {

    public EyeChartDialog(ITlkEditor parent, IController controller) {
        super(parent, controller, getControls());

    }

    public EyeChartDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public EyeChartDialog(ITlkEditor parent, IElement element) {
        super(parent, element, getControls());

    }

    public EyeChartDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public EyeChartDialog(ITlkEditor parent) {
        super(parent, getControls());

    }

    public EyeChartDialog() {
        super(getControls());

    }

    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public String getHelpContext() {
                return ImpulseNebulaExtension.PLUGIN_ID + "." + "eye_dialog";
            }

            @Override
            public boolean fillThis() {
                try {
                    tlk().addButtonSet(container(), new RadioSetController(editor(), EyeChart.class.getField("showTitle")), 4, cols(), TLK.LABEL | TLK.RADIO,
                            "Title:", EyeChart.titleOptions, null);

                    Object options = tlk().addGroup(container(), null, cols(), cols(), TLK.NONE, "Options", null);
                    addOptions(options, this);

                    tlk().addButton(options, new CheckController(editor(), EyeChart.class.getField("annotated")), 1, TLK.CHECK, "Annotated", null);
                    tlk().addButton(options, new CheckController(editor(), EyeChart.class.getField("showLegend")), 1, TLK.CHECK, "Show legend", null);
                    tlk().addButton(options, new CheckController(editor(), EyeChart.class.getField("showAxisTitle")), 1, TLK.CHECK, "Show axis title", null);
                    tlk().addButton(options, new CheckController(editor(), EyeChart.class.getField("showGrid")), 1, TLK.CHECK, "Show grid", null);
                    tlk().addText(container(), new TextController(editor(), EyeChart.class.getField("maxPoints")), cols(), TLK.LABEL | TLK.BORDER,
                            "Max points:");

                    // script
                    Object script = ScriptControls.fillScriptControls(tlk(), container(), editor(), EyeChart.class.getField("script"),
                            tlk().ld(cols(), TLK.FILL, 450, TLK.FILL, 350), "Nebula Script");
                    tlk().addButton(script, new CheckController(editor(), EyeChart.class.getField("enableScript")), cols(), TLK.CHECK, "Apply script", null);
                    tlk().addButton(script, new CheckController(editor(), EyeChart.class.getField("manualFill")), cols(), TLK.CHECK, "Manual Fill", null);

                    // Default plot parameter
                    tlk().addLabel(container(), null, cols(), TLK.NULL, "Default Plot Parameter:", null);
                    tlk().addTable(container(), new PropertyTableController(editor(), AbstractChartCell.class.getField("parameters")) {
                        @Override
                        public boolean needsUpdate() {
                            return true;
                        }

                        @Override
                        protected void doUpdatePre() {
                            super.doUpdatePre();
                            setModel(null);
                            if (this.getCell() instanceof EyeChart) {
                                IPropertyModel propertyModel = ((EyeChart) this.getCell()).getPropertyModel(true);
                                if (propertyModel != null) {
                                    setModel(propertyModel);
                                }
                            }
                        }

                    }, tlk().ld(cols(), TLK.FILL, TLK.DEFAULT, TLK.FILL, 100), TLK.BORDER | TLK.LEAD, null, new String[] { "Parameter", "Value" });
                    
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.insertBefore(new NameDescriptionEnableProvider());
        provider.setCellClass(EyeChart.class);
        return provider;
    }

    protected static void addOptions(Object options, AbstractControlProvider provider) {

    }
}
