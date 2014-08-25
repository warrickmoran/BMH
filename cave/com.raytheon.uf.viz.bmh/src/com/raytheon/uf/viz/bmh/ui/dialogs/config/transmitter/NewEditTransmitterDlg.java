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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter.TxMode;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;
import com.raytheon.uf.viz.bmh.ui.program.ProgramNameComparator;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * New/Edit transmitter configuration dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2014     3173   mpduff      Initial creation
 * Aug 18, 2014     3173   mpduff      Add Program selection
 * Aug 24, 2014     3432   mpduff      Implemented min/max db values
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewEditTransmitterDlg extends CaveSWTDialog {
    private final String STANDALONE = "Standalone";

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewEditTransmitterDlg.class);

    public enum TransmitterEditType {
        NEW_TRANSMITTER, NEW_TRANSMITTER_GROUP, EDIT_TRANSMITTER, EDIT_TRANSMITTER_GROUP;
    }

    private final String[] TIME_ZONES = { "ALASKA", "HAWAIIAN", "PACIFIC",
            "MOUNTAIN", "CENTRAL", "EASTERN" };

    private final String STATUS_PREFIX = "     Transmitter is ";

    private final ITransmitterStatusChange statusChange;

    /** Status Group */
    private Group statusGrp;

    /** Mode Group */
    private Group modeGrp;

    /** Status Icon */
    private Image statusIcon;

    /** Mode Icon */
    private Image modeIcon;

    private Button disableTransmitterBtn;

    private Button enableTransmitterBtn;

    private Button primaryTransmitterBtn;

    private Button secondaryTransmitterBtn;

    /** The transmitter being created/edited */
    private Transmitter transmitter;

    /** The transmitter group being created/edited */
    private TransmitterGroup group;

    /** The action type */
    private final TransmitterEditType type;

    private final TransmitterDataManager dataManager;

    private List<TransmitterGroup> groupList;

    private Combo grpNameCbo;

    private Combo dacCombo;

    private Combo dacPortCbo;

    private Combo timeZoneCbo;

    private Button disableSilenceChk;

    private Button noDstChk;

    private Text transmitterNameTxt;

    private Text mnemonicTxt;

    private Text frequencyTxt;

    private Text callSignTxt;

    private Text locationTxt;

    private Text serviceAreaTxt;

    private Text fipsTxt;

    private Text grpNameValueTxt;

    /** List of group controls */
    private final List<Control> groupControlList = new ArrayList<Control>();

    /** List of transmitter controls */
    private final List<Control> transmitterControlList = new ArrayList<Control>();

    /** Save group flag, save the TransmitterGroup if true */
    private boolean saveGroup = false;

    private final ICloseCallback callback;

    private Transmitter previousTransmitter;

    private Label statusLbl;

    private Label modeLbl;

    private Combo programCombo;

    private Text minDbTxt;

    private Text maxDbTxt;

    /**
     * Edit Transmitter constructor.
     * 
     * @param parentShell
     *            The parent shell
     * @param transmitter
     *            The Transmitter
     * @param group
     *            The TransmitterGroup
     * @param type
     *            The action type
     * @param callback
     *            The close callback
     */
    public NewEditTransmitterDlg(Shell parentShell, Transmitter transmitter,
            TransmitterGroup group, TransmitterEditType type,
            ICloseCallback callback, ITransmitterStatusChange statusChange) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                | CAVE.DO_NOT_BLOCK);
        this.type = type;
        this.transmitter = transmitter;
        this.group = group;
        this.callback = callback;
        this.statusChange = statusChange;
        dataManager = new TransmitterDataManager();

        switch (type) {
        case EDIT_TRANSMITTER:
            setText("Edit Transmitter - " + transmitter.getName());
            break;
        case EDIT_TRANSMITTER_GROUP:
            setText("Edit Transmitter Group - " + group.getName());
            break;
        case NEW_TRANSMITTER:
            setText("New Transmitter");
            break;
        case NEW_TRANSMITTER_GROUP:
            setText("New Transmitter Group");
            break;
        default:
            break;
        }
    }

    /**
     * New Transmitter/Group constructor.
     * 
     * @param parentShell
     */
    public NewEditTransmitterDlg(Shell parentShell, TransmitterEditType type,
            ICloseCallback callback, ITransmitterStatusChange statusChange) {
        this(parentShell, null, null, type, callback, statusChange);
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
        loadImages();

        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        createStatusComp(mainComp);
        createAttrComp(mainComp);

        createBottomButtons();
        populate();
    }

    /**
     * Create the Transmitter status composite
     * 
     * @param mainComp
     */
    private void createStatusComp(Composite mainComp) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite statusModeComp = new Composite(mainComp, SWT.NONE);
        statusModeComp.setLayout(gl);
        statusModeComp.setLayoutData(gd);

        // Status group
        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        statusGrp = new Group(statusModeComp, SWT.BORDER);
        statusGrp.setText(" Transmitter Status ");
        statusGrp.setLayout(gl);
        statusGrp.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite top = new Composite(statusGrp, SWT.NONE);
        top.setLayout(gl);
        top.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label statusIconLbl = new Label(top, SWT.NONE);
        statusIconLbl.setImage(this.statusIcon);
        statusIconLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.widthHint = 200;
        statusLbl = new Label(top, SWT.NONE);
        statusLbl.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite bottom = new Composite(statusGrp, SWT.NONE);
        bottom.setLayout(gl);
        bottom.setLayoutData(gd);

        int btnWidth = 130;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        enableTransmitterBtn = new Button(bottom, SWT.PUSH);
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
                                + transmitter.getMnemonic() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                setTransmitterStatus(TxStatus.ENABLED);
            }
        });
        transmitterControlList.add(enableTransmitterBtn);

        disableTransmitterBtn = new Button(bottom, SWT.PUSH);
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
                                + transmitter.getMnemonic() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                setTransmitterStatus(TxStatus.DISABLED);
            }
        });
        transmitterControlList.add(disableTransmitterBtn);

        // Mode Group
        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        modeGrp = new Group(statusModeComp, SWT.BORDER);
        modeGrp.setText(" Transmitter Mode ");
        modeGrp.setLayout(gl);
        modeGrp.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite modeTop = new Composite(modeGrp, SWT.NONE);
        modeTop.setLayout(gl);
        modeTop.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label modeIconLbl = new Label(modeTop, SWT.NONE);
        modeIconLbl.setImage(this.modeIcon);
        modeIconLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.widthHint = 200;
        modeLbl = new Label(modeTop, SWT.NONE);
        modeLbl.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite modeBottom = new Composite(modeGrp, SWT.NONE);
        modeBottom.setLayout(gl);
        modeBottom.setLayoutData(gd);

        gd = new GridData(btnWidth, SWT.DEFAULT);
        primaryTransmitterBtn = new Button(modeBottom, SWT.PUSH);
        primaryTransmitterBtn.setText("Set As Primary");
        primaryTransmitterBtn.setLayoutData(gd);
        primaryTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO trigger changes
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Transmitter Mode",
                        "Are you sure you want to set Transmitter "
                                + transmitter.getMnemonic() + " as "
                                + TxMode.PRIMARY.name() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                setTransmitterMode(TxMode.PRIMARY);
            }
        });
        transmitterControlList.add(primaryTransmitterBtn);

        secondaryTransmitterBtn = new Button(modeBottom, SWT.PUSH);
        secondaryTransmitterBtn.setText("Set As Secondary");
        secondaryTransmitterBtn.setLayoutData(gd);
        secondaryTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO trigger changes
                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_INFORMATION | SWT.YES | SWT.NO,
                        "Transmitter Mode",
                        "Are you sure you want to set Transmitter "
                                + transmitter.getMnemonic() + " as "
                                + TxMode.SECONDARY.name() + "?");
                if (answer == SWT.NO) {
                    return;
                }

                setTransmitterMode(TxMode.SECONDARY);
            }
        });
        transmitterControlList.add(secondaryTransmitterBtn);
    }

    /**
     * Create the Transmitter and Group attribute controls.
     * 
     * @param comp
     */
    private void createAttrComp(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);
        statusGrp = new Group(comp, SWT.BORDER);
        statusGrp.setText(" Attributes ");
        statusGrp.setLayout(gl);
        statusGrp.setLayoutData(gd);

        // Group settings
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite leftComp = new Composite(statusGrp, SWT.NONE);
        leftComp.setLayout(gl);
        leftComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite rightComp = new Composite(statusGrp, SWT.NONE);
        rightComp.setLayout(gl);
        rightComp.setLayoutData(gd);

        Label grpNameLbl = new Label(leftComp, SWT.NONE);
        grpNameLbl.setText("Group Name: ");
        grpNameLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        // For new transmitter need to have a combo box to select the group
        if (type == TransmitterEditType.NEW_TRANSMITTER
                || type == TransmitterEditType.EDIT_TRANSMITTER) {
            grpNameCbo = new Combo(leftComp, SWT.BORDER | SWT.READ_ONLY);
            grpNameCbo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                    false));
            grpNameCbo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    groupNameSelectionAction();
                }
            });
            grpNameCbo.setItems(getGroupNames());
            grpNameCbo.add(STANDALONE, 0);
            if (type == TransmitterEditType.NEW_TRANSMITTER) {
                grpNameCbo.select(0);
            } else {
                int idx = grpNameCbo.indexOf(group.getName());
                if (idx >= 0) {
                    grpNameCbo.select(idx);
                } else {
                    grpNameCbo.select(0);
                }
            }
        } else if (type == TransmitterEditType.NEW_TRANSMITTER_GROUP) {
            grpNameValueTxt = new Text(leftComp, SWT.BORDER);
            grpNameValueTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                    true, false));
            groupControlList.add(grpNameValueTxt);
        } else if (type == TransmitterEditType.EDIT_TRANSMITTER_GROUP) {
            grpNameValueTxt = new Text(leftComp, SWT.BORDER);
            grpNameValueTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                    true, false));
            grpNameValueTxt.setText(group.getName());
            groupControlList.add(grpNameValueTxt);

        }

        Label dacLbl = new Label(leftComp, SWT.NONE);
        dacLbl.setText("DAC #: ");
        dacLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        dacCombo = new Combo(leftComp, SWT.BORDER);
        dacCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dacCombo.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7",
                "8", "9" });
        dacCombo.select(0);
        groupControlList.add(dacCombo);

        Label dacPortLbl = new Label(leftComp, SWT.NONE);
        dacPortLbl.setText("DAC Port #: ");
        dacPortLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        dacPortCbo = new Combo(leftComp, SWT.BORDER);
        dacPortCbo
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        transmitterControlList.add(dacPortCbo);

        // TODO get real data?
        dacPortCbo.setItems(new String[] { "1", "2", "3", "4" });
        dacPortCbo.select(0);

        Label progLbl = new Label(leftComp, SWT.NONE);
        progLbl.setText("Program:");
        progLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        programCombo = new Combo(leftComp, SWT.BORDER);
        programCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        populateProgramCombo();
        groupControlList.add(programCombo);

        Label timeZoneLbl = new Label(rightComp, SWT.NONE);
        timeZoneLbl.setText("Time Zone: ");
        timeZoneLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        timeZoneCbo = new Combo(rightComp, SWT.BORDER | SWT.READ_ONLY);
        timeZoneCbo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        timeZoneCbo.setItems(TIME_ZONES);
        timeZoneCbo.select(0);
        groupControlList.add(timeZoneCbo);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        disableSilenceChk = new Button(rightComp, SWT.CHECK);
        disableSilenceChk.setText("Disable Silence Alarm");
        disableSilenceChk.setLayoutData(gd);
        groupControlList.add(disableSilenceChk);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        noDstChk = new Button(rightComp, SWT.CHECK);
        noDstChk.setText("No Daylight Savings Time Observed");
        noDstChk.setLayoutData(gd);
        groupControlList.add(noDstChk);

        // Blank labels
        Label spacer = new Label(rightComp, SWT.NONE);
        spacer.setText("");

        Label spacer2 = new Label(rightComp, SWT.NONE);
        spacer2.setText("");

        // Individual transmitter settings
        Label nameLbl = new Label(leftComp, SWT.NONE);
        nameLbl.setText("Name: ");
        nameLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        transmitterNameTxt = new Text(leftComp, SWT.BORDER);
        transmitterNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, false));
        transmitterControlList.add(transmitterNameTxt);

        Label mnemonicLbl = new Label(leftComp, SWT.NONE);
        mnemonicLbl.setText("Mnemonic: ");
        mnemonicLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        mnemonicTxt = new Text(leftComp, SWT.BORDER);
        mnemonicTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        transmitterControlList.add(mnemonicTxt);

        Label frequencyLbl = new Label(leftComp, SWT.NONE);
        frequencyLbl.setText("Frequency: ");
        frequencyLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        frequencyTxt = new Text(leftComp, SWT.BORDER);
        frequencyTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        transmitterControlList.add(frequencyTxt);

        Label callSignLbl = new Label(leftComp, SWT.NONE);
        callSignLbl.setText("Call Sign: ");
        callSignLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        callSignTxt = new Text(leftComp, SWT.BORDER);
        callSignTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        transmitterControlList.add(callSignTxt);

        Label locationLbl = new Label(rightComp, SWT.NONE);
        locationLbl.setText("Location: ");
        locationLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        locationTxt = new Text(rightComp, SWT.BORDER);
        locationTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        transmitterControlList.add(locationTxt);

        Label serviceAreaLbl = new Label(rightComp, SWT.NONE);
        serviceAreaLbl.setText("Service Area: ");
        serviceAreaLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        serviceAreaTxt = new Text(rightComp, SWT.BORDER);
        serviceAreaTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        transmitterControlList.add(serviceAreaTxt);

        Label fipsLbl = new Label(rightComp, SWT.NONE);
        fipsLbl.setText("FIPS Code: ");
        fipsLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        fipsTxt = new Text(rightComp, SWT.BORDER);
        fipsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        transmitterControlList.add(fipsTxt);

        Composite dbComp = new Composite(rightComp, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        dbComp.setLayout(gl);
        dbComp.setLayoutData(gd);

        Label minDbLbl = new Label(dbComp, SWT.NONE);
        minDbLbl.setText("Min dB:");
        minDbLbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                false));

        minDbTxt = new Text(dbComp, SWT.BORDER);
        minDbTxt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                false));
        groupControlList.add(minDbTxt);

        Label maxDbLbl = new Label(dbComp, SWT.NONE);
        maxDbLbl.setText("Max dB:");
        maxDbLbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                false));

        maxDbTxt = new Text(dbComp, SWT.BORDER);
        maxDbTxt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                false));
        groupControlList.add(maxDbTxt);

        // Disable all controls by default
        enableGroupControls(false);
        enableTransmitterControls(false);
    }

    private void createBottomButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveBtn = new Button(comp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (save()) {
                    callback.dialogClosed(getReturnValue());
                    close();
                }
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(comp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Populate the controls
     */
    private void populate() {
        boolean enabled = false;

        // Populate the fields
        enabled = (TransmitterEditType.EDIT_TRANSMITTER_GROUP == type || TransmitterEditType.NEW_TRANSMITTER_GROUP == type);
        if (group != null || enabled) {
            if (group == null) {
                group = new TransmitterGroup();
            }
            if (group.getDac() != null) {
                // TODO - When getting dac numbers from DB need to change this
                // logic
                dacCombo.select(group.getDac() - 1);
            }
            if (group.getTimeZone() != null) {
                timeZoneCbo.select(timeZoneCbo.indexOf(group.getTimeZone()));
            }

            if (group.getSilenceAlarm() != null) {
                disableSilenceChk.setSelection(group.getSilenceAlarm());
            }
            if (group.getDaylightSaving() != null) {
                noDstChk.setSelection(group.getDaylightSaving());
            }

            this.minDbTxt.setText(String.valueOf(group.getAdjustAudioMinDB()));
            this.maxDbTxt.setText(String.valueOf(group.getAdjustAudioMaxDB()));

            if (group != null && group.isStandalone()) {
                enabled = true;
            }

            // Enable/Disable controls
            enableGroupControls(enabled);
        }

        enabled = (TransmitterEditType.EDIT_TRANSMITTER == type || TransmitterEditType.NEW_TRANSMITTER == type);

        if (transmitter != null || enabled) {
            if (transmitter == null) {
                transmitter = new Transmitter();
            }
            if (transmitter.getDacPort() != null) {
                dacPortCbo.select(transmitter.getDacPort() - 1);
            }

            if (transmitter.getName() != null) {
                transmitterNameTxt.setText(transmitter.getName());
            }
            if (transmitter.getMnemonic() != null) {
                mnemonicTxt.setText(transmitter.getMnemonic());
            }

            frequencyTxt.setText(String.valueOf(transmitter.getFrequency()));
            if (transmitter.getCallSign() != null) {
                callSignTxt.setText(transmitter.getCallSign());
            }
            if (transmitter.getLocation() != null) {
                locationTxt.setText(transmitter.getLocation());
            }
            if (transmitter.getServiceArea() != null) {
                serviceAreaTxt.setText(transmitter.getServiceArea());
            }
            if (transmitter.getFipsCode() != null) {
                fipsTxt.setText(transmitter.getFipsCode());
            }

            statusLbl.setText(this.STATUS_PREFIX
                    + transmitter.getTxStatus().name());
            modeLbl.setText(this.STATUS_PREFIX + transmitter.getTxMode().name());

            // Enable/Disable controls
            enableTransmitterControls(enabled);
            if (TransmitterEditType.NEW_TRANSMITTER == type) {
                enableGroupControls(enabled);
            }
        }
    }

    /**
     * Get group names of groups that are not standalone
     * 
     * @return
     */
    private String[] getGroupNames() {
        try {
            groupList = dataManager.getTransmitterGroups();
            List<String> grpNameList = new ArrayList<String>();
            for (TransmitterGroup tg : groupList) {
                if (!tg.isStandalone()) {
                    grpNameList.add(tg.getName());
                }
            }

            return grpNameList.toArray(new String[0]);
        } catch (Exception e) {
            statusHandler.error("Error retrieving Transmitter Group data", e);
        }

        return new String[0];
    }

    private void populateProgramCombo() {
        try {
            List<Program> programList = dataManager.getPrograms();
            Collections.sort(programList, new ProgramNameComparator());

            String[] programs = new String[programList.size()];
            int select = 0;
            int i = 0;
            for (Program p : programList) {
                programs[i] = p.getName();
                if (p.getTransmitterGroups().contains(this.group)) {
                    select = i;
                }
                i++;
            }

            programCombo.setData(programList);
            programCombo.setItems(programs);
            programCombo.add("None", 0);
            // TODO - Fix selection of existing program.
            programCombo.select(select + 1);
        } catch (Exception e) {
            statusHandler.error("Error retrieving Program data", e);
        }
    }

    /**
     * Action handler for group name selection.
     */
    private void groupNameSelectionAction() {
        String groupName = grpNameCbo.getText();
        if (groupName.equals(STANDALONE)) {
            enableGroupControls(true);
        } else {
            populateGroupControls(groupName);
            enableGroupControls(false);
        }
    }

    /**
     * Populate the TransmitterGroup controls
     * 
     * @param groupName
     *            The name of the group
     */
    private void populateGroupControls(String groupName) {
        for (TransmitterGroup tg : groupList) {
            if (tg.getName().equals(groupName)) {
                group = tg;
                if (group.getDac() != null) {
                    // TODO change logic
                    dacCombo.select(group.getDac() - 1);
                }
                if (group.getTimeZone() != null) {
                    timeZoneCbo
                            .select(timeZoneCbo.indexOf(group.getTimeZone()));
                }

                if (group.getSilenceAlarm() != null) {
                    disableSilenceChk.setSelection(group.getSilenceAlarm());
                }
                if (group.getDaylightSaving() != null) {
                    noDstChk.setSelection(group.getDaylightSaving());
                }
                break;
            }
        }
    }

    /**
     * Save the changes
     * 
     * @return true if saved successfully
     */
    private boolean save() {
        try {
            if (type == TransmitterEditType.NEW_TRANSMITTER
                    || type == TransmitterEditType.EDIT_TRANSMITTER) {
                if (validateTransmitter()) {
                    this.previousTransmitter = new Transmitter(transmitter);
                    if (type == TransmitterEditType.NEW_TRANSMITTER) {
                        transmitter = new Transmitter();
                    }
                    transmitter.setCallSign(this.callSignTxt.getText().trim());
                    transmitter
                            .setDacPort(this.dacPortCbo.getSelectionIndex() + 1);
                    transmitter.setFipsCode(this.fipsTxt.getText().trim());
                    transmitter.setFrequency(Float.parseFloat(this.frequencyTxt
                            .getText().trim()));
                    transmitter.setLocation(locationTxt.getText().trim());
                    transmitter.setMnemonic(this.mnemonicTxt.getText().trim());
                    transmitter.setName(this.transmitterNameTxt.getText()
                            .trim());
                    transmitter.setServiceArea(this.serviceAreaTxt.getText()
                            .trim());
                    transmitter.setTransmitterGroup(group);

                    if (!saveGroup) {
                        // Save the transmitter
                        try {
                            dataManager.saveTransmitter(transmitter);
                            setReturnValue(true);
                            return true;
                        } catch (Exception e) {
                            statusHandler.error("Error saving transmitter "
                                    + transmitter.getName(), e);
                        }
                    }
                }
            }

            // TODO Fix validation on transmitter group program change
            // This causes vaildation failure because a group with that
            // that name already exists
            if (TransmitterEditType.NEW_TRANSMITTER_GROUP == type
                    || TransmitterEditType.EDIT_TRANSMITTER_GROUP == type
                    || saveGroup) {
                if (validateGroup()) {
                    if (type == TransmitterEditType.NEW_TRANSMITTER_GROUP
                            || type == TransmitterEditType.NEW_TRANSMITTER) {
                        group = new TransmitterGroup();
                    }

                    if (type == TransmitterEditType.NEW_TRANSMITTER
                            || type == TransmitterEditType.EDIT_TRANSMITTER) {
                        group.setName(mnemonicTxt.getText().trim());
                    } else {
                        group.setName(grpNameValueTxt.getText().trim());
                    }
                    group.setDac(dacCombo.getSelectionIndex() + 1);
                    group.setTimeZone(this.timeZoneCbo.getText());
                    group.setSilenceAlarm(this.disableSilenceChk.getSelection());
                    group.setDaylightSaving(this.noDstChk.getSelection());
                    group.setAdjustAudioMaxDB(Double.parseDouble(maxDbTxt
                            .getText().trim()));
                    group.setAdjustAudioMinDB(Double.parseDouble(minDbTxt
                            .getText().trim()));

                    Object obj = programCombo.getData();
                    if (obj != null && (obj instanceof List<?>)) {
                        ProgramDataManager pdm = new ProgramDataManager();
                        List<Program> progList = (List<Program>) obj;
                        Program p = progList.get(programCombo
                                .getSelectionIndex() - 1);
                        p.getTransmitterGroups().add(group);
                        pdm.saveProgram(p);
                    }

                    try {
                        if (transmitter != null) {
                            group.addTransmitter(transmitter);
                            if (saveGroup) {
                                transmitter.setTransmitterGroup(group);
                            }
                        }
                        dataManager.saveTransmitterGroup(group);
                        setReturnValue(true);
                        return true;
                    } catch (Exception e) {
                        statusHandler.error("Error saving transmitter.", e);
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error accessing database.  Changes not saved.", e);
        }

        setReturnValue(false);
        return false;
    }

    /**
     * Load the status and mode images
     */
    private void loadImages() {
        // TODO Load the correct images when they are decided
        ImageDescriptor id;
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/xmit_normal.xpm");
        statusIcon = id.createImage();

        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/xmit_normal.xpm");
        modeIcon = id.createImage();

    }

    /**
     * Validate the group values
     * 
     * @return true if all valid
     * @throws Exception
     */
    private boolean validateGroup() throws Exception {
        boolean valid = true;
        StringBuilder sb = new StringBuilder(
                "The following fields are incorrect:\n\n");
        if (type == TransmitterEditType.NEW_TRANSMITTER_GROUP) {
            if (grpNameValueTxt.getText().trim().length() == 0) {
                valid = false;
                sb.append("\tGroup Name\n");
            }
        }

        double minDb = Double.MIN_VALUE;
        double maxDb = Double.MIN_VALUE;
        if (this.minDbTxt.getText().trim().length() > 0) {
            try {
                minDb = Double.parseDouble(this.minDbTxt.getText().trim());
            } catch (NumberFormatException e) {
                valid = false;
                sb.append("\tMin dB\n");
            }
        }

        if (this.maxDbTxt.getText().trim().length() > 0) {
            try {
                maxDb = Double.parseDouble(this.maxDbTxt.getText().trim());
            } catch (NumberFormatException e) {
                valid = false;
                sb.append("\tMax dB\n");
            }
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        /*
         * Now validate the validity of the settings themselves
         */

        // Reset the message buffer
        sb.setLength(0);

        String grpName = null;
        if (type == TransmitterEditType.NEW_TRANSMITTER
                || type == TransmitterEditType.EDIT_TRANSMITTER) {
            grpName = grpNameCbo.getText();
        } else if (type == TransmitterEditType.NEW_TRANSMITTER_GROUP
                || type == TransmitterEditType.EDIT_TRANSMITTER_GROUP) {
            grpName = grpNameValueTxt.getText().trim();
        }

        if (!grpName.equals(group.getName())) {
            if (type != TransmitterEditType.EDIT_TRANSMITTER) {
                for (String name : getGroupNames()) {
                    if (name.equals(grpName)) {
                        valid = false;
                        sb.append("The Transmitter Group name must be unique\n");
                        break;
                    }
                }
            }
        }

        if (!grpName.equals(group.getName())) {
            List<Transmitter> transmitterList = dataManager.getTransmitters();
            transmitterList.remove(previousTransmitter);
            for (Transmitter t : transmitterList) {
                if (t.getMnemonic().equals(grpName)) {
                    valid = false;
                    sb.append("The Transmitter Group name must be unique\n");
                    break;
                }
            }
        }

        // Check for min db value less than max db value if db values are valid
        if (minDb >= maxDb) {
            valid = false;
            sb.append("\tMin db must be less than or equal to the Max dB\n");
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        return valid;
    }

    /**
     * Validate the Transmitter values
     * 
     * @return true if all valid
     * @throws Exception
     */
    private boolean validateTransmitter() throws Exception {
        boolean valid = true;
        StringBuilder sb = new StringBuilder(
                "The following fields are incorrect:\n\n");

        if (this.callSignTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tCall Sign\n");
        }

        if (this.frequencyTxt.getText().trim().length() > 0) {
            try {
                Float.parseFloat(this.frequencyTxt.getText().trim());
            } catch (NumberFormatException e) {
                valid = false;
                sb.append("\tFrequency\n");
            }
        }

        if (locationTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tLocation\n");
        }

        if (this.mnemonicTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tMnemonic\n");
        }

        if (this.transmitterNameTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tName\n");
        }

        if (this.serviceAreaTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tService Area\n");
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        /*
         * Now validate the validity of the settings themselves
         */

        // Reset the message buffer
        sb.setLength(0);

        // Fips Codes only for transmitters in a group of 2 or more
        if (group != null && group.getTransmitters().size() > 1) {
            // Fips code required
            if (this.fipsTxt.getText().trim().length() == 0
                    || this.fipsTxt.getText().trim().length() > 9) {
                valid = false;
                sb.append("FIPS Code is required for groups of 2 or more transmitters\n");
            }
        }

        /*
         * Check for port reuse, cannot have more than one transmitter on a port
         * unless they're daisy chained
         */
        if (group != null) {
            List<Integer> portList = new ArrayList<Integer>();
            for (Transmitter t : group.getTransmitters()) {
                portList.add(t.getDacPort());
            }
            // Remove this transmitters port
            portList.remove(transmitter.getDacPort());

            Integer selectedPort = Integer.parseInt(dacPortCbo.getText());
            if (portList.contains(selectedPort)) {
                String msg = "The selected port is already in use by "
                        + "another transmitter.  Are these transmitters daisy chained?";

                int answer = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Daisy Chained",
                        msg);
                if (answer == SWT.NO) {
                    msg = "The transmitters must be daisy chained in order to be on "
                            + "the same port.\n\nPlease select another port.\n";
                    valid = false;
                    sb.append(msg + "\n");
                }
            }
        }

        // Check for duplicate mnemonic
        String mnemonic = mnemonicTxt.getText().trim();
        List<Transmitter> transmitterList = dataManager.getTransmitters();

        // Remove the current transmitter
        transmitterList.remove(transmitter);
        for (Transmitter t : transmitterList) {
            if (t.getMnemonic().equals(mnemonic)) {
                valid = false;
                sb.append("Mnemonic must be unique.\n");
                break;
            }
        }

        String grpName = null;
        grpName = grpNameCbo.getText();

        if (grpName.equals(mnemonicTxt.getText().trim())) {
            if (group.getTransmitters().size() > 1) {
                valid = false;
                String msg = "Transmitter cannot have same mnemonic as the "
                        + "group unless there is only one transmitter in the group\n";
                sb.append(msg);
            }
        }

        /*
         * If only one transmitter in a group then a rename of the transmitter
         * also renames the group.
         */
        if (type == TransmitterEditType.EDIT_TRANSMITTER) {
            if (!mnemonicTxt.getText().trim()
                    .equals(this.transmitter.getMnemonic())) {
                if (group.getTransmitters().size() == 1) {
                    // Rename group too
                    group.setName(mnemonicTxt.getText().trim());
                    saveGroup = true;
                }
            }
        }

        if (type == TransmitterEditType.NEW_TRANSMITTER) {
            grpName = grpNameCbo.getText().trim();
            if (grpName.equals(STANDALONE)) {
                saveGroup = true;
            }
        } else {
            if (transmitter.getMnemonic().equals(group.getName())) {
                saveGroup = true;
            }
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        return valid;
    }

    private void setTransmitterStatus(TxStatus status) {
        transmitter.setTxStatus(status);
        try {
            transmitter = dataManager.saveTransmitter(transmitter);
            this.statusLbl.setText(STATUS_PREFIX + status.name());
            statusChange.statusChanged();
        } catch (Exception e) {
            statusHandler.error("Error saving Transmitter status change", e);
        }
    }

    private void setTransmitterMode(TxMode mode) {
        transmitter.setTxMode(mode);
        try {
            transmitter = dataManager.saveTransmitter(transmitter);
            this.modeLbl.setText(STATUS_PREFIX + mode.name());
            statusChange.statusChanged();
        } catch (Exception e) {
            statusHandler.error("Error saving Transmitter mode change", e);
        }
    }

    /**
     * Enable the group controls
     * 
     * @param enabled
     *            enable if true, disable if false
     */
    private void enableGroupControls(boolean enabled) {
        for (Control c : groupControlList) {
            c.setEnabled(enabled);
        }
    }

    /**
     * Enable the Transmitter controls
     * 
     * @param enabled
     *            enable if true, disable if false
     */
    private void enableTransmitterControls(boolean enabled) {
        for (Control c : transmitterControlList) {
            c.setEnabled(enabled);
        }
    }

    @Override
    protected void disposed() {
        statusIcon.dispose();
        modeIcon.dispose();
    }

    private void notImplemented() {
        DialogUtility.showMessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK,
                "Not Implemented", "This function is not yet implemented");
    }
}
