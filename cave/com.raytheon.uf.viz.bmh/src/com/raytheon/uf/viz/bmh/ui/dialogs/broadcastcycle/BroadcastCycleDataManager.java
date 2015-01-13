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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.BroadcastMsgRequest;
import com.raytheon.uf.common.bmh.request.BroadcastMsgRequest.BroadcastMessageAction;
import com.raytheon.uf.common.bmh.request.BroadcastMsgResponse;
import com.raytheon.uf.common.bmh.request.PlaylistRequest;
import com.raytheon.uf.common.bmh.request.PlaylistRequest.PlaylistAction;
import com.raytheon.uf.common.bmh.request.PlaylistResponse;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramRequest.ProgramAction;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteRequest.SuiteAction;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.bmh.request.TransmitterRequest;
import com.raytheon.uf.common.bmh.request.TransmitterRequest.TransmitterRequestAction;
import com.raytheon.uf.common.bmh.request.TransmitterResponse;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest.ZoneAreaAction;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;

/**
 * The {@link BroadcastCycleDlg} data manager class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 03, 2014    3432    mpduff      Initial creation
 * Aug 14, 2014    3432    mpduff      Implementing more methods.
 * Aug 23, 2014    3432    mpduff      Add getPlaylistDataForTransmitter
 * Aug 24, 2014    3432    mpduff      Added getEnabledTransmitterGroups
 * Sep 15, 2014   #3610    lvenable    Moved getMessageType functionality into the
 *                                     MessageTypeDataManager class.
 * Sep 12, 2014    3588    bsteffen    Broadcast msg support audio fragments.
 * Oct 21, 2014    3655    bkowal      Updated to use {@link IPlaylistData}.
 * Dec 08, 2014    3864    bsteffen    Add a PlaylistMsg class.
 * Dec 13, 2014    3843    mpduff      Implement periodic messages.
 * Dec 18, 2014    3865    bsteffen    add getBroadcastMessagesForInputMessage.
 * Jan 12, 2015    3843    bsteffen    Move Periodic message table creation to playlist data.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleDataManager {

    /** Message type data manager. */
    private MessageTypeDataManager messageTypeDataManager;

    /**
     * Constructor
     */
    public BroadcastCycleDataManager() {

    }

    /**
     * Get the list of enabled transmitter groups
     * 
     * @return
     * @throws Exception
     */
    public List<TransmitterGroup> getEnabledTransmitterGroupList()
            throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.GetEnabledTransmitterGroups);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);
        return response.getTransmitterGroupList();
    }

    /**
     * Get the {@link Program} associated with the provided
     * {@link TransmitterGroup}
     * 
     * @param group
     *            The TransmitterGroup
     * @return The associated Program
     * @throws Exception
     */
    public Program getProgramForTransmitterGroup(TransmitterGroup group)
            throws Exception {
        ProgramRequest req = new ProgramRequest();
        req.setAction(ProgramAction.GetProgramForTransmitterGroup);
        req.setTransmitterGroup(group);

        ProgramResponse response = (ProgramResponse) BmhUtils.sendRequest(req);

        return response.getProgramList().get(0);
    }

    /**
     * Create columns for the Periodic Message dialog.
     * 
     * @return
     */
    public List<TableColumnData> getPeriodicMessageColumns() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(4);
        columns.add(new TableColumnData("Last Broadcast Time"));
        columns.add(new TableColumnData("Next Predicted Broadcast"));
        columns.add(new TableColumnData("Message Id"));
        columns.add(new TableColumnData("Message Name"));

        return columns;
    }

    /**
     * Get the {@link MessageType} object for the associated afosId
     * 
     * @param afosId
     *            The afosId
     * @return The MessageType
     * @throws Exception
     */
    public MessageType getMessageType(String afosId) throws Exception {

        if (messageTypeDataManager == null) {
            messageTypeDataManager = new MessageTypeDataManager();
        }

        return messageTypeDataManager.getMessageType(afosId);
    }

    /**
     * Get the {@link BroadcastMsg} for the associated Broadcast Message Id.
     * 
     * @param broadcastMessageId
     *            The broadcastMessageId
     * @return The BroadcastMsg
     * @throws Exception
     */
    public BroadcastMsg getBroadcastMessage(long broadcastMessageId)
            throws Exception {
        BroadcastMsgRequest req = new BroadcastMsgRequest();
        req.setAction(BroadcastMessageAction.GET_MESSAGE_BY_ID);
        req.setMessageId(broadcastMessageId);

        BroadcastMsgResponse response = (BroadcastMsgResponse) BmhUtils
                .sendRequest(req);

        List<BroadcastMsg> msgList = response.getMessageList();
        if ((msgList != null) && !msgList.isEmpty()) {
            return msgList.get(0);
        }

        return null;
    }

    /**
     * Get all the {@link BroadcastMsg}s for the associated Input Message Id.
     * 
     * @param inputMessageId
     *            The inputMessageId
     * @return Some BroadcastMsgs
     * @throws Exception
     */
    public List<BroadcastMsg> getBroadcastMessagesForInputMessage(
            int inputMessageId) throws Exception {
        BroadcastMsgRequest req = new BroadcastMsgRequest();
        req.setAction(BroadcastMessageAction.GET_MESSAGE_BY_INPUT_ID);
        req.setMessageId((long) inputMessageId);

        BroadcastMsgResponse response = (BroadcastMsgResponse) BmhUtils
                .sendRequest(req);

        return response.getMessageList();
    }

    /**
     * Get transmitters playing this {@link MessageType}
     * 
     * @param messageType
     *            The MessageType
     * 
     * @return List of Transmitters playing the MessageType
     * @throws Exception
     */
    public List<Transmitter> getTransmitterForMessageType(
            MessageType messageType) throws Exception {
        List<Transmitter> transList = new ArrayList<>();
        ProgramRequest pRequest = new ProgramRequest();
        pRequest.setAction(ProgramAction.AllPrograms);
        ProgramResponse pResponse = (ProgramResponse) BmhUtils
                .sendRequest(pRequest);
        List<Program> programList = pResponse.getProgramList();

        List<Program> assocProgs = new ArrayList<>();
        ProgramFor: for (Program p : programList) {
            List<Suite> suitesInProgram = p.getSuites();
            for (Suite progSuite : suitesInProgram) {
                for (SuiteMessage sm : progSuite.getSuiteMessages()) {
                    if (messageType.getAfosid().equals(sm.getAfosid())) {
                        assocProgs.add(p);
                        continue ProgramFor;
                    }
                }
            }
        }

        for (Program p : assocProgs) {
            for (TransmitterGroup tg : p.getTransmitterGroups()) {
                transList.addAll(tg.getTransmitters());
            }
        }

        return transList;
    }

    /**
     * Get {@link Area}s for the provided {@link MessageType}
     * 
     * @param messageType
     *            The MessageType
     * @return The Area
     * @throws Exception
     */
    public List<Area> getAreasForMessageType(MessageType messageType)
            throws Exception {
        List<Transmitter> transmitterList = getTransmitterForMessageType(messageType);

        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetAreas);
        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);
        List<Area> areaList = response.getAreaList();

        Set<Area> returnList = new HashSet<>();

        for (Transmitter t : transmitterList) {
            for (Area a : areaList) {
                if (a.getTransmitters().contains(t)) {
                    returnList.add(a);
                }
            }
        }

        return new ArrayList<Area>(returnList);
    }

    /**
     * Get the {@link Program}s associated with the {@link MessageType}
     * 
     * @param messageType
     *            The MessageType
     * @return The list of associated Programs
     * @throws Exception
     */
    public List<Program> getProgramsForMessageType(MessageType messageType)
            throws Exception {
        ProgramRequest pRequest = new ProgramRequest();
        pRequest.setAction(ProgramAction.AllPrograms);
        ProgramResponse pResponse = (ProgramResponse) BmhUtils
                .sendRequest(pRequest);
        List<Program> programList = pResponse.getProgramList();

        List<Program> assocProgs = new ArrayList<>();
        ProgramFor: for (Program p : programList) {
            List<Suite> suitesInProgram = p.getSuites();
            for (Suite progSuite : suitesInProgram) {
                for (SuiteMessage sm : progSuite.getSuiteMessages()) {
                    if (messageType.getAfosid().equals(sm.getAfosid())) {
                        assocProgs.add(p);
                        continue ProgramFor;
                    }
                }
            }
        }

        return assocProgs;
    }

    /**
     * Get the {@link Suite}s associated with the provided {@link MessageType}
     * 
     * @param messageType
     * @return
     * @throws Exception
     */
    public List<Suite> getSuitesForMessageType(MessageType messageType)
            throws Exception {
        SuiteRequest req = new SuiteRequest();
        req.setAction(SuiteAction.AllSuites);

        List<Suite> assocSuites = new ArrayList<>();

        SuiteResponse response = (SuiteResponse) BmhUtils.sendRequest(req);
        List<Suite> suiteList = response.getSuiteList();
        for (Suite s : suiteList) {
            for (SuiteMessage sm : s.getSuiteMessages()) {
                if (messageType.getAfosid().equals(sm.getAfosid())) {
                    assocSuites.add(s);
                    break;
                }
            }
        }

        return assocSuites;
    }

    public IPlaylistData getPlaylistDataForTransmitter(String transmitterName)
            throws Exception {
        PlaylistRequest request = new PlaylistRequest();
        request.setAction(PlaylistAction.GET_PLAYLIST_DATA_FOR_TRANSMITTER);
        request.setTransmitterName(transmitterName);

        PlaylistResponse response = (PlaylistResponse) BmhUtils
                .sendRequest(request);

        return response.getPlaylistData();
    }
}