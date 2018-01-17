package com.notnotme.sketchup.egg.sound.muxm;

/*
 WAV Header format. All values little endian.

 CHAR[4] "RIFF"
 UINT32  Size of following data. Sample data length+36. Must be even.
 CHAR[4] "WAVE"
 CHAR[4] "fmt "
 UINT32  PCM Header chunk size = 16
 UINT16 0x0001 (PCM)
 UINT16 NumChannels
 UINT32 SampleRate
 UINT32 BytesPerSec = samplerate*frame size
 UINT16 frame Size (eg 4 bytes for stereo PCM16)
 UINT16 BitsPerSample
 CHAR[4] "data"
 UINT32 Length of sample data.
 <Samples>

 Example from real, valid wave file. 16 bit mono 8khz.

 524946466061000057415645666D74201000000001000100401F0000
 R I F F ^(dsiz) W A V E f m t   ^(PCMHS)^(P)^(C)^(Rate) 
 803E000002001000646174613C610000<Samples>
 ^(Byt/s)^(F)^(b)d a t a ^(slen)

 Total 44 bytes.

 Audio data that follows is:
 Unsigned if 8 bit.
 Signed little-endian if 16 bit or above.
 L-R interleaved if stereo.
 */

/**
 * A simple tool for computing and generating
 * basic, single chunk RIFF-WAV headers.
 * 
 * @author Martin Cameron
 */
public class WavHeader {
    
    /** Header size in bytes. */
    public static final int SIZE = 44;

    /**
     * <p>Write a WAV header for the specified format.</p>
     * <p>For a valid header you also need to specify the number of samples
     * that will immediately follow the header. This must result in an even number of bytes.</p>
     * 
     * @param outBuf the buffer to write header into, from index 0
     * @param channels the number of channels in audio stream.
     * @param rate the sampling rate.
     * @param bytes the number of bytes per sample (i.e. 2 for 16bit)
     * @param numSamples the number of samples of audio data to follow.
     * @return the number of bytes taken up by the header.
     */
    public static int writeHeader(byte[] outBuf, int channels, int rate, int bytes, int numSamples) {
        outBuf[0] = (byte) 0x52; //R
        outBuf[1] = (byte) 0x49; //I
        outBuf[2] = (byte) 0x46; //F
        outBuf[3] = (byte) 0x46; //F
        writeINT32LE(outBuf, 4, channels * bytes * numSamples + 36);
        outBuf[8] = (byte) 0x57; //W
        outBuf[9] = (byte) 0x41; //A
        outBuf[10] = (byte) 0x56; //V
        outBuf[11] = (byte) 0x45; //E
        outBuf[12] = (byte) 0x66; //f
        outBuf[13] = (byte) 0x6D; //m
        outBuf[14] = (byte) 0x74; //t
        outBuf[15] = (byte) 0x20; //Space
        writeINT32LE(outBuf, 16, 16);
        writeINT16LE(outBuf, 20, 1);
        writeINT16LE(outBuf, 22, channels);
        writeINT32LE(outBuf, 24, rate);
        writeINT32LE(outBuf, 28, channels * bytes * rate);
        writeINT16LE(outBuf, 32, channels * bytes);
        writeINT16LE(outBuf, 34, bytes * 8);
        outBuf[36] = (byte) 0x64; //d
        outBuf[37] = (byte) 0x61; //a
        outBuf[38] = (byte) 0x74; //t
        outBuf[39] = (byte) 0x61; //a
        writeINT32LE(outBuf, 40, channels * bytes * numSamples);
        return SIZE;
    }

    /** Write a little-endian short into the specified array. */
    public static void writeINT16LE(byte[] outBuf, int offset, int value) {
        outBuf[offset] = (byte) (value & 0xFF);
        outBuf[offset + 1] = (byte) (value >> 8);
    }

    /** Write a little-endian int into the specified array. */
    public static void writeINT32LE(byte[] outBuf, int offset, int value) {
        outBuf[offset] = (byte) (value & 0xFF);
        outBuf[offset + 1] = (byte) ((value >> 8) & 0xFF);
        outBuf[offset + 2] = (byte) ((value >> 16) & 0xFF);
        outBuf[offset + 3] = (byte) (value >> 24);
    }
}

