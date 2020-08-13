package de.toem.impulse.extension.nebula;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.pattern.information.IInformationGroup;

public class AbstractNebulaChartInstancer extends AbstractDefaultInstancer {

    @Override
    protected IInformationGroup getDefaultGroup() {
        return ImpulseNebulaExtension.EXTENSION_GROUP;
    }

}
