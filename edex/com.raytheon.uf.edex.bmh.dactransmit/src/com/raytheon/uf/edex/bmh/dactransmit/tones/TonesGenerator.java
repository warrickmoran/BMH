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
package com.raytheon.uf.edex.bmh.dactransmit.tones;

import java.nio.ByteBuffer;

import com.raytheon.uf.edex.bmh.generate.tones.ToneGenerationException;
import com.raytheon.uf.edex.bmh.generate.tones.TonesManager;

/**
 * Generates SAME (and optionally, alert) tone patterns to be played along with
 * messages sent to the DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 12, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class TonesGenerator {

    private static StaticTones defaultTonesInstance;

    private static synchronized StaticTones getStaticTones()
            throws ToneGenerationException {
        if (defaultTonesInstance == null) {
            defaultTonesInstance = new StaticTones();
        }

        return defaultTonesInstance;
    }

    /**
     * Generates the tone patterns (SAME and, optionally, alert) that are needed
     * given the specified SAME tone header.
     * 
     * @param sameHeader
     *            The SAME tone header to encode into tones.
     * @param includeAlertTone
     *            Whether or not the alert tone needs to be included.
     * @return The tone patterns, including any necessary pauses.
     * @throws ToneGenerationException
     *             If an error occurred encoding the SAME tone header string or
     *             generating any of the necessary static tones.
     */
    public static ByteBuffer getSAMEAlertTones(String sameHeader,
            boolean includeAlertTone) throws ToneGenerationException {
        StaticTones staticTones = getStaticTones();

        byte[] preamble = staticTones.getPreambleTones();
        byte[] betweenPause = staticTones.getBetweenPreambleOrClosingPause();
        byte[] beforeMessagePause = staticTones.getBeforeMessagePause();
        byte[] preambleHeader = TonesManager.generateSAMETone(sameHeader);

        int bufferSize = (3 * (preamble.length + preambleHeader.length))
                + (2 * betweenPause.length) + beforeMessagePause.length;
        if (includeAlertTone) {
            bufferSize += (staticTones.getAlertTone().length + staticTones
                    .getBeforeAlertTonePause().length);
        }

        ByteBuffer retVal = ByteBuffer.allocate(bufferSize);
        retVal.put(preamble).put(preambleHeader).put(betweenPause);
        retVal.put(preamble).put(preambleHeader).put(betweenPause);
        retVal.put(preamble).put(preambleHeader);
        if (includeAlertTone) {
            retVal.put(defaultTonesInstance.getBeforeAlertTonePause());
            retVal.put(defaultTonesInstance.getAlertTone());
        }
        retVal.put(beforeMessagePause);

        return retVal;
    }

    /**
     * Returns a {@code ByteBuffer} containing the data to play back the SAME
     * end of message tone patterns including any necessary pauses between the
     * tone patterns.
     * 
     * @return The end of message SAME tone patterns.
     * @throws ToneGenerationException
     *             If there was an error generating the static end of message
     *             tones.
     */
    public static ByteBuffer getEndOfMessageTones()
            throws ToneGenerationException {
        StaticTones staticTones = getStaticTones();
        return staticTones.getEndOfMessageTones();
    }

    private TonesGenerator() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}