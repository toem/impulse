/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula.command;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Specialized {@link CopyDataCommandHandler} that stores the copied cells in
 * the {@link InternalCellClipboard} so it can be pasted within NatTable.
 *
 * @since 1.4
 */
public class FormulaCopyDataCommandHandler extends InternalCopyDataCommandHandler {

    /**
     * Creates an instance that only checks the {@link SelectionLayer} for the
     * data to add to the system clipboard and the given
     * {@link InternalCellClipboard}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public FormulaCopyDataCommandHandler(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(selectionLayer, clipboard);
    }

    @Override
    protected void preInternalCopy() {
        this.selectionLayer.doCommand(new DisableFormulaEvaluationCommand());
    }

    @Override
    protected void postInternalCopy() {
        this.selectionLayer.doCommand(new EnableFormulaEvaluationCommand());
    }
}
