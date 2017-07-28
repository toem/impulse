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

package com.digitalpetri.opcua.stack.core.serialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaSerializationException;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.google.common.collect.Maps;

public class DelegateRegistry {

	private static final Map<Class<?>, EncoderDelegate<?>> encodersByClass = Maps.newConcurrentMap();

	private static final Map<NodeId, EncoderDelegate<?>> encodersById = Maps.newConcurrentMap();

	private static final Map<Class<?>, DecoderDelegate<?>> decodersByClass = Maps.newConcurrentMap();

	private static final Map<NodeId, DecoderDelegate<?>> decodersById = Maps.newConcurrentMap();

	public static <T> void registerEncoder(EncoderDelegate<T> delegate, Class<T> clazz, NodeId... ids) {
		encodersByClass.put(clazz, delegate);

		if (ids != null) {
			Arrays.stream(ids).forEach(id -> encodersById.put(id, delegate));
		}
	}

	public static <T> void registerDecoder(DecoderDelegate<T> delegate, Class<T> clazz, NodeId... ids) {
		decodersByClass.put(clazz, delegate);

		if (ids != null) {
			Arrays.stream(ids).forEach(id -> decodersById.put(id, delegate));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> EncoderDelegate<T> getEncoder(Object t) throws UaSerializationException {
		try {
			return (EncoderDelegate<T>) encodersByClass.get(t.getClass());
		} catch (NullPointerException e) {
			throw new UaSerializationException(StatusCodes.Bad_EncodingError, "no encoder registered for class=" + t);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> EncoderDelegate<T> getEncoder(Class<?> clazz) throws UaSerializationException {
		try {
			return (EncoderDelegate<T>) encodersByClass.get(clazz);
		} catch (NullPointerException e) {
			throw new UaSerializationException(StatusCodes.Bad_EncodingError, "no encoder registered for class=" + clazz);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> EncoderDelegate<T> getEncoder(NodeId encodingId) throws UaSerializationException {
		try {
			return (EncoderDelegate<T>) encodersById.get(encodingId);
		} catch (NullPointerException e) {
			throw new UaSerializationException(StatusCodes.Bad_EncodingError, "no encoder registered for encodingId=" + encodingId);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> DecoderDelegate<T> getDecoder(T t) throws UaSerializationException {
		try {
			return (DecoderDelegate<T>) decodersByClass.get(t.getClass());
		} catch (NullPointerException e) {
			throw new UaSerializationException(StatusCodes.Bad_DecodingError, "no decoder registered for class=" + t);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> DecoderDelegate<T> getDecoder(Class<T> clazz) throws UaSerializationException {
		try {
			return (DecoderDelegate<T>) decodersByClass.get(clazz);
		} catch (NullPointerException e) {
			throw new UaSerializationException(StatusCodes.Bad_DecodingError, "no decoder registered for class=" + clazz);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> DecoderDelegate<T> getDecoder(NodeId encodingId) {
		DecoderDelegate<T> decoder = (DecoderDelegate<T>) decodersById.get(encodingId);

		if (decoder == null) {
			throw new UaSerializationException(StatusCodes.Bad_DecodingError, "no decoder registered for encodingId=" + encodingId);
		}

		return decoder;
	}

	static {
		/*
		 * Reflect-o-magically find all generated structured and enumerated
		 * types and force their static initialization blocks to run,
		 * registering their encode/decode methods with the delegate registry.
		 */
		Logger logger = LoggerFactory.getLogger(DelegateRegistry.class);

		ClassLoader classLoader = Stack.getCustomClassLoader().orElse(DelegateRegistry.class.getClassLoader());

		try {
			loadGeneratedClasses(classLoader);
		} catch (Exception e1) {
			// Temporarily set the thread context ClassLoader to our
			// ClassLoader and try loading the classes one more time.

			Thread thread = Thread.currentThread();
			ClassLoader contextClassLoader = thread.getContextClassLoader();

			thread.setContextClassLoader(classLoader);

			try {
				loadGeneratedClasses(classLoader);
			} catch (Exception e2) {
				logger.error("Error loading generated classes.", e2);
			} finally {
				thread.setContextClassLoader(contextClassLoader);
			}
		}
	}

	private static void loadGeneratedClasses(ClassLoader classLoader) throws IOException, ClassNotFoundException {

		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.ApplicationType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.AttributeWriteMask", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.AxisScaleEnumeration", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.BrowseDirection", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.BrowseResultMask", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.ComplianceLevel", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.DataChangeTrigger", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.DeadbandType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.EnumeratedTestType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.ExceptionDeviationFormat", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.FilterOperator", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.HistoryUpdateType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.IdType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.MessageSecurityMode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.ModelChangeStructureVerbMask", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.NamingRuleType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.NodeAttributesMask", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.NodeIdType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.OpenFileMode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.PerformUpdateType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.RedundancySupport", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.SecurityTokenRequestType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.ServerState", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.TrustListMasks", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.enumerated.UserTokenType", true, classLoader);

		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ActivateSessionRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ActivateSessionResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddNodesItem", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddNodesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddNodesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddNodesResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddReferencesItem", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddReferencesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AddReferencesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AggregateConfiguration", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AggregateFilter", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AggregateFilterResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.Annotation", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AnonymousIdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ApplicationDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.Argument", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ArrayTestType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AttributeOperand", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.AxisInformation", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseNextRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseNextResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowsePath", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowsePathResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowsePathTarget", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BrowseResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.BuildInfo", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CallMethodRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CallMethodResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CallRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CallResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CancelRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CancelResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ChannelSecurityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CloseSecureChannelRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CloseSecureChannelResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CloseSessionRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CloseSessionResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ComplexNumberType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CompositeTestType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ContentFilter", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ContentFilterElement", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ContentFilterElementResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ContentFilterResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateMonitoredItemsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateMonitoredItemsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateSessionRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateSessionResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateSubscriptionRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.CreateSubscriptionResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DataChangeFilter", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DataChangeNotification", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DataTypeAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DataTypeNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteAtTimeDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteEventDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteMonitoredItemsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteMonitoredItemsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteNodesItem", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteNodesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteNodesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteRawModifiedDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteReferencesItem", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteReferencesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteReferencesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteSubscriptionsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DeleteSubscriptionsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DiscoveryConfiguration", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.DoubleComplexNumberType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ElementOperand", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EndpointConfiguration", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EndpointUrlListDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EnumValueType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EUInformation", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EventFieldList", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EventFilter", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EventFilterResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.EventNotificationList", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.FilterOperand", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.FindServersOnNetworkRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.FindServersOnNetworkResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.FindServersRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.FindServersResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.GetEndpointsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.GetEndpointsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryData", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryEvent", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryEventFieldList", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryModifiedData", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryReadDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryReadRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryReadResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryReadResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryReadValueId", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryUpdateDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryUpdateRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryUpdateResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.HistoryUpdateResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.InstanceNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.IssuedIdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.KerberosIdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.LiteralOperand", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MdnsDiscoveryConfiguration", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MethodAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MethodNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModelChangeStructureDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModificationInfo", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModifyMonitoredItemsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModifyMonitoredItemsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModifySubscriptionRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ModifySubscriptionResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemModifyRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemModifyResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemNotification", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoringFilter", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoringFilterResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.MonitoringParameters", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NetworkGroupDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.Node", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NodeAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NodeReference", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NodeTypeDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NotificationData", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.NotificationMessage", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ObjectAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ObjectNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ObjectTypeAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ObjectTypeNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.OpenSecureChannelRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.OpenSecureChannelResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.OptionSet", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ParsingResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ProgramDiagnosticDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.PublishRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.PublishResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryDataDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryDataSet", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryFirstRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryFirstResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryNextRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.QueryNextResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.Range", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadAtTimeDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadEventDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadProcessedDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadRawModifiedDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReadValueId", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RedundantServerDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReferenceDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReferenceNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReferenceTypeAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ReferenceTypeNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisteredServer", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterNodesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterNodesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterServer2Request", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterServer2Response", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterServerRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RegisterServerResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RelativePath", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RelativePathElement", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RepublishRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RepublishResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.RequestHeader", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ResponseHeader", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SamplingIntervalDiagnosticsDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ScalarTestType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SemanticChangeStructureDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ServerDiagnosticsSummaryDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ServerOnNetwork", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ServerStatusDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ServiceCounterDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ServiceFault", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SessionDiagnosticsDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SessionSecurityDiagnosticsDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetMonitoringModeRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetMonitoringModeResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetPublishingModeRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetPublishingModeResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetTriggeringRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SetTriggeringResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SignatureData", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SignedSoftwareCertificate", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SimpleAttributeOperand", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SoftwareCertificate", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.StatusChangeNotification", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.StatusResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SubscriptionAcknowledgement", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SubscriptionDiagnosticsDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.SupportedProfile", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TestStackExRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TestStackExResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TestStackRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TestStackResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TimeZoneDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TransferResult", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TransferSubscriptionsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TransferSubscriptionsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TranslateBrowsePathsToNodeIdsRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TranslateBrowsePathsToNodeIdsResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TrustListDataType", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.TypeNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.Union", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UnregisterNodesRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UnregisterNodesResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UpdateDataDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UpdateEventDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UpdateStructureDataDetails", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UserIdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UserNameIdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.UserTokenPolicy", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.VariableAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.VariableNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.VariableTypeAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.VariableTypeNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ViewAttributes", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ViewDescription", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.ViewNode", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.WriteRequest", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.WriteResponse", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.WriteValue", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.X509IdentityToken", true, classLoader);
		Class.forName("com.digitalpetri.opcua.stack.core.types.structured.XVType", true, classLoader);
	}

}
