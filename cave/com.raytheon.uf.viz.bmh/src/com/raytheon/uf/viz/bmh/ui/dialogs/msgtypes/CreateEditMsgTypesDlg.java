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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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

import com.raytheon.uf.common.bmh.StaticMessageIdentifier;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
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
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

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
 * Jul 30, 2014   3420     lvenable    Initial creation
 * Aug 12, 2014   3490     lvenable    Initial hook up of dialog to display selected values
 *                                     for the provided message type.
 * Aug 15, 2014   3490     lvenable    Hooked up transmitters and selected the SAME transmitters
 *                                     if in edit mode.
 * Aug 18, 2014   3411     mpduff      Implement New and Edit.
 * Sep 11, 2014   3411     mpduff      Pass MessageType object to Area Selection for populating, save TransmitterGroups
 * Sep 14, 2014   3610     lvenable    Removed unused code and renamed variable.
 * Sep 25, 2014   3620     bsteffen    Add seconds to periodicity and duration.
 * Oct 05, 2014   3411     mpduff      Added null checks and added transmitter to the group that contains it
 * Oct 08, 2014   3479     lvenable    Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 16, 2014   3657     bkowal      Relocated duration parsing methods.
 * Oct 30, 2014   3617     dgilling    Fix default states for blackout controls.
 * Nov 01, 2014   3784     mpduff      Added nullCheck
 * Nov 02, 2014   3783     lvenable    Replaced message type list with a set of AFOS Ids.
 * Nov 13, 2014   3803     bkowal      Use the new Voice Data Manager.
 * Jan 30, 2015   4067     bkowal      Construct a {@link AreaSelectionSaveData} in
 *                                     the constructor that will be used for area selection
 *                                     for new and existing message types.
 * Feb 04, 2015   4087     bkowal      Default to designation for new messages to Forecast.
 * Mar 11, 2015   4267     bkowal      Do not alter the local {@link AreaSelectionSaveData}
 *                                     if {@link AreaSelectionDlg} returns a {@code null} value.
 * Mar 18, 2015   4213     bkowal      No longer allow users to create a message type with the
 *                                     station id or time announcement designation.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateEditMsgTypesDlg extends CaveSWTDialog {

    private static final Designation DEFAULT_DESIGNATION = Designation.Forecast;

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

    /** Set of existing AFOS Ids. */
    private Set<String> existingAfosIds = null;

    /** Map of transmitters. */
    private final Map<String, Transmitter> transmitterMap = new HashMap<>();

    /** Data returned from the area selection dialog. */
    private AreaSelectionSaveData areaData;

    /**
     * Constructor that will default selected message type to null.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dialogType
     *            Dialog type.
     */
    public CreateEditMsgTypesDlg(Shell parentShell, DialogType dialogType,
            Set<String> existingAfosId) {
        this(parentShell, dialogType, existingAfosId, null);
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
            Set<String> existingAfosId, MessageType selectedMsgType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.dialogType = dialogType;
        this.selectedMsgType = selectedMsgType;
        this.existingAfosIds = existingAfosId;
        this.areaData = new AreaSelectionSaveData();
        if (this.selectedMsgType != null) {
            /*
             * populate the area selection save data.
             */
            for (Area area : this.selectedMsgType.getDefaultAreas()) {
                this.areaData.addArea(area);
            }

            for (Zone zone : this.selectedMsgType.getDefaultZones()) {
                this.areaData.addZone(zone);
            }

            for (TransmitterGroup tg : this.selectedMsgType
                    .getDefaultTransmitterGroups()) {
                for (Transmitter t : tg.getTransmitters()) {
                    this.areaData.addTransmitter(t);
                }
            }
        }
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
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label durationLbl = new Label(defaultsGroup, SWT.RIGHT);
        durationLbl.setText("Duration (DDHHMMSS): ");
        durationLbl.setLayoutData(gd);

        Map<DateFieldType, Integer> durMap = null;
        String durDateTimeStr = null;

        if (selectedMsgType != null) {
            durDateTimeStr = selectedMsgType.getDuration();
        }

        durMap = BmhUtils.generateDayHourMinuteSecondMap(durDateTimeStr);

        durationDTF = new DateTimeFields(defaultsGroup, durMap, false, false,
                true);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label periodicityLbl = new Label(defaultsGroup, SWT.RIGHT);
        periodicityLbl.setText("Periodicity (DDHHMMSS): ");
        periodicityLbl.setLayoutData(gd);

        Map<DateFieldType, Integer> periodicityMap = null;
        String periodicityDateTimeStr = null;

        if (selectedMsgType != null) {
            periodicityDateTimeStr = selectedMsgType.getPeriodicity();
        }

        periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(periodicityDateTimeStr);

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

        if (selectedMsgType != null) {
            enableBlackoutChk.setSelection(selectedMsgType
                    .isToneBlackoutEnabled());
        }

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

        boStartMap = BmhUtils.generateHourMinuteMap(boStartDateTimeStr);

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

        boEndMap = BmhUtils.generateHourMinuteMap(boEndDateTimeStr);

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
                AreaSelectionDlg dlg = new AreaSelectionDlg(getShell(),
                        areaData);
                dlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue instanceof AreaSelectionSaveData) {
                            areaData = (AreaSelectionSaveData) returnValue;
                        }
                    }
                });
                dlg.open();
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
                if (save(dialogType)) {
                    setReturnValue(selectedMsgType);
                    close();
                }
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Cancel ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    private boolean save(DialogType type) {
        boolean valid = true;
        StringBuilder msg = new StringBuilder(
                "Please correct the following problems:\n\n");

        if (dialogType == DialogType.CREATE) {
            // Validate afosId for correctness and uniqueness if new
            boolean validAfos = MessageTypeUtils.validateAfosId(msgTypeTF
                    .getText().toUpperCase());
            if (!validAfos) {
                String message = "Invalid Message Type name.\n\nMust be 7-9 alphanumeric characters "
                        + "with no spaces or special characters.";
                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                        "Invalid Name", message);
                return false;
            }

            if (!MessageTypeUtils.isUnique(msgTypeTF.getText().toUpperCase()
                    .trim(), existingAfosIds)) {
                String message = "Invalid name/AfosID.\n\n"
                        + msgTypeTF.getText().trim()
                        + " is already being used.\n\n" + "Enter another name";

                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                        "Invalid Name", message);
                return false;

            }
        }

        String title = msgTypeTitleTF.getText().trim();
        if (StringUtils.isEmpty(title)) {
            msg.append("Title cannot be blank\n");
            valid = false;
        }

        String duration = this.durationDTF.getFormattedValue();
        if (duration.length() != 8) {
            msg.append("Duration is invalid\n");
            valid = false;
        }

        String periodicity = this.periodicityDTF.getFormattedValue();
        if (periodicity.length() != 8) {
            msg.append("Periodicity is invalid\n");
            valid = false;
        }

        if (enableBlackoutChk.getSelection()) {
            String blackoutStart = blackoutStartDTF.getFormattedValue();
            if (blackoutStart.length() != 4) {
                msg.append("Blackout Start is invalid\n");
                valid = false;
            }

            String blackoutEnd = blackoutEndDTF.getFormattedValue();
            if (blackoutEnd.length() != 4) {
                msg.append("Blackout End is invalid\n");
                valid = false;
            }
        }

        if (!valid) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                    "Invalid Values", msg.toString());
        }

        // Valid, save the data
        if (selectedMsgType == null) {
            selectedMsgType = new MessageType();
        }

        if (dialogType == DialogType.CREATE) {
            selectedMsgType.setAfosid(msgTypeTF.getText().toUpperCase().trim());
        }
        selectedMsgType.setAlert(alertChk.getSelection());
        selectedMsgType.setConfirm(confirmChk.getSelection());
        selectedMsgType.setDesignation(Designation.valueOf(this.designationCbo
                .getText()));
        selectedMsgType.setDuration(duration);
        selectedMsgType.setEmergencyOverride(eoChk.getSelection());
        selectedMsgType.setInterrupt(interruptChk.getSelection());
        selectedMsgType.setPeriodicity(periodicity);
        selectedMsgType.setTitle(msgTypeTitleTF.getText().trim());
        selectedMsgType
                .setToneBlackoutEnabled(enableBlackoutChk.getSelection());
        if (enableBlackoutChk.getSelection()) {
            String blackoutStart = blackoutStartDTF.getFormattedValue();
            String blackoutEnd = blackoutEndDTF.getFormattedValue();
            selectedMsgType.setToneBlackOutEnd(blackoutEnd);
            selectedMsgType.setToneBlackOutStart(blackoutStart);
        }

        CheckListData sameData = sameTransmitters.getCheckedItems();
        if (selectedMsgType.getSameTransmitters() != null) {
            selectedMsgType.getSameTransmitters().clear();
        }

        for (String xmit : sameData.getCheckedItems()) {
            selectedMsgType.addSameTransmitter(transmitterMap.get(xmit));
        }

        String voiceName = voiceCbo.getText();
        for (TtsVoice voice : voiceList) {
            if (voiceName.equals(voice.getVoiceName())) {
                selectedMsgType.setVoice(voice);
                break;
            }
        }

        selectedMsgType.setWxr(wxrRdo.getSelection());

        if (areaData != null) {
            if (areaData.getAreas() != null) {
                selectedMsgType.setDefaultAreas(areaData.getAreas());
            } else {
                selectedMsgType.setDefaultAreas(new HashSet<Area>());
            }

            if (areaData.getZones() != null) {
                selectedMsgType.setDefaultZones(areaData.getZones());
            } else {
                selectedMsgType.setDefaultZones(new HashSet<Zone>());
            }

            Set<Transmitter> transmitterSet = areaData.getTransmitters();
            Set<TransmitterGroup> groupSet = new HashSet<>();
            for (Transmitter t : transmitterSet) {
                t.getTransmitterGroup().addTransmitter(t);
                groupSet.add(t.getTransmitterGroup());
            }
            selectedMsgType.setDefaultTransmitterGroups(groupSet);
        }
        MessageTypeDataManager dm = new MessageTypeDataManager();
        try {
            selectedMsgType = dm.saveMessageType(selectedMsgType);
        } catch (Exception e) {
            statusHandler.error("Error saving Message", e);
            valid = false;
        }

        return valid;
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
            if (transSet != null) {
                for (Transmitter t : transmitters) {
                    cld.addDataItem(t.getMnemonic(), transSet.contains(t));
                    transmitterMap.put(t.getMnemonic(), t);
                }
            } else {
                for (Transmitter t : transmitters) {
                    cld.addDataItem(t.getMnemonic(), false);
                    transmitterMap.put(t.getMnemonic(), t);
                }
            }
        } else {
            for (Transmitter t : transmitters) {
                cld.addDataItem(t.getMnemonic(), false);
                transmitterMap.put(t.getMnemonic(), t);
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
            /*
             * exclude the static message type designations.
             */
            if (des == StaticMessageIdentifier.stationIdDesignation
                    || des == StaticMessageIdentifier.timeDesignation) {
                continue;
            }
            designationCbo.add(des.name());
        }

        // If the selected message is null or the dialog is a create dialog then
        // set the selection to 0.
        if (selectedMsgType == null || dialogType == DialogType.CREATE) {
            this.designationCbo.setText(DEFAULT_DESIGNATION.name());
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
        VoiceDataManager vdm = new VoiceDataManager();

        try {
            this.voiceList = vdm.getAllVoices();
        } catch (Exception e) {
            statusHandler.error("Error retrieving voices from the database: ",
                    e);
        }
    }
}