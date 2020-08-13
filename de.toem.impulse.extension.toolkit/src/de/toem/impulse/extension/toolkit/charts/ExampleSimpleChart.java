package de.toem.impulse.extension.toolkit.charts;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import de.toem.eclipse.toolkits.tlk.IPaintable;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;
import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.ITheme;

import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.properties.IPropertyModel;

@CellAnnotation(type = ExampleSimpleChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class ExampleSimpleChart extends AbstractChartCell {
    public static final String TYPE = "chart.example.simple";

    @Override
    public IPaintable createChart(IPlotItem plannable, IDomainAxis axis, ITheme theme, IPropertyModel parameters) {

        return new IPaintable() {

            @Override
            public void paint(ITlkPainter painter, int x, int y, int width, int height, Object color,Object background) {
                painter.setBackground(color);
                painter.fillArc(x, y, width, height, 0, 270);
            };

        };
    }

}
