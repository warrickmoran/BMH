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
package com.raytheon.bmh.comms;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.DacVoiceStatus;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;

/**
 * 
 * Keeps track of silence and logs errors.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 26, 2014  3821     bsteffen    Initial Implementation
 * Jan 08, 2015  3821     bsteffen    Rename silenceAlarm to deadAirAlarm
 * Feb 26, 2015  4187     rjpeter     Make thread daemon so jvm can stop.
 * Mar 11, 2015  4186     bsteffen    Notify comms manager of silence changes.
 * Apr 22, 2015  4404     bkowal      Added {@link #handleDacDisconnect(String)} and
 *                                    {@link #clearSilenceAlarm(String)}.
 * Jun 01, 2015  4490     bkowal      Extend {@link AbstractJmsAlarm}.
 * Jun 12, 2015  4482     rjpeter     Keep Silence thread alive.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SilenceAlarm extends AbstractJmsAlarm {

    private static final int ALARM_AFTER_MS = Integer.getInteger(
            "SilenceAlarmInitialSeconds", 10) * 1000;

    private static final int ALARM_REPEAT_MS = Integer.getInteger(
            "SilenceAlarmRepeatSeconds", 60) * 1000;

    private final CommsManager commsManager;

    private final Map<String, SilenceTime> silenceTimes = new HashMap<>();

    private Set<String> alarmableGroups;

    private final SilenceAlarmThread thread;

    /**
     * Construct a new silence alarm that will notify based off the current
     * configuration.
     * 
     * @param config
     *            the current configuration for the comms manager.
     */
    public SilenceAlarm(CommsManager commsManager) {
        super(BMH_CATEGORY.DAC_TRANSMIT_SILENCE);
        this.commsManager = commsManager;
        reconfigure(commsManager.getCurrentConfigState());
        thread = new SilenceAlarmThread();
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Update configuration information.
     * 
     * @param config
     *            the current configuration for the comms manager.
     */
    public void reconfigure(CommsConfig config) {
        Set<DacConfig> dacs = config.getDacs();
        if (dacs == null) {
            this.alarmableGroups = Collections.emptySet();
        } else {
            Set<String> alarmableGroups = new HashSet<>();
            for (DacConfig dac : config.getDacs()) {
                for (DacChannelConfig channel : dac.getChannels()) {
                    if (channel.isDeadAirAlarm()) {
                        alarmableGroups.add(channel.getTransmitterGroup());
                    }
                }
            }
            this.alarmableGroups = alarmableGroups;
        }
        synchronized (silenceTimes) {
            silenceTimes.keySet().retainAll(alarmableGroups);
        }
    }

    /**
     * Check a new {@link DacHardwareStatusNotification} for silence or not and
     * alarm accordingly.
     * 
     * @param status
     *            the currrent hardware status of a dac.
     */
    public void handleDacHardwareStatus(DacHardwareStatusNotification status) {
        String group = status.getTransmitterGroup();
        if (!alarmableGroups.contains(group)) {
            return;
        }

        SilenceTime canceledAlarm = null;
        synchronized (silenceTimes) {
            /* status notification only contains info about the specific group */
            for (DacVoiceStatus voiceStatus : status.getVoiceStatus()) {
                if (voiceStatus != DacVoiceStatus.IP_AUDIO) {
                    this.initiateSilenceThread(group);
                    return;
                }
            }
            canceledAlarm = silenceTimes.remove(group);
        }

        if (canceledAlarm != null && canceledAlarm.isAlarming()) {
            commsManager.silenceStatusChanged();
        }
    }

    /**
     * Triggers a silence alarm in response to the loss of a connection to the
     * dac for the specified Transmitter Group.
     * 
     * @param group
     *            the specified Transmitter Group.
     */
    public void handleDacDisconnect(final String group) {
        synchronized (this.silenceTimes) {
            this.initiateSilenceThread(group);
        }
    }

    /**
     * Instantiates and starts a {@link SilenceAlarmThread} for the specified
     * Transmitter Group if one did not already exist.
     * 
     * @param group
     *            the specified Transmitter Group.
     */
    private void initiateSilenceThread(final String group) {
        if (!silenceTimes.containsKey(group)) {
            silenceTimes.put(group, new SilenceTime(logger, group));
            thread.interrupt();
        }
    }

    /**
     * Removes any existing Silence Alarm(s) for the specified Transmitter
     * Group.
     * 
     * @param group
     *            the specified Transmitter Group.
     */
    public void clearSilenceAlarm(final String group) {
        SilenceTime canceledAlarm = null;
        synchronized (this.silenceTimes) {
            canceledAlarm = silenceTimes.remove(group);
        }
        if (canceledAlarm != null && canceledAlarm.isAlarming()) {
            commsManager.silenceStatusChanged();
        }
    }

    public Set<String> getAlarmingGroups() {
        Set<String> result = new HashSet<>(alarmableGroups.size());
        synchronized (silenceTimes) {
            for (SilenceTime time : silenceTimes.values()) {
                if (time.isAlarming()) {
                    result.add(time.transmitterGroup);
                }
            }
        }
        return result;
    }

    private class SilenceAlarmThread extends Thread {

        public SilenceAlarmThread() {
            super("SilenceAlarmThread");
        }

        @Override
        public void run() {
            while (true) {
                boolean changed = false;
                long sleepTime = Long.MAX_VALUE;
                synchronized (silenceTimes) {
                    if (!silenceTimes.isEmpty()) {
                        for (SilenceTime time : silenceTimes.values()) {
                            boolean hasAlarmed = time.isAlarming();
                            sleepTime = Math.min(sleepTime, time.alarm());
                            changed |= hasAlarmed != time.isAlarming();
                        }
                    }
                }
                if (changed) {
                    commsManager.silenceStatusChanged();
                }
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    /* Just checks all the alarms again. */
                }
            }
        }

    }

    private static class SilenceTime {

        private final Logger logger;

        public final String transmitterGroup;

        public final long startSilence;

        private long lastAlarm;

        public SilenceTime(Logger logger, String transmitterGroup) {
            this.logger = logger;
            this.transmitterGroup = transmitterGroup;
            this.startSilence = System.currentTimeMillis();
        }

        /**
         * Alarm(log an error) if enough time has passed.
         * 
         * @return The amount of time before the next alarm should be sent.
         */
        public long alarm() {
            long currentTime = System.currentTimeMillis();
            long alarmAt;
            if (lastAlarm == 0) {
                alarmAt = startSilence + ALARM_AFTER_MS;
            } else {
                alarmAt = lastAlarm + ALARM_REPEAT_MS;
            }
            if (currentTime >= alarmAt) {
                lastAlarm = currentTime;
                this.logger.error("{} has been silent for  {} seconds.",
                        transmitterGroup, (currentTime - startSilence) / 1000);
                return ALARM_REPEAT_MS;
            } else {
                return alarmAt - currentTime;
            }

        }

        public boolean isAlarming() {
            return lastAlarm != 0;
        }

    }
}
