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

package com.digitalpetri.opcua.stack.core.types.structured;

import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.serialization.DelegateRegistry;
import com.digitalpetri.opcua.stack.core.serialization.UaDecoder;
import com.digitalpetri.opcua.stack.core.serialization.UaEncoder;
import com.digitalpetri.opcua.stack.core.serialization.UaStructure;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("ComplexNumberType")
public class ComplexNumberType implements UaStructure {

    public static final NodeId TypeId = Identifiers.ComplexNumberType;
    public static final NodeId BinaryEncodingId = Identifiers.ComplexNumberType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.ComplexNumberType_Encoding_DefaultXml;

    protected final Float _real;
    protected final Float _imaginary;

    public ComplexNumberType() {
        this._real = null;
        this._imaginary = null;
    }

    public ComplexNumberType(Float _real, Float _imaginary) {
        this._real = _real;
        this._imaginary = _imaginary;
    }

    public Float getReal() { return _real; }

    public Float getImaginary() { return _imaginary; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(ComplexNumberType complexNumberType, UaEncoder encoder) {
        encoder.encodeFloat("Real", complexNumberType._real);
        encoder.encodeFloat("Imaginary", complexNumberType._imaginary);
    }

    public static ComplexNumberType decode(UaDecoder decoder) {
        Float _real = decoder.decodeFloat("Real");
        Float _imaginary = decoder.decodeFloat("Imaginary");

        return new ComplexNumberType(_real, _imaginary);
    }

    static {
        DelegateRegistry.registerEncoder(ComplexNumberType::encode, ComplexNumberType.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(ComplexNumberType::decode, ComplexNumberType.class, BinaryEncodingId, XmlEncodingId);
    }

}
