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

#include <stdio.h>
#include <string.h>
#include "flux.h"
#include "math.h"

// buffers
#define MAX_ITEM_ID 20 // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096

int main() {

	int n;

	int iaVal[2];
	float faVal[2];
	flxuint eaVal[2];
	char taVal[32][2];

	// output file
	FILE *out = fopen("flux.example.recTr", "wb");

	// calculate required memory for trace and buffers
	unsigned bufferSize = FLX_BUFFER_BYTES(MAX_ENTRY_SIZE);
	unsigned traceSize = FLX_TRACE_BYTES(0, MAX_ITEM_ID);

	// static memory
	unsigned char memoryBuffer[bufferSize];
	unsigned char memoryTrace[traceSize];

	// buffer
	flxBuffer buffer = flxCreateLinearBuffer(memoryBuffer, bufferSize, flxWriteToFile, out);

	// trace
	flxTrace trace = flxCreateTrace(0, MAX_ITEM_ID, MAX_ENTRY_SIZE, memoryTrace, traceSize, buffer);

	if (trace != 0) {

		// head
		flxAddHead(trace, "example", "flux example");

		// array signals
		flxAddScope(trace, 1, 0, "Arrays", "Scope Description");
		flxAddSignal(trace, 2, 1, "integer array", "2 elements", FLX_TYPE_INTEGER_ARRAY, "default<dim=2>");
		flxAddSignal(trace, 3, 1, "float array", "2 elements", FLX_TYPE_FLOAT_ARRAY, "default<dim=2>");
		flxAddSignal(trace, 4, 1, "event array", "2 elements", FLX_TYPE_EVENT_ARRAY, "default<dim=2>");

		// open
		flxOpen(trace, 0, "ns", 0, 0);

		// write array defs for arrays (may be omitted)
		flxWriteArrayDef(trace, 2, 0, "x");
		flxWriteArrayDef(trace, 2, 1, "y");
		flxWriteArrayDef(trace, 3, 0, "x");
		flxWriteArrayDef(trace, 3, 1, "y");
		flxWriteArrayDef(trace, 4, 0, "state");
		flxWriteArrayDef(trace, 4, 1, "done");

		// write enums for for signal 4  (enum array)
		flxWriteEnumDef(trace, 4, FLX_ENUM_MEMBER_0 + 0, "Yes", 1);
		flxWriteEnumDef(trace, 4, FLX_ENUM_MEMBER_0 + 0, "No", 0);
		flxWriteEnumDef(trace, 4, FLX_ENUM_MEMBER_0 + 1, "Low", 1);
		flxWriteEnumDef(trace, 4, FLX_ENUM_MEMBER_0 + 1, "High", 0);

		// generate example trace
		for (n = 0; n < 500000; n++) {

			// integer array
			iaVal[0] = n % 16;
			iaVal[1] = n % 1024;
			flxWriteIntArrayAt(trace, 2, 0, n * 10, 0, iaVal, sizeof(int), 0, 2);

			// float array
			faVal[0] = sin(n / 1000.);
			faVal[1] = cos(n / 100.);
			flxWriteFloatArrayAt(trace, 3, 0, 0, 1, faVal, sizeof(float), 2);

			// event array
			eaVal[0] = n % 2;
			eaVal[1] = (n + 1) % 2;
			flxWriteEventArrayAt(trace, 4, 0, 0, 1, eaVal, 2);

		}

		// close
		flxClose(trace, 0, n * 10);
	}

	// flush buffers
	flxFlushBuffer(buffer);
	fclose(out);
}

#ifdef __cplusplus
}
#endif

