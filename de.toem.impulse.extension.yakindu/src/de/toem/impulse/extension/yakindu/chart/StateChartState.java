package de.toem.impulse.extension.yakindu.chart;

import de.toem.pattern.element.Cell;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = StateChartState.TYPE, dynamicChildOf = { StateChart.TYPE })
public class StateChartState extends Cell {
    public static final String TYPE = "chart.yakindu.statechart.state";

    public int x,y,width,height;
    public boolean compound;  
}
