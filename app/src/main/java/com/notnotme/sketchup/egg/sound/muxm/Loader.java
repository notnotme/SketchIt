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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Factory to get Module from an input stream.
 * 
 * @author Martin Cameron
 */
public class Loader {
    
    /**
     * Returns true if the InputStream contains a valid module.
     */
    public static boolean identify(InputStream i) throws IOException {
        DataInputStream dis = new DataInputStream(i);
        byte[] buf = new byte[1084];
        dis.readFully(buf, 0, 60);
        if (check(buf) > 0)
            return true;
        dis.readFully(buf, 60, 1024);
        if (check(buf) > 0)
            return true;
        return false;
    }

    /**
     * Identify and load a module file from the InputStream.
     */
    public static Module load(InputStream i) throws IOException {
        DataInputStream dis = new DataInputStream(i);
        byte[] buf = new byte[1084];
        dis.readFully(buf, 0, 60);
        if (check(buf) == 1)
            return XMLoader.loadXM(buf, dis);
        dis.readFully(buf, 60, 1024);
        if (check(buf) >= 3)
            return ModLoader.loadMOD(buf, dis);
        return null;
        
    }

    /*
    Check the module id.
    Returns:
    0  - Not Identified.
    1  - XM
    2  - S3M
    3  - Amiga 4 Channel MOD.
    4+ - PC MOD with n channels.
    */
    protected static int check(byte[] buf) throws IOException {
        String type = ascii2String(buf, 0, 17);
        if (type.equals("Extended Module: "))
            return 1;
        type = ascii2String(buf, 44, 4);
        if (type.equals("SCRM"))
            return 2;
        type = ascii2String(buf, 1080, 4);
        if (type.equals("M.K."))
            return 3;
        if (type.equals("M!K!"))
            return 3;
        if (type.equals("FLT4"))
            return 3;
        type = ascii2String(buf, 1081, 3);
        if (type.equals("CHN"))
            return buf[1080] - 48;
        type = ascii2String(buf, 1082, 2);
        if (type.equals("CH"))
            return ((buf[1080] - 48) * 10) + (buf[1081] - 48);
        return 0;
    }

    protected static String ascii2String(byte[] buf, int offset, int len) throws IOException {
        
        byte[] str = new byte[len];
        System.arraycopy(buf, offset, str, 0, len);
        for (int n = 0; n < len; n++)
            if (str[n] < 32)
                str[n] = 32;

        // Force to 8859_1 (Latin1) encoding
        String s = null;
        try {
            s = new String(str, 0, len, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            try {
                s = new String(str, 0, len, "ISO8859-1");
            } catch (UnsupportedEncodingException e2) {
                s = new String(str, 0, len, "8859-1");
            }
        }

        return s;
    }

}

