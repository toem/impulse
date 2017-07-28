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

	int eVal;
	char tVal[32];
	double fVal;

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

		// add integer signals
		flxAddScope(trace, 1, 0, "Other", "Scope Description");
		flxAddSignal(trace, 2, 1, "a text", "Signal Description", FLX_TYPE_TEXT, 0);
		flxAddSignal(trace, 3, 1, "an enumeration event", "Signal Description", FLX_TYPE_EVENT, 0);
		flxAddSignal(trace, 4, 1, "a binary", "Signal Description", FLX_TYPE_BINARY, 0);

		// open
		flxOpen(trace, 0, "ns", 0, 0);

		// write enums for signal 3 (event)
		flxWriteEnumDef(trace, 3, FLX_ENUM_GLOBAL, "Yes", 1);
		flxWriteEnumDef(trace, 3, FLX_ENUM_GLOBAL, "No", 0);

		// generate example trace
		for (n = 0; n < 500000; n++) {

			// values
			eVal = n & 1;
			fVal = cos(n / 100.);
			sprintf(tVal, "val: %f", fVal);

			// writes a text
			flxWriteTextAt(trace, 2, 0, n * 10, 0, tVal, strlen(tVal));

			// write an enum event yes/no
			flxWriteEventAt(trace, 3, 0, 0, 1, eVal);

			// write the bytes of a double value
			flxWriteBinaryAt(trace, 4, 0, 0, 1, (flxbptr) &fVal, 8);
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

