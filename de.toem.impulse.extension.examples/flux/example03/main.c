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
#include "flux.h"
#include "math.h"

// buffers
#define MAX_ITEM_ID 20 // maximum id of scope/signal
#define MAX_ENTRY_SIZE 4096

int main() {

	int n;

	unsigned char i0;
	int i1;
	unsigned short i2;
	long long i3;
	float f1;
	double f2;

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
		flxAddScope(trace, 1, 0, "Integers", "Scope Description");
		flxAddSignal(trace, 2, 1, "a char", "Signal Description", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 3, 1, "an integer", "Signal Description", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 4, 1, "a short", "Signal Description", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 5, 1, "a llong", 0 /* no description*/, FLX_TYPE_INTEGER, 0);

		// add float signals
		flxAddScope(trace, 11, 0, "Floats", "another Scope");
		flxAddSignal(trace, 12, 11, "a float", 0, FLX_TYPE_FLOAT, 0);
		flxAddSignal(trace, 13, 11, "a double", 0, FLX_TYPE_FLOAT, 0);

		// open
		flxOpen(trace, 0, "ns", 0, 0);

		// generate example trace
		for (n = 0; n < 500000; n++) {

			// values
			i0 = n % 8;
			i1 = n;
			i2 = n * 3;
			i3 = n * n;
			f1 = sin(n / 1000.);
			f2 = cos(n / 100.);

			// write integer values of multiple types
			flxWriteIntAt(trace, 2, 0, n * 10, 0, &i0, sizeof(char), 0);
			flxWriteIntAt(trace, 3, 0, 0, 1, &i1, sizeof(int), 1);
			flxWriteIntAt(trace, 4, 0, 0, 1, &i2, sizeof(short), 0);
			flxWriteIntAt(trace, 5, 0, 0, 1, &i3, sizeof(long long), 1);

			// write float values of multiple types (5ns later)
			flxWriteFloatAt(trace, 12, 0, 5, 1, &f1, sizeof(float));
			flxWriteFloatAt(trace, 13, 0, 0, 1, &f2, sizeof(double));
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

