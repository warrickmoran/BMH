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
package com.raytheon.uf.common.bmh.audio;

/**
 * Indicates that the converter for a specific {@link BMHAudioFormat} is not
 * supported on this machine.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2014  3880       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ConversionNotSupportedException extends Exception {
    private static final long serialVersionUID = 8797501893630995414L;

    /**
     * Constructor
     * 
     * @param format
     *            the {@link BMHAudioFormat} that is not supported
     * @param reason
     *            a description explaining why the format is not supported.
     */
    public ConversionNotSupportedException(BMHAudioFormat format, String reason) {
        super(buildMessage(format, reason));
    }

    public ConversionNotSupportedException(BMHAudioFormat format,
            String reason, Throwable cause) {
        super(buildMessage(format, reason), cause);
    }

    private static String buildMessage(BMHAudioFormat format, String reason) {
        StringBuilder sb = new StringBuilder("Conversion for audio format: ");
        sb.append(format.toString());
        sb.append(" is not supported on this machine. REASON = ");
        sb.append(reason);
        sb.append("!");

        return sb.toString();
    }
}