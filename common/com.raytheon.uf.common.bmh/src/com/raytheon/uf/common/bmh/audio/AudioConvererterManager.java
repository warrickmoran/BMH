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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;

import com.raytheon.uf.common.bmh.audio.impl.PcmAudioConverter;
import com.raytheon.uf.common.bmh.audio.impl.UlawAudioConverter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Keeps track of the registered audio converters and the supported audio types.
 * Used to invoke the audio converters to complete audio conversion.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014 3383       bkowal      Initial creation
 * Dec 3, 2014  3880       bkowal      Remove test code. Verify arguments
 *                                     to the convertAudio method.
 * Dec 4, 2014  3880       bkowal      getSupportedFormats now returns a {@link Set}.
 * Dec 16, 2014 3880       bkowal      Do not register the {@link IAudioConverter}
 *                                     if it is not compatible.
 * Feb 09, 2015 4091       bkowal      Made {@link #initialize()} protected.
 * Jan 14, 2015 5177       bkowal      Support chaining multiple {@link IAudioConverter}s
 *                                     together.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioConvererterManager {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AudioConvererterManager.class);

    private static final AudioConvererterManager instance = new AudioConvererterManager();

    /**
     * Keep track of audio conversions that are not supported because the
     * registration of a future converter may provide the needed intermediary
     * conversion(s) that makes the overall conversion possible.
     */
    private final Set<ChainedConversionKey> unsupportedConversionPaths = new HashSet<>();

    private final Map<BMHAudioFormat, IAudioConverter> registeredAudioConverters = new HashMap<>();

    private final Map<ChainedConversionKey, IAudioConverter> chainedAudioConverters = new HashMap<>();

    /**
     * Constructor
     */
    protected AudioConvererterManager() {
        this.initialize();
        statusHandler.info("Initialization Complete!");
    }

    /**
     * Returns the general instance of the {@link AudioConvererterManager}.
     * 
     * @return the general instance of the {@link AudioConvererterManager}.
     */
    public static AudioConvererterManager getInstance() {
        return instance;
    }

    /**
     * Initializes the Audio Converter Manager
     */
    protected void initialize() {
        statusHandler.info("Initializing the Audio Converter Manager ...");
        /* Register the audio conversions that are natively supported by Java. */

        // Presently, there is no perceived benefit from allowing and using a
        // spring-based registration process. If other plugins and/or projects
        // that would be dependent on BMH want to contribute audio converters,
        // then a spring-based registration process would be beneficial.
        this.registerConverter(new UlawAudioConverter());
        this.registerConverter(new PcmAudioConverter());
    }

    /**
     * Registers an audio converter so that it can now be used to convert audio
     * 
     * @param converter
     *            the audio converter to register
     */
    public void registerConverter(IAudioConverter converter) {
        synchronized (this.registeredAudioConverters) {
            /**
             * verify that the audio conversion is supported on this machine.
             */
            try {
                converter.verifyCompatibility();
            } catch (ConversionNotSupportedException e) {
                statusHandler.error(
                        "Failed to register an audio converter for the "
                                + converter.getOutputFormat().toString()
                                + " format.", e);
                return;
            }

            if (this.registeredAudioConverters.containsKey(converter
                    .getOutputFormat())) {
                // warn and replace the previously registered converter.
                statusHandler
                        .warn("A converter has already been registered for the: "
                                + converter.getOutputFormat().toString()
                                + " audio type! The converter that was previously registered will be replaced.");
            }
            this.registeredAudioConverters.put(converter.getOutputFormat(),
                    converter);
            statusHandler
                    .info("Successfully registered an audio converter for the "
                            + converter.getOutputFormat().toString()
                            + " format.");

            this.buildVerifyConversionChains(converter);
        }
    }

    private void buildVerifyConversionChains(final IAudioConverter converter) {
        /*
         * Determine if any of the known unsupported conversions have been
         * resolved by the addition of the additional converter.
         */
        if (this.unsupportedConversionPaths.isEmpty() == false) {
            Iterator<ChainedConversionKey> audioFormatIterator = this.unsupportedConversionPaths
                    .iterator();
            while (audioFormatIterator.hasNext()) {
                ChainedConversionKey key = audioFormatIterator.next();
                if (this.permutateConversionChain(key.getOrigin(),
                        key.getDestination())) {
                    audioFormatIterator.remove();
                }
            }
        }

        for (BMHAudioFormat supportedFormat : this.getSupportedFormats()) {
            if (supportedFormat == converter.getOutputFormat()) {
                continue;
            }

            if (converter.getSupportedSourceFormats().contains(supportedFormat)) {
                StringBuilder sb = new StringBuilder(
                        "Found audio conversion path: ");
                sb.append(supportedFormat.name()).append(" -> ")
                        .append(converter.getOutputFormat().name()).append(".");
                statusHandler.info(sb.toString());
            } else {
                StringBuilder sb = new StringBuilder(
                        "Unable to apply a direct conversion from: ");
                sb.append(supportedFormat.name()).append(" to ")
                        .append(converter.getOutputFormat().name())
                        .append(" ...");
                statusHandler.info(sb.toString());
                if (this.permutateConversionChain(supportedFormat,
                        converter.getOutputFormat()) == false) {
                    unsupportedConversionPaths.add(new ChainedConversionKey(
                            supportedFormat, converter.getOutputFormat()));
                }
            }
        }

        /*
         * Check the other side of the conversion.
         */
        for (IAudioConverter otherConverter : this.registeredAudioConverters
                .values()) {
            if (otherConverter.getOutputFormat() == converter.getOutputFormat()) {
                continue;
            }

            if (otherConverter.getSupportedSourceFormats().contains(
                    converter.getOutputFormat())) {
                StringBuilder sb = new StringBuilder(
                        "Found audio conversion path: ");
                sb.append(converter.getOutputFormat().name()).append(" -> ")
                        .append(otherConverter.getOutputFormat().name())
                        .append(".");
                statusHandler.info(sb.toString());
            } else {
                StringBuilder sb = new StringBuilder(
                        "Unable to apply a direct conversion from: ");
                sb.append(converter.getOutputFormat().name()).append(" to ")
                        .append(otherConverter.getOutputFormat().name())
                        .append(" ...");
                statusHandler.info(sb.toString());
                if (this.permutateConversionChain(converter.getOutputFormat(),
                        otherConverter.getOutputFormat()) == false) {
                    unsupportedConversionPaths.add(new ChainedConversionKey(
                            converter.getOutputFormat(), otherConverter
                                    .getOutputFormat()));
                }
            }
        }
    }

    private boolean permutateConversionChain(final BMHAudioFormat srcFormat,
            final BMHAudioFormat destFormat) {
        /*
         * Attempt to find the shortest path from the source format to the
         * destination format. There is no point in ever cycling during the
         * conversion in which case the audio ends up in a format that it had
         * previously been. So, the initial set of formats that will be
         * considered will consist of all recognized formats excluding the
         * source format and the destination format.
         */
        Set<BMHAudioFormat> availableFormats = new HashSet<>(
                this.getSupportedFormats());
        availableFormats.remove(srcFormat);
        availableFormats.remove(destFormat);

        // Base case: no intermediary formats remain.
        if (availableFormats.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    "No audio conversion path exists for: ");
            sb.append(srcFormat.name()).append(" -> ")
                    .append(destFormat.name()).append(".");
            return false;
        }
        // Nth case(s)
        /*
         * multiple potential paths exist, so we will explore a subset of the
         * permutations that do not terminate early due to incompatible formats.
         */

        /*
         * initially, we will just examine each element individually. If that
         * does not work, the next step will be to start building permutations.
         */
        List<AudioFormatSequence> nextSequencesToCheck = new ArrayList<>();
        Iterator<BMHAudioFormat> intermediaryIterator = availableFormats
                .iterator();
        while (intermediaryIterator.hasNext()) {
            BMHAudioFormat intermediaryFormat = intermediaryIterator.next();
            final boolean sourceSupported = this.registeredAudioConverters
                    .get(intermediaryFormat).getSupportedSourceFormats()
                    .contains(srcFormat);
            if (sourceSupported == false) {
                /*
                 * No point in continuing this check because there is no way to
                 * convert the source format to the intermediary format.
                 */
                continue;
            }
            /*
             * Keep track of the intermediary as a potential beginning for the
             * sequence.
             */
            nextSequencesToCheck
                    .add(new AudioFormatSequence(intermediaryFormat));
            final boolean conversionSupported = this.registeredAudioConverters
                    .get(destFormat).getSupportedSourceFormats()
                    .contains(intermediaryFormat);
            if (conversionSupported) {
                StringBuilder sb = new StringBuilder(
                        "Found audio conversion path: ");
                sb.append(srcFormat.name()).append(" -> ")
                        .append(intermediaryFormat.name()).append(" -> ")
                        .append(destFormat.name()).append(".");
                statusHandler.info(sb.toString());

                final List<BMHAudioFormat> audioPath = new ArrayList<>(1);
                audioPath.add(intermediaryFormat);

                this.constructChainedConverter(srcFormat, destFormat, audioPath);
                return true;
            }
        }

        if (nextSequencesToCheck.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    "No audio conversion path exists for: ");
            sb.append(srcFormat.name()).append(" -> ")
                    .append(destFormat.name()).append(".");
            statusHandler.info(sb.toString());
            return false;
        }

        AudioFormatSequence matchingSequence = null;
        while (nextSequencesToCheck.isEmpty() == false
                && matchingSequence == null) {
            Queue<AudioFormatSequence> sequenceQueue = new ArrayDeque<>(
                    nextSequencesToCheck);
            nextSequencesToCheck.clear();

            while (sequenceQueue.isEmpty() == false) {
                AudioFormatSequence sequenceToCheck = sequenceQueue.poll();
                List<AudioFormatSequence> continuingSequenceList = sequenceToCheck
                        .continueSequence(this.registeredAudioConverters
                                .values());
                if (continuingSequenceList.isEmpty()) {
                    continue;
                }

                for (AudioFormatSequence continuingSequence : continuingSequenceList) {
                    if (continuingSequence.sequenceFinished(destFormat)) {
                        matchingSequence = continuingSequence;
                        break;
                    }
                }
                nextSequencesToCheck.addAll(continuingSequenceList);
            }
        }

        if (matchingSequence == null) {
            StringBuilder sb = new StringBuilder(
                    "No audio conversion path exists for: ");
            sb.append(srcFormat.name()).append(" -> ")
                    .append(destFormat.name()).append(".");
            statusHandler.info(sb.toString());
            return false;
        }

        List<BMHAudioFormat> conversionSequence = matchingSequence
                .getSequence();

        /*
         * Build a log statement announcing the recently discovered conversion
         * path.
         */
        StringBuilder sb = new StringBuilder("Found audio conversion path: ");
        sb.append(srcFormat.name());
        for (BMHAudioFormat sequenceFormat : conversionSequence) {
            sb.append(" -> ").append(sequenceFormat.name());
        }
        statusHandler.info(sb.toString());

        this.constructChainedConverter(srcFormat, destFormat,
                conversionSequence);
        return true;
    }

    private void constructChainedConverter(final BMHAudioFormat srcFormat,
            final BMHAudioFormat destFormat,
            final List<BMHAudioFormat> conversionSequence) {
        List<IAudioConverter> audioPath = new LinkedList<>();
        for (BMHAudioFormat bmhAudioFormat : conversionSequence) {
            audioPath.add(this.registeredAudioConverters.get(bmhAudioFormat));
        }

        this.chainedAudioConverters.put(new ChainedConversionKey(srcFormat,
                destFormat), new ChainedAudioConverter(srcFormat, destFormat,
                audioPath));
    }

    /**
     * Uses an audio converter associated with the specified desired format to
     * generate and return the raw converted audio
     * 
     * @param source
     *            the raw source audio bytes
     * @param sourceFormat
     *            the current audio format
     * @param destinationFormat
     *            the desired audio format
     * @return the raw converted audio
     * @throws UnsupportedAudioFormatException
     *             when an unrecognized and/or unsupported audio format is
     *             encountered
     * @throws AudioConversionException
     *             when the audio conversion fails
     */
    public byte[] convertAudio(byte[] source, BMHAudioFormat sourceFormat,
            BMHAudioFormat destinationFormat)
            throws UnsupportedAudioFormatException, AudioConversionException {

        if (source == null) {
            throw new IllegalArgumentException(
                    "Required argument source can not be NULL.");
        }
        if (sourceFormat == null) {
            throw new IllegalArgumentException(
                    "Required argument sourceFormat can not be NULL.");
        }
        if (destinationFormat == null) {
            throw new IllegalArgumentException(
                    "Required argument destinationFormat can not be NULL.");
        }

        /* Does a converter exist for the output audio type? */
        IAudioConverter converter = this.registeredAudioConverters
                .get(destinationFormat);
        if (converter == null) {
            throw new AudioConversionException(
                    "No audio converter has been registered for the "
                            + destinationFormat.toString() + " audio format!");
        }

        /* Does the converter support the provided input audio type? */
        UnsupportedAudioFormatException ex = null;
        try {
            converter.verifySupportedAudioFormat(sourceFormat);
        } catch (UnsupportedAudioFormatException e) {
            ex = e;
        }

        if (ex != null) {
            // Determine if a chained audio converter can be used.
            ChainedConversionKey key = new ChainedConversionKey(sourceFormat,
                    destinationFormat);
            converter = this.chainedAudioConverters.get(key);
            if (converter == null) {
                throw ex;
            }
        }

        return converter.convertAudio(source, sourceFormat);
    }

    /**
     * Returns the supported audio formats based on the converters that have
     * been successfully registered.
     * 
     * @return the supported audio formats.
     */
    public Set<BMHAudioFormat> getSupportedFormats() {
        synchronized (this.registeredAudioConverters) {
            return this.registeredAudioConverters.keySet();
        }
    }
}