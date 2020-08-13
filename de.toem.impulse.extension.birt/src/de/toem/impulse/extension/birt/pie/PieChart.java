package de.toem.impulse.extension.birt.pie;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

import de.toem.impulse.cells.preferences.ImpulseCharts;
import de.toem.impulse.extension.birt.AbstractBirtChartCell;
import de.toem.impulse.paint.ITheme;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = PieChart.TYPE, dynamicChildOf = { ImpulseCharts.TYPE })
public class PieChart extends AbstractBirtChartCell {
    public static final String TYPE = "chart.birt.pie";

    public Chart createBirtChart(ITheme theme) {

        ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
        initDefaults(chart, LegendItemType.CATEGORIES_LITERAL, theme);

        // Base Series
        SeriesDefinition sdCat = SeriesDefinitionImpl.create();
        chart.getSeriesDefinitions().add(sdCat);
        sdCat.getSeriesPalette().shift(0);

        Series series = SeriesImpl.create();
        series.setDataSet(dataSets.get("CATEGORIES"));
        sdCat.getSeries().add(series);

        // Orthogonal Series
        SeriesDefinition sd = SeriesDefinitionImpl.create();
        sdCat.getSeriesDefinitions().add(sd);

        for (int s = 1; s < dataSets.keySet().size(); s++) {

            String seriesLabel = (String) dataSets.keySet().toArray()[s];
            PieSeries sePie = (PieSeries) PieSeriesImpl.create();
            
            // settings
            if (inner) {
                sePie.setInnerRadius(30);
                sePie.setInnerRadiusPercent(true);
            }
            if (explode)
                sePie.setExplosion(15);

            sePie.getTitle().setVisible(dataSets.keySet().size() > 2);

            // colors & fonts
            sePie.getLabel().getCaption().setColor(textColor.copyInstance());
            sePie.getTitle().getCaption().setColor(textColor.copyInstance());
            applyFont(sePie.getTitle().getCaption(), false, theme);

            // add series
            sePie.setDataSet(dataSets.get(seriesLabel));
            sePie.setSeriesIdentifier(seriesLabel);
            sd.getSeries().add(sePie);
        }
        return chart;
    }
}
