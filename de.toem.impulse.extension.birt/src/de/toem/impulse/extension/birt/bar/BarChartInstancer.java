//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.birt.bar;

import de.toem.impulse.extension.birt.AbstractBirtChartInstancer;

public class BarChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return BarChart.TYPE;
    }

}
