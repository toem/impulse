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

package com.digitalpetri.opcua.stack.core;


import java.lang.reflect.Field;
import java.util.Optional;

import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.util.annotations.Description;
import com.google.common.collect.ImmutableMap;


@SuppressWarnings("unused")
public class StatusCodes {

    private static final ImmutableMap<Long, String[]> DESCRIPTIONS;

    static {
        ImmutableMap.Builder<Long, String[]> builder = ImmutableMap.builder();

        for (Field f : StatusCodes.class.getFields()) {
            if (f.isAnnotationPresent(Description.class)) {
                try {
                    long code = f.getLong(null);
                    String name = f.getName();
                    String desc = f.getAnnotation(Description.class).value();

                    builder.put(code, new String[]{name, desc});
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        builder.put(StatusCode.GOOD.getValue(), new String[]{"Good", "Good; unspecified."});
        builder.put(StatusCode.BAD.getValue(), new String[]{"Bad", "Bad; unspecified."});

        DESCRIPTIONS = builder.build();
    }

    /**
     * Lookup information about a StatusCode.
     *
     * @param code the code to lookup.
     * @return a String[] where String[0] contains the name and String[1] contains the description.
     */
    public static Optional<String[]> lookup(long code) {
        String[] desc = DESCRIPTIONS.get(code & 0xFFFF0000);

        return Optional.ofNullable(desc);
    }

    @Description("An unexpected error occurred.")
    public final static long Bad_UnexpectedError = 0x80010000L;

    @Description("An internal error occurred as a result of a programming or configuration error.")
    public final static long Bad_InternalError = 0x80020000L;

    @Description("Not enough memory to complete the operation.")
    public final static long Bad_OutOfMemory = 0x80030000L;

    @Description("An operating system resource is not available.")
    public final static long Bad_ResourceUnavailable = 0x80040000L;

    @Description("A low level communication error occurred.")
    public final static long Bad_CommunicationError = 0x80050000L;

    @Description("Encoding halted because of invalid data in the objects being serialized.")
    public final static long Bad_EncodingError = 0x80060000L;

    @Description("Decoding halted because of invalid data in the stream.")
    public final static long Bad_DecodingError = 0x80070000L;

    @Description("The message encoding/decoding limits imposed by the stack have been exceeded.")
    public final static long Bad_EncodingLimitsExceeded = 0x80080000L;

    @Description("The request message size exceeds limits set by the server.")
    public final static long Bad_RequestTooLarge = 0x80B80000L;

    @Description("The response message size exceeds limits set by the client.")
    public final static long Bad_ResponseTooLarge = 0x80B90000L;

    @Description("An unrecognized response was received from the server.")
    public final static long Bad_UnknownResponse = 0x80090000L;

    @Description("The operation timed out.")
    public final static long Bad_Timeout = 0x800A0000L;

    @Description("The server does not support the requested service.")
    public final static long Bad_ServiceUnsupported = 0x800B0000L;

    @Description("The operation was cancelled because the application is shutting down.")
    public final static long Bad_Shutdown = 0x800C0000L;

    @Description("The operation could not complete because the client is not connected to the server.")
    public final static long Bad_ServerNotConnected = 0x800D0000L;

    @Description("The server has stopped and cannot process any requests.")
    public final static long Bad_ServerHalted = 0x800E0000L;

    @Description("There was nothing to do because the client passed a list of operations with no elements.")
    public final static long Bad_NothingToDo = 0x800F0000L;

    @Description("The request could not be processed because it specified too many operations.")
    public final static long Bad_TooManyOperations = 0x80100000L;

    @Description("The request could not be processed because there are too many monitored items in the subscription.")
    public final static long Bad_TooManyMonitoredItems = 0x80DB0000L;

    @Description("The extension object cannot be (de)serialized because the data type id is not recognized.")
    public final static long Bad_DataTypeIdUnknown = 0x80110000L;

    @Description("The certificate provided as a parameter is not valid.")
    public final static long Bad_CertificateInvalid = 0x80120000L;

    @Description("An error occurred verifying security.")
    public final static long Bad_SecurityChecksFailed = 0x80130000L;

    @Description("The Certificate has expired or is not yet valid.")
    public final static long Bad_CertificateTimeInvalid = 0x80140000L;

    @Description("An Issuer Certificate has expired or is not yet valid.")
    public final static long Bad_CertificateIssuerTimeInvalid = 0x80150000L;

    @Description("The HostName used to connect to a Server does not match a HostName in the Certificate.")
    public final static long Bad_CertificateHostNameInvalid = 0x80160000L;

    @Description("The URI specified in the ApplicationDescription does not match the URI in the Certificate.")
    public final static long Bad_CertificateUriInvalid = 0x80170000L;

    @Description("The Certificate may not be used for the requested operation.")
    public final static long Bad_CertificateUseNotAllowed = 0x80180000L;

    @Description("The Issuer Certificate may not be used for the requested operation.")
    public final static long Bad_CertificateIssuerUseNotAllowed = 0x80190000L;

    @Description("The Certificate is not trusted.")
    public final static long Bad_CertificateUntrusted = 0x801A0000L;

    @Description("It was not possible to determine if the Certificate has been revoked.")
    public final static long Bad_CertificateRevocationUnknown = 0x801B0000L;

    @Description("It was not possible to determine if the Issuer Certificate has been revoked.")
    public final static long Bad_CertificateIssuerRevocationUnknown = 0x801C0000L;

    @Description("The certificate has been revoked.")
    public final static long Bad_CertificateRevoked = 0x801D0000L;

    @Description("The issuer certificate has been revoked.")
    public final static long Bad_CertificateIssuerRevoked = 0x801E0000L;

    @Description("The certificate chain is incomplete.")
    public final static long Bad_CertificateChainIncomplete = 0x810D0000L;

    @Description("User does not have permission to perform the requested operation.")
    public final static long Bad_UserAccessDenied = 0x801F0000L;

    @Description("The user identity token is not valid.")
    public final static long Bad_IdentityTokenInvalid = 0x80200000L;

    @Description("The user identity token is valid but the server has rejected it.")
    public final static long Bad_IdentityTokenRejected = 0x80210000L;

    @Description("The specified secure channel is no longer valid.")
    public final static long Bad_SecureChannelIdInvalid = 0x80220000L;

    @Description("The timestamp is outside the range allowed by the server.")
    public final static long Bad_InvalidTimestamp = 0x80230000L;

    @Description("The nonce does appear to be not a random value or it is not the correct length.")
    public final static long Bad_NonceInvalid = 0x80240000L;

    @Description("The session id is not valid.")
    public final static long Bad_SessionIdInvalid = 0x80250000L;

    @Description("The session was closed by the client.")
    public final static long Bad_SessionClosed = 0x80260000L;

    @Description("The session cannot be used because ActivateSession has not been called.")
    public final static long Bad_SessionNotActivated = 0x80270000L;

    @Description("The subscription id is not valid.")
    public final static long Bad_SubscriptionIdInvalid = 0x80280000L;

    @Description("The header for the request is missing or invalid.")
    public final static long Bad_RequestHeaderInvalid = 0x802A0000L;

    @Description("The timestamps to return parameter is invalid.")
    public final static long Bad_TimestampsToReturnInvalid = 0x802B0000L;

    @Description("The request was cancelled by the client.")
    public final static long Bad_RequestCancelledByClient = 0x802C0000L;

    @Description("Too many arguments were provided.")
    public final static long Bad_TooManyArguments = 0x80E50000L;

    @Description("The subscription was transferred to another session.")
    public final static long Good_SubscriptionTransferred = 0x002D0000L;

    @Description("The processing will complete asynchronously.")
    public final static long Good_CompletesAsynchronously = 0x002E0000L;

    @Description("Sampling has slowed down due to resource limitations.")
    public final static long Good_Overload = 0x002F0000L;

    @Description("The value written was accepted but was clamped.")
    public final static long Good_Clamped = 0x00300000L;

    @Description("Communication with the data source is defined.")
    public final static long Bad_NoCommunication = 0x80310000L;

    @Description("Waiting for the server to obtain values from the underlying data source.")
    public final static long Bad_WaitingForInitialData = 0x80320000L;

    @Description("The syntax of the node id is not valid.")
    public final static long Bad_NodeIdInvalid = 0x80330000L;

    @Description("The node id refers to a node that does not exist in the server address space.")
    public final static long Bad_NodeIdUnknown = 0x80340000L;

    @Description("The attribute is not supported for the specified Node.")
    public final static long Bad_AttributeIdInvalid = 0x80350000L;

    @Description("The syntax of the index range parameter is invalid.")
    public final static long Bad_IndexRangeInvalid = 0x80360000L;

    @Description("No data exists within the range of indexes specified.")
    public final static long Bad_IndexRangeNoData = 0x80370000L;

    @Description("The data encoding is invalid.")
    public final static long Bad_DataEncodingInvalid = 0x80380000L;

    @Description("The server does not support the requested data encoding for the node.")
    public final static long Bad_DataEncodingUnsupported = 0x80390000L;

    @Description("The access level does not allow reading or subscribing to the Node.")
    public final static long Bad_NotReadable = 0x803A0000L;

    @Description("The access level does not allow writing to the Node.")
    public final static long Bad_NotWritable = 0x803B0000L;

    @Description("The value was out of range.")
    public final static long Bad_OutOfRange = 0x803C0000L;

    @Description("The requested operation is not supported.")
    public final static long Bad_NotSupported = 0x803D0000L;

    @Description("A requested item was not found or a search operation ended without success.")
    public final static long Bad_NotFound = 0x803E0000L;

    @Description("The object cannot be used because it has been deleted.")
    public final static long Bad_ObjectDeleted = 0x803F0000L;

    @Description("Requested operation is not implemented.")
    public final static long Bad_NotImplemented = 0x80400000L;

    @Description("The monitoring mode is invalid.")
    public final static long Bad_MonitoringModeInvalid = 0x80410000L;

    @Description("The monitoring item id does not refer to a valid monitored item.")
    public final static long Bad_MonitoredItemIdInvalid = 0x80420000L;

    @Description("The monitored item filter parameter is not valid.")
    public final static long Bad_MonitoredItemFilterInvalid = 0x80430000L;

    @Description("The server does not support the requested monitored item filter.")
    public final static long Bad_MonitoredItemFilterUnsupported = 0x80440000L;

    @Description("A monitoring filter cannot be used in combination with the attribute specified.")
    public final static long Bad_FilterNotAllowed = 0x80450000L;

    @Description("A mandatory structured parameter was missing or null.")
    public final static long Bad_StructureMissing = 0x80460000L;

    @Description("The event filter is not valid.")
    public final static long Bad_EventFilterInvalid = 0x80470000L;

    @Description("The content filter is not valid.")
    public final static long Bad_ContentFilterInvalid = 0x80480000L;

    @Description("An unregognized operator was provided in a filter.")
    public final static long Bad_FilterOperatorInvalid = 0x80C10000L;

    @Description("A valid operator was provided.")
    public final static long Bad_FilterOperatorUnsupported = 0x80C20000L;

    @Description("The number of operands provided for the filter operator was less then expected for the operand provided.")
    public final static long Bad_FilterOperandCountMismatch = 0x80C30000L;

    @Description("The operand used in a content filter is not valid.")
    public final static long Bad_FilterOperandInvalid = 0x80490000L;

    @Description("The referenced element is not a valid element in the content filter.")
    public final static long Bad_FilterElementInvalid = 0x80C40000L;

    @Description("The referenced literal is not a valid value.")
    public final static long Bad_FilterLiteralInvalid = 0x80C50000L;

    @Description("The continuation point provide is longer valid.")
    public final static long Bad_ContinuationPointInvalid = 0x804A0000L;

    @Description("The operation could not be processed because all continuation points have been allocated.")
    public final static long Bad_NoContinuationPoints = 0x804B0000L;

    @Description("The operation could not be processed because all continuation points have been allocated.")
    public final static long Bad_ReferenceTypeIdInvalid = 0x804C0000L;

    @Description("The browse direction is not valid.")
    public final static long Bad_BrowseDirectionInvalid = 0x804D0000L;

    @Description("The node is not part of the view.")
    public final static long Bad_NodeNotInView = 0x804E0000L;

    @Description("The ServerUri is not a valid URI.")
    public final static long Bad_ServerUriInvalid = 0x804F0000L;

    @Description("No ServerName was specified.")
    public final static long Bad_ServerNameMissing = 0x80500000L;

    @Description("No DiscoveryUrl was specified.")
    public final static long Bad_DiscoveryUrlMissing = 0x80510000L;

    @Description("The semaphore file specified by the client is not valid.")
    public final static long Bad_SemaphoreFileMissing = 0x80520000L;

    @Description("The security token request type is not valid.")
    public final static long Bad_RequestTypeInvalid = 0x80530000L;

    @Description("The security mode does not meet the requirements set by the Server.")
    public final static long Bad_SecurityModeRejected = 0x80540000L;

    @Description("The security policy does not meet the requirements set by the Server.")
    public final static long Bad_SecurityPolicyRejected = 0x80550000L;

    @Description("The server has reached its maximum number of sessions.")
    public final static long Bad_TooManySessions = 0x80560000L;

    @Description("The user token signature is missing or invalid.")
    public final static long Bad_UserSignatureInvalid = 0x80570000L;

    @Description("The signature generated with the client certificate is missing or invalid.")
    public final static long Bad_ApplicationSignatureInvalid = 0x80580000L;

    @Description("The client did not provide at least one software certificate that is valid and meets the profile requirements for the server.")
    public final static long Bad_NoValidCertificates = 0x80590000L;

    @Description("The server does not support changing the user identity assigned to the session.")
    public final static long Bad_IdentityChangeNotSupported = 0x80C60000L;

    @Description("The request was cancelled by the client with the Cancel service.")
    public final static long Bad_RequestCancelledByRequest = 0x805A0000L;

    @Description("The parent node id does not to refer to a valid node.")
    public final static long Bad_ParentNodeIdInvalid = 0x805B0000L;

    @Description("The reference could not be created because it violates constraints imposed by the data model.")
    public final static long Bad_ReferenceNotAllowed = 0x805C0000L;

    @Description("The requested node id was reject because it was either invalid or server does not allow node ids to be specified by the client.")
    public final static long Bad_NodeIdRejected = 0x805D0000L;

    @Description("The requested node id is already used by another node.")
    public final static long Bad_NodeIdExists = 0x805E0000L;

    @Description("The node class is not valid.")
    public final static long Bad_NodeClassInvalid = 0x805F0000L;

    @Description("The browse name is invalid.")
    public final static long Bad_BrowseNameInvalid = 0x80600000L;

    @Description("The browse name is not unique among nodes that share the same relationship with the parent.")
    public final static long Bad_BrowseNameDuplicated = 0x80610000L;

    @Description("The node attributes are not valid for the node class.")
    public final static long Bad_NodeAttributesInvalid = 0x80620000L;

    @Description("The type definition node id does not reference an appropriate type node.")
    public final static long Bad_TypeDefinitionInvalid = 0x80630000L;

    @Description("The source node id does not reference a valid node.")
    public final static long Bad_SourceNodeIdInvalid = 0x80640000L;

    @Description("The target node id does not reference a valid node.")
    public final static long Bad_TargetNodeIdInvalid = 0x80650000L;

    @Description("The reference type between the nodes is already defined.")
    public final static long Bad_DuplicateReferenceNotAllowed = 0x80660000L;

    @Description("The server does not allow this type of self reference on this node.")
    public final static long Bad_InvalidSelfReference = 0x80670000L;

    @Description("The reference type is not valid for a reference to a remote server.")
    public final static long Bad_ReferenceLocalOnly = 0x80680000L;

    @Description("The server will not allow the node to be deleted.")
    public final static long Bad_NoDeleteRights = 0x80690000L;

    @Description("The server was not able to delete all target references.")
    public final static long Uncertain_ReferenceNotDeleted = 0x40BC0000L;

    @Description("The server index is not valid.")
    public final static long Bad_ServerIndexInvalid = 0x806A0000L;

    @Description("The view id does not refer to a valid view node.")
    public final static long Bad_ViewIdUnknown = 0x806B0000L;

    @Description("The view timestamp is not available or not supported.")
    public final static long Bad_ViewTimestampInvalid = 0x80C90000L;

    @Description("The view parameters are not consistent with each other.")
    public final static long Bad_ViewParameterMismatch = 0x80CA0000L;

    @Description("The view version is not available or not supported.")
    public final static long Bad_ViewVersionInvalid = 0x80CB0000L;

    @Description("The list of references may not be complete because the underlying system is not available.")
    public final static long Uncertain_NotAllNodesAvailable = 0x40C00000L;

    @Description("The server should have followed a reference to a node in a remote server but did not. The result set may be incomplete.")
    public final static long Good_ResultsMayBeIncomplete = 0x00BA0000L;

    @Description("The provided Nodeid was not a type definition nodeid.")
    public final static long Bad_NotTypeDefinition = 0x80C80000L;

    @Description("One of the references to follow in the relative path references to a node in the address space in another server.")
    public final static long Uncertain_ReferenceOutOfServer = 0x406C0000L;

    @Description("The requested operation has too many matches to return.")
    public final static long Bad_TooManyMatches = 0x806D0000L;

    @Description("The requested operation requires too many resources in the server.")
    public final static long Bad_QueryTooComplex = 0x806E0000L;

    @Description("The requested operation has no match to return.")
    public final static long Bad_NoMatch = 0x806F0000L;

    @Description("The max age parameter is invalid.")
    public final static long Bad_MaxAgeInvalid = 0x80700000L;

    @Description("The operation is not permitted over the current secure channel.")
    public final static long Bad_SecurityModeInsufficient = 0x80E60000L;

    @Description("The history details parameter is not valid.")
    public final static long Bad_HistoryOperationInvalid = 0x80710000L;

    @Description("The server does not support the requested operation.")
    public final static long Bad_HistoryOperationUnsupported = 0x80720000L;

    @Description("The defined timestamp to return was invalid.")
    public final static long Bad_InvalidTimestampArgument = 0x80BD0000L;

    @Description("The server not does support writing the combination of value.")
    public final static long Bad_WriteNotSupported = 0x80730000L;

    @Description("The value supplied for the attribute is not of the same type as the attribute's value.")
    public final static long Bad_TypeMismatch = 0x80740000L;

    @Description("The method id does not refer to a method for the specified object.")
    public final static long Bad_MethodInvalid = 0x80750000L;

    @Description("The client did not specify all of the input arguments for the method.")
    public final static long Bad_ArgumentsMissing = 0x80760000L;

    @Description("The server has reached its  maximum number of subscriptions.")
    public final static long Bad_TooManySubscriptions = 0x80770000L;

    @Description("The server has reached the maximum number of queued publish requests.")
    public final static long Bad_TooManyPublishRequests = 0x80780000L;

    @Description("There is no subscription available for this session.")
    public final static long Bad_NoSubscription = 0x80790000L;

    @Description("The sequence number is unknown to the server.")
    public final static long Bad_SequenceNumberUnknown = 0x807A0000L;

    @Description("The requested notification message is no longer available.")
    public final static long Bad_MessageNotAvailable = 0x807B0000L;

    @Description("The Client of the current Session does not support one or more Profiles that are necessary for the Subscription.")
    public final static long Bad_InsufficientClientProfile = 0x807C0000L;

    @Description("The sub-state machine is not currently active.")
    public final static long Bad_StateNotActive = 0x80BF0000L;

    @Description("The server cannot process the request because it is too busy.")
    public final static long Bad_TcpServerTooBusy = 0x807D0000L;

    @Description("The type of the message specified in the header invalid.")
    public final static long Bad_TcpMessageTypeInvalid = 0x807E0000L;

    @Description("The SecureChannelId and/or TokenId are not currently in use.")
    public final static long Bad_TcpSecureChannelUnknown = 0x807F0000L;

    @Description("The size of the message specified in the header is too large.")
    public final static long Bad_TcpMessageTooLarge = 0x80800000L;

    @Description("There are not enough resources to process the request.")
    public final static long Bad_TcpNotEnoughResources = 0x80810000L;

    @Description("An internal error occurred.")
    public final static long Bad_TcpInternalError = 0x80820000L;

    @Description("The Server does not recognize the QueryString specified.")
    public final static long Bad_TcpEndpointUrlInvalid = 0x80830000L;

    @Description("The request could not be sent because of a network interruption.")
    public final static long Bad_RequestInterrupted = 0x80840000L;

    @Description("Timeout occurred while processing the request.")
    public final static long Bad_RequestTimeout = 0x80850000L;

    @Description("The secure channel has been closed.")
    public final static long Bad_SecureChannelClosed = 0x80860000L;

    @Description("The token has expired or is not recognized.")
    public final static long Bad_SecureChannelTokenUnknown = 0x80870000L;

    @Description("The sequence number is not valid.")
    public final static long Bad_SequenceNumberInvalid = 0x80880000L;

    @Description("The applications do not have compatible protocol versions.")
    public final static long Bad_ProtocolVersionUnsupported = 0x80BE0000L;

    @Description("There is a problem with the configuration that affects the usefulness of the value.")
    public final static long Bad_ConfigurationError = 0x80890000L;

    @Description("The variable should receive its value from another variable.")
    public final static long Bad_NotConnected = 0x808A0000L;

    @Description("There has been a failure in the device/data source that generates the value that has affected the value.")
    public final static long Bad_DeviceFailure = 0x808B0000L;

    @Description("There has been a failure in the sensor from which the value is derived by the device/data source.")
    public final static long Bad_SensorFailure = 0x808C0000L;

    @Description("The source of the data is not operational.")
    public final static long Bad_OutOfService = 0x808D0000L;

    @Description("The deadband filter is not valid.")
    public final static long Bad_DeadbandFilterInvalid = 0x808E0000L;

    @Description("Communication to the data source has failed. The variable value is the last value that had a good quality.")
    public final static long Uncertain_NoCommunicationLastUsableValue = 0x408F0000L;

    @Description("Whatever was updating this value has stopped doing so.")
    public final static long Uncertain_LastUsableValue = 0x40900000L;

    @Description("The value is an operational value that was manually overwritten.")
    public final static long Uncertain_SubstituteValue = 0x40910000L;

    @Description("The value is an initial value for a variable that normally receives its value from another variable.")
    public final static long Uncertain_InitialValue = 0x40920000L;

    @Description("The value is at one of the sensor limits.")
    public final static long Uncertain_SensorNotAccurate = 0x40930000L;

    @Description("The value is outside of the range of values defined for this parameter.")
    public final static long Uncertain_EngineeringUnitsExceeded = 0x40940000L;

    @Description("The value is derived from multiple sources and has less than the required number of Good sources.")
    public final static long Uncertain_SubNormal = 0x40950000L;

    @Description("The value has been overridden.")
    public final static long Good_LocalOverride = 0x00960000L;

    @Description("This Condition refresh failed.")
    public final static long Bad_RefreshInProgress = 0x80970000L;

    @Description("This condition has already been disabled.")
    public final static long Bad_ConditionAlreadyDisabled = 0x80980000L;

    @Description("This condition has already been enabled.")
    public final static long Bad_ConditionAlreadyEnabled = 0x80CC0000L;

    @Description("Property not available.")
    public final static long Bad_ConditionDisabled = 0x80990000L;

    @Description("The specified event id is not recognized.")
    public final static long Bad_EventIdUnknown = 0x809A0000L;

    @Description("The event cannot be acknowledged.")
    public final static long Bad_EventNotAcknowledgeable = 0x80BB0000L;

    @Description("The dialog condition is not active.")
    public final static long Bad_DialogNotActive = 0x80CD0000L;

    @Description("The response is not valid for the dialog.")
    public final static long Bad_DialogResponseInvalid = 0x80CE0000L;

    @Description("The condition branch has already been acknowledged.")
    public final static long Bad_ConditionBranchAlreadyAcked = 0x80CF0000L;

    @Description("The condition branch has already been confirmed.")
    public final static long Bad_ConditionBranchAlreadyConfirmed = 0x80D00000L;

    @Description("The condition has already been shelved.")
    public final static long Bad_ConditionAlreadyShelved = 0x80D10000L;

    @Description("The condition is not currently shelved.")
    public final static long Bad_ConditionNotShelved = 0x80D20000L;

    @Description("The shelving time not within an acceptable range.")
    public final static long Bad_ShelvingTimeOutOfRange = 0x80D30000L;

    @Description("No data exists for the requested time range or event filter.")
    public final static long Bad_NoData = 0x809B0000L;

    @Description("No data found to provide upper or lower bound value.")
    public final static long Bad_BoundNotFound = 0x80D70000L;

    @Description("The server cannot retrieve a bound for the variable.")
    public final static long Bad_BoundNotSupported = 0x80D80000L;

    @Description("Data is missing due to collection started/stopped/lost.")
    public final static long Bad_DataLost = 0x809D0000L;

    @Description("Expected data is unavailable for the requested time range due to an un-mounted volume.")
    public final static long Bad_DataUnavailable = 0x809E0000L;

    @Description("The data or event was not successfully inserted because a matching entry exists.")
    public final static long Bad_EntryExists = 0x809F0000L;

    @Description("The data or event was not successfully updated because no matching entry exists.")
    public final static long Bad_NoEntryExists = 0x80A00000L;

    @Description("The client requested history using a timestamp format the server does not support (i.e requested ServerTimestamp when server only supports SourceTimestamp).")
    public final static long Bad_TimestampNotSupported = 0x80A10000L;

    @Description("The data or event was successfully inserted into the historical database.")
    public final static long Good_EntryInserted = 0x00A20000L;

    @Description("The data or event field was successfully replaced in the historical database.")
    public final static long Good_EntryReplaced = 0x00A30000L;

    @Description("The value is derived from multiple values and has less than the required number of Good values.")
    public final static long Uncertain_DataSubNormal = 0x40A40000L;

    @Description("No data exists for the requested time range or event filter.")
    public final static long Good_NoData = 0x00A50000L;

    @Description("The data or event field was successfully replaced in the historical database.")
    public final static long Good_MoreData = 0x00A60000L;

    @Description("The requested number of Aggregates does not match the requested number of NodeIds.")
    public final static long Bad_AggregateListMismatch = 0x80D40000L;

    @Description("The requested Aggregate is not support by the server.")
    public final static long Bad_AggregateNotSupported = 0x80D50000L;

    @Description("The aggregate value could not be derived due to invalid data inputs.")
    public final static long Bad_AggregateInvalidInputs = 0x80D60000L;

    @Description("The aggregate configuration is not valid for specified node.")
    public final static long Bad_AggregateConfigurationRejected = 0x80DA0000L;

    @Description("The request pecifies fields which are not valid for the EventType or cannot be saved by the historian.")
    public final static long Good_DataIgnored = 0x00D90000L;

    @Description("The request was rejected by the server because it did not meet the criteria set by the server.")
    public final static long Bad_RequestNotAllowed = 0x80E40000L;

    @Description("The value does not come from the real source and has been edited by the server.")
    public final static long Good_Edited = 0x00DC0000L;

    @Description("There was an error in execution of these post-actions.")
    public final static long Good_PostActionFailed = 0x00DD0000L;

    @Description("The related EngineeringUnit has been changed but the Variable Value is still provided based on the previous unit.")
    public final static long Uncertain_DominantValueChanged = 0x40DE0000L;

    @Description("A dependent value has been changed but the change has not been applied to the device.")
    public final static long Good_DependentValueChanged = 0x00E00000L;

    @Description("The related EngineeringUnit has been changed but this change has not been applied to the device. The Variable Value is still dependent on the previous unit but its status is currently Bad.")
    public final static long Bad_DominantValueChanged = 0x80E10000L;

    @Description("A dependent value has been changed but the change has not been applied to the device. The quality of the dominant variable is uncertain.")
    public final static long Uncertain_DependentValueChanged = 0x40E20000L;

    @Description("A dependent value has been changed but the change has not been applied to the device. The quality of the dominant variable is Bad.")
    public final static long Bad_DependentValueChanged = 0x80E30000L;

    @Description("The communication layer has raised an event.")
    public final static long Good_CommunicationEvent = 0x00A70000L;

    @Description("The system is shutting down.")
    public final static long Good_ShutdownEvent = 0x00A80000L;

    @Description("The operation is not finished and needs to be called again.")
    public final static long Good_CallAgain = 0x00A90000L;

    @Description("A non-critical timeout occurred.")
    public final static long Good_NonCriticalTimeout = 0x00AA0000L;

    @Description("One or more arguments are invalid.")
    public final static long Bad_InvalidArgument = 0x80AB0000L;

    @Description("Could not establish a network connection to remote server.")
    public final static long Bad_ConnectionRejected = 0x80AC0000L;

    @Description("The server has disconnected from the client.")
    public final static long Bad_Disconnect = 0x80AD0000L;

    @Description("The network connection has been closed.")
    public final static long Bad_ConnectionClosed = 0x80AE0000L;

    @Description("The operation cannot be completed because the object is closed.")
    public final static long Bad_InvalidState = 0x80AF0000L;

    @Description("Cannot move beyond end of the stream.")
    public final static long Bad_EndOfStream = 0x80B00000L;

    @Description("No data is currently available for reading from a non-blocking stream.")
    public final static long Bad_NoDataAvailable = 0x80B10000L;

    @Description("The asynchronous operation is waiting for a response.")
    public final static long Bad_WaitingForResponse = 0x80B20000L;

    @Description("The asynchronous operation was abandoned by the caller.")
    public final static long Bad_OperationAbandoned = 0x80B30000L;

    @Description("The stream did not return all data requested (possibly because it is a non-blocking stream).")
    public final static long Bad_ExpectedStreamToBlock = 0x80B40000L;

    @Description("Non blocking behaviour is required and the operation would block.")
    public final static long Bad_WouldBlock = 0x80B50000L;

    @Description("A value had an invalid syntax.")
    public final static long Bad_SyntaxError = 0x80B60000L;

    @Description("The operation could not be finished because all available connections are in use.")
    public final static long Bad_MaxConnectionsReached = 0x80B70000L;

}
