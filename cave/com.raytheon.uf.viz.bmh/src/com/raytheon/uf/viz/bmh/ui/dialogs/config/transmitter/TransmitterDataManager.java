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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.TransmitterRequest;
import com.raytheon.uf.common.bmh.request.TransmitterRequest.TransmitterRequestAction;
import com.raytheon.uf.common.bmh.request.TransmitterResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * DataManager for the Transmitter Configuration dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014    3173    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterDataManager {
    /**
     * Get a list of {@link TransmitterGroup}s
     * 
     * @return List of TransmitterGroups
     * @throws Exception
     */
    public List<TransmitterGroup> getTransmitterGroups() throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.GetTransmitterGroups);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitterGroupList();
    }

    /**
     * Save a {@link Transmitter}
     * 
     * @param transmitter
     *            The Transmitter to save
     * @return
     * @throws Exception
     */
    public Transmitter saveTransmitter(Transmitter transmitter)
            throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.SaveTransmitter);
        request.setTransmitter(transmitter);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitter();
    }

    /**
     * Save the {@link TransmitterGroup}
     * 
     * @param group
     *            The TransmitterGroup to save
     * @return TransmitterGroup object in a list
     * @throws Exception
     */
    public List<TransmitterGroup> saveTransmitterGroup(TransmitterGroup group)
            throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.SaveGroup);
        request.setTransmitterGroup(group);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitterGroupList();
    }

    /**
     * Delete the {@link Transmitter}
     * 
     * @param toDelete
     *            The Transmitter to delete
     * @throws Exception
     */
    public void deleteTransmitter(Transmitter toDelete) throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.DeleteTransmitter);
        request.setTransmitter(toDelete);

        BmhUtils.sendRequest(request);
    }

    /**
     * Delete the {@link TransmitterGroup}
     * 
     * @param toDelete
     *            the TransmitterGroup to delete
     * @throws Exception
     */
    public void deleteTransmitterGroup(TransmitterGroup toDelete)
            throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.DeleteTransmitterGroup);
        request.setTransmitterGroup(toDelete);

        BmhUtils.sendRequest(request);
    }

    /**
     * Get a list of {@link Transmitter}s
     * 
     * @return List of Transmitters
     * @throws Exception
     */
    public List<Transmitter> getTransmitters() throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.GetTransmitters);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitterList();
    }

    /**
     * Save the list of {@link TransmitterGroup}s
     * 
     * @param groupList
     *            List of groups to save
     * @return Updated list of groups
     * @throws Exception
     */
    public List<TransmitterGroup> saveTransmitterGroups(
            List<TransmitterGroup> groupList) throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.SaveGroupList);
        request.setTransmitterGroupList(groupList);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitterGroupList();
    }

    /**
     * Save the {@link Transmitter} and delete the {@link TransmitterGroup}
     * 
     * @return The saved Transmitter
     */
    public Transmitter saveTransmitterDeleteGroup(Transmitter transmitter,
            TransmitterGroup group) throws Exception {
        TransmitterRequest request = new TransmitterRequest();
        request.setAction(TransmitterRequestAction.SaveTransmitterDeleteGroup);
        request.setTransmitter(transmitter);
        request.setTransmitterGroup(group);

        TransmitterResponse response = (TransmitterResponse) BmhUtils
                .sendRequest(request);

        return response.getTransmitter();
    }
}