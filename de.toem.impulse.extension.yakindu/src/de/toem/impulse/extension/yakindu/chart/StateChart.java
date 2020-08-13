package de.toem.impulse.extension.yakindu.chart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.OffscreenEditPartFactory;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.render.clipboard.DiagramGenerator;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.yakindu.base.base.NamedElement;
import org.yakindu.sct.model.sexec.naming.INamingService;
import org.yakindu.sct.model.sgraph.FinalState;
import org.yakindu.sct.model.sgraph.Region;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Vertex;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.ImpulsePreferences;
import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.extension.yakindu.StateChartLoader;
import de.toem.impulse.paint.ICursorItem;
import de.toem.impulse.paint.IPaint;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.plan.IPlan;
import de.toem.impulse.paint.plan.IPlan.IExternalPlanner;
import de.toem.impulse.paint.plan.IPlan.IPaintPlanGenerator;
import de.toem.impulse.paint.plan.IPlan.IPaintPlanner;
import de.toem.impulse.paint.plan.IPlan.ISinglePaintPlanner;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.scripting.Scripting;
import de.toem.impulse.ui.IRecordViewer;
import de.toem.impulse.ui.ImpulseNotifications;
import de.toem.impulse.ui.RecordViewerListener;
import de.toem.impulse.values.CompoundValue;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.ElementFieldModifier;
import de.toem.pattern.element.ElementHierarchyModifier;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElementModifier;
import de.toem.pattern.element.Link;
import de.toem.pattern.messages.IMessages.IProgressMessage;
import de.toem.pattern.messages.Messages;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;
import de.toem.pattern.threading.Progress;

@CellAnnotation(type = StateChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class StateChart extends AbstractChartCell implements IExternalPlanner<IPlotItem> {
    public static final String TYPE = "chart.yakindu.statechart";

    public static final int ALLIGN_LEFT = IPaint.PAINT_PAR_ALLIGN_LEFT;
    public static final int ALLIGN_CENTER = IPaint.PAINT_PAR_ALLIGN_CENTER;
    public static final int ALLIGN_RIGHT = IPaint.PAINT_PAR_ALLIGN_RIGHT;
    public static final String[] horizontalAlignLabels = new String[] { "Left", "Center", "Right" };
    public static final int ALLIGN_TOP = IPaint.PAINT_PAR_ALLIGN_TOP;
    public static final int ALLIGN_MIDDLE = IPaint.PAINT_PAR_ALLIGN_MIDDLE;
    public static final int ALLIGN_BOTTOM = IPaint.PAINT_PAR_ALLIGN_BOTTOM;
    public static final String[] verticalAlignLabels = new String[] { "Top", "Center", "Bottom" };
    public static final int MODE_CONTENT = 0;
    public static final int MODE_RESORUCE_REFERENCE = 1;
    public static final int MODE_FILE_REFERENCE = 2;
    public static final int MODE_CHART_REFERENCE = 3;

    public static final String[] modeLabels = { "Content", "Resource reference", "File reference" };

    public int mode = MODE_RESORUCE_REFERENCE;
    public byte[] content;
    public Link resourceReference;
    public String fileReference;
    public String diagram;
    public boolean synchronize = false;

    INamingService servise;

    // ========================================================================================================================
    // Property Model
    // ========================================================================================================================

    @Override
    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        tlk().addText(container(), new TextController(editor(), new PropertySource("diagram")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Diagram:");
                        tlk().addButton(container(), new CheckController(editor(), new PropertySource("scaleToWidthHeight")), cols(), TLK.CHECK | TLK.LABEL,
                                "Scale:", null);
                        tlk().addButtonSet(container(), new RadioSetController(editor(), new PropertySource("horizontalAlign")), cols(), cols(),
                                TLK.RADIO | TLK.LABEL, "Horizontal:", horizontalAlignLabels, null);
                        tlk().addButtonSet(container(), new RadioSetController(editor(), new PropertySource("verticalAlign")), cols(), cols(),
                                TLK.RADIO | TLK.LABEL, "Vertical:", verticalAlignLabels, null);
                        tlk().addButton(container(), new CheckController(editor(), new PropertySource("adaptColors")), cols(), TLK.CHECK | TLK.LABEL,
                                "Adapt Colors:", null);
                        return true;
                    }
                };

            }
        }.add("diagram", "", "Diagram", null, null).add("scaleToWidthHeight", true, "Scale", "Scale to fit width/height")
                .add("horizontalAlign", ALLIGN_CENTER, "Horizontal", horizontalAlignLabels, "Select the horizontal Align")
                .add("verticalAlign", ALLIGN_MIDDLE, "Vertical", verticalAlignLabels, "Select the horizontal Align")
                .add("adaptColors", true, "Adapt Colors", "Adapt color on dark background");
        if (configured)
            model.setTotal(parameters);
        return model;
    }

    // ========================================================================================================================
    // Loader
    // ========================================================================================================================

    public StateChartLoader loadChart(boolean parse, boolean ignoreContent) {
        StateChartLoader loader = null;
        if (mode == MODE_FILE_REFERENCE)
            loader = StateChartLoader.fromFilePath(fileReference, ignoreContent ? null : content, parse);
        else if (mode == MODE_CONTENT)
            loader = StateChartLoader.fromByteArray(content, parse);
        else if (mode == MODE_RESORUCE_REFERENCE)
            loader = StateChartLoader.fromResource(resourceReference, ignoreContent ? null : content, parse);
        return loader == null || (parse && loader.isEmpty()) ? null : loader;
    }

    public static StateChartLoader loadChart(Object caller, int mode, Object source, boolean parse) {

        StateChartLoader loader = null;
        if (mode == MODE_FILE_REFERENCE && source instanceof String)
            loader = StateChartLoader.fromFilePath((String) source, null, parse);
        else if (mode == MODE_CONTENT && source instanceof byte[])
            loader = StateChartLoader.fromByteArray((byte[]) source, parse);
        else if (mode == MODE_RESORUCE_REFERENCE && source instanceof Link)
            loader = StateChartLoader.fromResource((Link) source, null, parse);
        else if (mode == MODE_CHART_REFERENCE && source instanceof Link) {
            ICell chart = ((Link) source).resolveCell(ImpulsePreferences.chartPreferences);
            if (chart instanceof StateChart && chart != caller)
                return ((StateChart) chart).loadChart(parse, false);
        }
        return loader == null || (parse && loader.isEmpty()) ? null : loader;
    }

    // ========================================================================================================================
    // Create content
    // ========================================================================================================================

    public static List<StateChartDiagram> createDiagrams(StateChartLoader loader, IProgress progress) {

        // progress
        List<StateChartDiagram> diagrams = new ArrayList<>();

        // add new diagrams
        for (String diagram : loader.getDiagramNames()) {
            progress.doing("Load " + diagram);
            // progress.update();
            StateChartDiagram diagramCell = StateChart.createDiagram(loader, diagram);
            if (diagramCell != null)
                diagrams.add(diagramCell);
            progress.done(1.0 / loader.getDiagramNames().size());
        }
        return diagrams;
    }

    public static StateChartDiagram createDiagram(StateChartLoader loader, String diagramName) {

        try {

            StateChartDiagram diagramCell = new StateChartDiagram();
            Statechart statechart = loader.getStateChart();
            Diagram diagram = loader.getDiagram(diagramName);
            if (diagram == null)
                return null;
            diagramCell.setName(diagramName);

            // create offline editor
            final DiagramEditPart[] editPart = new DiagramEditPart[1];
            final Shell[] shell = new Shell[1];
            Actives.runInMain(new IExecutable() {

                @Override
                public void execute(IProgress p) {

                    shell[0] = new Shell();
                    try {
                        editPart[0] = OffscreenEditPartFactory.getInstance().createDiagramEditPart(diagram, shell[0]);
                    } catch (Throwable e) {
                        shell[0].dispose();
                    }
                }
            }, true);
            if (editPart[0] == null)
                return null;

            // generate png
            DiagramImageGenerator imageGenerator = new DiagramImageGenerator(editPart[0]);
            imageGenerator.setImageMargin(0);
            imageGenerator.createSWTImageDescriptorForDiagram();
            List<?> editparts = editPart[0].getPrimaryEditParts();
            Rectangle imageRect = imageGenerator.calculateImageRectangle(editparts);
            diagramCell.image = imageGenerator.getBytes();
            diagramCell.x = imageRect.x;
            diagramCell.y = imageRect.y;
            diagramCell.width = imageRect.width;
            diagramCell.height = imageRect.height;

            // extract state figures
            getStateFigures(editPart[0], statechart, diagramCell);

            return diagramCell;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    // get the state
    private static void getStateFigures(DiagramEditPart editPart, Statechart statechart, StateChartDiagram diagramCell) {

        for (Region r : statechart.getRegions()) {
            for (Vertex v : r.getVertices()) {
                if (v instanceof State) {
                    fillStateFigures(editPart, v, diagramCell);
                }
            }
        }
    }

    private static void fillStateFigures(DiagramEditPart editPart, Vertex v, StateChartDiagram diagramCell) {
        if (v instanceof State) {
            // Utils.log(v.getName());
            EditPart part = editPart.findEditPart(editPart.getRoot(), v);
            IFigure figure = part instanceof GraphicalEditPart ? ((GraphicalEditPart) part).getFigure() : null;

            StateChartState stateCell = null;
            if (figure != null) {
                stateCell = new StateChartState();
                org.eclipse.draw2d.geometry.Rectangle bounds = figure.getBounds().getCopy();
                figure.translateToAbsolute(bounds);
                stateCell.x = bounds.x;
                stateCell.y = bounds.y;
                stateCell.width = bounds.width - 4;
                stateCell.height = bounds.height - 4;
                stateCell.setName(getElementName(v));
                diagramCell.addChild(stateCell);

                for (Region r : ((State) v).getRegions())
                    for (Vertex v2 : r.getVertices())
                        if (v2 instanceof State) {
                            fillStateFigures(editPart, v2, diagramCell);
                            stateCell.compound = true;
                        }
            }
        }
    }

    private static class DiagramImageGenerator extends DiagramGenerator {

        public DiagramImageGenerator(DiagramEditPart dgrmEP) {
            super(dgrmEP);
        }

        PainterGraphics pg;

        public byte[] getBytes() {
            return pg.getBytes();
        }

        protected Graphics setUpGraphics(int width, int height) {

            pg = new PainterGraphics(width, height) {

                @Override
                public void fillRectangle(int x, int y, int width, int height) {
                }

                @Override
                public void fillRoundRectangle(org.eclipse.draw2d.geometry.Rectangle r, int arcWidth, int arcHeight) {
                }

                // @Override
                // public void fillArc(int x, int y, int width, int height, int offset, int length) {
                //
                // }

                @Override
                public void fillGradient(int x, int y, int w, int h, boolean vertical) {

                }

                // @Override
                // public void fillOval(int x, int y, int width, int height) {
                //
                // }

                @Override
                public void fillPath(Path path) {

                }

                // @Override
                // public void fillPolygon(int[] points) {
                // }
                //
                // @Override
                // public void fillPolygon(PointList points) {
                // }

            };
            pg.setAdvanced(true);
            pg.setAntialias(SWT.ON);

            return pg;
        }

        @Override
        protected ImageDescriptor getImageDescriptor(Graphics arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static String getElementName(NamedElement element) {

        Object v = element;
        String name = null;
        while (v instanceof Region || v instanceof Vertex || v instanceof State) {
            String elementName = ((NamedElement) v).getName();
            if (!Utils.isEmpty(elementName)) {
                elementName = elementName.replaceAll("[[^a-z]&&[^A-Z]&&[^0-9]]", "_");
            }
            // empty name
            else {
                if (v instanceof Region)
                    elementName = "_region" + (((Region) v).getComposite() != null ? ((Region) v).getComposite().getRegions().indexOf(v):0);
                else if (v instanceof FinalState)
                    elementName = "_final_" ;
                else 
                    elementName = "" ;
            }

            // combine
            name = name == null ? elementName : elementName + "." + name;

            // parent
            v = (v instanceof Vertex) ? ((Vertex) v).getParentRegion() : (v instanceof Region) ? (NamedElement) ((Region) v).getComposite() : null;
        }
        return !Utils.isEmpty(name) ? name : "unknown";
    }

    // ========================================================================================================================
    // Access Diagram
    // ========================================================================================================================

    public static StateChartDiagram getDiagram(Link link) {
        ICell chart = link != null ? link.resolveCell(ImpulsePreferences.chartPreferences) : null;
        if (chart instanceof StateChart)
            return ((StateChart) chart).getDiagram((String) null);
        return null;
    }

    StateChartDiagram getDiagram(String override) {
        if (!Utils.isEmpty(override)) {
            return (StateChartDiagram) getChildByName(override, StateChartDiagram.class);
        }
        if (!Utils.isEmpty(diagram)) {
            return (StateChartDiagram) getChildByName(diagram, StateChartDiagram.class);
        }
        return null;
    }

    // ========================================================================================================================
    // Create Plan
    // ========================================================================================================================

    @Override
    public void plan(ISinglePaintPlanner<IPlotItem> planner, IProgress progress, IPaintPlanGenerator generator, IPropertyModel parameters, IDomainAxis axis,
            Rectangle childArea, boolean extend) {

        String diagramName = parameters != null ? parameters.getTyped("diagram", String.class) : null;
        boolean scaleToWidthHeight = parameters != null ? parameters.getTyped("scaleToWidthHeight", Boolean.class) : true;
        int horizontalAlign = parameters != null ? parameters.getTyped("horizontalAlign", Integer.class) : ALLIGN_CENTER;
        int verticalAlign = parameters != null ? parameters.getTyped("verticalAlign", Integer.class) : ALLIGN_MIDDLE;
        boolean adaptColors = parameters != null ? parameters.getTyped("adaptColors", Boolean.class) : true;

        StateChartDiagram diagram = getDiagram(diagramName);

        if (diagram != null) {

            if (!extend) {
                generator.setScheme(IPlan.SCHEME_APPLICABALE_AREA_MATCH | IPlan.SCHEME_APPLICABALE_SAMPLES_MATCH | IPlan.SCHEME_APPLICABALE_PAINTSTYLE_MATCH
                        | IPlan.SCHEME_APPLICABALE_ACTIVECURSOR_MATCH);
                // Utils.log("creates birt",image);
                if (diagram.image != null) {
                    generator.add(IPaint.PAINT_CHART, null,
                            new short[] { (short) (scaleToWidthHeight ? IPaint.PAINT_PAR_SCALE_KEEPASPECT : IPaint.PAINT_PAR_SCALE_NONE),
                                    (short) horizontalAlign, (short) verticalAlign, (short) (adaptColors ? 1 : 0) },
                            0, diagram.image);
                    for (StateChartState state : diagram.getTypedChildren(StateChartState.class)) {
                        generator.add(IPaint.PAINT_ANNOTATION, null, new short[] { (short) (state.x - diagram.x), (short) (state.y - diagram.y),
                                (short) state.width, state.compound ? (short) -state.height : (short) state.height }, 0, state.getName());
                    }
                    generator.setScript(Scripting.initScript(StateChart.class, "paint.js"));

                }
            }
            // status
            if (planner.getItem() != null) {
                IPlotItem item = planner.getItem();

                // readable
                IReadableSamples samples = item.getSamples();

                // cursor
                ICursorItem cursor = item.getPlotTree() != null ? item.getPlotTree().getActiveCursor() : null;

                // value at cursor
                if (samples != null && cursor != null) {
                    // Utils.log(cursor,cursor.getPosition());
                    int index = samples.indexAt(cursor.getPosition());
                    String state = null;
                    while (index >= 0 && state == null) {
                        CompoundValue compound = samples.compoundAt(index);
                        if (compound.noOfMembers() > 0) {
                            List<Object> members = compound.membersWithContent(ISamples.CONTENT_STATE);
                            for (Object m : members)
                                state = compound.formatOf(members.get(0), ISample.FORMAT_TEXT);
                        } else
                            state = samples.formatAt(index,ISample.FORMAT_TEXT);
                        index--;
                    }
                    // Utils.log(index,state);
                    if (state != null)
                        generator.setStatus(state);
                }

            }
        }
    }

    @Override
    public int getPriority() {
        return IPaintPlanner.PRIORITY_MID;
    }
    // ========================================================================================================================
    // Synchronization listener
    // ========================================================================================================================

    static {
        ImpulseNotifications.addListener(new RecordViewerListener() {

            @Override
            public void aboutToSetInput(IRecordViewer viewer, IEditorInput input) {
                syncDiagrams();
            }

            @Override
            public void aboutToRefresh(IRecordViewer viewer) {
                syncDiagrams();
            }

            private void syncDiagrams() {
                for (ICell cell : ImpulsePreferences.chartPreferences.getCell().getChildren(StateChart.class)) {
                    if (((StateChart) cell).synchronize && ((StateChart) cell).mode != MODE_CONTENT)
                        syncDiagrams((StateChart) cell);
                }
            }

            private void syncDiagrams(final StateChart stateChart) {

                if (stateChart == null || !stateChart.isBound())
                    return;

                StateChartLoader loader = stateChart.loadChart(false, true);
                if (loader == null || Utils.equals(loader.getBytes(), stateChart.content))
                    return;
                loader.parse();

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

                // check loader
                if (loader != null) {

                    // modifier
                    List<IElementModifier> modifiers = new ArrayList<IElementModifier>();

                    // remove first
                    if (stateChart.hasChildren())
                        modifiers.add(ElementHierarchyModifier.removeAll(stateChart.getElement()));

                    // create
                    List<StateChartDiagram> diagrams = StateChart.createDiagrams(loader, status);

                    // add new diagrams
                    for (StateChartDiagram diagram : diagrams) {
                        modifiers.add(ElementHierarchyModifier.add(stateChart.getElement(), Elements.getElements(diagram), null));
                        status.done(1.0 / loader.getDiagramNames().size());
                    }

                    // change content
                    modifiers.add(
                            new ElementFieldModifier(stateChart.getElement(), "content", ElementFieldModifier.MODIFICATION_REPLACE, loader.getBytes(), false));
                    for (IElementModifier m : modifiers) {
                        m.doIt(null);
                    }
                }
                progressMessage[0].close();
                progressMessage[0] = null;
            }

        });
    }
}