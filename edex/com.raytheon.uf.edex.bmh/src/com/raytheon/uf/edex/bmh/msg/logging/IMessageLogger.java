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
 * Definition of a BMH Message Logger.
 * 
 * It would be counter-intuitive to generalize the message activity logging at
 * this level so individual methods have been created for each activity of
 * interest.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 8, 2014  3651       bkowal      Initial creation
 * Dec 11, 2014 3651       bkowal      Defined methods for message activity logging
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IMessageLogger {
    /**
     * Enum corresponding to the type of tone that has been broadcast.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * 
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * Dec 11, 2014 3651       bkowal      Initial creation
     * 
     * </pre>
     * 
     * @author bkowal
     * @version 1.0
     */
    public static enum TONE_TYPE {
        SAME, ALERT, END
    }

    /**
     * Logs that the specified {@link BroadcastMsg} has been activated.
     * 
     * @param msg
     *            the specified {@link BroadcastMsg}
     */
    public void logActivationActivity(BroadcastMsg msg);

    /**
     * Logs that the specified {@link DacPlaylistMessage} has just been created
     * for broadcast to the specified {@link TransmitterGroup}.
     * 
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     * @param tg
     *            the specified {@link TransmitterGroup}
     */
    public void logCreationActivity(DacPlaylistMessage msg, TransmitterGroup tg);

    /**
     * Logs that the specified newMsg {@link BroadcastMsg} has replaced the
     * specified replacedMsg {@link BroadcastMsg}.
     * 
     * @param newMsg
     *            the {@link BroadcastMsg} that will take the place of another
     *            {@link BroadcastMsg}.
     * @param replacedMsg
     *            the {@link BroadcastMsg} that has been replaced.
     */
    public void logReplacementActivity(BroadcastMsg newMsg,
            BroadcastMsg replacedMsg);

    /**
     * Logs that the specified {@link DacPlaylist} has been created or updated.
     * 
     * @param playlist
     *            the specified {@link DacPlaylist}
     */
    public void logPlaylistActivity(DacPlaylist playlist);

    /**
     * Logs that the specified {@link BroadcastMsg} will trigger an automatic
     * switch to the specified {@link DacPlaylist}.
     * 
     * @param msg
     *            the specified {@link BroadcastMsg}
     * @param playlist
     *            the specified {@link DacPlaylist}
     */
    public void logTriggerActivity(BroadcastMsg msg, DacPlaylist playlist);

    /**
     * Logs that the specified {@link DacPlaylistMessage} has just been
     * broadcast.
     * 
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     */
    public void logBroadcastActivity(DacPlaylistMessage msg);

    /**
     * Logs that the specified {@link TONE_TYPE} has just been broadcast for the
     * specified {@link DacPlaylistMessage}.
     * 
     * @param toneType
     *            the specified {@link TONE_TYPE}
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     */
    public void logTonesActivity(TONE_TYPE toneType, DacPlaylistMessage msg);

    // TODO: Implement error logging
    public void logError();
}