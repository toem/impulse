//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.birt.line;

import de.toem.impulse.charts.birt.AbstractBirtChartInstancer;

public class LineChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return LineChart.TYPE;
    }
}
