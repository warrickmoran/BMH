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
 * Dec 15, 2014 3651       bkowal      Defined methods for message error logging
 * Jan 05, 2015 3651       bkowal      Defined methods for playlist error logging
 * Jan 06, 2015 3651       bkowal      Defined {@link #logDaoError(BMH_ACTIVITY, Object, Throwable)}.
 * Mar 25, 2015 4290       bsteffen    Switch to global replacement.
 * Apr 24, 2015 4394       bkowal      Added {@link TONE_TYPE#TRANSFER}.
 * May 13, 2015 4429       rferrel     Changes for traceId.
 * May 21, 2015 4429       rjpeter     Added additional logging methods.
 * Nov 16, 2015  5127      rjpeter     Added logParseHeader.
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
        SAME, ALERT, END, TRANSFER
    }

    /**
     * Logs that the specified {@link BroadcastMsg} has been activated.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param msg
     *            the specified {@link BroadcastMsg}
     */
    public void logActivationActivity(ITraceable traceable, BroadcastMsg msg);

    /**
     * Logs that the specified {@link DacPlaylistMessage} has just been created
     * for broadcast to the specified {@link TransmitterGroup}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     * @param tg
     *            the specified {@link TransmitterGroup}
     */
    public void logPlaylistMessageActivity(ITraceable traceable,
            DacPlaylistMessage msg, TransmitterGroup tg);

    /**
     * Logs that the specified newMsg {@link InputMessage} has replaced the
     * specified replacedMsg {@link InputMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param newMsg
     *            the {@link BroadcastMsg} that will take the place of another
     *            {@link InputMessage}.
     * @param replacedMsg
     *            the {@link InputMessage} that has been replaced.
     */
    public void logReplacementActivity(ITraceable traceable,
            InputMessage newMsg, InputMessage replacedMsg);

    /**
     * Logs that the specified {@link DacPlaylist} has been created or updated.
     * 
     * @param playlist
     *            the specified {@link DacPlaylist}
     */
    public void logPlaylistActivity(ITraceable traceable, DacPlaylist playlist);

    /**
     * Logs that the specified {@link BroadcastMsg} will trigger an automatic
     * switch to the specified {@link DacPlaylist}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param msg
     *            the specified {@link BroadcastMsg}
     * @param playlist
     *            the specified {@link DacPlaylist}
     */
    public void logTriggerActivity(ITraceable traceable, BroadcastMsg msg,
            DacPlaylist playlist);

    /**
     * Logs that the specified {@link DacPlaylistMessage} has just been
     * broadcast.
     * 
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     */
    public void logBroadcastActivity(ITraceable traceable,
            DacPlaylistMessage msg);

    /**
     * Log activity on the specified {@link InputMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param activity
     * @param msg
     */
    public void logMessageActivity(ITraceable traceable,
            MessageActivity.MESSAGE_ACTIVITY activity, Object msg);

    /**
     * Logs that the specified {@link ValidatedMessage} has been validated.
     * 
     * @param msg
     *            the specified {@link ValidatedMessage}
     */
    public void logValidationActivity(ValidatedMessage msg);

    /**
     * Logs parse activity
     * 
     * @param msg
     *            the specified {@link InputMessage}
     */
    public void logParseActivity(InputMessage msg);

    /**
     * Logs parse header.
     * 
     * @param msg
     *            the specified {@link InputMessage}
     */
    public void logParseHeader(InputMessage msg, String header);

    /**
     * Logs TTS Success
     * 
     * @param traceable
     * @param msg
     *            the specified {@link InputMessage}
     * @param playbackTimeSeconds
     *            number of seconds to play back fragment
     */
    public void logTTSSucces(ITraceable traceable, BroadcastFragment msg,
            int playbackTimeSeconds);

    /**
     * Logs that the specified {@link TONE_TYPE} has just been broadcast for the
     * specified {@link DacPlaylistMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param toneType
     *            the specified {@link TONE_TYPE}
     * @param msg
     *            the specified {@link DacPlaylistMessage}
     */
    public void logTonesActivity(ITraceable traceable, TONE_TYPE toneType,
            DacPlaylistMessage msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link InputMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link InputMsg} that the component was interacting with
     *            when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, InputMessage msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link InputMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link InputMsg} that the component was interacting with
     *            when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, InputMessage msg, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link ValidatedMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link ValidatedMessage} that the component was
     *            interacting with when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, ValidatedMessage msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link ValidatedMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link ValidatedMessage} that the component was
     *            interacting with when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, ValidatedMessage msg, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link BroadcastMsg}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link BroadcastMsg} that the component was interacting
     *            with when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link BroadcastMsg}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link BroadcastMsg} that the component was interacting
     *            with when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link LdadMsg}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link LdadMsg} that the component was interacting with
     *            when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, LdadMsg msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link LdadMsg}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link LdadMsg} that the component was interacting with
     *            when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, LdadMsg msg, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link DacPlaylistMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link DacPlaylistMessage} that the component was
     *            interacting with when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylistMessage msg);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link DacPlaylistMessage}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param msg
     *            the {@link DacPlaylistMessage} that the component was
     *            interacting with when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylistMessage msg, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link Playlist}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param playlist
     *            the {@link Playlist} that the component was interacting with
     *            when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, Playlist playlist);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link Playlist}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param playlist
     *            the {@link Playlist} that the component was interacting with
     *            when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, Playlist playlist, Throwable e);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link DacPlaylist}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param playlist
     *            the {@link DacPlaylist} that the component was interacting
     *            with when the error was encountered.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylist playlist);

    /**
     * Logs that an error has been encountered by the specified
     * {@link BMH_COMPONENT} while performing the specified {@link BMH_ACTIVITY}
     * using the specified {@link DacPlaylist}.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            The specified BMH Component that encountered the error
     * @param activity
     *            The action that the BMH Component was performing when the
     *            error was encountered.
     * @param playlist
     *            the {@link DacPlaylist} that the component was interacting
     *            with when the error was encountered.
     * @param e
     *            an optional {@link Exception} generated by the system while
     *            performing the specified {@link BMH_ACTIVITY}.
     */
    public void logError(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, DacPlaylist playlist, Throwable e);

    /**
     * Logs any errors that are encountered by a BMH Dao component.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param activity
     *            the {@link BMH_ACTIVITY} that the dao was attempting to
     *            complete when the error was encountered
     * @param object
     *            the persistent data type that the dao was attempting to
     *            persist / update / delete when the error was encountered
     * @param e
     *            the error that was encountered by the dao
     */
    public void logDaoError(ITraceable traceable, BMH_ACTIVITY activity,
            Object object, Throwable e);

    /**
     * Log an information message is the same format as the error log.
     * 
     * @param traceable
     *            the {@link ITraceable} to place at the head of the message.
     * @param component
     *            the {@link BMH_ACTIVITY} that the dao was attempting to
     *            complete when the information was generated.
     * @param activity
     *            the {@link BMH_ACTIVITY} that the dao was attempting to
     *            complete when the information was generated.
     * @param msg
     *            the {@link BroadcastMsg} that the component was interacting
     *            with when the information generated.
     * @param details
     *            Additional details to be added to the log message.
     */
    public void logInfo(ITraceable traceable, BMH_COMPONENT component,
            BMH_ACTIVITY activity, BroadcastMsg msg, String details);
}