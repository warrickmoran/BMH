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

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.MessageActivity.MESSAGE_ACTIVITY;

/**
 * Wrapper around a {@link DefaultMessageLogger} that can be initialized via
 * Spring and injected into the necessary classes. Provides access to both the
 * practice logging capability and the operational logging capability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 8, 2014  3651       bkowal      Initial creation
 * Dec 11, 2014 3651       bkowal      Implemented additional {@link IMessageLogger} methods.
 * Dec 15, 2014 3651       bkowal      Implemented message error logging.
 * Jan 05, 2015 3651       bkowal      Implemented additional {@link IMessageLogger} error
 *                                     logging methods for playlists.
 * Jan 06, 2015 3651       bkowal      Implemented {@link #logDaoError(BMH_ACTIVITY, Object, Throwable)}.
 * May 13, 2015 4429       rferrel     Changes for traceId.
 * May 21, 2015 4429       rjpeter     Added additional logging methods.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EdexMessageLogger implements IMessageLogger {

    private static final boolean DEFAULT_OPERATIONAL = true;

    private final DefaultMessageLogger messageLogger;

    /**
     * Constructor
     */
    public EdexMessageLogger() {
        this(DEFAULT_OPERATIONAL);
    }

    /**
     * Constructor
     * 
     * @param operational
     *            boolean indicating whether or not this instance of the
     *            {@link EdexMessageLogger} should use the operational
     *            activity/error loggers
     */
    public EdexMessageLogger(final boolean operational) {
        this.messageLogger = operational ? DefaultMessageLogger.getInstance()
                : DefaultEdexPracticeMessageLogger.getInstance();
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
        this.getMessageLogger().logActivationActivity(traceable, msg);
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
        this.getMessageLogger().logReplacementActivity(traceable, newMsg,
                replacedMsg);
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
        this.getMessageLogger().logPlaylistMessageActivity(traceable, msg, tg);
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
        this.getMessageLogger().logPlaylistActivity(traceable, playlist);
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
        this.getMessageLogger().logTriggerActivity(traceable, msg, playlist);
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
        this.getMessageLogger().logBroadcastActivity(traceable, msg);
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
        this.getMessageLogger().logTonesActivity(traceable, toneType, msg);
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
        this.getMessageLogger().logError(traceable, component, activity, msg);
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
        this.getMessageLogger()
                .logError(traceable, component, activity, msg, e);
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
        this.getMessageLogger().logError(traceable, component, activity, msg);
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
        this.getMessageLogger()
                .logError(traceable, component, activity, msg, e);
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
        this.getMessageLogger().logError(traceable, component, activity, msg);
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
        this.getMessageLogger()
                .logError(traceable, component, activity, msg, e);
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
        this.getMessageLogger().logError(traceable, component, activity, msg);
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
        this.getMessageLogger()
                .logError(traceable, component, activity, msg, e);
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
        this.getMessageLogger().logError(traceable, component, activity, msg);
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
        this.getMessageLogger()
                .logError(traceable, component, activity, msg, e);
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
        this.getMessageLogger().logError(traceable, component, activity,
                playlist);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError(com.raytheon
     * .uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT,
     * com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY,
     * com.raytheon.uf.common.bmh.datamodel.playlist.Playlist,
     * java.lang.Exception)
     */
    @Override
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, Playlist playlist, Throwable e) {
        this.getMessageLogger().logError(traceable, component, activity,
                playlist, e);
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
        this.getMessageLogger().logError(traceable, component, activity,
                playlist);
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
        this.getMessageLogger().logError(traceable, component, activity,
                playlist, e);
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
        this.getMessageLogger().logDaoError(traceable, activity, object, e);
    }

    @Override
    public void logMessageActivity(ITraceable traceable,
            MESSAGE_ACTIVITY activity, Object msg) {
        this.getMessageLogger().logMessageActivity(traceable, activity, msg);
    }

    @Override
    public void logInfo(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg, String details) {
        this.getMessageLogger().logInfo(traceable, component, activity, msg,
                details);
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
        this.getMessageLogger().logValidationActivity(msg);
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
        this.getMessageLogger().logParseActivity(msg);
    }

    @Override
    public void logTTSSucces(ITraceable traceable, BroadcastFragment msg,
            int playbackTimeSeconds) {
        this.getMessageLogger().logTTSSucces(traceable, msg,
                playbackTimeSeconds);
    }

    /**
     * Returns a {@link DefaultMessageLogger} associated with the configured
     * mode.
     * 
     * @return a {@link DefaultMessageLogger} associated with the configured
     *         mode
     */
    private DefaultMessageLogger getMessageLogger() {
        return messageLogger;
    }

}