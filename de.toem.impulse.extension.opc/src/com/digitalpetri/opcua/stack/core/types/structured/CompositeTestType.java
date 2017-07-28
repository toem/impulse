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

@UaDataType("CompositeTestType")
public class CompositeTestType implements UaStructure {

    public static final NodeId TypeId = Identifiers.CompositeTestType;
    public static final NodeId BinaryEncodingId = Identifiers.CompositeTestType_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.CompositeTestType_Encoding_DefaultXml;

    protected final ScalarTestType _field1;
    protected final ArrayTestType _field2;

    public CompositeTestType() {
        this._field1 = null;
        this._field2 = null;
    }

    public CompositeTestType(ScalarTestType _field1, ArrayTestType _field2) {
        this._field1 = _field1;
        this._field2 = _field2;
    }

    public ScalarTestType getField1() {
        return _field1;
    }

    public ArrayTestType getField2() {
        return _field2;
    }

    @Override
    public NodeId getTypeId() {
        return TypeId;
    }

    @Override
    public NodeId getBinaryEncodingId() {
        return BinaryEncodingId;
    }

    @Override
    public NodeId getXmlEncodingId() {
        return XmlEncodingId;
    }


    public static void encode(CompositeTestType compositeTestType, UaEncoder encoder) {
        encoder.encodeSerializable("Field1", compositeTestType._field1 != null ? compositeTestType._field1 : new ScalarTestType());
        encoder.encodeSerializable("Field2", compositeTestType._field2 != null ? compositeTestType._field2 : new ArrayTestType());
    }

    public static CompositeTestType decode(UaDecoder decoder) {
        ScalarTestType _field1 = decoder.decodeSerializable("Field1", ScalarTestType.class);
        ArrayTestType _field2 = decoder.decodeSerializable("Field2", ArrayTestType.class);

        return new CompositeTestType(_field1, _field2);
    }

    static {
        DelegateRegistry.registerEncoder(CompositeTestType::encode, CompositeTestType.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(CompositeTestType::decode, CompositeTestType.class, BinaryEncodingId, XmlEncodingId);
    }

}
