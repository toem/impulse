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

public enum TrustListMasks implements UaEnumeration {

    None(0),
    TrustedCertificates(1),
    TrustedCrls(2),
    IssuerCertificates(4),
    IssuerCrls(8),
    All(15);

    private final int value;

    TrustListMasks(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    private static final ImmutableMap<Integer, TrustListMasks> VALUES;

    static {
        Builder<Integer, TrustListMasks> builder = ImmutableMap.builder();
        for (TrustListMasks e : values()) {
            builder.put(e.getValue(), e);
        }
        VALUES = builder.build();
    }

    public static TrustListMasks from(Integer value) {
        if (value == null) return null;
        return VALUES.getOrDefault(value, null);
    }

    public static void encode(TrustListMasks trustListMasks, UaEncoder encoder) {
        encoder.writeInt32(null, trustListMasks.getValue());
    }

    public static TrustListMasks decode(UaDecoder decoder) {
        int value = decoder.readInt32(null);

        return VALUES.getOrDefault(value, null);
    }

}
