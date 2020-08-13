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

package kanzi.util.color;

import kanzi.ColorModelType;


// Simply pack/unpack RGB channels
public class RGBColorModelConverter implements ColorModelConverter
{
    private final int height;
    private final int width;
    private final int rgbOffset;
    private final int stride;


    public RGBColorModelConverter(int width, int height)
    {
        this(width, height, 0, width);
    }


    public RGBColorModelConverter(int width, int height, int rgbOffset, int stride)
    {
        if (height < 8)
            throw new IllegalArgumentException("The height must be at least 8");

        if (width < 8)
            throw new IllegalArgumentException("The width must be at least 8");

        if (stride < 8)
            throw new IllegalArgumentException("The stride must be at least 8");

        if ((height & 7) != 0)
            throw new IllegalArgumentException("The height must be a multiple of 8");

        if ((width & 7) != 0)
            throw new IllegalArgumentException("The width must be a multiple of 8");

        if ((stride & 7) != 0)
            throw new IllegalArgumentException("The stride must be a multiple of 8");

        this.height = height;
        this.width = width;
        this.rgbOffset = rgbOffset;
        this.stride = stride;
    }


    @Override
    public boolean convertRGBtoYUV(int[] rgb, int[] y, int[] u, int[] v, ColorModelType type)
    {
        if (type != ColorModelType.YUV444)
            return false;

        int startLine  = this.rgbOffset;
        int startLine2 = 0;

        for (int j=0; j<this.height; j++)
        {
            final int end = startLine + this.width;

            for (int k=startLine, i=startLine2; k<end; i++)
            {
                // Unpack channels
                final int rgbVal = rgb[k++];
                y[i] = (rgbVal >> 16) & 0xFF; // r
                u[i] = (rgbVal >> 8) & 0xFF;  // g
                v[i] =  rgbVal & 0xFF;        // b
            }

            startLine2 += this.width;
            startLine  += this.stride;
        }

        return true;
    }
    
    
    @Override
    public boolean convertYUVtoRGB(int[] y, int[] u, int[] v, int[] rgb, ColorModelType type)
    {
        if (type != ColorModelType.YUV444)
            return false;

        int startLine = 0;
        int startLine2 = this.rgbOffset;

        for (int j=0; j<this.height; j++)
        {
            final int end = startLine + this.width;

            for (int i=startLine, k=startLine2; i<end; i++)
            {
               // Pack channels. Ensure that channel values are in [0.255]
               // convertRGBtoYUV() ewnsures proper range but the values may have been
               // manipulated after the convertRGBtoYUV() call.
               int r = y[i];
               r &= ~(r >> 31);
              
               if (r > 255) 
                  r = 255;
               
               int g = u[i];
               g &= ~(g >> 31);
               
               if (g > 255)
                  g = 255;
               
               int b = v[i];
               b &= ~(b >> 31);
               
               if (b > 255) 
                  b = 255;
               
               rgb[k++] = (r << 16) | (g << 8) | b;
            }

            startLine  += this.width;
            startLine2 += this.stride;
        }

        return true;
    }
}

