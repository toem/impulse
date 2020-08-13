/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.action.HoverStylingAction;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

/**
 * UI bindings for applying and clearing styles when moving the mouse over
 * NatTable cells. Is registered together with the HoverLayer that is used to
 * add the hover styling functionality.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 */
public class BodyHoverStylingBindings extends AbstractUiBindingConfiguration {

    /**
     * The HoverLayer that is used to add hover styling.
     */
    private final HoverLayer layer;

    /**
     * @param layer
     *            The HoverLayer that is used to add hover styling.
     */
    public BodyHoverStylingBindings(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // apply a hover styling on moving the mouse over a NatTable
        uiBindingRegistry.registerFirstMouseMoveBinding(
                new IMouseEventMatcher() {
                    @Override
                    public boolean matches(NatTable natTable, MouseEvent event,
                            LabelStack regionLabels) {
                        return BodyHoverStylingBindings.this.layer.getClientAreaProvider().getClientArea()
                                .contains(event.x, event.y);
                    }

                }, new HoverStylingAction(this.layer));

        // clear any hover styling if the mouse is moved out of the region area
        // uiBindingRegistry.registerMouseMoveBinding(
        // new IMouseEventMatcher() {
        // @Override
        // public boolean matches(NatTable natTable, MouseEvent event,
        // LabelStack regionLabels) {
        // return
        // (!layer.getClientAreaProvider().getClientArea().contains(event.x,
        // event.y));
        // }
        //
        // }, new ClearHoverStylingAction());

        // clear any hover styling if the mouse is moved out of a NatTable
        // region
        uiBindingRegistry.registerMouseMoveBinding(new IMouseEventMatcher() {
            @Override
            public boolean matches(NatTable natTable, MouseEvent event,
                    LabelStack regionLabels) {
                return (natTable != null && regionLabels == null);
            }

        }, new ClearHoverStylingAction());

        // clear any hover styling if the mouse is moved out of the NatTable
        // area
        uiBindingRegistry.registerMouseExitBinding(new IMouseEventMatcher() {
            @Override
            public boolean matches(NatTable natTable, MouseEvent event,
                    LabelStack regionLabels) {
                // always return true because this matcher is only asked in case
                // the mouse
                // exits the NatTable client area, therefore further checks are
                // not necessary
                return true;
            }

        }, new ClearHoverStylingAction());
    }

}
