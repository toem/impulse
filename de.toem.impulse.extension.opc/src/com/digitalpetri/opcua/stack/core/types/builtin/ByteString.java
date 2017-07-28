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

package com.digitalpetri.opcua.stack.core.types.builtin;

import java.util.Arrays;
import javax.annotation.Nullable;

import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned;
import com.google.common.base.MoreObjects;

public final class ByteString {

    public static final ByteString NULL_VALUE = new ByteString(null);

    private final byte[] bytes;

    public ByteString(@Nullable byte[] bytes) {
        this.bytes = bytes;
    }

    public int length() {
        return bytes != null ? bytes.length : 0;
    }

    public boolean isNull() {
        return bytes == null;
    }

    public boolean isNotNull() {
        return bytes != null;
    }

    @Nullable
    public byte[] bytes() {
        return bytes;
    }

    @Nullable
    public UByte[] uBytes() {
        if (bytes == null) return null;

        UByte[] bs = new UByte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            bs[i] = Unsigned.ubyte(bytes[i]);
        }
        return bs;
    }

    public byte byteAt(int index) {
        if (bytes == null) throw new IndexOutOfBoundsException("index=" + index);

        return bytes[index];
    }

    public UByte uByteAt(int index) {
        return Unsigned.ubyte(byteAt(index));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteString that = (ByteString) o;

        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return bytes != null ? Arrays.hashCode(bytes) : 0;
    }

    public static ByteString of(byte[] bs) {
        return new ByteString(bs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bytes", Arrays.toString(bytes))
                .toString();
    }

}
