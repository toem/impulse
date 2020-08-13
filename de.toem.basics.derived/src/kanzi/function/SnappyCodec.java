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
import kanzi.Sizeable;


// Snappy is a fast compression codec aiming for very high speed and
// reasonable compression ratios.
// This implementation is a port of the Go source at http://code.google.com/p/snappy-go/
public final class SnappyCodec implements ByteFunction, Sizeable
{
   private static final int MAX_OFFSET     = 32768;
   private static final int MAX_TABLE_SIZE = 16384;
   private static final int TAG_LITERAL    = 0x00;
   private static final int TAG_COPY1      = 0x01;
   private static final int TAG_COPY2      = 0x02;
   private static final int TAG_DEC_LEN1   = 0xF0;
   private static final int TAG_DEC_LEN2   = 0xF4;
   private static final int TAG_DEC_LEN3   = 0xF8;
   private static final int TAG_DEC_LEN4   = 0xFC;
   private static final byte TAG_ENC_LEN1  = (byte) (TAG_DEC_LEN1 | TAG_LITERAL);
   private static final byte TAG_ENC_LEN2  = (byte) (TAG_DEC_LEN2 | TAG_LITERAL);
   private static final byte TAG_ENC_LEN3  = (byte) (TAG_DEC_LEN3 | TAG_LITERAL);
   private static final byte TAG_ENG_LEN4  = (byte) (TAG_DEC_LEN4 | TAG_LITERAL);
   private static final byte B0            = (byte) (TAG_DEC_LEN4 | TAG_COPY2);
   private static final int HASH_SEED      = 0x1e35a7bd;

   private int size;
   private final int[] buffer;


   public SnappyCodec()
   {
      this(0);
   }


   public SnappyCodec(int size)
   {
      if (size < 0)
         throw new IllegalArgumentException("Invalid size parameter (must be at least 0)");

      this.size = size;
      this.buffer = new int[MAX_TABLE_SIZE];
   }


   @Override
   public int size()
   {
      return this.size;
   }

   
   @Override
   public boolean setSize(int sz)
   {
      if (size < 0)
         return false;
      
      this.size = sz;
      return true;
   }

   
   // emitLiteral writes a literal chunk and returns the number of bytes written.
   private static int emitLiteral(IndexedByteArray source, IndexedByteArray destination, int len)
   {
     final int srcIdx = source.index;
     int dstIdx = destination.index;
     final byte[] src = source.array;
     final byte[] dst = destination.array;
     final int n = len - 1;
     final int res;

     if (n < 60)
     {
        dst[dstIdx] = (byte) ((n<<2) | TAG_LITERAL);
        dstIdx++;
        res = len + 1;
        
        if (len <= 16)
        {
           if (len >= 4) 
           {
              dst[dstIdx]   = src[srcIdx];
              dst[dstIdx+1] = src[srcIdx+1];
              dst[dstIdx+2] = src[srcIdx+2];
              dst[dstIdx+3] = src[srcIdx+3];             
           }

           if (len >= 8) 
           {
              dst[dstIdx+4] = src[srcIdx+4];
              dst[dstIdx+5] = src[srcIdx+5];
              dst[dstIdx+6] = src[srcIdx+6];
              dst[dstIdx+7] = src[srcIdx+7];             
           }
           
           for (int i=0; i<len; i++)
              dst[dstIdx+i] = src[srcIdx+i];
           
           return res;
        }
     }
     else if (n < 0x0100)
     {
        dst[dstIdx]   = TAG_ENC_LEN1;
        dst[dstIdx+1] = (byte) n;
        dstIdx += 2;
        res = len + 2;
     }
     else if (n < 0x010000)
     {
        dst[dstIdx]   = TAG_ENC_LEN2;
        dst[dstIdx+1] = (byte) n;
        dst[dstIdx+2] = (byte) (n >> 8);
        dstIdx += 3;
        res = len + 3;
     }
     else if (n < 0x01000000)
     {
        dst[dstIdx]   = TAG_ENC_LEN3;
        dst[dstIdx+1] = (byte) n;
        dst[dstIdx+2] = (byte) (n >> 8);
        dst[dstIdx+3] = (byte) (n >> 16);
        dstIdx += 4;
        res = len + 4;
     }
     else
     {
        dst[dstIdx]   = TAG_ENG_LEN4;
        dst[dstIdx+1] = (byte) n;
        dst[dstIdx+2] = (byte) (n >> 8);
        dst[dstIdx+3] = (byte) (n >> 16);
        dst[dstIdx+4] = (byte) (n >> 24);
        dstIdx += 5;
        res = len + 5;
     }

     System.arraycopy(src, srcIdx, dst, dstIdx, len);
     return res;
  }


  // emitCopy writes a copy chunk and returns the number of bytes written.
  private static int emitCopy(IndexedByteArray destination, int offset, int len)
  {
     final byte[] dst = destination.array;
     int idx = destination.index;
     final byte b1 = (byte) offset;
     final byte b2 = (byte) (offset >> 8);

     while (len > 0)
     {
        if ((offset < 2048) && (len < 12) && (len >= 4))
        {
           dst[idx]   = (byte) (((b2&0x07) << 5) | ((len-4) << 2) | TAG_COPY1);
           dst[idx+1] = b1;
           idx += 2;
           break;
        }

        if (len < 64)
        {
           dst[idx] = (byte) (((len-1) << 2) | TAG_COPY2);
           len = 0;
        }
        else
        {
           dst[idx] = B0;
           len -= 64;
        }
        
        dst[idx+1] = b1;
        dst[idx+2] = b2;
        idx += 3;
     }

     return idx - destination.index;
  }


  @Override
  public boolean forward(IndexedByteArray source, IndexedByteArray destination)
  {
     if ((source == null) || (destination == null) || (source.array == destination.array))
        return false;

     final byte[] src = source.array;
     final byte[] dst = destination.array;
     final int count = (this.size > 0) ? this.size : src.length - source.index;

     if (dst.length - destination.index < getMaxEncodedLength(count))
        return false;

     // The block starts with the varint-encoded length of the decompressed bytes.
     int dstIdx = destination.index + putUvarint(destination, (long) count);

     // Return early if src is short
     if (count <= 4)
     {
        if (count > 0)
        {
           destination.index = dstIdx;
           dstIdx += emitLiteral(source, destination, count);
        }

        source.index += count;
        destination.index = dstIdx;
        return true;
     }

     // Initialize the hash table. Its size ranges from 1<<8 to 1<<14 inclusive.
     int shift = 24;
     int tableSize = 256;
     final int[] table = this.buffer; // aliasing
     final int max = (count < MAX_TABLE_SIZE) ? count : MAX_TABLE_SIZE;

     while (tableSize < max)
     {
        shift--;
        tableSize <<= 1;
     }

     for (int i=0; i<tableSize; i++)
        table[i] = -1;

     // Iterate over the source bytes
     final int srcIdx0 = source.index;
     int srcIdx = srcIdx0; // The iterator position
     int lit = srcIdx0; // The start position of any pending literal bytes
     final int ends1 = srcIdx0 + count;
     final int ends2 = ends1 - 3;
     
     while (srcIdx < ends2)
     {
        // Update the hash table
        final int h = (readInt(src, srcIdx) * HASH_SEED) >>> shift;
        int t = table[h]; // The last position with the same hash as srcIdx
        table[h] = srcIdx;

        // If t is invalid or src[srcIdx:srcIdx+4] differs from src[t:t+4], accumulate a literal byte
        if ((t < srcIdx0) || (srcIdx-t >= MAX_OFFSET))
        {
           srcIdx++;
           continue;
        }
        
        if (differentInts(src, srcIdx, t) == true)
        {
           srcIdx++;
           continue;
        }

        // We have a match. First, emit any pending literal bytes
        if (lit != srcIdx)
        {
           source.index = lit;
           destination.index = dstIdx;
           dstIdx += emitLiteral(source, destination, srcIdx-lit);
        }

        // Extend the match to be as long as possible
        final int s0 = srcIdx;
        srcIdx += 4;
        t += 4;

        while ((srcIdx < ends1) && (src[srcIdx] == src[t]))
        {
           srcIdx++;
           t++;
        }

        // Emit the copied bytes
        destination.index = dstIdx;
        dstIdx += emitCopy(destination, srcIdx-t, srcIdx-s0);
        lit = srcIdx;
     }

     // Emit any final pending literal bytes and return
     if (lit != ends1)
     {
        source.index = lit;
        destination.index = dstIdx;
        dstIdx += emitLiteral(source, destination, ends1-lit);       
     }

     source.index = ends1;
     destination.index = dstIdx;
     return true;
  }


  private static int putUvarint(IndexedByteArray iba, long x)
  {
     int idx = iba.index;
     final byte[] array = iba.array;

     for ( ; x >= 0x80; x>>=7)
        array[idx++] = (byte) (x | 0x80);

     array[idx++] = (byte) x;
     return idx;
  }

  
  // Uvarint decodes a long from the input array and returns that value.
  // If an error occurred, an exception is raised.
  // The index of the indexed byte array is incremented by the number of bytes read
  private static long getUvarint(IndexedByteArray iba) 
  {
     final int idx = iba.index;
     final byte[] array = iba.array;
     final int len = array.length;
     long res = 0;
     int s = 0;
 
     for (int i=idx; i<len; i++)
     {
        final long b = array[i] & 0xFF;
        
        if (((s == 63) && (b > 1)) || (s > 63))
           throw new NumberFormatException("Overflow: value is larger than 64 bits");
        
        if ((b & 0x80) == 0)
        {        
           iba.index = i + 1;
           return res | (b << s);        
        }
        
        res |= ((b & 0x7F) << s);
        s += 7;
     }
     
     throw new IllegalArgumentException("Input buffer too small");
  }

  // getMaxEncodedLength returns the maximum length of a snappy block, given its
  // uncompressed length.
  //
  // Compressed data can be defined as:
  //    compressed := item* literal*
  //    item       := literal* copy
  //
  // The trailing literal sequence has a space blowup of at most 62/60
  // since a literal of length 60 needs one tag byte + one extra byte
  // for length information.
  //
  // Item blowup is trickier to measure. Suppose the "copy" op copies
  // 4 bytes of data. Because of a special check in the encoding code,
  // we produce a 4-byte copy only if the offset is < 65536. Therefore
  // the copy op takes 3 bytes to encode, and this type of item leads
  // to at most the 62/60 blowup for representing literals.
  //
  // Suppose the "copy" op copies 5 bytes of data. If the offset is big
  // enough, it will take 5 bytes to encode the copy op. Therefore the
  // worst case here is a one-byte literal followed by a five-byte copy.
  // That is, 6 bytes of input turn into 7 bytes of "compressed" data.
  //
  // This last factor dominates the blowup, so the final estimate is:
  @Override
  public int getMaxEncodedLength(int srcLen)
  {
     return 32 + srcLen + srcLen/6;
  }


  // getDecodedLength returns the length of the decoded block or -1 if error
  // The index of the indexed byte array is incremented by the number
  // of bytes read
  private static int getDecodedLength(IndexedByteArray source) 
  {
     try
     {
        final long v = getUvarint(source);
        
        if (v > 0x7FFFFFFFL)
           return -1;
        
        return (int) v;
     }
     catch (NumberFormatException e)
     {
        return -1;
     }
     catch (IllegalArgumentException e)
     {
        return -1;
     }
  }    
  

  @Override
  public boolean inverse(IndexedByteArray source, IndexedByteArray destination)
  {
     if ((source == null) || (destination == null) || (source.array == destination.array))
        return false;

     final int srcIdx = source.index;
     final int dstIdx = destination.index;
     final byte[] src = source.array;
     final byte[] dst = destination.array;
     
     // Get decoded length (modifies source index)
     final int dLen = getDecodedLength(source);
    
     if ((dLen < 0) || (dst.length - dstIdx < dLen)) 
        return false;

     final int ends = (this.size > 0) ? srcIdx + this.size : src.length;
     int s = source.index;
     int d = dstIdx;
    
     try
     {     
        int offset;
        int length;

        while (s < ends) 
        {       
           switch (src[s] & 0x03)
           {
              case TAG_LITERAL:
              {
                int x = src[s] & 0xFC;

                if (x < TAG_DEC_LEN1)
                {
                    s++;
                    x >>= 2;
                }
                else if (x == TAG_DEC_LEN1)
                {
                   s += 2;
                   x = src[s-1] & 0xFF;
                }
                else if (x == TAG_DEC_LEN2)
                {
                   s += 3;  
                   x = (src[s-2] & 0xFF) | ((src[s-1] & 0xFF) << 8);
                }
                else if (x == TAG_DEC_LEN3)
                {
                   s += 4; 
                   x = (src[s-3] & 0xFF) | ((src[s-2] & 0xFF) << 8) | 
                      ((src[s-1]) & 0xFF) << 16;
                }
                else if (x == TAG_DEC_LEN4)
                {
                   s += 5;
                   x = (src[s-4] & 0xFF) | ((src[s-3] & 0xFF) << 8) |
                       ((src[s-2] & 0xFF) << 16) | ((src[s-1] & 0xFF) << 24);
                }   

                length = x + 1;

                if ((length <= 0) || (length > dst.length-d) || (length > ends-s))
                   return false;

                if (length < 16)
                {
                   for (int i=0; i<length; i++)
                      dst[d+i] = src[s+i];
                }
                else
                {
                   System.arraycopy(src, s, dst, d, length);
                }
 
                d += length;
                s += length;
                continue;
             }

             case TAG_COPY1:
             {
                s += 2;
                length = 4 + (((src[s-2] & 0xFF) >> 2) & 0x07);
                offset = ((src[s-2] & 0xE0) << 3) | (src[s-1] & 0xFF);
                break;
             }

             case TAG_COPY2:
             {
                s += 3;
                length = 1 + ((src[s-3] & 0xFF) >> 2);
                offset = (src[s-2] & 0xFF) | ((src[s-1] & 0xFF) << 8);
                break;
             }

             default:
                return false;
          }

          final int end = d + length;

          if ((offset > d) || (end > dst.length))
             return false;

          for ( ; d<end; d++)
             dst[d] = dst[d-offset];
        }
     }
     catch (ArrayIndexOutOfBoundsException e)
     {
        // Catch incorrectly formatted input
        // Fall through
     }
          
     source.index = ends;
     destination.index = d;
     return (d - dstIdx == dLen);
  }
  
  
  private static boolean differentInts(byte[] array, int srcIdx, int dstIdx)
  {
     return ((array[srcIdx] != array[dstIdx])     ||
             (array[srcIdx+1] != array[dstIdx+1]) ||
             (array[srcIdx+2] != array[dstIdx+2]) ||
             (array[srcIdx+3] != array[dstIdx+3]));
  }

   
  private static int readInt(byte[] array, int srcIdx)
  {
     return ((array[srcIdx]   & 0xFF))       | 
            ((array[srcIdx+1] & 0xFF) <<  8) |
            ((array[srcIdx+2] & 0xFF) << 16) | 
            ((array[srcIdx+3])        << 24);
  }
  
}