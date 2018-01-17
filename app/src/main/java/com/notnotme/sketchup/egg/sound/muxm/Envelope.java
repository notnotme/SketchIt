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
 * Envelope generator.
 * 
 * @author Martin Cameron
 */
class Envelope {
    public int[] tick, ampl;
    public int susPoint, loopStart, loopEnd;
    public boolean on, sustain, loop;

    public Envelope() {
        this(new int[1], new int[1], false, false, 0, false, 0, 0);
    }

    public Envelope(int[] tick, int[] ampl, boolean on, boolean sustain, int susPoint, boolean loop, int loopStart,
            int loopEnd) {
        if (tick.length != ampl.length || tick.length == 0)
            on = false;
        for (int n = 1; n < tick.length; n++)
            if (tick[n] <= tick[n - 1])
                on = false;
        for (int n = 0; n < ampl.length; n++)
            if (ampl[n] < 0 || ampl[n] > 64)
                on = false;
        if (!on) {
            tick = new int[1];
            ampl = new int[1];
        }
        tick[0] = 0;
        if (susPoint < 0 || susPoint >= tick.length)
            sustain = false;
        if (loopStart < 0 || loopEnd < 0)
            loop = false;
        if (loopStart >= tick.length || loopEnd >= tick.length)
            loop = false;
        if (loopEnd < loopStart)
            loop = false;
        if (!sustain)
            susPoint = 0;
        if (!loop)
            loopStart = loopEnd = 0;
        this.tick = tick;
        this.ampl = ampl;
        this.on = on;
        this.sustain = sustain;
        this.susPoint = susPoint;
        this.loop = loop;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
    }

    /*
     Updates the envelope position given the previous one.
     */
    public int update(int t, boolean keyOn) {
        t++;
        if (loop && t >= tick[loopEnd])
            t = tick[loopStart];
        if (sustain && keyOn && t >= tick[susPoint])
            t = tick[susPoint];
        return t;
    }

    /*
     Returns the envelope value for the specified tick.
     */
    public int calculate(int t) {
        int point = 0;
        if (!on)
            return 0;
        if (t >= tick[tick.length - 1])
            return ampl[tick.length - 1];
        for (int n = 0; n < tick.length; n++)
            if (tick[n] <= t)
                point = n;
        int da = ampl[point + 1] - ampl[point];
        int dt = tick[point + 1] - tick[point];
        int m = (da << ModuleEngine.FP_SHIFT) / dt;
        return (m * (t - tick[point]) >> ModuleEngine.FP_SHIFT) + ampl[point];
    }
}

