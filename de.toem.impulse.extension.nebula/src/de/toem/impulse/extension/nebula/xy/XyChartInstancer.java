//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.nebula.xy;

import java.io.InputStream;

import de.toem.basics.core.Utils;
import de.toem.impulse.extension.nebula.AbstractNebulaChartInstancer;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.toolkits.text.MultilineText;

public class XyChartInstancer extends AbstractNebulaChartInstancer {

    @Override
    public String getCellType() {
        return XyChart.TYPE;
    }

    @Override
    protected void initOne(String id, ICell cell, IElement preferences) {

        try {
            InputStream script = XyChart.class.getResourceAsStream("script.js");
            if (script != null) {
                String raw = Utils.readStringFromInputStream(script, "UTF-8", true);
                if (raw != null)
                    ((XyChart) cell).script = MultilineText.toXml(raw.trim());
                script.close();
            }
        } catch (Throwable e) {
        }
    }

}
