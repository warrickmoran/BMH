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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest.TtsVoiceAction;
import com.raytheon.uf.common.bmh.request.TtsVoiceResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListComp;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog that is used to create a new message type or edit and exiting message
 * type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014  #3420     lvenable    Initial creation
 * Aug 12, 2014  #3490     lvenable    Initial hook up of dialog to display selected values
 *                                     for the provided message type.
 * Aug 15, 2014  #3490     lvenable    Hooked up transmitters and selected the SAME transmitters
 *                                     if in edit mode.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateEditMsgTypesDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditMsgTypesDlg.class);

    /** Message type text field. */
    private Text msgTypeTF;

    /** Message type label. */
    private Label msgTypeLbl;

    /** MEssage type title text field. */
    private Text msgTypeTitleTF;

    /** Voice combo box. */
    private Combo voiceCbo;

    /** Designation combo box. */
    private Combo designationCbo;

    /** Emergency Override check box. */
    private Button eoChk;

    /** Duration date/time fields. */
    private DateTimeFields durationDTF;

    /** Periodicity date/time fields. */
    private DateTimeFields periodicityDTF;

    /** Alert check box. */
    private Button alertChk;

    /** Confirm check box. */
    private Button confirmChk;

    /** Interrupt check box. */
    private Button interruptChk;

    /** CIV radio button. */
    private Button civRdo;

    /** WXR radio button. */
    private Button wxrRdo;

    /** Enable blackout check box. */
    private Button enableBlackoutChk;

    /** Blackout Start date/time fields. */
    private DateTimeFields blackoutStartDTF;

    /** Blackout End date/time fields. */
    private DateTimeFields blackoutEndDTF;

    /** Blackout Start label. */
    private Label blackoutStartLbl;

    /** Blackout End label. */
    private Label blackoutEndLbl;

    /** List of SAME transmitters. */
    private CheckScrollListComp sameTransmitters;

    /** Relationship image. */
    private Image relationshipImg;

    /** Enumeration of dialog types. */
    public enum DialogType {
        CREATE, EDIT;
    };

    /** Type of dialog (Create or Edit). */
    private DialogType dialogType = DialogType.CREATE;

    /** Selected message type. */
    private MessageType selectedMsgType = null;

    /** List of available voices. */
    private List<TtsVoice> voiceList;

    /**
     * Constructor that will default selected message type to null.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dialogType
     *            Dialog type.
     */
    public CreateEditMsgTypesDlg(Shell parentShell, DialogType dialogType) {
        this(parentShell, dialogType, null);
    }

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dialogType
     *            Dialog type.
     * @param selectedMsgType
     *            Selected message type. If null then default values will be
     *            used.
     */
    public CreateEditMsgTypesDlg(Shell parentShell, DialogType dialogType,
            MessageType selectedMsgType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.dialogType = dialogType;
        this.selectedMsgType = selectedMsgType;
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
    protected void disposed() {
        if (relationshipImg != null) {
            relationshipImg.dispose();
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        if (dialogType == DialogType.CREATE) {
            setText("Create Message Type");
        } else if (dialogType == DialogType.EDIT) {
            setText("Edit Message Type");
        }

        retrieveVoicesFromDB();

        createMainControlComposite();
        createAreaAndRelationshipControls();
        createBottomButtons();
    }

    /**
     * Create the main control composite.
     */
    private void createMainControlComposite() {
        Composite mainControlComp = new Composite(shell, SWT.NONE);
        mainControlComp.setLayout(new GridLayout(2, false));
        mainControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        createGeneralAndDefaultGroups(mainControlComp);
        createSAMETranmitterControl(mainControlComp);
    }

    /**
     * Create the General and Default groups.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createGeneralAndDefaultGroups(Composite mainComp) {
        Composite genDefaultComp = new Composite(mainComp, SWT.NONE);
        genDefaultComp.setLayout(new GridLayout(1, false));
        genDefaultComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        createGeneralGroupControls(genDefaultComp);
        createDefaultGroupControls(genDefaultComp);
    }

    /**
     * Create the General Group and controls.
     * 
     * @param genDefaultComp
     *            General/Default composite.
     */
    private void createGeneralGroupControls(Composite genDefaultComp) {

        Group generalGroup = new Group(genDefaultComp, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(2, false);
        generalGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        generalGroup.setLayoutData(gd);
        generalGroup.setText(" General: ");

        int controlWidth = 200;

        /*
         * Message Type
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label msgTypeTFLbl = new Label(generalGroup, SWT.RIGHT);
        msgTypeTFLbl.setText("Message Type: ");
        msgTypeTFLbl.setLayoutData(gd);

        if (dialogType == DialogType.CREATE) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            gd.widthHint = controlWidth;
            msgTypeTF = new Text(generalGroup, SWT.BORDER);
            msgTypeTF.setLayoutData(gd);
        } else if (dialogType == DialogType.EDIT) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            gd.widthHint = controlWidth;
            msgTypeLbl = new Label(generalGroup, SWT.NONE);
            msgTypeLbl.setLayoutData(gd);

            if (selectedMsgType != null) {
                msgTypeLbl.setText(selectedMsgType.getAfosid());
            }
        }

        /*
         * Message Title
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label msgTypeTitleTFLbl = new Label(generalGroup, SWT.RIGHT);
        msgTypeTitleTFLbl.setText("Title: ");
        msgTypeTitleTFLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.widthHint = controlWidth;
        msgTypeTitleTF = new Text(generalGroup, SWT.BORDER);
        msgTypeTitleTF.setLayoutData(gd);

        if (dialogType == DialogType.EDIT && selectedMsgType != null) {
            msgTypeTitleTF.setText(selectedMsgType.getTitle());
        }

        /*
         * Voice
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label voiceCboLbl = new Label(generalGroup, SWT.RIGHT);
        voiceCboLbl.setText("Voice: ");
        voiceCboLbl.setLayoutData(gd);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.widthHint = controlWidth;
        voiceCbo = new Combo(generalGroup, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        voiceCbo.setLayoutData(gd);
        populateVoicesCombo();

        /*
         * Designation
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label designationCboLbl = new Label(generalGroup, SWT.RIGHT);
        designationCboLbl.setText("Designation: ");
        designationCboLbl.setLayoutData(gd);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.widthHint = controlWidth;
        designationCbo = new Combo(generalGroup, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        designationCbo.setLayoutData(gd);
        populateDesignationCombo();

        /*
         * Emergency Override
         */
        new Label(generalGroup, SWT.NONE); // Filler label
        eoChk = new Button(generalGroup, SWT.CHECK);
        eoChk.setText("Emergency Override");

        if (dialogType == DialogType.EDIT && selectedMsgType != null) {
            eoChk.setSelection(selectedMsgType.isEmergencyOverride());
        }
    }

    /**
     * Create the Default Group and controls.
     * 
     * @param genDefaultComp
     *            General/Default composite.
     */
    private void createDefaultGroupControls(Composite genDefaultComp) {

        Group defaultsGroup = new Group(genDefaultComp, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(2, false);
        defaultsGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        defaultsGroup.setLayoutData(gd);
        defaultsGroup.setText(" Defaults: ");

        /*
         * Time Controls
         */
        List<DateFieldType> fieldTypes = new ArrayList<DateFieldType>();
        fieldTypes.add(DateFieldType.DAY);
        fieldTypes.add(DateFieldType.HOUR);
        fieldTypes.add(DateFieldType.MINUTE);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label durationLbl = new Label(defaultsGroup, SWT.RIGHT);
        durationLbl.setText("Duration (DDHHMM): ");
        durationLbl.setLayoutData(gd);

        Map<DateFieldType, Integer> durMap = null;
        String durDateTimeStr = null;

        if (selectedMsgType != null) {
            durDateTimeStr = selectedMsgType.getDuration();
        }

        durMap = generateDayHourMinuteMap(durDateTimeStr);

        durationDTF = new DateTimeFields(defaultsGroup, durMap, false, false,
                true);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label periodicityLbl = new Label(defaultsGroup, SWT.RIGHT);
        periodicityLbl.setText("Periodicity (DDHHMM): ");
        periodicityLbl.setLayoutData(gd);

        Map<DateFieldType, Integer> periodicityMap = null;
        String periodicityDateTimeStr = null;

        if (selectedMsgType != null) {
            periodicityDateTimeStr = selectedMsgType.getPeriodicity();
        }

        periodicityMap = generateDayHourMinuteMap(periodicityDateTimeStr);

        periodicityDTF = new DateTimeFields(defaultsGroup, periodicityMap,
                false, false, true);

        /*
         * Check box controls
         */
        Composite checkBoxComp = new Composite(defaultsGroup, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        checkBoxComp.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        checkBoxComp.setLayoutData(gd);

        alertChk = new Button(checkBoxComp, SWT.CHECK);
        alertChk.setText("Alert");

        confirmChk = new Button(checkBoxComp, SWT.CHECK);
        confirmChk.setText("Confirm");

        interruptChk = new Button(checkBoxComp, SWT.CHECK);
        interruptChk.setText("Interrupt");

        if (selectedMsgType != null) {
            alertChk.setSelection(selectedMsgType.isAlert());
            confirmChk.setSelection(selectedMsgType.isConfirm());
            interruptChk.setSelection(selectedMsgType.isInterrupt());
        }

        /*
         * Radio controls
         */
        Composite radioComp = new Composite(defaultsGroup, SWT.NONE);
        gl = new GridLayout(3, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        radioComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        gd.horizontalSpan = 2;
        radioComp.setLayoutData(gd);

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.horizontalIndent = 20;
        Label sameOrigLbl = new Label(radioComp, SWT.RIGHT);
        sameOrigLbl.setText("SAME Originator: ");
        sameOrigLbl.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = 5;
        civRdo = new Button(radioComp, SWT.RADIO);
        civRdo.setText("CIV");
        civRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = 10;
        wxrRdo = new Button(radioComp, SWT.RADIO);
        wxrRdo.setText("WXR");
        wxrRdo.setLayoutData(gd);

        if (selectedMsgType != null) {
            if (selectedMsgType.isWxr()) {
                wxrRdo.setSelection(true);
            } else {
                civRdo.setSelection(true);
            }
        } else {
            wxrRdo.setSelection(true);
        }

        /*
         * Blackout controls.
         */
        Composite blackoutComp = new Composite(defaultsGroup, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        blackoutComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        blackoutComp.setLayoutData(gd);

        List<DateFieldType> blackoutFieldTypes = new ArrayList<DateFieldType>();
        blackoutFieldTypes.add(DateFieldType.HOUR);
        blackoutFieldTypes.add(DateFieldType.MINUTE);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.horizontalIndent = 20;
        gd.verticalIndent = 10;
        enableBlackoutChk = new Button(blackoutComp, SWT.CHECK);
        enableBlackoutChk.setText("Enable Tone Blackout Period:");
        enableBlackoutChk.setLayoutData(gd);
        enableBlackoutChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                enableBlackoutControls(enableBlackoutChk.getSelection());
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.horizontalIndent = 45;
        blackoutStartLbl = new Label(blackoutComp, SWT.NONE);
        blackoutStartLbl.setText("Black Out Start (HHMM)");
        blackoutStartLbl.setLayoutData(gd);

        // Create the map of values for the blackout start date/time field
        // controls.
        Map<DateFieldType, Integer> boStartMap = null;
        String boStartDateTimeStr = null;

        if (selectedMsgType != null) {
            boStartDateTimeStr = selectedMsgType.getToneBlackOutStart();
        }

        boStartMap = generateHourMinuteMap(boStartDateTimeStr);

        blackoutStartDTF = new DateTimeFields(blackoutComp, boStartMap, false,
                false, false);

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.horizontalIndent = 45;
        blackoutEndLbl = new Label(blackoutComp, SWT.NONE);
        blackoutEndLbl.setText("Black Out End (HHMM)");
        blackoutEndLbl.setLayoutData(gd);

        // Create the map of values for the blackout end date/time field
        // controls.
        Map<DateFieldType, Integer> boEndMap = null;
        String boEndDateTimeStr = null;

        if (selectedMsgType != null) {
            boEndDateTimeStr = selectedMsgType.getToneBlackOutEnd();
        }

        boEndMap = generateHourMinuteMap(boEndDateTimeStr);

        blackoutEndDTF = new DateTimeFields(blackoutComp, boEndMap, false,
                false, false);

        enableBlackoutControls(enableBlackoutChk.getSelection());
    }

    /**
     * Create the SAME transmitters list control.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createSAMETranmitterControl(Composite mainComp) {

        CheckListData cld = createTransmitterListData();

        sameTransmitters = new CheckScrollListComp(mainComp,
                "SAME Transmitters: ", cld, true, 125, 300);
    }

    /**
     * Create the Area and Relationship controls.
     */
    private void createAreaAndRelationshipControls() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Button areaSelectionBtn = new Button(buttonComp, SWT.PUSH);
        areaSelectionBtn.setText(" Area Selection... ");
        areaSelectionBtn.setLayoutData(gd);
        areaSelectionBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

            }
        });

        if (dialogType == DialogType.EDIT && selectedMsgType != null) {
            gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
            ImageDescriptor id;
            id = AbstractUIPlugin.imageDescriptorFromPlugin(
                    Activator.PLUGIN_ID, "icons/Relationship.png");
            relationshipImg = id.createImage();

            Button relationshipBtn = new Button(buttonComp, SWT.PUSH);
            relationshipBtn.setImage(relationshipImg);
            relationshipBtn.setToolTipText("View message type relationships");
            relationshipBtn.setLayoutData(gd);
            relationshipBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ViewMessageTypeDlg viewMessageTypeInfo = new ViewMessageTypeDlg(
                            shell, selectedMsgType);
                    viewMessageTypeInfo.open();
                }
            });
        }
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button createSaveBtn = new Button(buttonComp, SWT.PUSH);

        if (dialogType == DialogType.CREATE) {
            createSaveBtn.setText(" Create ");
        } else if (dialogType == DialogType.EDIT) {
            createSaveBtn.setText(" Save ");
        }

        createSaveBtn.setLayoutData(gd);
        createSaveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dialogType == DialogType.CREATE) {
                    // TODO : create action code needed
                } else if (dialogType == DialogType.EDIT) {
                    // TODO : save save action code needed
                }
                close();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Enable/Disable the blackout controls.
     * 
     * @param enable
     */
    private void enableBlackoutControls(boolean enable) {
        blackoutStartLbl.setEnabled(enable);
        blackoutStartDTF.enableControls(enable);
        blackoutEndLbl.setEnabled(enable);
        blackoutEndDTF.enableControls(enable);
    }

    /**
     * Get the list of transmitters.
     * 
     * @return Object containing the list of SAME transmitters.
     */
    private CheckListData createTransmitterListData() {

        CheckListData cld = new CheckListData();

        TransmitterDataManager tdm = new TransmitterDataManager();
        List<Transmitter> transmitters = null;

        try {
            transmitters = tdm.getTransmitters();

            if (transmitters == null) {
                return cld;
            }

            Collections.sort(transmitters, new TransmitterMnemonicComparator());
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return cld;
        }

        if (dialogType == DialogType.EDIT && selectedMsgType != null) {
            Set<Transmitter> transSet = selectedMsgType.getSameTransmitters();
            for (Transmitter t : transmitters) {
                cld.addDataItem(t.getMnemonic(), transSet.contains(t));
            }
        } else {
            for (Transmitter t : transmitters) {
                cld.addDataItem(t.getMnemonic(), false);
            }
        }

        return cld;
    }

    /**
     * Populate the Voices combo box.
     */
    private void populateVoicesCombo() {

        // If there are no voices then just return.
        if (voiceList.isEmpty()) {
            return;
        }

        for (TtsVoice voice : voiceList) {
            voiceCbo.add(voice.getVoiceName());
        }

        // If the selected message is null or the dialog is a create dialog then
        // set the selection to 0.
        if (selectedMsgType == null || dialogType == DialogType.CREATE) {
            voiceCbo.select(0);
            return;
        }

        int index = voiceCbo.indexOf(selectedMsgType.getVoice().getVoiceName());
        voiceCbo.select(index);
    }

    /**
     * Populate the Designation combo box.
     */
    private void populateDesignationCombo() {

        for (Designation des : Designation.values()) {
            designationCbo.add(des.name());
        }

        // If the selected message is null or the dialog is a create dialog then
        // set the selection to 0.
        if (selectedMsgType == null || dialogType == DialogType.CREATE) {
            designationCbo.select(0);
            return;
        }

        int index = designationCbo.indexOf(selectedMsgType.getDesignation()
                .name());
        designationCbo.select(index);
    }

    /**
     * Retrieve the voices from the DB.
     */
    private void retrieveVoicesFromDB() {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.AllVoices);
        TtsVoiceResponse voiceResponse = null;

        try {
            voiceResponse = (TtsVoiceResponse) BmhUtils
                    .sendRequest(voiceRequest);
            voiceList = voiceResponse.getTtsVoiceList();
        } catch (Exception e) {
            statusHandler.error("Error retrieving voices from the database: ",
                    e);
        }
    }

    /**
     * Generate a Map of DateFieldType Day/Hour/Minute keys with values pulled
     * from the provided string.
     * 
     * @param dateTimeStr
     *            Date/Time string (DDHHMM).
     * @return Map of DateFieldTypes and the associated values.
     */
    private Map<DateFieldType, Integer> generateDayHourMinuteMap(
            String dateTimeStr) {
        Map<DateFieldType, Integer> durmap = new LinkedHashMap<DateFieldType, Integer>();

        if (dateTimeStr == null || dateTimeStr.length() != 6) {
            durmap.put(DateFieldType.DAY, 0);
            durmap.put(DateFieldType.HOUR, 0);
            durmap.put(DateFieldType.MINUTE, 0);
        } else {
            int[] dtArray = splitDateTimeString(dateTimeStr);
            durmap.put(DateFieldType.DAY, dtArray[0]);
            durmap.put(DateFieldType.HOUR, dtArray[1]);
            durmap.put(DateFieldType.MINUTE, dtArray[2]);
        }

        return durmap;
    }

    /**
     * Generate a Map of DateFieldType Hour/Minute keys with values pulled from
     * the provided string.
     * 
     * @param timeStr
     *            Time string (HHMM).
     * @return Map of DateFieldTypes and the associated values.
     */
    private Map<DateFieldType, Integer> generateHourMinuteMap(String timeStr) {
        Map<DateFieldType, Integer> durmap = new LinkedHashMap<DateFieldType, Integer>();

        if (timeStr == null || timeStr.length() != 4) {
            durmap.put(DateFieldType.HOUR, 0);
            durmap.put(DateFieldType.MINUTE, 0);
        } else {
            int[] dtArray = splitDateTimeString(timeStr);
            durmap.put(DateFieldType.HOUR, dtArray[0]);
            durmap.put(DateFieldType.MINUTE, dtArray[1]);
        }

        return durmap;
    }

    /**
     * This method will split the date/time string into an array of integers for
     * each element in the array.
     * 
     * If the string passed in is 013422, the return int array will contain 3
     * elements: 1, 34, 22
     * 
     * @param dateTimeStr
     *            Date/Time string.
     * @return Array of numbers.
     */
    private int[] splitDateTimeString(String dateTimeStr) {
        int arraySize = dateTimeStr.length() / 2;
        int[] intArray = new int[arraySize];

        int idx = 0;
        for (int i = 0; i < arraySize; i++) {
            String subStr = dateTimeStr.substring(idx, idx + 2);

            try {
                intArray[i] = Integer.valueOf(subStr);
            } catch (NumberFormatException nfe) {
                intArray[i] = 0;
            }

            idx += 2;
        }

        return intArray;
    }
}
