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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@code ExecutorService} that executes tasks based on priority ordering.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class PriorityBasedExecutorService extends ThreadPoolExecutor {

    // TODO: we may need additional priorities.
    public static final int PRIORITY_LOW = 2;

    public static final int PRIORITY_NORMAL = 40;

    public static final int PRIORITY_HIGH = 1000;

    private static class PrioritizableFuture<T> implements RunnableFuture<T> {

        public static final Comparator<Runnable> PRIORITY_COMP = new Comparator<Runnable>() {

            @Override
            public int compare(Runnable o1, Runnable o2) {
                if ((o1 == null) && (o2 == null)) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    int p1 = ((PrioritizableFuture<?>) o1).getPriority();
                    int p2 = ((PrioritizableFuture<?>) o2).getPriority();
                    return Integer.compare(p1, p2);
                }
            }
        };

        private final RunnableFuture<T> task;

        private final int priority;

        public PrioritizableFuture(RunnableFuture<T> task, int priority) {
            this.task = task;
            this.priority = priority;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return task.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public boolean isDone() {
            return task.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return task.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return task.get(timeout, unit);
        }

        @Override
        public void run() {
            task.run();
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * Create an instance that uses the following number of threads.
     * 
     * @param nThreads
     *            Number of threads for the thread pool.
     */
    public PriorityBasedExecutorService(int nThreads) {
        this(nThreads, Executors.defaultThreadFactory());
    }

    /**
     * Create an instance that uses the following number of threads and the
     * specified {@code ThreadFactory}.
     * 
     * @param nThreads
     *            Number of threads for the thread pool.
     * @param threadFactory
     *            {@code ThreadFactory} instance to use to create new threads.
     */
    public PriorityBasedExecutorService(int nThreads,
            ThreadFactory threadFactory) {
        super(nThreads, nThreads, 0L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>(10,
                        PrioritizableFuture.PRIORITY_COMP), threadFactory);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        RunnableFuture<T> newTask = super.newTaskFor(callable);

        int priority;
        if (callable instanceof PrioritizableCallable<?>) {
            priority = ((PrioritizableCallable<T>) callable).getPriority();
        } else {
            priority = PRIORITY_NORMAL;
        }

        return new PrioritizableFuture<>(newTask, priority);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        RunnableFuture<T> newTask = super.newTaskFor(runnable, value);
        return new PrioritizableFuture<>(newTask, PRIORITY_NORMAL);
    }
}
