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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;

/**
 * Playlist Data Object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014   3432     mpduff      Initial creation
 * Aug 23, 2014   3432     mpduff      Add data
 * Oct 21, 2014   3655     bkowal      Updated to use {@link IPlaylistData}.
 * Nov 01, 2014   3782     mpduff      Implemented MRD column, added message name to table data
 * Nov 03, 2014   3655     bkowal      Fix EO message table display.
 * Nov 04, 2014   3792     lvenable    Put in null check for broadcast message.  It reports the NPE
 *                                     and then puts "Unknown" in for the missing data. This will
 *                                     allow us to track down the issue.
 * Nov 04, 2014   3781     dgilling    Fix SAME and alert tone display.
 * Nov 04, 2014   3778     bsteffen    Allow null transmit time.
 * Nov 17, 2014   3808     bkowal      Support broadcast live.
 * Nov 21, 2014   3845     bkowal      Made {@link PlaylistData#sdf} public so other Viz
 *                                     components could utilize the common playlist date format.
 * Nov 29, 2014   3844     mpduff      Implement interrupt and periodic message coloring
 * Dec 11, 2014   3895     lvenable    Changed SimpleDateFormat to use GMT.
 * Jan 13, 2015   3843     bsteffen    Add ability to populate periodic table.
 * Jan 13, 2015   3844     bsteffen    Include PlaylistMessages in PlaylistDataStructure
 * Feb 05, 2015   4088     bkowal      Handle interrupt playlists that are not saved to the
 *                                     database.
 * Feb 09, 2015   3844     bsteffen    Color interrupts only the first time they play.
 * Feb 10, 2015   4106     bkowal      Support caching live broadcast information.
 * Feb 11, 2015   4088     bkowal      Provide identifying information about the broadcast
 *                                     msg when it is not found.
 * Mar 10, 2015   4252     bkowal      Attempt to retrieve {@link BroadcastMsg}s that are not
 *                                     available in the playlist cache.
 * Mar 25, 2015   4290     bsteffen    Switch to global replacement.
 * Apr 29, 2015   4394     bkowal      Support {@link INonStandardBroadcast}.
 * May 19, 2015   4482     rjpeter     Added check of isDbReset.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistData {
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(PlaylistData.class);

    public static final String PLAYLIST_DATE_FORMAT = "MM/dd/yy HH:mm:ss";

    private final SimpleDateFormat sdf = new SimpleDateFormat(
            PLAYLIST_DATE_FORMAT);

    private final String UNKNOWN_TIME_STR = "--/--/-- --:--:--";

    private final String EMPTY = "";

    /** Color manager */
    private final BroadcastCycleColorManager colorManager;

    /** Map of Transmitter -> PlaylistData for that transmitter */
    private final ConcurrentMap<String, PlaylistDataStructure> playlistDataMap = new ConcurrentHashMap<>();

    /**
     * Map of Transmitter(s) that have an active live broadcast used to override
     * the state of the {@link #playlistDataMap}.
     */
    private final ConcurrentMap<String, INonStandardBroadcast> broadcastOverrideMap = new ConcurrentHashMap<>();

    /** The data manager */
    private final BroadcastCycleDataManager dataManager = new BroadcastCycleDataManager();

    /** Table Columns */
    private final List<TableColumnData> columns;

    /**
     * Constructor
     * 
     * @param columns
     *            Column data objects
     * @param display
     *            The Display
     */
    public PlaylistData(List<TableColumnData> columns, Shell shell) {
        colorManager = new BroadcastCycleColorManager(shell);
        this.columns = columns;

        sdf.setTimeZone(TimeUtil.GMT_TIME_ZONE);
    }

    /**
     * Handle the {@link PlaylistSwitchNotification}
     * 
     * @param notification
     */
    public void handlePlaylistSwitchNotification(
            PlaylistSwitchNotification notification) {
        String tg = notification.getTransmitterGroup();
        List<MessagePlaybackPrediction> messageList = notification
                .getMessages();
        List<MessagePlaybackPrediction> periodicMessageList = notification
                .getPeriodicMessages();

        PlaylistDataStructure playlistData = playlistDataMap.get(tg);
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(tg, playlistData);
        }
        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        predictionMap.clear();

        for (MessagePlaybackPrediction mpp : messageList) {
            predictionMap.put(mpp.getBroadcastId(), mpp);
        }

        Map<Long, MessagePlaybackPrediction> periodicPredictionMap = playlistData
                .getPeriodicPredictionMap();
        periodicPredictionMap.clear();
        if (periodicMessageList != null) {
            for (MessagePlaybackPrediction mpp : periodicMessageList) {
                periodicPredictionMap.put(mpp.getBroadcastId(), mpp);
            }
        }

        Map<Long, BroadcastMsg> playlistMap = playlistData.getPlaylistMap();

        playlistMap.clear();

        Map<Long, MessageType> messageTypeMap = playlistData
                .getMessageTypeMap();

        Playlist playlist;
        try {
            playlist = dataManager.getPlaylist(notification.getSuiteName(),
                    notification.getTransmitterGroup());
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database", e);
            return;
        }
        if (playlist != null) {
            for (BroadcastMsg broadcastMessage : playlist.getMessages()) {
                try {
                    long id = broadcastMessage.getId();
                    MessageType messageType = dataManager
                            .getMessageType(broadcastMessage.getAfosid());
                    playlistMap.put(id, broadcastMessage);
                    messageTypeMap.put(id, messageType);
                } catch (Exception e) {
                    statusHandler.error("Error accessing BMH database", e);
                }
            }
        }

        /*
         * Verify that all playlist messages in the prediction map have been
         * accounted for.
         */
        for (MessagePlaybackPrediction mpp : messageList) {
            final long broadcastId = mpp.getBroadcastId();
            if (playlistMap.containsKey(broadcastId) == false) {
                try {
                    this.forceLoadBroadcast(broadcastId, playlistMap,
                            messageTypeMap);
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to retrieve broadcast message with id: "
                                    + broadcastId
                                    + ". Setting data to unknown.", e);
                }
            }
        }
    }

    private void forceLoadBroadcast(final long broadcastId,
            Map<Long, BroadcastMsg> playlistMap,
            Map<Long, MessageType> messageTypeMap) throws Exception {
        BroadcastMsg broadcastMsg = this.dataManager
                .getBroadcastMessage(broadcastId);
        if (broadcastMsg == null) {
            String msg = "Broadcast Message with id: " + broadcastId
                    + " no longer exists.";
            if (BmhUtils.isDbReset()) {
                statusHandler.info(msg);
            } else {
                statusHandler.warn(msg);
            }
            return;
        }

        MessageType messageType = dataManager.getMessageType(broadcastMsg
                .getAfosid());
        playlistMap.put(broadcastId, broadcastMsg);
        messageTypeMap.put(broadcastId, messageType);
    }

    /**
     * Handle the {@link MessagePlaybackStatusNotification}
     * 
     * @param notification
     */
    public void handlePlaybackStatusNotification(
            MessagePlaybackStatusNotification notification) {
        if (this.broadcastOverrideMap.containsKey(notification
                .getTransmitterGroup())) {
            this.broadcastOverrideMap
                    .remove(notification.getTransmitterGroup());
        }
        PlaylistDataStructure playlistData = playlistDataMap.get(notification
                .getTransmitterGroup());
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(notification.getTransmitterGroup(),
                    playlistData);
        }
        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        long id = notification.getBroadcastId();
        MessagePlaybackPrediction pred = predictionMap.get(id);
        if (pred != null) {
            pred.setPlayCount(notification.getPlayCount());
            pred.setLastTransmitTime(notification.getTransmitTime());
            pred.setNextTransmitTime(null);
            pred.setPlayedAlertTone(notification.isPlayedAlertTone());
            pred.setPlayedSameTone(notification.isPlayedSameTone());
        } else {
            pred = new MessagePlaybackPrediction();
            predictionMap.put(id, pred);
        }
    }

    /**
     * Maintains a client-side cache of {@link LiveBroadcastSwitchNotification}s
     * now that the server is no longer queried every time a different
     * Transmitter is selected.
     * 
     * @param notification
     *            the {@link LiveBroadcastSwitchNotification} to add or remove
     *            from the cache.
     */
    public void handleLiveBroadcastSwitchNotification(
            LiveBroadcastSwitchNotification notification) {
        if (notification.getBroadcastState() == STATE.STARTED) {
            this.broadcastOverrideMap.put(notification.getTransmitterGroup()
                    .getName(), notification);
        } else if (notification.getBroadcastState() == STATE.FINISHED) {
            this.broadcastOverrideMap.remove(notification.getTransmitterGroup()
                    .getName());
        }
    }

    public void handleMaintenanceNotification(
            MaintenanceMessagePlayback notification) {
        this.broadcastOverrideMap.put(notification.getName(), notification);
    }

    public INonStandardBroadcast getNonStandardBroadcast(final String tg) {
        return this.broadcastOverrideMap.get(tg);
    }

    /**
     * Get the updated {@link TableData} object for the provided transmitter
     * group name.
     * 
     * @param transmitterGroupName
     * @return
     */
    public TableData getUpdatedTableData(String transmitterGroupName) {
        if (this.broadcastOverrideMap.containsKey(transmitterGroupName)) {
            return this.getNonStandardTableData(this.broadcastOverrideMap
                    .get(transmitterGroupName));
        }

        PlaylistDataStructure playlistDataStructure = playlistDataMap
                .get(transmitterGroupName);
        if (playlistDataStructure == null) {
            return new TableData(columns);
        }

        Map<Long, MessagePlaybackPrediction> predictionMap = playlistDataStructure
                .getPredictionMap();

        Map<Long, BroadcastMsg> playlistMap = playlistDataStructure
                .getPlaylistMap();
        Map<Long, MessageType> messageTypeMap = playlistDataStructure
                .getMessageTypeMap();

        List<BroadcastCycleTableDataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<Long, MessagePlaybackPrediction> entry : predictionMap
                .entrySet()) {
            Long broadcastId = entry.getKey();
            MessagePlaybackPrediction pred = entry.getValue();

            BroadcastCycleTableDataEntry cycleTableData = new BroadcastCycleTableDataEntry();
            cycleTableData.setAlertSent(pred.isPlayedAlertTone());
            cycleTableData.setPlayCount(pred.getPlayCount());
            cycleTableData.setSameSent(pred.isPlayedSameTone());

            if (pred.getNextTransmitTime() == null) {
                cycleTableData.setTransmitTime(pred.getLastTransmitTime());
                cycleTableData.setTransmitTimeColor(colorManager
                        .getActualTransmitTimeColor());
            } else {
                cycleTableData.setTransmitTime(pred.getNextTransmitTime());
                cycleTableData.setTransmitTimeColor(colorManager
                        .getPredictedTransmitTimeColor());
            }

            BroadcastMsg message = playlistMap.get(broadcastId);
            InputMessage inputMsg = null;

            if (message != null) {
                inputMsg = message.getInputMessage();
                cycleTableData.setExpirationTime(message.getExpirationTime());
                cycleTableData.setMessageId(message.getAfosid());
                cycleTableData.setInputMsg(inputMsg);
            } else {
                cycleTableData.setExpirationTime(null);
                cycleTableData.setMessageId("Unknown");
                cycleTableData.setInputMsg(null);
            }

            String title = "Unknown";
            if (messageTypeMap.get(broadcastId) != null) {
                /*
                 * handle potential NPE.
                 */
                title = messageTypeMap.get(broadcastId).getTitle();
            }
            cycleTableData.setMessageTitle(title);

            String mrd = null;

            if (message != null) {
                mrd = inputMsg.getMrd();

                if (inputMsg.isPeriodic()) {
                    cycleTableData.setMessageIdColor(colorManager
                            .getPeriodicColor());
                }

                if (inputMsg.getInterrupt() && pred.getPlayCount() <= 1) {
                    cycleTableData.setMessageIdColor(colorManager
                            .getInterruptColor());
                }
                if (message.getReplacementType() != null) {
                    cycleTableData.setMessageIdColor(colorManager
                            .getReplaceColor());
                }
            }

            if (mrd == null) {
                mrd = EMPTY;
            }

            cycleTableData.setMrd(mrd);

            cycleTableData.setBroadcastId(broadcastId);

            dataEntries.add(cycleTableData);
        }

        TableData tableData = new TableData(columns);

        List<TableRowData> rows = new ArrayList<>();
        for (BroadcastCycleTableDataEntry data : dataEntries) {
            TableRowData row = new TableRowData();
            Calendar time = data.getTransmitTime();
            TableCellData cell = null;
            if (time == null) {
                cell = new TableCellData(UNKNOWN_TIME_STR);
            } else {
                cell = new TableCellData(sdf.format(time.getTime()));

            }
            cell.setBackgroundColor(data.getTransmitTimeColor());
            row.addTableCellData(cell);
            TableCellData messageIdCell = new TableCellData(data.getMessageId());
            if (data.getMessageIdColor() != null) {
                messageIdCell.setBackgroundColor(data.getMessageIdColor());
            }

            row.addTableCellData(messageIdCell);
            row.addTableCellData(new TableCellData(data.getMessageTitle()));

            if (data.getInputMsg() != null) {
                row.addTableCellData(new TableCellData(data.getInputMsg()
                        .getName()));
            } else {
                row.addTableCellData(new TableCellData("Unknown"));
            }

            row.addTableCellData(new TableCellData(data.getMrd()));

            if (data.getExpirationTime() == null) {
                row.addTableCellData(new TableCellData(UNKNOWN_TIME_STR));
            } else {
                row.addTableCellData(new TableCellData(sdf.format(data
                        .getExpirationTime().getTime())));
            }

            if (data.isSameSent()) {
                row.addTableCellData(new TableCellData("SENT"));
            } else {
                row.addTableCellData(new TableCellData("NONE"));
            }

            if (data.isAlertSent()) {
                row.addTableCellData(new TableCellData("SENT"));
            } else {
                row.addTableCellData(new TableCellData("NONE"));
            }

            row.addTableCellData(new TableCellData(data.getPlayCount(), null));
            row.setData(data);
            rows.add(row);
            tableData.addDataRow(row);
        }

        return tableData;
    }

    public TableData getNonStandardTableData(INonStandardBroadcast notification) {
        TableData liveTableData = new TableData(columns);

        String[] columnText = new String[0];
        if (notification instanceof LiveBroadcastSwitchNotification) {
            LiveBroadcastSwitchNotification broadcastNotification = (LiveBroadcastSwitchNotification) notification;
            columnText = new String[] {
                    sdf.format(broadcastNotification.getTransitTime().getTime()),
                    broadcastNotification.getMessageId(),
                    broadcastNotification.getMessageTitle(),
                    broadcastNotification.getMessageName(),
                    broadcastNotification.getMrd(),
                    broadcastNotification.getExpirationTime(),
                    broadcastNotification.getAlertTone(),
                    broadcastNotification.getSameTone(),
                    broadcastNotification.getPlayCount() };
        } else if (notification instanceof MaintenanceMessagePlayback) {
            final String contentFiller = "-";

            MaintenanceMessagePlayback broadcastNotification = (MaintenanceMessagePlayback) notification;
            columnText = new String[] {
                    sdf.format(broadcastNotification.getTransitTime().getTime()),
                    contentFiller, contentFiller,
                    broadcastNotification.getName(), contentFiller,
                    UNKNOWN_TIME_STR, contentFiller,
                    broadcastNotification.getSameTone(), contentFiller };
        }
        TableRowData tableRowData = new TableRowData();
        for (String text : columnText) {
            TableCellData tableCellData = new TableCellData(text);
            tableCellData.setBackgroundColor(this.colorManager
                    .getLiveBroadcastColor());
            tableRowData.addTableCellData(tableCellData);
        }
        liveTableData.addDataRow(tableRowData);

        return liveTableData;
    }

    public TableData getPeriodicTableData(String transmitterGroupName) {
        PlaylistDataStructure playlistDataStructure = playlistDataMap
                .get(transmitterGroupName);
        if (playlistDataStructure == null) {
            return new TableData(dataManager.getPeriodicMessageColumns());
        }

        Map<Long, MessagePlaybackPrediction> predictionMap = playlistDataStructure
                .getPredictionMap();

        Map<Long, MessagePlaybackPrediction> periodicPredictionMap = playlistDataStructure
                .getPeriodicPredictionMap();

        Map<Long, BroadcastMsg> playlistMap = playlistDataStructure
                .getPlaylistMap();

        TableData tableData = new TableData(
                dataManager.getPeriodicMessageColumns());

        for (MessagePlaybackPrediction prediction : predictionMap.values()) {
            BroadcastMsg playlistMessage = playlistMap.get(prediction
                    .getBroadcastId());
            if (playlistMessage != null) {
                /*
                 * This is bad but the user was already informed when the
                 * broadcast cycle table was populated.
                 */
                if (playlistMessage.isPeriodic()) {
                    tableData.addDataRow(createPeriodicTableRow(prediction,
                            playlistMessage));
                }
            }
        }
        for (MessagePlaybackPrediction prediction : periodicPredictionMap
                .values()) {
            BroadcastMsg broadcast = playlistMap.get(prediction
                    .getBroadcastId());
            tableData.addDataRow(createPeriodicTableRow(prediction, broadcast));
        }
        return tableData;
    }

    private TableRowData createPeriodicTableRow(
            MessagePlaybackPrediction prediction, BroadcastMsg broadcast) {
        TableRowData rowData = new TableRowData();
        Calendar lastTransmitTime = prediction.getLastTransmitTime();
        if (lastTransmitTime == null) {
            rowData.addTableCellData(new TableCellData(UNKNOWN_TIME_STR));
        } else {
            rowData.addTableCellData(new TableCellData(sdf
                    .format(lastTransmitTime.getTime())));
        }
        Calendar nextTransmitTime = prediction.getNextTransmitTime();
        if (nextTransmitTime == null) {
            if (lastTransmitTime != null) {
                int pSecs = broadcast.getInputMessage().getPeriodicitySeconds();
                nextTransmitTime = (Calendar) lastTransmitTime.clone();
                nextTransmitTime.add(Calendar.SECOND, pSecs);
            } else {
                rowData.addTableCellData(new TableCellData(UNKNOWN_TIME_STR));
            }
        }
        if (nextTransmitTime != null) {
            rowData.addTableCellData(new TableCellData(sdf
                    .format(nextTransmitTime.getTime())));
        }

        TableCellData cell = new TableCellData(broadcast.getAfosid());
        rowData.addTableCellData(cell);

        cell = new TableCellData(broadcast.getInputMessage().getName());
        rowData.addTableCellData(cell);
        rowData.setData(broadcast);
        return rowData;
    }

    /**
     * Add data for a transmitter.
     * 
     * @param transmitterGrpName
     * @param dataStruct
     */
    public void setData(String transmitterGrpName,
            PlaylistDataStructure dataStruct) {
        playlistDataMap.put(transmitterGrpName, dataStruct);
    }

    /**
     * Used to remove cached playlist data when a Transmitter has been disabled.
     * 
     * @param transmitterGrpName
     *            the Transmitter that has been disabled.
     */
    public void purgeData(String transmitterGrpName) {
        playlistDataMap.remove(transmitterGrpName);
    }
}