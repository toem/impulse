package de.toem.impulse.extension.nebula.xy;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation.CursorLineStyle;
import org.eclipse.nebula.visualization.xygraph.figures.CrossAnnotation;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.extension.nebula.AbstractNebulaChartCell;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.ITheme;
import de.toem.impulse.samples.IMemberDescriptor;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.scripting.Scripting;
import de.toem.impulse.values.CompoundValue;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = XyChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class XyChart extends AbstractNebulaChartCell {
    public static final String TYPE = "chart.nebula.xy";
    private static final String SCIPT_DEFINITIONS = "if (typeof load == 'function'){load(\"nashorn:mozilla_compat.js\");}importPackage(Packages.de.toem.impulse.samples.iterator);"
            + "importPackage(Packages.de.toem.impulse.values);" + "importPackage(Packages.de.toem.impulse.samples);" + "" + "" + "" + "" + "" + "";

    public final static String[] titleOptions = new String[] { "No title", "Description", "Plot name", "Plot description" };
    public final static int TITLE_NO_TITLE = 0;
    public final static int TITLE_DESCRIPTION = 1;
    public final static int TITLE_CONFIG_NAME = 2;
    public final static int TITLE_CONFIG_DESCRIPTION = 3;
    public int showTitle = TITLE_CONFIG_DESCRIPTION;

    public boolean showLegend = true;
    public boolean showAxisTitle = false;
    public boolean showGrid = false;
    public boolean enableScript = false;
    public boolean manualFill = false;
    public boolean annotated = false;
    public int maxPoints = 1000;

    @Override
    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        tlk().addText(container(), new TextController(editor(), new PropertySource("members")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Members:");

                        tlk().addText(container(), new TextController(editor(), new PropertySource("samples")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Samples:");

                        return true;
                    }
                };

            }
        }.add("members", "", "Members", null, "Identify the structure or array elements you want to display.\n Use a comma-separated list of all members, e.g.'X,Y'.").add("samples", "0-999", "Samples", null, "Identify the samples you want to display. Use a comma-separated list of all sample indices, for example, '0,1,5-8'");
        if (configured)
            model.setTotal(parameters);
        return model;
    }

    @Override
    public Figure createChart(IProgress progress,GC gc,IPlotItem plannable, IDomainAxis axis, ITheme theme, IPropertyModel parameters, int x, int y, int width, int height, Font font, Color color, Color background) {

        // title
        String title = null;
        if (showTitle == TITLE_DESCRIPTION)
            title = description;
        else if (showTitle == TITLE_CONFIG_NAME)
            title = plannable != null ? plannable.getText() : "";
        else if (showTitle == TITLE_CONFIG_DESCRIPTION)
            title = plannable != null ? plannable.getDescription() : "";
        else
            title = null;

        // create a new XY Graph.
        XYGraph xyGraph = new XYGraph();
        xyGraph.setTransparent(true);

        // title
        xyGraph.setShowTitle(title != null);
        xyGraph.setTitle(title != null ? title : "");
        xyGraph.setTitleFont(font);
        xyGraph.setTitleColor((Color)theme.getColor(ITheme.COLOR_PLOT_TEXT));
        
        // legend
        xyGraph.setShowLegend(showLegend);

        // plot area
        xyGraph.getPlotArea().setBackgroundColor(background);

        // axes
        xyGraph.getPrimaryXAxis().setTitleFont(font);
        xyGraph.getPrimaryXAxis().setAutoScale(true);
        xyGraph.getPrimaryXAxis().setTitle("");
        xyGraph.getPrimaryXAxis().setShowMajorGrid(showGrid);
        xyGraph.getPrimaryYAxis().setTitleFont(font);
        xyGraph.getPrimaryYAxis().setAutoScale(true);
        xyGraph.getPrimaryYAxis().setTitle("");
        xyGraph.getPrimaryYAxis().setShowMajorGrid(showGrid);

        // samples
        List<Integer> samples = new ArrayList<Integer>();
        for (String e : (parameters.get("samples") != null ? parameters.get("samples") : "").split(",")) {
            e = e.trim();
            if (!Utils.isEmpty(e)) {
                int idx = e.indexOf("-");
                if (idx >= 0) {
                    int v1 = idx > 0 ? Utils.parseInt(e.substring(0, idx), -1) : 0;
                    int v2 = idx < e.length() - 1 ? Utils.parseInt(e.substring(idx + 1), -1) : maxPoints;
                    if (v1 >= 0 && v2 >= 0)
                        for (int v = v1; v <= v2; v++)
                            samples.add(v);
                } else {
                    int v = Utils.parseInt(e, -1);
                    if (v >= 0)
                        samples.add(v);
                }
            }
        }

        // members
        List<String> members = new ArrayList<String>();
        for (String m : (parameters.get("members") != null ? parameters.get("members") : "").split(",")) {
            m = m.trim();
            if (!Utils.isEmpty(m))
                members.add(m);
        }

        // cursor
        long cursorUnits = Long.MIN_VALUE;
        Sample cursorSample = null;

        // fill sample data
        if (!manualFill) {
            CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
            String name = "";
            if (plannable != null) {
                IReadableSamples readable = plannable.getSamples();
                if (readable != null ) {
                    name = readable.getName();
                    if (Utils.isEmpty(name))
                        name = "Unknown";

                    // members
                    Object xMember = 0;
                    Object yMember = 1;
                    if (!members.isEmpty()) {
                        if (members.size() >= 1)
                            xMember = members.get(0);
                        if (members.size() >= 2)
                            yMember = members.get(1);
                        else
                            yMember = null;
                    } else {
                        IMemberDescriptor member = readable.getMemberDescriptor(0);
                        if (member != null)
                            xMember = member.getName();
                        member = yMember != null ? readable.getMemberDescriptor(1) : null;
                        if (member != null)
                            yMember = member.getName();
                    }
                    if (readable.getSignalType().isSimple())
                        xMember = yMember = null;
                    if (readable.getSignalType().isArray() && readable.getSignalDescriptor().getScale() < 2)
                        yMember = null;
                    
                    // axis title
                    if (showAxisTitle) {
                        xyGraph.getPrimaryXAxis().setTitle(String.valueOf(xMember));
                        xyGraph.getPrimaryYAxis().setTitle(yMember != null ? String.valueOf(yMember) : readable.getDomainBase().getDomainLabel());
                    }

//                    // cursor
//                    if (this.viewContext != null) {
//                        DomainValue position = null;//viewContext.getPosition();
//                        position = position != null ? position.convertTo(readable.getDomainBase()):null;
//                        cursorUnits = position != null ? position.units:cursorUnits;
//                    }

                    // length
                    int length = Math.min(maxPoints, samples.isEmpty() ? readable.getCount() : samples.size());
                    traceDataProvider.setBufferSize(length);

                    // samples
                    Sample sample = null;
                    if (xMember != null && yMember != null) {
                        for (int n = 0; n < length; n++) {
                            CompoundValue value = samples.isEmpty() ?readable.compoundAt(n):readable.compoundAt(samples.get(n));
                            if (value != null) {
                                if (cursorSample == null && value.getUnits()  > cursorUnits && cursorUnits > Long.MIN_VALUE)
                                    cursorSample = sample;
                                traceDataProvider.addSample(sample = new Sample(value.doubleValueOf(xMember), value.doubleValueOf(yMember)));
                            }
                        }
                    } else {
                        for (int n = 0; n < length; n++) {
                            CompoundValue value = samples.isEmpty() ?readable.compoundAt(n):readable.compoundAt(samples.get(n));
                            if (value != null) {
                                if (cursorSample == null && value.getUnits()  > cursorUnits && cursorUnits > Long.MIN_VALUE)
                                    cursorSample = sample;
                                traceDataProvider.addSample(sample = new Sample(value.getUnits(),  xMember != null ? value.doubleValueOf(xMember):value.doubleValue()));
                            }
                        }
                    }
                }
            }
            // create the trace
            Trace trace = new Trace(name, xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), traceDataProvider);
            trace.setForegroundColor(color);
            trace.setTraceColor(color);

            // set trace property
            if (annotated)
                trace.setPointStyle(PointStyle.XCROSS);

            // add the trace to xyGraph
            xyGraph.addTrace(trace);

            // Set the value as an X
            if (cursorSample != null) {
                Annotation lAnnotation = new CrossAnnotation("Point1", xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis());
                lAnnotation.setValues(cursorSample.getXValue(),cursorSample.getYValue());
                lAnnotation.setEnabled(false); // the annotation can be moved on the graph
                lAnnotation.setShowPosition(false);
                lAnnotation.setShowName(false);
                lAnnotation.setShowSampleInfo(true);
                lAnnotation.setCurrentSnappedSample(cursorSample, annotated);
//                Utils.log(cursorSample,cursorUnits);
                lAnnotation.setCursorLineStyle(CursorLineStyle.NONE);
               // lAnnotation.setAnnotationColor(cursor.getColor());
                xyGraph.addAnnotation(lAnnotation);
            }
        }

        // scripting
        if (enableScript) {
            Scripting scripting = new Scripting(this,"script") {
                
                @Override
                public void init() {
                    setSymbol("gc", gc);
                    setSymbol("x", x);
                    setSymbol("y", y);
                    setSymbol("width", width);
                    setSymbol("height", height);
                    setSymbol("color", color);
                    setSymbol("background", background);
                    setSymbol("plannable", plannable);
                    setSymbol("axis", axis);
                    setSymbol("theme", theme);
                    setSymbol("readable", plannable != null ? plannable.getSamples() : null);
                    setSymbol("graph", xyGraph);
                }
                
                @Override
                protected Object eval(ScriptEngine engine, boolean init, String script) throws ScriptException {
                    return script != null ? engine.eval(removePsoidoCode(script)) : null;
                }
            };
            scripting.run(progress);
        }

        return xyGraph;
    }
}
