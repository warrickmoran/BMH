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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.ButtonImageCreator;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListComp;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.LiveBroadcastRecordPlaybackDlg;

/**
 * Dialog used to initiate live broadcasts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2014 3808       bkowal      Initial creation
 * Nov 21, 2014 3845       bkowal      Use BLBroadcastSettingsBuilder
 * Feb 05, 2015 3743       bsteffen    Allow subclasses to override loading of available groups.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastLiveDlg extends AbstractBMHDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BroadcastLiveDlg.class);

    private final TransmitterDataManager tdm = new TransmitterDataManager();

    private CheckScrollListComp sameTransmitters;

    private Button transmitBtn;

    protected Map<String, TransmitterGroup> transmitterGroupLookupMap;

    public BroadcastLiveDlg(Map<AbstractBMHDialog, String> map,
            Shell parentShell) {
        super(map, DlgInfo.BROADCAST_LIVE.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        this.setText(DlgInfo.BROADCAST_LIVE.getTitle());

        this.createTransmitterSelectionCheckList();
        this.createTransmitButton();
    }

    private void createTransmitterSelectionCheckList() {
        CheckListData cld = this.buildTransmitterChecklist();

        sameTransmitters = new CheckScrollListComp(this.shell,
                "Transmitter Groups: ", cld, false, 100, 165);
    }

    private void createTransmitButton() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        transmitBtn = new Button(this.shell, SWT.PUSH);
        transmitBtn.setLayoutData(gd);

        ButtonImageCreator bic = new ButtonImageCreator(shell);

        FontData fd = transmitBtn.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.setHeight(20);
        bic.setFontData(fd);

        Point imageWidthHeight = new Point(220, 35);

        Image transmitImg = bic.generateImage(imageWidthHeight.x,
                imageWidthHeight.y, "Transmit", new RGB(0, 235, 0));
        transmitBtn.setImage(transmitImg);
        transmitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTransmitAction();
            }
        });
    }

    protected CheckListData buildTransmitterChecklist() {
        CheckListData cld = new CheckListData();

        List<TransmitterGroup> transmitterGroups = null;
        try {
            transmitterGroups = this.tdm.getTransmitterGroups();
        } catch (Exception e) {
            statusHandler
                    .error("Failed to retrieve the transmitter groups.", e);
            return cld;
        }

        if (transmitterGroups == null || transmitterGroups.isEmpty()) {
            /*
             * System is not configured.
             */
            return cld;
        }

        this.transmitterGroupLookupMap = new HashMap<>(
                transmitterGroups.size(), 1.0f);
        for (TransmitterGroup transmitterGrp : transmitterGroups) {
            if (transmitterGrp.isEnabled() == false) {
                continue;
            }
            cld.addDataItem(transmitterGrp.getName(), false);
            transmitterGroupLookupMap.put(transmitterGrp.getName(),
                    transmitterGrp);
        }

        return cld;
    }

    private void handleTransmitAction() {
        if (this.validateSelections() == false) {
            return;
        }

        /* Build the broadcast settings. */
        BLBroadcastSettingsBuilder settingsBuilder = new BLBroadcastSettingsBuilder(
                this.getSelectedTransmitterGroups());

        /*
         * alert the user that they are about to start a live stream
         * interrupting all audio currently playing on the transmitter.
         */
        int option = DialogUtility
                .showMessageBox(
                        this.shell,
                        SWT.ICON_WARNING | SWT.YES | SWT.NO,
                        "Broadcast Live",
                        "You are about go to live. You will interrupt all broadcasts that are currently playing on the selected transmitters. Would you like to continue?");
        if (option == SWT.NO) {
            return;
        }

        LiveBroadcastRecordPlaybackDlg dlg = new LiveBroadcastRecordPlaybackDlg(
                this.shell, RecordPlaybackDlg.INDETERMINATE_PROGRESS,
                settingsBuilder);
        dlg.open();
    }

    private boolean validateSelections() {
        /*
         * Verify that at least one transmitter has been selected.
         */
        if (this.sameTransmitters.getCheckedItems().getCheckedItems().size() <= 0) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Broadcast Live - Transmitters",
                            "No transmitters have been selected. At least one transmitter must be selected.");
            return false;
        }

        return true;
    }

    private List<TransmitterGroup> getSelectedTransmitterGroups() {
        List<String> selectedTransmitterGrpNames = this.sameTransmitters
                .getCheckedItems().getCheckedItems();
        List<TransmitterGroup> selectedTransmitterGroups = new ArrayList<>(
                selectedTransmitterGrpNames.size());
        for (String name : selectedTransmitterGrpNames) {
            selectedTransmitterGroups.add(this.transmitterGroupLookupMap
                    .get(name));
        }

        return selectedTransmitterGroups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog#okToClose()
     */
    @Override
    public boolean okToClose() {
        return true;
    }
}