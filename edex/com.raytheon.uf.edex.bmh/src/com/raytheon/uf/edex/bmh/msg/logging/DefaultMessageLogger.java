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
package com.raytheon.uf.edex.bmh.msg.logging;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.edex.bmh.msg.logging.MessageActivity.MESSAGE_ACTIVITY;

/**
 * Message logger implementation that allows for logging via the declared
 * {@link DefaultMessageLogger#ACTIVITY_LOGGER} and
 * {@link DefaultMessageLogger#ERROR_LOGGER} loggers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 8, 2014  3651       bkowal      Initial creation
 * Dec 11, 2014 3651       bkowal      Implemented message activity logging.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DefaultMessageLogger implements IMessageLogger {

    private static final String NO_EXPIRATION = "N/A";

    private static final String ACTIVITY_LOGGER = "MessageActivityLogger";

    private static final String ERROR_LOGGER = "MessageErrorLogger";

    private final Logger activityLogger;

    private final Logger errorLogger;

    private static final DefaultMessageLogger instance = new DefaultMessageLogger(
            ACTIVITY_LOGGER, ERROR_LOGGER);

    /**
     * Constructor
     * 
     * @param activityLoggerName
     *            the name of the activity logger as it has been defined in the
     *            logback configuration
     * @param errorLoggerName
     *            the name of the error logger as it has been defined in the
     *            logback configuration
     */
    protected DefaultMessageLogger(final String activityLoggerName,
            final String errorLoggerName) {
        this.activityLogger = LoggerFactory.getLogger(activityLoggerName);

        this.errorLogger = LoggerFactory.getLogger(errorLoggerName);
    }

    public static DefaultMessageLogger getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logBroadcastActivity
     * (com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage)
     */
    @Override
    public void logBroadcastActivity(DacPlaylistMessage msg) {
        final String expire = this.getExpirationDate(msg.getExpire());
        Object[] logDetails = new Object[] { this.getMsgId(msg), expire };
        this.logActivity(MESSAGE_ACTIVITY.BROADCAST, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logReplacementActivity
     * (com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg,
     * com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg)
     */
    @Override
    public void logReplacementActivity(BroadcastMsg newMsg,
            BroadcastMsg replacedMsg) {
        final String expire = this.getExpirationDate(newMsg.getInputMessage()
                .getExpirationTime());
        Object[] logDetails = new Object[] { this.getMsgId(newMsg),
                this.getMsgId(replacedMsg),
                newMsg.getTransmitterGroup().getName(), expire };
        this.logActivity(MESSAGE_ACTIVITY.REPLACEMENT, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logCreationActivity
     * (com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage,
     * com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup)
     */
    @Override
    public void logCreationActivity(DacPlaylistMessage msg, TransmitterGroup tg) {
        final String expire = this.getExpirationDate(msg.getExpire());
        Object[] logDetails = new Object[] { this.getMsgId(msg), tg.getName(),
                expire };
        this.logActivity(MESSAGE_ACTIVITY.CREATION, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logActivationActivity
     * (com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg)
     */
    @Override
    public void logActivationActivity(BroadcastMsg msg) {
        final String expire = this.getExpirationDate(msg.getInputMessage()
                .getExpirationTime());
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                msg.getTransmitterGroup().getName(), expire };
        this.logActivity(MESSAGE_ACTIVITY.ACTIVATION, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logTonesActivity(
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger.TONE_TYPE,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage)
     */
    @Override
    public void logTonesActivity(TONE_TYPE toneType, DacPlaylistMessage msg) {
        final String expire = this.getExpirationDate(msg.getExpire());
        MESSAGE_ACTIVITY activity = null;
        Object[] logDetails = null;
        if (toneType == TONE_TYPE.SAME) {
            activity = MESSAGE_ACTIVITY.SAME_TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), msg.getSAMEtone(), expire };
        } else {
            activity = MESSAGE_ACTIVITY.TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), expire };
        }
        this.logActivity(activity, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logTriggerActivity
     * (com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
     */
    @Override
    public void logTriggerActivity(BroadcastMsg msg, DacPlaylist playlist) {
        final String expire = this.getExpirationDate(playlist.getExpired());
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                playlist.toString(), msg.getTransmitterGroup().getName(),
                expire };
        this.logActivity(MESSAGE_ACTIVITY.TRIGGER, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logPlaylistActivity
     * (com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
     */
    @Override
    public void logPlaylistActivity(DacPlaylist playlist) {
        final String expire = (playlist.getExpired() == null) ? NO_EXPIRATION
                : playlist.getExpired().getTime().toString();
        Object[] logDetails = new Object[] { playlist.toString(), expire };
        this.logActivity(MESSAGE_ACTIVITY.PLAYLIST, logDetails);
    }

    private void logActivity(final MESSAGE_ACTIVITY activityType,
            final Object[] logDetails) {
        StringBuilder sb = new StringBuilder("[");
        sb.append(activityType.toString())
                .append("] ")
                .append(String.format(activityType.getLogMsgFormat(),
                        logDetails));

        this.activityLogger.info(sb.toString());
    }

    /**
     * Determines and returns a formatted expiration date/time {@link String}
     * based on the specified {@link Calendar}. Returns {@link #NO_EXPIRATION}
     * if the specified {@link Calendar} is {@code null}.
     * 
     * @param calendar
     *            the specified {@link Calendar}.
     * @return a formatted date/time {@link String} or {@code null}.
     */
    private String getExpirationDate(final Calendar calendar) {
        if (calendar == null) {
            return NO_EXPIRATION;
        }
        return calendar.getTime().toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link DacPlaylistMessage}.
     * 
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     * @return an identification {@link String}
     */
    private String getMsgId(DacPlaylistMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }
        StringBuilder sb = new StringBuilder("[id=");
        sb.append(msg.getBroadcastId());
        sb.append(", afosid=");
        sb.append(msg.getMessageType());
        sb.append(", name=");
        sb.append(msg.getName());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link BroadcastMsg}.
     * 
     * @param msg
     *            the specified {@link BroadcastMsg}
     * @return an identification {@link String}
     */
    private String getMsgId(BroadcastMsg msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }
        StringBuilder sb = new StringBuilder("[id=");
        sb.append(msg.getId());
        sb.append(", afosid=");
        sb.append(msg.getAfosid());
        sb.append(", name=");
        sb.append(msg.getInputMessage().getName());
        sb.append("]");

        return sb.toString();
    }

    @Override
    public void logError() {
        // TODO: Implement.
        this.errorLogger.error("TEST TEST TEST");
    }
}