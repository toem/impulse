package de.toem.impulse.charts.birt.radar;

import org.eclipse.birt.chart.examples.radar.model.type.RadarSeries;
import org.eclipse.birt.chart.examples.radar.model.type.impl.RadarSeriesImpl;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;

import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.charts.birt.AbstractBirtChartCell;
import de.toem.impulse.paint.ITheme;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = RadarChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class RadarChart extends AbstractBirtChartCell {
    public static final String TYPE = "chart.birt.radar";

    public Chart createBirtChart(ITheme theme) {

        ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
        initDefaults(chart,(dataSets.size()>2)?LegendItemType.SERIES_LITERAL:LegendItemType.CATEGORIES_LITERAL,theme);
        chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);

        // Base Series
        SeriesDefinition sdCat = SeriesDefinitionImpl.create();
        chart.getSeriesDefinitions().add(sdCat);
        sdCat.getSeriesPalette().shift(-2);
        
        
        Series series = SeriesImpl.create();
        series.setDataSet(dataSets.get("CATEGORIES"));
        sdCat.getSeries().add(series);
        
        // Orthogonal Series
        SeriesDefinition sd = SeriesDefinitionImpl.create();
        sdCat.getSeriesDefinitions().add(sd);
        
        for (int s = 1; s < dataSets.keySet().size(); s++) {

            String seriesLabel = (String) dataSets.keySet().toArray()[s];                    
            RadarSeries seRadar = (RadarSeries) RadarSeriesImpl.create();
            seRadar.setStacked(stacked);
            seRadar.setDataSet(dataSets.get(seriesLabel));
            seRadar.setSeriesIdentifier(seriesLabel);
            sd.getSeries().add(seRadar);
            seRadar.getWebLineAttributes().setColor(scaleColor.copyInstance());          
        }
        return chart;
    } 
}
