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
package com.raytheon.bmh.dactransmit.util;

/**
 * Utility module for converting primitive types to C/C++ types unsupported by
 * Java.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014   #3268    dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PrimitiveTypeConversion {

    private PrimitiveTypeConversion() {
        throw new AssertionError(
                "You can not instantiate instances of this class.");
    }

    /**
     * Convert the specified value to the byte representation of the unsigned
     * 32-bit integer of the same value.
     * 
     * @param value
     *            Value to convert.
     * @return The byte representation of {@code value} as an unsigned 32-bit
     *         integer.
     */
    public static byte[] longToUInt32Bytes(final long value) {
        // TODO? Should I range-check to ensure we don't overflow?
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (value & 0xFF);
        bytes[2] = (byte) ((value >> 8) & 0xFF);
        bytes[1] = (byte) ((value >> 16) & 0xFF);
        bytes[0] = (byte) ((value >> 24) & 0xFF);
        return bytes;
    }

    /**
     * Convert the specified value to the byte representation of the unsigned
     * 16-bit integer of the same value.
     * 
     * @param value
     *            Value to convert.
     * @return The byte representation of {@code value} as an unsigned 16-bit
     *         integer.
     */
    public static byte[] intToUInt16Bytes(final int value) {
        // TODO? Should I range-check to ensure we don't overflow?
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (value & 0xFF);
        bytes[0] = (byte) ((value >> 8) & 0xFF);
        return bytes;
    }
}
