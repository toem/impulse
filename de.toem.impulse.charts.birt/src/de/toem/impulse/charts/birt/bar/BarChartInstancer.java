//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.birt.bar;

import de.toem.impulse.charts.birt.AbstractBirtChartInstancer;

public class BarChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return BarChart.TYPE;
    }

}
