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

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EdexMessageLogger implements IMessageLogger {

    private static final boolean DEFAULT_OPERATIONAL = true;

    private final boolean operational;

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
        this.operational = operational;
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
        this.getMessageLogger().logActivationActivity(msg);
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
        this.getMessageLogger().logReplacementActivity(newMsg, replacedMsg);
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
        this.getMessageLogger().logCreationActivity(msg, tg);
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
        this.getMessageLogger().logPlaylistActivity(playlist);
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
        this.getMessageLogger().logTriggerActivity(msg, playlist);
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
        this.getMessageLogger().logBroadcastActivity(msg);
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
        this.getMessageLogger().logTonesActivity(toneType, msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger#logError()
     */
    @Override
    public void logError() {
        this.getMessageLogger().logError();
    }

    /**
     * Returns a {@link DefaultMessageLogger} associated with the configured
     * mode.
     * 
     * @return a {@link DefaultMessageLogger} associated with the configured
     *         mode
     */
    private DefaultMessageLogger getMessageLogger() {
        return (this.operational) ? DefaultMessageLogger.getInstance()
                : DefaultEdexPracticeMessageLogger.getInstance();
    }
}