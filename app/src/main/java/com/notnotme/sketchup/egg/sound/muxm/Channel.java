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
 * Channel processor.
 * 
 * @author Martin Cameron
 */
class Channel {
    
    public static final int FX_ARPEGGIO = 0x00, FX_PORTA_UP = 0x01, FX_PORTA_DOWN = 0x02, FX_TONE_PORTA = 0x03,
            FX_VIBRATO = 0x04, FX_TONE_PORTA_VOL = 0x05, FX_VIBRATO_VOL = 0x06, FX_TREMOLO = 0x07,
            FX_SET_PANNING = 0x08, FX_SET_SAMPLEPOS = 0x09, FX_VOLUME_SLIDE = 0x0A, FX_PAT_JUMP = 0x0B,
            FX_SET_VOLUME = 0x0C, FX_PAT_BREAK = 0x0D, FX_EXTENDED = 0x0E, FX_SET_SPEED = 0x0F, FX_SET_GVOL = 0x10,
            FX_GVOL_SLIDE = 0x11, FX_KEY_OFF = 0x14, FX_SET_ENV_POS = 0x15, FX_PANNING_SLIDE = 0x19,
            FX_MULTI_RETRIG = 0x1B, FX_TREMOR = 0x1D, FX_EX_FINE_PORTA = 0x21;

    public static final int EFX_FINE_PORTA_UP = 0x10, EFX_FINE_PORTA_DN = 0x20, EFX_SET_GLISSANDO = 0x30,
            EFX_SET_VIBR_TYPE = 0x40, EFX_SET_FINE_TUNE = 0x50, EFX_PAT_LOOP = 0x60, EFX_SET_TREM_TYPE = 0x70,
            EFX_SET_PANNING = 0x80, EFX_RETRIG = 0x90, EFX_FINE_VOL_UP = 0xA0, EFX_FINE_VOL_DN = 0xB0,
            EFX_NOTE_CUT = 0xC0, EFX_NOTE_DELAY = 0xD0, EFX_PAT_DELAY = 0xE0, EFX_INVERT_LOOP = 0xF0;

    public static final int VC_VOL_SLIDE_DOWN = 0x60, VC_VOL_SLIDE_UP = 0x70, VC_FINE_VOL_DOWN = 0x80,
            VC_FINE_VOL_UP = 0x90, VC_SET_VIBR_SPEED = 0xA0, VC_VIBRATO = 0xB0, VC_SET_PANNING = 0xC0,
            VC_PAN_SLIDE_L = 0xD0, VC_PAN_SLIDE_R = 0xE0, VC_TONE_PORTA = 0xF0;

    public Sample sample;
    public int samplePos, sampleFrac;
    public int ampl, pann, step;
    public int chID, loopMark;

    private Instrument instrument;
    private Envelope volenv, panenv;
    private int samplingRate, midc;
    private boolean amiga, xm, linear;
    private Note gvol;

    private int period, finetune, volume, panning;
    private int porta, arpeggio, vibrato, tremolo;

    // Current fx
    private Instrument ins;
    private int key, vc, fx, fp;
    private int[] fxstore, efxstore;
    private int fxCounter, vtCounter, avCounter, tkCounter;
    private int venvpos, penvpos;
    private int vfadeout;
    private boolean keyon;

    private final int[] sinTable = new int[]{0, 24, 49, 74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
            255, 253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120, 97, 74, 49, 24};

    /*
     num - channel id, used to set panning for amiga mods.
     samplingRate - the sampling rate.
     gvol - Note object used to store global volume (pass the same object to each channel)
     amiga - use amiga trigger quirks and PAL tuning.
     linear - use linear periods.
     */
    public Channel(int num, int samplingRate, Note gvol, boolean amiga, boolean xm, boolean linear) {
        this.chID = num;
        this.samplingRate = samplingRate;
        this.gvol = gvol;
        this.amiga = amiga;
        this.xm = xm;
        this.linear = linear;
        midc = amiga ? 8287 : 8363;
        panning = 128;
        if (!xm)
            switch (chID & 0x3) {
                case 0 :
                    panning = 64;
                    break;
                case 1 :
                    panning = 192;
                    break;
                case 2 :
                    panning = 192;
                    break;
                case 3 :
                    panning = 64;
                    break;
            }
        fxstore = new int[33];
        efxstore = new int[16];
        instrument = new Instrument();
        sample = instrument.samples[0];
        row(48, null, 0, 0, 0);
    }

    /*
     Called once every row.
     */
    public void row(int key, Instrument i, int vc, int fx, int fp) {
        fxCounter = 0;

        // Store current fx
        this.ins = i;
        this.key = key;
        this.vc = vc;
        this.fx = fx;
        this.fp = fp;
        if (fx < fxstore.length && fp != 0)
            fxstore[fx] = fp;
        if (fx == FX_EXTENDED && (fp & 0x0F) != 0)
            efxstore[(fp & 0xF0) >> 4] = fp & 0x0F;

        // Handle note delay.
        if (fx == FX_EXTENDED && (fp & 0xF0) == EFX_NOTE_DELAY) {
            tick();
            return;
        }

        // Handle note
        trigger(i, key);

        // Handle fx
        arpeggio = vibrato = tremolo = 0;
        if (vc >= 0x10 && vc <= 0x50)
            volume = vc - 0x10;
        switch (vc & 0xF0) {
            case VC_FINE_VOL_DOWN :
                volume -= vc & 0x0F;
                if (volume <= 0)
                    volume = 0;
                break;
            case VC_FINE_VOL_UP :
                volume += vc & 0x0F;
                if (volume >= 64)
                    volume = 64;
                break;
            case VC_SET_VIBR_SPEED :
                int vparam = fxstore[FX_VIBRATO] & 0x0F;
                fxstore[FX_VIBRATO] = vparam | ((vc & 0x0F) << 4);
                break;
            case VC_VIBRATO :
                vparam = fxstore[FX_VIBRATO] & 0xF0;
                fxstore[FX_VIBRATO] = vparam | (vc & 0x0F);
                vibrato();
                break;
            case VC_SET_PANNING :
                panning = (vc & 0x0F) << 4;
                break;
            case VC_TONE_PORTA :
                fxstore[FX_TONE_PORTA] = vc & 0x0F;
                break;
        }
        switch (fx) {
            case FX_VIBRATO :
                vibrato();
                break;
            case FX_VIBRATO_VOL :
                vibrato();
                break;
            case FX_SET_PANNING :
                if (!amiga)
                    panning = fp;
                break;
            case FX_SET_SAMPLEPOS :
                samplePos = fp << 8;
                sampleFrac = 0;
                break;
            case FX_SET_VOLUME :
                volume = fp;
                if (volume > 64)
                    volume = 64;
                break;
            case FX_EXTENDED :
                switch (fp & 0xF0) {
                    case EFX_FINE_PORTA_UP :
                        period -= efxstore[EFX_FINE_PORTA_UP >> 4] << 2;
                        break;
                    case EFX_FINE_PORTA_DN :
                        period += efxstore[EFX_FINE_PORTA_DN >> 4] << 2;
                        break;
                    case EFX_FINE_VOL_UP :
                        volume += efxstore[EFX_FINE_VOL_UP >> 4];
                        if (volume > 64)
                            volume = 64;
                        break;
                    case EFX_FINE_VOL_DN :
                        volume -= efxstore[EFX_FINE_VOL_DN >> 4];
                        if (volume < 0)
                            volume = 0;
                        break;
                }
                break;
            case FX_SET_GVOL :
                gvol.vol = fp;
                if (gvol.vol > 64)
                    gvol.vol = 64;
                break;
            case FX_KEY_OFF :
                keyon = false;
                break;
            case FX_SET_ENV_POS :
                venvpos = penvpos = fp;
                break;
            case FX_MULTI_RETRIG :
                multiretrig();
                break;
            case FX_TREMOR :
                break;
            case FX_EX_FINE_PORTA :
                switch (fp & 0xF0) {
                    case 0x10 :
                        period -= fxstore[FX_EX_FINE_PORTA & 0xF];
                        break;
                    case 0x20 :
                        period += fxstore[FX_EX_FINE_PORTA & 0xF];
                        break;
                }
                break;
        }
        update();
        fxCounter++;
        tkCounter++;
    }

    /*
     Called once every tick, between calls to row.
     */
    public void tick() {
        // Handle note delay.
        if (fx == FX_EXTENDED && (fp & 0xF0) == EFX_NOTE_DELAY && (fp & 0x0F) == fxCounter) {
            row(key, ins, vc, 0, 0);
            return;
        }

        arpeggio = vibrato = tremolo = 0;
        switch (vc & 0xF0) {
            case VC_VOL_SLIDE_DOWN :
                volume -= vc & 0xF;
                if (volume < 0)
                    volume = 0;
                break;
            case VC_VOL_SLIDE_UP :
                volume += vc & 0xF;
                if (volume > 64)
                    volume = 64;
                break;
            case VC_VIBRATO :
                vibrato();
                break;
            case VC_PAN_SLIDE_L :
                panning -= vc & 0xF;
                if (panning < 0)
                    panning = 0;
                break;
            case VC_PAN_SLIDE_R :
                panning += vc & 0xF;
                if (panning > 255)
                    panning = 255;
                break;
            case VC_TONE_PORTA :
                porta();
                break;
        }
        switch (fx) {
            case FX_ARPEGGIO :
                if (fxCounter % 3 == 1)
                    arpeggio = (fp & 0xF0) >> 4;
                if (fxCounter % 3 == 2)
                    arpeggio = fp & 0x0F;
                break;
            case FX_PORTA_UP :
                period -= fxstore[FX_PORTA_UP] << 2;
                break;
            case FX_PORTA_DOWN :
                period += fxstore[FX_PORTA_DOWN] << 2;
                break;
            case FX_TONE_PORTA :
                porta();
                break;
            case FX_VIBRATO :
                vibrato();
                break;
            case FX_TONE_PORTA_VOL :
                volume += (fxstore[FX_TONE_PORTA_VOL] & 0xF0) >> 4;
                volume -= fxstore[FX_TONE_PORTA_VOL] & 0x0F;
                if (volume > 64)
                    volume = 64;
                if (volume < 0)
                    volume = 0;
                porta();
                break;
            case FX_VIBRATO_VOL :
                volume += (fxstore[FX_VIBRATO_VOL] & 0xF0) >> 4;
                volume -= fxstore[FX_VIBRATO_VOL] & 0x0F;
                if (volume > 64)
                    volume = 64;
                if (volume < 0)
                    volume = 0;
                vibrato();
                break;
            case FX_TREMOLO :
                int tspeed = (fxstore[FX_TREMOLO] & 0xF0) >> 4;
                int tdepth = fxstore[FX_TREMOLO] & 0x0F;
                tremolo = waveform(vtCounter * tspeed) * tdepth >> 7;
                break;
            case FX_VOLUME_SLIDE :
                volume += (fxstore[FX_VOLUME_SLIDE] & 0xF0) >> 4;
                volume -= fxstore[FX_VOLUME_SLIDE] & 0x0F;
                if (volume > 64)
                    volume = 64;
                if (volume < 0)
                    volume = 0;
                break;
            case FX_EXTENDED :
                switch (fp & 0xF0) {
                    case EFX_RETRIG :
                        int rtparam = fp & 0x0F;
                        if (rtparam == 0)
                            rtparam = 1;
                        if (fxCounter % rtparam == 0)
                            samplePos = sampleFrac = 0;
                        break;
                    case EFX_NOTE_CUT :
                        if (fxCounter == (fp & 0x0F))
                            volume = 0;
                        break;
                }
                break;
            case FX_GVOL_SLIDE :
                gvol.vol += (fxstore[FX_GVOL_SLIDE] & 0xF0) >> 4;
                gvol.vol -= fxstore[FX_GVOL_SLIDE] & 0x0F;
                if (gvol.vol < 0)
                    gvol.vol = 0;
                if (gvol.vol > 64)
                    gvol.vol = 64;
                break;
            case FX_PANNING_SLIDE :
                panning += (fxstore[FX_PANNING_SLIDE] & 0xF0) >> 4;
                panning -= fxstore[FX_PANNING_SLIDE] & 0x0F;
                if (panning > 255)
                    panning = 255;
                if (panning < 0)
                    panning = 0;
                break;
            case FX_MULTI_RETRIG :
                multiretrig();
                break;
        }
        update();
        fxCounter++;
        vtCounter++;
        tkCounter++;
    }

    private int getStep() {
        int l2Freq, p = period + vibrato;
        if (p < 27)
            p = 27; // Limit freq to 0.5mhz :)
        if (p > 32768)
            p = 32768;
        if (linear)
            l2Freq = LogTable.log2(midc) + (4608 - p) * ModuleEngine.FP_ONE / 768;
        else
            l2Freq = LogTable.log2(midc * 1712) - LogTable.log2(p);
        l2Freq += (finetune << ModuleEngine.FP_SHIFT - 7) / 12;
        l2Freq += (arpeggio << ModuleEngine.FP_SHIFT) / 12;
        return LogTable.pow2(l2Freq - LogTable.log2(samplingRate));
    }

    private int getAmpl() {
        int envvol = 64;
        if (instrument.volEnv.on)
            envvol = instrument.volEnv.calculate(venvpos);
        else if (!keyon)
            envvol = 0;
        int vol = volume + tremolo;
        if (vol > 64)
            vol = 64;
        if (vol < 0)
            vol = 0;
        int amp = vol << ModuleEngine.FP_SHIFT - 6;
        amp = amp * envvol >> 6;
        amp = amp * gvol.vol >> 6;
        return amp * (vfadeout >> 1) >> 15;
    }

    private int getPann() {
        int pan = panning << ModuleEngine.FP_SHIFT - 8;
        int envpan = 32;
        if (instrument.panEnv.on)
            envpan = instrument.panEnv.calculate(penvpos);
        int envrange = ModuleEngine.FP_ONE - pan;
        if (envrange > pan)
            envrange = pan;
        pan += envrange * (envpan - 32) >> 5;
        return pan;
    }

    /* Calculate amplitute, frequency and update envelopes. */
    private void update() {
        // Calculate autovibrato
        int avdepth = instrument.vibratoDepth & 0xF;
        int avspeed = instrument.vibratoRate & 0x3F;
        int avsweep = instrument.vibratoSweep & 0xFF;
        if (avCounter < avsweep)
            avdepth = avdepth * avCounter / avsweep;
        vibrato += waveform(avCounter * avspeed) * avdepth >> 9;
        // Calculate mix parameters
        step = getStep();
        ampl = getAmpl();
        pann = getPann();
        // Check if sample end reached, if so hint the mixer that it shouldn't bother mixing.
        if (samplePos > sample.loopEnd && sample.loopEnd - sample.loopStart == 0)
            ampl = 0;
        // Update envelopes etc
        if (keyon)
            avCounter++;
        if (instrument.volEnv.on) {
            if (!keyon) {
                vfadeout -= instrument.fadeOut << 1;
                if (vfadeout < 0)
                    vfadeout = 0;
            }
            venvpos = instrument.volEnv.update(venvpos, keyon);
        }
        if (instrument.panEnv.on)
            penvpos = instrument.panEnv.update(penvpos, keyon);
    }

    private void trigger(Instrument i, int key) {
        if (i != null) {
            instrument = i;
            Sample sam = i.samples[0];
            if (amiga && samplePos > sample.loopStart)
                sample = sam;
            volume = sam.volume;
            if (xm)
                panning = sam.panning;
            finetune = sam.finetune;
            venvpos = penvpos = 0;
            vfadeout = 65536;
            keyon = true;
        }
        if (key <= 0)
            return;
        if (key == 97) {
            keyon = false;
            return;
        }
        int samidx = 0;
        if (key < 97)
            samidx = instrument.sampleTable[key - 1];
        sample = instrument.samples[samidx];
        porta = toPeriod(key);
        vtCounter = avCounter = tkCounter = 0;
        if (fx == FX_TONE_PORTA)
            return;
        if (fx == FX_TONE_PORTA_VOL)
            return;
        if (((vc & 0xF0) >> 4) == VC_TONE_PORTA)
            return;
        period = porta;
        samplePos = sampleFrac = 0;
    }

    /*
     Convert the specified key value into a period.
     */
    private int toPeriod(int key) {
        if (!xm)
            return key << 2;
        key += sample.relativeNote;
        if (linear)
            return 7744 - key * 64;
        int l2p = LogTable.log2(29024) - key * ModuleEngine.FP_ONE / 12;
        return LogTable.pow2(l2p) >> ModuleEngine.FP_SHIFT;
    }

    private void porta() {
        if (period > porta) {
            period -= fxstore[FX_TONE_PORTA] << 2;
            if (period < porta)
                period = porta;
        }
        if (period < porta) {
            period += fxstore[FX_TONE_PORTA] << 2;
            if (period > porta)
                period = porta;
        }
    }

    private void vibrato() {
        int vspeed = (fxstore[FX_VIBRATO] & 0xF0) >> 4;
        int vdepth = fxstore[FX_VIBRATO] & 0x0F;
        vibrato += waveform(vtCounter * vspeed) * vdepth >> 5;
    }

    private int waveform(int x) {
        int t = x & 0x3F;
        int out = sinTable[t & 0x1F];
        if (t > 0x1F)
            out = -out;
        return out;
    }

    private void multiretrig() {
        int rtparam = fxstore[FX_MULTI_RETRIG] & 0x0F;
        if (rtparam == 0)
            rtparam = 1;
        if (tkCounter % rtparam == 0) {
            samplePos = sampleFrac = 0;
            switch ((fxstore[FX_MULTI_RETRIG] & 0xF0) >> 4) {
                case 0x1 :
                    volume -= 1;
                    break;
                case 0x2 :
                    volume -= 2;
                    break;
                case 0x3 :
                    volume -= 4;
                    break;
                case 0x4 :
                    volume -= 8;
                    break;
                case 0x5 :
                    volume -= 16;
                    break;
                case 0x6 :
                    volume -= volume / 3;
                    break;
                case 0x7 :
                    volume /= 2;
                    break;
                case 0x9 :
                    volume += 1;
                    break;
                case 0xA :
                    volume += 2;
                    break;
                case 0xB :
                    volume += 4;
                    break;
                case 0xC :
                    volume += 8;
                    break;
                case 0xD :
                    volume += 16;
                    break;
                case 0xE :
                    volume += volume / 2;
                    break;
                case 0xF :
                    volume *= 2;
                    break;
            }
            if (volume < 0)
                volume = 0;
            if (volume > 64)
                volume = 64;
        }
    }
}

