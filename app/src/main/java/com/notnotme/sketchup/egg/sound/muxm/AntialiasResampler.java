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
 * <p>Antialiasing Resampler.</p>
 * 
 * <p>Uses a nearest neighbour resampling algorithm with
 * 2x oversampling, and a 3-point fixed-frequency
 * downsampling FIR filter to reduce high frequency noise.</p>
 * 
 * @author Martin Cameron
 */
public class AntialiasResampler extends Resampler {
    public void resample(short[] input, int inputPos, int inputFrac, int step, int lAmp, int[] lBuf, int rAmp,
            int[] rBuf, int pos, int count) {
        for (int n = 0; n < count; n++) {
            int out = input[inputPos] >> 1;
            out += input[inputPos + (inputFrac - (step >> 1) >> ModuleEngine.FP_SHIFT)] >> 2;
            out += input[inputPos + (inputFrac + (step >> 1) >> ModuleEngine.FP_SHIFT)] >> 2;
            lBuf[pos] += out * lAmp >> ModuleEngine.FP_SHIFT;
            rBuf[pos] += out * rAmp >> ModuleEngine.FP_SHIFT;
            pos++;
            inputFrac += step;
            inputPos += inputFrac >> ModuleEngine.FP_SHIFT;
            inputFrac &= ModuleEngine.FP_MASK;
        }
    }
}

