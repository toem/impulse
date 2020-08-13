/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.api.nodes;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface ReferenceTypeNode extends Node {

    /**
     * Get the IsAbstract attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readIsAbstract()}.
     *
     * @return the IsAbstract attribute.
     */
    CompletableFuture<Boolean> getIsAbstract();

    /**
     * Get the Symmetric attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readSymmetric()}.
     *
     * @return the Symmetric attribute.
     */
    CompletableFuture<Boolean> getSymmetric();

    /**
     * Get the InverseName attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readInverseName()}.
     *
     * @return the InverseName attribute.
     */
    CompletableFuture<LocalizedText> getInverseName();

    /**
     * Set the IsAbstract attribute.
     *
     * @param isAbstract the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> setIsAbstract(boolean isAbstract);

    /**
     * Set the Symmetric attribute.
     *
     * @param symmetric the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> setSymmetric(boolean symmetric);

    /**
     * Set the InverseName attribute.
     *
     * @param inverseName the {@link LocalizedText} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> setInverseName(LocalizedText inverseName);

    /**
     * Read the IsAbstract {@link DataValue}.
     *
     * @return the IsAbstract {@link DataValue}.
     */
    CompletableFuture<DataValue> readIsAbstract();

    /**
     * Read the Symmetric attribute {@link DataValue}.
     *
     * @return the Symmetric attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readSymmetric();

    /**
     * Read the Symmetric attribute {@link DataValue}.
     *
     * @return the Symmetric attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readInverseName();

    /**
     * Write a {@link DataValue} to the IsAbstract attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeIsAbstract(DataValue value);

    /**
     * Write a {@link DataValue} to the Symmetric attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeSymmetric(DataValue value);

    /**
     * Write a {@link DataValue} to the InverseName attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeInverseName(DataValue value);

}
