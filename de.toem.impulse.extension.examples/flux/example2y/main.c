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
#include <math.h>
#include "flux.h"

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#endif

// geometry
#define MAX_ITEM_ID (8*8*8*8) // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096

// trace object
flxTrace trace;

flxresult handleReqScheme(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {
		unsigned version = 1;
		struct flxMemberValueStruct members[0];
		flxInitMember(members + 0, 0, "version", FLX_STRUCTTYPE_INTEGER, 0);
		flxSetMember(members, &version, sizeof(unsigned), 0, 1);
		flxWriteControlResult(trace, FLX_CONTROL_DB_O_REQ_SCHEME, memberId, members, 1);
	}
	return FLX_OK;
}

void addItems(flxid parent, flxid level, flxid id) {
	int n;
	flxchar name[8] = "aaaname";
	flxchar description[8] = "aaadescr";
	for (n = 0; n < 4; n++) {
		name[n] = (id >> (n * 3)) & 0x7;
		description[n] = (id >> (n * 3)) & 0x7;
	}
	if (level == 3) {
		flxAddSignal(trace, id + 1, parent, name, description, FLX_TYPE_INTEGER, 0);
	} else {
		flxAddScope(trace, id + 1, parent, name, description);

		level++;
		for (n = 0; n < 8; n++) {
			flxid nid = id | (n << (3 * level));
			addItems(id, level, nid);
		}
	}
}
flxresult handleReqItems(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {
	int n;
	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

		flxAddHead(trace, "example", "flux example");
		for (n = 0; n < 8; n++)
			addItems(0, 0, n);
		flxWriteControlResult(trace, FLX_CONTROL_DB_I_REQ_ITEMS, memberId, 0, 0);
		flxFlush(trace);
	}

	return FLX_OK;
}

flxresult handleReqTrace(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
		flxuint *opt) {

	FLX_CONTROL_HANDLE_INTEGER_PARAMETER(0, signals, unsigned, 0, 0)
	FLX_CONTROL_HANDLE_BINARY_PARAMETER(1, bVal, 256)

	if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE) {

	}
	return FLX_OK;
}

flxresult handleCommands(flxbyte command, flxid controlId, flxid messageId, flxid memberId, flxbyte type, void **value,
		flxuint *size, flxuint *opt) {
	switch (controlId) {
	case FLX_CONTROL_DB_O_REQ_SCHEME:
		return handleReqScheme(command, messageId, memberId, type, value, size, opt);
	case FLX_CONTROL_DB_I_REQ_ITEMS:
		return handleReqItems(command, messageId, memberId, type, value, size, opt);
	case FLX_CONTROL_DB_I_REQ_TRACE:
		return handleReqTrace(command, messageId, memberId, type, value, size, opt);
	}
	return FLX_ERROR_COMMAND_PARSE_ERROR;
}

void *controlThread(void *data) {

	return 0;
}

int main() {

#ifdef _WIN32
	setmode(fileno(stdout),O_BINARY);
	setmode(fileno(stdin),O_BINARY);
#endif

	pthread_t thread;

	if (pthread_create(&thread, NULL, controlThread, 0)) {

		printf("Error creating thread\n");
		return 1;
	}

// calculate required memory for trace and buffers
	unsigned bufferSize = FLX_BUFFER_BYTES(MAX_ENTRY_SIZE);
	unsigned traceSize = FLX_TRACE_BYTES(0, MAX_ITEM_ID);

// static memory
	unsigned char memoryBuffer[bufferSize];
	unsigned char memoryTrace[traceSize];

// buffer
	flxBuffer buffer = flxCreateFixedBuffer(memoryBuffer, bufferSize, flxWriteToFile, stdout);

// trace
	trace = flxCreateTrace(0, MAX_ITEM_ID, MAX_ENTRY_SIZE, memoryTrace, traceSize, buffer);
// head
	flxAddHead(trace, "example", "flux example");

// add signals
//flxAddSignal(trace, 1, 0, "integer", "an integer", FLX_TYPE_INTEGER, 0);
//flxAddSignal(trace, 2, 0, "float", "a float", FLX_TYPE_FLOAT, 0);

	/*
	 // open
	 flxOpen(trace, 0, "ns", 0, 0);

	 // generate example trace
	 int n;
	 int iVal;
	 float fVal;
	 for (n = 0; n < 500; n++) {

	 // time in ns
	 flxdomain current = n * 10;

	 // integer
	 iVal = n % 444;
	 flxWriteIntAt(trace, 1, 0, current, 0, &iVal, sizeof(int), 0);

	 // float - same time - use domain=0; isDelta=1
	 fVal = sin(n / 1000.0);
	 flxWriteFloatAt(trace, 2, 0, current, 0, &fVal, sizeof(float));

	 flxFlush(trace);
	 sleep(1);

	 }

	 // close
	 flxClose(trace, 0, n * 10);

	 // flush buffers
	 flxFlush(trace);
	 */

	flxParseControlInput(stdin, 128, handleCommands);

	pthread_join(thread, NULL);

}

#ifdef __cplusplus
}
#endif

