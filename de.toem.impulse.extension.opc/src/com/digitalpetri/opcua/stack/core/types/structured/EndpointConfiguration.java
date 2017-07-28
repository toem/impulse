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

@UaDataType("EndpointConfiguration")
public class EndpointConfiguration implements UaStructure {

    public static final NodeId TypeId = Identifiers.EndpointConfiguration;
    public static final NodeId BinaryEncodingId = Identifiers.EndpointConfiguration_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.EndpointConfiguration_Encoding_DefaultXml;

    protected final Integer _operationTimeout;
    protected final Boolean _useBinaryEncoding;
    protected final Integer _maxStringLength;
    protected final Integer _maxByteStringLength;
    protected final Integer _maxArrayLength;
    protected final Integer _maxMessageSize;
    protected final Integer _maxBufferSize;
    protected final Integer _channelLifetime;
    protected final Integer _securityTokenLifetime;

    public EndpointConfiguration() {
        this._operationTimeout = null;
        this._useBinaryEncoding = null;
        this._maxStringLength = null;
        this._maxByteStringLength = null;
        this._maxArrayLength = null;
        this._maxMessageSize = null;
        this._maxBufferSize = null;
        this._channelLifetime = null;
        this._securityTokenLifetime = null;
    }

    public EndpointConfiguration(Integer _operationTimeout, Boolean _useBinaryEncoding, Integer _maxStringLength, Integer _maxByteStringLength, Integer _maxArrayLength, Integer _maxMessageSize, Integer _maxBufferSize, Integer _channelLifetime, Integer _securityTokenLifetime) {
        this._operationTimeout = _operationTimeout;
        this._useBinaryEncoding = _useBinaryEncoding;
        this._maxStringLength = _maxStringLength;
        this._maxByteStringLength = _maxByteStringLength;
        this._maxArrayLength = _maxArrayLength;
        this._maxMessageSize = _maxMessageSize;
        this._maxBufferSize = _maxBufferSize;
        this._channelLifetime = _channelLifetime;
        this._securityTokenLifetime = _securityTokenLifetime;
    }

    public Integer getOperationTimeout() { return _operationTimeout; }

    public Boolean getUseBinaryEncoding() { return _useBinaryEncoding; }

    public Integer getMaxStringLength() { return _maxStringLength; }

    public Integer getMaxByteStringLength() { return _maxByteStringLength; }

    public Integer getMaxArrayLength() { return _maxArrayLength; }

    public Integer getMaxMessageSize() { return _maxMessageSize; }

    public Integer getMaxBufferSize() { return _maxBufferSize; }

    public Integer getChannelLifetime() { return _channelLifetime; }

    public Integer getSecurityTokenLifetime() { return _securityTokenLifetime; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(EndpointConfiguration endpointConfiguration, UaEncoder encoder) {
        encoder.encodeInt32("OperationTimeout", endpointConfiguration._operationTimeout);
        encoder.encodeBoolean("UseBinaryEncoding", endpointConfiguration._useBinaryEncoding);
        encoder.encodeInt32("MaxStringLength", endpointConfiguration._maxStringLength);
        encoder.encodeInt32("MaxByteStringLength", endpointConfiguration._maxByteStringLength);
        encoder.encodeInt32("MaxArrayLength", endpointConfiguration._maxArrayLength);
        encoder.encodeInt32("MaxMessageSize", endpointConfiguration._maxMessageSize);
        encoder.encodeInt32("MaxBufferSize", endpointConfiguration._maxBufferSize);
        encoder.encodeInt32("ChannelLifetime", endpointConfiguration._channelLifetime);
        encoder.encodeInt32("SecurityTokenLifetime", endpointConfiguration._securityTokenLifetime);
    }

    public static EndpointConfiguration decode(UaDecoder decoder) {
        Integer _operationTimeout = decoder.decodeInt32("OperationTimeout");
        Boolean _useBinaryEncoding = decoder.decodeBoolean("UseBinaryEncoding");
        Integer _maxStringLength = decoder.decodeInt32("MaxStringLength");
        Integer _maxByteStringLength = decoder.decodeInt32("MaxByteStringLength");
        Integer _maxArrayLength = decoder.decodeInt32("MaxArrayLength");
        Integer _maxMessageSize = decoder.decodeInt32("MaxMessageSize");
        Integer _maxBufferSize = decoder.decodeInt32("MaxBufferSize");
        Integer _channelLifetime = decoder.decodeInt32("ChannelLifetime");
        Integer _securityTokenLifetime = decoder.decodeInt32("SecurityTokenLifetime");

        return new EndpointConfiguration(_operationTimeout, _useBinaryEncoding, _maxStringLength, _maxByteStringLength, _maxArrayLength, _maxMessageSize, _maxBufferSize, _channelLifetime, _securityTokenLifetime);
    }

    static {
        DelegateRegistry.registerEncoder(EndpointConfiguration::encode, EndpointConfiguration.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(EndpointConfiguration::decode, EndpointConfiguration.class, BinaryEncodingId, XmlEncodingId);
    }

}
