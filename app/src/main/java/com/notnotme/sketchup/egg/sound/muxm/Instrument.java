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
 * An Instrument.
 * 
 * @author Martin Cameron
 */
public class Instrument {
    public String name;
    public Sample[] samples;
    public Envelope volEnv, panEnv;
    public int[] sampleTable;
    public int vibratoType, vibratoSweep, vibratoDepth, vibratoRate;
    public int fadeOut;

    public Instrument() {
        this("Empty Instrument", new Sample[]{new Sample()}, new Envelope(), new Envelope(), new int[96], 0, 0, 0, 0, 0);
    }

    public Instrument(String name, Sample[] samples, Envelope volEnv, Envelope panEnv, int[] sampleTable,
            int vibratoType, int vibratoSweep, int vibratoDepth, int vibratoRate, int fadeOut) {
        this.name = name;
        if (samples.length == 0)
            samples = new Sample[]{new Sample()};
        this.samples = samples;
        this.volEnv = volEnv;
        this.panEnv = panEnv;
        this.sampleTable = new int[96];
        for (int n = 0; n < sampleTable.length; n++) {
            int sam = sampleTable[n];
            if (sam >= samples.length | sam < 0)
                sam = 0;
            this.sampleTable[n] = sam;
        }
        this.vibratoType = vibratoType;
        this.vibratoSweep = vibratoSweep;
        this.vibratoDepth = vibratoDepth;
        this.vibratoRate = vibratoRate;
        this.fadeOut = fadeOut;
    }

    public void print() {
        System.out.println("Instrument:");
        System.out.println("  Name: " + name);
        System.out.println("  Sample table: ");
        for (int n = 0; n < sampleTable.length; n++)
            System.out.print(sampleTable[n] + ",");
        System.out.println();
        System.out.println("  Samples:");
        for (int n = 0; n < samples.length; n++)
            samples[n].print();
        //volEnv.print();
    }

}

