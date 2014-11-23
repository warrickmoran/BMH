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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.DacVoiceStatus;

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
 * 
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
            Map<String, DacHardwareStatusNotification> dacStatus) {

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
        Set<Integer> updatedDac = new HashSet<>();

        for (TransmitterGroup tg : transmitterGroups) {
            TransmitterGrpInfo tgi = createTransGroupInfo(tg);

            Integer tgDac = tg.getDac();
            if (tgDac != null) {
                DacInfo di = dtsd.getDacInfo(tgDac);
                if (di != null) {
                    di.addTransmitterGroupInfo(tgi);

                    // If the DAC info hasn't been updated then add the DAC
                    // status information.
                    if (updatedDac.contains(tgDac) == false) {
                        DacHardwareStatusNotification dhsn = dacStatus.get(tg
                                .getName());

                        // Check for a silence alarm and flag it so the
                        // transmitter group and be set
                        boolean silence = false;
                        DacVoiceStatus[] dvsArray = dhsn.getVoiceStatus();

                        for (DacVoiceStatus dvs : dvsArray) {
                            if (dvs != DacVoiceStatus.IP_AUDIO) {
                                silence = true;
                                break;
                            }
                        }
                        tgi.setSilenceAlarm(silence);

                        di.setPsu1Voltage(dhsn.getPsu1Voltage());
                        di.setPsu2Voltage(dhsn.getPsu2Voltage());
                        di.setBufferSize(dhsn.getBufferSize());
                        updatedDac.add(tgDac);
                    }
                } else {
                    dtsd.addTranmitterWithNoDac(tgi);
                }
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
        tgi.setDisabledSilenceAlarm(tg.getSilenceAlarm());
        tgi.setGroupName(tg.getName());

        List<Transmitter> transmitters = tg.getTransmitterList();

        for (Transmitter t : transmitters) {
            TransmitterInfo ti = new TransmitterInfo();
            ti.setId(t.getId());
            ti.setName(t.getName());
            ti.setMnemonic(t.getMnemonic());
            ti.setCallSign(t.getCallSign());
            ti.setDacPort(t.getDacPort());
            ti.setTxStatus(t.getTxStatus());

            tgi.addTransmitterInfo(ti);
        }

        return tgi;
    }

    /**
     * TODO : REMOVE WHEN TESTING IS DONE.
     * 
     * Print the data (for testing).
     */
    public void printData() {

        Map<Integer, DacInfo> dacInfoMap = dtsd.getDacInfoMap();

        for (Integer i : dacInfoMap.keySet()) {
            System.out.println("****** DAC Information ********");
            DacInfo di = dacInfoMap.get(i);
            System.out.println("DAC id     : " + di.getDacId());
            System.out.println("DAC name   : " + di.getDacName());
            System.out.println("DAC address: " + di.getDacAddress());

            List<TransmitterGrpInfo> tgInfoList = di
                    .getTransmitterGrpInfoList();
            for (TransmitterGrpInfo tgi : tgInfoList) {
                System.out.println("\t****** Transmitter Group Info ********");
                System.out.println("\t ID          :" + tgi.getId());
                System.out.println("\t Group Name  :" + tgi.getGroupName());
                System.out.println("\t Silent Alarm:"
                        + tgi.isDisabledSilenceAlarm());

                Map<Integer, List<TransmitterInfo>> transmitterInfoMap = tgi
                        .getTransmitterInfoMap();
                for (Integer mapKey : transmitterInfoMap.keySet()) {
                    System.out.println("\t\t****** Transmitter Info ********");

                    System.out.println("\t\t Key :" + mapKey);
                    List<TransmitterInfo> tiList = transmitterInfoMap
                            .get(mapKey);

                    for (TransmitterInfo ti : tiList) {
                        System.out.println("\t\t ID        :" + ti.getId());
                        System.out.println("\t\t Name      :" + ti.getName());
                        System.out.println("\t\t Mnemonic  :"
                                + ti.getMnemonic());
                        System.out.println("\t\t Call Sign :"
                                + ti.getCallSign());
                        System.out
                                .println("\t\t DAC Port  :" + ti.getDacPort());
                        System.out.println("\t\t Ts Status :"
                                + ti.getTxStatus().name());
                        System.out.println();
                    }
                }
            }
        }

        List<TransmitterGrpInfo> xmitWithNoDAC = dtsd
                .getNoDacTransGrpInfoList();
        System.out.println("\n\n****** Transmitters with no DACs ********");
        for (TransmitterGrpInfo tgi : xmitWithNoDAC) {
            System.out.println("\t****** Transmitter Group Info ********");
            System.out.println("ID          :" + tgi.getId());
            System.out.println("Group Name  :" + tgi.getGroupName());
            System.out.println("Silent Alarm:" + tgi.isDisabledSilenceAlarm());

            System.out.println("\t****** Transmitter Info ********");
            Map<Integer, List<TransmitterInfo>> transmitterInfoMap = tgi
                    .getTransmitterInfoMap();
            for (Integer mapKey : transmitterInfoMap.keySet()) {

                List<TransmitterInfo> tiList = transmitterInfoMap.get(mapKey);

                for (TransmitterInfo ti : tiList) {
                    System.out.println("\t ID        :" + ti.getId());
                    System.out.println("\t Name      :" + ti.getName());
                    System.out.println("\t Mnemonic  :" + ti.getMnemonic());
                    System.out.println("\t Call Sign :" + ti.getCallSign());
                    System.out.println("\t DAC Port  :" + ti.getDacPort());
                    System.out.println("\t Ts Status :"
                            + ti.getTxStatus().name());
                }
            }
        }
    }
}
