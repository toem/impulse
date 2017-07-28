/*******************************************************************************
 * Copyright (c) 2012-2017 toem
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

#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "flux.h"

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#endif

// geometry
#define MAX_ITEM_ID (300000) // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096 * 16
#define MAX_TRACE_REQUEST_ITEMS 4096 // max no of items to request
#define VERSION 1
// trace object
flxTrace trace;

flxresult handleReqScheme(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		// send geometry
		unsigned version = 1;
		unsigned maxTraceItems = MAX_TRACE_REQUEST_ITEMS;
		struct flxMemberValueStruct members[2];
		flxInitMember(members + 0, 0, 0, FLX_STRUCTTYPE_INTEGER, 0);
		flxInitMember(members + 1, 1, 0, FLX_STRUCTTYPE_INTEGER, 0);
		flxSetMember(members + 0, &version, sizeof(unsigned), 0, 1);
		flxSetMember(members + 1, &maxTraceItems, sizeof(unsigned), 0, 1);
		flxWriteControlResult(trace, FLX_CONTROL_DB_REQ_SCHEME, messageId, members, 2);
		flxFlush(trace);
	}
	return FLX_OK;
}

void addItems(flxid parent, flxid level) {

	static int id = 0;
	int n;
	flxchar name[15];

	id++;

	if (level == 5) {
		sprintf(name, "name%i%05i", level, id&0xff);
		flxAddSignal(trace, id, parent, name, "an integer", FLX_TYPE_INTEGER, 0);
	} else {
		sprintf(name, "name%i%05i", level, id);
		flxAddScope(trace, id, parent, name, "a scope");
		parent = id;
		for (n = 0; n < 8; n++) {
			addItems(parent, level + 1);
		}
	}
}
flxresult handleReqItems(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {
	int n;
	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		// add items
		for (n = 0; n < 8; n++)
			addItems(0, 0);

		// send open and close to notify about domain
		flxOpen(trace, 0, "ns", 0, 0);
		flxClose(trace, 0, 1000000L);

		// write result message & flush
		flxWriteControlResult(trace, FLX_CONTROL_DB_REQ_ITEMS, messageId, 0, 0);
		flxFlush(trace);
	}

	return FLX_OK;
}

flxresult handleReqTrace(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	static flxuint itemIds[MAX_TRACE_REQUEST_ITEMS];
	static flxuint count = 0;
	flxdomain current = 0;
	flxbint pos = 0;
	flxuint val = 0;


	// item ids as binary parameter
	FLX_CONTROL_HANDLE_BINARY_PARAMETER(0, bItemIds, MAX_ENTRY_SIZE)
	FLX_CONTROL_HANDLE_ENUM_PARAMETER(0, moreToCome, 0)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		// extract itemIds
		while (pos < bItemIdsSize) {
			val = 0;
			pos += _plusread(&val, bItemIds + pos, bItemIds + bItemIdsSize);
			if (val != 0 && count < MAX_TRACE_REQUEST_ITEMS) {
				itemIds[count++] = val;
			}
		}
		if (moreToCome)
			return FLX_OK;

		// psoido open
		trace->open = FLX_ITEM_OPEN_LOCAL;
		trace->current = 0;

		// write test data
		for (current = 0; current < 1000000L; current += 100) {
			for (pos = 0; pos < count; pos++) {
				val = itemIds[pos] + current;
				val = current;
				flxWriteIntAt(trace, itemIds[pos], 0, current, 0, &val, sizeof(val), 1);
			}
		}

		// write result message & flush
		flxWriteControlResult(trace, FLX_CONTROL_DB_REQ_TRACE, messageId, 0, 0);
		flxFlush(trace);

		// reset count
		count = 0;
	}
	return FLX_OK;
}

flxresult handleCommands(flxbyte command, flxid controlId, flxid messageId, flxid memberId, flxbyte type, void **value,
		flxuint *size, flxuint *opt) {
	switch (controlId) {
	case FLX_CONTROL_DB_REQ_SCHEME:
		return handleReqScheme(command, messageId, memberId, type, value, size, opt);
	case FLX_CONTROL_DB_REQ_ITEMS:
		return handleReqItems(command, messageId, memberId, type, value, size, opt);
	case FLX_CONTROL_DB_REQ_TRACE:
		return handleReqTrace(command, messageId, memberId, type, value, size, opt);
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
	unsigned char memoryBuffer[bufferSize * 2];
	unsigned char memoryTrace[traceSize * 2];

	// buffer
	flxBuffer buffer1 = flxCreateLinearBuffer(memoryBuffer, bufferSize, flxWriteToFile, stdout);
	flxBuffer buffer2 = flxCreateLinearBuffer(memoryBuffer + bufferSize, bufferSize, flxCompressFlz, buffer1);

	// trace
	trace = flxCreateTrace(0, MAX_ITEM_ID, MAX_ENTRY_SIZE, memoryTrace, traceSize, buffer1);

	flxAddHead(trace, "example", "flux example");

	flxSetBuffer(trace, buffer2);

	//parse input
	return flxParseControlInput(stdin, MAX_ENTRY_SIZE, handleCommands);
}

#ifdef __cplusplus
}
#endif

