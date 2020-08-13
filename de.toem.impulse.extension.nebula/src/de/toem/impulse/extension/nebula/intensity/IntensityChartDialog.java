//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.nebula.intensity;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.dialog.ControlProviderElementDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.ITlkEditor;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.extension.nebula.ImpulseNebulaExtension;
import de.toem.impulse.extension.nebula.xy.XyChart;
import de.toem.impulse.scripting.ScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class IntensityChartDialog extends ControlProviderElementDialog {

    public IntensityChartDialog(ITlkEditor parent, IController controller) {
        super(parent, controller,getControls());

    }

    public IntensityChartDialog(ITlkEditor parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public IntensityChartDialog(ITlkEditor parent, IElement element) {
        super(parent, element, getControls());

    }

    public IntensityChartDialog(ITlkEditor parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public IntensityChartDialog(ITlkEditor parent) {
        super(parent, getControls());

    }

    public IntensityChartDialog() {
        super(getControls());

    }

    public static  IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

        	@Override
            public String getHelpContext() {
                return ImpulseNebulaExtension.PLUGIN_ID + "." + "intensity_dialog";
            }
        	
            @Override
            public boolean fillThis() {
                try {

                    Object options =tlk().addGroup(container(), null, cols(), cols(), TLK.NONE, "Options", null);

                    tlk().addButton(options, new CheckController(editor(), IntensityChart.class.getField("showLegend")), 1, TLK.CHECK,
                            "Show legend", null);

                    // script
                    ScriptControls.fillScriptControls(tlk(), container(), editor(), IntensityChart.class.getField("script"),
                            tlk().ld(cols(), TLK.FILL, 450, TLK.FILL, 350), "Nebula Script");


                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.insertBefore(new NameDescriptionEnableProvider());
        provider.setCellClass(IntensityChart.class);
        return provider;
    }
    
 
    protected static void addOptions(Composite options, AbstractControlProvider provider) {

    }
}
