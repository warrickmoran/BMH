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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

/**
 * {@code Enum} that describes the current output status of a channel on the
 * DAC. There are 3 options:
 * <ol>
 * <li>{@code SILENCE}: currently playing no audio
 * <li>{@code IP_AUDIO}: currently playing audio being transmitted to it.
 * <li>{@code MAINTENANCE_MESSAGE}: hasn't received any audio data in a long
 * enough time to trigger the automated off-the-air message.
 * </ol>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014   #3286    dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public enum DacVoiceStatus {

    SILENCE(0), IP_AUDIO(1), MAINTENANCE_MESSAGE(2);

    private final int dacStatusCode;

    private DacVoiceStatus(final int statusCode) {
        this.dacStatusCode = statusCode;
    }

    public static DacVoiceStatus fromStatusCode(final int statusCode) {
        for (DacVoiceStatus status : DacVoiceStatus.values()) {
            if (status.dacStatusCode == statusCode) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid status code specified: "
                + statusCode);
    }
}
