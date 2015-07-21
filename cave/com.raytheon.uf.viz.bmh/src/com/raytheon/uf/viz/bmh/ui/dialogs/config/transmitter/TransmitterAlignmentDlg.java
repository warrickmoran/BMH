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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.TransmitterAlignmentException;
import com.raytheon.uf.common.bmh.broadcast.AbstractOnDemandBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.broadcast.TransmitterMaintenanceCommand;
import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupIdentifier;
import com.raytheon.uf.common.bmh.request.MaintenanceMessageRequest;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.ScaleSpinnerComp;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * The transmitter alignment dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2014    3630    mpduff      Initial creation
 * Nov 05, 2014    3630    bkowal      Initial implementation of run test.
 * Nov 10, 2014    3630    bkowal      Build the TransmitterAlignmentTestCommand.
 * Nov 11, 2014    3413    rferrel     Use DlgInfo to get title.
 * Nov 15, 2014    3630    bkowal      Run the test and report the result.
 * Nov 21, 2014    3845    bkowal      Updates to {@link AbstractOnDemandBroadcastMessage}.
 * Dec 09, 2014    3894    bkowal      Keep track of which {@link TransmitterGroup}s were disabled
 *                                     using the dialog.
 * Dec 12, 2014 3603       bsteffen    Move backgrounded execution out for reuse by transfer tones.
 * Mar 02, 2015    3962    rferrel     Changes for MAINT status.
 * Mar 31, 2015 4248       rjpeter     Removed TransmitterGroupPositionComparator.
 * Apr 09, 2015    4364    bkowal      Set the maintenance broadcast timeout.
 * Apr 14, 2015    4394    bkowal      Alignment Tests can now only be completed when the Transmitter
 *                                     is in Maintenance mode.
 * Apr 24, 2015    4394    bkowal      Eliminated MaintenanceMessageResponse.
 * Apr 29, 2015    4394    bkowal      Include the transmitter group in the
 *                                     {@link MaintenanceMessageRequest}.
 * Jun 05, 2015 4490       rjpeter     Updated constructor.
 * Jul 01, 2015 4602       rjpeter     Use specific dataport.
 * Jul 08, 2015 4636       bkowal      Support setting multiple decibel levels. Support transfer tone
 *                                     alignment testing.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Jul 15, 2015 4649       rferrel     When saving Decible Levels update the Target Decible Level.
 *                                     Duration default is 10 seconds and only the user can change its value.
 * Jul 16, 2015 4636       bkowal      Updated to use volume sliders.
 * Jul 17, 2015 4636       bkowal      Automatically updates internal data structures in response to
 *                                     Transmitter Group updates.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterAlignmentDlg extends AbstractBMHDialog implements
        IVolumeChangeListener, INotificationObserver {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TransmitterAlignmentDlg.class);

    /** Constant */
    private final String STATUS_PREFIX = "Transmitter is ";

    /** List widget of transmitter groups */
    private List transmitterList;

    /** Map of TransmitterGroup name -> TransmitterGroup object */
    private final Map<String, TransmitterGroup> transmitterGroupNameMap = new HashMap<>();

    /** Data access manager */
    private final TransmitterDataManager dataManager = new TransmitterDataManager();

    /** Transmitter status label */
    private Label statusLbl;

    /** Selected TransmitterGroup object */
    protected TransmitterGroup selectedTransmitterGrp;

    private TransmitterVolumeComp audioVolume;

    private TransmitterVolumeComp sameVolume;

    private TransmitterVolumeComp alertVolume;

    private TransmitterVolumeComp tfr1800Volume;

    private TransmitterVolumeComp tfr2400Volume;

    private Button saveButton;

    /** Duration ScaleSpinner composite */
    private ScaleSpinnerComp durScaleComp;

    /** Button to change toggle status between ENABLED and MAINT. */
    private Button changeStatusBtn;

    private final String maintStr = "Maintenance";

    private final String enableStr = "Enable";

    private Button sameRdo;

    private Button alertRdo;

    private Button textRdo;

    private Button transfer18Rdo;

    private Button transfer24Rdo;

    /** "Run Test" Button **/
    private Button testBtn;

    /**
     * Keep track of which {@link TransmitterGroup}s have been disabled via the
     * dialog.
     **/
    java.util.List<TransmitterGroup> transmittersDisabledByDialog = new ArrayList<>();

    /**
     * Used to lock knowledge of the known current state of all known
     * {@link TransmitterGroup}s. Lock is applied both when using the knowledge
     * and when updating the knowledge.
     */
    private final Object transmitterGrpLock = new Object();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            The parent shell
     */
    public TransmitterAlignmentDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                | CAVE.DO_NOT_BLOCK);
        setText(DlgInfo.TRANSMITTER_ALIGNMENT.getTitle());
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite left = new Composite(mainComp, SWT.NONE);
        left.setLayout(gl);
        left.setLayoutData(gd);

        createTransmitterList(left);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite right = new Composite(mainComp, SWT.NONE);
        right.setLayout(gl);
        right.setLayoutData(gd);

        createTransmitterStatusGroup(right);
        createDbLevelGroup(right);

        createComp(right);

        createBottomButtons(mainComp);

        populateTransmitters();
        synchronized (transmitterGrpLock) {
            populate();
        }
    }

    @Override
    protected void opened() {
        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);
    }

    @Override
    protected void disposed() {
        super.disposed();
        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);
    }

    private void createTransmitterList(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Group transGrp = new Group(comp, SWT.BORDER);
        transGrp.setText(" Transmitters ");
        transGrp.setLayout(gl);
        transGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 125;
        transmitterList = new List(transGrp, SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.SINGLE | SWT.BORDER);
        transmitterList.setLayoutData(gd);
        transmitterList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                synchronized (transmitterGrpLock) {
                    populate();
                }
            }
        });
    }

    private void createDbLevelGroup(Composite comp) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Group dbGroup = new Group(comp, SWT.BORDER);
        dbGroup.setText(" Transmitter Volume ");
        dbGroup.setLayout(gl);
        dbGroup.setLayoutData(gd);

        this.audioVolume = new TransmitterVolumeComp(dbGroup, "Audio Volume");
        this.audioVolume.setListener(this);
        this.sameVolume = new TransmitterVolumeComp(dbGroup, "SAME Volume");
        this.sameVolume.setListener(this);
        this.alertVolume = new TransmitterVolumeComp(dbGroup, "Alert Volume");
        this.alertVolume.setListener(this);
        this.tfr1800Volume = new TransmitterVolumeComp(dbGroup,
                "1800 Hz Volume");
        this.tfr1800Volume.setListener(this);
        this.tfr2400Volume = new TransmitterVolumeComp(dbGroup,
                "2400 Hz Volume");
        this.tfr2400Volume.setListener(this);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 80;
        saveButton = new Button(dbGroup, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(gd);
        saveButton.setEnabled(false);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                synchronized (transmitterGrpLock) {
                    handleSaveAction();
                }
            }
        });
    }

    private void handleSaveAction() {
        /*
         * Determine if any changes have actually been made.
         */
        if (this.dataIsCurrent()) {
            this.saveButton.setEnabled(false);
            DialogUtility.showMessageBox(shell, SWT.ICON_INFORMATION | SWT.OK,
                    "Save Transmitter Alignment",
                    "No changes have been made. There is nothing to Save.");
            return;
        }

        this.selectedTransmitterGrp.setAudioDBTarget(this.audioVolume
                .getCurrentDecibelValue());
        this.selectedTransmitterGrp.setSameDBTarget(this.sameVolume
                .getCurrentDecibelValue());
        this.selectedTransmitterGrp.setAlertDBTarget(this.alertVolume
                .getCurrentDecibelValue());
        this.selectedTransmitterGrp.setTransferLowDBTarget(this.tfr1800Volume
                .getCurrentDecibelValue());
        this.selectedTransmitterGrp.setTransferHighDBTarget(this.tfr2400Volume
                .getCurrentDecibelValue());

        try {
            this.dataManager.saveTransmitterGroup(this.selectedTransmitterGrp);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to update decibel levels for Transmitter Group: "
                            + this.selectedTransmitterGrp.getName() + ".", e);
            return;
        }

        this.saveButton.setEnabled(false);

        this.audioVolume.setCurrentDecibelVolume(this.selectedTransmitterGrp
                .getAudioDBTarget());
        this.sameVolume.setCurrentDecibelVolume(this.selectedTransmitterGrp
                .getSameDBTarget());
        this.alertVolume.setCurrentDecibelVolume(this.selectedTransmitterGrp
                .getAlertDBTarget());
        this.tfr1800Volume.setCurrentDecibelVolume(this.selectedTransmitterGrp
                .getTransferLowDBTarget());
        this.tfr2400Volume.setCurrentDecibelVolume(this.selectedTransmitterGrp
                .getTransferHighDBTarget());
    }

    private void createTransmitterStatusGroup(Composite comp) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Group statusGrp = new Group(comp, SWT.BORDER);
        statusGrp.setText(" Transmitter Status ");
        statusGrp.setLayout(gl);
        statusGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalAlignment = SWT.LEFT;
        gd.widthHint = 285;
        statusLbl = new Label(statusGrp, SWT.NONE);
        statusLbl.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite btnComp = new Composite(statusGrp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        int btnWidth = 130;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;

        changeStatusBtn = new Button(btnComp, SWT.PUSH);
        changeStatusBtn.setText(maintStr);
        changeStatusBtn.setLayoutData(gd);
        changeStatusBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeStatusAction();
            }
        });
    }

    private void changeStatusAction() {
        if (changeStatusBtn.getText().equals(maintStr)) {
            int answer = DialogUtility.showMessageBox(getShell(),
                    SWT.ICON_INFORMATION | SWT.YES | SWT.NO, "Disable Group",
                    "Are you sure you want to do maintenance on Transmitter Group "
                            + selectedTransmitterGrp.getName() + "?");
            if (answer != SWT.YES) {
                return;
            }

            transmitterGroupMaintenanceMode();
            changeStatusBtn.setText(enableStr);
        } else {
            int answer = DialogUtility.showMessageBox(getShell(),
                    SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                    "Enable Transmitter",
                    "Are you sure you want to enable Transmitter Group "
                            + selectedTransmitterGrp.getName() + "?");
            if (answer != SWT.YES) {
                return;
            }

            enableTransmitterGroup();
            changeStatusBtn.setText(maintStr);
        }
    }

    private void createComp(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);

        Group group = new Group(comp, SWT.BORDER);
        group.setLayout(gl);
        group.setLayoutData(gd);
        group.setText(" Level Test ");

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite rdoComp = new Composite(group, SWT.NONE);
        rdoComp.setLayout(gl);
        rdoComp.setLayoutData(gd);

        textRdo = new Button(rdoComp, SWT.RADIO);
        textRdo.setText("Audio");
        textRdo.setSelection(true);

        sameRdo = new Button(rdoComp, SWT.RADIO);
        sameRdo.setText("SAME");

        alertRdo = new Button(rdoComp, SWT.RADIO);
        alertRdo.setText("Alert");

        transfer18Rdo = new Button(rdoComp, SWT.RADIO);
        transfer18Rdo.setText("Transfer 1800 Hz");

        transfer24Rdo = new Button(rdoComp, SWT.RADIO);
        transfer24Rdo.setText("Transfer 2400 Hz");

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite durComp = new Composite(group, SWT.NONE);
        durComp.setLayout(gl);
        durComp.setLayoutData(gd);

        Label durLbl = new Label(durComp, SWT.NONE);
        durLbl.setText("Duration (seconds):");

        durScaleComp = new ScaleSpinnerComp(durComp, 1, 30, "", 85, 25);
        durScaleComp.setSelectedValue(10);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.CENTER;
        testBtn = new Button(group, SWT.PUSH);
        testBtn.setText(" Run Test ");
        testBtn.setLayoutData(gd);
        testBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                synchronized (transmitterGrpLock) {
                    handleRunTest();
                }
            }
        });
    }

    private void createBottomButtons(Composite comp) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        Composite btnComp = new Composite(comp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setText("Close");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (okToClose()) {
                    close();
                }
            }
        });

    }

    /**
     * Populate the dialog for the selected transmitter
     */
    private void populate() {
        if (this.selectedTransmitterGrp != null) {
            /*
             * Determine if any changes have been made.
             */
            if (this.dataIsCurrent() == false) {
                int option = DialogUtility
                        .showMessageBox(this.shell, SWT.ICON_WARNING | SWT.YES
                                | SWT.NO, "Unsaved Changes",
                                "You have unsaved changes, would you like to discard them?");
                if (option != SWT.YES) {
                    /*
                     * Determine the index of the previously selected
                     * transmitter group and re-select it.
                     */
                    for (int i = 0; i < transmitterList.getItemCount(); i++) {
                        if (transmitterList.getItem(i).equals(
                                this.selectedTransmitterGrp.getName())) {
                            transmitterList.select(i);
                            break;
                        }
                    }
                    return;
                }
            }
        }

        if (transmitterList.getSelectionCount() > 0) {
            String grpName = transmitterList.getSelection()[0];
            selectedTransmitterGrp = transmitterGroupNameMap.get(grpName);

            this.audioVolume.enable();
            this.sameVolume.enable();
            this.alertVolume.enable();
            this.tfr1800Volume.enable();
            this.tfr2400Volume.enable();
            this.audioVolume.setCurrentDecibelVolume(selectedTransmitterGrp
                    .getAudioDBTarget());
            this.sameVolume.setCurrentDecibelVolume(selectedTransmitterGrp
                    .getSameDBTarget());
            this.alertVolume.setCurrentDecibelVolume(selectedTransmitterGrp
                    .getAlertDBTarget());
            this.tfr1800Volume.setCurrentDecibelVolume(selectedTransmitterGrp
                    .getTransferLowDBTarget());
            this.tfr2400Volume.setCurrentDecibelVolume(selectedTransmitterGrp
                    .getTransferHighDBTarget());

            if (selectedTransmitterGrp.isEnabled()) {
                this.statusLbl.setText(STATUS_PREFIX
                        + selectedTransmitterGrp.getName() + " "
                        + TxStatus.ENABLED.toString());
                this.updateLevelTestState(false);
                this.changeStatusBtn.setText(maintStr);
                this.changeStatusBtn.setEnabled(true);
            } else if (selectedTransmitterGrp.isMaint()) {
                this.statusLbl.setText(STATUS_PREFIX
                        + selectedTransmitterGrp.getName() + " "
                        + TxStatus.MAINT);
                this.updateLevelTestState(this
                        .isTransmitterGroupConfigured(this.selectedTransmitterGrp));
                this.changeStatusBtn.setText(enableStr);
                this.changeStatusBtn.setEnabled(true);
            } else {
                // Assume group DISABLED and disable the changeStatusBtn.
                this.statusLbl.setText(STATUS_PREFIX
                        + selectedTransmitterGrp.getName() + " "
                        + TxStatus.DISABLED.toString());
                this.updateLevelTestState(false);
                this.changeStatusBtn.setText(maintStr);
                this.changeStatusBtn
                        .setEnabled(this
                                .isTransmitterGroupConfigured(this.selectedTransmitterGrp));
            }
        } else {
            this.statusLbl.setText("<No Transmitter Selected>");
            this.audioVolume.disable();
            this.sameVolume.disable();
            this.alertVolume.disable();
            this.tfr1800Volume.disable();
            this.tfr2400Volume.disable();
            this.updateLevelTestState(false);
            this.changeStatusBtn.setEnabled(false);
        }
    }

    private boolean dataIsCurrent() {
        return this.audioVolume.dataChanged() == false
                && this.sameVolume.dataChanged() == false
                && this.alertVolume.dataChanged() == false
                && this.tfr1800Volume.dataChanged() == false
                && this.tfr2400Volume.dataChanged() == false;
    }

    /**
     * Populate the transmitter list box
     */
    private void populateTransmitters() {
        try {
            java.util.List<TransmitterGroup> transmitterGroupObjectList = dataManager
                    .getTransmitterGroups();
            Collections.sort(transmitterGroupObjectList,
                    new PositionComparator());
            String[] tNames = new String[transmitterGroupObjectList.size()];
            int idx = 0;
            for (TransmitterGroup tg : transmitterGroupObjectList) {
                tNames[idx] = tg.getName();
                idx++;
                transmitterGroupNameMap.put(tg.getName(), tg);
            }

            transmitterList.setItems(tNames);
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database.", e);
        }
        if (transmitterList.getItemCount() > 0) {
            transmitterList.select(0);
            String grpName = transmitterList.getItem(transmitterList
                    .getSelectionIndex());
            this.selectedTransmitterGrp = transmitterGroupNameMap.get(grpName);
        }
    }

    /**
     * In selected group change transmitter's with maintenance status to
     * enabled.
     */
    private void enableTransmitterGroup() {
        try {
            updateTransmitterGroup(TxStatus.ENABLED, selectedTransmitterGrp);
            this.updateLevelTestState(false);

            statusLbl.setText(STATUS_PREFIX + selectedTransmitterGrp.getName()
                    + " " + TxStatus.ENABLED.toString());
            this.transmittersDisabledByDialog
                    .remove(this.selectedTransmitterGrp);
        } catch (Exception e) {
            statusHandler.error("Error enabling Transmitter Group", e);
        }
    }

    /**
     * Update group's transmitters with given status.
     * 
     * 
     * @param status
     *            - update transmitters with this status
     * @param tg
     * @throws Exception
     */
    private void updateTransmitterGroup(TxStatus status, TransmitterGroup tg)
            throws Exception {
        for (Transmitter transmitter : tg.getTransmitterList()) {
            if (transmitter.getTxStatus() != status) {
                transmitter.setTxStatus(status);
                dataManager.saveTransmitter(transmitter);
            }
        }
    }

    /**
     * In the selected group change status of enabled transmitters to
     * maintenance.
     */
    private void transmitterGroupMaintenanceMode() {
        try {
            // Update status of transmitters changed to maintenance.
            updateTransmitterGroup(TxStatus.MAINT, selectedTransmitterGrp);
            dataManager.saveTransmitterGroup(selectedTransmitterGrp);
            this.updateLevelTestState(this
                    .isTransmitterGroupConfigured(this.selectedTransmitterGrp));
            statusLbl.setText(STATUS_PREFIX + selectedTransmitterGrp.getName()
                    + " " + TxStatus.MAINT.toString());
            this.transmittersDisabledByDialog.add(this.selectedTransmitterGrp);
        } catch (Exception e) {
            statusHandler.error("Error enabling Transmitter Group", e);
        }
    }

    private void updateLevelTestState(boolean enabled) {
        this.testBtn.setEnabled(enabled);
        this.sameRdo.setEnabled(enabled);
        this.alertRdo.setEnabled(enabled);
        this.textRdo.setEnabled(enabled);
        this.transfer18Rdo.setEnabled(enabled);
        this.transfer24Rdo.setEnabled(enabled);
    }

    /**
     * Determines if the specified {@link TransmitterGroup} has been fully
     * configured. A transmitter group is configured when it has been assigned
     * to both a dac and a dac port.
     * 
     * @param transmitterGroup
     *            the specified {@link TransmitterGroup}.
     * @return a boolean flag indicating whether or not the transmitter group
     *         has been configured.
     */
    public boolean isTransmitterGroupConfigured(
            TransmitterGroup transmitterGroup) {

        return (transmitterGroup != null)
                && (transmitterGroup.getDac() != null)
                && (transmitterGroup.getTransmitterList() != null)
                && (transmitterGroup.getTransmitterList().isEmpty() == false)
                && (transmitterGroup.getTransmitterList().get(0).getDacPort() != null);
    }

    private void handleRunTest() {
        TransmitterMaintenanceCommand command = null;
        try {
            command = this.buildCommand();
        } catch (TransmitterAlignmentException e) {
            statusHandler.error(
                    "Failed to configure the transmitter alignment test.", e);
            return;
        }
        this.updateLevelTestState(false);

        TransmitterMaintenanceThread.runAndReportResult(statusHandler,
                this.getShell(), command);

        this.updateLevelTestState(true);
    }

    private TransmitterMaintenanceCommand buildCommand()
            throws TransmitterAlignmentException {
        String audioLocation = null;

        try {
            audioLocation = this.constructMessageFile();
        } catch (Exception e) {
            throw new TransmitterAlignmentException(
                    "Failed to retrieve / generate audio required to complete the transmitter alignment test.",
                    e);
        }

        /*
         * Retrieve the {@link Dac} associated with the selected {@link
         * TransmitterGroup} to extract the required networking information.
         */
        DacDataManager dacDataManager = new DacDataManager();
        Dac dac = null;
        try {
            dac = dacDataManager.getDacById(this.selectedTransmitterGrp
                    .getDac());
        } catch (Exception e) {
            throw new TransmitterAlignmentException(
                    "Failed to retrieve the dac associated with id: "
                            + this.selectedTransmitterGrp.getDac(), e);
        }
        if (dac == null) {
            throw new TransmitterAlignmentException(
                    "Unable to find a dac associated with id: "
                            + this.selectedTransmitterGrp.getDac());
        }

        /*
         * Build a list of transmitter radios.
         */
        Set<Transmitter> transmitters = this.selectedTransmitterGrp
                .getTransmitters();
        int[] radios = new int[transmitters.size()];
        int rIndex = 0;
        for (Transmitter transmitter : transmitters) {
            if (transmitter.getDacPort() == null) {
                statusHandler.warn("No port has been assigned to transmitter: "
                        + transmitter.toString()
                        + ". It will be excluded from the test.");
                continue;
            }
            radios[rIndex] = transmitter.getDacPort();
            ++rIndex;
        }
        if (radios.length != rIndex) {
            radios = Arrays.copyOf(radios, rIndex);
        }
        Arrays.sort(radios);

        TransmitterMaintenanceCommand command = new TransmitterMaintenanceCommand();
        command.setMaintenanceDetails("Transmitter Alignment Test");
        command.setMsgSource(MSGSOURCE.VIZ);
        command.addTransmitterGroup(this.selectedTransmitterGrp);
        command.setDacHostname(dac.getAddress());
        /*
         * Make comms manager find an available data port when it receives this
         * request. There is not a pure mapping between available / unavailable
         * data ports that can be determined via the db. comms manager will
         * check the existing configuration to determine which data port(s), if
         * any, are available. If an available data port cannot be found, comms
         * manager will return a {@link TransmitterAlignmentException}.
         */
        if ((dac.getDataPorts() == null) || dac.getDataPorts().isEmpty()) {
            throw new TransmitterAlignmentException(
                    "No data ports have been assigned to dac " + dac.getName()
                            + ".");
        }
        command.setDataPort(dac.getDataPorts().get(radios[0] - 1));
        command.setRadios(radios);
        command.setDecibelTarget(this.getSelectedDecibelTarget());
        command.setInputAudioFile(audioLocation);
        command.setBroadcastDuration(this.durScaleComp.getSelectedValue());
        command.setBroadcastTimeout((int) (TransmitterMaintenanceThread.MAINTENANCE_TIMEOUT / TimeUtil.MILLIS_PER_MINUTE));

        return command;
    }

    public double getSelectedDecibelTarget() {
        if (this.textRdo.getSelection()) {
            return this.audioVolume.getCurrentDecibelValue();
        } else if (this.sameRdo.getSelection()) {
            return this.sameVolume.getCurrentDecibelValue();
        } else if (this.alertRdo.getSelection()) {
            return this.alertVolume.getCurrentDecibelValue();
        } else if (this.transfer18Rdo.getSelection()) {
            return this.tfr1800Volume.getCurrentDecibelValue();
        } else if (this.transfer24Rdo.getSelection()) {
            return this.tfr2400Volume.getCurrentDecibelValue();
        }

        return 0.;
    }

    /**
     * Sends a {@link MaintenanceMessageRequest} to request the construction of
     * a dac maintenance message file.
     * 
     * @return the location of the maintenance message XML
     * @throws Exception
     *             if the request to EDEX fails for any reason
     */
    private String constructMessageFile() throws Exception {
        MaintenanceMessageRequest request = new MaintenanceMessageRequest();
        request.setDuration(this.durScaleComp.getSelectedValue());
        request.setTransmitterGroup(this.selectedTransmitterGrp.getName());

        if (this.sameRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.SAME);
        } else if (this.alertRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.ALERT);
        } else if (this.textRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.TEXT);
        } else if (this.transfer18Rdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.TRANSFER_18);
        } else if (this.transfer24Rdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.TRANSFER_24);
        }

        return (String) BmhUtils.sendRequest(request);
    }

    @Override
    public boolean okToClose() {
        /*
         * Check for unsaved changes.
         */
        if (this.dataIsCurrent() == false) {
            int option = DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_WARNING | SWT.YES
                            | SWT.NO, "Unsaved Changes",
                            "You have unsaved changes, would you like to discard them?");
            if (option != SWT.YES) {
                return false;
            }
        }

        java.util.List<String> maintGroups = new ArrayList<>(
                transmitterGroupNameMap.size());
        for (String groupName : transmitterGroupNameMap.keySet()) {
            TransmitterGroup tg = transmitterGroupNameMap.get(groupName);
            if (tg.isMaint()) {
                maintGroups.add(groupName);
            }
        }
        if (!maintGroups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following transmitter group(s) are in maintenance:\n");
            for (String groupName : maintGroups) {
                sb.append(groupName).append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(),
                    ".\n\nSelect OK to enable and close dialog.");

            MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK
                    | SWT.CANCEL);
            mb.setText("Enable Transmitter(s)");
            mb.setMessage(sb.toString());
            int answer = mb.open();

            if (answer != SWT.OK) {
                return false;
            }

            for (String groupName : maintGroups) {
                TransmitterGroup tg = transmitterGroupNameMap.get(groupName);
                try {
                    updateTransmitterGroup(TxStatus.ENABLED, tg);
                } catch (Exception e) {
                    statusHandler.error("Error enabling Transmitter Group", e);
                }
            }
        }

        return true;
    }

    private void syncTransmitterUpdates(
            TransmitterGroupConfigNotification notification) {
        /*
         * Have the transmitters been updated (added) or removed?
         */
        if (notification.getType() == ConfigChangeType.Delete) {
            boolean selectedDeleted = false;
            final java.util.List<String> transmittersToRemove = new ArrayList<>(
                    notification.getIdentifiers().size());
            for (TransmitterGroupIdentifier identifier : notification
                    .getIdentifiers()) {
                if (selectedTransmitterGrp != null
                        && selectedTransmitterGrp.getName().equals(
                                identifier.getName())) {
                    selectedDeleted = true;
                }
                if (this.transmitterGroupNameMap.remove(identifier.getName()) != null) {
                    transmittersToRemove.add(identifier.getName());
                }
            }

            final boolean reloadSelection = selectedDeleted;
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    synchronized (transmitterGrpLock) {
                        for (String transmitterGrp : transmittersToRemove) {
                            transmitterList.remove(transmitterGrp);
                        }
                        if (reloadSelection) {
                            selectedTransmitterGrp = null;
                            populate();
                        }
                    }
                }
            });
        } else {
            boolean selectedUpdated = false;
            final Set<Integer> groupIds = new HashSet<>(notification
                    .getIdentifiers().size(), 1.0f);
            for (TransmitterGroupIdentifier identifier : notification
                    .getIdentifiers()) {
                if (selectedTransmitterGrp != null
                        && selectedTransmitterGrp.getName().equals(
                                identifier.getName())) {
                    selectedUpdated = true;
                }
                groupIds.add(identifier.getId());
            }

            /*
             * Attempt to retrieve all {@link TransmitterGroup}s that have been
             * added or updated.
             */
            java.util.List<TransmitterGroup> updatedTransmitterGroups;
            try {
                updatedTransmitterGroups = this.dataManager
                        .getTransmitterGroupsWithIds(groupIds);
            } catch (Exception e) {
                statusHandler
                        .error("Failed to retrieve the updated Transmitter Groups associated with notification: "
                                + notification.toString() + ".", e);
                return;
            }

            final java.util.List<String> newGroupNames = new ArrayList<>(
                    updatedTransmitterGroups.size());
            for (TransmitterGroup tg : updatedTransmitterGroups) {
                if (transmitterGroupNameMap.containsKey(tg.getName()) == false) {
                    newGroupNames.add(tg.getName());
                }
                transmitterGroupNameMap.put(tg.getName(), tg);
            }

            if (selectedUpdated || newGroupNames.isEmpty() == false) {
                final boolean reloadSelection = selectedUpdated;
                VizApp.runAsync(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (transmitterGrpLock) {
                            for (String groupName : newGroupNames) {
                                transmitterList.add(groupName);
                            }

                            if (reloadSelection) {
                                populate();
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void volumeChanged() {
        this.saveButton.setEnabled(this.dataIsCurrent() == false);
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        if (isDisposed()) {
            return;
        }

        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                if (o instanceof TransmitterGroupConfigNotification) {
                    TransmitterGroupConfigNotification notification = (TransmitterGroupConfigNotification) o;
                    synchronized (this.transmitterGrpLock) {
                        this.syncTransmitterUpdates(notification);
                    }
                }
            } catch (NotificationException e) {
                statusHandler.error("Error processing update notification", e);
            }
        }
    }
}