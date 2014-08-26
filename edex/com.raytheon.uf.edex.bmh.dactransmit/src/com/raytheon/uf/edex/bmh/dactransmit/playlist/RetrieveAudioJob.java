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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.math.Range;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.edex.bmh.audio.AudioOverflowException;
import com.raytheon.uf.edex.bmh.audio.AudioRegulator;
import com.raytheon.uf.edex.bmh.dactransmit.tones.TonesGenerator;
import com.raytheon.uf.edex.bmh.generate.tones.ToneGenerationException;

/**
 * Allows for the asynchronous retrieval of audio data. Will adjust the audio
 * data according to a specified decibel range after successfully retrieving it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2014 3532       bkowal      Initial creation
 * Aug 26, 2014 3558       rjpeter     Disable audio attenuation.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RetrieveAudioJob implements PrioritizableCallable<AudioFileBuffer> {

    private final int priority;

    private final Range dbRange;

    private final DacPlaylistMessage message;

    /**
     * Constructor
     * 
     * @param priority
     *            the priority; the higher the number, the higher the associated
     *            priority will be.
     * @param dbRange
     *            the decibel range that the retrieved audio must be within
     * @param message
     *            identifying information
     */
    public RetrieveAudioJob(final int priority, final Range dbRange,
            final DacPlaylistMessage message) {
        this.priority = priority;
        this.dbRange = dbRange;
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public AudioFileBuffer call() throws Exception {
        Path filePath = Paths.get(this.message.getSoundFile());

        /*
         * We really don't want to leave essentially null messages in the cache
         * for playback. However for the highly-controlled environment of the
         * initial demo version, file read errors and tone generation errors
         * shouldn't happen.
         * 
         * FIXME: if caching a message's audio data fails, perform retry of some
         * sort.
         */
        byte[] rawData = new byte[0];
        try {
            rawData = Files.readAllBytes(filePath);
        } catch (IOException e) {
            String msg = "Failed to buffer audio file for message: " + message
                    + ", file: " + filePath;
            throw new AudioRetrievalException(msg, e);
        }

        /* Adjust the raw audio data as needed. */
        byte[] regulatedRawData = rawData;// this.adjustAudio(rawData, message);

        ByteBuffer tones = null;
        ByteBuffer endOfMessage = null;
        if ((message.getSAMEtone() != null)
                && (!message.getSAMEtone().isEmpty())) {
            try {
                endOfMessage = TonesGenerator.getEndOfMessageTones();
                tones = TonesGenerator.getSAMEAlertTones(message.getSAMEtone(),
                        message.isAlertTone());
            } catch (ToneGenerationException e) {
                String msg = "Unable to generate tones for message: " + message;
                throw new AudioRetrievalException(msg, e);
            }
        }

        /* regulate tones (if they exist). */
        if (tones != null) {
            // byte[] regulatedTones = adjustAudio(tones.array(), message);
            // tones = ByteBuffer.wrap(regulatedTones);
        }
        if (endOfMessage != null) {
            // byte[] regulatedEndOfMessage = adjustAudio(endOfMessage.array(),
            // message);
            // endOfMessage = ByteBuffer.wrap(regulatedEndOfMessage);
        }

        AudioFileBuffer buffer = new AudioFileBuffer(regulatedRawData, tones,
                endOfMessage);

        return buffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.playlist.PrioritizableRunnable#
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
     *            identifies the audio data; used for auditing purposes
     * @return the adjusted audio data
     * @throws AudioRetrievalException
     */
    private byte[] adjustAudio(final byte[] originalAudio,
            final DacPlaylistMessage message) throws AudioRetrievalException {
        byte[] regulatedAudio = new byte[0];
        try {
            AudioRegulator audioRegulator = new AudioRegulator(originalAudio);
            regulatedAudio = audioRegulator.regulateAudioVolumeRange(
                    dbRange.getMinimumDouble(), dbRange.getMaximumDouble());
        } catch (UnsupportedAudioFormatException | AudioConversionException
                | AudioOverflowException e) {
            final String msg = "Failed to adjust the audio signal to be within range "
                    + dbRange.toString() + " for message: " + message;
            throw new AudioRetrievalException(msg, e);
        }

        return regulatedAudio;
    }
}