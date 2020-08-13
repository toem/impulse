// gcc example3.c flux.c lz4.c -lm -o example3 -g -D FLX_COMPRESS -D FLX_STDIO
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

int main()
{

    int n;

    // buffers
#define MAX_ITEMS 100
#define BUFFER_SIZE 4096
#define TRACE_SIZE FLX_TRACE_BYTES(1,MAX_ITEMS)

    unsigned char memoryBuffer[BUFFER_SIZE * 2];
    unsigned char memoryTrace[TRACE_SIZE];

    FILE *ptr_myfile = fopen("example3.trace", "wb");

    flxBuffer buffer2 = flxCreateFixedBuffer(memoryBuffer + BUFFER_SIZE, BUFFER_SIZE, flxWriteToFile, ptr_myfile);
    flxBuffer buffer1 = flxCreateFixedBuffer(memoryBuffer, BUFFER_SIZE, flxCompress, buffer2);
    flxTrace trace = flxCreateTrace(0, MAX_ITEMS, BUFFER_SIZE, memoryTrace, TRACE_SIZE, buffer2);
    if (trace != 0)
    {
        flxAddHead(trace, "huh", "ooh");
        flxSetBuffer(trace, buffer1);
        flxAddScope(trace, 21, 0, "Logics", "yep");
        flxAddScatteredSignal(trace, 22, 21, "bits", "desc", FLX_TYPE_LOGIC, 0,0,0);
        flxAddScatteredSignal(trace, 23, 21, "bits", "desc", FLX_TYPE_LOGIC, 0,1,1);
        flxAddScatteredSignal(trace, 24, 21, "bits", "desc", FLX_TYPE_LOGIC, 0,2,3);


        flxAddScope(trace, 31, 0, "Logics Ref", "yep");
        flxAddScatteredSignalReference(trace, 22, 31, "bits", "desc" ,0,0);
        flxAddScatteredSignalReference(trace, 23, 31, "bits", "desc",1,1);
        flxAddScatteredSignalReference(trace, 24, 31, "bits", "desc",2,3);

        flxOpen(trace, 0, "ms", 0, 0);

        for (n = 0; n < 500000; n++)
        {
            flxWriteLogicTextAt(trace, 22, 0, n * 10 + 0, 0, 0, n%2==0 ?"1":"0", 1);
            flxWriteLogicTextAt(trace, 23, 0, n * 10 + 5, 0, 0, n%3==0?"0":"Z", 1);
            flxWriteLogicTextAt(trace, 24, 0, n * 10 + 6, 0, 0, (n%4)?"10":"00", 1);
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


