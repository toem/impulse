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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ConversionUtil {

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(List<T> list, Class<T> c) {
        Object array = Array.newInstance(c, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return (T[]) array;
    }

    public static <T> List<T> toList(T[] array) {
        return array != null ? Arrays.asList(array) : Collections.emptyList();
    }

    public static <T> Stream<T> toStream(T[] array) {
        return array != null ? Arrays.stream(array) : Stream.empty();
    }

    public static <T> T[] a(List<T> list, Class<T> c) {
        return toArray(list, c);
    }

    public static <T> List<T> l(T[] array) {
        return toList(array);
    }

    public static <T> Stream<T> s(T[] array) {
        return toStream(array);
    }

}
