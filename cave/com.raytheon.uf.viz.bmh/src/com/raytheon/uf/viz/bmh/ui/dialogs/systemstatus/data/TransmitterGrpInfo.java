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
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class that hold the status information for a transmitter group and list of
 * transmitters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014  3349      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class TransmitterGrpInfo {

    /** Silence alarm flag. */
    private boolean silenceAlarm = false;

    /** Group name. */
    private String groupName;

    /** ID */
    private Integer id;

    /**
     * Map of transmitter information that is related to a transmitter group.
     */
    private SortedMap<Integer, List<TransmitterInfo>> transmitterInfoMap = new TreeMap<Integer, List<TransmitterInfo>>();

    /**
     * A port number that is assigned to a transmitter if the DAC port number is
     * null. Once assigned the variable is automatically incremented. This is
     * done so the valuse in the map don't get replaced.
     */
    private int tmpPortNumber = 1000;

    /**
     * Constructor.
     */
    public TransmitterGrpInfo() {

    }

    public boolean isSilenceAlarm() {
        return silenceAlarm;
    }

    public void setSilenceAlarm(boolean silenceAlarm) {
        this.silenceAlarm = silenceAlarm;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SortedMap<Integer, List<TransmitterInfo>> getTransmitterInfoMap() {
        return transmitterInfoMap;
    }

    /**
     * Add the transmitter information to the map.
     * 
     * @param transmitterInfo
     */
    public void addTransmitterInfo(TransmitterInfo transmitterInfo) {

        Integer port = transmitterInfo.getDacPort();

        if (port == null) {
            port = tmpPortNumber;
            ++tmpPortNumber;
        }

        if (transmitterInfoMap.containsKey(port)) {
            transmitterInfoMap.get(port).add(transmitterInfo);
        } else {
            List<TransmitterInfo> transmitterInfoList = new ArrayList<TransmitterInfo>();
            transmitterInfoList.add(transmitterInfo);
            transmitterInfoMap.put(port, transmitterInfoList);
        }
    }

    /**
     * Check if the transmitter group is a stand-alone transmitter.
     * 
     * Considered stand-alone if the the list is a size of one and the mnemonic
     * is the same as the group name.
     * 
     * @return True if stand-alone. False otherwise.
     */
    public boolean isStandalone() {
        if ((transmitterInfoMap != null) && (transmitterInfoMap.size() == 1)) {
            if (transmitterInfoMap.get(transmitterInfoMap.firstKey()).get(0)
                    .getMnemonic().equals(this.groupName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the lowest DAC port number associated with a transmitter.
     * 
     * @return
     */
    public Integer getLowestDacPortNumber() {
        return transmitterInfoMap.firstKey();
    }
}
