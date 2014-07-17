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
package com.raytheon.uf.edex.bmh.test.dac;

import java.io.IOException;
import java.io.InputStreamReader;

import com.raytheon.uf.common.bmh.dac.DacLiveStreamer;
import com.raytheon.uf.common.bmh.dac.DacPlaybackException;
import com.raytheon.uf.common.bmh.dac.DacReceiveThread;

/**
 * A simple executable class that was created to test and demonstrate the dac
 * streaming capability. Ran independently from EDEX.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014 3374       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacStreamTester {

    /* Just use dac channel 1 for this test. */
    private static final int DAC_CHANNEL = 4;

    /* Port used to connect to the dac. */
    private static final int DAC_REBROADCAST_PORT = 21000;

    /**
     * @param args
     */
    public static void main(String[] args) {
        /* Initialize the thread that will be reading data from the dac. */
        DacReceiveThread dacReceiveThread = new DacReceiveThread(
                DAC_REBROADCAST_PORT);

        /*
         * Initialize the listener that will be listening for received data,
         * buffering it, and streaming it.
         */
        DacLiveStreamer dacLiveStreamer = null;
        try {
            dacLiveStreamer = new DacLiveStreamer(DAC_CHANNEL);
        } catch (DacPlaybackException e) {
            System.out.println("Failed to create the Dac Live Streamer!");
            e.printStackTrace();
            System.exit(-1);
        }

        dacReceiveThread.subscribe(dacLiveStreamer);

        /* Start the receive thread. */
        dacReceiveThread.start();

        boolean wait = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        System.out.println("Press Any Key to Continue ...");
        /* Wait */
        while (wait) {
            try {
                streamReader.read();
                wait = false;
            } catch (IOException e) {
                System.out.println("Failed to read stdin!");
                e.printStackTrace();
            }
        }

        System.out.println("Shutting Down ...");

        dacReceiveThread.halt();
        try {
            dacReceiveThread.join();
        } catch (InterruptedException e) {
            // Ignore Interrupted Exception. Stopping the Application.
        }

        /* Shutdown the live streamer. */
        dacLiveStreamer.dispose();
    }
}