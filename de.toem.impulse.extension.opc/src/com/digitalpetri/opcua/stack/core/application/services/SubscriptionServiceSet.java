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

package com.digitalpetri.opcua.stack.core.application.services;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.structured.CreateSubscriptionRequest;
import com.digitalpetri.opcua.stack.core.types.structured.CreateSubscriptionResponse;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteSubscriptionsRequest;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteSubscriptionsResponse;
import com.digitalpetri.opcua.stack.core.types.structured.ModifySubscriptionRequest;
import com.digitalpetri.opcua.stack.core.types.structured.ModifySubscriptionResponse;
import com.digitalpetri.opcua.stack.core.types.structured.PublishRequest;
import com.digitalpetri.opcua.stack.core.types.structured.PublishResponse;
import com.digitalpetri.opcua.stack.core.types.structured.RepublishRequest;
import com.digitalpetri.opcua.stack.core.types.structured.RepublishResponse;
import com.digitalpetri.opcua.stack.core.types.structured.SetPublishingModeRequest;
import com.digitalpetri.opcua.stack.core.types.structured.SetPublishingModeResponse;
import com.digitalpetri.opcua.stack.core.types.structured.TransferSubscriptionsRequest;
import com.digitalpetri.opcua.stack.core.types.structured.TransferSubscriptionsResponse;

public interface SubscriptionServiceSet {

    default void onCreateSubscription(ServiceRequest<CreateSubscriptionRequest, CreateSubscriptionResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onModifySubscription(ServiceRequest<ModifySubscriptionRequest, ModifySubscriptionResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onDeleteSubscriptions(ServiceRequest<DeleteSubscriptionsRequest, DeleteSubscriptionsResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onTransferSubscriptions(ServiceRequest<TransferSubscriptionsRequest, TransferSubscriptionsResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onSetPublishingMode(ServiceRequest<SetPublishingModeRequest, SetPublishingModeResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onPublish(ServiceRequest<PublishRequest, PublishResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

    default void onRepublish(ServiceRequest<RepublishRequest, RepublishResponse> serviceRequest) throws UaException {
        serviceRequest.setServiceFault(StatusCodes.Bad_ServiceUnsupported);
    }

}
