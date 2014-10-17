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
package com.raytheon.bmh.dacsimulator;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.dacsimulator.channel.DacSimulatedChannel;
import com.raytheon.bmh.dacsimulator.channel.JitterBuffer;

/**
 * Entry point for DAC Simulator application.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 02, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacSimulatorMain {

    private static final Logger logger = LoggerFactory
            .getLogger(DacSimulatorMain.class);

    public static void main(String[] args) throws SocketException,
            InterruptedException {
        logger.info("Starting DacSimulator.");

        DacSimulatorArgParser argParser = new DacSimulatorArgParser();

        DacSimulatorConfig config = null;
        try {
            config = argParser.parseCommandLine(args);
        } catch (ParseException e) {
            logger.error("Invalid argument specified.", e);
            argParser.printUsage();
        }

        if (config != null) {
            logger.debug("Simulator configuration: {}", config);

            if (!config.isPrintHelp()) {
                int numChannels = config.getDacChannels().size();
                List<JitterBuffer> buffers = new ArrayList<>(numChannels);
                for (int i = 0; i < numChannels; i++) {
                    JitterBuffer buffer = new JitterBuffer(
                            config.getMinimumBufferSize());
                    buffers.add(buffer);
                }

                List<DacSimulatedChannel> dacChannels = new ArrayList<>(
                        numChannels);
                for (int i = 0; i < numChannels; i++) {
                    DacSimChannelConfig channelConfig = config.getDacChannels()
                            .get(i);
                    DacSimulatedChannel channel = new DacSimulatedChannel(
                            channelConfig, buffers.get(i), buffers);
                    dacChannels.add(channel);
                }

                DacRebroadcastThread rebroadcastThread = new DacRebroadcastThread(
                        config.getRebroadcastAddress(),
                        config.getRebroadcastPort(), buffers);

                // start all threads
                for (DacSimulatedChannel channel : dacChannels) {
                    channel.start();
                }
                rebroadcastThread.start();

                // wait for termination
                for (DacSimulatedChannel channel : dacChannels) {
                    channel.waitForTermination();
                }
                rebroadcastThread.join();
            } else {
                argParser.printUsage();
            }
        }

        logger.info("Exiting DacSimulator.");
    }
}
