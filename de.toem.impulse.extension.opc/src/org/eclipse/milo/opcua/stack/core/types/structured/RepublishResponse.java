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

import com.google.common.base.MoreObjects;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaResponseMessage;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class RepublishResponse implements UaResponseMessage {

    public static final NodeId TypeId = Identifiers.RepublishResponse;
    public static final NodeId BinaryEncodingId = Identifiers.RepublishResponse_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.RepublishResponse_Encoding_DefaultXml;

    protected final ResponseHeader responseHeader;
    protected final NotificationMessage notificationMessage;

    public RepublishResponse() {
        this.responseHeader = null;
        this.notificationMessage = null;
    }

    public RepublishResponse(ResponseHeader responseHeader, NotificationMessage notificationMessage) {
        this.responseHeader = responseHeader;
        this.notificationMessage = notificationMessage;
    }

    public ResponseHeader getResponseHeader() { return responseHeader; }

    public NotificationMessage getNotificationMessage() { return notificationMessage; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ResponseHeader", responseHeader)
            .add("NotificationMessage", notificationMessage)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<RepublishResponse> {

        @Override
        public Class<RepublishResponse> getType() {
            return RepublishResponse.class;
        }

        @Override
        public RepublishResponse decode(UaDecoder decoder) throws UaSerializationException {
            ResponseHeader responseHeader = (ResponseHeader) decoder.readBuiltinStruct("ResponseHeader", ResponseHeader.class);
            NotificationMessage notificationMessage = (NotificationMessage) decoder.readBuiltinStruct("NotificationMessage", NotificationMessage.class);

            return new RepublishResponse(responseHeader, notificationMessage);
        }

        @Override
        public void encode(RepublishResponse value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeBuiltinStruct("ResponseHeader", value.responseHeader, ResponseHeader.class);
            encoder.writeBuiltinStruct("NotificationMessage", value.notificationMessage, NotificationMessage.class);
        }
    }

}
