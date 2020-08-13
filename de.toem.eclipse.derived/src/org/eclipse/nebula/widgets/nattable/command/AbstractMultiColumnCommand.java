/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractMultiColumnCommand implements ILayerCommand {

    protected Collection<ColumnPositionCoordinate> columnPositionCoordinates;

    protected AbstractMultiColumnCommand(ILayer layer, int... columnPositions) {
        setColumnPositions(layer, columnPositions);
    }

    protected AbstractMultiColumnCommand(AbstractMultiColumnCommand command) {
        this.columnPositionCoordinates = new HashSet<ColumnPositionCoordinate>(
                command.columnPositionCoordinates);
    }

    public Collection<Integer> getColumnPositions() {
        Collection<Integer> columnPositions = new HashSet<Integer>();
        for (ColumnPositionCoordinate columnPositionCoordinate : this.columnPositionCoordinates) {
            columnPositions.add(Integer
                    .valueOf(columnPositionCoordinate.columnPosition));
        }
        return columnPositions;
    }

    protected final void setColumnPositions(ILayer layer,
            int... columnPositions) {
        this.columnPositionCoordinates = new HashSet<ColumnPositionCoordinate>();
        for (int columnPosition : columnPositions) {
            this.columnPositionCoordinates.add(new ColumnPositionCoordinate(layer,
                    columnPosition));
        }
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Collection<ColumnPositionCoordinate> convertedColumnPositionCoordinates = new HashSet<ColumnPositionCoordinate>();

        for (ColumnPositionCoordinate columnPositionCoordinate : this.columnPositionCoordinates) {
            ColumnPositionCoordinate convertedColumnPositionCoordinate = LayerCommandUtil
                    .convertColumnPositionToTargetContext(
                            columnPositionCoordinate, targetLayer);
            if (convertedColumnPositionCoordinate != null) {
                convertedColumnPositionCoordinates
                        .add(convertedColumnPositionCoordinate);
            }
        }

        if (convertedColumnPositionCoordinates.size() > 0) {
            this.columnPositionCoordinates = convertedColumnPositionCoordinates;
            return true;
        } else {
            return false;
        }
    }

}
