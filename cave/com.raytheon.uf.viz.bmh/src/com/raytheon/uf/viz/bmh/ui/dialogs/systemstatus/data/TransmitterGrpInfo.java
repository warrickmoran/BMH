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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
 * Nov 23, 2014  #3287     lvenable     Added silent alarm flag.
 * Dec 09, 2014  #3910     lvenable     Added check for empty map.
 * Apr 01, 2015  4219      bsteffen     Allow multiple transmitter groups with no ports assigned.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class TransmitterGrpInfo {

    /** Disabled silence alarm flag. */
    private boolean disabledSilenceAlarm = false;

    /** Silence alarm flag. */
    private boolean silenceAlarm = false;

    /** Group name. */
    private String groupName;

    /** ID */
    private Integer id;

    /** List of transmitter information. */
    private List<TransmitterInfo> transmitterInfoList = new ArrayList<>();

    /**
     * Constructor.
     */
    public TransmitterGrpInfo() {

    }

    public boolean isDisabledSilenceAlarm() {
        return disabledSilenceAlarm;
    }

    public void setDisabledSilenceAlarm(boolean silenceAlarm) {
        this.disabledSilenceAlarm = silenceAlarm;
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

    public List<TransmitterInfo> getTransmitterInfoList() {
        return transmitterInfoList;
    }

    /**
     * Add the transmitter information to the map.
     * 
     * @param transmitterInfo
     */
    public void addTransmitterInfo(TransmitterInfo transmitterInfo) {
        transmitterInfoList.add(transmitterInfo);
        Collections.sort(transmitterInfoList,
                new Comparator<TransmitterInfo>() {

                    @Override
                    public int compare(TransmitterInfo o1, TransmitterInfo o2) {
                        Integer p1 = o1.getDacPort();
                        Integer p2 = o2.getDacPort();
                        if (p1 == null) {
                            if (p2 == null) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else if (p2 == null) {
                            return -1;
                        } else {
                            return p1.compareTo(p2);
                        }
                    }

                });
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
        if (transmitterInfoList != null && transmitterInfoList.size() == 1) {
            return transmitterInfoList.get(0).getMnemonic()
                    .equals(this.groupName);
        } else {
            return false;
        }
    }

    /**
     * @return true if no transmitters are in this group, otherwise false.
     */
    public boolean isEmpty() {
        return transmitterInfoList.isEmpty();
    }

    /**
     * Get the lowest DAC port number associated with a transmitter.
     * 
     * @return
     */
    public Integer getLowestDacPortNumber() {
        if (transmitterInfoList == null || transmitterInfoList.isEmpty()) {
            return null;
        } else {
            return transmitterInfoList.get(0).getDacPort();
        }
    }
}
