//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.charts;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.pattern.information.IInformationGroup;

public class ExampleSimpleChartInstancer extends AbstractDefaultInstancer {

    @Override
    public String getCellType() {
        return ExampleSimpleChart.TYPE;
    }
}
