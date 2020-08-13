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
package org.eclipse.nebula.widgets.nattable.command;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class LayerCommandUtil {

    public static PositionCoordinate convertPositionToTargetContext(
            PositionCoordinate positionCoordinate, ILayer targetLayer) {
        ILayer layer = positionCoordinate.getLayer();

        if (layer == targetLayer) {
            return positionCoordinate;
        }

        int columnPosition = positionCoordinate.getColumnPosition();
        int underlyingColumnPosition = layer
                .localToUnderlyingColumnPosition(columnPosition);
        if (underlyingColumnPosition < 0) {
            return null;
        }

        int rowPosition = positionCoordinate.getRowPosition();
        int underlyingRowPosition = layer
                .localToUnderlyingRowPosition(rowPosition);
        if (underlyingRowPosition < 0) {
            return null;
        }

        ILayer underlyingLayer = layer.getUnderlyingLayerByPosition(
                columnPosition, rowPosition);
        if (underlyingLayer == null) {
            return null;
        }

        return convertPositionToTargetContext(new PositionCoordinate(
                underlyingLayer, underlyingColumnPosition,
                underlyingRowPosition), targetLayer);
    }

    public static ColumnPositionCoordinate convertColumnPositionToTargetContext(
            ColumnPositionCoordinate columnPositionCoordinate,
            ILayer targetLayer) {
        if (columnPositionCoordinate != null) {
            ILayer layer = columnPositionCoordinate.getLayer();

            if (layer == targetLayer) {
                return columnPositionCoordinate;
            }

            int columnPosition = columnPositionCoordinate.getColumnPosition();
            int underlyingColumnPosition = layer
                    .localToUnderlyingColumnPosition(columnPosition);
            if (underlyingColumnPosition < 0) {
                return null;
            }

            Collection<ILayer> underlyingLayers = layer
                    .getUnderlyingLayersByColumnPosition(columnPosition);
            if (underlyingLayers != null) {
                for (ILayer underlyingLayer : underlyingLayers) {
                    if (underlyingLayer != null) {
                        ColumnPositionCoordinate convertedColumnPositionCoordinate = convertColumnPositionToTargetContext(
                                new ColumnPositionCoordinate(underlyingLayer,
                                        underlyingColumnPosition), targetLayer);
                        if (convertedColumnPositionCoordinate != null) {
                            return convertedColumnPositionCoordinate;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static RowPositionCoordinate convertRowPositionToTargetContext(
            RowPositionCoordinate rowPositionCoordinate, ILayer targetLayer) {
        if (rowPositionCoordinate != null) {
            ILayer layer = rowPositionCoordinate.getLayer();

            if (layer == targetLayer) {
                return rowPositionCoordinate;
            }

            int rowPosition = rowPositionCoordinate.getRowPosition();
            int underlyingRowPosition = layer
                    .localToUnderlyingRowPosition(rowPosition);
            if (underlyingRowPosition < 0) {
                return null;
            }

            Collection<ILayer> underlyingLayers = layer
                    .getUnderlyingLayersByRowPosition(rowPosition);
            if (underlyingLayers != null) {
                for (ILayer underlyingLayer : underlyingLayers) {
                    if (underlyingLayer != null) {
                        RowPositionCoordinate convertedRowPositionCoordinate = convertRowPositionToTargetContext(
                                new RowPositionCoordinate(underlyingLayer,
                                        underlyingRowPosition), targetLayer);
                        if (convertedRowPositionCoordinate != null) {
                            return convertedRowPositionCoordinate;
                        }
                    }
                }
            }
        }
        return null;
    }

}
