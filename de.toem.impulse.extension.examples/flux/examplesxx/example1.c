// gcc example1.c flux.c lz4.c -lm -o example1 -g -D FLX_COMPRESS -D FLX_STDIO
/*******************************************************************************
 * Copyright (c) 2012-2016
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


int main() {

  int n;
  int i1;
  unsigned short i2;
  long long i3;
  float f1;
  double f2;
  int ia[2];
  float fa[2];
  flxuint ea[2];

  // buffers
#define MAX_ITEMS 100
#define BUFFER_SIZE 4096
#define TRACE_SIZE FLX_TRACE_BYTES(1,MAX_ITEMS)

  unsigned char memoryBuffer[BUFFER_SIZE*2];
  unsigned char memoryTrace[TRACE_SIZE];

  FILE *ptr_myfile = fopen("test.trace", "wb");

  flxBuffer buffer2 = flxCreateFixedBuffer(memoryBuffer+BUFFER_SIZE, BUFFER_SIZE, flxWriteToFile, ptr_myfile);
  flxBuffer buffer1 = flxCreateFixedBuffer(memoryBuffer, BUFFER_SIZE, flxCompress, buffer2);
  flxTrace trace = flxCreateTrace(0, MAX_ITEMS, BUFFER_SIZE, memoryTrace, TRACE_SIZE, buffer2);
  if (trace != 0) {
    flxAddHead(trace, "huh", "ooh");
    flxSetBuffer(trace, buffer1);
    flxAddScope(trace, 1, 0, "Integers", "yep");
    flxAddSignal(trace, 2, 1, "integer", "desc", FLX_TYPE_INTEGER, 0);
    flxAddSignal(trace, 3, 1, "short", "desc", FLX_TYPE_INTEGER, 0);
    flxAddSignal(trace, 4, 1, "llong", "desc", FLX_TYPE_INTEGER, 0);
    flxAddSignal(trace, 5, 1, "event", "desc", FLX_TYPE_EVENT, 0);
    flxAddScope(trace, 11, 0, "Floats", "yep");
    flxAddSignal(trace, 12, 11, "float", "desc", FLX_TYPE_FLOAT, 0);
    flxAddSignal(trace, 13, 11, "double", "desc", FLX_TYPE_FLOAT, 0);
    flxAddScope(trace, 21, 0, "Logics", "yep");
    flxAddSignal(trace, 22, 21, "bit", "desc", FLX_TYPE_LOGIC, 0);
    flxAddSignal(trace, 23, 21, "vector", "desc", FLX_TYPE_LOGIC, "default<bits=16>");

    flxAddScope(trace, 31, 0, "Structs", "yep");
    flxAddSignal(trace, 32, 31, "Simple Struct", "desc", FLX_TYPE_STRUCT, 0);
    struct flxMemberValueStruct members[4];
    flxInitMember(members+0,0,"m0",FLX_STRUCTTYPE_ENUM,0);
    flxInitMember(members+1,1,"m1",FLX_STRUCTTYPE_INTEGER,"default<df=Hex>");
    flxInitMember(members+2,2,"m2",FLX_STRUCTTYPE_FLOAT,0);
    flxInitMember(members+3,3,"m3",FLX_STRUCTTYPE_TEXT,0);

    flxAddScope(trace, 41, 0, "Arrays", "yep");
    flxAddSignal(trace, 42, 41, "integer[2]", "desc", FLX_TYPE_INTEGER, "default<dim=2>");
    flxAddSignal(trace, 43, 41, "float[2]", "desc", FLX_TYPE_FLOAT, "default<dim=2>");
    flxAddSignal(trace, 44, 41, "event[2]", "desc", FLX_TYPE_EVENT, "default<dim=2>");

    //flxAddScatteredSignal(trace, 24, 21, "dig", "desc", FLX_TYPE_LOGIC, 0,0);
    //flxAddScatteredSignal(trace, 25, 21, "dig", "desc", FLX_TYPE_LOGIC, 1,1);
    flxOpen(trace, 0, "ms", 0, 0);

    // write enums for signal 5 (event)
    flxWriteEnumDef(trace, 5, FLX_ENUM_GLOBAL, "Yes", 1);
    flxWriteEnumDef(trace, 5, FLX_ENUM_GLOBAL, "No", 0);

    // write enums/members for signal 32 (struct)
    flxWriteMemberDefs(trace, 32, members,4);
    flxWriteEnumDef(trace, 32, FLX_ENUM_GLOBAL, "Yes", 1);
    flxWriteEnumDef(trace, 32, FLX_ENUM_GLOBAL, "No", 0);

    flxWriteArrayDef(trace, 42, 0, "x");
    flxWriteArrayDef(trace, 42, 1, "y");
    flxWriteArrayDef(trace, 43, 0, "x");
    flxWriteArrayDef(trace, 43, 1, "y");
    flxWriteArrayDef(trace, 44, 0, "state");
    flxWriteArrayDef(trace, 44, 1, "done");

    for (n = 0; n < 500000; n++) {
      i1 = n;
      i2 = n;
      i3 = n * n;
      flxWriteIntAt(trace, 2, 0, n * 10, 0, &i1, sizeof(int), 0);
      flxWriteIntAt(trace, 3, 0, 0, 1, &i2, sizeof(short), 1);
      flxWriteIntAt(trace, 4, 0, 0, 1, &i3, sizeof(long long), 1);
      flxWriteEventAt(trace, 5, 0, 0, 1, n % 2);

      f1 = sin(n / 1000);
      f2 = cos(n / 100);
      if (n == 100000) {
        flxWriteNoneAt(trace, 12, 0, n * 10 + 7, 0);
        flxWriteNoneAt(trace, 13, 0, 0, 1);
      } else if (n < 100000 || n >= 200000) {
        flxWriteFloatAt(trace, 12, 0, n * 10 + 7, 0, &f1, sizeof(float));
        flxWriteFloatAt(trace, 13, 0, 0, 1, &f2, sizeof(double));
      }
      flxWriteLogicTextAt(trace, 22, 0, n * 10 + 8, 0, 0, "1", 1);
      flxWriteLogicTextAt(trace, 23, 0, 0, 1, 0, "0011x1", 6);
      //flxWriteLogicTextAt(trace, 24, 0, 0, 1, 0, "u", 1);
      //flxWriteLogicTextAt(trace, 25, 0, 0, 1, 0, "x", 1);
      flxSetMember(members+0,&i1, sizeof(int),0,1);
      flxSetMember(members+1,&i2, sizeof(short),0,1);
      flxSetMember(members+2,&f1, sizeof(float),0,1);
      flxSetMember(members+3,(void*)"haha", 4,0,1);
      flxWriteMembersAt(trace, 32, 0, 0, 1,members,4);

      ia[0]=i1;
      ia[1]=i2;
      flxWriteIntArrayAt(trace, 41, 0, n * 10, 0, &i1, sizeof(int), 0,2);
      fa[0] = f1;
      fa[1] = f1;
      flxWriteFloatArrayAt(trace, 42, 0, n * 10 + 7, 0, &f1, sizeof(float),2);
      ea[0] = n%2;
      ea[1] = (n+1)%2;
      flxWriteEventArrayAt(trace, 43, 0, 0, 1, ea,2);
    }
    flxClose(trace, 0, n * 10);
  }
  flxFlushBuffer(buffer1);
  flxFlushBuffer(buffer2);
  fclose(ptr_myfile);
}

#ifdef __cplusplus
}
#endif

