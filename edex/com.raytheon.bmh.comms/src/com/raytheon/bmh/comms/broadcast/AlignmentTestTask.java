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
package com.raytheon.bmh.comms.broadcast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.raytheon.uf.common.bmh.TransmitterAlignmentException;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.TransmitterAlignmentTestCommand;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.DAC_MODE;
import com.raytheon.uf.edex.bmh.dactransmit.DacMaintenanceArgParser;

/**
 * Starts a comms manager in maintenance mode to run an alignment test. Reports
 * on the status of the alignment test based on the exit code of the process.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AlignmentTestTask extends AbstractBroadcastingTask {

    private static final String DESCRIPTION = "alignment test task";

    private final TransmitterAlignmentTestCommand command;

    private final CommsConfig commsConfig;

    private final BroadcastStreamServer streamServer;

    private Process maintenanceDacSession;

    /** NOT THREAD SAFE: only use from the run method */
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    /**
     * @param name
     * @param socket
     */
    public AlignmentTestTask(Socket socket,
            TransmitterAlignmentTestCommand command,
            final CommsConfig commsConfig,
            final BroadcastStreamServer streamServer) {
        super(UUID.randomUUID().toString().toUpperCase(), DESCRIPTION, socket);
        this.command = command;
        this.commsConfig = commsConfig;
        this.streamServer = streamServer;
    }

    @Override
    public void run() {
        List<String> args = null;
        try {
            args = this.prepare();
        } catch (TransmitterAlignmentException e) {
            try {
                logger.error("Alignment test initialization has failed.", e);
                super.sendClientReplyMessage(super.buildErrorStatus(
                        "Alignment test initialization has failed.", e, null));
            } catch (Exception e1) {
                logger.error("Failed to send a reply to the client!", e);
            }
            return;
        }

        logger.info("Starting a dac session in maintenance mode ...");
        ProcessBuilder startCommand = new ProcessBuilder(args);
        startCommand.environment().put("TRANSMITTER_GROUP",
                this.command.getTransmitterGroup().getName());
        startCommand.environment().put("BMH_LOG_BASE",
                "dactransmit-maintenance");

        // Prepare logging
        /*
         * Send console output to a file. This is quite rudimentary, the dac
         * transmit process itself has a more refined log configuration that
         * will hopefully be used for all output but if any messages do make it
         * to stdout/stderr we do not want them interleaved with this process
         * logs and we do not want them to be silently discarded.
         */
        String logDate = logDateFormat.format(new Date());
        StringBuilder logFileName = new StringBuilder(64);
        logFileName.append("dactransmit-");
        logFileName.append("maintenance-");
        logFileName.append(this.command.getTransmitterGroup().getName());
        logFileName.append("-console-");
        logFileName.append(logDate);
        logFileName.append(".log");
        Path logFilePath = Paths.get(BMHConstants.getBmhHomeDirectory())
                .resolve("logs").resolve(logFileName.toString());
        startCommand.redirectOutput(Redirect.appendTo(logFilePath.toFile()));
        startCommand.redirectError(Redirect.appendTo(logFilePath.toFile()));

        long start = System.currentTimeMillis();

        try {
            this.maintenanceDacSession = startCommand.start();
        } catch (IOException e) {
            final String msg = "Failed to start the dac transmit in maintenance mode!";
            logger.error(
                    "Failed to start the dac transmit in maintenance mode!", e);
            try {
                super.sendClientReplyMessage(super.buildErrorStatus(msg, e,
                        null));
            } catch (Exception e1) {
                logger.error("Failed to send a reply to the client!", e);
            }
        }

        int returnCode = 9999; // initialize to some value other than 0 (0 =
                               // success)
        try {
            returnCode = this.maintenanceDacSession.waitFor();
        } catch (InterruptedException e) {
            logger.warn(
                    "Interrupted while waiting for the dac maintenance session to terminate.",
                    e);
        }

        StringBuilder resultMsg = new StringBuilder(
                "The transmitter alignment test ");
        boolean success = false;
        if (returnCode == 0) {
            resultMsg.append("successfully finished");
            success = true;
        } else {
            resultMsg.append("failed ");
        }
        resultMsg.append(" in ");
        resultMsg.append(Double.toString((System.currentTimeMillis() - start)));
        resultMsg.append(" ms.");
        logger.info(resultMsg.toString());

        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setStatus(success);
        status.setMessage(resultMsg.toString());
        try {
            super.sendClientReplyMessage(status);
        } catch (Exception e) {
            logger.error("Failed to send a reply to the client!", e);
        }

        this.streamServer.broadcastTaskFinished(this.getName());
    }

    private List<String> prepare() throws TransmitterAlignmentException {
        logger.info("Preparing to run the alignment test ...");

        int dacDataPort = this.findDataPort();
        logger.info("Using dac data port: " + dacDataPort + ".");

        logger.info("Building command line to start the process ...");
        List<String> args = new ArrayList<>();
        args.add(commsConfig.getDacTransmitStarter());
        args.add("-" + DacMaintenanceArgParser.DAC_MODE);
        args.add(DAC_MODE.MAINTENANCE.getArg());
        args.add("-" + DacMaintenanceArgParser.DAC_HOSTNAME_OPTION_KEY);
        args.add(this.command.getDacHostname());
        args.add("-" + DacMaintenanceArgParser.DATA_PORT_OPTION_KEY);
        args.add(Integer.toString(dacDataPort));
        args.add("-" + DacMaintenanceArgParser.TRANSMITTER_OPTION_KEY);
        StringBuilder radios = new StringBuilder(4);
        for (int radio : this.command.getRadios()) {
            radios.append(radio);
        }
        args.add(radios.toString());
        args.add("-" + DacMaintenanceArgParser.TRANSMISSION_DB_TARGET_KEY);
        args.add(Double.toString(this.command.getDecibelTarget()));
        args.add("-" + DacMaintenanceArgParser.MAINT_AUDIO_LENGTH_KEY);
        args.add(Integer.toString(this.command.getBroadcastDuration()));
        args.add("-" + DacMaintenanceArgParser.INPUT_AUDIO_OPTION_KEY);
        args.add(this.command.getInputAudioFile());

        return args;
    }

    private int findDataPort() throws TransmitterAlignmentException {
        logger.info("Searching for an available dac data port ...");

        List<Integer> allowedDataPorts = new ArrayList<Integer>(
                this.command.getAllowedDataPorts());

        /*
         * Retrieve the {@link DacConfig} associated with the transmitter that
         * we will be connecting to.
         */
        DacConfig alignmentDacConfig = null;
        if (this.commsConfig != null && this.commsConfig.getDacs() != null) {
            for (DacConfig dacConfig : this.commsConfig.getDacs()) {
                if (dacConfig.getIpAddress().equals(
                        this.command.getDacHostname())) {
                    alignmentDacConfig = dacConfig;
                    break;
                }
            }
        }

        /*
         * No other transmitters are currently connected to the dac, return the
         * last dac data port that is allowed.
         */
        if (alignmentDacConfig == null
                || alignmentDacConfig.getChannels() == null
                || alignmentDacConfig.getChannels().isEmpty()) {
            return allowedDataPorts.get(allowedDataPorts.size() - 1);
        }

        /*
         * Build a list of the dac data ports that have already been reserved /
         * are in use.
         */
        List<Integer> reservedDataPorts = new ArrayList<>(alignmentDacConfig
                .getChannels().size());
        for (DacChannelConfig channel : alignmentDacConfig.getChannels()) {
            reservedDataPorts.add(channel.getDataPort());
        }

        /*
         * The {@link CommsConfigurator} will start at the beginning of the list
         * when assigning ports. So, we will start at the end of the list to
         * reduce the potential for conflicts during the test window if one of
         * the other dac transmits that is using the set of ports is altered.
         */
        while (allowedDataPorts.isEmpty() == false) {
            int dataPort = allowedDataPorts.remove(allowedDataPorts.size() - 1);
            if (reservedDataPorts.contains(dataPort) == false) {
                return dataPort;
            }
        }

        throw new TransmitterAlignmentException(
                "Unable to find an available dac data port!");
    }

    @Override
    public void shutdown() {
        if (this.maintenanceDacSession == null) {
            return;
        }

        // stop the process forcefully. error status will be returned to CAVE.
        this.maintenanceDacSession.destroy();
    }
}