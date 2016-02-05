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
package com.raytheon.bmh.dactransmit.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@code ThreadFactory} implementation that uses a specified string as the
 * base name for all threads created by it. An incremented number will be tacked
 * onto the end to ensure unique names are given to all threads.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class NamedThreadFactory implements ThreadFactory {

    private final String name;

    private final AtomicInteger threadNumber;

    /**
     * Creates a new {@code NamedThreadFactory} using the specified string as
     * the base name for any threads created by this {@code ThreadFactory}.
     * 
     * @param name
     *            String to use as base name for any threads created. Threads
     *            created by this class will use the name format
     *            "NAME-thread-THREADNUMBER".
     */
    public NamedThreadFactory(String name) {
        this.name = name;
        this.threadNumber = new AtomicInteger(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        String threadName = name + "-thread-" + threadNumber.incrementAndGet();
        return new Thread(r, threadName);
    }
}
