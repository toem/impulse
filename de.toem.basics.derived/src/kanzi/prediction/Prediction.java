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


package kanzi.prediction;


public class Prediction
{
   public int blockDim;
   public int sad;
   public int[] frame;
   public final int[] residue;
   public int x;
   public int y;
   public int anchor;


   public Prediction(int maxBlockDim)
   {
      if ((maxBlockDim < 4) || (maxBlockDim > 64))
         throw new IllegalArgumentException("The maximum block dimension must be in the [4..64] range"); // for now

      if ((maxBlockDim & 7) != 0)
         throw new IllegalArgumentException("The maximum block dimension must be a multiple of 8");

      this.residue = new int[maxBlockDim*maxBlockDim];
      this.blockDim = maxBlockDim;
   }


   public Prediction(int[] frame, int x, int y, int blockDim)
   {
      this.frame = frame;
      this.x = x;
      this.y = y;
      this.residue = new int[blockDim*blockDim];
      this.blockDim = blockDim;
   }
}