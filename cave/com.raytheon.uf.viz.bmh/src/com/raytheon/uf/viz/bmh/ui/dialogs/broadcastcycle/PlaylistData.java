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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistData {
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(PlaylistData.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "MM/dd/yy HH:mm:ss");

    private final BroadcastCycleTableEntryComparator comparator = new BroadcastCycleTableEntryComparator();

    /** Color manager */
    private final BroadcastCycleColorManager colorManager;

    /** Map of Transmitter -> PlaylistData for that transmitter */
    private final Map<String, PlaylistDataStructure> playlistDataMap = new HashMap<>();

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
    }

    /**
     * Handle the {@link PlaylistSwitchNotification}
     * 
     * @param notification
     */
    public void handlePLaylistSwitchNotification(
            PlaylistSwitchNotification notification) {
        String tg = notification.getTransmitterGroup();
        List<DacPlaylistMessageId> playlist = notification.getPlaylist();
        List<MessagePlaybackPrediction> messageList = notification
                .getMessages();

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

        Map<Long, BroadcastMsg> playlistMap = playlistData.getPlaylistMap();
        // remove unused messages
        playlistMap.keySet().retainAll(predictionMap.keySet());

        Map<Long, MessageType> messageTypeMap = playlistData
                .getMessageTypeMap();

        for (DacPlaylistMessageId id : playlist) {
            try {
                BroadcastMsg broadcastMessage = dataManager
                        .getBroadcastMessage(id.getBroadcastId());
                MessageType messageType = dataManager
                        .getMessageType(broadcastMessage.getAfosid());
                playlistMap.put(id.getBroadcastId(), broadcastMessage);
                messageTypeMap.put(id.getBroadcastId(), messageType);
            } catch (Exception e) {
                statusHandler.error("Error accessing BMH database", e);
            }
        }
    }

    /**
     * Handle the {@link MessagePlaybackStatusNotification}
     * 
     * @param notification
     */
    public void handlePlaybackStatusNotification(
            MessagePlaybackStatusNotification notification) {
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
     * Get the updated {@link TableData} object for the provided transmitter
     * group name.
     * 
     * @param transmitterGroupName
     * @return
     */
    public TableData getUpdatedTableData(String transmitterGroupName) {
        PlaylistDataStructure playlistData = playlistDataMap
                .get(transmitterGroupName);
        if (playlistData == null) {
            return new TableData(columns);
        }

        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        Map<Long, BroadcastMsg> playlistMap = playlistData.getPlaylistMap();
        Map<Long, MessageType> messageTypeMap = playlistData
                .getMessageTypeMap();

        List<BroadcastCycleTableDataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<Long, MessagePlaybackPrediction> entry : predictionMap
                .entrySet()) {
            Long broadcastId = entry.getKey();
            MessagePlaybackPrediction pred = entry.getValue();
            BroadcastCycleTableDataEntry data = new BroadcastCycleTableDataEntry();
            data.setAlertSent(pred.isPlayedAlertTone());
            data.setPlayCount(pred.getPlayCount());
            data.setSameSent(pred.isPlayedSameTone());
            if (pred.getNextTransmitTime() == null) {
                data.setTransmitTime(pred.getLastTransmitTime());
                data.setTransmitTimeColor(colorManager
                        .getActualTransmitTimeColor());
            } else {
                data.setTransmitTime(pred.getNextTransmitTime());
                data.setTransmitTimeColor(colorManager
                        .getPredictedTransmitTimeColor());
            }

            BroadcastMsg message = playlistMap.get(broadcastId);
            data.setExpirationTime(message.getInputMessage()
                    .getExpirationTime());
            data.setMessageId(message.getAfosid());
            String title = messageTypeMap.get(broadcastId).getTitle();
            data.setMessageTitle(title);
            data.setMrd("MRD"); // TODO

            data.setBroadcastId(broadcastId);
            data.setInputMsg(message.getInputMessage());
            // TODO set other background colors
            dataEntries.add(data);
        }

        Collections.sort(dataEntries, comparator);

        TableData tableData = new TableData(columns);

        List<TableRowData> rows = new ArrayList<>();
        for (BroadcastCycleTableDataEntry data : dataEntries) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(sdf.format(data
                    .getTransmitTime().getTime()));
            cell.setBackgroundColor(data.getTransmitTimeColor());
            row.addTableCellData(cell);
            row.addTableCellData(new TableCellData(data.getMessageId()));
            row.addTableCellData(new TableCellData(data.getMessageTitle()));
            row.addTableCellData(new TableCellData(data.getMrd()));
            row.addTableCellData(new TableCellData(sdf.format(data
                    .getExpirationTime().getTime())));
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

    public TableData getLiveTableData(
            LiveBroadcastSwitchNotification notification) {
        TableData liveTableData = new TableData(columns);

        final String[] columnText = new String[] {
                sdf.format(notification.getTransitTime().getTime()),
                notification.getMessageType().getAfosid(),
                notification.getMessageType().getTitle(), "-",
                sdf.format(notification.getExpirationTime().getTime()),
                notification.getAlertTone(), notification.getSameTone(), "1" };
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
}
