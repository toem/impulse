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
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public class StatusChangeNotification extends NotificationData {

    public static final NodeId TypeId = Identifiers.StatusChangeNotification;
    public static final NodeId BinaryEncodingId = Identifiers.StatusChangeNotification_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.StatusChangeNotification_Encoding_DefaultXml;

    protected final StatusCode status;
    protected final DiagnosticInfo diagnosticInfo;

    public StatusChangeNotification() {
        super();
        this.status = null;
        this.diagnosticInfo = null;
    }

    public StatusChangeNotification(StatusCode status, DiagnosticInfo diagnosticInfo) {
        super();
        this.status = status;
        this.diagnosticInfo = diagnosticInfo;
    }

    public StatusCode getStatus() { return status; }

    public DiagnosticInfo getDiagnosticInfo() { return diagnosticInfo; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Status", status)
            .add("DiagnosticInfo", diagnosticInfo)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<StatusChangeNotification> {

        @Override
        public Class<StatusChangeNotification> getType() {
            return StatusChangeNotification.class;
        }

        @Override
        public StatusChangeNotification decode(UaDecoder decoder) throws UaSerializationException {
            StatusCode status = decoder.readStatusCode("Status");
            DiagnosticInfo diagnosticInfo = decoder.readDiagnosticInfo("DiagnosticInfo");

            return new StatusChangeNotification(status, diagnosticInfo);
        }

        @Override
        public void encode(StatusChangeNotification value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeStatusCode("Status", value.status);
            encoder.writeDiagnosticInfo("DiagnosticInfo", value.diagnosticInfo);
        }
    }

}
