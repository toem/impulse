//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.examples.scope;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.extension.examples.ExtensionToolkit;
import de.toem.pattern.information.IInformationGroup;

public class ExampleScopeAdapterInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return ExampleScopeAdapter.TYPE;
	}
    @Override
    protected IInformationGroup getDefaultGroup() {
        return ExtensionToolkit.EXTENSION_GROUP;
    }
}
