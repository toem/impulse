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
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

@UaDataType("BuildInfo")
public class BuildInfo implements UaStructure {

    public static final NodeId TypeId = Identifiers.BuildInfo;
    public static final NodeId BinaryEncodingId = Identifiers.BuildInfo_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.BuildInfo_Encoding_DefaultXml;

    protected final String _productUri;
    protected final String _manufacturerName;
    protected final String _productName;
    protected final String _softwareVersion;
    protected final String _buildNumber;
    protected final DateTime _buildDate;

    public BuildInfo() {
        this._productUri = null;
        this._manufacturerName = null;
        this._productName = null;
        this._softwareVersion = null;
        this._buildNumber = null;
        this._buildDate = null;
    }

    public BuildInfo(String _productUri, String _manufacturerName, String _productName, String _softwareVersion, String _buildNumber, DateTime _buildDate) {
        this._productUri = _productUri;
        this._manufacturerName = _manufacturerName;
        this._productName = _productName;
        this._softwareVersion = _softwareVersion;
        this._buildNumber = _buildNumber;
        this._buildDate = _buildDate;
    }

    public String getProductUri() { return _productUri; }

    public String getManufacturerName() { return _manufacturerName; }

    public String getProductName() { return _productName; }

    public String getSoftwareVersion() { return _softwareVersion; }

    public String getBuildNumber() { return _buildNumber; }

    public DateTime getBuildDate() { return _buildDate; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(BuildInfo buildInfo, UaEncoder encoder) {
        encoder.encodeString("ProductUri", buildInfo._productUri);
        encoder.encodeString("ManufacturerName", buildInfo._manufacturerName);
        encoder.encodeString("ProductName", buildInfo._productName);
        encoder.encodeString("SoftwareVersion", buildInfo._softwareVersion);
        encoder.encodeString("BuildNumber", buildInfo._buildNumber);
        encoder.encodeDateTime("BuildDate", buildInfo._buildDate);
    }

    public static BuildInfo decode(UaDecoder decoder) {
        String _productUri = decoder.decodeString("ProductUri");
        String _manufacturerName = decoder.decodeString("ManufacturerName");
        String _productName = decoder.decodeString("ProductName");
        String _softwareVersion = decoder.decodeString("SoftwareVersion");
        String _buildNumber = decoder.decodeString("BuildNumber");
        DateTime _buildDate = decoder.decodeDateTime("BuildDate");

        return new BuildInfo(_productUri, _manufacturerName, _productName, _softwareVersion, _buildNumber, _buildDate);
    }

    static {
        DelegateRegistry.registerEncoder(BuildInfo::encode, BuildInfo.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(BuildInfo::decode, BuildInfo.class, BinaryEncodingId, XmlEncodingId);
    }

}
