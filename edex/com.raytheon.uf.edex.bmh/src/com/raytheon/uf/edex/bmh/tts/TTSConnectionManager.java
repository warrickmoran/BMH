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
package com.raytheon.uf.edex.bmh.tts;

import java.util.List;
import java.util.ArrayList;

/**
 * Responsible for keeping a pool of and managing a collection of connections to
 * the TTS Server. Ensures that TTS connections are not reused until they are
 * ready.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2014 3538       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSConnectionManager {

    private final int maxThreads;

    private List<TimeLockedTTSInterface> ttsInterfaces;

    /**
     * Constructor
     * 
     * @param maxThreads
     *            the maximum number of tts connections available
     * @param ttsServer
     *            the name of the tts server
     * @param ttsPort
     *            the port that the tts server is listening on
     * @param ttsConnectionTimeout
     *            the amount of time before the connection should timeout
     */
    public TTSConnectionManager(final int maxThreads, final String ttsServer,
            final int ttsPort, int ttsConnectionTimeout) {
        this.maxThreads = maxThreads;
        this.ttsInterfaces = new ArrayList<>(this.maxThreads);
        for (int i = 0; i < this.maxThreads; i++) {
            TTSInterface ttsInterface = new TTSInterface(ttsServer, ttsPort,
                    ttsConnectionTimeout);
            TimeLockedTTSInterface timeLock = new TimeLockedTTSInterface(
                    ttsInterface);

            this.ttsInterfaces.add(timeLock);
        }
    }

    /**
     * Returns a connection that can be used to interact with the tts server if
     * one is available. Otherwise, NULL
     * 
     * @return a connection to the tts server or NULL when a connection is not
     *         available.
     */
    public TimeLockedTTSInterface requestConnection() {
        synchronized (this.ttsInterfaces) {
            for (TimeLockedTTSInterface timeLock : this.ttsInterfaces) {
                if (timeLock.isUnlocked() == false) {
                    continue;
                }

                timeLock.inUse();
                return timeLock;
            }
        }

        return null;
    }

    /**
     * Returns a connection that is no longer needed
     * 
     * @param timeLock
     *            the connection that is no longer needed
     * @param byteCount
     *            the number of bytes that were synthesized
     */
    public void returnConnection(TimeLockedTTSInterface timeLock,
            final int byteCount) {
        synchronized (this.ttsInterfaces) {
            if (byteCount > 0) {
                timeLock.lockForDuration(byteCount);
            }
        }
    }
}