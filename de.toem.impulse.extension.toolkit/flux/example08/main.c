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
#include <string.h>
#include "flux.h"
#include "math.h"

// buffers
#define MAX_ITEM_ID 20 // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096

int main() {

	int n;

	int eVal0;
	int eVal1;

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

	// add event signals
	flxAddScope(trace, 1, 0, "Enums", "Scope Description");
	flxAddSignal(trace, 2, 1, "enum0", 0, FLX_TYPE_EVENT, "gantt<>");
	flxAddSignal(trace, 3, 1, "enum1", 0, FLX_TYPE_EVENT, "gantt<>");
	flxAddSignal(trace, 4, 1, "enum2", 0, FLX_TYPE_EVENT, "gantt<>");

	// open
	flxOpen(trace, 0, "ns", 0, 0);

	// write enums for signal 2,3,4 (events)
	flxWriteEnumDef(trace, 2, FLX_ENUM_GLOBAL, "Started", 0xfff1);
	flxWriteEnumDef(trace, 2, FLX_ENUM_GLOBAL, "Running", 2);
	flxWriteEnumDef(trace, 3, FLX_ENUM_GLOBAL, "Reading", 1);
	flxWriteEnumDef(trace, 3, FLX_ENUM_GLOBAL, "Writing", 2);
	flxWriteEnumDef(trace, 4, FLX_ENUM_GLOBAL, "Fetching", 1);
	flxWriteEnumDef(trace, 4, FLX_ENUM_GLOBAL, "Pushing", 2);

	// write relation and label related enums for signal 2,4 (events)
	flxWriteEnumDef(trace, 2, FLX_ENUM_RELATION_STYLE, "Trigger/444444/line/no", 1);
	flxWriteEnumDef(trace, 2, FLX_ENUM_RELATION_STYLE, "Reference/00ff00/cubic/normal", 2);
	flxWriteEnumDef(trace, 2, FLX_ENUM_RELATION_TARGET, "\\Enums\\enum1", 1);
	flxWriteEnumDef(trace, 2, FLX_ENUM_RELATION_TARGET, "\\Enums\\enum2", 2);
	flxWriteEnumDef(trace, 4, FLX_ENUM_LABEL_STYLE, "My label/444444/star", 1);

		// generate example trace
		for (n = 0; n < 50000; n++) {

			// write at n * 100ns
			flxWriteEventAt(trace, 2, 0, n * 100, 0, 0xfff1);
			flxWriteRelation(trace, 2, 1, 1, 25);
			flxWriteEventAt(trace, 3, 0, 0, 1, 0);
			flxWriteEventAt(trace, 4, 0, 0, 1, 0);
			flxWriteLabel(trace, 4, 1, 5,5);

			// write at +25ns
			flxWriteEventAt(trace, 3, 0, 25, 1, 1);

			// write at +5ns
			flxWriteEventAt(trace, 4, 0, 5, 1, 1);

			// write at +20ns
			flxWriteEventAt(trace, 2, 0, 20, 1, 1);
			flxWriteRelation(trace, 2, 2, 2, 20);
			flxWriteEventAt(trace, 3, 0, 0, 1, 2);
			flxWriteEventAt(trace, 4, 0, 0, 1, 2);

			// write at +20ns
			flxWriteEventAt(trace, 2, 0, 20, 1, 2);

			// write at +20ns
			flxWriteEventAt(trace, 2, 0, 20, 1, 0);

		}

		// close
		flxClose(trace, 0, n * 100);
	}

	// flush buffers
	flxFlushBuffer(buffer);
	fclose(out);
}

#ifdef __cplusplus
}
#endif

