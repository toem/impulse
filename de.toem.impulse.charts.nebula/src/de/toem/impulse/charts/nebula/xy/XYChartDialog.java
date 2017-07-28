//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.nebula.xy;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;

import de.kupzog.ktable.SWTX;
import de.toem.eclipse.hooks.registry.Registration;
import de.toem.eclipse.toolkits.colorizer.JavaScriptColorizer;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.ButtonController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.GridController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.StyledTextController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.grid.GridPropertyModel;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.parts.editor.ElementEditorInput;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.eclipse.toolkits.util.EclipseUtils;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.charts.nebula.intensity.IntensityChart;
import de.toem.impulse.charts.nebula.xy.XyChart;
import de.toem.impulse.scripting.JsContentProposalExtension;
import de.toem.impulse.scripting.JsScriptControls;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.properties.IPropertyModel;

public class XYChartDialog extends ControlProviderDialog {

    public XYChartDialog(IEditorParent parent, IController controller) {
        super(parent, controller,getControls());

    }

    public XYChartDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());

    }

    public XYChartDialog(IEditorParent parent, IElement element) {
        super(parent, element, getControls());

    }

    public XYChartDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements, getControls());

    }

    public XYChartDialog(IEditorParent parent) {
        super(parent, getControls());

    }

    public XYChartDialog() {
        super(getControls());

    }

    public static  IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            @Override
            public boolean fillThis() {
                try {
                    tlk().addButtonSet(container(), new RadioSetController(editor(), XyChart.class.getField("showTitle")), 4, cols(),
                            TLK.LABEL | SWT.RADIO, "Title:", XyChart.titleOptions, null);
                    Composite options =tlk().addGroup(container(), null, cols(), cols(), SWT.NONE, "Options", null);
                    addOptions(options,this);


                    tlk().addButton(options, new CheckController(editor(), XyChart.class.getField("annotated")), 1, SWT.CHECK,
                            "Annotated", null);
                    tlk().addButton(options, new CheckController(editor(), XyChart.class.getField("showLegend")), 1, SWT.CHECK,
                            "Show legend", null);
                    tlk().addButton(options, new CheckController(editor(), XyChart.class.getField("showAxisTitle")), 1, SWT.CHECK,
                            "Show axis title", null);
                    tlk().addButton(options, new CheckController(editor(), XyChart.class.getField("showGrid")), 1, SWT.CHECK,
                            "Show grid", null);
                    tlk().addText(container(), new TextController(editor(), XyChart.class.getField("maxPoints")), cols(), TLK.LABEL |SWT.BORDER,
                            "Max points:");
                    tlk().addButton(container(), new CheckController(editor(), XyChart.class.getField("enableScript")), cols()-1, SWT.CHECK,
                            "Use Script", null);
                    tlk().addButton(container(), new CheckController(editor(), XyChart.class.getField("manualFill")), 1, SWT.CHECK,
                            "Manual Fill", null);

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
                    
                    JsScriptControls.fillScriptControls(tlk(), container(), editor(), XyChart.class.getField("script"), cols(),tlk().ld(cols(),SWT.FILL, 450, SWT.FILL, 350));


                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }
        }.add(new NameDescriptionEnableProvider());
        provider.setCellClass(XyChart.class);
        return provider;
    }
    

    protected static void addOptions(Composite options, AbstractControlProvider provider) {

    }
}
