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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistNotification;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
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
 * May 20, 2015   4490     bkowal      Ensure that an interrupt message is only displayed in
 *                                     red during the initial playback.
 * May 22, 2015   4481     bkowal      Set the dynamic flag on {@link BroadcastCycleTableDataEntry}.
 * Jul 20, 2015   4424     bkowal      Ensure the local playlist data structure always references
 *                                     the correct suite.
 * Aug 03, 2015   4686     bkowal      Do not display messages that expire before their next periodic
 *                                     broadcast.
 * Jan 28, 2016   5300     rjpeter     Fixed PlaylistDataStructure memory leak.
 * Feb 04, 2016   5308     rjpeter     Ask comms manager for playlist data if none in memory.
 * Mar 14, 2016   5472     rjpeter     Moved comms manager playlist request to BroadcastCycleDlg.
 * Jan 20, 2017   6078     bkowal      Update to account for the fact that the Alert column is displayed
 *                                     before the SAME column in the Broadcast Cycle dialog.
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
     * Handle the {@link PlaylistNotification}
     * 
     * @param notification
     */
    public void handlePlaylistSwitchNotification(
            PlaylistNotification notification) {
        String tg = notification.getTransmitterGroup();
        List<MessagePlaybackPrediction> messageList = notification
                .getMessages();
        List<MessagePlaybackPrediction> periodicMessageList = notification
                .getPeriodicMessages();

        PlaylistDataStructure playlistData = playlistDataMap.get(tg);
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure(messageList,
                    periodicMessageList, notification.getTimestamp());
        } else {
            if (playlistData.getTimeStamp() > notification.getTimestamp()) {
                /*
                 * handle race condition where an outdated notification is
                 * received out of order
                 */
                return;
            }

            playlistData = new PlaylistDataStructure(messageList,
                    periodicMessageList, playlistData,
                    notification.getTimestamp());
        }

        playlistData.setSuiteName(notification.getSuiteName());
        playlistData.setPlaybackCycleTime(notification.getPlaybackCycleTime());
        Set<Long> missingIds = playlistData.getMissingBroadcastIds();
        if (!missingIds.isEmpty()) {
            try {
                Map<BroadcastMsg, MessageType> broadcastData = dataManager
                        .getPlaylistDataForBroadcastIds(missingIds);
                playlistData.setBroadcastMsgData(broadcastData);
            } catch (Exception e) {
                statusHandler.error("Error accessing BMH database", e);
                return;
            }
        }

        playlistDataMap.put(tg, playlistData);
    }

    /**
     * Handle the {@link MessagePlaybackStatusNotification}
     * 
     * @param notification
     */
    public void handlePlaybackStatusNotification(
            MessagePlaybackStatusNotification notification) {
        if (this.broadcastOverrideMap
                .containsKey(notification.getTransmitterGroup())) {
            this.broadcastOverrideMap
                    .remove(notification.getTransmitterGroup());
        }
        PlaylistDataStructure playlistData = playlistDataMap
                .get(notification.getTransmitterGroup());
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(notification.getTransmitterGroup(),
                    playlistData);
        }
        playlistData.updatePlaybackData(notification);
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
            this.broadcastOverrideMap.put(
                    notification.getTransmitterGroup().getName(), notification);
        } else if (notification.getBroadcastState() == STATE.FINISHED) {
            this.broadcastOverrideMap
                    .remove(notification.getTransmitterGroup().getName());
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
            return this.getNonStandardTableData(
                    this.broadcastOverrideMap.get(transmitterGroupName));
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
                cycleTableData.setTransmitTimeColor(
                        colorManager.getActualTransmitTimeColor());
            } else {
                cycleTableData.setTransmitTime(pred.getNextTransmitTime());
                cycleTableData.setTransmitTimeColor(
                        colorManager.getPredictedTransmitTimeColor());
            }

            BroadcastMsg message = playlistMap.get(broadcastId);
            InputMessage inputMsg = null;

            if (message != null) {
                inputMsg = message.getInputMessage();
                cycleTableData.setExpirationTime(message.getExpirationTime());
                cycleTableData.setMessageId(message.getAfosid());
                cycleTableData.setInputMsg(inputMsg);
                cycleTableData.setDynamic(pred.isDynamic());
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
                    cycleTableData
                            .setMessageIdColor(colorManager.getPeriodicColor());
                }

                if (message.getReplacementType() != null) {
                    cycleTableData
                            .setMessageIdColor(colorManager.getReplaceColor());
                }

                if (inputMsg.getInterrupt()
                        && ((message.isPlayedInterrupt() == false)
                                || playlistDataStructure.getSuiteName()
                                        .startsWith("Interrupt"))) {
                    cycleTableData.setMessageIdColor(
                            colorManager.getInterruptColor());
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
            TableCellData messageIdCell = new TableCellData(
                    data.getMessageId());
            if (data.getMessageIdColor() != null) {
                messageIdCell.setBackgroundColor(data.getMessageIdColor());
            }

            row.addTableCellData(messageIdCell);
            row.addTableCellData(new TableCellData(data.getMessageTitle()));

            if (data.getInputMsg() != null) {
                row.addTableCellData(
                        new TableCellData(data.getInputMsg().getName()));
            } else {
                row.addTableCellData(new TableCellData("Unknown"));
            }

            row.addTableCellData(new TableCellData(data.getMrd()));

            if (data.getExpirationTime() == null) {
                row.addTableCellData(new TableCellData(UNKNOWN_TIME_STR));
            } else {
                row.addTableCellData(new TableCellData(
                        sdf.format(data.getExpirationTime().getTime())));
            }

            if (data.isAlertSent()) {
                row.addTableCellData(new TableCellData("SENT"));
            } else {
                row.addTableCellData(new TableCellData("NONE"));
            }

            if (data.isSameSent()) {
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

    public TableData getNonStandardTableData(
            INonStandardBroadcast notification) {
        TableData liveTableData = new TableData(columns);

        String[] columnText = new String[0];
        if (notification instanceof LiveBroadcastSwitchNotification) {
            LiveBroadcastSwitchNotification broadcastNotification = (LiveBroadcastSwitchNotification) notification;
            columnText = new String[] {
                    sdf.format(
                            broadcastNotification.getTransitTime().getTime()),
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
                    sdf.format(
                            broadcastNotification.getTransitTime().getTime()),
                    contentFiller, contentFiller,
                    broadcastNotification.getName(), contentFiller,
                    UNKNOWN_TIME_STR, contentFiller,
                    broadcastNotification.getSameTone(), contentFiller };
        }
        TableRowData tableRowData = new TableRowData();
        for (String text : columnText) {
            TableCellData tableCellData = new TableCellData(text);
            tableCellData.setBackgroundColor(
                    this.colorManager.getLiveBroadcastColor());
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
            BroadcastMsg playlistMessage = playlistMap
                    .get(prediction.getBroadcastId());
            if (playlistMessage != null) {
                /*
                 * This is bad but the user was already informed when the
                 * broadcast cycle table was populated.
                 */
                if (playlistMessage.isPeriodic()) {
                    TableRowData trd = createPeriodicTableRow(prediction,
                            playlistMessage);
                    if (trd != null) {
                        tableData.addDataRow(trd);
                    }
                }
            }
        }
        for (MessagePlaybackPrediction prediction : periodicPredictionMap
                .values()) {
            BroadcastMsg broadcast = playlistMap
                    .get(prediction.getBroadcastId());
            TableRowData trd = createPeriodicTableRow(prediction, broadcast);
            if (trd != null) {
                tableData.addDataRow(trd);
            }
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
            rowData.addTableCellData(
                    new TableCellData(sdf.format(lastTransmitTime.getTime())));
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
            if (nextTransmitTime
                    .after(broadcast.getInputMessage().getExpirationTime())) {
                /*
                 * The message expires before the next time it is supposed to be
                 * broadcast, exclude it.
                 */
                return null;
            }
            rowData.addTableCellData(
                    new TableCellData(sdf.format(nextTransmitTime.getTime())));
        }

        TableCellData cell = new TableCellData(broadcast.getAfosid());
        rowData.addTableCellData(cell);

        cell = new TableCellData(broadcast.getInputMessage().getName());
        rowData.addTableCellData(cell);
        rowData.setData(broadcast);
        return rowData;
    }

    /**
     * If no data for transmitter, request it from CommsManager.
     * 
     * @param transmitterGrpName
     * @param dataStruct
     */
    public PlaylistDataStructure getPlaylistData(String transmitterGrpName) {
        return playlistDataMap.get(transmitterGrpName);
    }

    /**
     * Used to remove cached playlist data when a Transmitter has been disabled.
     * 
     * @param transmitterGrpName
     *            the Transmitter that has been disabled.
     */
    public void purgeData(String transmitterGrpName) {
        playlistDataMap.remove(transmitterGrpName);
        broadcastOverrideMap.remove(transmitterGrpName);
    }
}