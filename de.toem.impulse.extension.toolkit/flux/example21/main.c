/*******************************************************************************
 * Copyright (c) 2012-2019 Thomas Haber
 *
 * All rights reserved. This source code and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>
#include <math.h>
#include "flux.h"

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#endif

// geometry
#define MAX_ITEM_ID 2 // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096

// trace object
flxTrace trace;


flxresult handleDeclare(flxbyte command, flxid controlId,flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	FLX_CONTROL_HANDLE_ENUM_PARAMETER(0, eId, 0)
	FLX_CONTROL_HANDLE_TEXT_PARAMETER(1, tName, 32)
	FLX_CONTROL_HANDLE_ENUM_PARAMETER(2, eStype, 0)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		flxAddSignal(trace, eId, 0, tName, 0, eStype, 0);
		flxWriteControlResult(trace, controlId, messageId, 0, 0);
		flxFlush(trace);
	}
	return FLX_OK;
}

flxresult handleOpen(flxbyte command, flxid controlId,flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	FLX_CONTROL_HANDLE_INTEGER_PARAMETER(0, iStart, long, 0, 1)
	FLX_CONTROL_HANDLE_TEXT_PARAMETER(1, tBase, 32)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		flxOpen(trace, 0, tBase, iStart, 0);
		flxWriteControlResult(trace, controlId, messageId, 0, 0);
		flxFlush(trace);
	}

	return FLX_OK;
}

flxresult handleWrite(flxbyte command, flxid controlId,flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	FLX_CONTROL_HANDLE_ENUM_PARAMETER(0, eID, 0)
	FLX_CONTROL_HANDLE_ENUM_PARAMETER(1, eStype, 0)
	FLX_CONTROL_HANDLE_INTEGER_PARAMETER(2, iCurrent, long, 0, 1)
	FLX_CONTROL_HANDLE_BINARY_PARAMETER(3, bVal, 32)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {
		switch (eStype) {
		case FLX_TYPE_INTEGER:
			flxWriteIntAt(trace, eID, 0, iCurrent, 0,(void*) bVal, bValSize, 0);
			break;
		case FLX_TYPE_FLOAT:
			flxWriteFloatAt(trace, eID, 0, iCurrent, 0, (void*)bVal, bValSize);
			break;
		case FLX_TYPE_TEXT:
			flxWriteTextAt(trace, eID, 0, iCurrent, 0, (void*)bVal, bValSize);
			break;
		case FLX_TYPE_BINARY:
			flxWriteBinaryAt(trace, eID, 0, iCurrent, 0, (void*)bVal, bValSize);
			break;
		}
		flxWriteControlResult(trace, controlId, messageId, 0, 0);
		flxFlush(trace);
	}
	return FLX_OK;
}

flxresult handleClose(flxbyte command,flxid controlId, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	FLX_CONTROL_HANDLE_INTEGER_PARAMETER(0, iEnd, long, 0, 1)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		flxClose(trace, 0,iEnd);
		flxWriteControlResult(trace, controlId, messageId, 0, 0);
		flxFlush(trace);
	}
	return FLX_OK;
}

flxresult handleCommands(flxbyte command, flxid controlId, flxid messageId, flxid memberId, flxbyte type, void **value,
		flxuint *size, flxuint *opt) {
	switch (controlId) {
	case 1:
		return handleDeclare(command,controlId, messageId, memberId, type, value, size, opt);
	case 2:
		return handleOpen(command,controlId, messageId, memberId, type, value, size, opt);
	case 3:
		return handleWrite(command,controlId, messageId, memberId, type, value, size, opt);
	case 4:
		return handleClose(command,controlId, messageId, memberId, type, value, size, opt);
	}
	return FLX_ERROR_COMMAND_PARSE_ERROR;
}


int main() {

#ifdef _WIN32
	setmode(fileno(stdout),O_BINARY);
	setmode(fileno(stdin),O_BINARY);
#endif

	// calculate required memory for trace and buffers
	unsigned bufferSize = FLX_BUFFER_BYTES(MAX_ENTRY_SIZE);
	unsigned traceSize = FLX_TRACE_BYTES(0, MAX_ITEM_ID);

	// static memory
	unsigned char memoryBuffer[bufferSize];
	unsigned char memoryTrace[traceSize];

	// buffer
	flxBuffer buffer = flxCreateLinearBuffer(memoryBuffer, bufferSize, flxWriteToFile, stdout);

	// trace
	trace = flxCreateTrace(0, MAX_ITEM_ID, MAX_ENTRY_SIZE, memoryTrace, traceSize, buffer);

	// head
	flxAddHead(trace, "example", "flux example");

	//parse input
	flxParseControlInput(stdin, 128, handleCommands);
}

#ifdef __cplusplus
}
#endif

