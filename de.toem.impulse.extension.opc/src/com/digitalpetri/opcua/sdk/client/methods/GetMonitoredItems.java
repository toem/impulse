/*
 * Copyright 2016 Kevin Herron
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

package com.digitalpetri.opcua.sdk.client.methods;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.api.UaClient;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.jooq.lambda.tuple.Tuple2;

public class GetMonitoredItems extends AbstractUaMethod {

    public GetMonitoredItems(UaClient client, NodeId objectId, NodeId methodId) {
        super(client, objectId, methodId);
    }

    /**
     * GetMonitoredItems is used to get information about monitored items of a subscription.
     *
     * @param subscriptionId identifier of the subscription.
     * @return a {@link Tuple2} containing the output arguments.
     * <p>
     * serverHandles (UInt32[]) - array of serverHandles for all MonitoredItems of the subscription identified by
     * subscriptionId.
     * <p>
     * clientHandles (UInt32[]) - array of clientHandles for all MonitoredItems of the subscription identified by
     * subscriptionId.
     */
    public CompletableFuture<Tuple2<UInteger[], UInteger[]>> invoke(UInteger subscriptionId) {
        Variant[] inputArguments = new Variant[]{
                new Variant(subscriptionId)
        };

        return invoke(inputArguments).thenCompose(outputArguments -> {
            try {
                UInteger[] v0 = (UInteger[]) outputArguments[0].getValue();
                UInteger[] v1 = (UInteger[]) outputArguments[1].getValue();

                return CompletableFuture.completedFuture(new Tuple2<>(v0, v1));
            } catch (Throwable t) {
                CompletableFuture<Tuple2<UInteger[], UInteger[]>> f = new CompletableFuture<>();
                f.completeExceptionally(new UaException(t));
                return f;
            }
        });
    }

}
