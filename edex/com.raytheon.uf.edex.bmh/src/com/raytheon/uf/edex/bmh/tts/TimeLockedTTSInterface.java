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

import java.util.UUID;

import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * A time-based locked around a connection to the TTS Server.
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

public class TimeLockedTTSInterface {
    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TimeLockedTTSInterface.class);

    /*
     * When a connection is checked out and locked, the timeout will ensure that
     * a connection will not be locked indefinitely even if it is never
     * officially returned to the pool. If the timeout capability remains, we
     * will need to ensure that the timeout takes larger blocks of audio into
     * account.
     */
    private static final int LOCK_TIMEOUT_SECONDS = 1800;

    private final String identifier;

    private final TTSInterface ttsInterface;

    private boolean unlocked;

    private long unlockTime;

    /**
     * Constructor
     * 
     * @param ttsInterface
     *            the tts interface to manage
     */
    public TimeLockedTTSInterface(final TTSInterface ttsInterface) {
        this.identifier = UUID.randomUUID().toString();
        this.ttsInterface = ttsInterface;
        this.unlocked = true;
    }

    /**
     * Returns the identifier
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Locks the interface for use. Will automatically be unlocked when the
     * timeout period ends unless the user of the lock alters the lockout
     * duration via the lockForDuration method.
     */
    public void inUse() {
        this.unlocked = false;
        /*
         * ensure that the lock will timeout if the connection is not returned
         * for some reason.
         */
        this.unlockTime = System.currentTimeMillis()
                + (LOCK_TIMEOUT_SECONDS * 1000);
    }

    /**
     * Needs to be invoked when text synthesis is complete. Will update the
     * duration so that it reflects the time length of the audio that was
     * synthesized.
     * 
     * @param byteCount
     *            the number of bytes returned by the synthesis
     */
    public synchronized void lockForDuration(final int byteCount) {
        if (this.unlocked) {
            /*
             * Indicates that the lock has timed out.
             */
            return;
        }
        /*
         * set the lock time before calculating the duration to take the time
         * required to calculate the duration into account.
         */
        final long lockTime = System.currentTimeMillis();
        /* For ULAW encoded files, 160 bytes = 20 ms of playback time. */
        long playbackTime = byteCount / 160L * 20L;
        statusHandler.info("Locking TTS connection " + this.identifier
                + " for " + playbackTime + " ms.");
        this.unlockTime = lockTime + playbackTime;
    }

    /**
     * Determines if the lockout period has ended.
     */
    private synchronized void checkLock() {
        if (System.currentTimeMillis() >= this.unlockTime
                && this.unlocked == false) {
            statusHandler.info("Unlocking TTS connection " + this.identifier
                    + ".");
            this.unlocked = true;
        }
    }

    /**
     * Returns the managed interface to the TTS Server
     * 
     * @return the managed interface to the TTS Server
     */
    public TTSInterface getInterface() {
        return this.ttsInterface;
    }

    /**
     * Returns a flag indicating whether or not the TTS Interface is currently
     * locked.
     * 
     * @return true when unlocked; false otherwise
     */
    public synchronized boolean isUnlocked() {
        this.checkLock();
        return unlocked;
    }
}