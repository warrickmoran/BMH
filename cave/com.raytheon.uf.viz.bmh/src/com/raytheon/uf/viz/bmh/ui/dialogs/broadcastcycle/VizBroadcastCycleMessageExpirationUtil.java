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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.ExpireBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Provides a common, shared method to initiate the expiration of a
 * {@link BroadcastMsg}. This capability has been implemented as a utility
 * because the two classes that would have a use for it (as of July 2016) extend
 * completely different parent classes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2016 5767       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class VizBroadcastCycleMessageExpirationUtil {

    protected VizBroadcastCycleMessageExpirationUtil() {
    }

    /**
     * Common method to initiate the expiration of the specified
     * {@link BroadcastMsg}.
     * 
     * @param broadcastMsg
     *            the specified {@link BroadcastMsg}
     * @param dataManager
     *            {@link BroadcastCycleDataManager} used to retrieve additional
     *            information related to a {@link BroadcastMsg}
     * @param shell
     *            {@link Shell} associated with the dialog that is utilizing the
     *            common expiration method
     * @param statusHandler
     *            {@link IUFStatusHandler} associated with the dialog that is
     *            utilizing the common expiration method
     */
    public static void initiateMessageExpiration(
            final BroadcastMsg broadcastMsg,
            final BroadcastCycleDataManager dataManager, final Shell shell,
            final IUFStatusHandler statusHandler) {
        try {
            final InputMessage inputMessage = broadcastMsg.getInputMessage();
            List<BroadcastMsg> messages = dataManager
                    .getBroadcastMessagesForInputMessage(inputMessage.getId());
            Iterator<BroadcastMsg> it = messages.iterator();
            while (it.hasNext()) {
                if (it.next().getForcedExpiration()) {
                    /*
                     * Exclude individual broadcast messages that have already
                     * been expired.
                     */
                    it.remove();
                }
            }
            if (messages.size() == 1) {
                expireMessageOneDestination(inputMessage, broadcastMsg, shell);
                return;
            }

            CheckListData cld = new CheckListData();

            int scheduledCount = 0;
            BroadcastMsg scheduledBroadcastMsg = null;
            final Set<Transmitter> allTransmittersSet = new HashSet<>();
            final Map<String, BroadcastMsg> transmitterGrpToBroadcastMsgMap = new HashMap<>();
            for (BroadcastMsg message : messages) {
                TransmitterGroup transmitterGroup = message
                        .getTransmitterGroup();
                /*
                 * Determine if the message has actually been scheduled for
                 * broadcast.
                 */
                boolean scheduled = false;
                try {
                    scheduled = dataManager.isMessageScheduledForBroadcast(
                            transmitterGroup.getName(), message.getId());
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder(
                            "Failed to determine if broadcast message: ");
                    sb.append(message.getId())
                            .append(" has been scheduled for broadcast on transmitter: ");
                    sb.append(transmitterGroup.getName()).append(".");

                    statusHandler.error(sb.toString(), e);
                }
                if (scheduled == false) {
                    /*
                     * Note: if the user expires the message on all Transmitters
                     * that the message has been expired on, the message will be
                     * permanently expired and it will not be broadcast on other
                     * transmitters that it has not previously been scheduled on
                     * even if they are enabled.
                     */
                    continue;
                }

                scheduledBroadcastMsg = message;
                ++scheduledCount;

                transmitterGrpToBroadcastMsgMap.put(transmitterGroup.getName(),
                        message);
                cld.addDataItem(transmitterGroup.getName(), true);
                allTransmittersSet.addAll(transmitterGroup.getTransmitters());
            }

            if (scheduledCount == 1) {
                /*
                 * Only one message has actually been scheduled for broadcast.
                 */
                expireMessageOneDestination(inputMessage,
                        scheduledBroadcastMsg, shell);
                return;
            }

            String dialogText = "Expiring " + inputMessage.getName()
                    + "\n\nSelect Transmitter Groups:";

            CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                    "Expire Selection", dialogText, cld, true);
            checkListDlg.setCloseCallback(new ICloseCallback() {
                @Override
                public void dialogClosed(Object returnValue) {
                    if ((returnValue != null)
                            && (returnValue instanceof CheckListData)) {
                        handleExpireDialogCallback(inputMessage,
                                (CheckListData) returnValue,
                                transmitterGrpToBroadcastMsgMap,
                                allTransmittersSet, statusHandler);
                    }
                }

            });
            checkListDlg.open();
        } catch (Exception e) {
            statusHandler.error(
                    "Error expiring message: " + broadcastMsg.getId(), e);
        }
    }

    /**
     * Common method used to expire the specified {@link InputMessage} that has
     * been broadcast to only one Transmitter.
     * 
     * @param inputMessage
     *            the specified {@link InputMessage}
     * @param broadcastMsg
     *            the {@link Broadcast} message associated with the specified
     *            input message
     * @param shell
     *            {@link Shell} associated with the dialog that is utilizing the
     *            common expiration method
     * @throws Exception
     */
    private static void expireMessageOneDestination(InputMessage inputMessage,
            BroadcastMsg broadcastMsg, Shell shell) throws Exception {
        String message = "Are you sure you want to Expire/Delete "
                + inputMessage.getName() + "?";
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.CANCEL
                | SWT.ICON_QUESTION);
        mb.setText("Confirm Expire/Delete");
        mb.setMessage(message);
        if (mb.open() == SWT.OK) {
            NewBroadcastMsgRequest request = new NewBroadcastMsgRequest(
                    System.currentTimeMillis());
            inputMessage.setActive(false);
            request.setInputMessage(inputMessage);
            request.setSelectedTransmitters(new ArrayList<>(broadcastMsg
                    .getTransmitterGroup().getTransmitters()));
            BmhUtils.sendRequest(request);
        }
    }

    /**
     * Handles the potential expiration of multiple {@link BroadcastMsg}s at
     * once. Messages are selected using a {@link CheckScrollListDlg}.
     * 
     * @param inputMessage
     *            the {@link InputMessage} associated with the
     *            {@link BroadcastMsg}s that can be selected
     * @param data
     *            {@link CheckListData} that indicates transmitters the message
     *            should be discontinued on
     * @param transmitterGrpToBroadcastMsgMap
     *            a {@link Map} of transmitter names to the {@link BroadcastMsg}
     *            that would be disseminated using the transmitter
     * @param allTransmittersSet
     *            a {@link Set} of all transmitters that the specified
     *            {@link InputMessage} could be broadcast t0
     * @param statusHandler
     *            {@link IUFStatusHandler} associated with the dialog that is
     *            utilizing the common expiration method
     */
    private static void handleExpireDialogCallback(InputMessage inputMessage,
            CheckListData data,
            Map<String, BroadcastMsg> transmitterGrpToBroadcastMsgMap,
            Set<Transmitter> allTransmittersSet,
            final IUFStatusHandler statusHandler) {
        if (data.allChecked()) {
            NewBroadcastMsgRequest request = new NewBroadcastMsgRequest(
                    System.currentTimeMillis());
            inputMessage.setActive(false);
            request.setInputMessage(inputMessage);
            request.setSelectedTransmitters(new ArrayList<>(allTransmittersSet));
            sendExpireRequest(request, statusHandler);
        } else {
            List<BroadcastMsg> messagesToExpire = new ArrayList<>();
            for (Entry<String, Boolean> entry : data.getDataMap().entrySet()) {
                if (entry.getValue()) {
                    messagesToExpire.add(transmitterGrpToBroadcastMsgMap
                            .get(entry.getKey()));
                }
            }
            if (messagesToExpire.isEmpty()) {
                return;
            }
            ExpireBroadcastMsgRequest request = new ExpireBroadcastMsgRequest();
            request.setExpiredBroadcastMsgs(messagesToExpire);
            sendExpireRequest(request, statusHandler);
        }
    }

    /**
     * Submits the specified {@link AbstractBMHServerRequest} to the EDEX BMH
     * server. Handles any errors that may occur.
     * 
     * @param request
     *            the specified {@link AbstractBMHServerRequest}
     * @param statusHandler
     *            {@link IUFStatusHandler} associated with the dialog that is
     *            utilizing the common expiration method
     */
    private static void sendExpireRequest(AbstractBMHServerRequest request,
            final IUFStatusHandler statusHandler) {
        try {
            BmhUtils.sendRequest(request);
        } catch (Exception e) {
            statusHandler.error("Error expiring message.", e);
        }
    }
}