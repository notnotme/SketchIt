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
 * <p>Non-seekable 16 bit stereo RIFF-WAV format InputStream for an ModuleEngine object.</p>
 * <p>Playback positioning can be achieved by seeking the underlying ModuleEngine object.</p>
 */
public class Module2WavInputStream extends InputStream {
	private ModuleEngine engine;

	private final int OUT_BUF_FRAMES = 256;
	private byte[] outBuf = new byte[ OUT_BUF_FRAMES << 2 ];
	private int bufPos;

	public Module2WavInputStream(ModuleEngine engine) {
		this.engine = engine;
		reset();
	}

	public int read() {
		int out = outBuf[ bufPos++ ] & 0xFF;
		if( bufPos == outBuf.length ) newBuffer();
		return out;
	}

	public int read( byte[] buf, int offset, int len ) {
		int length = len;
		while( len > 0 ) {
			int count = len;
			int bufrem = outBuf.length - bufPos;
			if( count > bufrem ) count = bufrem;
			System.arraycopy( outBuf, bufPos, buf, offset, count );
			offset += count;
			bufPos += count;
			if( bufPos == outBuf.length ) newBuffer();
			len -= count;
		}
		return length;
	}

	public int available() {
		return outBuf.length - bufPos;
	}

	public void reset() {
		int hlen = WavHeader.writeHeader( outBuf, 2, engine.getSampleRate(), 2, engine.getSongLength() );
		engine.getAudio( outBuf, hlen, outBuf.length - hlen >> 2, true );
		bufPos = 0;
	}

	private void newBuffer() {
		engine.getAudio( outBuf, 0, outBuf.length >> 2, true );
		bufPos = 0;
	}

}

