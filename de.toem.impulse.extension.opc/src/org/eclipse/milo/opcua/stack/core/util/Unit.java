/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.util;

/**
 * A once-inhabited alternative to {@link java.lang.Void}.
 */
public enum Unit {

    VALUE;

    public static Unit unit() {
        return VALUE;
    }

}
