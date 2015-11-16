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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.LiveBroadcastMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
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
 * Dec 15, 2014 3651       bkowal      Implemented message error logging.
 * Jan 05, 2015 3651       bkowal      Implemented additional {@link IMessageLogger} error
 *                                     logging methods for playlists.
 * Jan 06, 2015  3651      bkowal      Implemented {@link #logDaoError(BMH_ACTIVITY, Object, Throwable)}.
 * Jan 27, 2015  4037      bkowal      Message identifiers are no longer optional.
 * Mar 25, 2015  4290      bsteffen    Switch to global replacement.
 * Apr 24, 2015  4394      bkowal      Added {@link #logMaintenanceTonesActivity(TONE_TYPE, DacMaintenanceMessage)}
 *                                     and {@link #getMsgId(DacMaintenanceMessage)}.
 * May 07, 2015  4466      bkowal      Added {@link #logPlaylistError(BMH_COMPONENT, BMH_ACTIVITY, String, Throwable)}.
 * May 13, 2015  4429      rferrel     Changes for traceId.
 * May 21, 2015  4429      rjpeter     Added additional logging methods.
 * Sep 01, 2015  4825      bkowal      Added methods to log live broadcast activity.
 * Nov 16, 2015  5127      rjpeter     Added logParseHeader.
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

    private final String host;

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

        /*
         * Attempt to determine the host that the logging will be occurring on.
         * If the host cannot be determined, default to using a host of unknown.
         */
        String determinedHost = null;
        try {
            determinedHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            /* failed to determine the host */
            this.errorLogger.error("Failed to determine the hostname.", e);
        }
        host = (determinedHost == null) ? "<Unknown>" : determinedHost;
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
    public void logBroadcastActivity(ITraceable traceable,
            DacPlaylistMessage msg) {
        final String expire = this.getExpirationDate(msg.getExpire());
        Object[] logDetails = new Object[] { this.getMsgId(msg), expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.BROADCAST, logDetails);
    }

    /**
     * Logs that the specified {@link LiveBroadcastMessage} has just been
     * broadcast.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param msg
     *            the specified {@link LiveBroadcastMessage}
     */
    public void logLiveBroadcastActivity(ITraceable traceable,
            LiveBroadcastMessage msg) {
        Object[] logDetails = new Object[] { this.getMsgId(msg), NO_EXPIRATION };
        this.logActivity(traceable, MESSAGE_ACTIVITY.BROADCAST, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logReplacementActivity
     * (com.raytheon.uf.common.bmh.datamodel.msg.InputMessage,
     * com.raytheon.uf.common.bmh.datamodel.msg.InputMessage)
     */
    @Override
    public void logReplacementActivity(ITraceable traceable,
            InputMessage newMsg, InputMessage replacedMsg) {
        final String expire = this
                .getExpirationDate(newMsg.getExpirationTime());
        Object[] logDetails = new Object[] { this.getMsgId(newMsg),
                this.getMsgId(replacedMsg), expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.REPLACEMENT, logDetails);
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
    public void logPlaylistMessageActivity(ITraceable traceable,
            DacPlaylistMessage msg, TransmitterGroup tg) {
        final String expire = this.getExpirationDate(msg.getExpire());
        Object[] logDetails = new Object[] { this.getMsgId(msg), tg.getName(),
                expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.PLAYLIST_MSG, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logActivationActivity
     * (com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg)
     */
    @Override
    public void logActivationActivity(ITraceable traceable, BroadcastMsg msg) {
        final String expire = this.getExpirationDate(msg.getInputMessage()
                .getExpirationTime());
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                msg.getTransmitterGroup().getName(), expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.ACTIVATION, logDetails);
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
    public void logTonesActivity(ITraceable traceable, TONE_TYPE toneType,
            DacPlaylistMessage msg) {
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
        this.logActivity(traceable, activity, logDetails);
    }

    /**
     * Logs that the specified {@link TONE_TYPE} has just been broadcast for the
     * specified {@link DacMaintenanceMessage}. This method is only used by DAC
     * maintenance sessions.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param toneType
     *            the specified {@link TONE_TYPE}
     * @param msg
     *            the specified {@link DacMaintenanceMessage}
     */
    public void logMaintenanceTonesActivity(ITraceable traceable,
            TONE_TYPE toneType, DacMaintenanceMessage msg) {
        MESSAGE_ACTIVITY activity = null;
        Object[] logDetails = null;
        if (toneType == TONE_TYPE.SAME) {
            activity = MESSAGE_ACTIVITY.SAME_TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), msg.getSAMEtone(), NO_EXPIRATION };
        } else {
            activity = MESSAGE_ACTIVITY.TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), NO_EXPIRATION };
        }
        this.logActivity(traceable, activity, logDetails);
    }

    /**
     * Logs that the specified {@link TONE_TYPE} has just been broadcast for the
     * specified {@link LiveBroadcastMessage}. This method is only used by live
     * broadcast sessions.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param toneType
     *            the specified {@link TONE_TYPE}
     * @param msg
     *            the specified {@link LiveBroadcastMessage}
     */
    public void logLiveBroadcastTonesActivity(ITraceable traceable,
            TONE_TYPE toneType, LiveBroadcastMessage msg) {
        MESSAGE_ACTIVITY activity = null;
        Object[] logDetails = null;
        if (toneType == TONE_TYPE.SAME) {
            activity = MESSAGE_ACTIVITY.SAME_TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), msg.getSAMEtone(), NO_EXPIRATION };
        } else {
            activity = MESSAGE_ACTIVITY.TONE;
            logDetails = new Object[] { toneType.toString(),
                    this.getMsgId(msg), NO_EXPIRATION };
        }
        this.logActivity(traceable, activity, logDetails);
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
    public void logTriggerActivity(ITraceable traceable, BroadcastMsg msg,
            DacPlaylist playlist) {
        final String expire = this.getExpirationDate(playlist.getExpired());
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                playlist.toString(), msg.getTransmitterGroup().getName(),
                expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.TRIGGER, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logValidationActivity
     * (com.raytheon.uf.common.bmh.trace.ITraceable,
     * com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage)
     */
    @Override
    public void logValidationActivity(ValidatedMessage msg) {
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                msg.getTransmissionStatus().toString(),
                msg.getLdadStatus().toString() };
        this.logActivity(msg, MESSAGE_ACTIVITY.VALIDATION_END, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logTTSSucces(com.
     * raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment, int)
     */
    @Override
    public void logTTSSucces(ITraceable traceable, BroadcastFragment msg,
            int playbackTimeSeconds) {
        Object[] logDetails = new Object[] { this.getMsgId(msg),
                msg.getOutputName(), playbackTimeSeconds };
        this.logActivity(traceable, MESSAGE_ACTIVITY.TTS_SUCCESS, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logPlaylistActivity
     * (com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
     */
    @Override
    public void logPlaylistActivity(ITraceable traceable, DacPlaylist playlist) {
        final String expire = (playlist.getExpired() == null) ? NO_EXPIRATION
                : playlist.getExpired().getTime().toString();
        Object[] logDetails = new Object[] { playlist.toString(), expire };
        this.logActivity(traceable, MESSAGE_ACTIVITY.PLAYLIST, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.InputMessage)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, InputMessage msg) {
        this.logError(traceable, component, activity, msg, null);
    }

    @Override
    public void logMessageActivity(ITraceable traceable,
            MESSAGE_ACTIVITY activity, Object msg) {
        Object[] logDetails = new Object[] { getMsgId(msg) };
        logActivity(traceable, activity, logDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.InputMessage,
     * java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, InputMessage msg, Throwable e) {
        this.logError(traceable, component, activity, this.getMsgId(msg), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, ValidatedMessage msg) {
        this.logError(traceable, component, activity, msg, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage,
     * java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, ValidatedMessage msg, Throwable e) {
        this.logError(traceable, component, activity, this.getMsgId(msg), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg) {
        this.logError(traceable, component, activity, msg, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg,
     * java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg, Throwable e) {
        this.logError(traceable, component, activity, this.getMsgId(msg), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.edex.bmh.ldad.LdadMsg)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, LdadMsg msg) {
        this.logError(traceable, component, activity, msg, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.edex.bmh.ldad.LdadMsg, java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, LdadMsg msg, Throwable e) {
        this.logError(traceable, component, activity, this.getMsgId(msg), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylistMessage msg) {
        this.logError(traceable, component, activity, msg, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage,
     * java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylistMessage msg, Throwable e) {
        this.logError(traceable, component, activity, this.getMsgId(msg), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.Playlist)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, Playlist playlist) {
        this.logError(traceable, component, activity, playlist, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.Playlist,
     * java.lang.Throwable)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, Playlist playlist, Throwable e) {
        this.logError(traceable, component, activity,
                this.getIdentifier(playlist), e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylist playlist) {
        this.logError(traceable, component, activity, playlist, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist,
     * java.lang.Throwable)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylist playlist, Throwable e) {
        final String identifier = "DacPlaylist [" + playlist.toString() + "]";
        this.logError(traceable, component, activity, identifier, e);
    }

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while attempting to perform the specified
     * {@link BMH_ACTIVITY} using the specified playlist file.
     * 
     * @param component
     *            the specified {@link BMH_COMPONENT} that encountered the
     *            error.
     * @param activity
     *            the specified {@link BMH_ACTIVITY} that was attempted.
     * @param playlistFile
     *            the specific name of the playlist file.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logPlaylistError(BMH_COMPONENT component,
            BMH_ACTIVITY activity, String playlistFile, Throwable e) {
        final String identifier = "Playlist File [" + playlistFile + "]";
        this.logError(null, component, activity, identifier, e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logDaoError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY, java.lang.Object,
     * java.lang.Throwable)
     */
    @Override
    public void logDaoError(ITraceable traceable, BMH_ACTIVITY activity,
            Object object, Throwable e) {
        if (object == null) {
            return;
        }

        /**
         * The specified {@link Object} must be one of the recognized persistent
         * data types that the {@link DefaultMessageLogger} can retrieve
         * identification information for.
         */
        if (object instanceof InputMessage) {
            this.logError(traceable, BMH_COMPONENT.BMH_DAO, activity,
                    (InputMessage) object, e);
        } else if (object instanceof ValidatedMessage) {
            this.logError(traceable, BMH_COMPONENT.BMH_DAO, activity,
                    (ValidatedMessage) object, e);
        } else if (object instanceof BroadcastMsg) {
            this.logError(traceable, BMH_COMPONENT.BMH_DAO, activity,
                    (BroadcastMsg) object, e);
        } else if (object instanceof Playlist) {
            this.logError(traceable, BMH_COMPONENT.BMH_DAO, activity,
                    (Playlist) object, e);
        } else {
            throw new IllegalArgumentException(
                    "The current logging implementation does not support or does not recognize the specified persistent object: "
                            + object.getClass().getName() + "!");
        }
    }

    @Override
    public void logInfo(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg, String details) {
        logInfo(traceable, component, activity, this.getMsgId(msg), details);
    }

    private void logInfo(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, String msgId, String details) {
        String msg = createLogMsg(traceable, component, activity, msgId);
        this.activityLogger.info(msg + ": " + details);
    }

    private String createLogMsg(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, String msgId) {
        StringBuilder msg = new StringBuilder(
                TraceableUtil.createTraceMsgHeader(traceable));
        msg.append(String.format(ErrorActivity.LOG_FORMAT, component, activity,
                msgId, this.host));
        return msg.toString();
    }

    private void logActivity(final ITraceable traceable,
            final MESSAGE_ACTIVITY activityType, final Object[] logDetails) {
        StringBuilder sb = new StringBuilder(
                TraceableUtil.createTraceMsgHeader(traceable));
        sb.append("[")
                .append(activityType.toString())
                .append("] ")
                .append(String.format(activityType.getLogMsgFormat(),
                        logDetails));

        this.activityLogger.info(sb.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logParseActivity(
     * com.raytheon.uf.common.bmh.datamodel.msg.InputMessage)
     */
    @Override
    public void logParseActivity(InputMessage msg) {
        this.activityLogger.info(String.format("Parsing file: %s",
                msg.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logParseHeader(com
     * .raytheon.uf.common.bmh.datamodel.msg.InputMessage, java.lang.String)
     */
    @Override
    public void logParseHeader(InputMessage msg, String header) {
        this.activityLogger.info(String.format(
                "Parsing file %s:, message header: %s", msg.getName(), header));
    }

    private void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, String msgId, Throwable e) {
        String msg = createLogMsg(traceable, component, activity, msgId);
        if (e == null) {
            this.errorLogger.error(msg);
        } else {
            this.errorLogger.error(msg, e);
        }
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

    private String getMsgId(Object msg) {
        if (msg instanceof DacPlaylistMessage) {
            return getMsgId((DacPlaylistMessage) msg);
        } else if (msg instanceof DacMaintenanceMessage) {
            return getMsgId((DacMaintenanceMessage) msg);
        } else if (msg instanceof BroadcastFragment) {
            return getMsgId(msg);
        } else if (msg instanceof BroadcastMsg) {
            return getMsgId((BroadcastMsg) msg);
        } else if (msg instanceof BroadcastMsgGroup) {
            return getMsgId((BroadcastMsgGroup) msg);
        } else if (msg instanceof InputMessage) {
            return getMsgId((InputMessage) msg);
        } else if (msg instanceof ValidatedMessage) {
            return getMsgId((ValidatedMessage) msg);
        } else if (msg instanceof LdadMsg) {
            return getMsgId((LdadMsg) msg);
        } else if (msg instanceof LiveBroadcastMessage) {
            return getMsgId((LiveBroadcastMessage) msg);
        } else {
            if (msg == null) {
                throw new IllegalArgumentException(
                        "Required argument msg can not be NULL.");
            } else {

                return msg.toString();
            }
        }
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

        StringBuilder sb = new StringBuilder("DacPlaylistMessage [id=");
        sb.append(msg.getBroadcastId());
        sb.append(", afosid=");
        sb.append(msg.getMessageType());
        sb.append(", name=");
        sb.append(msg.getName());
        sb.append("]");

        return sb.toString();
    }

    private String getMsgId(DacMaintenanceMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("DacMaintenanceMessage [name=");
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

        StringBuilder sb = new StringBuilder("BroadcastMsg [id=");
        sb.append(msg.getId());
        sb.append(", afosid=");
        sb.append(msg.getAfosid());
        sb.append(", name=");
        sb.append(msg.getInputMessage().getName());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link BroadcastFragment}.
     * 
     * @param msg
     *            the specified {@link BroadcastFragment}
     * @return an identification {@link String}
     */
    private String getMsgId(BroadcastFragment msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("BroadcastFragment [id=");
        sb.append(msg.getId());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link BroadcastMsgGroup}.
     * 
     * @param msg
     *            the specified {@link BroadcastMsg}
     * @return an identification {@link String}
     */
    private String getMsgId(BroadcastMsgGroup msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("BroadcastMsgGroup [");

        if (!CollectionUtil.isNullOrEmpty(msg.getMessages())) {
            BroadcastMsg bMsg = msg.getMessages().get(0);
            sb.append("afosid=");
            sb.append(bMsg.getAfosid());
            sb.append(", name=");
            sb.append(bMsg.getInputMessage().getName());
        }
        sb.append("]");

        return sb.toString();
    }

    private String getMsgId(LiveBroadcastMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("LiveBroadcastMessage [afosid=");
        sb.append(msg.getMessageType());
        sb.append(", user=");
        sb.append(msg.getUser());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link InputMessage}.
     * 
     * @param msg
     *            the specified {@link InputMessage}
     * @return an identification {@link String}
     */
    private String getMsgId(InputMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("InputMessage [id=");
        sb.append(msg.getId());
        sb.append(", afosid=");
        sb.append(msg.getAfosid());
        sb.append(", name=");
        sb.append(msg.getName());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link ValidatedMessage}.
     * 
     * @param msg
     *            the specified {@link ValidatedMessage}
     * @return an identification {@link String}
     */
    private String getMsgId(ValidatedMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("ValidatedMessage [id=");
        sb.append(msg.getId());
        sb.append(", afosid=");
        sb.append(msg.getInputMessage().getAfosid());
        sb.append(", name=");
        sb.append(msg.getInputMessage().getName());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates an identification {@link String} based on the specified
     * {@link LdadMsg}.
     * 
     * @param msg
     *            the specified {@link LdadMsg}
     * @return an identification {@link String}
     */
    private String getMsgId(LdadMsg msg) {
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Required argument msg can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("LdadMsg [id=");
        sb.append(msg.getLdadId());
        sb.append(", afosid=");
        sb.append(msg.getAfosid());
        sb.append("]");

        return sb.toString();
    }

    private String getIdentifier(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException(
                    "Required argument playlist can not be NULL.");
        }

        StringBuilder sb = new StringBuilder("Playlist [id=");
        sb.append(playlist.getId());
        sb.append(", transmitterGroup=");
        sb.append(playlist.getTransmitterGroup().getName());
        sb.append(", suite=");
        sb.append(playlist.getSuite().getName());
        sb.append("]");

        return sb.toString();
    }
}