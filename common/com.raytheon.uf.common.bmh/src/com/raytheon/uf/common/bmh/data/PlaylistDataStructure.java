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
package com.raytheon.uf.common.bmh.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Playlist Data Object by Transmitter Group
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 22, 2014     3432   mpduff      Initial creation
 * Aug 24, 2014     3432   mpduff      Added Copy constructor
 * Oct 21, 2014     3655   bkowal      Updated to use {@link IPlaylistData}.
 * Nov 04, 2014     3778   bsteffen    Preserve the order of the prediction map.
 * Nov 30, 2014     3752   mpduff      Added Suite name and playlist cycle duration time.
 * Nov 30, 2014     3752   mpduff      Added Suite name and playlist cycle duration time to copy constructor.
 * Jan 13, 2015     3843   bsteffen    Add periodic predictions
 * Jan 13, 2015     3844   bsteffen    Change included message to be PlaylistMessage
 * Mar 25, 2015     4290   bsteffen    Switch to global replacement.
 * Jan 28, 2016     5300   rjpeter     Updated to use copy construction instead of in place update.
 * Jan 29, 2016     5300   bkowal      Compare update times when determining if a message has already
 *                                     been loaded to ensure the latest version is stored locally.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistDataStructure implements IPlaylistData {
    /**
     * DacPlaylistMessageId -> BroadcastMsg
     */
    @DynamicSerializeElement
    private Map<Long, BroadcastMsg> playlistMap;

    /**
     * Broadcast Message ID -> MessagePlaybackPrediction
     */
    @DynamicSerializeElement
    private volatile LinkedHashMap<Long, MessagePlaybackPrediction> predictionMap;

    /**
     * Broadcast Message ID -> MessagePlaybackPrediction Contains predicitions
     * for periodic messages which are not in the current cycle.
     */
    @DynamicSerializeElement
    private LinkedHashMap<Long, MessagePlaybackPrediction> periodicPredictionMap;

    /**
     * Broadcast Message ID -> MessageType
     */
    @DynamicSerializeElement
    private Map<Long, MessageType> messageTypeMap;

    /**
     * Suite name
     */
    @DynamicSerializeElement
    private String suiteName;

    /**
     * Time for the entire cycle to play
     */
    @DynamicSerializeElement
    private long playbackCycleTime;

    @DynamicSerializeElement
    private long timeStamp;

    public PlaylistDataStructure() {
    }

    public PlaylistDataStructure(List<MessagePlaybackPrediction> messages,
            List<MessagePlaybackPrediction> periodicMessages, long timeStamp) {
        this(messages, periodicMessages, null, timeStamp);
    }

    public PlaylistDataStructure(List<MessagePlaybackPrediction> messages,
            List<MessagePlaybackPrediction> periodicMessages,
            PlaylistDataStructure that, long timeStamp) {
        this.predictionMap = new LinkedHashMap<>(messages.size());
        this.periodicPredictionMap = new LinkedHashMap<>(
                periodicMessages.size());
        this.playlistMap = new HashMap<>();
        this.messageTypeMap = new HashMap<>();
        this.timeStamp = timeStamp;

        for (MessagePlaybackPrediction pred : messages) {
            predictionMap.put(pred.getBroadcastId(), pred);
        }

        for (MessagePlaybackPrediction pred : periodicMessages) {
            periodicPredictionMap.put(pred.getBroadcastId(), pred);
        }

        if (that != null) {
            /* Copy over the referenced broadcast msgs and msg types */
            Map<Long, BroadcastMsg> thatPlaylistMap = that.getPlaylistMap();
            Map<Long, MessageType> thatMsgTypeMap = that.getMessageTypeMap();

            for (MessagePlaybackPrediction pred : messages) {
                Long broadcastId = pred.getBroadcastId();
                BroadcastMsg bMsg = thatPlaylistMap.get(broadcastId);
                if (bMsg != null) {
                    playlistMap.put(broadcastId, bMsg);
                }
                MessageType msgType = thatMsgTypeMap.get(broadcastId);
                if (msgType != null) {
                    messageTypeMap.put(broadcastId, msgType);
                }
            }

            for (MessagePlaybackPrediction pred : periodicMessages) {
                Long broadcastId = pred.getBroadcastId();
                BroadcastMsg bMsg = thatPlaylistMap.get(broadcastId);
                if (bMsg != null) {
                    playlistMap.put(broadcastId, bMsg);
                }
                MessageType msgType = thatMsgTypeMap.get(broadcastId);
                if (msgType != null) {
                    messageTypeMap.put(broadcastId, msgType);
                }
            }
        }

    }

    public PlaylistDataStructure(PlaylistDataStructure that) {
        this.playlistMap = new HashMap<>(that.getPlaylistMap());
        this.predictionMap = new LinkedHashMap<>(that.getPredictionMap());
        this.periodicPredictionMap = new LinkedHashMap<>(
                that.getPeriodicPredictionMap());
        this.messageTypeMap = new HashMap<>(that.getMessageTypeMap());
        this.suiteName = that.getSuiteName();
        this.playbackCycleTime = that.getPlaybackCycleTime();
    }

    public Map<Long, BroadcastMsg> getPlaylistMap() {
        if (playlistMap == null) {
            playlistMap = new HashMap<>();
        }

        return playlistMap;
    }

    public void setPlaylistMap(Map<Long, BroadcastMsg> playlistMap) {
        this.playlistMap = playlistMap;
    }

    public LinkedHashMap<Long, MessagePlaybackPrediction> getPredictionMap() {
        if (predictionMap == null) {
            predictionMap = new LinkedHashMap<>();
        }

        return predictionMap;
    }

    public void setPredictionMap(
            LinkedHashMap<Long, MessagePlaybackPrediction> predictionMap) {
        this.predictionMap = predictionMap;
    }

    public LinkedHashMap<Long, MessagePlaybackPrediction> getPeriodicPredictionMap() {
        if (periodicPredictionMap == null) {
            periodicPredictionMap = new LinkedHashMap<>();
        }
        return periodicPredictionMap;
    }

    public void setPeriodicPredictionMap(
            LinkedHashMap<Long, MessagePlaybackPrediction> periodicPredictionMap) {
        this.periodicPredictionMap = periodicPredictionMap;
    }

    public Map<Long, MessageType> getMessageTypeMap() {
        if (messageTypeMap == null) {
            messageTypeMap = new HashMap<>();
        }

        return messageTypeMap;
    }

    public void setMessageTypeMap(Map<Long, MessageType> messageTypeMap) {
        this.messageTypeMap = messageTypeMap;
    }

    /**
     * @return the suiteName
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * @param suiteName
     *            the suiteName to set
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * @return the playbackCycleTime
     */
    public long getPlaybackCycleTime() {
        return playbackCycleTime;
    }

    /**
     * @param playbackCycleTime
     *            the playbackCycleTime to set
     */
    public void setPlaybackCycleTime(long playbackCycleTime) {
        this.playbackCycleTime = playbackCycleTime;
    }

    public Set<Long> getMissingBroadcastIds() {
        Set<Long> rval = new HashSet<>();
        rval.addAll(getPredictionMap().keySet());
        rval.addAll(getPeriodicPredictionMap().keySet());
        Iterator<Long> broadcastIdIterator = rval.iterator();
        while (broadcastIdIterator.hasNext()) {
            final Long broadcastId = broadcastIdIterator.next();
            final BroadcastMsg msg = getPlaylistMap().get(broadcastId);
            if (msg == null) {
                /*
                 * message has not previously been retrieved yet.
                 */
                continue;
            }

            /*
             * check predictions
             */
            MessagePlaybackPrediction pred = getPredictionMap()
                    .get(broadcastId);
            if (pred == null) {
                /*
                 * check periodic predictions.
                 */
                pred = getPeriodicPredictionMap().get(broadcastId);
            }
            if (pred == null) {
                // unlikely - message should not even be in the list.
                broadcastIdIterator.remove();
                continue;
            }
            /*
             * ensure that the timestamps match.
             */
            if (msg.getUpdateDate().getTimeInMillis() == pred.getTimestamp()) {
                // the message has not been updated since it was last retrieved.
                broadcastIdIterator.remove();
            }
        }
        return rval;
    }

    public void setBroadcastMsgData(Map<BroadcastMsg, MessageType> broadcastData) {
        for (Map.Entry<BroadcastMsg, MessageType> entry : broadcastData
                .entrySet()) {
            BroadcastMsg bMsg = entry.getKey();
            playlistMap.put(bMsg.getId(), bMsg);
            messageTypeMap.put(bMsg.getId(), entry.getValue());
        }
    }

    public synchronized void updatePlaybackData(
            MessagePlaybackStatusNotification notification) {
        long id = notification.getBroadcastId();
        MessagePlaybackPrediction pred = new MessagePlaybackPrediction(
                notification);
        LinkedHashMap<Long, MessagePlaybackPrediction> newPredictionMap = new LinkedHashMap<>(
                getPredictionMap());
        newPredictionMap.put(id, pred);
        predictionMap = newPredictionMap;
    }

    /**
     * @param timeStamp
     *            the timeStamp to set
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * 
     * @return
     */
    public long getTimeStamp() {
        return timeStamp;
    }
}
