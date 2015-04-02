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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.systemstatus.SystemStatusMonitor;

/**
 * This class will take all of the DAC and Transmitter Group information and
 * build a data structure that will organize the data for displaying on the
 * status monitor dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Added addition status for reporting.
 * Dec 01, 2014  #3287     lvenable     Added null check.
 * Jan 08, 2015  3821      bsteffen     Rename silenceAlarm to deadAirAlarm
 * Feb 09, 2015  4095      bsteffen     Remove Transmitter Name.
 * Mar 11, 2015  #4186     bsteffen     Check system status monitor for silence instead of dac hardware status.
 * Apr 01, 2015  4219      bsteffen     Allow multiple transmitter groups with no ports assigned.
 * Apr 02, 2015  4248      rjpeter      Use PositionComparator.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class StatusDataManager {

    /** DAC and Transmitter status data object. */
    private DacTransmitterStatusData dtsd = null;

    /**
     * Constructor.
     */
    public StatusDataManager() {
    }

    /**
     * Create the DAC and Transmitter information object using the list of DACs
     * and Transmitter Groups passed in.
     * 
     * @param dacs
     *            List of DACs.
     * @param transmitterGroups
     *            List of Transmitter Groups.
     * @return DAC/Transmitter status data object.
     */
    public DacTransmitterStatusData createDacTransmitterStatusData(
            List<Dac> dacs, List<TransmitterGroup> transmitterGroups,
            SystemStatusMonitor statusMonitor) {
        Collections.sort(transmitterGroups, new PositionComparator());

        dtsd = new DacTransmitterStatusData();

        /*
         * Create the DAC Info data objects.
         */
        for (Dac d : dacs) {
            DacInfo di = new DacInfo();
            di.setDacId(d.getId());
            di.setDacName(d.getName());
            di.setDacAddress(d.getAddress());
            dtsd.addDacInfo(d.getId(), di);
        }

        /*
         * Loop over the transmitter groups and create transmitter group info
         * objects. Add each transmitter group info object to the associated DAC
         * or put them in a separate list if they are not associated with a DAC.
         */
        Set<Integer> updatedDac = new HashSet<>(dacs.size(), 1.0f);

        Map<String, DacHardwareStatusNotification> dacStatus = statusMonitor
                .getDacStatus();

        for (TransmitterGroup tg : transmitterGroups) {
            TransmitterGrpInfo tgi = createTransGroupInfo(tg);
            if (tgi.isEmpty()) {
                continue;
            }
            Integer tgDac = tg.getDac();
            if (tgDac != null) {
                DacInfo di = dtsd.getDacInfo(tgDac);
                if (di != null) {
                    di.addTransmitterGroupInfo(tgi);

                    // If the DAC info hasn't been updated then add the DAC
                    // status information.
                    DacHardwareStatusNotification dhsn = dacStatus.get(tg
                            .getName());
                    if ((dhsn != null) && (updatedDac.contains(tgDac) == false)) {
                        di.setPsu1Voltage(dhsn.getPsu1Voltage());
                        di.setPsu2Voltage(dhsn.getPsu2Voltage());
                        updatedDac.add(tgDac);
                    }
                } else {
                    dtsd.addTranmitterWithNoDac(tgi);
                }
                tgi.setSilenceAlarm(statusMonitor.isTransmitterGroupSilent(
                        di.getDacAddress(), tg.getName()));
            } else {
                dtsd.addTranmitterWithNoDac(tgi);
            }
        }

        return dtsd;
    }

    /**
     * Create a transmitter info file using the data from the transmitter group.
     * 
     * @param tg
     *            Transmitter Group
     * @return Transmitter group info.
     */
    private TransmitterGrpInfo createTransGroupInfo(TransmitterGroup tg) {
        TransmitterGrpInfo tgi = new TransmitterGrpInfo();
        tgi.setId(tg.getId());
        tgi.setDisabledSilenceAlarm(Boolean.FALSE.equals(tg.getDeadAirAlarm()));
        tgi.setGroupName(tg.getName());

        List<Transmitter> transmitters = tg.getTransmitterList();
        Collections.sort(transmitters, new PositionComparator());

        for (Transmitter t : transmitters) {
            TransmitterInfo ti = new TransmitterInfo();
            ti.setId(t.getId());
            ti.setName(t.getLocation());
            ti.setMnemonic(t.getMnemonic());
            ti.setCallSign(t.getCallSign());
            ti.setDacPort(t.getDacPort());
            ti.setTxStatus(t.getTxStatus());

            tgi.addTransmitterInfo(ti);
        }

        return tgi;
    }

}
