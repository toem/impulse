//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.ports.highspeed;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.pattern.information.IInformationGroup;

public class ExampleHighspeedAdapterInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return ExampleHighspeedAdapter.TYPE;
	}
}
