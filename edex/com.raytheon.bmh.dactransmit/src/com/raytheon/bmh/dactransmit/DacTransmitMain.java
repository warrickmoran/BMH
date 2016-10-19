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
package com.raytheon.bmh.dactransmit;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.raytheon.bmh.dactransmit.dacsession.AbstractDacConfig;
import com.raytheon.bmh.dactransmit.dacsession.IDacSession;
import com.raytheon.bmh.dactransmit.dacsession.IDacSession.SHUTDOWN_STATUS;
import com.raytheon.uf.common.bmh.audio.SAMEPaddingConfiguration;
import com.raytheon.uf.edex.bmh.LoadedSAMEPaddingConfiguration;

/**
 * Main entry point for DacTransmit program. Reads from a specified directory
 * for playlist files, sorts them into correct playback order (by priority, then
 * newest creation time), and then plays the playlist continuously until program
 * termination.
 * <p>
 * At this time, this program can only be run via the Eclipse IDE. Future
 * versions will need to be structured so they can be launched as standalone
 * applications via a future CommsManager component.
 * <p>
 * Usage: DacTransmit [--help] -d hostname -p port -c port -t channel -g group
 * -i directory
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Used logback for logging.
 * Jul 15, 2014  #3388     dgilling     Ensure all RuntimeExceptions are
 *                                      caught.
 * Jul 16, 2014  #3286     dgilling     Change execution now that
 *                                      startPlayback() doesn't block.
 * Oct 24, 2014  #3703     bsteffen     Bridge jul and slf4j.
 * Nov 7, 2014   #3630     bkowal       Support maintenance mode.
 * Sep 30, 2016  #5912     bkowal       Retrieve {@link SAMEPaddingConfiguration} during startup.
 * 
 * </pre>
 * 
 * @author dgilling
 */

public class DacTransmitMain {

    private static final Logger logger = LoggerFactory
            .getLogger(DacTransmitMain.class);

    public static void main(String[] args) {
        logger.info("Starting DacTransmit.");

        AbstractDacConfig dacConfig = null;
        AbstractDacArgParser argParser = null;
        try {
            argParser = DacCliArgParser.getDacArgParser(args);
            if (DacCliArgParser.isHelp(args)) {
                argParser.printUsage();
                shutdown(SHUTDOWN_STATUS.SUCCESS);
            }

            /*
             * Check JVM parameters for SAME Padding information.
             */
            SAMEPaddingConfiguration samePaddingConfiguration = null;
            try {
                samePaddingConfiguration = LoadedSAMEPaddingConfiguration
                        .getConfiguration();
            } catch (Exception e) {
                samePaddingConfiguration = new SAMEPaddingConfiguration();
                logger.warn(
                        "Failed to load the SAME Padding Configuration. Using default configuration: "
                                + samePaddingConfiguration.toString() + ".", e);
            }

            dacConfig = argParser.parseCommandLine(args,
                    samePaddingConfiguration);
        } catch (ParseException e) {
            logger.error("Invalid argument specified.", e);
            if (argParser != null) {
                argParser.printUsage();
                shutdown(SHUTDOWN_STATUS.FAILURE);
            }
        }

        if (dacConfig == null) {
            logger.error("Failed to process the dac configuration.");
            shutdown(SHUTDOWN_STATUS.FAILURE);
        }

        SHUTDOWN_STATUS status = SHUTDOWN_STATUS.FAILURE;
        try {
            /*
             * The event bus is using java.util.logging but we use slf4j logging
             * so an adapter needs to be installed to convert.
             */
            SLF4JBridgeHandler.install();
            IDacSession session = dacConfig.buildDacSession();
            session.startPlayback();
            status = session.waitForShutdown();
        } catch (Throwable t) {
            logger.error("Unhandled exception thrown from DacSession:", t);
        }

        shutdown(status);
    }

    private static void shutdown(SHUTDOWN_STATUS status) {
        logger.info("Exiting DacTransmit.");
        System.exit(status.getStatusCode());
    }
}