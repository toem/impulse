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

import javax.annotation.Nullable;

import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public final class DataValue {

    private final Variant value;
    private final StatusCode status;
    private final DateTime sourceTime;
    private final UShort sourcePicoseconds;
    private final DateTime serverTime;
    private final UShort serverPicoseconds;

    public DataValue(long statusCode) {
        this(new StatusCode(statusCode));
    }

    public DataValue(StatusCode statusCode) {
        this(Variant.NULL_VALUE, statusCode, DateTime.MIN_VALUE);
    }

    public DataValue(Variant value) {
        this(value, StatusCode.GOOD);
    }

    public DataValue(Variant value, StatusCode status) {
        this(value, status, DateTime.now());
    }

    public DataValue(Variant value, StatusCode status, @Nullable DateTime time) {
        this(value, status, time, time);
    }

    public DataValue(Variant value, StatusCode status, @Nullable DateTime sourceTime, @Nullable DateTime serverTime) {
        this(value, status, sourceTime, null, serverTime, null);
    }

    public DataValue(Variant value,
                     StatusCode status,
                     @Nullable DateTime sourceTime,
                     @Nullable UShort sourcePicoseconds,
                     @Nullable DateTime serverTime,
                     @Nullable UShort serverPicoseconds) {

        this.value = value;
        this.status = status;
        this.sourceTime = sourceTime;
        this.sourcePicoseconds = sourcePicoseconds;
        this.serverTime = serverTime;
        this.serverPicoseconds = serverPicoseconds;
    }

    public Variant getValue() {
        return value;
    }

    public StatusCode getStatusCode() {
        return status;
    }

    @Nullable
    public DateTime getSourceTime() {
        return sourceTime;
    }

    @Nullable
    public UShort getSourcePicoseconds() {
        return sourcePicoseconds;
    }

    @Nullable
    public DateTime getServerTime() {
        return serverTime;
    }

    @Nullable
    public UShort getServerPicoseconds() {
        return serverPicoseconds;
    }

    public DataValue withStatus(StatusCode status) {
        return new DataValue(value, status, sourceTime, serverTime);
    }

    public DataValue withSourceTime(@Nullable DateTime sourceTime) {
        return new DataValue(value, status, sourceTime, serverTime);
    }

    public DataValue withServerTime(@Nullable DateTime serverTime) {
        return new DataValue(value, status, sourceTime, serverTime);
    }

    @Override
    public String toString() {
        ToStringHelper helper = MoreObjects.toStringHelper(this);
        helper.add("value", value);
        helper.add("status", status);
        if (sourceTime != null) {
            helper.add("sourceTime", sourceTime);
        }
        if (serverTime != null) {
            helper.add("serverTime", serverTime);
        }

        return helper.toString();
    }

    /**
     * Derive a new {@link DataValue} from a given {@link DataValue}.
     *
     * @param from       the {@link DataValue} to derive from.
     * @param timestamps the timestamps to return in the derived value.
     * @return a derived {@link DataValue}.
     */
    public static DataValue derivedValue(DataValue from, TimestampsToReturn timestamps) {
        boolean includeSource = timestamps == TimestampsToReturn.Source || timestamps == TimestampsToReturn.Both;
        boolean includeServer = timestamps == TimestampsToReturn.Server || timestamps == TimestampsToReturn.Both;

        return new DataValue(
                from.value,
                from.status,
                includeSource ? from.sourceTime : null,
                includeServer ? from.serverTime : null
        );
    }

    /**
     * Derive a new {@link DataValue} from a given {@link DataValue}.
     * <p>
     * The value is assumed to be for a non-value Node attribute, and therefore the source timestamp is not returned.
     *
     * @param from       the {@link DataValue} to derive from.
     * @param timestamps the timestamps to return in the derived value.
     * @return a derived {@link DataValue}.
     */
    public static DataValue derivedNonValue(DataValue from, TimestampsToReturn timestamps) {
        boolean includeServer = timestamps == TimestampsToReturn.Server || timestamps == TimestampsToReturn.Both;

        return new DataValue(
                from.value,
                from.status,
                null,
                includeServer ? from.serverTime : null
        );
    }

}
