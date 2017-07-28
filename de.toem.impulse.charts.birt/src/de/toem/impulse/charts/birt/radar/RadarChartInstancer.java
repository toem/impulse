//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.birt.radar;

import de.toem.impulse.charts.birt.AbstractBirtChartInstancer;

public class RadarChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return RadarChart.TYPE;
    }

}
