//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.examples.analog;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.extension.examples.ExtensionToolkit;
import de.toem.pattern.information.IInformationGroup;

public class ExampleAnalogAdapterInstancer extends AbstractDefaultInstancer {

    @Override
    public String getCellType() {
        return ExampleAnalogAdapter.TYPE;
    }

    @Override
    protected IInformationGroup getDefaultGroup() {
        return ExtensionToolkit.EXTENSION_GROUP;
    }
}
