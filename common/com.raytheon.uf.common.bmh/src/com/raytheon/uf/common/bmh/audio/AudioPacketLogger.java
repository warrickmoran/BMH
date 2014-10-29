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
package com.raytheon.uf.common.bmh.audio;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Object for logging the performance of BMH components which are processing
 * audio packets. Each time a packet is processed {@link #packetProcessed()}
 * should be called. A summuray of performance will be displayed at the
 * specified intervals and when {@link #close()} is called.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 29, 2014  3774     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class AudioPacketLogger implements AutoCloseable {

    private final DecimalFormat avarageFormat = new DecimalFormat("#.##");

    private final CharSequence label;

    private final Logger logger;

    private final long incrementalLoggingDuration;

    private AudioPacketStats totalStats;

    private AudioPacketStats incrementalStats;

    /**
     * Create a new Logger without a previously available {@link Logger}
     * 
     * @param label
     *            text displayed at the beginning of all log statements.
     * @param classForLogger
     *            class used to retrieve a {@link Logger}
     * @param incrementalLoggingDuration
     *            Time interval(in seconds) between incremental logging.
     */
    public AudioPacketLogger(CharSequence label, Class<?> classForLogger,
            long incrementalLoggingDuration) {
        this.label = label;
        this.logger = LoggerFactory.getLogger(classForLogger);
        this.incrementalLoggingDuration = incrementalLoggingDuration
                * TimeUtil.MILLIS_PER_SECOND;
    }

    /**
     * Create a new Logger with the specified {@link Logger}
     * 
     * @param label
     *            text displayed at the beginning of all log statements.
     * @param logger
     *            logger to use for output.
     * @param incrementalLoggingDuration
     *            Time interval(in seconds) between incremental logging.
     */
    public AudioPacketLogger(CharSequence label, Logger logger,
            long incrementalLoggingDuration) {
        this.label = label;
        this.logger = logger;
        this.incrementalLoggingDuration = incrementalLoggingDuration
                * TimeUtil.MILLIS_PER_SECOND;
    }

    /**
     * This method should be called each time a packet is processed so the
     * logger can track statistics.
     */
    public void packetProcessed() {
        long currentTime = System.currentTimeMillis();
        if (totalStats == null) {
            totalStats = new AudioPacketStats(currentTime);
        } else {
            totalStats.packetReceived(currentTime);
        }

        if (incrementalStats == null) {
            incrementalStats = new AudioPacketStats(currentTime);
        } else {
            incrementalStats.packetReceived(currentTime);
        }
        if (incrementalStats.getDuration() >= incrementalLoggingDuration) {
            log(incrementalStats);
            incrementalStats = new AudioPacketStats(currentTime);
        }

    }

    /**
     * This must be called when packets are no longer processed to print total
     * statistics.
     */
    @Override
    public void close() {
        if (totalStats != null) {
            if (incrementalStats != null
                    && incrementalStats.getDuration() < totalStats
                            .getDuration()) {
                log(incrementalStats);
            }
            log(totalStats);
        } else {
            logger.info("{} has been closed without processing any packets",
                    label);
        }

    }

    private void log(AudioPacketStats stats) {
        long count = stats.getCount();
        long duration = stats.getDuration();
        if (count == 1) {
            /* Only one packet, no meaningful interval */
            logger.info("{} has processed 1 packets in the last {}ms", label,
                    count, duration);
        } else if (duration < 5 * TimeUtil.MILLIS_PER_SECOND) {
            /* Less than 5 seconds, just log millis */
            logger.info(
                    "{} has processed {} packets in the last {}ms with an average packet interval of {}ms and a maximum packet interval of {}ms",
                    label, count, duration,
                    avarageFormat.format(stats.getAverageInterval()),
                    stats.getMaxInterval());
        } else {
            /* Include human readable time frame with exact millis. */
            String unit;
            long value;
            if (duration < 5 * TimeUtil.MILLIS_PER_MINUTE) {
                unit = "seconds";
                value = duration / TimeUtil.MILLIS_PER_SECOND;
            } else if (duration < 5 * TimeUtil.MILLIS_PER_HOUR) {
                unit = "minutes";
                value = duration / TimeUtil.MILLIS_PER_MINUTE;
            } else if (duration < 5 * TimeUtil.MILLIS_PER_DAY) {
                unit = "hours";
                value = duration / TimeUtil.MILLIS_PER_HOUR;
            } else {
                unit = "days";
                value = duration / TimeUtil.MILLIS_PER_DAY;
            }

            logger.info(
                    "{} has processed {} packets in the last {} {}({}ms) with an average packet interval of {}ms and a maximum packet interval of {}ms",
                    label, count, value, unit, duration,
                    avarageFormat.format(stats.getAverageInterval()),
                    stats.getMaxInterval());
        }
    }

    private static final class AudioPacketStats {

        private final long firstPacketTime;

        private long count;

        private long lastPacketTime = 0;

        private long maxInterval = 0;

        public AudioPacketStats(long firstPacketTime) {
            this.firstPacketTime = firstPacketTime;
            this.lastPacketTime = firstPacketTime;
            this.count = 1;
        }

        public void packetReceived(long currentTime) {
            long interval = currentTime - lastPacketTime;
            if (interval > maxInterval) {
                maxInterval = interval;
            }
            lastPacketTime = currentTime;
            count += 1;
        }

        public long getCount() {
            return count;
        }

        public long getDuration() {
            return lastPacketTime - firstPacketTime;
        }

        public long getMaxInterval() {
            return maxInterval;
        }

        public double getAverageInterval() {
            return getDuration() / (double) (count - 1);
        }

    }

}
