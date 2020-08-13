/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter2;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * ILayerPainter implementation that is rendering the background of the space
 * that is available for the NatTable instance. It uses the Color that is
 * configured via
 * {@link org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)}
 * . It then calls the ILayerPainter of the underlying layers in the layer stack
 * and calls all registered IOverlayPainter at the end, to render the overlays
 * correctly.
 */
public class NatLayerPainter implements ILayerPainter {

    private static final Log log = LogFactory.getLog(NatLayerPainter.class);

    private final NatTable natTable;

    public NatLayerPainter(NatTable natTable) {
        this.natTable = natTable;
    }

    @Override
    public void paintLayer(
            ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle rectangle,
            IConfigRegistry configRegistry) {

        try {
            paintBackground(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);

            gc.setForeground(this.natTable.getForeground());

            ILayerPainter layerPainter = this.natTable.getLayer().getLayerPainter();
            layerPainter.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);

            paintOverlays(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
        } catch (Exception e) {
            log.error("Error while painting table", e); //$NON-NLS-1$
        }
    }

    protected void paintBackground(ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle rectangle,
            IConfigRegistry configRegistry) {

        gc.setBackground(this.natTable.getBackground());

        // Clean Background
        gc.fillRectangle(rectangle);
    }

    protected void paintOverlays(ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle rectangle,
            IConfigRegistry configRegistry) {

        for (IOverlayPainter overlayPainter : this.natTable.getOverlayPainters()) {
            if (overlayPainter instanceof IOverlayPainter2) {
                ((IOverlayPainter2) overlayPainter).paintOverlay(this.natTable, gc, xOffset, yOffset, rectangle);
            } else {
                overlayPainter.paintOverlay(gc, this.natTable);
            }
        }
    }

    @Override
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds) {
        ILayerPainter layerPainter = this.natTable.getLayer().getLayerPainter();
        return layerPainter.adjustCellBounds(columnPosition, rowPosition, cellBounds);
    }

}
