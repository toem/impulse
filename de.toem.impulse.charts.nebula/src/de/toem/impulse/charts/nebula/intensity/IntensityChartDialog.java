//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.nebula.intensity;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;

import de.toem.eclipse.hooks.registry.Registration;
import de.toem.eclipse.toolkits.colorizer.JavaScriptColorizer;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.ButtonController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.StyledTextController;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.parts.editor.ElementEditorInput;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.util.EclipseUtils;
import de.toem.impulse.scripting.JsContentProposalExtension;
import de.toem.impulse.scripting.JsScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class IntensityChartDialog extends ControlProviderDialog {

    public IntensityChartDialog(IEditorParent parent, IController controller) {
        super(parent, controller,getControls());

    }

    public IntensityChartDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public IntensityChartDialog(IEditorParent parent, IElement element) {
        super(parent, element, getControls());

    }

    public IntensityChartDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public IntensityChartDialog(IEditorParent parent) {
        super(parent, getControls());

    }

    public IntensityChartDialog() {
        super(getControls());

    }

    public static  IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public boolean fillThis() {
                try {

                    Composite options =tlk().addGroup(container(), null, cols(), cols(), SWT.NONE, "Options", null);

                    tlk().addButton(options, new CheckController(editor(), IntensityChart.class.getField("showLegend")), 1, SWT.CHECK,
                            "Show legend", null);

                    JsScriptControls.fillScriptControls(tlk(), container(), editor(), IntensityChart.class.getField("script"), cols(),tlk().ld(cols(),SWT.FILL, 450, SWT.FILL, 350));

                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.add(new NameDescriptionEnableProvider());
        provider.setCellClass(IntensityChart.class);
        return provider;
    }
    
 
    protected static void addOptions(Composite options, AbstractControlProvider provider) {

    }
}
