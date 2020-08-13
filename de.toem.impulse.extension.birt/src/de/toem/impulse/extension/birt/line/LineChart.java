package de.toem.impulse.extension.birt.line;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;

import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.extension.birt.AbstractBirtChartCell;
import de.toem.impulse.paint.ITheme;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = LineChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class LineChart extends AbstractBirtChartCell {
    public static final String TYPE = "chart.birt.line";

    public Chart createBirtChart(ITheme theme) {

        ChartWithAxesImpl chart = (ChartWithAxesImpl) ChartWithAxesImpl.create();
        initDefaults(chart,LegendItemType.SERIES_LITERAL,theme);

     // X-Axis
        Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
        
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getTitle().setVisible(false);
        xAxisPrimary.getLineAttributes().setColor(scaleColor.copyInstance());
        applyFont(xAxisPrimary.getLabel().getCaption(),false,theme);
                
        // Y-Axis
        Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
        yAxisPrimary.getLabel().getCaption().getFont().setRotation(90);
        applyFont(yAxisPrimary.getLabel().getCaption(),false,theme);
        yAxisPrimary.getTitle().setVisible(false);
        yAxisPrimary.setType( log10 ? AxisType.LOGARITHMIC_LITERAL:AxisType.LINEAR_LITERAL);
        yAxisPrimary.getLineAttributes().setColor(scaleColor.copyInstance());
        
     // X-Series
        SeriesDefinition sdCat = SeriesDefinitionImpl.create();
        sdCat.getSeriesPalette().shift(0);
        xAxisPrimary.getSeriesDefinitions().add(sdCat);

        Series series = SeriesImpl.create();
        series.setDataSet(dataSets.get("CATEGORIES"));
        sdCat.getSeries().add(series);
            
        // Y-Series
        SeriesDefinition sd = SeriesDefinitionImpl.create();
        yAxisPrimary.getSeriesDefinitions().add(sd);      
        
        for (int s = 1; s < dataSets.keySet().size(); s++) {

            String seriesLabel = (String) dataSets.keySet().toArray()[s];                    
            LineSeries seLine = (LineSeries) LineSeriesImpl.create();
            seLine.setDataSet(dataSets.get(seriesLabel));
            seLine.setSeriesIdentifier(seriesLabel);
            sd.getSeries().add(seLine);
        }
        return chart;
    }
}

