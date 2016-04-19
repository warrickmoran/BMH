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
package com.raytheon.uf.common.bmh.lock;

import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Manages files locked by reference in memory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 09, 2016 5382       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InMemoryFileLockManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final InMemoryFileLockManager INSTANCE = new InMemoryFileLockManager();

    private final Map<Path, ReentrantLock> managedResourceLocks = new WeakHashMap<>();

    private final Object lock = new Object();

    protected InMemoryFileLockManager() {
    }

    public static InMemoryFileLockManager getInstance() {
        return INSTANCE;
    }

    /**
     * Attempts to lock a resource.
     * 
     * @param resource
     *            the resource to lock.
     * @param timeout
     *            how long to wait for the lock to be successful before failing.
     * @return
     */
    public ReentrantLock requestResourceLock(final Path resource,
            final long timeout) {
        final long startTime = System.currentTimeMillis();

        ReentrantLock fileLock = null;
        synchronized (lock) {
            fileLock = managedResourceLocks.get(resource);
            if (fileLock == null) {
                fileLock = new ReentrantLock();
                this.managedResourceLocks.put(resource, fileLock);
            }
        }

        try {
            fileLock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to lock resource: "
                    + resource.toString() + ".", e);
            return null;
        }

        logger.info("Successfully locked resource: {} in {}.",
                resource.toString(),
                TimeUtil.prettyDuration(System.currentTimeMillis() - startTime));

        return fileLock;
    }
}