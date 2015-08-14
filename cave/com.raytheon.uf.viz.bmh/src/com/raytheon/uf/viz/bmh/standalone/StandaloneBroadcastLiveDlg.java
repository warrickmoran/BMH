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
package com.raytheon.uf.viz.bmh.standalone;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastGroupsMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastListGroupsCommand;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.comms.CommsCommunicationException;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcast.BroadcastLiveDlg;

/**
 * An extension of the {@link BroadcastLiveDlg} that works when the bmh server
 * is not running edex by requesting the available groups directly from the
 * comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * Jun 05, 2015  4490      rjpeter     Updated constructor.
 * Aug 13, 2015  4424      bkowal      Removed extra quotes around the transmitter group name.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class StandaloneBroadcastLiveDlg extends BroadcastLiveDlg {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(StandaloneBroadcastLiveDlg.class);

    public StandaloneBroadcastLiveDlg(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected CheckListData buildTransmitterChecklist() {
        CheckListData cld = new CheckListData();

        String[] groups;
        try {
            groups = getGroups();
        } catch (CommsCommunicationException e) {
            statusHandler.error(
                    "Unable to retrieve groups from comms manager.", e);
            groups = new String[0];
        }
        this.transmitterGroupLookupMap = new HashMap<>(groups.length, 1.0f);
        for (String group : groups) {
            cld.addDataItem(group, false);
            TransmitterGroup g = new TransmitterGroup();
            g.setName(group);
            transmitterGroupLookupMap.put(group, g);
        }

        return cld;
    }

    /**
     * Download the available transmitter groups from the comms manager.
     */
    public static String[] getGroups() throws CommsCommunicationException {
        String commsLoc = BMHServers.getBroadcastServer();
        if (commsLoc == null) {
            throw new CommsCommunicationException(
                    "No address has been specified for comms manager "
                            + BMHServers.getBroadcastServerKey() + ".");
        }
        URI commsURI = null;
        try {
            commsURI = new URI(commsLoc);
        } catch (URISyntaxException e) {
            throw new CommsCommunicationException(
                    "Invalid Comms Manager Location.", e);
        }
        try (Socket socket = new Socket(commsURI.getHost(), commsURI.getPort())) {
            socket.setTcpNoDelay(true);
            SerializationUtil.transformToThriftUsingStream(
                    new LiveBroadcastListGroupsCommand(),
                    socket.getOutputStream());
            Object message = SerializationUtil.transformFromThrift(
                    Object.class, socket.getInputStream());
            if (message instanceof LiveBroadcastGroupsMessage) {
                return ((LiveBroadcastGroupsMessage) message).getGroups();
            } else if (message == null) {
                throw new NullPointerException(
                        "Unexpected null response from comms manager.");
            } else {
                throw new IllegalStateException(
                        "Unexpected response from comms manager of type: "
                                + message.getClass().getSimpleName());
            }
        } catch (IOException | SerializationException e) {
            throw new CommsCommunicationException(
                    "Error Communicating with CommsManager", e);
        }
    }

}
