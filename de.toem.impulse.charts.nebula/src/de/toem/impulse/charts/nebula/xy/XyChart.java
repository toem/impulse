package de.toem.impulse.charts.nebula.xy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.console.MessageConsoleStream;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.charts.nebula.AbstractNebulaChartCell;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.values.CompoundValue;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.toolkits.text.MultilineText;

@CellAnnotation(type = XyChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class XyChart extends AbstractNebulaChartCell {
    public static final String TYPE = "chart.nebula.xy";
    private static final String SCIPT_DEFINITIONS = "if (typeof load == 'function'){load(\"nashorn:mozilla_compat.js\");}importPackage(Packages.de.toem.impulse.samples.iterator);"
            + "importPackage(Packages.de.toem.impulse.values);" + "importPackage(Packages.de.toem.impulse.samples);" + "" + "" + "" + "" + "" + "";

    public final static String[] titleOptions = new String[] { "No title", "Description", "Config name", "Config description" };
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
    public String script;
    public int maxPoints = 1000;

    @Override
    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        tlk().addText(container(), new TextController(editor(), new PropertySource("members")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Members:");

                        tlk().addText(container(), new TextController(editor(), new PropertySource("samples")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Samples:");

                        return true;
                    }
                };

            }
        }.add("members", "", "Members").add("samples", "0-999", "Samples");
        if (configured)
            model.setValue(parameters);
        return model;
    }

    @Override
    public Figure createChart(GC gc, int x, int y, int width, int height, Color color, Color background, IPropertyModel parameters) {

        // title
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
        xyGraph.setTitleFont(gc.getFont());

        // legend
        xyGraph.setShowLegend(showLegend);

        // plot area
        xyGraph.getPlotArea().setBackgroundColor(background);

        // axes

        xyGraph.getPrimaryXAxis().setTitleFont(gc.getFont());
        xyGraph.getPrimaryXAxis().setAutoScale(true);
        xyGraph.getPrimaryXAxis().setTitle("");
        xyGraph.getPrimaryXAxis().setShowMajorGrid(showGrid);
        xyGraph.getPrimaryYAxis().setTitleFont(gc.getFont());
        xyGraph.getPrimaryYAxis().setAutoScale(true);
        xyGraph.getPrimaryYAxis().setTitle("");
        xyGraph.getPrimaryYAxis().setShowMajorGrid(showGrid);

        List<Integer> samples = new ArrayList<Integer>();
        for (String e : (parameters.getVal("samples") != null ? parameters.getVal("samples") : "").split(",")) {
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
        List<String> members = new ArrayList<String>();
        for (String m : (parameters.getVal("members") != null ? parameters.getVal("members") : "").split(",")) {
            m = m.trim();
            if (!Utils.isEmpty(m))
                members.add(m);
        }

        if (!manualFill) {
            CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
            String name = "";
            if (plannable != null) {
                IReadableSamples readable = plannable.getReadable();
                if (readable != null && (readable.getSignalType() == SignalType.FloatArray || readable.getSignalType() == SignalType.Float
                        || readable.getSignalType() == SignalType.Integer || readable.getSignalType() == SignalType.IntegerArray)) {
                    name = readable.getName();
                    if (Utils.isEmpty(name))
                        name = "Unknown";

                    // members
                    Object xMember = 0;
                    Object yMember = 1;
                    if (members.isEmpty() && (readable.getSignalType() == SignalType.Float || readable.getSignalType() == SignalType.Integer
                            || readable.getSignalDescriptor().getScale() == 1))
                        yMember = null;
                    if (!members.isEmpty()) {
                        if (members.size() >= 1)
                            xMember = members.get(0);
                        if (members.size() >= 2)
                            yMember = members.get(1);
                        else
                            yMember = null;
                    }

                    // axis title
                    if (showAxisTitle) {
                        xyGraph.getPrimaryXAxis().setTitle(String.valueOf(xMember));
                        xyGraph.getPrimaryYAxis().setTitle(yMember != null ? String.valueOf(xMember) : readable.getDomainBase().getDomainLabel());
                    }

                    // length
                    int length = Math.min(maxPoints, samples.isEmpty() ? readable.getCount() : samples.size());
                    traceDataProvider.setBufferSize(length);

                    if (yMember != null) {

                        for (int n = 0; n < length; n++) {
                            CompoundValue value = readable.compoundAt(n);
                            if (value != null)
                                traceDataProvider.addSample(new Sample(value.doubleValueOf(xMember), value.doubleValueOf(yMember)));
                        }
                    } else {
                        for (int n = 0; n < length; n++) {
                            CompoundValue value = readable.compoundAt(n);
                            if (value != null)
                                traceDataProvider.addSample(new Sample(value.getUnits(),value.doubleValueOf(xMember)));
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
        }

        if (enableScript) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            final ScriptEngine engine = mgr.getEngineByName("JavaScript");
            if (engine != null) {
                engine.put("gc", gc);
                engine.put("x", x);
                engine.put("y", y);
                engine.put("width", width);
                engine.put("height", height);
                engine.put("color", color);
                engine.put("background", background);
                engine.put("plannable", plannable);
                engine.put("axis", axis);
                engine.put("theme", theme);
                engine.put("readable", plannable != null ? plannable.getReadable() : null);
                engine.put("graph", xyGraph);

                MessageConsoleStream console = null;
                if (ImpulseBase.console != null) {
                    console = ImpulseBase.console.newMessageStream();
                    engine.put("console", console);
                }

                try {

                    engine.eval(SCIPT_DEFINITIONS + MultilineText.toAscii(script));
                } catch (ThreadDeath e) {
                    console.println("Script killed!");
                } catch (Throwable e) {
                    console.println(e.getLocalizedMessage());

                } finally {

                    try {
                        if (console != null)
                            console.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return xyGraph;
    }
}
