//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.jdt.watchpoint;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;

public class WatchpointTracerInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return WatchpointTracer.TYPE;
	}
}
