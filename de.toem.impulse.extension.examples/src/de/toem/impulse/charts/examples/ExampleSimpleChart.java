package de.toem.impulse.charts.examples;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.paint.IPaintable;
import de.toem.impulse.paint.IPlannable;
import de.toem.impulse.paint.ITheme;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;

@CellAnnotation(type = ExampleSimpleChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class ExampleSimpleChart extends AbstractChartCell {
    public static final String TYPE = "chart.example.simple";

    @Override
    public IPaintable createChart(IPlannable plannable, IDomainAxis axis, ITheme theme, IPropertyModel parameters) {

        return new IPaintable() {

            @Override
            public void paint(GC gc, int x, int y, int width, int height, Color color,Color background) {
                gc.setBackground(color);
                gc.fillArc(x, y, width, height, 0, 270);
            };

        };
    }

}
