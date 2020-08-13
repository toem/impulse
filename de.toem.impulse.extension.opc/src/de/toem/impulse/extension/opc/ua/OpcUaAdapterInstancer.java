//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import de.toem.eclipse.toolkits.instancer.AbstractDefaultInstancer;
import de.toem.impulse.cells.ports.ResourceAdapter;
import de.toem.impulse.scripting.Scripting;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;

public class OpcUaAdapterInstancer extends AbstractDefaultInstancer {

	@Override
	public String getCellType() {
		return OpcUaAdapter.TYPE;
	}

    @Override
    protected void initOne(String id, ICell cell, IElement preferences) {
        cell.setValue("syncScript", Scripting.initScript(ResourceAdapter.class, "sync.js"));
        cell.setValue("stimulationScript", Scripting.initScript(OpcUaAdapter.class, "stimulate.js"));
    }
}
