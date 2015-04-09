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

/**
 * Modernized version of:
 * http://thorntonzone.com/manuals/Compression/Fax,%20IBM%20MMR/MMSC/mmsc
 * /uk/co/mmscomputing/sound/ (less C++-like).
 * 
 * Note: this version only supports ulaw and pcm; excluding alaw because it is
 * not used within BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 9, 2015  4365       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PCMToUlawAlgorithm {

    private static final int cClip = 32635;

    private static final int cBias = 0x84;

    private static final int[] uLawCompressTable = { 0, 0, 1, 1, 2, 2, 2, 2, 3,
            3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7 };

    public static byte[] convert(final byte[] src) {
        if (src == null || src.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument src has not been provided.");
        }

        byte[] convertedAudio = new byte[src.length >> 1];
        convert(src, convertedAudio);

        return convertedAudio;
    }

    public static void convert(final byte[] src, byte[] destination) {
        convert(src, 0, src.length, destination, 0);
    }

    public static void convert(final byte[] src, int srcOffset, int srcLength,
            byte[] destination) {
        convert(src, srcOffset, srcLength, destination, 0);
    }

    public static void convert(final byte[] src, byte[] destination,
            int dstOffset) {
        convert(src, 0, src.length, destination, dstOffset);
    }

    public static void convert(final byte[] src, int srcOffset, int srcLength,
            byte[] destination, int dstOffset) {
        if (src == null || src.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument src has not been provided.");
        }
        if (destination == null || destination.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument destination has not been provided.");
        }
        if (dstOffset < 0) {
            throw new IllegalArgumentException(
                    "Required argument dstOffset must be > 0.");
        }

        /*
         * ensure that there is enough room in the destination array.
         */
        int allowedLength = destination.length - dstOffset;
        int requiredLength = (srcLength - srcOffset) >> 1;
        if (allowedLength < requiredLength) {
            StringBuilder sb = new StringBuilder(
                    "The provided destination array beginning at offset ");
            sb.append(dstOffset)
                    .append(" is not large enough to hold the result of the conversion.");

            throw new IllegalStateException(sb.toString());
        }

        /*
         * Complete the conversion.
         */
        int i = srcOffset;
        while (i < (srcOffset + srcLength)) {
            int sample = (src[i++] & 0x00FF);
            sample |= (src[i++] << 8);
            destination[dstOffset++] = (byte) compress((short) sample);
        }
    }

    private static int compress(short sample) {
        int sign = (sample >> 8) & 0x80;
        if (sign != 0) {
            sample *= -1;
        }
        if (sample > cClip) {
            sample = cClip;
        }
        sample += cBias;

        int exponent = uLawCompressTable[(sample >> 7) & 0x00FF];
        int mantissa = (sample >> (exponent + 3)) & 0x0F;
        int compressedByte = ~(sign | (exponent << 4) | mantissa);

        return compressedByte & 0x000000FF;
    }
}