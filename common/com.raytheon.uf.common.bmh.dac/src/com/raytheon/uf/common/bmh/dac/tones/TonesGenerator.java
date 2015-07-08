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
package com.raytheon.uf.common.bmh.dac.tones;

import java.nio.ByteBuffer;

import com.raytheon.uf.common.bmh.tones.GeneratedTonesBuffer;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;

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
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * Nov 03, 2014  #3781     dgilling     Allow alert tones to be generated
 *                                      independently from SAME tones.
 * Mar 23, 2015  #4299     bkowal       Generate the SAME tone with the preamble.
 *                                      Add padding to the end of the preamble + SAME
 *                                      tones.
 * May 05, 2015  #4464     bkowal       SAME Tone Padding is now configurable via a system property.
 * Jul 07, 2015  #4464     bkowal       Default SAME padding is now 0.
 * Jul 08, 2015  #4636     bkowal       Updated to use {@link GeneratedTonesBuffer}.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class TonesGenerator {

    /**
     * Overridable SAME Tones padding constant. This is the number of 0 bytes
     * that will be placed at the end of the SAME Tone bytes array that is
     * generated. This property may no longer be overridable after we
     * transitional to operational builds.
     */
    private static final int SAME_PADDING = Integer.getInteger(
            "samePaddingOverride", 0);

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
     * @param includeSilence
     *            Whether or not to include 4 seconds of silence after the same
     *            or alert tones.
     * @return The tone patterns, including any necessary pauses.
     * @throws ToneGenerationException
     *             If an error occurred encoding the SAME tone header string or
     *             generating any of the necessary static tones.
     */
    public static GeneratedTonesBuffer getSAMEAlertTones(String sameHeader,
            boolean includeAlertTone, boolean includeSilence)
            throws ToneGenerationException {
        StaticTones staticTones = getStaticTones();

        byte[] betweenPause = staticTones.getBetweenPreambleOrClosingPause();
        byte[] beforeMessagePause = staticTones.getBeforeMessagePause();
        byte[] preambleHeader = TonesManager.generateSAMETone(sameHeader,
                SAME_PADDING);

        int bufferSize = (3 * (preambleHeader.length))
                + (2 * betweenPause.length) + beforeMessagePause.length;

        ByteBuffer retVal = ByteBuffer.allocate(bufferSize);
        retVal.put(preambleHeader).put(betweenPause);
        retVal.put(preambleHeader).put(betweenPause);
        retVal.put(preambleHeader);

        GeneratedTonesBuffer buffer = new GeneratedTonesBuffer();
        buffer.setSameTones(retVal.array());

        if (includeAlertTone) {
            buffer.setBeforeAlertTonePause(defaultTonesInstance
                    .getBeforeAlertTonePause());
            buffer.setAlertTones(defaultTonesInstance.getAlertTone());
        }
        if (includeSilence) {
            buffer.setBeforeMessagePause(beforeMessagePause);
        }
        return buffer;
    }

    /**
     * Generates the alert tone without any SAME tones.
     * 
     * @return The alert tone and the necessary after tone pause.
     * @throws ToneGenerationException
     *             If there was an error generating the static alert tone.
     */
    public static GeneratedTonesBuffer getOnlyAlertTones()
            throws ToneGenerationException {
        StaticTones staticTones = getStaticTones();

        GeneratedTonesBuffer buffer = new GeneratedTonesBuffer();
        buffer.setBeforeMessagePause(staticTones.getBeforeMessagePause());
        buffer.setAlertTones(staticTones.getAlertTone());

        return buffer;
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
