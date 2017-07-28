//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.extension.opc.Opc;
import de.toem.pattern.information.IInformationGroup;

public class OpcUaAdapterInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return OpcUaAdapter.TYPE;
	}
    @Override
    protected IInformationGroup getDefaultGroup() {
        return Opc.EXTENSION_GROUP;
    }
}
