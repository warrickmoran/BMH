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
package com.raytheon.bmh.comms.logging;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

import com.raytheon.bmh.comms.AbstractJmsAlarm;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.jms.JmsCommunicator;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.notify.MessageDelayedBroadcastNotification;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.status.BMHNotificationAction;
import com.raytheon.uf.edex.bmh.status.BMHNotificationManager;
import com.raytheon.uf.edex.bmh.status.BMH_ACTION;

/**
 * 
 * An Appender which uses the {@link CommsManager} {@link JmsCommunicator} to
 * send mlog messages to alertViz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 03, 2014  3525     bsteffen    Initial Implementation
 * Nov 26, 2014  3821     bsteffen    Utilize the BMHNotificationManager
 * Jan 19, 2015  4002     bkowal      Added support for {@link MessageDelayedBroadcastNotification}.
 * Jun 01, 2015  4490     bkowal      Added {@link #bmhAlarmToCategoryMap}.
 * Dec 21, 2015  5218     rjpeter     Made communicator volatile
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class JmsStatusMessageAppender extends AppenderBase<ILoggingEvent> {

    protected Layout<ILoggingEvent> layout;

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(ILoggingEvent event) {
        StatusMessage sm = new StatusMessage();
        if (event.getLevel() == Level.ERROR) {
            sm.setPriority(Priority.SIGNIFICANT);
        } else if (event.getLevel() == Level.WARN) {
            sm.setPriority(Priority.PROBLEM);
        } else if (event.getLevel() == Level.INFO) {
            sm.setPriority(Priority.EVENTA);
        } else if (event.getLevel() == Level.DEBUG) {
            sm.setPriority(Priority.VERBOSE);
        } else if (event.getLevel() == Level.TRACE) {
            sm.setPriority(Priority.VERBOSE);
        } else {
            sm.setPriority(Priority.EVENTB);
        }
        BMH_CATEGORY bmhCategory = bmhAlarmToCategoryMap.get(event
                .getLoggerName());
        if (bmhCategory == null) {
            Marker marker = event.getMarker();
            if ((marker != null) && marker.contains("Dac Transmit")) {
                bmhCategory = BMH_CATEGORY.DAC_TRANSMIT_ERROR;
            } else {
                bmhCategory = BMH_CATEGORY.COMMS_MANAGER_ERROR;
            }
        }
        sm.setCategory(bmhCategory.getAlertVizCategory());

        BMHNotificationAction notificationAction = BMHNotificationManager
                .getInstance().getAction(bmhCategory, sm.getPriority());
        for (BMH_ACTION action : notificationAction.getActions()) {
            switch (action) {
            case ACTION_ALERTVIZ_AUDIO:
                sm.setAudioFile(notificationAction.getAudioFile());
            case ACTION_ALERTVIZ:
                sm.setPlugin(event.getLoggerName());
                sm.setMessage(event.getFormattedMessage());
                sm.setMachineToCurrent();
                sm.setSourceKey("BMH");
                sm.setDetails(layout.doLayout(event));
                sm.setEventTime(new Date(event.getTimeStamp()));
                JmsCommunicator communicator = JmsStatusMessageAppender.communicator;
                if (communicator != null) {
                    communicator.sendStatusMessage(sm);
                }
            case ACTION_DEFAULT:
            case ACTION_LOG:
                // Logging is handled by other appenders, do not handle here.
            }
        }

    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    private static volatile JmsCommunicator communicator;

    private static final ConcurrentMap<String, BMH_CATEGORY> bmhAlarmToCategoryMap = new ConcurrentHashMap<>();

    public static void setJmsCommunicator(JmsCommunicator communicator) {
        JmsStatusMessageAppender.communicator = communicator;
    }

    public static void registerAlarm(
            Class<? extends AbstractJmsAlarm> alarmClass,
            final BMH_CATEGORY bmhCategory) {
        bmhAlarmToCategoryMap.put(alarmClass.getName(), bmhCategory);
    }
}