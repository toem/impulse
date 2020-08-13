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
package org.eclipse.nebula.widgets.nattable.resize.event;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

public class RowResizeEventMatcher extends MouseEventMatcher {

    public RowResizeEventMatcher(int stateMask, int button) {
        this(stateMask, GridRegion.ROW_HEADER, button);
    }

    public RowResizeEventMatcher(int stateMask, String eventRegion, int button) {
        super(stateMask, eventRegion, button);
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        return super.matches(natTable, event, regionLabels)
                && indexIsResizable(natTable, event);
    }

    private boolean indexIsResizable(ILayer natLayer, MouseEvent event) {
        int rowPosition = CellEdgeDetectUtil.getRowPositionToResize(natLayer,
                new Point(event.x, event.y));
        if (rowPosition < 0) {
            return false;
        } else {
            return natLayer.isRowPositionResizable(rowPosition);
        }
    }
}
