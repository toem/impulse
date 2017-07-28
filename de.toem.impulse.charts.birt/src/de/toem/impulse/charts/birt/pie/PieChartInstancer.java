//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.birt.pie;

import de.toem.impulse.charts.birt.AbstractBirtChartInstancer;

public class PieChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return PieChart.TYPE;
    }
}
