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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;

public class ReorderColumnGroupStartCommand extends ColumnReorderStartCommand {

    public ReorderColumnGroupStartCommand(ILayer layer, int fromColumnPosition) {
        super(layer, fromColumnPosition);
    }

    public ReorderColumnGroupStartCommand(ReorderColumnGroupStartCommand command) {
        super(command);
    }

    @Override
    public ReorderColumnGroupStartCommand cloneCommand() {
        return new ReorderColumnGroupStartCommand(this);
    }

}
