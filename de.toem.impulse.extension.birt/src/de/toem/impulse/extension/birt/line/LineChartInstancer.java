//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.birt.line;

import de.toem.impulse.extension.birt.AbstractBirtChartInstancer;

public class LineChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return LineChart.TYPE;
    }
}
