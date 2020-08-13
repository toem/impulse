/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.structured;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class EventNotificationList extends NotificationData {

    public static final NodeId TypeId = Identifiers.EventNotificationList;
    public static final NodeId BinaryEncodingId = Identifiers.EventNotificationList_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.EventNotificationList_Encoding_DefaultXml;

    protected final EventFieldList[] events;

    public EventNotificationList() {
        super();
        this.events = null;
    }

    public EventNotificationList(EventFieldList[] events) {
        super();
        this.events = events;
    }

    @Nullable
    public EventFieldList[] getEvents() { return events; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Events", events)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<EventNotificationList> {

        @Override
        public Class<EventNotificationList> getType() {
            return EventNotificationList.class;
        }

        @Override
        public EventNotificationList decode(UaDecoder decoder) throws UaSerializationException {
            EventFieldList[] events =
                decoder.readBuiltinStructArray(
                    "Events",
                    EventFieldList.class
                );

            return new EventNotificationList(events);
        }

        @Override
        public void encode(EventNotificationList value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeBuiltinStructArray(
                "Events",
                value.events,
                EventFieldList.class
            );
        }
    }

}
