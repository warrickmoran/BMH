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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.DacVoiceStatus;
import com.raytheon.uf.edex.bmh.dactransmit.exceptions.MalformedDacStatusException;

/**
 * A parsed form of the status/heartbeat message that is transmitted by the DAC
 * during an active transmission session. Provides the following data:
 * <ul>
 * <li>Voltage levels on the 2 power supplies.
 * <li>Current size of the DAC's jitter buffer for the current session.
 * <li>Output gain level for the radio transmitters.
 * <li>Audio playback status for all the channels.
 * <li>Number of recoverable packet errors for the current session.
 * <li>Number of unrecoverable packet errors for the current session.
 * </ul>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Used logback for logging.
 * Jul 31, 2014  #3286     dgilling     Send DAC status back to CommsManager.
 * Aug 12, 2014  #3486     bsteffen     Remove tranmistter group name
 * Aug 25, 2014  #3286     bsteffen     Fix buffer size alerting logic.
 * Nov 11, 2014  #3817     bsteffen     Make buildNotification public
 * Jan 19, 2015  #3912     bsteffen     Always log when jitter buffer is outside threshold.
 * Jan 23, 2015  #3995     rjpeter      Correct bufferSize to always be within valid range.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacStatusMessage {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int JITTER_BUFFER_SIZE = 256;

    private final static char STATUS_MSG_INDICATOR = '0';

    private final static String SEPARATOR = ",";

    private final static String NO_VOLTAGE = "----";

    private final static int PSU1_VOLTAGE_TOKEN = 0;

    private final static int PSU2_VOLTAGE_TOKEN = 1;

    private final static int BUFFER_STATE_TOKEN = 2;

    private final static int RADIO1_GAIN_TOKEN = 3;

    private final static int RADIO2_GAIN_TOKEN = 4;

    private final static int RADIO3_GAIN_TOKEN = 5;

    private final static int RADIO4_GAIN_TOKEN = 6;

    private final static int AUDIO_DECTECT_TOKEN = 7;

    private final static int RECOVERED_PACKET_TOKEN = 8;

    private final static int UNRECOVERED_PACKET_TOKEN = 9;

    private final static int NUMBER_OF_RADIOS = 4;

    private double psu1Voltage;

    private double psu2Voltage;

    private int bufferSize;

    private double[] outputGain;

    private DacVoiceStatus[] voiceStatus;

    private int recoverablePacketErrors;

    private int unrecoverablePacketErrors;

    /* TODO: REMOVE when random broadcasting silence messages fixed. */
    private final String rawMessage;

    /* This is useful for debugging */
    private final long receiveTime;

    /* Not part of the original status, but can be added in for better logging. */
    private Integer sequenceNumber;

    /**
     * Parses a DAC heartbeat message into its component parts. Splits the DAC's
     * message by the token character {@code ','} into the individual fields.
     * 
     * @param rawMessage
     *            The message sent to us from the DAC as a character string.
     * @throws MalformedDacStatusException
     *             If the message sent from the DAC doesn't correctly parse.
     */
    public DacStatusMessage(final String rawMessage)
            throws MalformedDacStatusException {
        this.receiveTime = System.currentTimeMillis();
        this.rawMessage = rawMessage;

        if (rawMessage.charAt(0) != STATUS_MSG_INDICATOR) {
            logger.debug("Received malformed status message: " + rawMessage);
            throw new MalformedDacStatusException(
                    "This is not a valid DAC heartbeat/status message.");
        }

        String[] tokens = rawMessage.substring(1).split(SEPARATOR);
        if (tokens.length != 10) {
            throw new MalformedDacStatusException(
                    "This is not a valid DAC Heartbeat/Status message: only "
                            + tokens.length + " tokens detected.");
        }

        String psu1VoltageString = tokens[PSU1_VOLTAGE_TOKEN];
        if (NO_VOLTAGE.equals(psu1VoltageString)) {
            psu1Voltage = Double.NaN;
        } else {
            try {
                psu1Voltage = Double.parseDouble(psu1VoltageString.substring(0,
                        psu1VoltageString.length() - 1));
            } catch (NumberFormatException e) {
                throw new MalformedDacStatusException(
                        "Malformed value for PSU 1 voltage reading.", e);
            }
        }

        String psu2VoltageString = tokens[PSU2_VOLTAGE_TOKEN];
        if (NO_VOLTAGE.equals(psu2VoltageString)) {
            psu2Voltage = Double.NaN;
        } else {
            try {
                psu2Voltage = Double.parseDouble(psu2VoltageString.substring(0,
                        psu2VoltageString.length() - 1));
            } catch (NumberFormatException e) {
                throw new MalformedDacStatusException(
                        "Malformed value for PSU 2 voltage reading.", e);
            }
        }

        /*
         * It appears the buffer information is session specific, while all
         * other information appears to be global. Recoverable/unrecoverable
         * packet counts might also be session specific, but can't tell for
         * sure.
         */
        try {
            bufferSize = Integer.parseInt(tokens[BUFFER_STATE_TOKEN]);

            /*
             * buffer can only be from 0-255, dacs have been known to give
             * values outside of the valid range
             */
            if ((bufferSize < 0) || (bufferSize >= JITTER_BUFFER_SIZE)) {
                int oldBufferSize = bufferSize;

                while (bufferSize < 0) {
                    bufferSize += JITTER_BUFFER_SIZE;
                }

                while (bufferSize >= JITTER_BUFFER_SIZE) {
                    bufferSize -= JITTER_BUFFER_SIZE;
                }

                logger.warn("Received bufferSize [" + oldBufferSize
                        + "] outside of valid range (0-"
                        + (JITTER_BUFFER_SIZE - 1) + ").  Corrected to ["
                        + bufferSize + "]");
            }
        } catch (NumberFormatException e) {
            throw new MalformedDacStatusException(
                    "Malformed value for jitter buffer size.", e);
        }

        try {
            outputGain = new double[NUMBER_OF_RADIOS];
            outputGain[0] = Double.parseDouble(tokens[RADIO1_GAIN_TOKEN]);
            outputGain[1] = Double.parseDouble(tokens[RADIO2_GAIN_TOKEN]);
            outputGain[2] = Double.parseDouble(tokens[RADIO3_GAIN_TOKEN]);
            outputGain[3] = Double.parseDouble(tokens[RADIO4_GAIN_TOKEN]);
        } catch (NumberFormatException e) {
            throw new MalformedDacStatusException(
                    "Malformed value for radio output gain reading.", e);
        }

        try {
            voiceStatus = new DacVoiceStatus[NUMBER_OF_RADIOS];
            String voiceStatusString = tokens[AUDIO_DECTECT_TOKEN];
            voiceStatus[0] = DacVoiceStatus.fromStatusCode(Integer
                    .parseInt(voiceStatusString.substring(0, 1)));
            voiceStatus[1] = DacVoiceStatus.fromStatusCode(Integer
                    .parseInt(voiceStatusString.substring(1, 2)));
            voiceStatus[2] = DacVoiceStatus.fromStatusCode(Integer
                    .parseInt(voiceStatusString.substring(2, 3)));
            voiceStatus[3] = DacVoiceStatus.fromStatusCode(Integer
                    .parseInt(voiceStatusString.substring(3, 4)));
        } catch (NumberFormatException e) {
            throw new MalformedDacStatusException(
                    "Malformed value for transmitter voice status reading.", e);
        }

        try {
            recoverablePacketErrors = Integer
                    .parseInt(tokens[RECOVERED_PACKET_TOKEN]);
        } catch (NumberFormatException e) {
            throw new MalformedDacStatusException(
                    "Malformed value for recoverable packet errors reading.", e);
        }

        try {
            unrecoverablePacketErrors = Integer
                    .parseInt(tokens[UNRECOVERED_PACKET_TOKEN]);
        } catch (NumberFormatException e) {
            throw new MalformedDacStatusException(
                    "Malformed value for unrecoverable packet errors reading.",
                    e);
        }
    }

    /**
     * Add a sequence number to this status which will be used to generate more
     * useful logs during
     * {@link #validateStatus(DacSessionConfig, DacStatusMessage)}.
     * 
     * @param sequenceNumber
     *            the sequence number of the last packet sent from this process
     *            to the dac.
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DacStatusMessage [psu1Voltage=" + psu1Voltage
                + ", psu2Voltage=" + psu2Voltage + ", bufferSize=" + bufferSize
                + ", outputGain=" + Arrays.toString(outputGain)
                + ", voiceStatus=" + Arrays.toString(voiceStatus)
                + ", recoverablePacketErrors=" + recoverablePacketErrors
                + ", unrecoverablePacketErrors=" + unrecoverablePacketErrors
                + "]";
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Validates this status message against the given {@code DacSessionConfig}
     * and a copy of the previous status message. If any inconsistencies are
     * found, they will be logged.
     * 
     * @param sessionConfig
     *            The configuration information for this session.
     * @param previous
     *            The copy of the last status message received. Any important
     *            differences between the 2 statuses will cause an error to be
     *            logged.
     * @return If significant conditions were found or a change was noted
     *         between this and the previous state, a
     *         {@code DacHardwareStatusNotification} will be generated to send
     *         back to the CommsManager.
     */
    public DacHardwareStatusNotification validateStatus(
            final DacSessionConfig sessionConfig,
            final DacStatusMessage previous) {
        /*
         * TODO this code needs configurable thresholds for some of the settings
         * to determine whether or not errors get logged/status reported back to
         * CommsManager.
         */

        boolean reportStatus = false;

        /* Always report the first status message to establish a baseline. */
        if (previous == null) {
            reportStatus = true;
        }

        if (previous != null) {
            boolean psu1Okay = !Double.isNaN(psu1Voltage);
            boolean psu2Okay = !Double.isNaN(psu2Voltage);
            boolean prevPsu1Okay = !Double.isNaN(previous.psu1Voltage);
            boolean prevPsu2Okay = !Double.isNaN(previous.psu2Voltage);

            if (psu1Okay != prevPsu1Okay) {
                if (!psu1Okay) {
                    logger.error("DAC Power Supply 1 is offline.");
                } else {
                    logger.info("DAC Power Supply 1 is back online.");
                }
                reportStatus = true;
            }

            if (psu2Okay != prevPsu2Okay) {
                if (!psu2Okay) {
                    logger.error("DAC Power Supply 2 is offline.");
                } else {
                    logger.info("DAC Power Supply 2 is back online.");
                }
                reportStatus = true;
            }

            boolean alertPrevBuffer = (previous.bufferSize <= DataTransmitConstants.ALERT_LOW_PACKETS_IN_BUFFER)
                    || (previous.bufferSize >= DataTransmitConstants.ALERT_HIGH_PACKETS_IN_BUFFER);
            boolean alertCurrentBuffer = (bufferSize <= DataTransmitConstants.ALERT_LOW_PACKETS_IN_BUFFER)
                    || (bufferSize >= DataTransmitConstants.ALERT_HIGH_PACKETS_IN_BUFFER);
            if (alertCurrentBuffer || alertPrevBuffer) {
                if (alertCurrentBuffer) {
                    Object packetsSent;
                    if ((sequenceNumber != null)
                            && (previous.sequenceNumber != null)) {
                        packetsSent = sequenceNumber - previous.sequenceNumber;
                    } else {
                        packetsSent = "an unknown number";
                    }
                    logger.error(
                            "DAC's jitter buffer size is outside acceptable thresholds. Current reading is {} packets.\n"
                                    + "Previous Reading was {} packets {}ms ago and {} packets have been sent since then.",
                            bufferSize, previous.bufferSize,
                            (receiveTime - previous.receiveTime), packetsSent);
                } else {
                    logger.info("DAC's jitter buffer size back within acceptable thresholds. Current reading is: "
                            + bufferSize);
                }
                reportStatus = true;
            }

            if (recoverablePacketErrors > 0) {
                logger.warn("Detected "
                        + recoverablePacketErrors
                        + " new recoverable packet errors in this session since the last status update.");
                reportStatus = true;
            }

            if (unrecoverablePacketErrors > 0) {
                logger.error("Detected "
                        + unrecoverablePacketErrors
                        + " new unrecoverable packet errors in this session since the last status update.");
                reportStatus = true;
            }
        } else {
            if (Double.isNaN(psu1Voltage)) {
                logger.error("DAC Power Supply 1 is offline.");
            }

            if (Double.isNaN(psu2Voltage)) {
                logger.error("DAC Power Supply 2 is offline.");
            }

            if ((bufferSize <= DataTransmitConstants.ALERT_LOW_PACKETS_IN_BUFFER)
                    || (bufferSize >= DataTransmitConstants.ALERT_HIGH_PACKETS_IN_BUFFER)) {
                logger.error("DAC's jitter buffer size is outside acceptable thresholds. Current reading is: "
                        + bufferSize);
            }

            if (recoverablePacketErrors > 0) {
                logger.warn("Detected " + recoverablePacketErrors
                        + " recoverable packet errors in this session.");
            }

            if (unrecoverablePacketErrors > 0) {
                logger.error("Detected " + unrecoverablePacketErrors
                        + " unrecoverable packet errors in this session.");
            }
        }

        for (Integer channelNumber : sessionConfig.getTransmitters()) {
            int index = channelNumber - 1;

            if (previous != null) {
                if (voiceStatus[index] != previous.voiceStatus[index]) {
                    if (voiceStatus[index] == DacVoiceStatus.IP_AUDIO) {
                        logger.info("DAC channel " + channelNumber
                                + " has resumed broadcasting IP audio.");
                    } else {
                        logger.warn("DAC channel "
                                + channelNumber
                                + " appears to have stopped broadcasting IP audio stream. Reporting voice status of "
                                + voiceStatus[index]);
                        logger.debug("Current jitter buffer size: "
                                + bufferSize);
                        logger.debug("Original status from DAC: " + rawMessage);
                    }
                    reportStatus = true;
                }
            } else {
                if (voiceStatus[index] != DacVoiceStatus.IP_AUDIO) {
                    logger.warn("DAC channel "
                            + channelNumber
                            + " doesn't appear to be receiving audio broadcast stream. Reporting voice status of "
                            + voiceStatus[index]);
                }
            }
        }

        DacHardwareStatusNotification notify = reportStatus ? buildNotification(sessionConfig)
                : null;
        return notify;
    }

    public DacHardwareStatusNotification buildNotification(
            final DacSessionConfig sessionConfig) {
        int[] validChannels = Ints.toArray(sessionConfig.getTransmitters());

        double[] gainValues = new double[validChannels.length];
        DacVoiceStatus[] voiceValues = new DacVoiceStatus[validChannels.length];
        for (int i = 0; i < validChannels.length; i++) {
            int index = validChannels[i] - 1;
            gainValues[i] = outputGain[index];
            voiceValues[i] = voiceStatus[index];
        }

        return new DacHardwareStatusNotification(null, psu1Voltage,
                psu2Voltage, bufferSize, validChannels, gainValues,
                voiceValues, recoverablePacketErrors, unrecoverablePacketErrors);
    }
}
