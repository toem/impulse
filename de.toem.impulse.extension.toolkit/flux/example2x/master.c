
/*******************************************************************************
 * Copyright (c) 2012-2019 Thomas Haber
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
#include <math.h>
#include "flux.h"

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#endif

int main()
{

#ifdef _WIN32
    setmode(fileno(stdout),O_BINARY);
    setmode(fileno(stdin),O_BINARY);
#endif

    // buffers
#define BUFFER_SIZE 4096

    unsigned char memoryBuffer[BUFFER_SIZE];
    flxBuffer buffer = flxCreateFixedBuffer(memoryBuffer, BUFFER_SIZE, flxWriteToFile, (void*)stdout);

    struct flxMemberValueStruct members[4];
    flxInitMember(members + 0, 0, "m0", FLX_STRUCTTYPE_ENUM, 0);
    flxInitMember(members + 1, 1, "m1", FLX_STRUCTTYPE_INTEGER, 0);
    flxInitMember(members + 2, 2, "m2", FLX_STRUCTTYPE_TEXT, 0);
    flxInitMember(members + 3, 3, "m3", FLX_STRUCTTYPE_FLOAT, 0);

    int n = 0;
    int i1 = 4;
    short i2 = 5;
    float f1 = 3.14;
    char* t1 = "haha";

    for (n = 0; n < 100; n++)
    {
        //usleep(2);
        i1=n;
        flxSetMember(members + 0, &i1, sizeof(int), 0, 1);
        flxSetMember(members + 1, &i2, sizeof(short), 0, 1);
        flxSetMember(members + 2, t1, 4, 0, 1);
        flxSetMember(members + 3, &f1, sizeof(float), 0, 1);

        flxWriteControlReqEntry(buffer, 1, 1, members, 4);

        flxFlushBuffer(buffer);
    }
}

#ifdef __cplusplus
}
#endif


