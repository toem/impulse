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

package kanzi.transform;

import kanzi.ByteTransform;
import kanzi.IndexedByteArray;
import kanzi.Sizeable;
import kanzi.util.DivSufSort;


// The Burrows-Wheeler Transform is a reversible transform based on
// permutation of the data in the original message to reduce the entropy.

// The initial text can be found here:
// Burrows M and Wheeler D, [A block sorting lossless data compression algorithm]
// Technical Report 124, Digital Equipment Corporation, 1994

// See also Peter Fenwick, [Block sorting text compression - final report]
// Technical Report 130, 1996

// This implementation replaces the 'slow' sorting of permutation strings
// with the construction of a suffix array (faster but more complex).
// The suffix array contains the indexes of the sorted suffixes.
//
// E.G.    0123456789A
// Source: mississippi\0
// Suffixes:    rank  sorted
// mississippi\0  0  -> 4             i\0
//  ississippi\0  1  -> 3          ippi\0
//   ssissippi\0  2  -> 10      issippi\0
//    sissippi\0  3  -> 8    ississippi\0
//     issippi\0  4  -> 2   mississippi\0 
//      ssippi\0  5  -> 9            pi\0
//       sippi\0  6  -> 7           ppi\0
//        ippi\0  7  -> 1         sippi\0
//         ppi\0  8  -> 6      sissippi\0
//          pi\0  9  -> 5        ssippi\0
//           i\0  10 -> 0     ssissippi\0
// Suffix array SA : 10 7 4 1 0 9 8 6 3 5 2 
// BWT[i] = input[SA[i]-1] => BWT(input) = pssm[i]pissii (+ primary index 4) 
// The suffix array and permutation vector are equal when the input is 0 terminated
// The insertion of a guard is done internally and is entirely transparent.
//
// See https://code.google.com/p/libdivsufsort/source/browse/wiki/SACA_Benchmarks.wiki
// for respective performance of different suffix sorting algorithms.

public class BWT implements ByteTransform, Sizeable
{
    private int size;
    private int[] buffer1;   // Only used in inverse
    private byte[] buffer2;  // Only used for big blocks (size >= 1<<24)
    private int[] buckets;
    private int primaryIndex;
    private DivSufSort saAlgo;

    
    public BWT()
    {
       this(0);
    }


    // Static allocation of memory
    public BWT(int size)
    {
       if (size < 0)
          throw new IllegalArgumentException("Invalid size parameter (must be at least 0)");

       this.size = size;
       this.buffer1 = new int[0];  // Allocate empty: only used in inverse
       this.buffer2 = new byte[0]; // Allocate empty: only used for big blocks (size >= 1<<24)
       this.buckets = new int[256];
    }


    public int getPrimaryIndex()
    {
       return this.primaryIndex;
    }


    // Not thread safe
    public boolean setPrimaryIndex(int primaryIndex)
    {
       if (primaryIndex < 0)
          return false;

       this.primaryIndex = primaryIndex;
       return true;
    }


    @Override
    public int size()
    {
       return this.size;
    }


    @Override
    public boolean setSize(int size)
    {
       if (size < 0)
           return false;

       this.size = size;
       return true;
    }


    // Not thread safe
    @Override
    public boolean forward(IndexedByteArray src, IndexedByteArray dst)
    {
        final byte[] input = src.array;
        final byte[] output = dst.array;
        final int srcIdx = src.index;
        final int dstIdx = dst.index;
        final int count = (this.size == 0) ? input.length - srcIdx :  this.size;

        if (count < 2)
        {
           if (count == 1)
              output[dst.index++] = input[src.index++];

           return true;
        }
       
        if (this.saAlgo == null)
           this.saAlgo = new DivSufSort(); // lazy instantiation
        else
           this.saAlgo.reset();

        // Compute suffix array
        final int[] sa = this.saAlgo.computeSuffixArray(input, srcIdx, count);
        final int srcIdx2 = srcIdx - 1;    
        int i = 0;
       
        for (; i<count; i++) 
        {
          // Found primary index
           if (sa[i] == 0)
              break;

           output[dstIdx+i] = input[srcIdx2+sa[i]];
        }
        
        output[dstIdx+i] = input[srcIdx2+count];
        this.setPrimaryIndex(i);

        for (i++; i<count; i++) 
           output[dstIdx+i] = input[srcIdx2+sa[i]];
                
        src.index += count;
        dst.index += count;
        return true;
    }


    // Not thread safe
    @Override
    public boolean inverse(IndexedByteArray src, IndexedByteArray dst)
    {
       final int count = (this.size == 0) ? src.array.length - src.index :  this.size;

       if (count < 2)
       {
          if (count == 1)
             dst.array[dst.index++] = src.array[src.index++];

          return true;
       }

       if (count >= 1<<24)
          return this.inverseBigBlock(src, dst, count);
       
       return this.inverseRegularBlock(src, dst, count);
    }
    
    
    // When count < 1<<24
    private boolean inverseRegularBlock(IndexedByteArray src, IndexedByteArray dst, int count)
    {
       final byte[] input = src.array;
       final byte[] output = dst.array;
       final int srcIdx = src.index;
       final int dstIdx = dst.index;
       
       // Lazy dynamic memory allocation
       if (this.buffer1.length < count)
          this.buffer1 = new int[count];

       // Aliasing
       final int[] buckets_ = this.buckets;
       final int[] data = this.buffer1;
       
       // Initialize histogram
       for (int i=0; i<256; i++)
          buckets_[i] = 0;
       
       // Build array of packed index + value (assumes block size < 2^24)
       // Start with the primary index position
       final int pIdx = this.getPrimaryIndex();
       final int val0 = input[srcIdx+pIdx] & 0xFF;
       data[pIdx] = val0;
       buckets_[val0]++;

       for (int i=0; i<pIdx; i++)
       {
          final int val = input[srcIdx+i] & 0xFF;
          data[i] = (buckets_[val] << 8) | val;
          buckets_[val]++;
       }
       
       for (int i=pIdx+1; i<count; i++)
       {
          final int val = input[srcIdx+i] & 0xFF;
          data[i] = (buckets_[val] << 8) | val;
          buckets_[val]++;
       }

       // Create cumulative histogram
       for (int i=0, sum=0; i<256; i++)
       {
          final int tmp = buckets_[i];
          buckets_[i] = sum;
          sum += tmp;
       }

       int ptr = data[pIdx];
       output[dstIdx+count-1] = (byte) ptr;
       
       // Build inverse
       for (int i=dstIdx+count-2; i>=dstIdx; i--)
       {
          ptr = data[(ptr>>>8) + buckets_[ptr&0xFF]];
          output[i] = (byte) ptr;
       }
       
       src.index += count;
       dst.index += count;
       return true;
    }

    
    // When count >= 1<<24
    private boolean inverseBigBlock(IndexedByteArray src, IndexedByteArray dst, int count)
    {
       final byte[] input = src.array;
       final byte[] output = dst.array;
       final int srcIdx = src.index;
       final int dstIdx = dst.index;
       
       // Lazy dynamic memory allocations
       if (this.buffer1.length < count)
          this.buffer1 = new int[count];

       if (this.buffer2.length < count)
          this.buffer2 = new byte[count];

       // Aliasing
       final int[] buckets_ = this.buckets;
       final int[] data1 = this.buffer1;
       final byte[] data2 = this.buffer2;
       
       // Initialize histogram
       for (int i=0; i<256; i++)
          buckets_[i] = 0;

       // Build arrays
       // Start with the primary index position
       final int pIdx = this.getPrimaryIndex();
       final byte val0 = input[srcIdx+pIdx];
       data1[pIdx] = buckets_[val0&0xFF];
       data2[pIdx] = val0;
       buckets_[val0&0xFF]++;

       for (int i=0; i<pIdx; i++)
       {
          final byte val = input[srcIdx+i];
          data1[i] = buckets_[val&0xFF];
          data2[i] = val;
          buckets_[val&0xFF]++;
       }
       
       for (int i=pIdx+1; i<count; i++)
       {
          final byte val = input[srcIdx+i];
          data1[i] = buckets_[val&0xFF];
          data2[i] = val;
          buckets_[val&0xFF]++;
       }

       // Create cumulative histogram
       for (int i=0, sum=0; i<256; i++)
       {
          final int tmp = buckets_[i];
          buckets_[i] = sum;
          sum += tmp;
       }

       int val1 = data1[pIdx];
       byte val2 = data2[pIdx];
       output[dstIdx+count-1] = val2;
       
       // Build inverse
       for (int i=dstIdx+count-2; i>=dstIdx; i--)
       {
          final int idx = val1 + buckets_[val2&0xFF];
          val1 = data1[idx];
          val2 = data2[idx];
          output[i] = val2;
       }      
       
       src.index += count;
       dst.index += count;
       return true;
    }

}
