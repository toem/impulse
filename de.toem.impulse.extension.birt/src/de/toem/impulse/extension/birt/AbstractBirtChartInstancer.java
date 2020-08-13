package de.toem.impulse.extension.birt;

import java.io.InputStream;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.information.IInformationGroup;
import de.toem.toolkits.text.MultilineText;

public class AbstractBirtChartInstancer extends AbstractDefaultInstancer {

    @Override
    protected IInformationGroup getDefaultGroup() {
        return ImpulseBirtExtension.EXTENSION_GROUP;
    }

    @Override
    protected void initOne(String id, ICell cell, IElement preferences) {
        if (cell instanceof AbstractBirtChartCell) {
            try {
                InputStream script = AbstractBirtChartInstancer.class.getResourceAsStream("script.js");
                if (script != null) {
                    String raw = Utils.readStringFromInputStream(script, "UTF-8", true);
                    if (raw != null)
                        ((AbstractBirtChartCell) cell).script = MultilineText.toXml(raw.trim());
                    script.close();
                }
            } catch (Throwable e) {
            }
        }
    }

}
