package de.toem.impulse.extension.yakindu.chart;

import de.toem.pattern.element.Cell;
import de.toem.pattern.element.CellAnnotation;

@CellAnnotation(type = StateChartDiagram.TYPE, dynamicChildOf = { StateChart.TYPE })
public class StateChartDiagram extends Cell {
    public static final String TYPE = "chart.yakindu.statechart.diagram";

    public byte[] image;
    public int x,y,width,height;

}