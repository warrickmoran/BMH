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
package com.raytheon.uf.common.bmh.tones;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.WordUtils;

import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.tones.data.Tone;

/**
 * Used to generate the various tones that may be precede the broadcast of
 * certain messages. The Tones Manager is currently capable of generating the
 * following types of tones: 1) Alert 2) SAME 3) Transfer {primary to secondary;
 * secondary to primary}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * Aug 04, 2014 3286       dgilling    Remove unnecessary try-catch block from
 *                                     generateSAMETone().
 * Aug 12, 2014 3286       dgilling    Add second generateSAMETone() that 
 *                                     supports encoding an arbitrary byte string.
 * Aug 17, 2014 3655       bkowal      Move tones to common.
 * Mar 04, 2015 4224       bkowal      Pad generated SAME tones.
 * Mar 23, 2015 4299       bkowal      Add the premable and the specified amount of
 *                                     padding to all generated tones. 
 *                                     Use {@link FskTonesEncoder}.
 * Apr 24, 2015 4394       bkowal      Renamed TRANSFER_TYPE to {@link TransferType}.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TonesManager {
    /*
     * The device that receives the SAME Tones that it supposed to react to them
     * may drop the end bytes unless we add extra padding that it can drop
     * instead.
     */
    public static final byte[] PREAMBLE_CODE = { (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB };

    private static final byte PADDING_BYTE = (byte) 0;

    private static final double ALERT_TONE_FREQUENCY = 1050.0; // Hz

    private static final double TRANSFER_TONE_PRIMARY_FREQUENCY = 1800.0;

    private static final double TRANSFER_TONE_SECONDARY_FREQUENCY = 2400.0;

    private static final double TRANSFER_TONE_AMPLITUDE = 24000.0;

    public static final double TRANSFER_TONE_DURATION = 5.0;

    public static enum TransferType {
        PRIMARY_TO_SECONDARY, SECONDARY_TO_PRIMARY;

        private final String text;

        private TransferType() {
            this.text = WordUtils.capitalizeFully(this.name().replace("_TO_",
                    " -> "));
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    /**
     * Utility. Prevent Instantiation.
     */
    protected TonesManager() {
    }

    /**
     * Generates an Alert tone using the default frequency (1050 Hz), the
     * specified amplitude, and the specified duration.
     * 
     * @param amplitude
     *            the specified amplitude
     * @param duration
     *            the specified duration
     * @return The generated Alert tone in ulaw format
     * @throws IOException
     *             when Alert tone generation fails
     */
    public static byte[] generateAlertTone(double amplitude, double duration)
            throws ToneGenerationException {
        Tone tone = new Tone();
        tone.setFrequency(ALERT_TONE_FREQUENCY);
        tone.setAmplitude(amplitude);
        tone.setDuration(duration);

        ToneGenerator toneGenerator = new ToneGenerator();

        short[] data = toneGenerator.encode(tone);
        byte[] dataBytes = convertShortToBytes(data);
        return convertToUlaw(dataBytes);
    }

    /**
     * Generates a SAME tone based on the specified SAME Message
     * 
     * @param SAMEMessage
     *            the specified Same Message
     * @return The generated SAME tone in ulaw format
     * @throws ToneGenerationException
     *             If the data could not be encoded into ulaw format.
     */
    public static byte[] generateSAMETone(final String SAMEMessage,
            final int paddingBytes) throws ToneGenerationException {
        return generateSAMETone(
                SAMEMessage.trim().getBytes(StandardCharsets.US_ASCII),
                paddingBytes);
    }

    /**
     * Generate a SAME tone based on the specified byte string.
     * 
     * @param SAMEData
     *            The byte string to convert into SAME tones.
     * @return The SAME tones encoded in ulaw format.
     * @throws ToneGenerationException
     *             If the data could not be encoded into ulaw format.
     */
    private static byte[] generateSAMETone(final byte[] SAMEData,
            final int paddingBytes) throws ToneGenerationException {
        int fullSAMEBufferLength = PREAMBLE_CODE.length + SAMEData.length
                + paddingBytes;
        ByteBuffer paddedSAMEBuffer = ByteBuffer.allocate(fullSAMEBufferLength);

        paddedSAMEBuffer.put(PREAMBLE_CODE);
        paddedSAMEBuffer.put(SAMEData);
        for (int i = 0; i < paddingBytes; i++) {
            paddedSAMEBuffer.put(PADDING_BYTE);
        }

        FskTonesEncoder tonesEncoder = new FskTonesEncoder();
        short[] data = tonesEncoder.execute(paddedSAMEBuffer.array());
        if (data.length <= 0) {
            return null;
        }
        byte[] dataBytes = convertShortToBytes(data);
        return convertToUlaw(dataBytes);
    }

    /**
     * Generates a Transfer tone based on the specified transfer type
     * 
     * @param transferType
     *            the specified transfer type
     * @return The generated transfer tone in ulaw format
     * @throws IOException
     *             when Transfer tone generation fails
     */
    public static byte[] generateTransferTone(TransferType transferType)
            throws ToneGenerationException {
        Tone primaryTone = new Tone();
        primaryTone.setFrequency(TRANSFER_TONE_PRIMARY_FREQUENCY);
        primaryTone.setAmplitude(TRANSFER_TONE_AMPLITUDE);
        primaryTone.setDuration(TRANSFER_TONE_DURATION);

        Tone secondaryTone = new Tone();
        secondaryTone.setFrequency(TRANSFER_TONE_SECONDARY_FREQUENCY);
        secondaryTone.setAmplitude(TRANSFER_TONE_AMPLITUDE);
        secondaryTone.setDuration(TRANSFER_TONE_DURATION);

        List<short[]> generatedData = new LinkedList<short[]>();
        ToneGenerator toneGenerator = new ToneGenerator();
        switch (transferType) {
        case PRIMARY_TO_SECONDARY:
            generatedData.add(toneGenerator.encode(primaryTone));
            generatedData.add(toneGenerator.encode(secondaryTone));
            break;
        case SECONDARY_TO_PRIMARY:
            generatedData.add(toneGenerator.encode(secondaryTone));
            generatedData.add(toneGenerator.encode(primaryTone));
            break;
        }

        short[] data = mergeShortData(generatedData);
        byte[] dataBytes = convertShortToBytes(data);
        return convertToUlaw(dataBytes);
    }

    /**
     * Combines a {@link List} of short[] into short[]
     * 
     * @param dataList
     *            the {@link List} of short[] to combine
     * @return a short[] containing the combined data
     */
    public static short[] mergeShortData(List<short[]> dataList) {
        /* Determine the overall length of the destination. */
        int totalLength = 0;
        for (short[] source : dataList) {
            totalLength += source.length;
        }

        /* Create and populate the destination. */
        short[] destination = new short[totalLength];
        int startIndex = 0;
        for (short[] source : dataList) {
            System.arraycopy(source, 0, destination, startIndex, source.length);
            startIndex += source.length;
        }

        return destination;
    }

    /**
     * Converts the provided short data into bytes in little endian order.
     * 
     * @param data
     *            the data to convert
     * @return the converted byte data
     */
    private static byte[] convertShortToBytes(short[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length * 2);
        /*
         * need little-endian to convert the bits to ulaw format.
         */
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(data);

        return buffer.array();
    }

    /**
     * Converts the byte data, currently in PCM format, to ulaw format.
     * 
     * @param pcmData
     *            the data to convert
     * @return the converted data
     * @throws UnsupportedAudioFormatException
     * @throws IOException
     *             when conversion fails
     */
    private static byte[] convertToUlaw(byte[] pcmData)
            throws ToneGenerationException {
        try {
            return AudioConvererterManager.getInstance().convertAudio(pcmData,
                    BMHAudioFormat.PCM, BMHAudioFormat.ULAW);
        } catch (UnsupportedAudioFormatException | AudioConversionException e) {
            throw new ToneGenerationException(
                    "Failed to convert the tone data to the required format!",
                    e);
        }
    }
}