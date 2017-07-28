//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.jdt.watchpoint;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.ports.jdt.JdtPorts;
import de.toem.pattern.information.IInformationGroup;

public class WatchpointTracerInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return WatchpointTracer.TYPE;
	}
    @Override
    protected IInformationGroup getDefaultGroup() {
        return JdtPorts.EXTENSION_GROUP;
    }
}
