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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.AudioOverflowException;
import com.raytheon.uf.common.bmh.audio.AudioRegulator;
import com.raytheon.uf.common.bmh.audio.AudioRetrievalException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;

/**
 * Abstract representation of an audio retrieval job.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 2, 2014             bkowal      Initial creation
 * Oct 23, 2014 3748       bkowal      AudioRetrievalException is now in common
 * Mar 04, 2014 4224       bkowal      Optionally skip audio adjustments based on
 *                                     DISABLE_AUDIO_ATTENUATION.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Aug 17, 2015 4757       bkowal      Relocated regulation to BMH common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractAudioJob<T extends IAudioFileBuffer> implements
        PrioritizableCallable<T> {

    private static final boolean DISABLE_AUDIO_ATTENUATION = Boolean
            .getBoolean("disableAudioAttenuation");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /* equivalent to 0.125 seconds */
    private static final int AUDIO_SAMPLE_SIZE = 1000;

    private final int priority;

    protected final double dbTarget;

    protected final double sameDbTarget;

    protected final double alertDbTarget;

    protected final DacPlaylistMessage message;

    /**
     * Constructor
     * 
     * @param priority
     *            the job priority
     * @param dbTarget
     *            the decibel target associated with the transmitter
     * @param message
     *            the message to retrieve audio for
     * @param logger
     *            a {@code Logger} associated with the implementing class
     */
    public AbstractAudioJob(final int priority, final double dbTarget,
            final double sameDbTarget, final double alertDbTarget,
            final DacPlaylistMessage message) {
        this.priority = priority;
        this.dbTarget = dbTarget;
        this.message = message;
        this.sameDbTarget = sameDbTarget;
        this.alertDbTarget = alertDbTarget;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public abstract T call() throws Exception;

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.playlist.PrioritizableCallable#
     * getPriority()
     */
    @Override
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * Adjusts the specified audio data so that it will be within the decibel
     * range of the transmitter.
     * 
     * @param originalAudio
     *            the specified audio data
     * @param message
     *            identifies the audio data; used for logging purposes
     * @param part
     *            identifies the portion of audio that is being adjusted; used
     *            for logging purposes
     * @return the adjusted audio data
     * @throws AudioRetrievalException
     */
    protected byte[] adjustAudio(final byte[] originalAudio, final String part,
            final double dbTarget) throws AudioRetrievalException {
        if (DISABLE_AUDIO_ATTENUATION) {
            return originalAudio;
        }

        byte[] regulatedAudio = new byte[0];
        try {
            AudioRegulator audioRegulator = new AudioRegulator();
            regulatedAudio = audioRegulator.regulateAudioVolume(originalAudio,
                    dbTarget, AUDIO_SAMPLE_SIZE);
            logger.info("Successfully finished audio attenuation/amplification in "
                    + audioRegulator.getDuration()
                    + " ms for message: "
                    + message.getBroadcastId() + " (" + part + ").");
        } catch (UnsupportedAudioFormatException | AudioConversionException
                | AudioOverflowException e) {
            final String msg = "Failed to adjust the audio signal to the target "
                    + this.dbTarget
                    + " dB for message: "
                    + message.getBroadcastId();
            throw new AudioRetrievalException(msg, e);
        }

        return regulatedAudio;
    }
}