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

package kanzi.filter;

import kanzi.IndexedIntArray;
import kanzi.IntFilter;


public class ContrastFilter implements IntFilter
{
    private final int width;
    private final int height;
    private final int stride;
    private final int[] intensities;
    private int contrast256;

    
    // contrast in percent
    public ContrastFilter(int width, int height, int contrast)
    {
       this(width, height, width, contrast);
    }

    
    // contrast in percent
    public ContrastFilter(int width, int height, int stride, int contrast)
    {
        if (height < 8)
            throw new IllegalArgumentException("The height must be at least 8");

        if (width < 8)
            throw new IllegalArgumentException("The width must be at least 8");

        if (stride < 8)
            throw new IllegalArgumentException("The stride must be at least 8");
        
        if ((contrast < 0) || (contrast > 1000))
            throw new IllegalArgumentException("The contrast parameter (in %) must be in the range [0..1000]");
        
        this.height = height;
        this.width = width;
        this.stride = stride;
        this.intensities = new int[256];
        this.contrast256 = contrast << 8;
        this.initBuffer(contrast);
    }
    
    
    @Override
    public boolean apply(IndexedIntArray source, IndexedIntArray destination)
    {
        // Aliasing
        final int[] src = source.array;
        final int[] dst = destination.array;
        int srcIdx = source.index;
        int dstIdx = destination.index;
        final int w = this.width;
        final int h = this.height;
        final int len = src.length;
        final int[] buffer = this.intensities;
        
        for (int y=0; y<h; y++)
        {
           final int endX = (srcIdx + w < len) ? srcIdx + w : len;
           
           for (int xs=srcIdx, xd=dstIdx; xs<endX; xs++, xd++)
           {
              final int pixel = src[xs];
              final int r = buffer[(pixel >> 16) & 0xFF];
              final int g = buffer[(pixel >>  8) & 0xFF];
              final int b = buffer[pixel & 0xFF];
              dst[xd] = (r << 16) | (g << 8) | b;
           }
           
           srcIdx += this.stride;
           dstIdx += this.stride;

           if (srcIdx >= len)
              break;
        }
        
        return true;
    }
    
    
    // in percent
    public int getContrast()
    {
       return this.contrast256 >> 8;
    }
    
    
    public boolean setContrast(int contrast)
    {
       if ((contrast < 0) || (contrast > 1000))
          return false;
        
       this.contrast256 = contrast << 8;
       this.initBuffer(contrast);
       return true;
    }
    
    
    private void initBuffer(int contrast)
    {
       int ratio = (contrast << 8) / 100;
       ratio *= ratio;

       for (int i=0; i<256; i++)
       {
          int val = (ratio * i) >> 16;
           
          if (val > 255)
             val = 255;
          else if (val < 0)
             val = 0;
           
          this.intensities[i] = val;
       }
   }    
}
