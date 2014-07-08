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

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConfig;

/**
 * Main entry point for DacTransmit program. Reads from a specified directory
 * for audio files and plays them back to the specified DAC endpoint in creation
 * time order (newest->oldest).
 * <p>
 * At this time, this program can only be run via the Eclipse IDE. Future
 * versions will need to be structured so they can be launched as standalone
 * applications via a future CommsManager component.
 * <p>
 * Usage: DacTransmit [--help] -d hostname -p port -c port -t channel -i
 * directory
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014   #3286    dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacTransmitMain {

    public static void main(String[] args) {
        /*
         * FIXME: this and other sysout prints will have to be replaced by a
         * true logging mechanism. See redmine ticket #3367.
         */
        System.out.println("INFO [main] : Starting DacTransmit.");

        DacTransmitArgParser argParser = new DacTransmitArgParser();

        DacSessionConfig sessionConfig = null;
        try {
            sessionConfig = argParser.parseCommandLine(args);
        } catch (ParseException e) {
            System.out.println("ERROR [main] : Invalid argument specified");
            e.printStackTrace();
            argParser.printUsage();
        }

        if (sessionConfig != null) {
            if (!sessionConfig.isPrintHelp()) {
                try {
                    DacSession session = new DacSession(sessionConfig);
                    session.startPlayback();
                } catch (IOException | InterruptedException e) {
                    System.out.println("ERROR [main] : Unhandled exception:");
                    e.printStackTrace();
                }
            } else {
                argParser.printUsage();
            }
        }

        System.out.println("INFO [main] : Exiting DacTransmit.");
    }
}
