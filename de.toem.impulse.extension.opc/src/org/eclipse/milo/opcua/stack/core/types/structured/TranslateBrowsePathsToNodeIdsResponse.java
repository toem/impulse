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
import org.eclipse.milo.opcua.stack.core.serialization.UaResponseMessage;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.BuiltinDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class TranslateBrowsePathsToNodeIdsResponse implements UaResponseMessage {

    public static final NodeId TypeId = Identifiers.TranslateBrowsePathsToNodeIdsResponse;
    public static final NodeId BinaryEncodingId = Identifiers.TranslateBrowsePathsToNodeIdsResponse_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.TranslateBrowsePathsToNodeIdsResponse_Encoding_DefaultXml;

    protected final ResponseHeader responseHeader;
    protected final BrowsePathResult[] results;
    protected final DiagnosticInfo[] diagnosticInfos;

    public TranslateBrowsePathsToNodeIdsResponse() {
        this.responseHeader = null;
        this.results = null;
        this.diagnosticInfos = null;
    }

    public TranslateBrowsePathsToNodeIdsResponse(ResponseHeader responseHeader, BrowsePathResult[] results, DiagnosticInfo[] diagnosticInfos) {
        this.responseHeader = responseHeader;
        this.results = results;
        this.diagnosticInfos = diagnosticInfos;
    }

    public ResponseHeader getResponseHeader() { return responseHeader; }

    @Nullable
    public BrowsePathResult[] getResults() { return results; }

    @Nullable
    public DiagnosticInfo[] getDiagnosticInfos() { return diagnosticInfos; }

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
            .add("Results", results)
            .add("DiagnosticInfos", diagnosticInfos)
            .toString();
    }

    public static class Codec extends BuiltinDataTypeCodec<TranslateBrowsePathsToNodeIdsResponse> {

        @Override
        public Class<TranslateBrowsePathsToNodeIdsResponse> getType() {
            return TranslateBrowsePathsToNodeIdsResponse.class;
        }

        @Override
        public TranslateBrowsePathsToNodeIdsResponse decode(UaDecoder decoder) throws UaSerializationException {
            ResponseHeader responseHeader = (ResponseHeader) decoder.readBuiltinStruct("ResponseHeader", ResponseHeader.class);
            BrowsePathResult[] results =
                decoder.readBuiltinStructArray(
                    "Results",
                    BrowsePathResult.class
                );
            DiagnosticInfo[] diagnosticInfos = decoder.readArray("DiagnosticInfos", decoder::readDiagnosticInfo, DiagnosticInfo.class);

            return new TranslateBrowsePathsToNodeIdsResponse(responseHeader, results, diagnosticInfos);
        }

        @Override
        public void encode(TranslateBrowsePathsToNodeIdsResponse value, UaEncoder encoder) throws UaSerializationException {
            encoder.writeBuiltinStruct("ResponseHeader", value.responseHeader, ResponseHeader.class);
            encoder.writeBuiltinStructArray(
                "Results",
                value.results,
                BrowsePathResult.class
            );
            encoder.writeArray("DiagnosticInfos", value.diagnosticInfos, encoder::writeDiagnosticInfo);
        }
    }

}
