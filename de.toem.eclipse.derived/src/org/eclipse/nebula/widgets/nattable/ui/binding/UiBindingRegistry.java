/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.binding;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class UiBindingRegistry implements IUiBindingRegistry {

    private static final Log log = LogFactory.getLog(UiBindingRegistry.class);

    private NatTable natTable;

    private LinkedList<KeyBinding> keyBindings = new LinkedList<KeyBinding>();

    private Map<MouseEventTypeEnum, LinkedList<MouseBinding>> mouseBindingsMap = new HashMap<MouseEventTypeEnum, LinkedList<MouseBinding>>();

    private LinkedList<DragBinding> dragBindings = new LinkedList<DragBinding>();

    public UiBindingRegistry(NatTable natTable) {
        this.natTable = natTable;
    }

    // Lookup /////////////////////////////////////////////////////////////////

    @Override
    public IKeyAction getKeyEventAction(KeyEvent event) {
        for (KeyBinding keyBinding : this.keyBindings) {
            if (keyBinding.getKeyEventMatcher().matches(event)) {
                return keyBinding.getAction();
            }
        }
        return null;
    }

    @Override
    public IDragMode getDragMode(MouseEvent event) {
        LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x, event.y);

        for (DragBinding dragBinding : this.dragBindings) {
            if (dragBinding.getMouseEventMatcher().matches(this.natTable, event, regionLabels)) {
                return dragBinding.getDragMode();
            }
        }

        return null;
    }

    @Override
    public IMouseAction getMouseMoveAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_MOVE, event);
    }

    @Override
    public IMouseAction getMouseDownAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_DOWN, event);
    }

    @Override
    public IMouseAction getSingleClickAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_SINGLE_CLICK, event);
    }

    @Override
    public IMouseAction getDoubleClickAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, event);
    }

    @Override
    public IMouseAction getMouseHoverAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_HOVER, event);
    }

    @Override
    public IMouseAction getMouseEnterAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_ENTER, event);
    }

    @Override
    public IMouseAction getMouseExitAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_EXIT, event);
    }

    // /////////////////////////////////////////////////////////////////////////

    private IMouseAction getMouseEventAction(MouseEventTypeEnum mouseEventType, MouseEvent event) {

        // TODO: This code can be made more performant by mapping mouse bindings
        // not only to the mouseEventType but
        // also to the region that they are interested in. That way, given an
        // area and an event we can narrow down the
        // list of mouse bindings that need to be searched. -- Azubuko.Obele

        try {
            LinkedList<MouseBinding> mouseEventBindings = this.mouseBindingsMap.get(mouseEventType);
            if (mouseEventBindings != null) {
                LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x, event.y);

                for (MouseBinding mouseBinding : mouseEventBindings) {

                    if (mouseBinding.getMouseEventMatcher().matches(this.natTable, event, regionLabels)) {
                        return mouseBinding.getAction();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception on retrieving a mouse event action", e); //$NON-NLS-1$
        }
        return null;
    }

    // Registration ///////////////////////////////////////////////////////////

    // Key

    public void registerFirstKeyBinding(IKeyEventMatcher keyMatcher, IKeyAction action) {
        this.keyBindings.addFirst(new KeyBinding(keyMatcher, action));
    }

    public void registerKeyBinding(IKeyEventMatcher keyMatcher, IKeyAction action) {
        this.keyBindings.addLast(new KeyBinding(keyMatcher, action));
    }

    public void unregisterKeyBinding(IKeyEventMatcher keyMatcher) {
        for (KeyBinding keyBinding : this.keyBindings) {
            if (keyBinding.getKeyEventMatcher().equals(keyMatcher)) {
                this.keyBindings.remove(keyBinding);
                return;
            }
        }
    }

    // Drag

    public void registerFirstMouseDragMode(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
        this.dragBindings.addFirst(new DragBinding(mouseEventMatcher, dragMode));
    }

    public void registerMouseDragMode(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
        this.dragBindings.addLast(new DragBinding(mouseEventMatcher, dragMode));
    }

    public void unregisterMouseDragMode(IMouseEventMatcher mouseEventMatcher) {
        for (DragBinding dragBinding : this.dragBindings) {
            if (dragBinding.getMouseEventMatcher().equals(mouseEventMatcher)) {
                this.dragBindings.remove(dragBinding);
                return;
            }
        }
    }

    // Mouse move

    public void registerFirstMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    public void registerMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    public void unregisterMouseMoveBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher);
    }

    // Mouse down

    public void registerFirstMouseDownBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher, action);
    }

    public void registerMouseDownBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher, action);
    }

    public void unregisterMouseDownBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher);
    }

    // Single click

    public void registerFirstSingleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher, action);
    }

    public void registerSingleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher, action);
    }

    public void unregisterSingleClickBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher);
    }

    // Double click

    public void registerFirstDoubleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher, action);
    }

    public void registerDoubleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher, action);
    }

    public void unregisterDoubleClickBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher);
    }

    // Mouse hover

    public void registerFirstMouseHoverBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher, action);
    }

    public void registerMouseHoverBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher, action);
    }

    public void unregisterMouseHoverBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher);
    }

    // Mouse enter

    public void registerFirstMouseEnterBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher, action);
    }

    public void registerMouseEnterBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher, action);
    }

    public void unregisterMouseEnterBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher);
    }

    // Mouse exit

    public void registerFirstMouseExitBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher, action);
    }

    public void registerMouseExitBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher, action);
    }

    public void unregisterMouseExitBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher);
    }

    // /////////////////////////////////////////////////////////////////////////

    private void registerMouseBinding(boolean first, MouseEventTypeEnum mouseEventType, IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        LinkedList<MouseBinding> mouseEventBindings = this.mouseBindingsMap.get(mouseEventType);
        if (mouseEventBindings == null) {
            mouseEventBindings = new LinkedList<MouseBinding>();
            this.mouseBindingsMap.put(mouseEventType, mouseEventBindings);
        }
        if (first) {
            mouseEventBindings.addFirst(new MouseBinding(mouseEventMatcher, action));
        } else {
            mouseEventBindings.addLast(new MouseBinding(mouseEventMatcher, action));
        }
    }

    private void unregisterMouseBinding(MouseEventTypeEnum mouseEventType, IMouseEventMatcher mouseEventMatcher) {
        LinkedList<MouseBinding> mouseBindings = this.mouseBindingsMap.get(mouseEventType);
        if (mouseBindings != null) {
            for (MouseBinding mouseBinding : mouseBindings) {
                if (mouseBinding.getMouseEventMatcher().equals(mouseEventMatcher)) {
                    mouseBindings.remove(mouseBinding);
                    return;
                }
            }
        }
    }

    private enum MouseEventTypeEnum {
        MOUSE_DOWN, MOUSE_MOVE, MOUSE_SINGLE_CLICK, MOUSE_DOUBLE_CLICK, MOUSE_HOVER, MOUSE_ENTER, MOUSE_EXIT
    }

}
