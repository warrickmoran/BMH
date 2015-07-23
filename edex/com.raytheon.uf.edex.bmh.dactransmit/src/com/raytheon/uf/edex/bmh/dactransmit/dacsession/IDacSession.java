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

import java.io.IOException;

/**
 * Generic reference to a dac session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Jul 22, 2015 4676       bkowal      Set status code in the constructor.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IDacSession {
    public static enum SHUTDOWN_STATUS {
        SUCCESS(0), FAILURE(-1);

        private int statusCode;

        SHUTDOWN_STATUS(int statusCode) {
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * Initiates the playback of audio.
     * 
     * @throws IOException
     */
    public void startPlayback() throws IOException;

    /**
     * Waits for the dac session to shutdown either naturally or due to
     * distress (graceful failure).
     * 
     * @return the execution status indicating whether or not any problems were
     *         encountered during the audio stream.
     */
    public SHUTDOWN_STATUS waitForShutdown();
}