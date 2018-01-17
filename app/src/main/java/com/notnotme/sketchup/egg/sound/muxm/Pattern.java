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
 * This class represents a sequence of notes.
 * 
 * @author Martin Cameron
 */
public class Pattern {
    public int rows, channels;
    public byte[] data;

    public Pattern(int rows, int channels) {
        this.rows = rows;
        this.channels = channels;
        data = new byte[rows * channels * 6];
    }

    public void getNote(Note n, int row, int channel) {
        int idx = (row * channels + channel) * 6;
        n.key = ((data[idx] & 0xFF) << 8) | (data[idx + 1] & 0xFF);
        n.inst = data[idx + 2] & 0xFF;
        n.vol = data[idx + 3] & 0xFF;
        n.fx = data[idx + 4] & 0xFF;
        n.fp = data[idx + 5] & 0xFF;
    }

    public void setNote(Note n, int row, int channel) {
        int idx = (row * channels + channel) * 6;
        data[idx] = (byte) (n.key >> 8);
        data[idx + 1] = (byte) (n.key & 0xFF);
        data[idx + 2] = (byte) n.inst;
        data[idx + 3] = (byte) n.vol;
        data[idx + 4] = (byte) n.fx;
        data[idx + 5] = (byte) n.fp;
    }

    public void print() {
        Note note = new Note();
        for (int r = 0; r < rows; r++) {
            System.out.println();
            for (int c = 0; c < channels; c++) {
                getNote(note, r, c);
                System.out.print(note.key + " " + note.inst + ", ");
            }
        }
    }

}

