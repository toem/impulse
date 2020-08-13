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
#include "flux.h"
#include "math.h"

// buffers
#define MAX_ITEM_ID 100 // maximum no of scopes and signals
#define MAX_ENTRY_SIZE 4096

int main() {

	int n;
	int i0;
	int i1;
	unsigned short i2;
	long long i3;
	float f1;
	double f2;
	int ia[2];
	float fa[2];
	flxuint ea[2];

	// output file
	FILE *out = fopen("flux.example.recTr", "wb");

	// buffer & trace memory
	unsigned bufferSize = FLX_BUFFER_BYTES(MAX_ENTRY_SIZE);
	unsigned traceSize = FLX_TRACE_BYTES(1, MAX_ITEM_ID);
	unsigned char memoryBuffer[bufferSize * 3]; // for 3 buffers
	unsigned char memoryTrace[traceSize];

	// buffers
	flxBuffer buffer3 = flxCreateLinearBuffer(memoryBuffer + bufferSize * 2, bufferSize, flxWriteToFile, out); // writes to file
	flxBuffer buffer2 = flxCreateLinearBuffer(memoryBuffer + bufferSize * 1, bufferSize, flxCompressLz4, buffer3); // compresses to buffer 3
	flxBuffer buffer1 = flxCreateLinearBuffer(memoryBuffer + bufferSize * 0, bufferSize, flxCompressFlz, buffer2); // compresses to buffer 2

	// trace
	flxTrace trace = flxCreateTrace(0, MAX_ITEM_ID, MAX_ENTRY_SIZE, memoryTrace, traceSize, buffer3);

	if (trace != 0) {

		// head - uncompressed
		flxAddHead(trace, "example", "flux example");

		// active compression
		flxSetBuffer(trace, buffer1);

		// integer/event signals
		flxAddScope(trace, 1, 0, "Integers", "yep");
		flxAddSignal(trace, 2, 1, "integer", "desc", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 3, 1, "short", "desc", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 4, 1, "llong", "desc", FLX_TYPE_INTEGER, 0);
		flxAddSignal(trace, 5, 1, "event", "desc", FLX_TYPE_EVENT, 0);

		// float signals
		flxAddScope(trace, 11, 0, "Floats", "yep");
		flxAddSignal(trace, 12, 11, "float", "desc", FLX_TYPE_FLOAT, 0);
		flxAddSignal(trace, 13, 11, "double", "desc", FLX_TYPE_FLOAT, 0);

		// logic signals
		flxAddScope(trace, 21, 0, "Logics", "yep");
		flxAddSignal(trace, 22, 21, "bit", "desc", FLX_TYPE_LOGIC, 0);
		flxAddSignal(trace, 23, 21, "vector", "desc", FLX_TYPE_LOGIC, "default<bits=16>");
		flxAddScatteredSignal(trace, 24, 21, "scattered", 0, FLX_TYPE_LOGIC, 0, 0, 0);
		flxAddScatteredSignal(trace, 25, 21, "scattered", 0, FLX_TYPE_LOGIC, 0, 1, 1);

		// struct signals
		flxAddScope(trace, 31, 0, "Structs", "yep");
		flxAddSignal(trace, 32, 31, "Simple Struct", "desc", FLX_TYPE_STRUCT, 0);
		struct flxMemberValueStruct members[4];
		flxInitMember(members + 0, 0, "m0", FLX_STRUCTTYPE_ENUM, 0);
		flxInitMember(members + 1, 1, "m1", FLX_STRUCTTYPE_INTEGER, "default<df=Hex>");
		flxInitMember(members + 2, 2, "m2", FLX_STRUCTTYPE_FLOAT, 0);
		flxInitMember(members + 3, 3, "m3", FLX_STRUCTTYPE_TEXT, 0);

		// array signals
		flxAddScope(trace, 41, 0, "Arrays", "yep");
		flxAddSignal(trace, 42, 41, "integer[2]", "desc",
		FLX_TYPE_INTEGER_ARRAY, "default<dim=2>");
		flxAddSignal(trace, 43, 41, "float[2]", "desc", FLX_TYPE_FLOAT_ARRAY, "default<dim=2>");
		flxAddSignal(trace, 44, 41, "event[2]", "desc", FLX_TYPE_EVENT_ARRAY, "default<dim=2>");

		// open
		flxOpen(trace, 0, "ms", 0, 0);

		// write enums for signal 5 (event)
		flxWriteEnumDef(trace, 5, FLX_ENUM_GLOBAL, "Yes", 1);
		flxWriteEnumDef(trace, 5, FLX_ENUM_GLOBAL, "No", 0);

		// write enums/members for signal 32 (struct)
		flxWriteMemberDefs(trace, 32, members, 4);
		flxWriteEnumDef(trace, 32, FLX_ENUM_GLOBAL, "Yes", 1);
		flxWriteEnumDef(trace, 32, FLX_ENUM_GLOBAL, "No", 0);

		// write member defs for arrays
		flxWriteArrayDef(trace, 42, 0, "x");
		flxWriteArrayDef(trace, 42, 1, "y");
		flxWriteArrayDef(trace, 43, 0, "x");
		flxWriteArrayDef(trace, 43, 1, "y");
		flxWriteArrayDef(trace, 44, 0, "state");
		flxWriteArrayDef(trace, 44, 1, "done");

		// write enums for for signal 44  (enum array)
		flxWriteEnumDef(trace, 44, FLX_ENUM_MEMBER_0 + 0, "Yes", 1);
		flxWriteEnumDef(trace, 44, FLX_ENUM_MEMBER_0 + 0, "No", 0);
		flxWriteEnumDef(trace, 44, FLX_ENUM_MEMBER_0 + 1, "Low", 1);
		flxWriteEnumDef(trace, 44, FLX_ENUM_MEMBER_0 + 1, "High", 0);

		// generate example trace
		for (n = 0; n < 500000; n++) {

			// integer
			i0 = n & 1;
			i1 = n / 2;
			i2 = n / 4;
			i3 = i1 * i2;

			flxWriteIntAt(trace, 2, 0, n * 10, 0, &i1, sizeof(int), 0);
			flxWriteIntAt(trace, 3, 0, 0, 1, &i2, sizeof(short), 1);
			flxWriteIntAt(trace, 4, 0, 0, 1, &i3, sizeof(long long), 1);
			flxWriteEventAt(trace, 5, 0, 0, 1, n % 2);

			// float
			f1 = sin(n / 4 / 1000.);
			f2 = cos(n / 4 / 100.);

			if (n == 100000) {
				flxWriteNoneAt(trace, 12, 0, n * 10 + 7, 0);
				flxWriteNoneAt(trace, 13, 0, 0, 1);
			} else if (n < 100000 || n >= 200000) {
				flxWriteFloatAt(trace, 12, 0, n * 10 + 7, 0, &f1, sizeof(float));
				flxWriteFloatAt(trace, 13, 0, 0, 1, &f2, sizeof(double));
			}

			// logic
			flxWriteLogicTextAt(trace, 22, 0, n * 10 + 8, 0, 0, "1", 1);
			flxWriteLogicTextAt(trace, 23, 0, 0, 1, 0, "0011x1", 6);
			flxWriteLogicTextAt(trace, 24, 0, 0, 1, 0, "u", 1);
			flxWriteLogicTextAt(trace, 25, 0, 0, 1, 0, "x", 1);

			// struct
			flxSetMember(members + 0, &i0, sizeof(int), 0, 1);
			flxSetMember(members + 1, &i2, sizeof(short), 0, 1);
			flxSetMember(members + 2, &f1, sizeof(float), 0, 1);
			flxSetMember(members + 3, (void*) "haha", 4, 0, 1);
			flxWriteMembersAt(trace, 32, 0, 0, 1, members, 4);

			// integer array
			ia[0] = i1;
			ia[1] = i2;
			flxWriteIntArrayAt(trace, 42, 0, 0, 1, ia, sizeof(int), 0, 2);

			// float array
			fa[0] = f1;
			fa[1] = f2;
			flxWriteFloatArrayAt(trace, 43, 0, 0, 1, fa, sizeof(float), 2);

			// ecent array
			ea[0] = n % 2;
			ea[1] = (n + 1) % 2;
			flxWriteEventArrayAt(trace, 44, 0, 0, 1, ea, 2);
		}

		// close
		flxClose(trace, 0, n * 10);
	}

	// flush buffers
	//flxFlushBuffer(buffer0);
	flxFlushBuffer(buffer1);
	flxFlushBuffer(buffer2);
	flxFlushBuffer(buffer3);
	fclose(out);
}

#ifdef __cplusplus
}
#endif

