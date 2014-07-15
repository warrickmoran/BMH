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
package com.raytheon.uf.edex.bmh.dactransmit;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConfig;

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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacTransmitMain {

    private static final Logger logger = LoggerFactory
            .getLogger(DacTransmitMain.class);

    public static void main(String[] args) {
        logger.info("Starting DacTransmit.");

        DacTransmitArgParser argParser = new DacTransmitArgParser();

        DacSessionConfig sessionConfig = null;
        try {
            sessionConfig = argParser.parseCommandLine(args);
        } catch (ParseException e) {
            logger.error("Invalid argument specified.", e);
            argParser.printUsage();
        }

        if (sessionConfig != null) {
            if (!sessionConfig.isPrintHelp()) {
                try {
                    DacSession session = new DacSession(sessionConfig);
                    session.startPlayback();
                } catch (Throwable t) {
                    logger.error("Unhandled exception thrown from DacSession:",
                            t);
                }
            } else {
                argParser.printUsage();
            }
        }

        logger.info("Exiting DacTransmit.");
    }
}
