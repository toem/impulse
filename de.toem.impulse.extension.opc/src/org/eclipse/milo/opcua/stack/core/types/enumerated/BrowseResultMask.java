/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.enumerated;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEnumeration;

public enum BrowseResultMask implements UaEnumeration {

    None(0),
    ReferenceTypeId(1),
    IsForward(2),
    NodeClass(4),
    BrowseName(8),
    DisplayName(16),
    TypeDefinition(32),
    All(63),
    ReferenceTypeInfo(3),
    TargetInfo(60);

    private final int value;

    BrowseResultMask(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    private static final ImmutableMap<Integer, BrowseResultMask> VALUES;

    static {
        Builder<Integer, BrowseResultMask> builder = ImmutableMap.builder();
        for (BrowseResultMask e : values()) {
            builder.put(e.getValue(), e);
        }
        VALUES = builder.build();
    }

    public static BrowseResultMask from(Integer value) {
        if (value == null) return null;
        return VALUES.getOrDefault(value, null);
    }

    public static void encode(BrowseResultMask browseResultMask, UaEncoder encoder) {
        encoder.writeInt32(null, browseResultMask.getValue());
    }

    public static BrowseResultMask decode(UaDecoder decoder) {
        int value = decoder.readInt32(null);

        return VALUES.getOrDefault(value, null);
    }

}
