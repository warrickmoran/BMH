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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.TransmitterAlignmentException;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.broadcast.AbstractOnDemandBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.TransmitterAlignmentTestCommand;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.request.MaintenanceMessageRequest;
import com.raytheon.uf.common.bmh.request.MaintenanceMessageResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.ScaleSpinnerComp;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

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
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Nov 15, 2014    3630    bkowal      Run the test and report the result.
 * Nov 21, 2014    3845    bkowal      Updates to {@link AbstractOnDemandBroadcastMessage}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterAlignmentDlg extends AbstractBMHDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TransmitterAlignmentDlg.class);

    private static final long TRANSMITTER_ALIGNMENT_TEST_TIMEOUT = 45000;

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

    /** Decibel value label */
    private Label dbValueLbl;

    /** Duration ScaleSpinner composite */
    private ScaleSpinnerComp durScaleComp;

    /** Enable Button */
    private Button enableTransmitterBtn;

    /** Disable Button */
    private Button disableTransmitterBtn;

    private Button sameRdo;

    private Button alertRdo;

    private Button textRdo;

    /** "Run Test" Button **/
    private Button testBtn;

    private TransmitterAlignmentTestThread alignmentTestThread;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            The parent shell
     * @param dlgMap
     *            Map of open dialogs
     */
    public TransmitterAlignmentDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.TRANSMITTER_ALIGNMENT.getTitle(), parentShell,
                SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
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
        populate();
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
                populate();
            }
        });
    }

    private void createDbLevelGroup(Composite comp) {
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Group dbGroup = new Group(comp, SWT.BORDER);
        dbGroup.setText(" Decibel Level ");
        dbGroup.setLayout(gl);
        dbGroup.setLayoutData(gd);

        Label dbLbl = new Label(dbGroup, SWT.NONE);
        dbLbl.setText("Target Decibel Level: ");

        gd = new GridData(35, SWT.DEFAULT);
        dbValueLbl = new Label(dbGroup, SWT.NONE);
        dbValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, false, false);
        Button changeBtn = new Button(dbGroup, SWT.PUSH);
        changeBtn.setText(" Change Target... ");
        changeBtn.setLayoutData(gd);
        changeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DecibelLevelValidator validator = new DecibelLevelValidator();
                InputTextDlg dlg = new InputTextDlg(shell, "Target dB Value",
                        "Enter the Target dB Value:", validator, true);
                dlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            String dbLevelStr = (String) returnValue;
                            System.out.println("dB Level: " + dbLevelStr);
                            selectedTransmitterGrp.setAudioDBTarget(Double
                                    .parseDouble(dbLevelStr));
                            try {
                                dataManager
                                        .saveTransmitterGroup(selectedTransmitterGrp);
                                dbValueLbl.setText(String
                                        .valueOf(selectedTransmitterGrp
                                                .getAudioDBTarget()));
                            } catch (Exception e) {
                                statusHandler.error(
                                        "Error saving Target Decibel Level", e);
                            }
                        }
                    }
                });
                dlg.open();
            }
        });
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
        gl = new GridLayout(2, false);
        Composite btnComp = new Composite(statusGrp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        int btnWidth = 130;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        enableTransmitterBtn = new Button(btnComp, SWT.PUSH);
        enableTransmitterBtn.setText("Enable Group");
        enableTransmitterBtn.setLayoutData(gd);
        enableTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Enable Transmitter",
                        "Are you sure you want to enable Transmitter Group "
                                + selectedTransmitterGrp.getName() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                enableTransmitterGroup();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        disableTransmitterBtn = new Button(btnComp, SWT.PUSH);
        disableTransmitterBtn.setText("Disable Transmitter");
        disableTransmitterBtn.setLayoutData(gd);
        disableTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Disable Group",
                        "Are you sure you want to disable Transmitter Group "
                                + selectedTransmitterGrp.getName() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                disableTransmitterGroup();
            }
        });
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

        sameRdo = new Button(rdoComp, SWT.RADIO);
        sameRdo.setText("SAME");
        sameRdo.setSelection(true);

        alertRdo = new Button(rdoComp, SWT.RADIO);
        alertRdo.setText("Alert");

        textRdo = new Button(rdoComp, SWT.RADIO);
        textRdo.setText("Text");

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite durComp = new Composite(group, SWT.NONE);
        durComp.setLayout(gl);
        durComp.setLayoutData(gd);

        Label durLbl = new Label(durComp, SWT.NONE);
        durLbl.setText("Duration (seconds):");

        durScaleComp = new ScaleSpinnerComp(durComp, 1, 30, "", 85, 25);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.CENTER;
        testBtn = new Button(group, SWT.PUSH);
        testBtn.setText(" Run Test ");
        testBtn.setLayoutData(gd);
        testBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRunTest();
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
                close();
            }
        });

    }

    /**
     * Populate the dialog for the selected transmitter
     */
    private void populate() {
        if (transmitterList.getSelectionCount() > 0) {
            String grpName = transmitterList.getSelection()[0];
            selectedTransmitterGrp = transmitterGroupNameMap.get(grpName);
            dbValueLbl.setText(String.valueOf(selectedTransmitterGrp
                    .getAudioDBTarget()));

            if (selectedTransmitterGrp.isEnabled()) {
                this.statusLbl.setText(STATUS_PREFIX
                        + selectedTransmitterGrp.getName() + " "
                        + TxStatus.ENABLED.toString());
                this.enableTransmitterBtn.setEnabled(false);
                this.disableTransmitterBtn.setEnabled(true);
                this.testBtn.setEnabled(false);
            } else {
                this.statusLbl.setText(STATUS_PREFIX
                        + selectedTransmitterGrp.getName() + " "
                        + TxStatus.DISABLED.toString());
                /*
                 * If transmitter is disabled then it should not be enabled from
                 * this dialog
                 */
                this.enableTransmitterBtn.setEnabled(false);
                this.disableTransmitterBtn.setEnabled(false);
                this.testBtn
                        .setEnabled(this
                                .isTransmitterGroupConfigured(this.selectedTransmitterGrp));
            }
        }
    }

    /**
     * Populate the transmitter list box
     */
    private void populateTransmitters() {
        try {
            java.util.List<TransmitterGroup> transmitterGroupObjectList = dataManager
                    .getTransmitterGroups();
            Collections.sort(transmitterGroupObjectList,
                    new TransmitterGroupPositionComparator());
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

    private void enableTransmitterGroup() {
        selectedTransmitterGrp.enableGroup(false);

        try {
            dataManager.saveTransmitterGroup(selectedTransmitterGrp);
            enableTransmitterBtn.setEnabled(false);
            disableTransmitterBtn.setEnabled(true);
            testBtn.setEnabled(false);

            statusLbl.setText(STATUS_PREFIX + selectedTransmitterGrp.getName()
                    + " " + TxStatus.ENABLED.toString());
        } catch (Exception e) {
            statusHandler.error("Error enabling Transmitter Group", e);
        }
    }

    private void disableTransmitterGroup() {
        selectedTransmitterGrp.disableGroup();

        try {
            dataManager.saveTransmitterGroup(selectedTransmitterGrp);
            enableTransmitterBtn.setEnabled(true);
            disableTransmitterBtn.setEnabled(false);
            testBtn.setEnabled(this
                    .isTransmitterGroupConfigured(this.selectedTransmitterGrp));
            statusLbl.setText(STATUS_PREFIX + selectedTransmitterGrp.getName()
                    + " " + TxStatus.DISABLED.toString());
        } catch (Exception e) {
            statusHandler.error("Error enabling Transmitter Group", e);
        }
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

        return transmitterGroup != null
                && transmitterGroup.getDac() != null
                && transmitterGroup.getTransmitterList() != null
                && transmitterGroup.getTransmitterList().isEmpty() == false
                && transmitterGroup.getTransmitterList().get(0).getDacPort() != null;
    }

    private void handleRunTest() {
        TransmitterAlignmentTestCommand command = null;
        try {
            command = this.buildCommand();
        } catch (TransmitterAlignmentException e) {
            statusHandler.error(
                    "Failed to configure the transmitter alignment test.", e);
            return;
        }
        this.testBtn.setEnabled(false);

        this.alignmentTestThread = new TransmitterAlignmentTestThread(command);
        this.alignmentTestThread.start();

        /*
         * Block all interaction. Will only last a maximum of 45 seconds
         * worst-case scenario.
         */
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                this.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask("Running Transmitter Alignment Test",
                            IProgressMonitor.UNKNOWN);
                    try {
                        alignmentTestThread
                                .join(TRANSMITTER_ALIGNMENT_TEST_TIMEOUT);
                    } catch (InterruptedException e) {
                        statusHandler
                                .error("Interrupted while waiting for the alignment test to finish.",
                                        e);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            statusHandler.error(
                    "Failed to run the transmitter alignment test.", e);
        }

        // check the status of the task.
        int icon = 9999;
        String message = null;
        switch (this.alignmentTestThread.getStatus()) {
        case FAIL:
            icon = SWT.ICON_ERROR;
            message = this.alignmentTestThread.getStatusDetails();
            break;
        case SUCCESS:
            icon = SWT.ICON_INFORMATION;
            message = this.alignmentTestThread.getStatusDetails();
            break;
        case UNKNOWN:
            icon = SWT.ICON_WARNING;
            message = "The final status of the alignment test is unknown. Please check the server logs.";
            break;
        }
        DialogUtility.showMessageBox(this.shell, icon | SWT.OK,
                "Transmitter Alignment Test Result", message);
        this.testBtn.setEnabled(true);
    }

    private TransmitterAlignmentTestCommand buildCommand()
            throws TransmitterAlignmentException {
        String audioLocation = null;

        try {
            audioLocation = this.retrieveAudioLocation();
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

        TransmitterAlignmentTestCommand command = new TransmitterAlignmentTestCommand();
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
        if (dac.getDataPorts() == null || dac.getDataPorts().isEmpty()) {
            throw new TransmitterAlignmentException(
                    "No data ports have been assigned to dac " + dac.getName()
                            + ".");
        }
        command.setAllowedDataPorts(dac.getDataPorts());
        command.setRadios(radios);
        command.setDecibelTarget(Double.parseDouble(this.dbValueLbl.getText()));
        command.setInputAudioFile(audioLocation);
        command.setBroadcastDuration(this.durScaleComp.getSelectedValue());

        return command;
    }

    /**
     * Retrieves the location of the maintenance audio that will be transmitted
     * to the dac.
     * 
     * @return the location of the maintenance audio
     * @throws Exception
     *             if the request to EDEX fails for any reason
     */
    private String retrieveAudioLocation() throws Exception {
        MaintenanceMessageRequest request = new MaintenanceMessageRequest();

        if (this.sameRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.SAME);
        } else if (this.alertRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.ALERT);
        } else if (this.textRdo.getSelection()) {
            request.setType(MaintenanceMessageRequest.AUDIOTYPE.TEXT);
        }

        MaintenanceMessageResponse response = (MaintenanceMessageResponse) BmhUtils
                .sendRequest(request);

        return response.getMaintenanceAudioFileLocation();
    }

    @Override
    public boolean okToClose() {
        // no need to block closing
        return true;
    }
}
