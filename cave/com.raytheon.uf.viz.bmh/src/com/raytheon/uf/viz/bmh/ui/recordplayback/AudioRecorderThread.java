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
package com.raytheon.uf.viz.bmh.ui.recordplayback;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.audio.AudioRegulationConfiguration;
import com.raytheon.uf.common.bmh.audio.CollectibleAudioRegulator;
import com.raytheon.uf.common.bmh.broadcast.AudioRegulationSettingsCommand;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.comms.CommsCommunicationException;

/**
 * Manages the recording and storage of audio. Provides access to all recorded
 * audio at the conclusion of the recording. Also allows for forking the
 * recorded audio to another destination via a listener.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2014  3657       bkowal      Initial creation
 * Oct 29, 2014 3774       bsteffen    Log Packets
 * Nov 24, 2014 3862       bkowal      Added a plot listener.
 * Aug 17, 2015 4757       bkowal      Audio is now altered in 5 packet
 *                                     segments prior to saving.
 * Aug 24, 2015 4770       bkowal      Utilize the {@link AudioRegulationConfiguration}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRecorderThread extends Thread {

    // TODO: need a common location for this format throughout BMH.
    private static final AudioFormat ULAW_AUDIO_FMT = new AudioFormat(
            Encoding.ULAW, 8000, 8, 1, 1, 8000, true);

    /*
     * 4 -5 audio packets are sent to Comms Manager at a time.
     */
    private final int REGULATORY_SIZE = 5;

    private final int sampleCount;

    private final IAudioRecorderListener plotListener;

    private AudioRegulationConfiguration regulationConfiguration;

    private List<byte[]> samples;

    private TargetDataLine line;

    private IAudioRecorderListener listener;

    public AudioRecorderThread(final int sampleCount,
            final IAudioRecorderListener plotListener) throws Exception {
        super(AudioRecorderThread.class.getName());

        /*
         * Retrieve the {@link AudioRegulationConfiguration}.
         */
        this.retrieveRegulationConfiguration();

        this.samples = new LinkedList<>();
        this.sampleCount = sampleCount;
        this.plotListener = plotListener;

        try {
            this.line = AudioSystem.getTargetDataLine(null);
            this.line.open(ULAW_AUDIO_FMT);
        } catch (LineUnavailableException e) {
            this.closeLine();
            throw new AudioException(
                    "Failed to prepare an audio recording session!", e);
        }
    }

    private void retrieveRegulationConfiguration() throws Exception {
        String commsLoc = BMHServers.getBroadcastServer();
        if (commsLoc == null) {
            throw new CommsCommunicationException(
                    "No address has been specified for comms manager "
                            + BMHServers.getBroadcastServerKey() + ".");
        }
        URI commsURI = null;
        try {
            commsURI = new URI(commsLoc);
        } catch (URISyntaxException e) {
            throw new CommsCommunicationException(
                    "Invalid Comms Manager Location.", e);
        }
        try (Socket socket = new Socket(commsURI.getHost(), commsURI.getPort())) {
            socket.setTcpNoDelay(true);
            SerializationUtil.transformToThriftUsingStream(
                    new AudioRegulationSettingsCommand(),
                    socket.getOutputStream());
            Object message = SerializationUtil.transformFromThrift(
                    Object.class, socket.getInputStream());
            if (message == null) {
                throw new NullPointerException(
                        "Unexpected null response from comms manager.");
            } else if (message instanceof AudioRegulationConfiguration) {
                this.regulationConfiguration = (AudioRegulationConfiguration) message;
            } else {
                throw new IllegalStateException(
                        "Unexpected response from comms manager of type: "
                                + message.getClass().getSimpleName());
            }
        }
    }

    public void setRecordingListener(final IAudioRecorderListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        this.line.start();
        final int bytesToRead = this.sampleCount
                * ULAW_AUDIO_FMT.getSampleSizeInBits();
        AudioPacketLogger logger = new AudioPacketLogger("Audio Recorder",
                getClass(), 10);
        while (true) {
            byte[] audioData = new byte[bytesToRead];
            int bytesRead = this.line.read(audioData, 0, bytesToRead);
            logger.packetProcessed();
            if (bytesRead == 0) {
                break;
            }
            if (bytesRead != audioData.length) {
                audioData = Arrays.copyOf(audioData, bytesRead);
            }
            this.notifyListener(audioData);
            this.samples.add(audioData);
        }
        logger.close();
    }

    private void notifyListener(final byte[] audioData) {
        /*
         * May end up threading it so that the recorder does not end up waiting
         * for the listener to finish its work?
         */
        // the plot listener will always exist.
        this.plotListener.audioReady(audioData);
        if (this.listener != null) {
            this.listener.audioReady(audioData);
        }
    }

    private void closeLine() {
        if (this.line == null) {
            return;
        }

        this.line.close();
        this.line = null;
    }

    public void halt() {
        this.closeLine();
    }

    public ByteBuffer getAudioSamples() throws Exception {
        int totalSampleBytes = 0;
        for (byte[] sample : this.samples) {
            totalSampleBytes += sample.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(totalSampleBytes);

        /*
         * Adjust the audio.
         */
        List<byte[]> regulatorySequence = new ArrayList<>(REGULATORY_SIZE);
        for (int i = 0; i < this.samples.size(); i++) {
            byte[] sample = this.samples.get(i);
            regulatorySequence.add(sample);

            if (regulatorySequence.size() == REGULATORY_SIZE
                    && (this.samples.size() - i) > REGULATORY_SIZE) {
                this.regulateAudioSamples(regulatorySequence, buffer);
                regulatorySequence.clear();
            }
        }
        if (regulatorySequence.isEmpty() == false) {
            this.regulateAudioSamples(regulatorySequence, buffer);
        }

        return buffer;
    }

    private void regulateAudioSamples(List<byte[]> regulatorySequence,
            ByteBuffer destination) throws Exception {
        final CollectibleAudioRegulator regulator = new CollectibleAudioRegulator(
                this.regulationConfiguration.getDbSilenceLimit(),
                regulatorySequence);
        regulatorySequence = regulator.regulateAudioCollection(0);
        for (byte[] regulatedSample : regulatorySequence) {
            destination.put(regulatedSample);
        }
    }
}