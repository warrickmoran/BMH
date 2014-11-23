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
package com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * Object to store the DACs and Transmitter status. This stores the DACs and
 * associated Transmitters/Groups plus the Transmitter/Groups that are not
 * assigned to a DAC port.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Removed constructor.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class DacTransmitterStatusData {

    /** Map with the DAC Id as the key. */
    private SortedMap<Integer, DacInfo> dacInfoMap = new TreeMap<Integer, DacInfo>();

    /** List of transmitter groups that are not associated with a DAC. */
    private SortedMap<String, TransmitterGrpInfo> noTransmitterGrpInfoMap = new TreeMap<String, TransmitterGrpInfo>();

    /**
     * Add the DAC information to the DAC data info map.
     * 
     * @param dacId
     *            DAC Id.
     * @param dacInfo
     *            DAC information.
     */
    public void addDacInfo(int dacId, DacInfo dacInfo) {
        dacInfoMap.put(dacId, dacInfo);
    }

    /**
     * Get the DAC info map.
     * 
     * @return The DAC info map.
     */
    public Map<Integer, DacInfo> getDacInfoMap() {
        return dacInfoMap;
    }

    /**
     * Add the transmitter group information to the list of transmitters/groups
     * that are not associated with a DAC.
     * 
     * @param transmitterGrpInfo
     *            Transmitter group information.
     */
    public void addTranmitterWithNoDac(TransmitterGrpInfo transmitterGrpInfo) {
        noTransmitterGrpInfoMap.put(transmitterGrpInfo.getGroupName(),
                transmitterGrpInfo);
    }

    /**
     * Get the list of transmitter group information that is not associated with
     * a DAC.
     * 
     * @return
     */
    public List<TransmitterGrpInfo> getNoDacTransGrpInfoList() {
        List<TransmitterGrpInfo> tgiList = new ArrayList<TransmitterGrpInfo>();

        for (String str : noTransmitterGrpInfoMap.keySet()) {
            tgiList.add(noTransmitterGrpInfoMap.get(str));
        }

        return tgiList;
    }

    /**
     * Get the DAC information.
     * 
     * @param dacId
     *            DAC Id.
     * @return The DAC information associated with the DAC Id.
     */
    public DacInfo getDacInfo(Integer dacId) {
        return dacInfoMap.get(dacId);
    }
}
