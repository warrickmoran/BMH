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
package com.raytheon.uf.viz.bmh.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Used to manage Transmitter silence alarms. Keeps track of the current state
 * of each silence alarm to ensure that only the minimum set of Transmitters are
 * actually updated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2015 4141       bkowal      Initial creation
 * Mar 31, 2015 4248       rjpeter     Removed TransmitterGroupPositionComparator.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SilenceAlarmDelegate {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SilenceAlarmDelegate.class);

    private final TransmitterDataManager tdm = new TransmitterDataManager();

    private final Map<String, TransmitterGroup> currentStateMap = new HashMap<>();

    /**
     * 
     */
    public SilenceAlarmDelegate(final Shell parentShell) {
        this.initialize(parentShell);
    }

    private void initialize(Shell parentShell) {
        CheckListData cld = this.getTransmitterData();
        if (cld == null) {
            return;
        }

        CheckScrollListDlg silenceAlarmDialog = new CheckScrollListDlg(
                parentShell, DlgInfo.DISABLES_SILENCE_ALARM.getTitle(),
                "Select Transmitter to Disable:", cld, true);
        silenceAlarmDialog.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if ((returnValue == null)
                        || ((returnValue instanceof CheckListData) == false)) {
                    return;
                }

                updateTransmitterData((CheckListData) returnValue);
            }
        });
        silenceAlarmDialog.open();
    }

    private CheckListData getTransmitterData() {
        List<TransmitterGroup> transmitterGrps = null;

        try {
            transmitterGrps = tdm
                    .getTransmitterGroups(new PositionComparator());
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return null;
        }

        CheckListData cld = new CheckListData();
        for (TransmitterGroup tg : transmitterGrps) {
            cld.addDataItem(tg.getName(), !tg.getDeadAirAlarm());
            /*
             * Keep track of the current Transmitter state.
             */
            this.currentStateMap.put(tg.getName(), tg);
        }

        return cld;
    }

    private void updateTransmitterData(CheckListData cld) {
        Map<String, Boolean> lastTransmitterStateMap = cld.getDataMap();

        /*
         * Determine which Transmitter Groups need to be updated.
         */
        List<TransmitterGroup> groupsToUpdate = new ArrayList<>(
                lastTransmitterStateMap.size());
        for (String tgName : lastTransmitterStateMap.keySet()) {
            TransmitterGroup tg = this.currentStateMap.get(tgName);

            if (tg == null) {
                continue;
            }

            if (tg.getDeadAirAlarm() == !(lastTransmitterStateMap.get(tgName))) {
                /*
                 * the transmitter has not been updated.
                 */
                continue;
            }

            /*
             * the transmitter will need to be updated.
             */
            tg.setDeadAirAlarm(!(lastTransmitterStateMap.get(tgName)));
            groupsToUpdate.add(tg);
        }

        if (groupsToUpdate.isEmpty()) {
            return;
        }

        try {
            this.tdm.saveTransmitterGroups(groupsToUpdate);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to save updated silence alarm settings.", e);
        }
    }
}