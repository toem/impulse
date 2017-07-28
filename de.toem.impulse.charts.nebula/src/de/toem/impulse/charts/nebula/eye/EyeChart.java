package de.toem.impulse.charts.nebula.eye;

import java.io.IOException;

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
import de.toem.impulse.domain.DomainBase;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.values.CompoundValue;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.toolkits.text.MultilineText;

@CellAnnotation(type = EyeChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class EyeChart extends AbstractNebulaChartCell {
    public static final String TYPE = "chart.nebula.eye";
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


    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("offset")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Offset:");
                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("rate")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Rate:");
                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("cycles")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Cycles:");

                        return true;
                    }
                };

            }
        }.add("offset", "0.05ms", "Offset").add("rate", "2us", "Rate").add("cycles", 100, "Cycles");
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
        xyGraph.getPrimaryXAxis().setAutoScale(false);
        xyGraph.getPrimaryXAxis().setTitle("");
        xyGraph.getPrimaryXAxis().setShowMajorGrid(showGrid);
        xyGraph.getPrimaryYAxis().setTitleFont(gc.getFont());
        xyGraph.getPrimaryYAxis().setAutoScale(true);
        xyGraph.getPrimaryYAxis().setTitle("");
        xyGraph.getPrimaryYAxis().setShowMajorGrid(showGrid);

        if (!manualFill) {
            CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);

            String name = "";
            if (plannable != null) {
                IReadableSamples readable = plannable.getReadable();
                if (readable != null) {
                    name = readable.getName();
                    if (Utils.isEmpty(name))
                        name = "Unknown";
                    if (showAxisTitle) {
                        xyGraph.getPrimaryXAxis().setTitle("Domain");
                        xyGraph.getPrimaryYAxis().setTitle("Value");
                    }
                    int length = readable.getCount();
                    traceDataProvider.setBufferSize(length>0?length:1);
                    traceDataProvider.setChronological(true);


                    long rate = 0;
                    IDomainBase diagramBase = null;
                    double factor = 1;
                    long offset = 0;
                    int cycles=-1;
                    long prevMUnits = 0;
                    int currentCycles = 0;
                    try {
                        rate = readable.getDomainBase().parseUnits(parameters.getVal("rate"));
                        for (IDomainBase t : DomainBase.ALL)
                            if (parameters.getVal("rate").toLowerCase().trim().endsWith(t.toString().toLowerCase())) {
                                diagramBase = t;
                                break;
                            }
                        factor = readable.getDomainBase().getQuantum().doubleValue() / diagramBase.getQuantum().doubleValue();
                        offset = readable.getDomainBase().parseUnits(parameters.getVal("offset"));
                        cycles = Utils.parseInt(parameters.getVal("cycles"), -1); 
                    } catch (Throwable e) {

                    }
                    xyGraph.getPrimaryXAxis().setRange((-rate / 2) * factor, (rate / 2) * factor);
                    CompoundValue prev =null;
                    for (int n = 0; n < length; n++) {
                        
                        CompoundValue value = readable.compoundAt(n);
                        long units = value.getUnits();
                        if (units < offset)
                            continue;

                        
                        long munits = (units - offset) % rate;
                        if (munits<prevMUnits && prev != null){
                            traceDataProvider.addSample(new Sample((munits +rate - rate / 2) * factor, value.floatValue()));
                            currentCycles++;
                            if (cycles >=0 && cycles <= currentCycles)
                                break;
                            long punits = ((prev.getUnits() - offset) % rate)-rate;
                            traceDataProvider.addSample(new Sample((punits - rate / 2) * factor, prev.floatValue()));

                        }
                        traceDataProvider.addSample(new Sample((munits - rate / 2) * factor, value.floatValue()));

                        prevMUnits = munits;
                        prev = value;

                    }

                }
            }
            traceDataProvider.setChronological(true);
            // create the trace
            Trace trace = new EyeTrace(name, xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), traceDataProvider);
            trace.setForegroundColor(color);
            trace.setTraceColor(color);

            // set trace property
            if (annotated)
                trace.setPointStyle(PointStyle.XCROSS);

            // add the trace to xyGraph
            xyGraph.addTrace(trace);
            xyGraph.setShowLegend(false);
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
