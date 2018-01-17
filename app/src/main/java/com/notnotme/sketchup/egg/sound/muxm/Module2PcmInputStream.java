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

import java.io.InputStream;

/**
 * Seekable InputStream.
 * Outputs 16 bit signed, stereo, little endian audio.
 * 
 * @author Martin Cameron
 */
public class Module2PcmInputStream extends InputStream {
    
    private ModuleEngine engine;

    private final int OUT_BUF_FRAMES = 256;
    private byte[] outBuf;
    private int bufPos, bytePos, byteMark;
    private int frameSizeShift;

    private boolean first = true;

    public Module2PcmInputStream(ModuleEngine ibxm) {
        
        this.engine = ibxm;
        
        frameSizeShift = engine.isStereo() ? 2 : 1;
        outBuf = new byte[OUT_BUF_FRAMES << frameSizeShift];
        
        ibxm.reset();
        newBuffer();
        
    }

    public int available() {
        return outBuf.length - bufPos;
    }

    public void mark(int limit) {
        byteMark = bytePos;
    }

    public boolean markSupported() {
        return true;
    }

    public int read() {
        
        int out = outBuf[bufPos++] & 0xFF;
        if (bufPos == outBuf.length)
            newBuffer();
        bytePos++;
        return out;
        
    }
    
    public int read(byte[] buf) {
        return read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int offset, int len) {
      
        int length = len;
        while (len > 0) {
            int count = len;
            int bufRem = outBuf.length - bufPos;
            if (count > bufRem)
                count = bufRem;
            System.arraycopy(outBuf, bufPos, buf, offset, count);
            bytePos += count;
            offset += count;
            bufPos += count;
            if (bufPos == outBuf.length)
                newBuffer();
            len -= count;
        }
        return length;
    }

    public void reset() {
        engine.seek(byteMark >> frameSizeShift);
        newBuffer();
        bytePos = byteMark;
        bufPos = byteMark % 4;
    }

    public long skip(long bytes) {
        byteMark = bytePos + (int) bytes;
        if (byteMark < 0)
            byteMark = 0;
        reset();
        return bytes;
    }

    private void newBuffer() {
        engine.getAudio(outBuf, 0, outBuf.length >> frameSizeShift);
        bufPos = 0;
    }
}

