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

package kanzi.entropy;

import kanzi.InputBitStream;
import kanzi.BitStreamException;
import kanzi.EntropyDecoder;


// Based on Order 0 range coder by Dmitry Subbotin itself derived from the algorithm
// described by G.N.N Martin in his seminal article in 1979.
// [G.N.N. Martin on the Data Recording Conference, Southampton, 1979]
// Optimized for speed.

// Not thread safe
public final class RangeDecoder implements EntropyDecoder
{
    private static final long TOP_RANGE    = 0x0FFFFFFFFFFFFFFFL;
    private static final long BOTTOM_RANGE = 0x0000000000FFFFFFL;
    private static final long MASK         = 0x0FFFFF0000000000L;
    private static final int DEFAULT_CHUNK_SIZE = 1 << 16; // 64 KB by default


    private long code;
    private long low;
    private long range;
    private final int[] alphabet;
    private final int[] freqs;
    private final int[] cumFreqs;
    private short[] f2s; // mapping frequency -> symbol
    private final InputBitStream bitstream;
    private final int chunkSize;
    private long invSum;

    
    public RangeDecoder(InputBitStream bitstream)
    {
       this(bitstream, DEFAULT_CHUNK_SIZE);
    }


    // The chunk size indicates how many bytes are encoded (per block) before
    // resetting the frequency stats. 0 means that frequencies calculated at the
    // beginning of the block apply to the whole block.
    // The default chunk size is 65536 bytes.
    public RangeDecoder(InputBitStream bitstream, int chunkSize)
    {
        if (bitstream == null)
            throw new NullPointerException("Invalid null bitstream parameter");

        if ((chunkSize != 0) && (chunkSize < 1024))
           throw new IllegalArgumentException("The chunk size must be at least 1024");

        if (chunkSize > 1<<30)
           throw new IllegalArgumentException("The chunk size must be at most 2^30");

        this.range = TOP_RANGE;
        this.bitstream = bitstream;
        this.chunkSize = chunkSize;
        this.cumFreqs = new int[257];
        this.freqs = new int[256];
        this.alphabet = new int[256];
        this.f2s = new short[0];
    }


    protected int decodeHeader(int[] frequencies)
    {
      int alphabetSize = EntropyUtils.decodeAlphabet(this.bitstream, this.alphabet);

      if (alphabetSize == 0)
         return 0;

      if (alphabetSize != 256)
      {
         for (int i=0; i<256; i++)
            frequencies[i] = 0;
      }

      final int logRange = (int) (8 + this.bitstream.readBits(3));
      final int scale = 1 << logRange;
      int sum = 0;
      int inc = (alphabetSize > 64) ? 16 : 8;
      int llr = 3;

      while (1<<llr <= logRange)
         llr++;

      // Decode all frequencies (but the first one) by chunks of size 'inc'
      for (int i=1; i<alphabetSize; i+=inc)
      {
         final int logMax = (int) (1 + this.bitstream.readBits(llr));
         final int endj = (i+inc < alphabetSize) ? i + inc : alphabetSize;

         // Read frequencies
         for (int j=i; j<endj; j++)
         {
            int val = (int) this.bitstream.readBits(logMax);

            if ((val <= 0) || (val >= scale))
            {
               throw new BitStreamException("Invalid bitstream: incorrect frequency " +
                       val + " for symbol '" + this.alphabet[j] + "' in range decoder",
                       BitStreamException.INVALID_STREAM);
            }

            frequencies[this.alphabet[j]] = val;
            sum += val;
         }
      }

      // Infer first frequency
      frequencies[this.alphabet[0]] = scale - sum;

      if ((frequencies[this.alphabet[0]] <= 0) || (frequencies[this.alphabet[0]] > scale))
      {
         throw new BitStreamException("Invalid bitstream: incorrect frequency " +
                 frequencies[this.alphabet[0]] + " for symbol '" + this.alphabet[0] +
                 "' in range decoder", BitStreamException.INVALID_STREAM);
      }

      this.cumFreqs[0] = 0;

      if (this.f2s.length < scale)
         this.f2s = new short[scale];

      // Create histogram of frequencies scaled to 'range' and reverse mapping
      for (int i=0; i<256; i++)
      {
         this.cumFreqs[i+1] = this.cumFreqs[i] + frequencies[i];

         for (int j=frequencies[i]-1; j>=0; j--)
            this.f2s[this.cumFreqs[i]+j] = (short) i;
      }
      
      this.invSum = (1L<<24) / this.cumFreqs[256];
      return alphabetSize;
    }
   

    // Initialize once (if necessary) at the beginning, the use the faster decodeByte_()
    // Reset frequency stats for each chunk of data in the block
    @Override
    public int decode(byte[] array, int blkptr, int len)
    {
      if ((array == null) || (blkptr + len > array.length) || (blkptr < 0) || (len < 0))
         return -1;

      if (len == 0)
         return 0;
      
      final int end = blkptr + len;
      final int sz = (this.chunkSize == 0) ? len : this.chunkSize;
      int startChunk = blkptr;

      while (startChunk < end)
      {
         if (this.decodeHeader(this.freqs) == 0)
            return startChunk - blkptr;

         this.range = TOP_RANGE;
         this.low = 0;
         this.code = this.bitstream.readBits(60);
         final int endChunk = (startChunk + sz < end) ? startChunk + sz : end;

         for (int i=startChunk; i<endChunk; i++)
            array[i] = this.decodeByte();

         startChunk = endChunk;
      }

      return len;
    }


    protected byte decodeByte()
    {
       this.range = (this.range >>> 24) * this.invSum;
       final int count = (int) ((this.code - this.low) / this.range);
       final int value = this.f2s[count];

       // Compute next low and range
       final int symbolLow = this.cumFreqs[value];
       final int symbolHigh = this.cumFreqs[value+1];
       this.low += (symbolLow * this.range);
       this.range *= (symbolHigh - symbolLow);

       while (true)
       {
          if (((this.low ^ (this.low + this.range)) & MASK) != 0)
          {
             if (this.range > BOTTOM_RANGE)
                break;
             
             // Normalize
             this.range = -this.low & BOTTOM_RANGE;
          }

          this.code = (this.code << 20) | this.bitstream.readBits(20);
          this.range <<= 20;
          this.low <<= 20;
       }

       return (byte) value;
    }


    @Override
    public InputBitStream getBitStream()
    {
       return this.bitstream;
    }

   
    @Override  
    public void dispose() 
    {
    }
}
