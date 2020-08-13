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

		// add struct signals
		flxAddScope(trace, 1, 0, "Other", "Scope Description");
		flxAddSignal(trace, 2, 1, "Simple Struct", "desc", FLX_TYPE_STRUCT, 0);

		// open
		flxOpen(trace, 0, "ns", 0, 0);

		// write member defs for signal 2 (struct)
		struct flxMemberValueStruct members[4];
		flxInitMember(members + 0, 0, "m0", FLX_STRUCTTYPE_ENUM, 0);
		flxInitMember(members + 1, 1, "m1", FLX_STRUCTTYPE_INTEGER, "default<df=Hex>");
		flxInitMember(members + 2, 2, "m2", FLX_STRUCTTYPE_FLOAT, 0);
		flxInitMember(members + 3, 3, "m3", FLX_STRUCTTYPE_TEXT, 0);
		flxWriteMemberDefs(trace, 2, members, 4);

		// write enum defs for signal 2 (struct)
		flxWriteEnumDef(trace, 2, FLX_ENUM_GLOBAL, "Yes", 1);
		flxWriteEnumDef(trace, 2, FLX_ENUM_GLOBAL, "No", 0);

		// generate example trace
		for (n = 0; n < 500000; n++) {

			// values
			eVal = n & 1;
			fVal = cos(n / 100.);
			sprintf(tVal, "val: %f", fVal);

			// fill struct members and write
			flxSetMember(members + 0, &eVal, sizeof(int), 0, 1);
			flxSetMember(members + 1, &n, sizeof(int), 0, 1);
			flxSetMember(members + 2, &fVal, sizeof(double), 0, 1);
			flxSetMember(members + 3, (void*) tVal, strlen(tVal), 0, 1);
			flxWriteMembersAt(trace, 2, 0, n * 10, 0, members, 4);
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

