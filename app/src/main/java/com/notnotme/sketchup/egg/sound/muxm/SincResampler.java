package com.notnotme.sketchup.egg.sound.muxm;

/* ================================================================
 * MuXM - MOD/XM/S3M player library for J2ME/J2SE
 * Copyright (C) 2005 Martin Cameron, Guillaume Legris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ================================================================
 */

/**
 * Simplified Sinc Resampler 0.3.
 * 
 * @author Martin Cameron
 */
public class SincResampler extends Resampler {
    
    private final int POINT_SHIFT = 4; // 16 points
    private final int OVER_SHIFT = 4; // 16x oversampling
    private final short[] table = {0, -7, 27, -71, 142, -227, 299, 32439, 299, -227, 142, -71, 27, -7, 0, 0, 0, 0, -5,
            36, -142, 450, -1439, 32224, 2302, -974, 455, -190, 64, -15, 2, 0, 0, 6, -33, 128, -391, 1042, -2894,
            31584, 4540, -1765, 786, -318, 105, -25, 3, 0, 0, 10, -55, 204, -597, 1533, -4056, 30535, 6977, -2573,
            1121, -449, 148, -36, 5, 0, -1, 13, -71, 261, -757, 1916, -4922, 29105, 9568, -3366, 1448, -578, 191, -47,
            7, 0, -1, 15, -81, 300, -870, 2185, -5498, 27328, 12263, -4109, 1749, -698, 232, -58, 9, 0, -1, 15, -86,
            322, -936, 2343, -5800, 25249, 15006, -4765, 2011, -802, 269, -68, 10, 0, -1, 15, -87, 328, -957, 2394,
            -5849, 22920, 17738, -5298, 2215, -885, 299, -77, 12, 0, 0, 14, -83, 319, -938, 2347, -5671, 20396, 20396,
            -5671, 2347, -938, 319, -83, 14, 0, 0, 12, -77, 299, -885, 2215, -5298, 17738, 22920, -5849, 2394, -957,
            328, -87, 15, -1, 0, 10, -68, 269, -802, 2011, -4765, 15006, 25249, -5800, 2343, -936, 322, -86, 15, -1, 0,
            9, -58, 232, -698, 1749, -4109, 12263, 27328, -5498, 2185, -870, 300, -81, 15, -1, 0, 7, -47, 191, -578,
            1448, -3366, 9568, 29105, -4922, 1916, -757, 261, -71, 13, -1, 0, 5, -36, 148, -449, 1121, -2573, 6977,
            30535, -4056, 1533, -597, 204, -55, 10, 0, 0, 3, -25, 105, -318, 786, -1765, 4540, 31584, -2894, 1042,
            -391, 128, -33, 6, 0, 0, 2, -15, 64, -190, 455, -974, 2302, 32224, -1439, 450, -142, 36, -5, 0, 0, 0, 0,
            -7, 27, -71, 142, -227, 299, 32439, 299, -227, 142, -71, 27, -7, 0};

    /*
     private final int POINT_SHIFT = 1; // 2 points
     private final int OVER_SHIFT = 0; // 1x oversampling
     private final short[] table = {	
     32767,     0,
     0    , 32767
     };
     */

    private final int POINTS = 1 << POINT_SHIFT;
    private final int INTERP_SHIFT = ModuleEngine.FP_SHIFT - OVER_SHIFT;
    private final int INTERP_BITMASK = (1 << INTERP_SHIFT) - 1;

    public void resample(short[] input, int inputPos, int inputFrac, int step, int lAmp, int[] lBuf, int rAmp,
            int[] rBuf, int pos, int count) {
        for (int p = 0; p < count; p++) {
            int tabidx1 = (inputFrac >> INTERP_SHIFT) << POINT_SHIFT;
            int tabidx2 = tabidx1 + POINTS;
            int bufidx = inputPos - POINTS / 2 + 1;
            int a1 = 0, a2 = 0;
            for (int t = 0; t < POINTS; t++) {
                a1 += table[tabidx1++] * input[bufidx] >> 15;
                a2 += table[tabidx2++] * input[bufidx] >> 15;
                bufidx++;
            }
            int out = a1 + ((a2 - a1) * (inputFrac & INTERP_BITMASK) >> INTERP_SHIFT);
            lBuf[pos] += out * lAmp >> ModuleEngine.FP_SHIFT;
            rBuf[pos] += out * rAmp >> ModuleEngine.FP_SHIFT;
            pos++;
            inputFrac += step;
            inputPos += inputFrac >> ModuleEngine.FP_SHIFT;
            inputFrac &= ModuleEngine.FP_MASK;
        }
    }
}