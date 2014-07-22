/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.bmh.audio.impl.algorithm;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SOURCE:
 * http://thorntonzone.com/manuals/Compression/Fax,%20IBM%20MMR/MMSC/mmsc
 * /uk/co/mmscomputing/sound/
 * 
 * License: GNU Lesser General Public License
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CompressInputStream extends FilterInputStream {

    /*
     * Convert mono PCM byte stream into A-Law u-Law byte stream
     * 
     * static AudioFormat alawformat= new
     * AudioFormat(AudioFormat.Encoding.ALAW,8000,8,1,1,8000,false); static
     * AudioFormat ulawformat= new
     * AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);
     * 
     * PCM 8000.0 Hz, 16 bit, mono, SIGNED, little-endian static AudioFormat
     * pcmformat = new AudioFormat(8000,16,1,true,false);
     */

    /*
     * Added by bkowal 06/24/2014
     */
    public static enum COMPRESSION_TYPE {
        /* Enum constant for alaw compression */
        ALAW(new ALawCompressor()),
        /* Enum constant for ulaw compression */
        ULAW(new uLawCompressor());

        private Compressor compressor;

        private COMPRESSION_TYPE(Compressor compressor) {
            this.compressor = compressor;
        }

        public Compressor getCompressor() {
            return this.compressor;
        }
    }

    private Compressor compressor = null;

    /**
     * 
     * 
     * @param in
     *            The {@link InputStream} to process
     * @param compressionType
     *            enum corresponding to the compression algorithm that should be
     *            used.
     * @throws IOException
     */
    public CompressInputStream(InputStream in, COMPRESSION_TYPE compressionType)
            throws IOException {
        super(in);
        compressor = compressionType.getCompressor();
    }

    /**
     * NOTE: Not Supported!
     */
    public int read() throws IOException {
        throw new IOException(getClass().getName()
                + ".read() :\n\tDo not support simple read().");
    }

    /**
     * Copies the compressed data into the specified destination array
     * 
     * @param b
     *            the destination array that the compressed data will be written
     *            to
     * @return the number of compressed bytes that were copied into the array.
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Copies the compressed data into the specified destination array starting
     * at index off for a total of len bytes
     * 
     * @param b
     *            the destination array that the compressed data will be written
     *            to
     * @param off
     *            the offset
     * @param len
     *            the length
     * @return the number of compressed bytes that were copied into the array.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int i, sample;
        byte[] inb;

        inb = new byte[len << 1]; // get 16bit PCM data
        len = in.read(inb);
        if (len == -1) {
            return -1;
        }
        ;

        i = 0;
        while (i < len) {
            sample = (inb[i++] & 0x00FF);
            sample |= (inb[i++] << 8);
            b[off++] = (byte) compressor.compress((short) sample);
        }
        return len >> 1;
    }
}

/**
 * Abstract representation of a data compressor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
abstract class Compressor {
    /**
     * Compress the specified value using the implemented algorithm.
     * 
     * @param sample
     *            the value to compress.
     * @return the compressed value.
     */
    protected abstract int compress(short sample);
}

/*
 * Mathematical Tools in Signal Processing with C++ and Java Simulations by
 * Willi-Hans Steeb International School for Scientific Computing
 */

/**
 * Implementation of the alaw compression algorithm. (see reference above)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
class ALawCompressor extends Compressor {

    static final int cClip = 32635;

    static final int[] ALawCompressTable = { 1, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4,
            4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7 };

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.mmscomputing.sound.Compressor#compress(short)
     */
    protected int compress(short sample) {
        int sign;
        int exponent;
        int mantissa;
        int compressedByte;

        sign = ((~sample) >> 8) & 0x80;
        if (sign == 0) {
            sample *= -1;
        }
        if (sample > cClip) {
            sample = cClip;
        }
        if (sample >= 256) {
            exponent = ALawCompressTable[(sample >> 8) & 0x007F];
            mantissa = (sample >> (exponent + 3)) & 0x0F;
            compressedByte = 0x007F & ((exponent << 4) | mantissa);
        } else {
            compressedByte = 0x007F & (sample >> 4);
        }
        compressedByte ^= (sign ^ 0x55);
        return compressedByte;
    }
}

/**
 * Implementation of the ulaw compression algorithm. (see reference above)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
class uLawCompressor extends Compressor {

    static final int cClip = 32635;

    static final int cBias = 0x84;

    int[] uLawCompressTable = { 0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.mmscomputing.sound.Compressor#compress(short)
     */
    protected int compress(short sample) {
        int sign;
        int exponent;
        int mantissa;
        int compressedByte;

        sign = (sample >> 8) & 0x80;
        if (sign != 0) {
            sample *= -1;
        }
        if (sample > cClip) {
            sample = cClip;
        }
        sample += cBias;

        exponent = uLawCompressTable[(sample >> 7) & 0x00FF];
        mantissa = (sample >> (exponent + 3)) & 0x0F;
        compressedByte = ~(sign | (exponent << 4) | mantissa);
        return compressedByte & 0x000000FF;
    }
}
