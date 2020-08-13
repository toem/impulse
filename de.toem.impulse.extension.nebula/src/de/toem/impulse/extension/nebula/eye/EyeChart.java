package de.toem.impulse.extension.nebula.eye;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
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
import de.toem.impulse.domain.DomainBase;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.extension.nebula.AbstractNebulaChartCell;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.ITheme;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.scripting.Scripting;
import de.toem.impulse.values.CompoundValue;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.properties.PropertyModel.Property;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = EyeChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class EyeChart extends AbstractNebulaChartCell  {
    public static final String TYPE = "chart.nebula.eye";

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


    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("offset")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Offset:");
                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("rate")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Rate:");
                        
                        tlk().addText(container(), new TextController(editor(), new PropertySource("cycles")) {

                        }, cols(), DialogToolkit.LABEL | TLK.BORDER, "Cycles:");

                        return true;
                    }
                };

            }
        }.add(new Property("offset", "0.05ms", "Offset","Define an offset at which the eye chart starts (e.g. '1ms')")).add(new Property("rate", "2us", "Rate","Define the rate at which the eye chart is triggered (e.g. ' 10ms')")).add("cycles", 100, "Cycles", "Define the (max) number of cycles");
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
        xyGraph.getPrimaryXAxis().setAutoScale(false);
        xyGraph.getPrimaryXAxis().setTitle("");
        xyGraph.getPrimaryXAxis().setShowMajorGrid(showGrid);
        xyGraph.getPrimaryYAxis().setTitleFont(font);
        xyGraph.getPrimaryYAxis().setAutoScale(true);
        xyGraph.getPrimaryYAxis().setTitle("");
        xyGraph.getPrimaryYAxis().setShowMajorGrid(showGrid);

        if (!manualFill) {
            CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);

            String name = "";
            if (plannable != null) {
                IReadableSamples readable = plannable.getSamples();
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
                        rate = readable.getDomainBase().parseUnits(parameters.get("rate"));
                        for (IDomainBase t : DomainBase.ALL)
                            if (parameters.get("rate").toLowerCase().trim().endsWith(t.toString().toLowerCase())) {
                                diagramBase = t;
                                break;
                            }
                        factor = readable.getDomainBase().getQuantum().doubleValue() / diagramBase.getQuantum().doubleValue();
                        offset = readable.getDomainBase().parseUnits(parameters.get("offset"));
                        cycles = Utils.parseInt(parameters.get("cycles"), -1); 
                    } catch (Throwable e) {

                    }
                    xyGraph.getPrimaryXAxis().setRange((-rate / 2) * factor, (rate / 2) * factor);
                    CompoundValue prev =null;
                    for (int n = 0; n < length; n++) {
                        
                        CompoundValue value = readable.compoundAt(n);
                        long units = value.getUnits();
                        if (units < offset)
                            continue;

                        
                        long munits = rate != 0 ?  (units - offset) % rate : 0;
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
