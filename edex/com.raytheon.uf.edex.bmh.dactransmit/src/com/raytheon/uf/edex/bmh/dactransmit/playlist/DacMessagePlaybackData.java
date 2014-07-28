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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import java.util.Calendar;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * All the necessary data needed by the {@code DataTransmitThread} to play a
 * file, including the raw audio data itself.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2014  #3286     dgilling     Initial creation
 * Jul 25, 2014  #3286     dgilling     Add additional methods needed for
 *                                      sending playback updates to 
 *                                      CommsManager.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacMessagePlaybackData {

    private DacPlaylistMessage message;

    private AudioFileBuffer audio;

    private boolean interrupt;

    private boolean firstCallToGet = true;

    public MessagePlaybackStatusNotification get(byte[] dst) {
        MessagePlaybackStatusNotification playbackStatus = null;
        if (firstCallToGet) {
            int playCount = message.getPlayCount() + 1;
            message.setPlayCount(playCount);
            Calendar transmitTime = TimeUtil.newGmtCalendar();
            message.setLastTransmitTime(transmitTime);
            playbackStatus = new MessagePlaybackStatusNotification(
                    message.getBroadcastId(), transmitTime, playCount, false,
                    false, null);
            firstCallToGet = false;
        }

        audio.get(dst, 0, dst.length);
        return playbackStatus;
    }

    public boolean hasRemaining() {
        return audio.hasRemaining();
    }

    public DacPlaylistMessage getMessage() {
        return message;
    }

    public AudioFileBuffer getAudio() {
        return audio;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setMessage(DacPlaylistMessage message) {
        this.message = message;
    }

    public void setAudio(AudioFileBuffer audio) {
        this.audio = audio;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
}
