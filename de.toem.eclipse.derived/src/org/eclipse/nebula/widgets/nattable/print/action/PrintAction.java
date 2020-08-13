/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.print.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.print.command.PrintCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * IKeyAction that is used to print a NatTable.
 */
public class PrintAction implements IKeyAction {

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        natTable.doCommand(new PrintCommand(natTable.getConfigRegistry(),
                natTable.getShell()));
    }

}
