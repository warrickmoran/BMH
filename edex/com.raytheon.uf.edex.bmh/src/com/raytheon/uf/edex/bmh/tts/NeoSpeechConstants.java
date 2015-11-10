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
package com.raytheon.uf.edex.bmh.tts;

/**
 * Maintains and provides access to the NeoSpeech volume defined in the BMH configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 6, 2015  4904       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class NeoSpeechConstants {

    private static final String NEOSPEECH_VOLUME_PRORPERTY = "bmh.neospeech.volume";

    private static boolean verify;

    private static String volume = System.getProperty(
            NEOSPEECH_VOLUME_PRORPERTY, null);

    public NeoSpeechConstants() {
    }

    /**
     * Retrieves and verifies the NeoSpeech volume from configuration. Provided
     * as a convenience method to be used during application startup. An
     * {@link IllegalStateException} will be thrown if a problem is found with
     * the volume specified in configuration.
     */
    public static void verify() {
        if (verify) {
            return;
        }

        /*
         * Verify that the volume has been provided.
         */
        if (volume == null) {
            throw new IllegalStateException(
                    "Failed to retrieve the NeoSpeech volume from configuration!");
        }
        /*
         * Verify that the volume is set to a valid value.
         */
        try {
            int volumeNumeric = Integer.parseInt(volume);
            if (volumeNumeric < 1 || volumeNumeric > 100) {
                throw new IllegalStateException("Invalid NeoSpeech volume: "
                        + volume + "!The NeoSpeech volume must be numeric.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid NeoSpeech volume: "
                            + volume
                            + "!The NeoSpeech volume must be in the range 1 to 100 inclusive.");
        }

        verify = true;
    }

    public static String getVolume() {
        if (volume == null) {
            if (verify) {
                throw new IllegalStateException(
                        "Failed to retrieve the NeoSpeech volume from configuration!");
            } else {
                verify();
            }
        }
        return volume;
    }
}