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
package com.raytheon.uf.edex.bmh.audio;

/**
 * Indicates that an invalid audio sample has been generated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 23, 2014 3424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioOverflowException extends Exception {

    private static final long serialVersionUID = 1693306060211544977L;

    public AudioOverflowException(double requestedVolumeIncrease,
            int invalidAmplitude) {
        super(generateExceptionText(requestedVolumeIncrease, invalidAmplitude));
    }

    private static String generateExceptionText(double requestedVolumeIncrease,
            int invalidAmplitude) {
        StringBuilder stringBuilder = new StringBuilder(
                "The desired volume increase: ");
        stringBuilder.append(requestedVolumeIncrease);
        stringBuilder.append(" generates an audio sample with amplitude: ");
        stringBuilder.append(invalidAmplitude);
        stringBuilder.append(" that exceeds the maximum allowable amplitude.");

        return stringBuilder.toString();
    }
}