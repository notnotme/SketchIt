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
 * Base Integer Resampler.
 * 
 * @author Martin Cameron
 */
public class Resampler {
    public void resample(short[] input, int inputPos, int inputFrac, int step, int lAmp, int[] lBuf, int rAmp,
            int[] rBuf, int pos, int count) {
        for (int n = 0; n < count; n++) {
            int out = input[inputPos];
            lBuf[pos] += out * lAmp >> ModuleEngine.FP_SHIFT;
            rBuf[pos] += out * rAmp >> ModuleEngine.FP_SHIFT;
            pos++;
            inputFrac += step;
            inputPos += inputFrac >> ModuleEngine.FP_SHIFT;
            inputFrac &= ModuleEngine.FP_MASK;
        }
    }
}

