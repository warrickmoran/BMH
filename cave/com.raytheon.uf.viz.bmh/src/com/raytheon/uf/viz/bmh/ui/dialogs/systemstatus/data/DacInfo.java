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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Class containing the information for the DAC and the associated transmitter
 * groups.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Added addition DAC information.
 * Dec 09, 2014  #3910     lvenable     Added null check for DAC port number.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class DacInfo {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DacInfo.class);

    /** DAC Id. */
    private Integer dacId;

    /** DAC name. */
    private String dacName;

    /** DAC address. */
    private String dacAddress;

    /** Power supply unit one voltage. */
    private Double psu1Voltage = 0.0;

    /** Power supply unit two voltage. */
    private Double psu2Voltage = 0.0;

    /** DAC buffer size. */
    private Integer bufferSize = 0;

    /** List of transmitter group information. */
    private SortedMap<Integer, TransmitterGrpInfo> transmitterGrpInfoMap = new TreeMap<Integer, TransmitterGrpInfo>();

    /**
     * Constructor.
     */
    public DacInfo() {

    }

    public Integer getDacId() {
        return dacId;
    }

    public void setDacId(Integer dacId) {
        this.dacId = dacId;
    }

    public String getDacName() {
        return dacName;
    }

    public void setDacName(String dacName) {
        this.dacName = dacName;
    }

    /**
     * Get a list of Transmitter Group information.
     * 
     * @return List of Transmitter Group information.
     */
    public List<TransmitterGrpInfo> getTransmitterGrpInfoList() {
        List<TransmitterGrpInfo> tgList = new ArrayList<TransmitterGrpInfo>();

        for (Integer i : transmitterGrpInfoMap.keySet()) {
            tgList.add(transmitterGrpInfoMap.get(i));
        }

        return tgList;
    }

    /**
     * Add a transmitter group info object to the data map.
     * 
     * @param transmitterGrpInfo
     */
    public void addTransmitterGroupInfo(TransmitterGrpInfo transmitterGrpInfo) {

        /*
         * When inserting the data into the map, use the lowest DAC port number
         * of all the transmitters in the transmitter group. If the dac port is
         * null then don't add it to the transmitter group information map.
         */
        Integer portNumber = transmitterGrpInfo.getLowestDacPortNumber();

        if (portNumber != null) {
            transmitterGrpInfoMap.put(portNumber, transmitterGrpInfo);
        } else {
            statusHandler
                    .warn("The lowest DAC port number for a transmitter for Transmitter Goup "
                            + transmitterGrpInfo.getGroupName()
                            + " was null.  There are no transmitters for this transmitter group.");
        }
    }

    public String getDacAddress() {
        return dacAddress;
    }

    public void setDacAddress(String dacAddress) {
        this.dacAddress = dacAddress;
    }

    public Double getPsu1Voltage() {
        return psu1Voltage;
    }

    public void setPsu1Voltage(Double psu1Voltage) {
        if (psu1Voltage == null) {
            this.psu1Voltage = Double.NaN;
            return;
        }
        this.psu1Voltage = psu1Voltage;
    }

    public Double getPsu2Voltage() {
        return psu2Voltage;
    }

    public void setPsu2Voltage(Double psu2Voltage) {
        if (psu2Voltage == null) {
            this.psu2Voltage = Double.NaN;
            return;
        }
        this.psu2Voltage = psu2Voltage;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
}
