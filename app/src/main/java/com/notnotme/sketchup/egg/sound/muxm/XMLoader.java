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
import java.io.IOException;

/**
 * Loader for XM files.
 * 
 * @author Martin Cameron
 */
public class XMLoader {
    /*
     Load an XM file.

     header - 60 byte XM header.
     i      - positioned at the data after header.
     */
    public static Module loadXM(byte[] header, DataInput i) throws IOException {
        Module mod = new Module();
        mod.type = "XM";
        if (Loader.check(header) != 1)
            throw new IOException("MuXM: Not an XM file!");
        mod.songName = Loader.ascii2String(header, 17, 20);
        mod.trackerName = Loader.ascii2String(header, 38, 20);
        int ver = ushortle(header, 58);
        if (ver != 0x0104)
            throw new IOException("MuXM: Unsupported XM Version! (" + Integer.toHexString(ver) + ")");
        byte[] buf = new byte[4];
        i.readFully(buf);
        buf = new byte[intle(buf, 0)];
        i.readFully(buf, 4, buf.length - 4);
        mod.patternOrder = new int[ushortle(buf, 4)];
        mod.restart = ushortle(buf, 6);
        if (mod.restart >= mod.patternOrder.length)
            mod.restart = 0;
        int numChannels = ushortle(buf, 8);
        int numPatterns = ushortle(buf, 10);
        int numInsts = ushortle(buf, 12);
        int flags = ushortle(buf, 14);
        mod.linear = (flags & 0x1) > 0;
        mod.tempo = ushortle(buf, 16);
        mod.bpm = ushortle(buf, 18);
        mod.amiga = false;
        mod.xm = true;
        for (int n = 0; n < mod.patternOrder.length; n++)
            mod.patternOrder[n] = buf[n + 20];
        mod.patterns = new Pattern[numPatterns];
        for (int n = 0; n < numPatterns; n++)
            mod.patterns[n] = readXMPatt(i, numChannels);
        mod.instruments = new Instrument[numInsts + 1];
        for (int n = 1; n <= numInsts; n++)
            mod.instruments[n] = readXMInst(i);
        return mod;
    }

    private static Pattern readXMPatt(DataInput i, int chans) throws IOException {
        byte[] buf = new byte[4];
        i.readFully(buf);
        buf = new byte[intle(buf, 0)];
        i.readFully(buf, 4, buf.length - 4);
        if (buf[4] != 0)
            throw new IOException("Wrong Pattern Packing Type!");
        int rows = ushortle(buf, 5);
        buf = new byte[ushortle(buf, 7)];
        i.readFully(buf);
        // Unpack
        Note note = new Note();
        Pattern pat = new Pattern(rows, chans);
        int idx = 0, n = 0;
        while (idx < buf.length) {
            int flags = 0x1F;
            note.key = note.inst = note.vol = note.fx = note.fp = 0;
            if ((buf[idx] & 0x80) > 0)
                flags = buf[idx++] & 0xFF; // Packed
            if ((flags & 0x01) > 0)
                note.key = buf[idx++] & 0xFF;
            if ((flags & 0x02) > 0)
                note.inst = buf[idx++] & 0xFF;
            if ((flags & 0x04) > 0)
                note.vol = buf[idx++] & 0xFF;
            if ((flags & 0x08) > 0)
                note.fx = buf[idx++] & 0xFF;
            if ((flags & 0x10) > 0)
                note.fp = buf[idx++] & 0xFF;
            pat.setNote(note, n / chans, n % chans);
            n++;
        }
        return pat;
    }

    private static Instrument readXMInst(DataInput i) throws IOException {
        Instrument inst;
        byte[] buf = new byte[4];
        i.readFully(buf);
        buf = new byte[intle(buf, 0)];
        i.readFully(buf, 4, buf.length - 4);
        String name = Loader.ascii2String(buf, 4, 22);
        int numSamples = ushortle(buf, 27);
        if (numSamples == 0) {
            inst = new Instrument();
            inst.name = name;
        } else {
            int[] samtab = new int[96];
            for (int n = 0; n < 96; n++)
                samtab[n] = buf[33 + n];
            int numVPts = buf[225];
            int[] vtick = new int[numVPts];
            int[] vampl = new int[numVPts];
            for (int n = 0; n < numVPts; n++) {
                vtick[n] = ushortle(buf, 129 + n * 4);
                vampl[n] = ushortle(buf, 131 + n * 4);
            }
            int vsusp = buf[227];
            int vlpsp = buf[228];
            int vlpep = buf[229];
            boolean von = (buf[233] & 0x1) > 0;
            boolean vsus = (buf[233] & 0x2) > 0;
            boolean vloop = (buf[233] & 0x4) > 0;
            Envelope volEnv = new Envelope(vtick, vampl, von, vsus, vsusp, vloop, vlpsp, vlpep);
            int numPPts = buf[226];
            int[] ptick = new int[numPPts];
            int[] pampl = new int[numPPts];
            for (int n = 0; n < numPPts; n++) {
                ptick[n] = ushortle(buf, 177 + n * 4);
                pampl[n] = ushortle(buf, 179 + n * 4);
            }
            int psusp = buf[230];
            int plpsp = buf[231];
            int plpep = buf[232];
            boolean pon = (buf[234] & 0x1) > 0;
            boolean psus = (buf[234] & 0x2) > 0;
            boolean ploop = (buf[234] & 0x4) > 0;
            Envelope panEnv = new Envelope(ptick, pampl, pon, psus, psusp, ploop, plpsp, plpep);
            int vibt = buf[235];
            int vibs = buf[236];
            int vibd = buf[237];
            int vibr = buf[238];
            int vfad = ushortle(buf, 239);
            buf = new byte[numSamples * 40];
            i.readFully(buf);
            Sample[] samples = new Sample[numSamples];
            for (int n = 0; n < numSamples; n++)
                samples[n] = readXMSamp(buf, n * 40, i);
            inst = new Instrument(name, samples, volEnv, panEnv, samtab, vibt, vibs, vibd, vibr, vfad);
        }
        //inst.print();
        return inst;
    }

    /*
     buf - buffer containing sample headers
     offset - offset in buffer of sample header
     i - positioned at start of sample data.
     */
    private static Sample readXMSamp(byte[] buf, int offset, DataInput i) throws IOException {
        int slen = intle(buf, offset);
        int lsta = intle(buf, offset + 4);
        int lend = lsta + intle(buf, offset + 8) - 1;
        int vol = buf[offset + 12];
        int fine = buf[offset + 13];
        boolean loop = (buf[offset + 14] & 0x03) > 0;
        boolean bidi = (buf[offset + 14] & 0x02) > 0;
        boolean b16 = (buf[offset + 14] & 0x10) > 0;
        int pann = buf[offset + 15] & 0xFF;
        int reln = buf[offset + 16];
        String name = Loader.ascii2String(buf, offset + 18, 22);
        buf = new byte[slen];
        i.readFully(buf);
        if (b16) {
            slen >>= 1;
            lsta >>= 1;
            lend >>= 1;
        }
        short[] sdata = new short[slen + 1];
        if (b16) {
            short out = 0;
            for (int n = 0; n < buf.length; n += 2) {
                out += (short) (((buf[n + 1] & 0xFF) << 8) | (buf[n] & 0xFF));
                sdata[n >> 1] = out;
            }
        } else {
            byte out = 0;
            for (int n = 0; n < buf.length; n++) {
                out += (byte) buf[n];
                sdata[n] = (short) (out << 8);
            }
        }
        Sample s = new Sample(name, sdata, loop, lsta, lend, bidi, vol, pann, fine, reln);
        return s;
    }

    /* little endian */
    private static int ushortle(byte[] buf, int offset) {
        return (buf[offset] & 0xFF) | ((buf[offset + 1] & 0xFF) << 8);
    }

    /* little endian */
    private static int intle(byte[] buf, int offset) {
        int v = (buf[offset] & 0xFF) | ((buf[offset + 1] & 0xFF) << 8) | ((buf[offset + 2] & 0xFF) << 16)
                | ((buf[offset + 3] & 0xFF) << 24);
        return v;
    }
}

