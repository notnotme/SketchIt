package com.notnotme.sketchup.egg.sound;

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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.notnotme.sketchup.egg.sound.muxm.Loader;
import com.notnotme.sketchup.egg.sound.muxm.ModuleEngine;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>A simple class that uses MuXM API and JavaSound to play audio from given file names.</p>
 *
 * <p>This example uses getAudio() methods of the MuXM class to get the audio data.</p>
 *
 * @author Martin Cameron
 */
public final class ModulePlayer implements Runnable {

    private final int SAMPLE_RATE = 44100;

    private final ModuleEngine mEngine;
	private final State mState;
	private boolean mSongLoop;
	private boolean mRunning;
	private boolean mPaused;

    public ModulePlayer(InputStream f) throws IOException {
        mEngine = new ModuleEngine(Loader.load(f));
        mEngine.setSampleRate(SAMPLE_RATE);
		mState = new State();
        setLoop(true);
    }

    /**
     *	Set whether the song is to loop continuously or not.
     *	The default is to loop.
     */
    public void setLoop(boolean loop) {
        mSongLoop = loop;
    }

    /**
     *	Begin playback.
     *	This method will return once the song has finished, or stop has been called.
     */
    public void run() {
        mRunning = true;
        int bufframes = 1024;
        byte[] obuf = new byte[bufframes << 2];

		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufframes, AudioTrack.MODE_STREAM);
		track.play();

		int songlen = mEngine.getSongLength();
		int remain = songlen;
		while (remain > 0 && mRunning) {
			if(mPaused) {
				try { Thread.sleep(10); }
				catch (InterruptedException e) { }
				continue;
			}

			int count = bufframes;
			if (count > remain) {
                count = remain;
            }

			mEngine.getAudio(obuf, 0, count, true);
			track.write(obuf, 0, count << 2);
			remain -= count;

			if (remain <= 0 && mSongLoop) {
                remain = songlen;
            }
		}

		track.stop();
		track.release();
    }

    /**
     *	Instruct the run() method to finish playing and return.
     */
    public void stop() {
        mRunning = false;
    }

	public void setPaused(boolean paused) {
		this.mPaused = paused;
	}

	public boolean isPaused() {
		return mPaused;
	}

	public State getState() {
		mState.row = mEngine.getRow();
		mState.pattern = mEngine.getPattern();
		return mState;
	}

	public static class State {
		public int row;
        public int pattern;

		State() {
			row = pattern = 0;
		}
	}

}

