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
 * An audio module.
 *
 * @author Martin Cameron
 */
public class Module {
	public String songName, trackerName;
	public boolean amiga, xm, linear;
	public int songLength, restart, tempo, bpm;

	public int[] patternOrder;
	public Pattern[] patterns;
	public Instrument[] instruments;
    public String type;

	public Module() {
		songName = "Empty Module";
		trackerName = "MuXM " + ModuleEngine.VERSION;
		xm = false;
		amiga = true;
		songLength = 1;
		tempo = 6;
		bpm = 125;
		patternOrder = new int[ 1 ];
		patterns = new Pattern[ 1 ];
		patterns[ 0 ] = new Pattern( 64, 4 );
		instruments = new Instrument[ 1 ];
		instruments[ 0 ] = new Instrument();
	}
}

