package de.toem.impulse.charts.nebula;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.pattern.information.IInformationGroup;

public class AbstractNebulaChartInstancer extends AbstractDefaultInstancer {

    @Override
    protected IInformationGroup getDefaultGroup() {
        return ImpulseNebulaCharts.EXTENSION_GROUP;
    }

}
