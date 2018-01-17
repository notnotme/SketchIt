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

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Loader for MOD files.
 * 
 * @author Martin Cameron
 */
class ModLoader {
    /*
     Load a MOD file.

     header - 1084 byte MOD header.
     i      - positioned at the data after header.
     */
    public static Module loadMOD(byte[] header, DataInput i) throws IOException {
        Module mod = new Module();
        mod.type = "MOD";
        mod.bpm = 125;
        mod.tempo = 6;
        mod.amiga = false;
        mod.xm = false;
        mod.linear = false;
        int numChannels = 4;
        int id = Loader.check(header);
        if (id <= 2)
            throw new IOException("Not a valid MOD file!");
        if (id == 3)
            mod.amiga = true;
        if (id >= 4)
            numChannels = id;
        mod.songName = Loader.ascii2String(header, 0, 20);
        int songLength = header[950] & 0x7F;
        mod.patternOrder = new int[songLength];
        mod.restart = header[951] & 0x7F;
        if (mod.restart >= songLength)
            mod.restart = 0;
        for (int n = 0; n < songLength; n++)
            mod.patternOrder[n] = header[952 + n] & 0x7F;
        int numPatterns = 0;
        for (int n = 0; n < 128; n++) {
            int pat = header[952 + n] & 0x7F;
            if (pat >= numPatterns)
                numPatterns = pat + 1;
        }
        mod.patterns = new Pattern[numPatterns];
        for (int n = 0; n < mod.patterns.length; n++)
            mod.patterns[n] = readModPatt(i, numChannels);
        mod.instruments = new Instrument[32];
        for (int n = 1; n < 32; n++)
            mod.instruments[n] = readModInst(header, n, i);
        return mod;
    }

    /*
     header - mod header
     num - sample number (1-31)
     sdata - positioned at start of sample data
     */
    private static Instrument readModInst(byte[] header, int num, DataInput sdata) throws IOException {
        Instrument inst = new Instrument();
        int offset = (num - 1) * 30 + 20;
        inst.name = Loader.ascii2String(header, offset, 22);
        int sampleLength = ushortbe(header, offset + 22) << 1;
        int finetune = (header[offset + 24] & 0xF) << 4;
        if (finetune > 127)
            finetune -= 256;
        int volume = header[offset + 25] & 0x7F;
        int loopStart = ushortbe(header, offset + 26) << 1;
        int loopLength = ushortbe(header, offset + 28) << 1;
        boolean loop = true;
        if (loopLength < 4)
            loop = false;
        int loopEnd = loopStart + loopLength - 1;
        byte[] buf = new byte[sampleLength];
        try {
            sdata.readFully(buf);
        } catch (EOFException e) {
            System.out.println("MuXM: Instrument " + num + " incomplete!");
        }
        short[] samples = new short[sampleLength + 1];
        for (int n = 0; n < buf.length; n++)
            samples[n] = (short) (buf[n] << 8);
        inst.samples[0] = new Sample(inst.name, samples, loop, loopStart, loopEnd, false, volume, 128, finetune, 0);
        return inst;
    }

    private static Pattern readModPatt(DataInput i, int numChannels) throws IOException {
        Note n = new Note();
        Pattern pat = new Pattern(64, numChannels);
        byte[] buf = new byte[64 * numChannels * 4];
        i.readFully(buf);
        for (int row = 0; row < 64; row++) {
            for (int chn = 0; chn < numChannels; chn++) {
                int idx = (row * numChannels + chn) * 4;
                n.key = ((buf[idx] & 0x0F) << 8) | (buf[idx + 1] & 0xFF);
                n.inst = (buf[idx] & 0x10) | ((buf[idx + 2] & 0xF0) >> 4);
                n.vol = 0;
                n.fx = buf[idx + 2] & 0x0F;
                n.fp = buf[idx + 3] & 0xFF;
                pat.setNote(n, row, chn);
            }
        }
        return pat;
    }

    /* big endian */
    private static int ushortbe(byte[] buf, int offset) {
        return ((buf[offset] & 0xFF) << 8) | (buf[offset + 1] & 0xFF);
    }
}

