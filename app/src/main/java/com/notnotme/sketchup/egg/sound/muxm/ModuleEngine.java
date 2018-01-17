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
 * Main class for playing modules (MOD, XM, S3M).
 * 
 * @author Martin Cameron
 */
public class ModuleEngine {
    
    public static final String VERSION = "Alpha 31d";

    public static final int FP_SHIFT = 15;
    public static final int FP_ONE = 1 << FP_SHIFT;
    public static final int FP_MASK = FP_ONE - 1;

    private Module module;
    private Channel[] channels = new Channel[0];
    private Note note = new Note();

    private Resampler resampler;
    private final int INPUT_SAMPLES = 512;
    private final int OVERLAP_SAMPLES = 64;
    private final int VRAMP_SHIFT = 4;
    private final int VRAMP_LEN = 1 << VRAMP_SHIFT;
    private short[] input = new short[INPUT_SAMPLES + OVERLAP_SAMPLES * 2];
    private int[] lmixbuf, rmixbuf;

    private int sampleRate, gain;
    private int tickPos, tickLen;

    private int tick, tempo, bpm;
    private int row, nextRow;
    private int pattern, nextPattern;
    private int loopCount, loopChan;

    private boolean stereoEnabled;

    public ModuleEngine() {
        this(new Module());
    }

    public ModuleEngine(Module m) {
        //System.out.println("MuXM " + VERSION + " (c)2005 mumart@gmail.com");
        setSampleRate(44100);
        setGain(FP_ONE >> 2); // 0.25
        setResampler(new LinearResampler());
        setModule(m);
        setStereo(true);
    }

    public void setModule(Module m) {
        module = m;
        channels = new Channel[m.patterns[0].channels];
        reset();
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int rate) {
        sampleRate = rate;
        lmixbuf = new int[sampleRate / 10];
        rmixbuf = new int[sampleRate / 10];
    }

    /**
     * @param b
     */
    public void setStereo(boolean b) {
        stereoEnabled = b;
    }
    
    public boolean isStereo() {
        return stereoEnabled;
    }

    public void setGain(int gain) {
        this.gain = gain;
    }

    public void setResampler(Resampler r) {
        resampler = r;
    }

    public void reset() {
        Note gvol = new Note();
        gvol.vol = 64;
        for (int n = 0; n < channels.length; n++)
            channels[n] = new Channel(n, sampleRate, gvol, module.amiga, module.xm, module.linear);
        row = nextRow = 0;
        pattern = nextPattern = 0;
        loopCount = loopChan = 0;
        tick = tempo = module.tempo;
        bpm = module.bpm;
        row();
        getTick();
    }

    /*
     Calculate and return the length of the song in samples
     at the current sampling rate. This uses a simple
     calculation that should work for the majority of songs.
     */
    public int getSongLength() {
        reset();
        int length = getTickLength();
        while (!tick())
            length += getTickLength();
        reset();
        return length;
    }

    /*
     Set the playback position length samples from the beginning.
     */
    public void seek(int length) {
        reset();
        while (length > 0) {
            int count = length;
            int tickRem = tickLen - tickPos;
            if (count > tickRem)
                count = tickRem;
            tickPos += count;
            length -= count;
            if (tickPos >= tickLen) {
                // Tick
                tickLen = getTickLength();
                tickPos = 0;
                if (length >= tickLen) {
                    for (int n = 0; n < channels.length; n++) {
                        Channel c = channels[n];
                        c.sampleFrac += c.step * tickLen;
                        c.samplePos += c.sampleFrac >> ModuleEngine.FP_SHIFT;
                        c.sampleFrac &= ModuleEngine.FP_MASK;
                    }
                    tick();
                } else {
                    getTick();
                }
            }
        }
    }

    public void getAudio(byte[] buf, int offset, int frames) {
        getAudio(buf, offset, frames, stereoEnabled);
    }

    /*
     Write "frames" 16-bit little-endian frames of audio into the buffer provided.
     If "stereo" is true, a frame is 4 bytes, otherwise it is 2 bytes.
     */
    public void getAudio(byte[] buf, int offset, int frames, boolean stereo) {
        int bpos = offset;
        while (frames > 0) {
            int count = frames;
            int tickRem = tickLen - tickPos;
            if (count > tickRem)
                count = tickRem;
            if (stereo) {
                for (int n = 0; n < count; n++) {
                    int l = lmixbuf[tickPos + n];
                    int r = rmixbuf[tickPos + n];
                    buf[bpos++] = (byte) (l & 0xFF);
                    buf[bpos++] = (byte) (l >> 8);
                    buf[bpos++] = (byte) (r & 0xFF);
                    buf[bpos++] = (byte) (r >> 8);
                }
            } else {
                for (int n = 0; n < count; n++) {
                    int s = lmixbuf[tickPos + n] + rmixbuf[tickPos + n] >> 1;
                    buf[bpos++] = (byte) (s & 0xFF);
                    buf[bpos++] = (byte) (s >> 8);
                }
            }
            tickPos += count;
            frames -= count;
            if (tickPos >= tickLen)
                getTick();
        }
    }

    private void getTick() {
        tickLen = getTickLength();
        tickPos = 0;
        int rlen = tickLen + VRAMP_LEN;
        for (int n = 0; n < rlen; n++)
            lmixbuf[n] = rmixbuf[n] = 0;
        for (int n = 0; n < channels.length; n++) {
            Channel c = channels[n];
            resample(c, lmixbuf, rmixbuf, 0, rlen);
            c.sampleFrac += c.step * tickLen;
            c.samplePos += c.sampleFrac >> ModuleEngine.FP_SHIFT;
            c.sampleFrac &= ModuleEngine.FP_MASK;
        }
        for (int n = 0; n < VRAMP_LEN; n++) {
            int va = VRAMP_LEN - n;
            int vn = lmixbuf.length - va;
            lmixbuf[n] = lmixbuf[n] * n + lmixbuf[vn] * va >> VRAMP_SHIFT;
            rmixbuf[n] = rmixbuf[n] * n + rmixbuf[vn] * va >> VRAMP_SHIFT;
            lmixbuf[vn] = lmixbuf[tickLen + n];
            rmixbuf[vn] = rmixbuf[tickLen + n];
        }
        tick();
    }

    private void resample(Channel c, int[] l, int[] r, int offset, int len) {
        // Don't mix if silent
        int ampl = c.ampl;
        if (ampl == 0)
            return;
        ampl = ampl * gain >> FP_SHIFT;
        int pann = c.pann;
        int lvol = ampl * (FP_ONE - pann) >> FP_SHIFT;
        int rvol = ampl * (pann) >> FP_SHIFT;
        int step = c.step;
        int spos = c.samplePos;
        int frac = c.sampleFrac;
        int maxlen = INPUT_SAMPLES * FP_ONE / step;
        while (len > 0) {
            int count = maxlen;
            if (count > len)
                count = len;
            int isam = count * step >> FP_SHIFT;
            c.sample.getSamples(spos - OVERLAP_SAMPLES + 1, input, 0, isam + OVERLAP_SAMPLES * 2);
            resampler.resample(input, OVERLAP_SAMPLES - 1, frac, step, lvol, l, rvol, r, offset, count);
            frac += step * count;
            spos += frac >> ModuleEngine.FP_SHIFT;
            frac &= ModuleEngine.FP_MASK;
            offset += count;
            len -= count;
        }
    }

    /*
     Advance the song one tick. If the song end
     has been reached, true is returned.
     */
    private boolean tick() {
        tick--;
        if (tick <= 0) {
            tick = tempo;
            return row();
        }
        for (int n = 0; n < channels.length; n++)
            channels[n].tick();
        return false;
    }

    private boolean row() {
        // Decide whether to restart
        boolean songEnd = false;
        if (nextPattern < pattern)
            songEnd = true;
        if (nextPattern == pattern && nextRow <= row && loopCount <= 0)
            songEnd = true;
        // Jump to next row and pattern
        pattern = nextPattern;
        row = nextRow;
        // Decide where to go next
        nextRow = row + 1;
        if (nextRow >= module.patterns[module.patternOrder[pattern]].rows) {
            nextPattern = pattern + 1;
            nextRow = 0;
        }
        for (int n = 0; n < channels.length; n++) {
            module.patterns[module.patternOrder[pattern]].getNote(note, row, n);
            Instrument i = null;
            if (note.inst > 0 && note.inst < module.instruments.length)
                i = module.instruments[note.inst];
            channels[n].row(note.key, i, note.vol, note.fx, note.fp);
            int fp = note.fp & 0xFF;
            int fp1 = fp >> 4;
            int fp2 = fp & 0xF;
            switch (note.fx) {
                case Channel.FX_SET_SPEED :
                    if (fp < 32)
                        tick = tempo = note.fp;
                    else
                        bpm = note.fp;
                    break;
                case Channel.FX_PAT_JUMP :
                    if (loopCount <= 0) {
                        nextPattern = fp;
                        nextRow = 0;
                    }
                    break;
                case Channel.FX_PAT_BREAK :
                    if (loopCount <= 0) {
                        nextPattern = pattern + 1;
                        nextRow = fp1 * 10 + fp2;
                    }
                    break;
                case Channel.FX_EXTENDED :
                    switch (fp & 0xF0) {
                        case Channel.EFX_PAT_DELAY :
                            tick = tempo + tempo * fp2;
                            break;
                        case Channel.EFX_PAT_LOOP :
                            if (fp2 == 0)
                                channels[n].loopMark = row;
                            if (fp2 > 0 && channels[n].loopMark < row) {
                                if (loopCount <= 0) {
                                    loopCount = fp2;
                                    loopChan = n;
                                    nextRow = channels[n].loopMark;
                                    nextPattern = pattern;
                                } else if (loopChan == n) {
                                    if (loopCount == 1) {
                                        channels[n].loopMark = row + 1;
                                    } else {
                                        nextRow = channels[n].loopMark;
                                        nextPattern = pattern;
                                    }
                                    loopCount--;
                                }
                            }
                            break;
                    }
                    break;
            }
        }
        // Make sure next row and pattern are valid.
        if (nextPattern >= module.patternOrder.length)
            nextPattern = module.restart;
        if (module.patternOrder[nextPattern] >= module.patterns.length)
            nextPattern = 0;
        if (nextRow >= module.patterns[module.patternOrder[nextPattern]].rows)
            nextRow = 0;
        return songEnd;
    }

    private int getTickLength() {
        return (sampleRate * 2 + sampleRate / 2) / bpm;
    }

	public int getRow() {
		return row;
	}

	public int getPattern() {
		return pattern;
	}

}

