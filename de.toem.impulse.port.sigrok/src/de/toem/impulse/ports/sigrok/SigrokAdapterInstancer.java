//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.sigrok;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.pattern.information.IInformationGroup;

public class SigrokAdapterInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return SigrokAdapter.TYPE;
	}

    @Override
    protected IInformationGroup getDefaultGroup() {
        return SigrokPort.EXTENSION_GROUP;
    }
}
