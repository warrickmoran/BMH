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
 * 
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

    private final ITransmitterStatusChange statusChange;

    /** Status Group */
    private Group statusGrp;

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

    private Button saveBtn;

    /** List of group controls */
    private final List<Control> groupControlList = new ArrayList<Control>();

    /** List of transmitter controls */
    private final List<Control> transmitterControlList = new ArrayList<Control>();

    /** Save group flag, save the TransmitterGroup if true */
    private boolean saveGroup = false;

    private final ICloseCallback callback;

    private Transmitter previousTransmitter;

    private Combo programCombo;

    private DacDataManager dacDataManager;

    private final boolean prevIsStandalone;

    private final TransmitterGroup prevGroup;

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
        this.transmitter = transmitter;
        this.group = group;
        this.callback = callback;
        this.statusChange = statusChange;
        dataManager = new TransmitterDataManager();

        if (group != null) {
            prevIsStandalone = group.isStandalone();
            prevGroup = group;
        } else if (transmitter != null) {
            prevGroup = transmitter.getTransmitterGroup();
            prevIsStandalone = prevGroup.isStandalone();
        } else {
            prevIsStandalone = false;
            prevGroup = null;
        }

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

        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        // createStatusComp(mainComp);
        createAttrComp(mainComp);

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
                    TimeZone tz = TimeZone.getTimeZone(group.getTimeZone());
                    noDstChk.setSelection(!tz.observesDaylightTime());
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
        fipsTxt.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                e.text = e.text.toUpperCase();
            }
        });
        transmitterControlList.add(fipsTxt);

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

            if (group == null) {
                grpNameCbo.select(0);
            } else {
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
            if (group != null) {
                dacCombo.add(String.valueOf(group.getDac()), 1);
            }
        }

        populateProgramCombo();

        // Populate the fields
        enabled = ((TransmitterEditType.EDIT_TRANSMITTER_GROUP == type) || (TransmitterEditType.NEW_TRANSMITTER_GROUP == type));
        if ((group != null) || enabled) {
            if (group == null) {
                group = new TransmitterGroup();
            }
            if (group.getDac() != null) {
                // TODO - When getting dac numbers from DB need to change this
                // logic
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

            populateTimeZoneControls();

            if (group.getSilenceAlarm() != null) {
                disableSilenceChk.setSelection(group.getSilenceAlarm());
            }

            if ((group != null) && group.isStandalone()) {
                enabled = true;
            }

            // Enable/Disable controls
            enableGroupControls(enabled);
        }

        enabled = ((TransmitterEditType.EDIT_TRANSMITTER == type) || (TransmitterEditType.NEW_TRANSMITTER == type));

        if ((transmitter != null) || enabled) {
            if (transmitter == null) {
                transmitter = new Transmitter();
            }
            setCboSelect(dacPortCbo, transmitter.getDacPort());

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

            // Enable/Disable controls
            enableTransmitterControls(enabled);
            if (TransmitterEditType.NEW_TRANSMITTER == type) {
                enableGroupControls(enabled);
            }
        }

        if (TransmitterEditType.NEW_TRANSMITTER == type) {
            setTransmitterStatus(TxStatus.DISABLED);
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

            ProgramSummary origProgram = null;
            if (group != null) {
                origProgram = group.getProgramSummary();
            }
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
            groupName = prevGroup.getName();
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
                group = tg;
                try {
                    setCboSelect(dacCombo,
                            dacDataManager.getDacNameById(group.getDac()));
                    handleDacSelection();
                } catch (Exception e) {
                    statusHandler.error("Error retreiving DAC information", e);
                }
                
                populateTimeZoneControls();

                if (group.getProgramSummary() == null) {
                    programCombo.select(0);
                } else {
                    programCombo.select(programCombo.indexOf(group.getProgramSummary()
                            .getName()));
                }

                if (group.getSilenceAlarm() != null) {
                    disableSilenceChk.setSelection(group.getSilenceAlarm());
                }

                return;
            }
        }

        // Assume stand alone
        if ((transmitter != null)
                && (transmitter.getTransmitterGroup() != null)
                && transmitter.getTransmitterGroup().isStandalone()) {
            try {
                setCboSelect(dacCombo,
                        dacDataManager.getDacNameById(transmitter
                                .getTransmitterGroup().getDac()));
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
        saveGroup = false;

        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        try {
            if ((type == TransmitterEditType.NEW_TRANSMITTER)
                    || (type == TransmitterEditType.EDIT_TRANSMITTER)) {
                this.previousTransmitter = new Transmitter(transmitter);

                if (validateTransmitter()) {
                    if (!prevIsStandalone
                            && STANDALONE.equals(grpNameCbo.getText())) {
                        saveGroup = true;
                        group = new TransmitterGroup();
                    }

                    if (type == TransmitterEditType.NEW_TRANSMITTER) {
                        transmitter = new Transmitter();
                    }
                    transmitter.setCallSign(this.callSignTxt.getText().trim());
                    Integer dac = null;
                    if (this.dacCombo.getSelectionIndex() > 0) {
                        dac = dacDataManager.getDacIdByName(this.dacCombo
                                .getText());
                    }

                    if ((dac == null && group.getDac() != null)
                            || (dac != null && !dac.equals(group.getDac()))) {
                        group.setDac(dac);
                        saveGroup = true;
                    }

                    if (programCombo.getSelectionIndex() > 0) {
                        @SuppressWarnings("unchecked")
                        List<ProgramSummary> progList = (List<ProgramSummary>) programCombo
                                .getData();
                        ProgramSummary p = progList.get(programCombo
                                .getSelectionIndex() - 1);
                        ProgramSummary origProgram = group.getProgramSummary();

                        if ((origProgram == null)
                                || (p.getId() != origProgram.getId())) {
                            group.setProgramSummary(p);
                            saveGroup = true;
                        }
                    } else if (group.getProgramSummary() != null) {
                        /*
                         * None selected and a program used to assigned
                         */
                        group.setProgramSummary(null);
                        saveGroup = true;
                    }

                    Integer selectedPort = null;
                    if (this.dacPortCbo.getSelectionIndex() > 0) {
                        selectedPort = Integer.parseInt(this.dacPortCbo
                                .getText());
                    }

                    transmitter.setDacPort(selectedPort);
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
                    transmitter.setTxMode(this.previousTransmitter.getTxMode());
                    transmitter.setTxStatus(this.previousTransmitter
                            .getTxStatus());

                    if (!saveGroup) {
                        // Save the transmitter
                        try {
                            if (prevIsStandalone && !group.isStandalone()) {
                                TransmitterGroup saGroup = previousTransmitter
                                        .getTransmitterGroup();
                                transmitter = dataManager
                                        .saveTransmitterDeleteGroup(
                                                transmitter, saGroup);
                                setReturnValue(transmitter);
                                return true;
                            } else {
                                saveGroup = checkGroupUpdate();
                                if (!saveGroup) {
                                    transmitter = dataManager
                                            .saveTransmitter(transmitter);
                                    setReturnValue(transmitter);
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            statusHandler.error("Error saving transmitter "
                                    + transmitter.getName(), e);
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            // TODO Fix validation on transmitter group program change
            // This causes validation failure because a group with that
            // name already exists
            if ((TransmitterEditType.NEW_TRANSMITTER_GROUP == type)
                    || (TransmitterEditType.EDIT_TRANSMITTER_GROUP == type)
                    || saveGroup) {
                if (validateGroup()) {
                    if ((type == TransmitterEditType.NEW_TRANSMITTER_GROUP)
                            || (type == TransmitterEditType.NEW_TRANSMITTER)) {
                        group = new TransmitterGroup();
                    }

                    if ((type == TransmitterEditType.NEW_TRANSMITTER)
                            || (type == TransmitterEditType.EDIT_TRANSMITTER)) {
                        group.setName(mnemonicTxt.getText().trim());
                    } else {
                        group.setName(grpNameValueTxt.getText().trim());
                    }
                    Integer dac = null;
                    if (dacCombo.getSelectionIndex() > 0) {
                        dac = dacDataManager.getDacIdByName(dacCombo.getText());
                    }
                    group.setDac(dac);
                    TimeZone groupTz = BMHTimeZone.getTimeZoneFromUI(
                            timeZoneCbo.getText(), !noDstChk.getSelection());
                    group.setTimeZone(groupTz.getID());
                    group.setSilenceAlarm(this.disableSilenceChk.getSelection());

                    if (programCombo.getSelectionIndex() > 0) {
                        @SuppressWarnings("unchecked")
                        List<ProgramSummary> progList = (List<ProgramSummary>) programCombo
                                .getData();
                        ProgramSummary p = progList.get(programCombo
                                .getSelectionIndex() - 1);
                        ProgramSummary origProgram = group.getProgramSummary();

                        if ((origProgram == null)
                                || (p.getId() != origProgram.getId())) {
                            group.setProgramSummary(p);
                            saveGroup = true;
                        }
                    } else if (group.getProgramSummary() != null) {
                        /*
                         * None selected and a program used to assigned
                         */
                        group.setProgramSummary(null);
                        saveGroup = true;
                    }

                    try {
                        if (transmitter != null) {
                            group.addTransmitter(transmitter);
                            if (saveGroup) {
                                transmitter.setTransmitterGroup(group);
                            }
                        }
                        List<TransmitterGroup> savedGroup = dataManager
                                .saveTransmitterGroup(group);
                        if ((savedGroup != null) && !savedGroup.isEmpty()) {
                            group = savedGroup.get(0);
                        }

                        setReturnValue(group);
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
    private boolean checkGroupUpdate() {
        TimeZone tz = BMHTimeZone.getTimeZoneFromUI(timeZoneCbo.getText(),
                !noDstChk.getSelection());
        if (!tz.getID().equals(group.getTimeZone())) {
            return true;
        }
        if (disableSilenceChk.getSelection() != group.getSilenceAlarm()) {
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
        return false;
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
        if ((type == TransmitterEditType.NEW_TRANSMITTER)
                || (type == TransmitterEditType.EDIT_TRANSMITTER)) {
            grpName = grpNameCbo.getText();
        } else if ((type == TransmitterEditType.NEW_TRANSMITTER_GROUP)
                || (type == TransmitterEditType.EDIT_TRANSMITTER_GROUP)) {
            grpName = grpNameValueTxt.getText().trim();
        }

        if (group != null) {
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
                List<Transmitter> transmitterList = dataManager
                        .getTransmitters();
                transmitterList.remove(previousTransmitter);
                for (Transmitter t : transmitterList) {
                    if (t.getMnemonic().equals(grpName)) {
                        valid = false;
                        sb.append("The Transmitter Group name must be unique\n");
                        break;
                    }
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

        String mnemonic = this.mnemonicTxt.getText().trim();
        if ((mnemonic.length() == 0) || (mnemonic.length() > 5)) {
            valid = false;
            sb.append("\tMnemonic must be btween 1 and 5 characters in length.\n");
        }

        String transmitterName = this.transmitterNameTxt.getText().trim();
        if ((transmitterName.length() == 0) || (transmitterName.length() > 40)) {
            valid = false;
            sb.append("\tName must be 1 to 40 characters in length.\n");
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
            return valid;
        }

        /*
         * Now validate the validity of the settings themselves
         */

        // Reset the message buffer
        sb.setLength(0);

        // Check for valid DAC and port.
        if (dacPortCbo.getSelectionIndex() > 0) {
            Integer dac = dacDataManager
                    .getDacIdByName(this.dacCombo.getText());
            Integer dacPort = new Integer(dacPortCbo.getText());
            List<TransmitterGroup> groups = dataManager.getTransmitterGroups();

            TransmitterGroup selGroup = null;
            String grpName = null;
            if (grpNameCbo.getSelectionIndex() == 0) {
                grpName = mnemonicTxt.getText().trim();
            } else {
                grpName = grpNameCbo.getText();
            }

            for (TransmitterGroup grp : groups) {
                if (grpName.equals(grp.getName())) {
                    selGroup = grp;
                    break;
                }
            }

            // Check that DAC port not in use in some other group.
            for (TransmitterGroup grp : groups) {
                if ((selGroup == null) || !grp.equals(selGroup)) {
                    if (dac.equals(grp.getDac())) {
                        for (Transmitter trans : grp.getTransmitterList()) {
                            if (!trans.equals(previousTransmitter)
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

            // Check for valid FIPS code
            if (valid && (selGroup != null)) {
                for (Transmitter trans : selGroup.getTransmitterList()) {
                    if (!trans.equals(previousTransmitter)
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

            if (dacPortCbo.getSelectionIndex() > 0) {
                Integer selectedPort = Integer.parseInt(dacPortCbo.getText());
                if (portList.contains(selectedPort)) {
                    String msg = "The selected port is already in use by "
                            + "another transmitter.  Are these transmitters daisy chained?";

                    int answer = DialogUtility.showMessageBox(getShell(),
                            SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                            "Daisy Chained", msg);
                    if (answer == SWT.NO) {
                        msg = "The transmitters must be daisy chained in order to be on "
                                + "the same port.\n\nPlease select another port.\n";
                        valid = false;
                        sb.append(msg + "\n");
                    }
                }
            }
        }

        // Check for duplicate mnemonic
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

        if (grpNameCbo.getSelectionIndex() > 0) {
            String grpName = group.getName();

            if (grpName.equals(mnemonicTxt.getText().trim())) {
                if (group.getTransmitters().size() > 1) {
                    valid = false;
                    String msg = "Transmitter cannot have same mnemonic as the "
                            + "group unless there is only one transmitter in the group\n";
                    sb.append(msg);
                }
            }
        }

        /*
         * If only one transmitter in a group then a rename of the transmitter
         * also renames the group.
         */
        if (type == TransmitterEditType.EDIT_TRANSMITTER) {
            if (!mnemonicTxt.getText().trim()
                    .equals(this.transmitter.getMnemonic())) {
                if (prevIsStandalone && (grpNameCbo.getSelectionIndex() == 0)) {
                    // Rename group too
                    group.setName(mnemonicTxt.getText().trim());
                    saveGroup = true;
                }
            }
        }

        else if (type == TransmitterEditType.NEW_TRANSMITTER) {
            String grpName = grpNameCbo.getText().trim();
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

    private void setTransmitterStatus(TxStatus status) {
        transmitter.setTxStatus(status);
        try {
            if (type != TransmitterEditType.NEW_TRANSMITTER) {
                transmitter = dataManager.saveTransmitter(transmitter);
                statusChange.statusChanged();
            }
        } catch (Exception e) {
            statusHandler.error("Error saving Transmitter status change", e);
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
        callback.dialogClosed(getReturnValue());
    }

    /**
     * Set the selection on the Time Zone Combo based on zone value in group.
     */
    private void populateTimeZoneControls() {
        String timeZoneID = group.getTimeZone();
        TimeZone tz = TimeZone.getTimeZone(timeZoneID);
        if (timeZoneID == null) {
            // Use local time zone to determine key.
            tz = TimeZone.getDefault();
        }

        String timeZone = BMHTimeZone.getTimeZoneUIName(tz);
        int index = timeZoneCbo.indexOf(timeZone);
        timeZoneCbo.select(index);

        noDstChk.setSelection(!tz.observesDaylightTime());
        if (BMHTimeZone.isForcedNoDst(timeZone)) {
            noDstChk.setEnabled(false);
        }
    }

}
