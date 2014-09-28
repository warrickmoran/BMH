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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.ScaleSpinnerComp;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterAlignmentDlg extends AbstractBMHDialog {
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
    protected TransmitterGroup transmitterGrp;

    /** Decibel value label */
    private Label dbValueLbl;

    /** Duration ScaleSpinner composite */
    private ScaleSpinnerComp durScaleComp;

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
        super(dlgMap, "Transmitter Alignment Dialog", parentShell,
                SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                        | CAVE.DO_NOT_BLOCK);
        setText("Transmitter Alignment");
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

        createDbLevelGroup(right);
        createTransmitterStatusGroup(right);

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
                // Use this one if double values needed
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
                        }
                    }
                });

                // Use this one if int value needed
                // // TODO find out if this is int or double
                // DecibelLevelSelectionDlg dlg = new DecibelLevelSelectionDlg(
                // getShell(), (int) Math.round(transmitterGrp
                // .getAudioDBTarget()));
                // dlg.setCloseCallback(new ICloseCallback() {
                // @Override
                // public void dialogClosed(Object returnValue) {
                // if (returnValue != null
                // && returnValue instanceof Integer) {
                // int dbLevel = (int) returnValue;
                // // TODO save value to DB
                // dbValueLbl.setText(String.valueOf(dbLevel));
                // }
                // }
                // });

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

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalAlignment = SWT.CENTER;
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
        Button enableTransmitterBtn = new Button(btnComp, SWT.PUSH);
        enableTransmitterBtn.setText("Enable Transmitter");
        enableTransmitterBtn.setLayoutData(gd);
        enableTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO trigger changes
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Enable Transmitter",
                        "Are you sure you want to enable Transmitter "
                                + transmitterGrp.getName() + "?");
                if (answer == SWT.NO) {
                    return;
                }
                // TODO set these
                // setTransmitterStatus(TxStatus.ENABLED);
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button disableTransmitterBtn = new Button(btnComp, SWT.PUSH);
        disableTransmitterBtn.setText("Disable Transmitter");
        disableTransmitterBtn.setLayoutData(gd);
        disableTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO trigger changes
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Enable Transmitter",
                        "Are you sure you want to disable Transmitter "
                                + transmitterGrp.getName() + "?");
                if (answer == SWT.NO) {
                    return;
                }
                // TODO set these
                // setTransmitterStatus(TxStatus.DISABLED);
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

        Button sameRdo = new Button(rdoComp, SWT.RADIO);
        sameRdo.setText("SAME");
        sameRdo.setSelection(true);

        Button alertRdo = new Button(rdoComp, SWT.RADIO);
        alertRdo.setText("Alert");

        Button textRdo = new Button(rdoComp, SWT.RADIO);
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
        Button testBtn = new Button(group, SWT.PUSH);
        testBtn.setText(" Run Test ");
        testBtn.setLayoutData(gd);
        testBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO hook this up
                System.out.println("Run test for "
                        + durScaleComp.getSelectedValue() + " seconds");
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
        String grpName = transmitterList.getSelection()[0];
        TransmitterGroup transGrp = transmitterGroupNameMap.get(grpName);
        dbValueLbl.setText(String.valueOf(transGrp.getAudioDBTarget()));
        // TODO set status label here when populating
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
        }
    }

    @Override
    public boolean okToClose() {
        // TODO Fix this
        return false;
    }
}
