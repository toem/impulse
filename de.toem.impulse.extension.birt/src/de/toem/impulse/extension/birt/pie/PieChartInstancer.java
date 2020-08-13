//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.birt.pie;

import de.toem.impulse.extension.birt.AbstractBirtChartInstancer;

public class PieChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return PieChart.TYPE;
    }
}
