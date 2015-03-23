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
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacComparator;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.uf.viz.bmh.ui.program.ProgramSummaryNameComparator;
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
 * Aug 25, 2014     3558   rjpeter     Fix saving groups.
 * Aug 28, 2014     3432   mpduff      Added Dac info from db.
 * Sep 23, 2014     3649   rferrel     Changes to handle all types of groups.
 * Oct 13, 2014     3654   rjpeter     Updated to use ProgramSummary.
 * Oct 18, 2014    #3728   lvenable    Updated to use time zone abbreviations outside
 *                                     the time zone combo box.
 * Oct 23, 2014     3687   bsteffen    Display dac name instead of id.
 * Oct 24, 2014    #3617   dgilling    Cleaner handling of time zone, make DST
 *                                     checkbox match legacy system.
 * Oct 29, 2014    #3617   dgilling    Fix exceptions when creating new groups.
 * Jan 08, 2015     3821   bsteffen    Rename silenceAlarm to deadAirAlarm
 * Jan 13, 2015     3809   bkowal      Added {@link TransmitterLanguageComp}.
 * Jan 13, 2015     3995   rjpeter     Fix NPEs and issues with adding new transmitters
 *                                      to existing groups.
 * Jan 22, 2014     3995   rjpeter     Update to not corrupt internal state if save failed,
 *                                      fix position on new transmitter/group and group change.
 * Jan 26, 2015     4035   bkowal      Fix Transmitter form validation.
 * Feb 09, 2015     4095   bsteffen    Remove Transmitter Name.
 * 
 * Feb 09, 2015     4082   bkowal      It is now possible to save Languages with new
 *                                     Transmitter Groups.
 * Mar 18, 2015     4289   bkowal      Do not allow the user to remove the dac and/or port from
 *                                     a Transmitter in the enabled or maintenance state.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewEditTransmitterDlg extends CaveSWTDialog {
    private final String NONE = "None";

    private final String STANDALONE = "Standalone";

    private final String FIPS_CODE_VALID_PATTERN = "^[A-Z0-9]{1,9}$";

    private final String FIPS_CODE_ERROR_MSG = "FIPS Code must be between 1 and 9 characters in length\nand only numbers and upper case letters.";

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewEditTransmitterDlg.class);

    public enum TransmitterEditType {
        NEW_TRANSMITTER, NEW_TRANSMITTER_GROUP, EDIT_TRANSMITTER, EDIT_TRANSMITTER_GROUP;
    }

    /** Status Group */
    private Group statusGrp;

    /** The transmitter being edited, will be new entry for create */
    private final Transmitter transmitter;

    /** The transmitter group being edited, will be new entry for create */
    private final TransmitterGroup group;

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

    private Text mnemonicTxt;

    private Text frequencyTxt;

    private Text callSignTxt;

    private Text locationTxt;

    private Text serviceAreaTxt;

    private Text fipsTxt;

    private Text grpNameValueTxt;

    private Button saveBtn;

    /** List of group controls */
    private final List<Control> groupControlList = new ArrayList<Control>();

    /** List of transmitter controls */
    private final List<Control> transmitterControlList = new ArrayList<Control>();

    /** Used to manage the Transmitter Language(s) */
    private TransmitterLanguageComp transmitterLanguageComp;

    private final ICloseCallback callback;

    private Combo programCombo;

    private DacDataManager dacDataManager;

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
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        this.type = type;
        this.transmitter = (transmitter != null ? transmitter
                : new Transmitter());
        this.group = (this.transmitter.getTransmitterGroup() != null ? this.transmitter
                .getTransmitterGroup() : (group != null ? group
                : new TransmitterGroup()));
        this.callback = callback;
        dataManager = new TransmitterDataManager();

        switch (type) {
        case EDIT_TRANSMITTER:
            setText("Edit Transmitter - " + this.transmitter.getLocation());
            break;
        case EDIT_TRANSMITTER_GROUP:
            setText("Edit Transmitter Group - " + this.group.getName());
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

        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        // createStatusComp(mainComp);
        createAttrComp(mainComp);
        this.transmitterLanguageComp = new TransmitterLanguageComp(shell,
                this.group, this.type);
        // Disable all controls by default
        enableGroupControls(false);
        enableTransmitterControls(false);

        createBottomButtons();

        populate();
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
        if ((type == TransmitterEditType.NEW_TRANSMITTER)
                || (type == TransmitterEditType.EDIT_TRANSMITTER)) {
            grpNameCbo = new Combo(leftComp, SWT.BORDER | SWT.READ_ONLY);
            grpNameCbo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                    false));
            grpNameCbo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    groupNameSelectionAction();
                }
            });
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
        dacLbl.setText("DAC: ");
        dacLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        dacCombo = new Combo(leftComp, SWT.BORDER | SWT.READ_ONLY);
        dacCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dacCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleDacSelection();
                } catch (Exception exception) {
                    statusHandler.error("Error retreiving DAC information",
                            exception);
                }
            }
        });
        groupControlList.add(dacCombo);

        Label dacPortLbl = new Label(leftComp, SWT.NONE);
        dacPortLbl.setText("DAC Port #: ");
        dacPortLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        dacPortCbo = new Combo(leftComp, SWT.BORDER | SWT.READ_ONLY);
        dacPortCbo
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        transmitterControlList.add(dacPortCbo);

        Label progLbl = new Label(rightComp, SWT.NONE);
        progLbl.setText("Program:");
        progLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        programCombo = new Combo(rightComp, SWT.BORDER | SWT.READ_ONLY);
        programCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        this.programCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleProgramChange();
            }
        });
        groupControlList.add(programCombo);

        Label timeZoneLbl = new Label(rightComp, SWT.NONE);
        timeZoneLbl.setText("Time Zone: ");
        timeZoneLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false));

        timeZoneCbo = new Combo(rightComp, SWT.BORDER | SWT.READ_ONLY);
        timeZoneCbo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        timeZoneCbo.setItems(BMHTimeZone.getUISelections());
        timeZoneCbo.select(0);
        timeZoneCbo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String timeZoneName = timeZoneCbo.getText();
                if (BMHTimeZone.isForcedNoDst(timeZoneName)) {
                    noDstChk.setSelection(true);
                    noDstChk.setEnabled(false);
                } else {
                    String timeZoneID = group.getTimeZone();
                    TimeZone defaultTZ = (timeZoneID != null) ? TimeZone
                            .getTimeZone(timeZoneID) : TimeZone.getDefault();
                    noDstChk.setSelection(!defaultTZ.observesDaylightTime());
                    noDstChk.setEnabled(true);
                }
            }
        });
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
        groupControlList.add(noDstChk); // Individual transmitter settings

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
        fipsTxt.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                e.text = e.text.toUpperCase();
            }
        });
        transmitterControlList.add(fipsTxt);
    }

    private void createBottomButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        saveBtn = new Button(comp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (save()) {
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

        if (grpNameCbo != null) {
            grpNameCbo.setItems(getGroupNames());
            grpNameCbo.add(STANDALONE, 0);

            int idx = -1;
            if (group.getName() != null) {
                idx = grpNameCbo.indexOf(group.getName());
            }
            if (idx >= 0) {
                grpNameCbo.select(idx);
            } else {
                grpNameCbo.select(0);
            }
        }

        try {
            dacDataManager = new DacDataManager();
            List<Dac> dacList = dacDataManager.getDacs();
            Collections.sort(dacList, new DacComparator());
            if (dacList.size() > 0) {
                String[] dacs = new String[dacList.size()];
                for (int i = 0; i < dacList.size(); i++) {
                    Dac d = dacList.get(i);
                    dacs[i] = String.valueOf(d.getName());
                }

                dacCombo.setItems(dacs);
                dacCombo.add(NONE, 0);
                dacCombo.select(0);
            }

        } catch (Exception e) {
            statusHandler.error("Error gettign Dac information", e);
            dacCombo.add(NONE, 0);
            dacCombo.select(0);
            if (group.getDac() != null) {
                dacCombo.add(String.valueOf(group.getDac()), 1);
            }
        }

        populateProgramCombo();

        // Populate the fields
        if (group.getDac() != null) {
            // TODO - When getting dac numbers from DB need to change this logic
            try {
                setCboSelect(dacCombo,
                        dacDataManager.getDacNameById(group.getDac()));
                handleDacSelection();
                Integer dacPort = null;
                if (transmitter != null) {
                    dacPort = transmitter.getDacPort();
                }
                setCboSelect(dacPortCbo, dacPort);
            } catch (Exception e) {
                statusHandler.error("Error retrieving Dac data", e);
                dacCombo.select(0);
                dacPortCbo.setItems(new String[0]);
                dacPortCbo.select(0);
            }
        }

        populateTimeZoneControls(group);

        disableSilenceChk.setSelection(!group.getDeadAirAlarm());

        enabled = (group.isStandalone()
                || (TransmitterEditType.EDIT_TRANSMITTER_GROUP == type)
                || (TransmitterEditType.NEW_TRANSMITTER_GROUP == type) || (TransmitterEditType.NEW_TRANSMITTER == type));

        // Enable/Disable controls
        enableGroupControls(enabled);

        if (transmitter.getDacPort() != null) {
            setCboSelect(dacPortCbo, transmitter.getDacPort());
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

        // Enable/Disable controls
        enabled = ((TransmitterEditType.EDIT_TRANSMITTER == type) || (TransmitterEditType.NEW_TRANSMITTER == type));
        enableTransmitterControls(enabled);

        if (TransmitterEditType.NEW_TRANSMITTER == type) {
            transmitter.setTxStatus(TxStatus.DISABLED);
        }

        if (TransmitterEditType.NEW_TRANSMITTER_GROUP != type) {
            if (grpNameCbo != null) {
                if (group.isStandalone()) {
                    grpNameCbo.select(0);
                } else {
                    int selIndex = -1;
                    if (group.getName() != null) {
                        selIndex = grpNameCbo.indexOf(group.getName());
                    }
                    if (selIndex < 0) {
                        selIndex = 0;
                    }
                    grpNameCbo.select(selIndex);
                }
                String groupName = grpNameCbo.getText();
                groupNameSelectionAction();
                populateGroupControls(groupName);
            }
        }
    }

    /**
     * populate the dacPortCbo based on current dacCombo selection.
     */
    private void handleDacSelection() throws Exception {
        int dac = -1;
        if (dacCombo.getSelectionIndex() > 0) {
            dac = dacDataManager.getDacIdByName(dacCombo.getText());

            // TODO get real data?
            dacPortCbo.setItems(new String[] { "1", "2", "3", "4" });

        } else {
            dacPortCbo.setItems(new String[0]);
        }
        dacPortCbo.add(NONE, 0);

        // Only select transmitter's dac port when its group dac is selected.
        if ((transmitter == null)
                || (transmitter.getTransmitterGroup() == null)
                || (transmitter.getTransmitterGroup().getDac() == null)
                || (!transmitter.getTransmitterGroup().getDac().equals(dac))) {
            dacPortCbo.select(0);
        } else {
            setCboSelect(dacPortCbo, transmitter.getDacPort());
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
            /*
             * This programList only has id and name populated for each program.
             */
            List<ProgramSummary> programList = dataManager
                    .getProgramSummaries();
            Collections.sort(programList, new ProgramSummaryNameComparator());

            ProgramSummary origProgram = group.getProgramSummary();

            int groupProgramId = -1;
            if (origProgram != null) {
                groupProgramId = origProgram.getId();
            }

            String[] programs = new String[programList.size()];
            int select = -1;
            int i = 0;
            for (ProgramSummary p : programList) {
                programs[i] = p.getName();
                if (groupProgramId == p.getId()) {
                    select = i;
                }
                i++;
            }

            programCombo.setData(programList);
            programCombo.setItems(programs);
            programCombo.add(NONE, 0);
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
            groupName = group.getName();
        } else {
            enableGroupControls(false);
        }
        populateGroupControls(groupName);
    }

    /**
     * Sets the combo's selection to select or first entry if select is null or
     * not in the combo's list.
     * 
     * @param combo
     * @param select
     */
    private void setCboSelect(Combo combo, Object select) {
        int selIndex = -1;
        if (select != null) {
            selIndex = combo.indexOf(select.toString());
        }
        if (selIndex < 0) {
            selIndex = 0;
        }
        combo.select(selIndex);
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
                try {
                    setCboSelect(dacCombo,
                            dacDataManager.getDacNameById(tg.getDac()));
                    handleDacSelection();
                } catch (Exception e) {
                    statusHandler.error("Error retreiving DAC information", e);
                }

                populateTimeZoneControls(tg);

                if (tg.getProgramSummary() == null) {
                    programCombo.select(0);
                    this.transmitterLanguageComp.setSelectedProgram(null);
                } else {
                    programCombo.select(programCombo.indexOf(tg
                            .getProgramSummary().getName()));
                    this.transmitterLanguageComp.setSelectedProgram(tg
                            .getProgramSummary());
                }

                disableSilenceChk.setSelection(!tg.getDeadAirAlarm());
                return;
            }
        }

        // Assume stand alone
        if (group.isStandalone() && (group.getDac() != null)) {
            try {
                setCboSelect(dacCombo,
                        dacDataManager.getDacNameById(group.getDac()));
                handleDacSelection();
            } catch (Exception e) {
                statusHandler.error("Error retreiving DAC information", e);
            }
            setCboSelect(dacPortCbo, transmitter.getDacPort());
        } else {
            dacCombo.select(0);
            dacPortCbo.select(0);
        }
    }

    /**
     * Save the changes
     * 
     * @return true if saved successfully
     */

    private boolean save() {
        boolean saveGroup = false;

        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        try {
            TransmitterGroup tg = null;
            if ((type == TransmitterEditType.NEW_TRANSMITTER)
                    || (type == TransmitterEditType.EDIT_TRANSMITTER)) {
                if (this.validateTransmitterForm() == false) {
                    return false;
                }
                Transmitter transToBeSaved = getSelectedTransmitter();
                tg = transToBeSaved.getTransmitterGroup();

                if (validateTransmitter(transToBeSaved)) {
                    saveGroup = checkGroupUpdate(tg);

                    if (!saveGroup) {
                        // Save the transmitter
                        try {
                            if (group.isStandalone() && !tg.isStandalone()) {
                                transToBeSaved = dataManager
                                        .saveTransmitterDeleteGroup(
                                                transToBeSaved, group);
                                setReturnValue(transToBeSaved);
                                return true;
                            } else {
                                transToBeSaved = dataManager
                                        .saveTransmitter(transToBeSaved);
                                setReturnValue(transToBeSaved);
                                return true;
                            }
                        } catch (Exception e) {
                            statusHandler.error("Error saving transmitter "
                                    + transmitter.getLocation(), e);
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            if ((TransmitterEditType.NEW_TRANSMITTER_GROUP == type)
                    || (TransmitterEditType.EDIT_TRANSMITTER_GROUP == type)
                    || saveGroup) {
                if (tg == null) {
                    tg = getSelectedTransmitterGroup();
                }

                if (validateGroup(tg)) {
                    /*
                     * Just save the Transmitter Group.
                     */
                    try {
                        List<TransmitterGroup> savedGroup = dataManager
                                .saveTransmitterGroup(
                                        tg,
                                        this.transmitterLanguageComp
                                                .getUnsavedTransmitterLanguages());
                        if ((savedGroup != null) && !savedGroup.isEmpty()) {
                            tg = savedGroup.get(0);
                        }

                        setReturnValue(tg);
                        return true;
                    } catch (Exception e) {
                        statusHandler.error("Error saving transmitter.", e);
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error accessing database.  Changes not saved.", e);
        } finally {
            shell.setCursor(null);
        }

        return false;
    }

    /**
     * Check to see if group needs to be updated.
     * 
     * @return true if changes to group values
     * @throws Exception
     */
    private boolean checkGroupUpdate(TransmitterGroup tg) {
        if (tg.isStandalone()) {
            if (!tg.getName().equals(group.getName())) {
                return true;
            }

            if (!tg.getTimeZone().equals(group.getTimeZone())) {
                return true;
            }

            if (tg.getDeadAirAlarm() != (group.getDeadAirAlarm())) {
                return true;
            }

            ProgramSummary groupProgram = group.getProgramSummary();
            if (groupProgram == null) {
                if (programCombo.getSelectionIndex() > 0) {
                    return true;
                }
            } else if (!programCombo.getText().equals(groupProgram.getName())) {
                return true;
            }

            Integer dac = group.getDac();
            if (dac == null) {
                if (tg.getDac() != null) {
                    return true;
                }
            } else if (!dac.equals(tg.getDac())) {
                return true;
            }

        }
        return false;
    }

    /**
     * Validate the group values
     * 
     * @return true if all valid
     * @throws Exception
     */
    private boolean validateGroup(TransmitterGroup tg) throws Exception {
        boolean valid = true;
        StringBuilder sb = new StringBuilder(
                "The following fields are incorrect:\n\n");
        if (type == TransmitterEditType.NEW_TRANSMITTER_GROUP) {
            if (tg.getName().trim().length() == 0) {
                valid = false;
                sb.append("\tGroup Name\n");
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

        String grpName = tg.getName();

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

            List<Transmitter> transmitterList = dataManager.getTransmitters();
            transmitterList.remove(transmitter);
            for (Transmitter t : transmitterList) {
                if (t.getMnemonic().equals(grpName)) {
                    valid = false;
                    sb.append("The Transmitter Group name must be unique\n");
                    break;
                }
            }
        }

        if (tg.getDac() == null) {
            Set<Transmitter> enabledTransmitters = tg.getEnabledTransmitters();
            Set<Transmitter> maintenanceTransmitters = tg
                    .getTransmitterWithStatus(TxStatus.MAINT);
            boolean transmittersEnabled = enabledTransmitters.isEmpty() == false;
            boolean transmittersMaintenance = maintenanceTransmitters.isEmpty() == false;

            if (transmittersEnabled || transmittersMaintenance) {
                StringBuilder configError = new StringBuilder(
                        "You cannot remove the dac from Transmitter Group ");
                configError.append(tg.getName()).append(
                        " because Transmitters: ");
                if (transmittersEnabled) {
                    boolean first = true;
                    for (Transmitter transmitter : enabledTransmitters) {
                        if (first) {
                            first = false;
                        } else {
                            configError.append(", ");
                        }
                        configError.append(transmitter.getMnemonic());
                    }
                    configError.append(" are in the  ")
                            .append(TxStatus.ENABLED.name()).append(" state");
                    if (transmittersMaintenance) {
                        configError.append(" and Transmitters: ");
                    }
                }
                if (transmittersMaintenance) {
                    boolean first = true;
                    for (Transmitter transmitter : maintenanceTransmitters) {
                        if (first) {
                            first = false;
                        } else {
                            configError.append(", ");
                        }
                        configError.append(transmitter.getMnemonic());
                    }
                    configError.append(" are in the  ")
                            .append(TxStatus.MAINT.name()).append(" state");
                }
                configError.append(".");

                sb.append(configError.toString()).append("\n");
                valid = false;
            }
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        return valid;
    }

    private boolean validateTransmitterForm() {
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
        } else {
            /*
             * frequency is still invalid if it has not been specified.
             */
            valid = false;
            sb.append("\tFrequency\n");
        }

        if (locationTxt.getText().trim().length() == 0) {
            valid = false;
            sb.append("\tLocation\n");
        }

        String mnemonic = this.mnemonicTxt.getText().trim();
        if ((mnemonic.length() == 0) || (mnemonic.length() > 5)) {
            valid = false;
            sb.append("\tMnemonic must be btween 1 and 5 characters in length.\n");
        }

        String callSign = this.callSignTxt.getText().trim();
        if ((callSign.length() == 0) || (callSign.length() > 10)) {
            valid = false;
            sb.append("\tCall Sign must be 1 to 10 characters in length.\n");
        }

        String location = this.locationTxt.getText().trim();
        if ((location.length() == 0) || (location.length() > 40)) {
            valid = false;
            sb.append("\tLocation must be 1 to 40 characters in length.\n");
        }

        String serviceArea = this.serviceAreaTxt.getText().trim();
        if ((serviceArea.length() == 0) || (serviceArea.length() > 40)) {
            valid = false;
            sb.append("\tService Area must be 1 to 40 characters in length.\n");
        }

        String fipsCode = fipsTxt.getText().trim();
        if ((fipsCode.length() > 0)
                && !fipsCode.matches(FIPS_CODE_VALID_PATTERN)) {
            valid = false;
            sb.append(FIPS_CODE_ERROR_MSG);
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return false;
        }

        return valid;
    }

    /**
     * Validate the Transmitter values
     * 
     * @return true if all valid
     * @throws Exception
     */
    private boolean validateTransmitter(Transmitter transToBeSaved)
            throws Exception {
        boolean valid = true;

        /*
         * Validate the validity of the settings themselves
         */

        StringBuilder sb = new StringBuilder();

        // Check for valid DAC and port.
        if (transToBeSaved.getDacPort() != null) {
            TransmitterGroup selGroup = transToBeSaved.getTransmitterGroup();
            Integer dac = transToBeSaved.getTransmitterGroup().getDac();
            Integer dacPort = transToBeSaved.getDacPort();
            List<TransmitterGroup> groups = dataManager.getTransmitterGroups();

            if (dac != null) {
                // Check that DAC port not in use in some other group.
                for (TransmitterGroup grp : groups) {
                    if (!grp.equals(selGroup)) {
                        if (dac.equals(grp.getDac())) {
                            for (Transmitter trans : grp.getTransmitterList()) {
                                if (!trans.equals(transmitter)
                                        && dacPort.equals(trans.getDacPort())) {
                                    valid = false;
                                    if (grp.isStandalone()) {
                                        sb.append(
                                                "DAC's Port already in use by stand alone transmitter ")
                                                .append(trans.getMnemonic())
                                                .append(".\n");
                                    } else {
                                        sb.append(
                                                "DAC's Port already in use by transmitter ")
                                                .append(trans.getMnemonic())
                                                .append(" in group ")
                                                .append(grp.getName())
                                                .append(".\n");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Check for valid FIPS code
            if (valid) {
                for (Transmitter trans : selGroup.getTransmitterList()) {
                    if (!trans.equals(transToBeSaved)
                            && dacPort.equals(trans.getDacPort())) {
                        String fips = this.fipsTxt.getText().trim();
                        if (fips.length() == 0) {
                            valid = false;
                            sb.append("FIPS Code is required for transmitters on the same dac and port\n");
                            break;
                        } else if (!fips.matches(FIPS_CODE_VALID_PATTERN)) {
                            sb.append(FIPS_CODE_ERROR_MSG);
                            break;
                        } else {
                            String tFips = trans.getFipsCode();
                            if ((tFips == null) || (tFips.trim().length() == 0)) {
                                tFips = getFipsCode(
                                        selGroup.getTransmitterList(), trans);
                                if (tFips == null) {
                                    return false;
                                } else {
                                    trans.setFipsCode(tFips);
                                    try {
                                        dataManager.saveTransmitter(trans);
                                    } catch (Exception e) {
                                        statusHandler.handle(Priority.PROBLEM,
                                                "Unable to update FIPS code for "
                                                        + trans.getMnemonic(),
                                                e);
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * Check for port reuse, cannot have more than one transmitter on a
             * port unless they're daisy chained
             */
            List<Integer> portList = new ArrayList<Integer>();
            for (Transmitter t : transToBeSaved.getTransmitterGroup()
                    .getTransmitters()) {
                if (!t.equals(transToBeSaved)) {
                    portList.add(t.getDacPort());
                }
            }

            if (portList.contains(dacPort)) {
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

        /*
         * Verify that the user is not attempting to remove the port and/or dac
         * from a transmitter that is currently active. Transmitters in
         * Maintenance mode also must remain properly configured to function
         * correctly.
         */
        if (transToBeSaved.getTxStatus() == TxStatus.ENABLED
                || transToBeSaved.getTxStatus() == TxStatus.MAINT) {
            if (transToBeSaved.getDacPort() == null) {
                String msg = "You cannot remove the port from a Transmitter in the "
                        + transToBeSaved.getTxStatus().name() + " state.";
                valid = false;
                sb.append(msg).append("\n");
            }

            if (transToBeSaved.getTransmitterGroup().getDac() == null) {
                String msg = "You cannot remove the dac from a Transmitter in the "
                        + transToBeSaved.getTxStatus().name() + " state.";
                valid = false;
                sb.append(msg).append("\n");
            }
        }

        // Check for duplicate mnemonic
        List<Transmitter> transmitterList = dataManager.getTransmitters();

        final String mnemonic = this.mnemonicTxt.getText().trim();

        // Remove the previous transmitter
        transmitterList.remove(transmitter);
        for (Transmitter t : transmitterList) {
            if (t.getMnemonic().equals(mnemonic)) {
                valid = false;
                sb.append("Mnemonic must be unique.\n");
                break;
            }
        }

        if (grpNameCbo.getSelectionIndex() > 0) {
            String grpName = grpNameCbo.getText();

            if (grpName.equals(mnemonicTxt.getText().trim())) {
                if (transToBeSaved.getTransmitterGroup().getTransmitters()
                        .size() > 1) {
                    valid = false;
                    String msg = "Transmitter cannot have same mnemonic as the "
                            + "group unless there is only one transmitter in the group\n";
                    sb.append(msg);
                }
            }
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR, "Invalid",
                    sb.toString());
            return valid;
        }

        return valid;
    }

    /**
     * Get a valid FIPS Code for transmitter.
     * 
     * @param transmitters
     *            - list to check for duplicates
     * @param transmitter
     *            - transmitter code is for
     * @return ftpsCode - null if user cancels else valid code.
     */
    private String getFipsCode(final List<Transmitter> transmitters,
            final Transmitter transmitter) {
        IInputTextValidator textValidator = new IInputTextValidator() {

            @Override
            public boolean validateInputText(Shell shell, String text) {
                String value = text.trim();
                if (!value.matches(FIPS_CODE_VALID_PATTERN)) {
                    DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR
                            | SWT.OK, "FIPS Code Error", FIPS_CODE_ERROR_MSG);
                    return false;
                }
                if (!uniqueFipsCode(transmitters, transmitter, value)) {
                    DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR
                            | SWT.OK, "FIPS Code Error",
                            "FIPS Code must be unique.");
                    return false;
                }
                return true;
            }
        };
        InputTextDlg dlg = new InputTextDlg(shell, "Update - "
                + transmitter.getMnemonic(), transmitter.getMnemonic()
                + " must now have a FIPS Code: ", null, textValidator, false,
                true);
        dlg.open();

        if (dlg.getReturnValue() == null) {
            return null;
        }
        return dlg.getReturnValue().toString().trim();
    }

    /**
     * Checks the group's transmitters to see if the new FIPS code for
     * transmitter is unique.
     * 
     * @param transmitters
     * @param transmitter
     * @param fipsCode
     * @return isUnique
     */
    private boolean uniqueFipsCode(List<Transmitter> transmitters,
            Transmitter transmitter, String fipsCode) {
        for (Transmitter t : transmitters) {
            if (!transmitter.equals(t)) {
                if (fipsCode.equals(t.getFipsCode())) {
                    return false;
                }
            }
        }
        return true;
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
        this.transmitterLanguageComp.enableGroupControls(enabled);
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
        callback.dialogClosed(getReturnValue());
    }

    /**
     * Set the selection on the Time Zone Combo based on zone value in group.
     */
    private void populateTimeZoneControls(TransmitterGroup tg) {
        String timeZoneID = tg.getTimeZone();
        TimeZone tz = (timeZoneID != null) ? TimeZone.getTimeZone(timeZoneID)
                : TimeZone.getDefault();

        TimeZone normalizedTZ = BMHTimeZone.getTimeZone(tz,
                tz.observesDaylightTime());
        String timeZone = BMHTimeZone.getTimeZoneUIName(normalizedTZ);
        int index = timeZoneCbo.indexOf(timeZone);
        timeZoneCbo.select(index);

        noDstChk.setSelection(!tz.observesDaylightTime());
        if (BMHTimeZone.isForcedNoDst(timeZone)) {
            noDstChk.setEnabled(false);
        }
    }

    /**
     * Returns TransmitterGroup object populated from current selections.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private TransmitterGroup getSelectedTransmitterGroup() throws Exception {
        TransmitterGroup rval = new TransmitterGroup();
        String groupName = null;
        boolean populateFromComboBoxes = true;

        if (TransmitterEditType.EDIT_TRANSMITTER_GROUP.equals(type)) {
            // populate initially from previous group
            rval = new TransmitterGroup(group);
            groupName = grpNameValueTxt.getText().trim();
        } else if (TransmitterEditType.NEW_TRANSMITTER_GROUP.equals(type)) {
            groupName = grpNameValueTxt.getText().trim();
        } else if (TransmitterEditType.NEW_TRANSMITTER.equals(type)
                || TransmitterEditType.EDIT_TRANSMITTER.equals(type)) {
            groupName = grpNameCbo.getText();
            if (groupName.equals(STANDALONE)) {
                groupName = this.mnemonicTxt.getText().trim();

                if (TransmitterEditType.EDIT_TRANSMITTER.equals(type)
                        && group.isStandalone()) {
                    // still standalone, populate from previous group
                    rval = new TransmitterGroup(group);
                }
            } else {
                // pull data from DB
                populateFromComboBoxes = false;
                List<TransmitterGroup> groups = dataManager
                        .getTransmitterGroups();
                for (TransmitterGroup tg : groups) {
                    if (tg.getName().equals(groupName)) {
                        rval = tg;
                        break;
                    }
                }
            }
        }

        if (populateFromComboBoxes) {
            rval.setName(groupName);
            Integer dac = null;
            if (this.dacCombo.getSelectionIndex() > 0) {
                dac = dacDataManager.getDacIdByName(this.dacCombo.getText());
            }
            rval.setDac(dac);
            ProgramSummary p = null;
            if (programCombo.getSelectionIndex() > 0) {
                List<ProgramSummary> progList = (List<ProgramSummary>) programCombo
                        .getData();
                p = progList.get(programCombo.getSelectionIndex() - 1);
            }
            rval.setProgramSummary(p);
            TimeZone groupTz = BMHTimeZone.getTimeZoneFromUI(
                    timeZoneCbo.getText(), !noDstChk.getSelection());
            rval.setTimeZone(groupTz.getID());
            rval.setDeadAirAlarm(!this.disableSilenceChk.getSelection());
        }

        return rval;
    }

    @SuppressWarnings("unchecked")
    private void handleProgramChange() {
        if (programCombo.getSelectionIndex() <= 0) {
            this.transmitterLanguageComp.setSelectedProgram(null);
        } else {
            List<ProgramSummary> progList = (List<ProgramSummary>) programCombo
                    .getData();
            this.transmitterLanguageComp.setSelectedProgram(progList
                    .get(programCombo.getSelectionIndex() - 1));
        }
    }

    /**
     * Returns Transmitter object populated from current selections.
     * 
     * @return
     */
    private Transmitter getSelectedTransmitter() throws Exception {
        Transmitter rval = new Transmitter(transmitter);
        rval.setCallSign(this.callSignTxt.getText().trim());
        Integer selectedPort = null;
        if (this.dacPortCbo.getSelectionIndex() > 0) {
            selectedPort = Integer.parseInt(this.dacPortCbo.getText());
        }
        rval.setDacPort(selectedPort);
        rval.setFipsCode(this.fipsTxt.getText().trim());
        rval.setFrequency(Float.parseFloat(this.frequencyTxt.getText().trim()));
        rval.setLocation(locationTxt.getText().trim());
        rval.setMnemonic(this.mnemonicTxt.getText().trim());
        rval.setServiceArea(this.serviceAreaTxt.getText().trim());
        TransmitterGroup tg = getSelectedTransmitterGroup();

        if (!tg.equals(rval.getTransmitterGroup())) {
            // set transmitter position within group
            int maxPosition = 0;
            for (Transmitter t : tg.getTransmitters()) {
                maxPosition = Math.max(maxPosition, t.getPosition());
            }

            rval.setPosition(maxPosition + 1);
        }
        tg.addTransmitter(rval);
        return rval;
    }
}
