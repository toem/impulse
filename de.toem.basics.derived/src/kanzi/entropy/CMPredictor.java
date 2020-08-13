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


// Context model predictor based on BCM by Ilya Muravyov. 
// See http://sourceforge.net/projects/bcm
public class CMPredictor implements Predictor
{
   private static final int SLOW_RATE   = 2;
   private static final int MEDIUM_RATE = 4;
   private static final int FAST_RATE   = 6;
   
   private int c1;
   private int c2;
   private int ctx;
   private int run;
   private int bpos;
   private int idx;
   private int runMask;
   private final int[][] counter1;
   private final int[][] counter2;
   
   
   public CMPredictor()
   {   
      this.bpos = 7;
      this.ctx = 1;
      this.run = 1;
      this.idx = 8;
      this.counter1 = new int[256][257];
      this.counter2 = new int[512][17];
      
      for (int i=0; i<256; i++)
      {        
         for (int j=0; j<=256; j++)
            this.counter1[i][j] = 32768;
            
         for (int j=0; j<16; j++)
         {
            this.counter2[i+i][j]   = j << 12;
            this.counter2[i+i+1][j] = j << 12;
         }

         this.counter2[i+i][16]   = 15 << 12;
         this.counter2[i+i+1][16] = 15 << 12;
      }			
   }
   
   
   // Update the probability model
   @Override
   public void update(int bit)
   { 
      final int[] counter2_ = this.counter2[(this.ctx<<1)|this.runMask];            
      final int[] counter1_ = this.counter1[this.ctx];
           
      if (bit == 0)
      {
         counter1_[256]        -= (counter1_[256]        >> SLOW_RATE);
         counter1_[this.c1]    -= (counter1_[this.c1]    >> MEDIUM_RATE);
         counter2_[this.idx]   -= (counter2_[this.idx]   >> FAST_RATE);
         counter2_[this.idx+1] -= (counter2_[this.idx+1] >> FAST_RATE);         
      }
      else
      {
         counter1_[256]        += ((counter1_[256]^0xFFFF)        >> SLOW_RATE);
         counter1_[this.c1]    += ((counter1_[this.c1]^0xFFFF)    >> MEDIUM_RATE);
         counter2_[this.idx]   += ((counter2_[this.idx]^0xFFFF)   >> FAST_RATE);
         counter2_[this.idx+1] += ((counter2_[this.idx+1]^0xFFFF) >> FAST_RATE);
      } 
      
      this.ctx = (this.ctx << 1) | bit;

      if (this.bpos == 0)
      {
        this.bpos = 7;
        this.c2 = this.c1;
        this.c1 = this.ctx & 0xFF;
        this.ctx = 1;

        if (this.c1 == this.c2)
        {
           this.run++;
           this.runMask = (2-this.run) >>> 31;
        }
        else
        {
           this.run = 0; 
           this.runMask = 0;
        }
      }  
      else
      {
         this.bpos--;     
      }
   }

   
   // Return the split value representing the probability of 1 in the [0..4095] range. 
   @Override
   public int get()
   {
      final int p0 = this.counter1[this.ctx][256];
      final int p1 = this.counter1[this.ctx][this.c1];
      final int p2 = this.counter1[this.ctx][this.c2];
      final int p = ((p0<<2)+(p1<<1)+p1+p2+4) >>> 3;
      this.idx = p >>> 12;
      final int[] counter2_ = this.counter2[(this.ctx<<1)|this.runMask];            
      final int x1 = counter2_[this.idx];
      final int x2 = counter2_[this.idx+1];
      final int ssep = x1 + (((x2-x1)*(p&4095)) >> 12);
      return (p + ssep + ssep + ssep + 32) >>> 6; // rescale to [0..4095]
   }
}   