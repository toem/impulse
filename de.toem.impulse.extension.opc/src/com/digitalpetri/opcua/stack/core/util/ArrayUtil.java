/*
 * Copyright 2015 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.opcua.stack.core.util;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

public class ArrayUtil {

    /**
     * Flatten a multi-dimensional array into a one-dimensional array.
     *
     * @param array the array to flatten.
     * @return a 1-dimensional array.
     */
    public static Object flatten(Object array) {
        Preconditions.checkArgument(array.getClass().isArray(), "array");

        Class<?> type = getType(array);
        int[] dimensions = getDimensions(array);
        int length = length(dimensions);

        Object flattened = Array.newInstance(type, length);

        flatten(array, flattened, dimensions, 0);

        return flattened;
    }

    private static void flatten(Object array, Object flattened, int[] dimensions, int offset) {
        if (dimensions.length == 1) {
            for (int i = 0; i < dimensions[0]; i++) {
                Object element = Array.get(array, i);
                Array.set(flattened, i + offset, element);
            }
        } else {
            int[] tail = Arrays.copyOfRange(dimensions, 1, dimensions.length);

            for (int i = 0; i < dimensions[0]; i++) {
                Object a = Array.get(array, i);
                flatten(a, flattened, tail, offset + i * length(tail));
            }
        }
    }

    /**
     * Un-flatten a one-dimensional array into an multi-dimensional array based on the provided dimensions.
     *
     * @param array      the 1-dimensional array to un-flatten.
     * @param dimensions the dimensions to un-flatten to.
     * @return a multi-dimensional array of the provided dimensions.
     */
    public static Object unflatten(Object array, int[] dimensions) {
        Class<?> type = getType(array);

        return unflatten(type, array, dimensions, 0);
    }

    private static Object unflatten(Class<?> type, Object array, int[] dimensions, int offset) {
        if (dimensions.length == 1) {
            Object a = Array.newInstance(type, dimensions[0]);

            for (int i = 0; i < dimensions[0]; i++) {
                Array.set(a, i, Array.get(array, offset + i));
            }

            return a;
        } else {
            Object a = Array.newInstance(type, dimensions);

            int[] tail = Arrays.copyOfRange(dimensions, 1, dimensions.length);

            for (int i = 0; i < dimensions[0]; i++) {
                Object element = unflatten(type, array, tail, offset + i * length(tail));
                Array.set(a, i, element);
            }

            return a;
        }
    }

    public static int[] getDimensions(Object array) {
        int[] dimensions = new int[0];
        Class<?> type = array.getClass();

        while (type.isArray()) {
            int length = array != null ? Array.getLength(array) : 0;
            dimensions = Ints.concat(dimensions, new int[]{length});

            array = length > 0 ? Array.get(array, 0) : null;
            type = type.getComponentType();
        }

        return dimensions;
    }

    public static Class<?> getType(Object array) {
        Class<?> type = array.getClass();

        while (type.isArray()) {
            type = type.getComponentType();
        }

        return type;
    }


    private static int length(int[] tail) {
        int product = 1;
        for (int aTail : tail) {
            product *= aTail;
        }
        return product;
    }

}
