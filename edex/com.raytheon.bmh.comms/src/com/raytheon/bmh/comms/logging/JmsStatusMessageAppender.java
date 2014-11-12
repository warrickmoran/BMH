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

import javax.jms.JMSException;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.jms.JmsCommunicator;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.UFStatus.Priority;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class JmsStatusMessageAppender extends AppenderBase<ILoggingEvent> {

    protected Layout<ILoggingEvent> layout;

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
        sm.setPlugin(event.getLoggerName());
        Marker marker = event.getMarker();
        if (marker != null && marker.contains("Dac Transmit")) {
            sm.setCategory("Dac Transmit");
        } else {
            sm.setCategory("Comms Manager");
        }
        sm.setMessage(event.getFormattedMessage());
        sm.setMachineToCurrent();
        sm.setSourceKey("BMH");
        sm.setDetails(layout.doLayout(event));
        sm.setEventTime(new Date(event.getTimeStamp()));
        JmsCommunicator communicator = JmsStatusMessageAppender.communicator;
        if (communicator != null) {
            try {
                communicator.sendStatusMessage(sm);
            } catch (JMSException | SerializationException e) {
                addError("Unable to append message to jms.", e);
            }
        }
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    private static JmsCommunicator communicator;

    public static void setJmsCommunicator(JmsCommunicator communicator) {
        JmsStatusMessageAppender.communicator = communicator;
    }

}