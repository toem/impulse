package de.toem.impulse.extension.yakindu.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class StateChartInstancer extends AbstractDefaultInstancer {

    @Override
    public String getCellType() {
        return StateChart.TYPE;
    }

    @Override
    public List<ICell> create(String id, ICell container, IElement preferences) {
        if ("INITIAL".equals(id)) {
            return createInitalCharts();
        } else
            return super.create(id, container, preferences);
    }

    protected List<ICell> createInitalCharts() {
        byte[] data;
        List<ICell> list = new ArrayList<>();
        try {
            data = Bundles.getBundleEntryAsByteArray("de.toem.impulse.extension.yakindu", "inital.walML");
            if (data != null) {
                IElement element = Elements.getElement(data);
                if (element.isBound() && element.hasCell()) {
                    List<ICell> charts = element.getCell().getChildren(StateChart.class);
                    for (ICell chart : charts)
                        list.add(chart.clone());
                }
            }

        } catch (IOException e) {
        }
        return list;
    }
}
