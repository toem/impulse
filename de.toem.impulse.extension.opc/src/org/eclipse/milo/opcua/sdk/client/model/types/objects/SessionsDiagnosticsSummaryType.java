/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.SessionDiagnosticsArrayType;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.SessionSecurityDiagnosticsArrayType;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.SessionDiagnosticsDataType;
import org.eclipse.milo.opcua.stack.core.types.structured.SessionSecurityDiagnosticsDataType;

public interface SessionsDiagnosticsSummaryType extends BaseObjectType {
    CompletableFuture<? extends SessionDiagnosticsArrayType> getSessionDiagnosticsArrayNode();

    CompletableFuture<SessionDiagnosticsDataType[]> getSessionDiagnosticsArray();

    CompletableFuture<StatusCode> setSessionDiagnosticsArray(SessionDiagnosticsDataType[] value);

    CompletableFuture<? extends SessionSecurityDiagnosticsArrayType> getSessionSecurityDiagnosticsArrayNode();

    CompletableFuture<SessionSecurityDiagnosticsDataType[]> getSessionSecurityDiagnosticsArray();

    CompletableFuture<StatusCode> setSessionSecurityDiagnosticsArray(SessionSecurityDiagnosticsDataType[] value);
}
