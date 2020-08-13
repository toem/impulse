/*
Copyright 2011-2013 Frederic Langlet
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package kanzi.function;

import kanzi.ByteFunction;
import kanzi.IndexedByteArray;


public class NullFunction implements ByteFunction
{
   private final int size;


   public NullFunction()
   {
      this(0);
   }


   public NullFunction(int size)
   {
      if (size < 0)
         throw new IllegalArgumentException("Invalid size parameter (must be at least 0)");

      this.size = size;
   }


   public int size()
   {
      return this.size;
   }

   
   @Override
   public boolean forward(IndexedByteArray source, IndexedByteArray destination)
   {
      return doCopy(source, destination, this.size);
   }
  

   @Override
   public boolean inverse(IndexedByteArray source, IndexedByteArray destination)
   {    
      return doCopy(source, destination, this.size);
   }

   
   private static boolean doCopy(IndexedByteArray source, IndexedByteArray destination, int sz)
   {      
      final int len = (sz == 0) ? source.array.length : sz;

      if (source.index + len > source.array.length)
         return false;
      
      if (destination.index + len > destination.array.length)
         return false;

      if ((source.array == destination.array) && (source.index == destination.index))
      {
         source.index += len;
         destination.index += len;
         return true;
      }

      if (source.array == destination.array)
      {
         final int len8 = len & - 8;
         final byte[] srcArray = source.array;
         final byte[] dstArray = destination.array;
         int srcIdx = source.index;
         int dstIdx = destination.index;
         
         for (int i=0; i<len8; i+=8)   
         {
            dstArray[dstIdx]   = srcArray[srcIdx];
            dstArray[dstIdx+1] = srcArray[srcIdx+1];
            dstArray[dstIdx+2] = srcArray[srcIdx+2];
            dstArray[dstIdx+3] = srcArray[srcIdx+3];
            dstArray[dstIdx+4] = srcArray[srcIdx+4];
            dstArray[dstIdx+5] = srcArray[srcIdx+5];
            dstArray[dstIdx+6] = srcArray[srcIdx+6];
            dstArray[dstIdx+7] = srcArray[srcIdx+7]; 
            srcIdx += 8;
            dstIdx += 8;
         }
         
         for (int i=len8; i<len; i++)
            dstArray[destination.index+i] = srcArray[source.index+i];
      }
      else
      {
         System.arraycopy(source.array, source.index, destination.array, destination.index, len);
      }
      
      source.index += len;
      destination.index += len;
      return true;
   }
   
   
   @Override
   public int getMaxEncodedLength(int srcLen)
   {
      return srcLen;
   }   
}