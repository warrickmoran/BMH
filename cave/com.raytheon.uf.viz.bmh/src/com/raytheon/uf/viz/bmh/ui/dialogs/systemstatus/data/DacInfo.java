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
 * Jan 22, 2015  3995      rjpeter      Remove repeat message when DAC has no transmitters.
 * Jan 27, 2015  4029      bkowal       Removed buffer size.
 * Jan 29, 2015  4029      bkowal       Added {@link DAC_VOLTAGE}.
 * Apr 01, 2015  4219      bsteffen     Allow multiple transmitter groups with no ports assigned.
 * Dec 01, 2015  5113      bkowal       Report when a BMH Dac and a DAC are no longer in sync.
 * May 09, 2016  5630      rjpeter      Remove DAC Sync.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class DacInfo {

    public static enum DAC_VOLTAGE {
        OK("OK"), LOW_VOLTAGE("Low Voltage"), NO_READING("No Reading");

        private final String text;

        private DAC_VOLTAGE(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    /** DAC Id. */
    private Integer dacId;

    /** DAC name. */
    private String dacName;

    /** DAC address. */
    private String dacAddress;

    /** Power supply unit one voltage. */
    private DAC_VOLTAGE psu1Voltage = DAC_VOLTAGE.NO_READING;

    /** Power supply unit two voltage. */
    private DAC_VOLTAGE psu2Voltage = DAC_VOLTAGE.NO_READING;

    /** List of transmitter group information. */
    private final List<TransmitterGrpInfo> transmitterGrpInfoList = new ArrayList<>();

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
        return transmitterGrpInfoList;
    }

    /**
     * Add a transmitter group info object to the data map.
     * 
     * @param transmitterGrpInfo
     */
    public void addTransmitterGroupInfo(TransmitterGrpInfo transmitterGrpInfo) {
        transmitterGrpInfoList.add(transmitterGrpInfo);
        /*
         * List should be sorted by lowest dac port and allow duplicates and
         * allow null ports, existing data structures don't support this so list
         * MUST be sorted on insertion.
         */
        Collections.sort(transmitterGrpInfoList,
                new Comparator<TransmitterGrpInfo>() {

                    @Override
                    public int compare(TransmitterGrpInfo o1,
                            TransmitterGrpInfo o2) {
                        Integer p1 = o1.getLowestDacPortNumber();
                        Integer p2 = o2.getLowestDacPortNumber();
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

    public String getDacAddress() {
        return dacAddress;
    }

    public void setDacAddress(String dacAddress) {
        this.dacAddress = dacAddress;
    }

    public DAC_VOLTAGE getPsu1Voltage() {
        return psu1Voltage;
    }

    public void setPsu1Voltage(Double psu1Voltage) {
        this.psu1Voltage = this.determineVoltageStatus(psu1Voltage);
    }

    public DAC_VOLTAGE getPsu2Voltage() {
        return psu2Voltage;
    }

    public void setPsu2Voltage(Double psu2Voltage) {
        this.psu2Voltage = this.determineVoltageStatus(psu2Voltage);
    }

    private DAC_VOLTAGE determineVoltageStatus(Double voltage) {
        if ((voltage == null) || voltage.isNaN()) {
            return DAC_VOLTAGE.NO_READING;
        }

        if (voltage < 8.) {
            return DAC_VOLTAGE.LOW_VOLTAGE;
        }

        return DAC_VOLTAGE.OK;
    }
}
