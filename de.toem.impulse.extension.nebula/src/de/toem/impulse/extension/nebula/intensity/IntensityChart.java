package de.toem.impulse.extension.nebula.intensity;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.nebula.visualization.widgets.figures.IntensityGraphFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.extension.nebula.AbstractNebulaChartCell;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.ITheme;
import de.toem.impulse.scripting.Scripting;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = IntensityChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class IntensityChart extends AbstractNebulaChartCell {
    public static final String TYPE = "chart.nebula.intensity";

    public boolean showLegend = true;

    @Override
    public Figure createChart(IProgress progress,GC gc, IPlotItem plannable, IDomainAxis axis, ITheme theme, IPropertyModel parameters, int x, int y, int width, int height,
            Font font, Color color, Color background) {

        // Create Intensity Graph
        final IntensityGraphFigure intensityGraph = new IntensityGraphFigure();

        // axes
        intensityGraph.getXAxis().setTitleFont(font);
        intensityGraph.getXAxis().setAutoScale(true);
        intensityGraph.getXAxis().setTitle("");
        intensityGraph.getYAxis().setTitleFont(font);
        intensityGraph.getYAxis().setAutoScale(true);
        intensityGraph.getYAxis().setTitle("");
        intensityGraph.setShowRamp(showLegend);

        intensityGraph.setColorMap(new ColorMap(PredefinedColorMap.JET, true, true));

        // scripting
        Scripting scripting = new Scripting(this, "script") {
            
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
                setSymbol("graph", intensityGraph);
            }
            
            @Override
            protected Object eval(ScriptEngine engine, boolean init, String script) throws ScriptException {
                return script != null ? engine.eval(removePsoidoCode(script)) : null;
            }
        };
        scripting.run(progress);

        return intensityGraph;
    }
}
