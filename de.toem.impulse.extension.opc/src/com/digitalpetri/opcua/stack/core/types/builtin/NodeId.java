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

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaRuntimeException;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.enumerated.IdType;
import com.google.common.base.MoreObjects;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;
import static com.google.common.base.Preconditions.checkNotNull;

public final class NodeId {

    public static final NodeId NULL_NUMERIC = new NodeId(ushort(0), uint(0));
    public static final NodeId NULL_STRING = new NodeId(ushort(0), "");
    public static final NodeId NULL_GUID = new NodeId(ushort(0), new UUID(0, 0));
    public static final NodeId NULL_OPAQUE = new NodeId(ushort(0), ByteString.NULL_VALUE);

    public static final NodeId NULL_VALUE = NULL_NUMERIC;

    private final UShort namespaceIndex;
    private final Object identifier;

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(int namespaceIndex, int identifier) {
        this(ushort(namespaceIndex), uint(identifier));
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(int namespaceIndex, String identifier) {
        this(ushort(namespaceIndex), identifier);
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(int namespaceIndex, UUID identifier) {
        this(ushort(namespaceIndex), identifier);
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(int namespaceIndex, ByteString identifier) {
        this(ushort(namespaceIndex), identifier);
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(UShort namespaceIndex, UInteger identifier) {
        checkNotNull(namespaceIndex);
        checkNotNull(identifier);

        this.namespaceIndex = namespaceIndex;
        this.identifier = identifier;
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(UShort namespaceIndex, String identifier) {
        checkNotNull(namespaceIndex);

        if (identifier == null) identifier = "";

        this.namespaceIndex = namespaceIndex;
        this.identifier = identifier;
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(UShort namespaceIndex, UUID identifier) {
        checkNotNull(namespaceIndex);
        checkNotNull(identifier);

        this.namespaceIndex = namespaceIndex;
        this.identifier = identifier;
    }

    /**
     * @param namespaceIndex the index for a namespace URI. An index of 0 is used for OPC UA defined NodeIds.
     * @param identifier     the identifier for a node in the address space of an OPC UA Server.
     */
    public NodeId(UShort namespaceIndex, ByteString identifier) {
        checkNotNull(namespaceIndex);
        checkNotNull(identifier);

        this.namespaceIndex = namespaceIndex;
        this.identifier = identifier;
    }

    public UShort getNamespaceIndex() {
        return namespaceIndex;
    }

    public Object getIdentifier() {
        return identifier;
    }

    public IdType getType() {
        if (identifier instanceof UInteger) {
            return IdType.Numeric;
        } else if (identifier instanceof String) {
            return IdType.String;
        } else if (identifier instanceof UUID) {
            return IdType.Guid;
        } else {
            return IdType.Opaque;
        }
    }

    public ExpandedNodeId expanded() {
        return new ExpandedNodeId(this);
    }

    public boolean isNull() {
        return namespaceIndex.intValue() == 0 &&
                (NULL_NUMERIC.equals(this) || NULL_STRING.equals(this) ||
                        NULL_GUID.equals(this) || NULL_OPAQUE.equals(this));
    }

    public boolean isNotNull() {
        return !isNull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeId nodeId = (NodeId) o;

        return identifier.equals(nodeId.identifier) &&
                namespaceIndex.equals(nodeId.namespaceIndex);
    }

    @Override
    public int hashCode() {
        int result = namespaceIndex.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ns", namespaceIndex)
                .add("id", identifier)
                .toString();
    }

    public String toParseableString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ns=").append(namespaceIndex).append(";");

        switch (getType()) {
            case Numeric:
                sb.append("i=").append(identifier);
                break;
            case String:
                sb.append("s=").append(identifier);
                break;
            case Guid:
                sb.append("g=").append(identifier);
                break;
            case Opaque:
                ByteString bs = (ByteString) identifier;
                if (bs.isNull()) sb.append("b=");
                else sb.append("b=").append(DatatypeConverter.printBase64Binary(bs.bytes()));
                break;
        }

        return sb.toString();
    }

    // TODO Re-write this crap... or at the very least write some good unit tests.

    private static final Pattern INT_INT = Pattern.compile("ns=(\\d*);i=(\\d*)");
    private static final Pattern NONE_INT = Pattern.compile("i=(\\d*)");

    private static final Pattern INT_STRING = Pattern.compile("ns=(\\d*);s=(.*)");
    private static final Pattern NONE_STRING = Pattern.compile("s=(.*)");

    private static final Pattern INT_GUID = Pattern.compile("ns=(\\d*);g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
    private static final Pattern NONE_GUID = Pattern.compile("g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    private static final Pattern INT_OPAQUE = Pattern.compile("ns=(\\d*);b=([0-9a-zA-Z\\+/=]*)");
    private static final Pattern NONE_OPAQUE = Pattern.compile("b=([0-9a-zA-Z\\+/=]*)");

    public static NodeId parse(@Nonnull String s) {
        Matcher m;

        m = NONE_STRING.matcher(s);
        if (m.matches()) {
            String obj = m.group(1);
            return new NodeId(ushort(0), obj);
        }

        m = NONE_INT.matcher(s);
        if (m.matches()) {
            Integer obj = Integer.valueOf(m.group(1));
            return new NodeId(ushort(0), uint(obj));
        }

        m = NONE_GUID.matcher(s);
        if (m.matches()) {
            UUID obj = UUID.fromString(m.group(1));
            return new NodeId(ushort(0), obj);
        }

        m = NONE_OPAQUE.matcher(s);
        if (m.matches()) {
            byte[] obj = DatatypeConverter.parseBase64Binary(m.group(1));
            return new NodeId(ushort(0), ByteString.of(obj));
        }

        m = INT_INT.matcher(s);
        if (m.matches()) {
            Integer nsi = Integer.valueOf(m.group(1));
            Integer obj = Integer.valueOf(m.group(2));
            return new NodeId(ushort(nsi), uint(obj));
        }

        m = INT_STRING.matcher(s);
        if (m.matches()) {
            Integer nsi = Integer.valueOf(m.group(1));
            String obj = m.group(2);
            return new NodeId(ushort(nsi), obj);
        }

        m = INT_GUID.matcher(s);
        if (m.matches()) {
            Integer nsi = Integer.valueOf(m.group(1));
            UUID obj = UUID.fromString(m.group(2));
            return new NodeId(ushort(nsi), obj);
        }

        m = INT_OPAQUE.matcher(s);
        if (m.matches()) {
            Integer nsi = Integer.valueOf(m.group(1));
            byte[] obj = DatatypeConverter.parseBase64Binary(m.group(2));
            return new NodeId(ushort(nsi), ByteString.of(obj));
        }

        throw new UaRuntimeException(StatusCodes.Bad_NodeIdInvalid);
    }

    public static Optional<NodeId> parseSafe(String s) {
        try {
            return Optional.of(parse(s));
        } catch (UaRuntimeException e) {
            return Optional.empty();
        }
    }

}
