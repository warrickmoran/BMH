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
package com.raytheon.uf.edex.bmh.test.util.tones;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.configuration.Configuration;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;
import com.raytheon.uf.common.bmh.tones.TonesManager.TRANSFER_TYPE;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.test.AbstractWavFileGeneratingTest;
import com.raytheon.uf.edex.bmh.test.TestProcessingFailedException;

/**
 * NOT OPERATIONAL CODE! Created to demonstrate the tone generation capability.
 * Refer to the data/util/tone directory in this plugin for a template input
 * properties file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * Jul 17, 2014 3383       bkowal      Updated to use the Audio Conversion API.
 * Oct 17, 2014 3655       bkowal      Move tones to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Deprecated
public class ToneGenerationTester extends AbstractWavFileGeneratingTest {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToneGenerationTester.class);

    private static final String TEST_NAME = "Tone Generation Util";

    private static final Map<String, TONE_TYPE> toneTypeLookupMap;

    private static final Map<String, TRANSFER_TYPE> transferTypeLookupMap;

    private static final String TONE_DIRECTORY_INPUT_PROPERTY = "bmh.util.tones.test.directory.input";

    private static final String TONE_DIRECTORY_OUTPUT_PROPERTY = "bmh.util.tones.test.directory.output";

    private static enum TONE_TYPE {
        ALERT, SAME, TRANSFER
    }

    static {
        toneTypeLookupMap = new HashMap<String, TONE_TYPE>();
        for (TONE_TYPE toneType : TONE_TYPE.values()) {
            toneTypeLookupMap.put(toneType.toString(), toneType);
        }

        transferTypeLookupMap = new HashMap<String, TRANSFER_TYPE>();
        for (TRANSFER_TYPE transferType : TRANSFER_TYPE.values()) {
            transferTypeLookupMap.put(transferType.toString(), transferType);
        }
    }

    private static final class INPUT_PROPERTIES {
        private static final String TONE_PROPERTY_PREFIX = "tone.";

        public static final String TONE_TYPE_PROPERTY = TONE_PROPERTY_PREFIX
                + "type";

        public static final String TONE_AMPLITUDE_PROPERTY = TONE_PROPERTY_PREFIX
                + "amplitude";

        public static final String TONE_DURATION_PROPERTY = TONE_PROPERTY_PREFIX
                + "duration";

        public static final String TONE_MESSAGE_PROPERTY = TONE_PROPERTY_PREFIX
                + "message";

        public static final String TONE_TRANSFER_PROPERTY = TONE_PROPERTY_PREFIX
                + "transfer";
    }

    public ToneGenerationTester() {
        super(statusHandler, TEST_NAME, TONE_DIRECTORY_INPUT_PROPERTY,
                TONE_DIRECTORY_OUTPUT_PROPERTY);
    }

    protected Object processInput(Configuration configuration,
            final String inputFileName) throws TestProcessingFailedException {
        String toneTypeSpecifier = super.getStringProperty(configuration,
                INPUT_PROPERTIES.TONE_TYPE_PROPERTY, inputFileName);
        TONE_TYPE toneType = toneTypeLookupMap.get(toneTypeSpecifier);
        if (toneType == null) {
            throw new TestProcessingFailedException(
                    "Invalid tone type specified: " + toneTypeSpecifier
                            + " in input file: " + inputFileName + "!");
        }

        byte[] audioData = null;
        switch (toneType) {
        case ALERT:
            /* Retrieve the frequency, amplitude, and duration from the input. */
            Double amplitude = super.getDoubleProperty(configuration,
                    INPUT_PROPERTIES.TONE_AMPLITUDE_PROPERTY, inputFileName);
            Double duration = super.getDoubleProperty(configuration,
                    INPUT_PROPERTIES.TONE_DURATION_PROPERTY, inputFileName);
            statusHandler
                    .info("Attempting to generate Alert tone for input file: "
                            + inputFileName + " ...");
            try {
                audioData = TonesManager.generateAlertTone(amplitude, duration);
            } catch (ToneGenerationException e) {
                throw new TestProcessingFailedException(
                        "Failed to generate an Alert tone for input file: "
                                + inputFileName + "!", e);
            }
            break;
        case SAME:
            /* Retrieve the SAME Message from the input. */
            String SAMEMessage = super.getStringProperty(configuration,
                    INPUT_PROPERTIES.TONE_MESSAGE_PROPERTY, inputFileName);
            statusHandler
                    .info("Attempting to generate SAME tone for input file: "
                            + inputFileName + " ...");
            try {
                audioData = TonesManager.generateSAMETone(SAMEMessage);
            } catch (ToneGenerationException e) {
                throw new TestProcessingFailedException(
                        "Failed to generate a SAME tone for input file: "
                                + inputFileName + "!", e);
            }
            break;
        case TRANSFER:
            /* Retrieve the transfer type from the input. */
            String transferTypeSpecifier = super.getStringProperty(
                    configuration, INPUT_PROPERTIES.TONE_TRANSFER_PROPERTY,
                    inputFileName);
            TRANSFER_TYPE transferType = transferTypeLookupMap
                    .get(transferTypeSpecifier);
            if (transferType == null) {
                throw new TestProcessingFailedException(
                        "Invalid transfer type specified: "
                                + transferTypeSpecifier + " in input file: "
                                + inputFileName + "!");
            }

            statusHandler
                    .info("Attempting to generate Transfer tone for input file: "
                            + inputFileName + " ...");
            try {
                audioData = TonesManager.generateTransferTone(transferType);
            } catch (ToneGenerationException e) {
                throw new TestProcessingFailedException(
                        "Failed to generate a Transfer tone for input file: "
                                + inputFileName + "!", e);
            }
            break;
        }

        super.writeWavData(BMHAudioFormat.ULAW, audioData,
                this.generateOutputFileName(toneType));

        return null;
    }

    private String generateOutputFileName(TONE_TYPE toneType) {
        StringBuilder stringBuilder = new StringBuilder("tone");
        stringBuilder.append(toneType.toString());
        stringBuilder.append("_");
        stringBuilder.append(System.currentTimeMillis());

        return stringBuilder.toString();
    }
}