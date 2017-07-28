// gcc example2c.c flux.c lz4.c -lm -o example2c -g -D FLX_COMPRESS -D FLX_STDIO -D FLX_CONTROL
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

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#endif

flxresult handleCommand1(flxbyte command, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size, flxuint *opt)
{

    static unsigned i1 = 0;
    static short i2 = 0;
    static float f1 = 0;
    static char t1[32];
    static flxuint n = 0;
    // start
    if (command == FLX_CONTROL_HANDLE_ENTER_MESSAGE)
    {
        printf("enter command1 %i\n", messageId);

        i1 = 0;
        i2 = 0;
        f1 = 0;
        t1[32];
    }
    // requests member structure
    else if (command == FLX_CONTROL_HANDLE_PARSE_PARAMETER)
    {

        printf("command1 param %i %i\n", memberId, type);
        if (memberId == 0 && type == FLX_STRUCTTYPE_ENUM)
        {
            *value = (void*)&i1;
            *size = sizeof(unsigned);
            *opt = 0; // signed
        }
        else if (memberId == 1 && type == FLX_STRUCTTYPE_INTEGER)
        {
            *value = (void*)&i2;
            *size = sizeof(short);
            *opt = 1; // signed
        }
        else if (memberId == 2 && type == FLX_STRUCTTYPE_TEXT && *size < 32)
        {
            *value = (void*)&t1;
            *size = 32;
        }
        else if (memberId == 3 && type == FLX_STRUCTTYPE_FLOAT && *size == 4)
        {
            *value = (void*)&f1;
            *size = 4;
        }
    }
    // member structure filled with data
    else if (command == FLX_CONTROL_HANDLE_LEAVE_MESSAGE)
    {
        printf("leave command1  %i %i %i %s %f\n", n++, i1, i2, t1, f1);
    }
    return FLX_OK;
}

flxresult handleControlParse(flxbyte command, flxid controlId, flxid messageId, flxid memberId, flxbyte type, void **value, flxuint *size,
                             flxuint *opt)
{
    if (controlId == 1)
        return handleCommand1(command, messageId, memberId, type, value, size, opt);
    return FLX_ERROR_COMMAND_PARSE_ERROR;
}
int main()
{

#ifdef _WIN32
    setmode(fileno(stdout),O_BINARY);
    setmode(fileno(stdin),O_BINARY);
#endif

    // buffers
#define MAX_ITEMS 100
#define BUFFER_SIZE 128
#define BUFFER_BYTE FLX_BUFFER_BYTES(BUFFER_SIZE)

    unsigned char memoryBuffer[BUFFER_BYTE];

    flxBuffer buffer = flxCreateFixedBuffer(memoryBuffer, BUFFER_BYTE, flxHandleControl, (void*)handleControlParse);

    //stdin=fopen("out", "rb");

    while (1)
    {
        flxbint avail;
        flxbptr bytes;
        flxbint read = 0;



        if (buffer->access(FLX_BUFFER_AVAIL, buffer, &avail, &bytes) == FLX_OK)
        {
            read = fread(bytes, 1, avail, stdin);
            if (read != 0)
            {
                printf("read %u\n", read);
                buffer->access(FLX_BUFFER_COMMIT, buffer, &read, 0);
                int r = flxFlushBuffer(buffer);
                printf("flushresult %i\n", r);
            }
        }
        else
            break;

    }
}

#ifdef __cplusplus
}
#endif


