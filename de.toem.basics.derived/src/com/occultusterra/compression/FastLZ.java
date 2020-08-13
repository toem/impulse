/*  
  Copyright (C) 2016 William Welna (wwelna@occultusterra.com)
  
  Converted from original FastLZ C source code by Ariya Hidayat (ariya@kde.org)

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
*/

package com.occultusterra.compression;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FastLZ {

/*
    MAX_COPY = 32;
    MAX_LEN = 264; //256 + 8
    HASH_LOG = 13;
    HASH_SIZE = (1<< HASH_LOG); // 8192
    HASH_MASK = (HASH_SIZE-1); // 8191
    MAX_DISTANCE_LZ1 = 8192;
    MAX_DISTANCE_LZ2 = 8191;
    MAX_FARDISTANCE_LZ2 = (65535+MAX_DISTANCE_LZ2-1); // 73725
*/
    
    // This is the C version converted into Java
    private static final int VERSION = 0x000100;
    private static final String VERSION_STRING = "0.1.0";
    
    // A bit pointless but I wanted to use a enum! So There
    enum defs {
        FASTLZ0(0), FASTLZ1(1), FASTLZ2(2);
        private int value;
        
        private defs(int value) {
            this.setValue(value); // I DO WHAT I WANT!
        }

        public int getValue() {
            return value;
        }

        void setValue(int value) {
            this.value = value;
        }
    } // I'M GOING TO HAVE THIS ENUM!
    
    public int getVersion() {
        return FastLZ.VERSION;
    }
    
    public String getVersionString() {
        return FastLZ.VERSION_STRING;
    }
    
    static int readU16(byte[] in, int offset) {
        if(offset + 1 >= in.length)
            return in[offset] & 0xff;
        return (in[offset] & 0xff) + ((in[offset+1] & 0xff) << 8);
    }
    
    static int hashFunction(byte[] in, int offset) {
        int v = readU16(in, offset);
        v ^= readU16(in, offset + 1) ^ (v >>> (3)); // (16 - HASH_LOG)
        v &= 8191; //HASH_MASK;
        return v;
    }

    static defs findLevel(byte[] in) {
        byte level = (byte) ((in[0] >>> 5) + 1);
        if(level == 1) return defs.FASTLZ1;
        else if (level == 2) return defs.FASTLZ2;
        else return defs.FASTLZ0;
    }
    
    public static int calcLength(int length) {
        return (int) Math.max(66, (length * 1.06));
    }
    
    public static byte[] compress(byte[] in) {
        byte[] out = new byte[calcLength(in.length)];
        int size = compress(in, out);
        return Arrays.copyOf(out, size);
    }
    
    public static byte[] compress(String in) {
        byte[] out = new byte[calcLength(in.length())];
        int size = compress(in.getBytes(), out);
        return Arrays.copyOf(out, size);
    }
    
    public static int compress(byte[] in, byte[] out) {
        if(in.length < 65536) return compress_lz(in, out, defs.FASTLZ1);
        else return compress_lz(in, out, defs.FASTLZ2);
    }
    
    static int compress_lz(byte[] in, byte[] out, defs compression_level) {
        int ip = 0;
        int op = 0;
        int ipBound = in.length-2;
        int ipLimit = in.length-12;
        int[] htab = new int[8192]; // HASH_SIZE
        int hslot;
        int hval;
        int copy;
        
        if(in.length < 4) {
            if(in.length != 0) {
                out[op++] = (byte) (in.length-1);
                ipBound++;
                while(ip <= ipBound) out[op++] = in[ip++];
                return in.length+1;
            } else return 0;
        }

        for(hslot = 0; hslot < 8192; hslot++) htab[hslot] = ip; // hslot < HASH_SIZE;

        copy = 2;
        out[op++] = 31;//MAX_COPY-1;
        out[op++] = in[ip++];
        out[op++] = in[ip++];

        while(ip < ipLimit) {
            int ref = 0;
            int distance = 0;
            int len = 3;
            int anchor = ip;
            boolean labelMatch = false;

            if(compression_level == defs.FASTLZ2)
                if(in[ip] == in[ip-1] && readU16(in, ip-1) == readU16(in, ip+1)) {
                    distance = 1;
                    ip += 3;
                    ref = anchor-1+3;
                    labelMatch = true;
                }
            if(!labelMatch) {
                hval = hashFunction(in, ip);
                hslot = hval;
                ref = htab[hval];
                distance = anchor-ref;
                htab[hslot] = anchor;

                if(distance == 0 || (compression_level == defs.FASTLZ1 ? distance >= 8192 : distance >= 73725) //distance >= MAX_DISTANCE_LZ1 : distance >= MAX_FARDISTANCE_LZ2)
                        || in[ref++] != in[ip++]
                        || in[ref++] != in[ip++]
                        || in[ref++] != in[ip++]) {
                    out[op++] = in[anchor++];
                    ip = anchor;
                    copy++;
                    if(copy == 32) { // MAX_COPY
                        copy = 0;
                        out[op++] = 31; //MAX_COPY-1;
                    } continue;
                }

                if(compression_level == defs.FASTLZ2)
                    if(distance >= 8191) { // MAX_DISTANCE_LZ2
                        if(in[ip++] != in[ref++] || in[ip++] != in[ref++]) {
                            out[op++] = in[anchor++];
                            ip = anchor;
                            copy++;
                            if(copy == 32) { // MAX_COPY
                                copy = 0;
                                out[op++] = 31; //MAX_COPY-1;
                            } continue;
                        }
                        len += 2;
                    }
            } //labelMatch
            ip = anchor + len;
            distance--;

            if(distance == 0) {
                byte x = in[ip - 1];
                while(ip < ipBound)
                    if(in[ref++] != x) break; else ip++;
            } else
                for(;;) {
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    if (in[ref++] != in[ip++]) break;
                    while(ip < ipBound) if (in[ref++] != in[ip++]) break;
                    break;
                }

            if(copy != 0) out[op-copy-1] = (byte) (copy-1); else op--;

            copy = 0;
            ip -= 3;
            len = ip - anchor;

            if(compression_level == defs.FASTLZ2)
                if(distance < 8191) // MAX_DISTANCE_LZ2
                    if(len < 7) {
                        out[op++] = (byte) ((len << 5) + (distance >>> 8));  // >>>
                        out[op++] = (byte) (distance & 255);
                    } else {
                        out[op++] = (byte) ((7 << 5) + (distance >>> 8)); // >>>
                        for(len -= 7; len >= 255; len -= 255) out[op++] = (byte) 255;
                        out[op++] = (byte) len;
                        out[op++] = (byte) (distance & 255);
                    }
                else
                    if(len < 7) {
                        distance -= 8191; // MAX_DISTANCE_LZ2
                        out[op++] = (byte) ((len << 5) + 31);
                        out[op++] = (byte) 255;
                        out[op++] = (byte) (distance >>> 8);  // >>>
                        out[op++] = (byte) (distance & 255);
                    } else {
                        distance -= 8191; // MAX_DISTANCE_LZ2
                        out[op++] = (byte) ((7 << 5) + 31);
                        for(len -= 7; len >= 255; len -= 255) out[op++] = (byte) 255;
                        out[op++] = (byte) len;
                        out[op++] = (byte) 255;
                        out[op++] = (byte) (distance >>> 8);  // >>>
                        out[op++] = (byte) (distance & 255);
                    }
            else {
                if(len > 262) // MAXLEN - 2
                    while (len > 262) { //  MAX_LEN - 2
                        out[op++] = (byte) ((7 << 5) + (distance >>> 8)); // >>>
                        out[op++] = (byte) 253; // (MAX_LEN - 2 - 7 - 2); // MAX_LEN - 11
                        out[op++] = (byte) (distance & 255);
                        len -= 262; //MAX_LEN - 2;
                    }

                if(len < 7) {
                    out[op++] = (byte) ((len << 5) + (distance >>> 8)); // >>>
                    out[op++] = (byte) (distance & 255);
                } else {
                    out[op++] = (byte) ((7 << 5) + (distance >>> 8)); // >>>
                    out[op++] = (byte) (len - 7);
                    out[op++] = (byte) (distance & 255);
                }
            }
            
            hval = hashFunction(in, ip);
            htab[hval] = ip++;
            
            hval = hashFunction(in, ip);
            htab[hval] = ip++;

            out[op++] = 31; //MAX_COPY-1;

            continue;
        }
            
        ipBound++;
        while(ip <= ipBound) {
            out[op++] = in[ip++];
            copy++;
            if(copy == 32) { //MAX_COPY
                copy = 0;
                out[op++] = 31; //MAX_COPY-1;
            }
        }
        
        if(copy != 0) out[op-copy-1] = (byte) (copy-1); else op--;
        
        if(compression_level == defs.FASTLZ2) out[0] |= 1 << 5;
        
        return op;
    }
    
    /* All of these use size_output to leave it up whoever uses this
     * library to figure that stuff out. :)
     * 
     * Will throw exception if there is not enough space to decompress
     */
    
    public static String decompressToString(byte[] in, int size_output) throws Exception {
        byte[] result = decompress(in, size_output);
        return new String(result, StandardCharsets.US_ASCII);
    }
    
    public static byte[] decompress(byte[] in, int size_output) throws Exception {
        byte[] out = new byte[size_output];
        int result = decompress(in, out);
        return Arrays.copyOf(out, result); 
    }
    
    public static int decompress(byte[] in, byte[] out) throws Exception {
        return decompress(in, out, findLevel(in));
    }
    
    static int decompress(byte[] in, byte[] out, defs compression_level) throws Exception {
        if(compression_level == defs.FASTLZ0) throw new Exception("WTF?!");
        int ip = 0;
        int op = 0;
        long ctrl = in[ip++] & 31;

        boolean loop = true;        
        do {
            int ref = op;
            long len = ctrl >>> 5;
            long ofs = (ctrl & 31) << 8;

            if(ctrl >= 32) {
                int code;
                len--;
                ref -= ofs;
                
                if(len == 6) { // (len == 7-1)
                    if(compression_level == defs.FASTLZ1) len += in[ip++] & 0xff;
                    else
                        do {
                            code = in[ip++] & 0xff;
                            len += code;
                        } while(code == 255);
                }
                if(compression_level == defs.FASTLZ1) ref -= in[ip++] & 0xff;
                else {
                    code = in[ip++] & 0xff;
                    ref -= code;

                    if(code == 255 && ofs == 31 << 8) {
                        ofs = (in[ip++] & 0xff) << 8;
                        ofs += in[ip++] & 0xff;

                        ref = (int) (op-ofs-8191); // MAX_DISTANCE_LZ2
                    }
                }

                if(op+len+3 > out.length) throw new Exception("(op+len+3 > out.length)");
                if(ref-1 < 0) throw new Exception("(ref-1 < 0)");
                
                if(ip < in.length) ctrl = in[ip++] & 0xff; else loop = false;

                if(ref == op) {
                    byte b = out[ref-1];
                    out[op++] = b;
                    out[op++] = b;
                    out[op++] = b;
                    
                    for(;len != 0; --len) out[op++] = b;     
                } else {
                    ref--;

                    out[op++] = out[ref++];
                    out[op++] = out[ref++];
                    out[op++] = out[ref++];

                    for(;len != 0; --len) out[op++] = out[ref++];
                }
            } else {
                ctrl++;
                
                if(op+ctrl > out.length) throw new Exception("(op+ctrl > out.length)");
                if(ip+ctrl > in.length) throw new Exception("(ip+ctrl > in.length)");

                out[op++] = in[ip++];

                for(--ctrl; ctrl != 0; ctrl--) out[op++] = in[ip++];

                loop = ip < in.length ? true : false;
                if(loop) ctrl = in[ip++] & 0xff;
            }
        } while(loop);
        
        return op;
    }
}
