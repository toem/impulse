//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.charts.nebula.eye;

import java.io.InputStream;

import de.toem.basics.core.Utils;
import de.toem.impulse.charts.nebula.AbstractNebulaChartInstancer;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.toolkits.text.MultilineText;

public class EyeChartInstancer extends AbstractNebulaChartInstancer {

    @Override
    public String getCellType() {
        return EyeChart.TYPE;
    }

    @Override
    protected void initOne(ICell cell, IElement preferences) {

        try {
            InputStream script = EyeChart.class.getResourceAsStream("script.js");
            if (script != null) {
                String raw = Utils.readStringFromInputStream(script, "UTF-8", true);
                if (raw != null)
                    ((EyeChart) cell).script = MultilineText.toXml(raw.trim());
                script.close();
            }
        } catch (Throwable e) {
        }
    }

}
