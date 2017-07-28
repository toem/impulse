package de.toem.impulse.charts.nebula.intensity;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.nebula.visualization.widgets.figures.IntensityGraphFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.console.MessageConsoleStream;

import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.charts.nebula.AbstractNebulaChartCell;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.toolkits.text.MultilineText;

@CellAnnotation(type = IntensityChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class IntensityChart extends AbstractNebulaChartCell {
    public static final String TYPE = "chart.nebula.intensity";
    private static final String SCIPT_DEFINITIONS = "if (typeof load == 'function'){load(\"nashorn:mozilla_compat.js\");}importPackage(Packages.de.toem.impulse.samples.iterator);"
            + "importPackage(Packages.de.toem.impulse.values);" + "importPackage(Packages.de.toem.impulse.samples);" + "" + "" + "" + "" + "" + "";

    
    public boolean showLegend = true;
    public String script;
    
    @Override
    public Figure createChart(GC gc, int x, int y, int width, int height, Color color, Color background, IPropertyModel parameters) {
        

        // Create Intensity Graph
        final IntensityGraphFigure intensityGraph = new IntensityGraphFigure();

        // axes
        intensityGraph.getXAxis().setTitleFont(gc.getFont());
        intensityGraph.getXAxis().setAutoScale(true);
        intensityGraph.getXAxis().setTitle("");
        intensityGraph.getYAxis().setTitleFont(gc.getFont());
        intensityGraph.getYAxis().setAutoScale(true);
        intensityGraph.getYAxis().setTitle("");        
        intensityGraph.setShowRamp(showLegend);


        
        intensityGraph.setColorMap(new ColorMap(PredefinedColorMap.JET, true, true));

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
            engine.put("graph", intensityGraph);
            
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

        


        return intensityGraph;
    }
}
