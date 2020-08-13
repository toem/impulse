/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types;

import java.util.Map;

import org.eclipse.milo.opcua.stack.core.serialization.codecs.DataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;

public interface DataTypeDictionary<T extends DataTypeCodec> {

    /**
     * @return the namespace URI this {@link DataTypeDictionary} belongs to.
     */
    String getNamespaceUri();

    /**
     * @return the name of the datatype encoding for codecs in this dictionary.
     */
    QualifiedName getEncodingName();

    /**
     * Register a {@link DataTypeCodec} that serializes an enumeration with this dictionary.
     *
     * @param codec       the codec to register.
     * @param description the value of the DataTypeDescription Node that identifies {@code codec} in the dictionary.
     */
    void registerEnumCodec(T codec, String description);

    /**
     * Register a {@link DataTypeCodec} that serializes a structure with this dictionary.
     *
     * @param codec       the codec to register.
     * @param description the value of the DataTypeDescription Node that identifies {@code codec} in the dictionary.
     * @param dataTypeId  the {@link NodeId} of the DataType Node for the DataType serialized by {@code codec}.
     * @param encodingId  the {@link NodeId} of the appropriate DataTypeEncoding Node for the DataType serialized
     *                    by {@code codec}.
     */
    void registerStructCodec(T codec, String description, NodeId dataTypeId, NodeId encodingId);

    /**
     * Get a {@link DataTypeCodec} registered with this dictionary.
     *
     * @param description the value of the DataTypeDescription that identifies the codec in the dictionary.
     * @return a {@link DataTypeCodec} for {@code description}, or {@code null} if none is found.
     */
    T getCodec(String description);

    /**
     * Get a {@link DataTypeCodec} registered with this dictionary.
     *
     * @param dataTypeId the {@link NodeId} of the DataType Node for the DataType serialized by the codec.
     * @return a {@link DataTypeCodec} for {@code dataTypeId}, or {@code null} if none is found.
     */
    T getCodec(NodeId dataTypeId);

    /**
     * @return a Map of all codecs registered with this dictionary, keyed by description.
     */
    Map<String, T> getCodecsByDescription();

    /**
     * @return a Map of all codecs registered with this dictionary, keyed by encoding id.
     */
    Map<NodeId, T> getCodecsByEncodingId();

    /**
     * @return a Map of all codecs registered with this dictionary, keyed by datatype id.
     */
    Map<NodeId, T> getCodecsByDataTypeId();

}
