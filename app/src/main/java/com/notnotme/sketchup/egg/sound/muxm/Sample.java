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
 * An audio sample. 
 * 
 * @author Martin Cameron
 */
 
class Sample {
    
    public String name;
    public int volume, finetune;
    public int panning, relativeNote;
    public int loopStart, loopEnd;
    public boolean bidi;
    public short[] samples;

    public Sample() {
        this("Empty Sample", new short[1], false, 0, 0, false, 0, 128, 0, 0);
    }

    public Sample(String name, short[] samples, boolean loop, int loopStart, int loopEnd, boolean bidi, int volume,
            int panning, int finetune, int relativeNote) {
        if (samples.length == 0)
            samples = new short[1];
        if (loopStart < 0 || loopEnd < 0)
            loop = false;
        if (loopStart >= samples.length || loopEnd >= samples.length)
            loop = false;
        if (loopEnd < loopStart)
            loop = false;
        if (!loop)
            loopStart = loopEnd = samples.length - 1;
        if (volume < 0)
            volume = 0;
        if (volume > 64)
            volume = 64;
        if (panning < 0 || panning > 255)
            panning = 128;
        if (finetune < -128 || finetune > 127)
            finetune = 0;
        if (relativeNote < -96 || relativeNote > 95)
            relativeNote = 0;
        this.name = name;
        this.samples = samples;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        this.bidi = bidi;
        this.volume = volume;
        this.panning = panning;
        this.finetune = finetune;
        this.relativeNote = relativeNote;
    }

    public void print() {
        System.out.println("Sample:");
        System.out.println("  Name: " + name);
        System.out.println("  Volume: " + volume);
        System.out.println("  Finetune: " + finetune);
        System.out.println("  Panning: " + panning);
        System.out.println("  Relativenote: " + relativeNote);
        System.out.println("  Loopstart: " + loopStart);
        System.out.println("  Loopend: " + loopEnd);
        System.out.println("  Bidi: " + bidi);
    }

    /*
     Decode the loop into the specified buffer.
     */
    public void getSamples(int samplePos, short[] buffer, int offset, int length) {
        short[] samples = this.samples;
        boolean fwd = true;
        int sidx = normalise(samplePos);
        if (sidx > loopEnd) {
            sidx = (loopEnd << 1) - sidx + 1;
            fwd = false;

        }
        int count;
        if (sidx < 0) {
            count = -samplePos;
            if (count > length)
                count = length;
            length -= count;
            sidx = 0;
            while (count-- > 0)
                buffer[offset++] = 0;
        }
        while (length > 0) {
            if (fwd) {
                count = loopEnd - sidx + 1;
                if (count > length)
                    count = length;
                length -= count;
                while (count-- > 0)
                    buffer[offset++] = samples[sidx++];
            } else {
                count = sidx - loopStart + 1;
                if (count > length)
                    count = length;
                length -= count;
                while (count-- > 0)
                    buffer[offset++] = samples[sidx--];
            }
            sidx = loopStart;
            if (bidi) {
                if (fwd)
                    sidx = loopEnd;
                fwd = !fwd;
            }
        }
    }

    public int normalise(int samplePos) {
        if (samplePos <= loopEnd)
            return samplePos;
        int llen = loopEnd - loopStart + 1;
        if (bidi)
            llen <<= 1;
        return loopStart + (samplePos - loopStart) % llen;
    }
}

