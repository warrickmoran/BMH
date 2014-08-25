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

import java.util.concurrent.Callable;

import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Used to lock a synthesizer for the duration of the audio that it was used to
 * synthesize.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014 3538       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSLockResourceTask implements Callable<UnlockNotification> {
    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TTSLockResourceTask.class);

    /*
     * The identifier of the resource that will be locked.
     */
    private final String resourceIdentifier;

    /*
     * The length of time that the resource must be locked.
     */
    private final long duration;

    /**
     * Constructor
     * 
     * @param resourceIdentifier
     *            the id of the synthesizer that will be locked
     * @param duration
     *            the length of time that the synthesizer will be unavailable
     *            (in milliseconds)
     */
    public TTSLockResourceTask(final String resourceIdentifier,
            final long duration) {
        this.resourceIdentifier = resourceIdentifier;
        this.duration = duration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public UnlockNotification call() throws Exception {
        StringBuilder message = new StringBuilder("Locking TTS Synthesizer ");
        message.append(this.resourceIdentifier);
        message.append(" for ");
        message.append(this.duration);
        message.append(" ms.");
        statusHandler.info(message.toString());

        Thread.sleep(this.duration);
        return new UnlockNotification(this.resourceIdentifier);
    }
}