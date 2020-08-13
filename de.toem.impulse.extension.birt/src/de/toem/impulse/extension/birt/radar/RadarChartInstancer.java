//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.birt.radar;

import de.toem.impulse.extension.birt.AbstractBirtChartInstancer;

public class RadarChartInstancer extends AbstractBirtChartInstancer {

    @Override
    public String getCellType() {
        return RadarChart.TYPE;
    }

}
