/*******************************************************************************
 * Copyright (c) 2006-2009 Nicolas Richeton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Nicolas Richeton (nicolas.richeton@gmail.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.nebula.cwt.animation.effects;

import org.eclipse.nebula.cwt.animation.AnimationRunner;
import org.eclipse.nebula.cwt.animation.movement.IMovement;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class Grow extends AbstractEffect {

	/**
	 * @deprecated
	 * @param w
	 * @param duration
	 * @param movement
	 * @param onStop
	 * @param onCancel
	 */
	public void grow(AnimationRunner runner, Control w, int duration,
			IMovement movement, Runnable onStop, Runnable onCancel) {
		IEffect effect = new Grow(w, w.getBounds(), new Rectangle(w
				.getBounds().x + 10, w.getBounds().y + 10,
				w.getBounds().width + 10, w.getBounds().height + 10), duration,
				movement, onStop, onCancel);
		runner.runEffect(effect);
	}

	Rectangle src, dest, diff;

	Control control = null;

	public Grow(Control control, Rectangle src, Rectangle dest,
			long lengthMilli, IMovement movement, Runnable onStop,
			Runnable onCancel) {
		super(lengthMilli, movement, onStop, onCancel);
		this.src = src;
		this.dest = dest;
		this.control = control;
		this.diff = new Rectangle(dest.x - src.x, dest.y - src.y, dest.width
				- src.width, dest.height - src.height);

		easingFunction.init(0, 1, (int) lengthMilli);
	}

	public void applyEffect(final long currentTime) {
		if (!control.isDisposed()) {
			control.setBounds((int) (src.x - diff.x
					* easingFunction.getValue(currentTime)),
					(int) (src.y - diff.y
							* easingFunction.getValue(currentTime)),
					(int) (src.width + 2 * diff.width
							* easingFunction.getValue(currentTime)),
					(int) (src.height + 2 * diff.height
							* easingFunction.getValue(currentTime)));
		}
	}
}