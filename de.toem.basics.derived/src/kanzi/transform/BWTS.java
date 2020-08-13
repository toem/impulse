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

import kanzi.util.DivSufSort;
import kanzi.ByteTransform;
import kanzi.IndexedByteArray;
import kanzi.Sizeable;


// Bijective version of the Burrows-Wheeler Transform
// The main advantage over the regular BWT is that there is no need for a primary
// index (hence the bijectivity). BWTS is about 10% slower than BWT.
// Forward transform based on the code at https://code.google.com/p/mk-bwts/ 
// by Neal Burns and DivSufSort (port of libDivSufSort by Yuta Mori)

public class BWTS implements ByteTransform, Sizeable
{
    private int size;
    private int[] buffer;
    private int[] buckets;
    private DivSufSort saAlgo;

    
    public BWTS()
    {
       this(0);
    }


    // Static allocation of memory
    public BWTS(int size)
    {
       if (size < 0)
          throw new IllegalArgumentException("Invalid size parameter (must be at least 0)");

       this.size = size;
       this.buffer = new int[size];  
       this.buckets = new int[256];
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

        // Lazy dynamic memory allocation 
        if (this.buffer.length < count)
           this.buffer = new int[count];
       
        if (this.saAlgo == null)
           this.saAlgo = new DivSufSort();
        else
           this.saAlgo.reset();

        // Compute suffix array
        final int[] sa = this.saAlgo.computeSuffixArray(input, srcIdx, count);
        
        // Aliasing
        final int[] isa = this.buffer;

        for (int i=0; i<count; i++)
           isa[sa[i]] = i;

        int min = isa[0];
        int idxMin = 0;
        
        for (int i=1; ((i<count) && (min>0)); i++) 
        {
            if (isa[i] >= min) 
               continue;
            
            int headRank = this.moveLyndonWordHead(sa, input, count, srcIdx, idxMin, i-idxMin, min);
            int refRank = headRank;

            for (int j=i-1; j>idxMin; j--) 
            { 
                // iterate through the new lyndon word from end to start
                int testRank = isa[j];
                int startRank = testRank;

                while (testRank < count-1) 
                {                 
                   int nextRankStart = sa[testRank+1];

                   if ((j > nextRankStart) || (input[srcIdx+j] != input[srcIdx+nextRankStart]) 
                            || (refRank < isa[nextRankStart+1])) 
                      break;

                   sa[testRank] = nextRankStart;
                   isa[nextRankStart] = testRank;
                   testRank++;
                }

                sa[testRank] = j;
                isa[j] = testRank;
                refRank = testRank;

                if (startRank == testRank) 
                   break;
            }

            min = isa[i];
            idxMin = i;
        }
        
        min = count;
        final int srcIdx2 = srcIdx - 1;
        
        for (int i=0; i<count; i++) 
        {
           if (isa[i] >= min) 
           {
              output[dstIdx+isa[i]] = input[srcIdx2+i];
              continue;
           }

           if (min < count)
              output[dstIdx+min] = input[srcIdx2+i];
           
           min = isa[i];
        }
   
        output[dstIdx] = input[srcIdx2+count];
        src.index += count;
        dst.index += count; 
        return true;
    }

    
    private int moveLyndonWordHead(int[] sa, byte[] data, int count, int srcIdx, int start, int size, int rank)
    {
       final int[] isa = this.buffer;
       final int end = start + size;
       final int startIdx = srcIdx + start;

       while (rank+1 < count)
       {
          final int nextStart0 = sa[rank+1];
          
          if (nextStart0 <= end)
             break;         
          
          int nextStart = nextStart0;
          int k = 0;

          while ((k < size) && (nextStart < count) && (data[startIdx+k] == data[srcIdx+nextStart]))
          {
              k++;
              nextStart++;
          }

          if ((k == size) && (rank < isa[nextStart]))
             break;

          if ((k < size) && (nextStart < count) && ((data[startIdx+k] & 0xFF) < (data[srcIdx+nextStart] & 0xFF)))
             break;         

          sa[rank] = nextStart0;
          isa[nextStart0] = rank;
          rank++;
       }
       
       sa[rank] = start;
       isa[start] = rank;
       return rank;
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
       
       final byte[] input = src.array;
       final byte[] output = dst.array;
       final int srcIdx = src.index;
       final int dstIdx = dst.index;
       
       // Lazy dynamic memory allocation
       if (this.buffer.length < count)
          this.buffer = new int[count];

       // Aliasing
       final int[] buckets_ = this.buckets;
       final int[] lf = this.buffer;
       
       // Initialize histogram
       for (int i=0; i<256; i++)
          buckets_[i] = 0;

       for (int i=0; i<count; i++)
          buckets_[input[srcIdx+i] & 0xFF]++;

       // Histogram
       for (int i=0, sum=0; i<256; i++)
       { 
          final int tmp = buckets_[i]; 
          buckets_[i] = sum; 
          sum += tmp; 
       }
       
       for (int i=0; i<count; i++)
          lf[i] = buckets_[input[srcIdx+i] & 0xFF]++;
       
       // Build inverse
       for (int i=0, j=dstIdx+count-1; j>=dstIdx; i++) 
       {         
          if (lf[i] < 0) 
             continue;  

          int p = i;
           
          do 
          {
             output[j] = input[srcIdx+p];
             j--;
             final int t = lf[p];
             lf[p] = -1;
             p = t;           
          } 
          while (lf[p] >= 0);
       }
       
       src.index += count;
       dst.index += count;
       return true;
    }
}
