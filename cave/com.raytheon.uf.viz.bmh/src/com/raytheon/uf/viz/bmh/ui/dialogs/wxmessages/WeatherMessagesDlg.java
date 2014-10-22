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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.ButtonImageCreator;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListComp;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionSaveData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Weather Messages dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 11, 2014  #3610     lvenable     Initial creation
 * Sep 25, 2014   3620     bsteffen     Add seconds to periodicity.
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  #3728     lvenable     Fixed date/time field arguments and added a call back
 *                                      to the select message type dialog.
 * Oct 14, 2014  #3728     lvenable     Added reading in a text file and displaying the Message
 *                                      Text Contents dialog.
 * Oct 15, 2014 #3728      lvenable     Added code to populate message type controls.
 * Oct 15, 2014 #3728      lvenable     Added New/Edit buttons and call to select input message.
 * Oct 18, 2014  #3728     lvenable     Hooked in more functionality.
 * Oct 21, 2014   #3728    lvenable     Added code for area selection and populating the input message controls.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class WeatherMessagesDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WeatherMessagesDlg.class);

    /** Message name. */
    private Text msgNameTF;

    /** Message Type label. */
    private Label msgTypeLbl;

    /** Message Title label. */
    private Label msgTitleLbl;

    /** Languages label. */
    private Label languageLbl;

    /** Designation label. */
    private Label designationLbl;

    /** Emergency Override label. */
    private Label emergenyOverrideLbl;

    /** Create date/time field. */
    private DateTimeFields creationDTF;

    /** Effective date/time field. */
    private DateTimeFields effectiveDTF;

    /** Expiration date/time field. */
    private DateTimeFields expirationDTF;

    /** Periodicity date/time field. */
    private DateTimeFields periodicityDTF;

    /** List of SAME transmitters. */
    private CheckScrollListComp sameTransmitters;

    /** Interrupt check box. */
    private Button interruptChk;

    /** Alert check box. */
    private Button alertChk;

    /** Confirm check box. */
    private Button confirmChk;

    /** Active radio button. */
    private Button activeRdo;

    /** Inactive radio button. */
    private Button inactiveRdo;

    /** From file radio button. */
    private Button fromFileRdo;

    /** Microphone radio button. */
    private Button microphoneRdo;

    /** Contents button. */
    private Button contentsBtn;

    /** Area selection button. */
    private Button areaSelectionBtn;

    /** Submit Message button. */
    private Button submitMsgBtn;

    /** Map of all the transmitters. */
    private final Map<String, Transmitter> transmitterMap = new HashMap<>();

    /** Area data from the Area Selection dialog. */
    private AreaSelectionSaveData areaData;

    /** Selected Message Type */
    private MessageType selectedMessageType = null;

    /** Input message selected by the user. */
    private InputMessage userInputMessage = null;

    /** Button used to change the message type. */
    private Button changeMsgTypeBtn;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            PArent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public WeatherMessagesDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Weather Messages Dialog", parentShell, SWT.DIALOG_TRIM
                | SWT.MIN, CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
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
        setText("Weather Messages");

        createNewEditButtons();
        createMainControls();
        resetControls();
    }

    /**
     * Method to check if the dialog can close.
     * 
     * For example: if there are items that are unsaved then the user should be
     * prompted that the dialog has unsaved items and be given the opportunity
     * to prevent the dialog from closing.
     */
    @Override
    public boolean okToClose() {
        /*
         * TODO:
         * 
         * Need to put in code to check/validate if the dialog can close (need
         * to save before closing, etc).
         */
        return true;
    }

    /**
     * Create the New and Edit buttons.
     */
    private void createNewEditButtons() {
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(new GridLayout(2, false));
        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        int buttonWidth = 70;
        GridData gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button newBtn = new Button(btnComp, SWT.PUSH);
        newBtn.setText("New");
        newBtn.setLayoutData(gd);
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                String msg = "Creating a new weather message will lose any existing changes.  Continue?";
                int choice = DialogUtility.showMessageBox(shell,
                        SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                        "New Weather Message", msg);

                if (choice == SWT.CANCEL) {
                    return;
                }

                userInputMessage = new InputMessage();
                resetControls();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button editBtn = new Button(btnComp, SWT.PUSH);
        editBtn.setText("Edit...");
        editBtn.setLayoutData(gd);
        editBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String msg = "Editing a weather message will lose any existing changes.  Continue?";
                int choice = DialogUtility.showMessageBox(shell,
                        SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                        "Edit Weather Message", msg);

                if (choice == SWT.CANCEL) {
                    return;
                }

                SelectInputMsgDlg simd = new SelectInputMsgDlg(shell);
                simd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue == null) {
                            return;
                        }

                        if ((returnValue != null)
                                && (returnValue instanceof InputMessage)) {
                            populateControlsForEdit((InputMessage) returnValue);
                        }
                    }
                });
                simd.open();
            }
        });

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
    }

    /**
     * Create the main controls.
     */
    private void createMainControls() {
        Composite mainControlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        mainControlComp.setLayout(gl);
        mainControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        createMessageTypeControls(mainControlComp);
        createSameXmitControls(mainControlComp);
        createDefaultContentControls(mainControlComp);
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createSubmitButton();
    }

    /**
     * Message type controls.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createMessageTypeControls(Composite mainComp) {
        Composite controlComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        controlComp.setLayout(gl);
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Message Name
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, true);
        gd.verticalIndent = 5;
        Label msgNameLbl = new Label(controlComp, SWT.RIGHT);
        msgNameLbl.setText("Message Name: ");
        msgNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.widthHint = 175;
        gd.verticalIndent = 5;
        msgNameTF = new Text(controlComp, SWT.BORDER);
        msgNameTF.setLayoutData(gd);

        // Message Type
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label msgTypeDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTypeDescLbl.setText("Message Type: ");
        msgTypeDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        msgTypeLbl = new Label(controlComp, SWT.BORDER);
        msgTypeLbl.setLayoutData(gd);

        changeMsgTypeBtn = new Button(controlComp, SWT.PUSH);
        changeMsgTypeBtn.setText(" Change... ");
        changeMsgTypeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectMessageTypeDlg selectMsgTypeDlg = new SelectMessageTypeDlg(
                        shell);
                selectMsgTypeDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null) {
                            selectedMessageType = (MessageType) returnValue;

                            updateMessageTypeControls();
                        } else {
                            if (selectedMessageType == null) {
                                areaSelectionBtn.setEnabled(false);
                                submitMsgBtn.setEnabled(false);
                            }
                        }
                    }
                });
                selectMsgTypeDlg.open();
            }
        });

        // Message Title
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label msgTitleDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTitleDescLbl.setText("Message Title: ");
        msgTitleDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        msgTitleLbl = new Label(controlComp, SWT.BORDER);
        msgTitleLbl.setLayoutData(gd);

        // Language
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label languageDescLbl = new Label(controlComp, SWT.RIGHT);
        languageDescLbl.setText("Language: ");
        languageDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        gd.horizontalSpan = 2;
        languageLbl = new Label(controlComp, SWT.NONE);
        languageLbl.setLayoutData(gd);

        // Designation
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label designationDescLbl = new Label(controlComp, SWT.RIGHT);
        designationDescLbl.setText("Designation: ");
        designationDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        designationLbl = new Label(controlComp, SWT.NONE);
        designationLbl.setLayoutData(gd);

        // Emergency Override
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label eoLbl = new Label(controlComp, SWT.RIGHT);
        eoLbl.setText("Emergency Override: ");
        eoLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        emergenyOverrideLbl = new Label(controlComp, SWT.NONE);
        emergenyOverrideLbl.setLayoutData(gd);

        /*
         * Creation, Effective, Expiration Dates
         */

        Map<DateFieldType, Integer> dateTimeMap = new LinkedHashMap<DateFieldType, Integer>();
        dateTimeMap.put(DateFieldType.YEAR, null);
        dateTimeMap.put(DateFieldType.MONTH, null);
        dateTimeMap.put(DateFieldType.DAY, null);
        dateTimeMap.put(DateFieldType.HOUR, null);
        dateTimeMap.put(DateFieldType.MINUTE, null);

        /*
         * Creation
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.verticalIndent = 15;
        Label creationLbl = new Label(controlComp, SWT.CENTER);
        creationLbl.setText("Creation Date/Time\n(YYMMDDHHmm): ");
        creationLbl.setLayoutData(gd);

        Composite creationDTFComp = new Composite(controlComp, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        creationDTFComp.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 15;
        creationDTFComp.setLayoutData(gd);

        creationDTF = new DateTimeFields(creationDTFComp, dateTimeMap, false,
                false, false);

        /*
         * Effective
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.verticalIndent = 5;
        Label effectiveLbl = new Label(controlComp, SWT.CENTER);
        effectiveLbl.setText("Effective Date/Time\n(YYMMDDHHmm): ");
        effectiveLbl.setLayoutData(gd);

        Composite effectiveDTFComp = new Composite(controlComp, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        effectiveDTFComp.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        effectiveDTFComp.setLayoutData(gd);

        effectiveDTF = new DateTimeFields(effectiveDTFComp, dateTimeMap, false,
                false, false);

        /*
         * Expiration
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.verticalIndent = 5;
        Label expirationLbl = new Label(controlComp, SWT.CENTER);
        expirationLbl.setText("Expiration Date/Time\n(YYMMDDHHmm): ");
        expirationLbl.setLayoutData(gd);

        Composite expirationDTFComp = new Composite(controlComp, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        expirationDTFComp.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        expirationDTFComp.setLayoutData(gd);
        expirationDTF = new DateTimeFields(expirationDTFComp, dateTimeMap,
                false, false, false);

        /*
         * Area Selection
         */
        DialogUtility.addSeparator(controlComp, SWT.HORIZONTAL);

        gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalIndent = 7;
        areaSelectionBtn = new Button(controlComp, SWT.PUSH);
        areaSelectionBtn.setText("Area Selection...");
        areaSelectionBtn.setLayoutData(gd);
        areaSelectionBtn.setEnabled(false);
        areaSelectionBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAreadSelectionAction();
            }
        });
    }

    /**
     * Create the SAME transmitter controls.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createSameXmitControls(Composite mainComp) {
        CheckListData cld = createTransmitterListData();

        sameTransmitters = new CheckScrollListComp(mainComp, "SAME: ", cld,
                false, 80, 250, true);
    }

    /**
     * Create the controls for the content part of the GUI.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createDefaultContentControls(Composite mainComp) {
        Composite controlComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 10;
        controlComp.setLayout(gl);
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        /*
         * Defaults Group
         */
        Group defaultsGrp = new Group(controlComp, SWT.SHADOW_OUT);
        gl = new GridLayout(2, false);
        defaultsGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        defaultsGrp.setLayoutData(gd);
        defaultsGrp.setText(" Defaults: ");

        // Periodicity
        Label periodicityLbl = new Label(defaultsGrp, SWT.RIGHT);
        periodicityLbl.setText("Periodicity\n(DDHHMMSS): ");
        periodicityLbl.setLayoutData(gd);

        Map<DateFieldType, Integer> periodicityMap = null;
        String periodicityDateTimeStr = null;

        if (selectedMessageType != null) {
            periodicityDateTimeStr = selectedMessageType.getPeriodicity();
        }

        periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(periodicityDateTimeStr);

        periodicityDTF = new DateTimeFields(defaultsGrp, periodicityMap, false,
                false, true);

        // Interrupt, Alert, Confirm
        int hIndent = 15;
        gd = new GridData();
        gd.horizontalIndent = hIndent;
        gd.verticalIndent = 7;
        gd.horizontalSpan = 2;
        interruptChk = new Button(defaultsGrp, SWT.CHECK);
        interruptChk.setText("Interrupt");
        interruptChk.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = hIndent;
        gd.horizontalSpan = 2;
        alertChk = new Button(defaultsGrp, SWT.CHECK);
        alertChk.setText("Alert");
        alertChk.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = hIndent;
        gd.horizontalSpan = 2;
        confirmChk = new Button(defaultsGrp, SWT.CHECK);
        confirmChk.setText("Confirm");
        confirmChk.setLayoutData(gd);

        /*
         * Status Group
         */
        Group statusGrp = new Group(controlComp, SWT.SHADOW_OUT);
        gl = new GridLayout(2, false);
        statusGrp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        statusGrp.setLayoutData(gd);
        statusGrp.setText(" Status: ");

        activeRdo = new Button(statusGrp, SWT.RADIO);
        activeRdo.setText("Active");
        activeRdo.setSelection(true);

        inactiveRdo = new Button(statusGrp, SWT.RADIO);
        inactiveRdo.setText("Inactive");

        /*
         * Content Source Group
         */
        Group contentSourceGrp = new Group(controlComp, SWT.SHADOW_OUT);
        gl = new GridLayout(2, false);
        contentSourceGrp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        contentSourceGrp.setLayoutData(gd);
        contentSourceGrp.setText(" Content Source: ");

        fromFileRdo = new Button(contentSourceGrp, SWT.RADIO);
        fromFileRdo.setText("From File");

        microphoneRdo = new Button(contentSourceGrp, SWT.RADIO);
        microphoneRdo.setText("Microphone");
        microphoneRdo.setSelection(true);

        /*
         * Contents button
         */
        ButtonImageCreator bic = new ButtonImageCreator(shell);

        // Contents button.
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        contentsBtn = new Button(controlComp, SWT.PUSH);
        contentsBtn.setLayoutData(gd);

        FontData fd = contentsBtn.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.setHeight(24);
        bic.setFontData(fd);

        Image img = bic.generateImage(250, 40, "Contents", new RGB(0, 235, 0));
        contentsBtn.setImage(img);
        contentsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fromFileRdo.getSelection()) {
                    handleContentsFromFileAction();
                } else {
                    handleContentsMicrophoneAction();
                }
            }
        });
    }

    /**
     * Create the Submit button for the message type.
     */
    private void createSubmitButton() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        submitMsgBtn = new Button(buttonComp, SWT.PUSH);
        submitMsgBtn.setText("Submit Message");
        submitMsgBtn.setLayoutData(gd);
        submitMsgBtn.setEnabled(false);
        submitMsgBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * TODO:
                 * 
                 * need to determine what to do once something is submitted. --
                 * can we submit again? -- clear the dialog? -- what happens if
                 * I record voice for microphone and load a file?
                 */
            }
        });
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

        for (Transmitter t : transmitters) {
            cld.addDataItem(t.getMnemonic(), false);
            transmitterMap.put(t.getMnemonic(), t);
        }

        return cld;
    }

    /**
     * Open a file dialog and let this user select a file to edit. If the file
     * is valid then open the Message Contents dialog.
     */
    private void handleContentsFromFileAction() {

        String[] filterNames = { "Text (*.txt)", "All Extensions (*.*)",
                "All Files (*)" };

        // These filter extensions are used to filter which files are displayed.
        String[] filterExtentions = { "*.txt", "*.*", "*" };

        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtentions);
        String fn = dlg.open();
        if (fn != null) {
            File f = new File(fn);

            try {
                List<String> fileContents = Files.readAllLines(f.toPath(),
                        Charset.defaultCharset());

                if (fileContents.isEmpty()) {
                    StringBuilder msg = new StringBuilder();

                    msg.append("The file: \n");
                    msg.append(fn).append("\n");
                    msg.append("does not have any text to read in.");

                    DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING
                            | SWT.OK, "No Text Error", msg.toString());
                    return;
                }

                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = fileContents.iterator();

                while (iter.hasNext()) {
                    sb.append(iter.next());

                    if (iter.hasNext()) {
                        sb.append("\n");
                    }
                }

                MessageTextContentsDlg mtcd = new MessageTextContentsDlg(shell,
                        sb.toString());
                mtcd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            // TODO - handle getting text back...

                            System.out.println((String) returnValue);
                        }

                    }
                });
                mtcd.open();

            } catch (IOException e) {
                statusHandler.error("Error reading data from file: " + fn
                        + " --- ", e);

                StringBuilder msg = new StringBuilder();

                msg.append("The file: \n");
                msg.append(fn).append("\n");
                msg.append("could not be read in.  The file must be an ASCII text file.");

                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING
                        | SWT.OK, "File Read Error", msg.toString());
                return;
            }
        }
    }

    /**
     * For the microphone contents, display the record/playback dialog.
     */
    private void handleContentsMicrophoneAction() {
        RecordPlaybackDlg recPlaybackDlg = new RecordPlaybackDlg(shell, 600);
        recPlaybackDlg.open();
    }

    /**
     * Handle the area selection action.
     */
    private void handleAreadSelectionAction() {
        AreaSelectionDlg dlg = null;

        if (userInputMessage != null) {
            dlg = new AreaSelectionDlg(getShell(),
                    userInputMessage.getAreaCodes());
        } else {
            dlg = new AreaSelectionDlg(getShell(), selectedMessageType);
        }

        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                areaData = (AreaSelectionSaveData) returnValue;
                Set<String> areaCodes = areaData.getSelectedAreaCodes();

                if (areaCodes == null || areaCodes.isEmpty()) {
                    return;
                }

                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = areaCodes.iterator();
                while (iter.hasNext()) {
                    sb.append(iter.next());

                    if (iter.hasNext()) {
                        sb.append("-");
                    }
                }

                userInputMessage.setAreaCodes(sb.toString());
            }
        });
        dlg.open();
    }

    /**
     * Set the message type controls using the selected message type.
     */
    private void updateMessageTypeControls() {
        if (selectedMessageType == null) {
            return;
        }

        // Message Type
        msgTypeLbl.setText(selectedMessageType.getAfosid());
        msgTitleLbl.setText(selectedMessageType.getTitle());
        languageLbl
                .setText(selectedMessageType.getVoice().getLanguage().name());
        designationLbl.setText(selectedMessageType.getDesignation().name());
        String eo = (selectedMessageType.isEmergencyOverride()) ? "Yes" : "No";
        emergenyOverrideLbl.setText(eo);

        CheckListData cld = new CheckListData();
        Set<Transmitter> transSet = selectedMessageType.getSameTransmitters();
        if (transSet != null) {
            for (Transmitter t : transSet) {
                cld.addDataItem(t.getMnemonic(), transSet.contains(t));
                transmitterMap.put(t.getMnemonic(), t);
            }
        }

        sameTransmitters.selectCheckboxes(cld);
    }

    /**
     * Reset the controls to "default" values.
     */
    private void resetControls() {
        selectedMessageType = null;
        userInputMessage = new InputMessage();

        // Input message name.
        msgNameTF.setText("");
        msgNameTF.setEditable(true);
        msgNameTF.setBackground(null);

        /*
         * Message Type Controls
         */
        msgTypeLbl.setText("");
        msgTitleLbl.setText("");
        languageLbl.setText("");
        designationLbl.setText("");
        emergenyOverrideLbl.setText("");
        sameTransmitters.selectCheckboxes(false);

        submitMsgBtn.setEnabled(false);
        changeMsgTypeBtn.setEnabled(true);

        /*
         * Input Message controls.
         */
        interruptChk.setSelection(false);
        alertChk.setSelection(false);
        confirmChk.setSelection(false);

        activeRdo.setSelection(true);
        inactiveRdo.setSelection(false);

        microphoneRdo.setSelection(true);
        fromFileRdo.setSelection(false);

        periodicityDTF.setFieldValue(DateFieldType.DAY, 0);
        periodicityDTF.setFieldValue(DateFieldType.HOUR, 0);
        periodicityDTF.setFieldValue(DateFieldType.MINUTE, 0);
        periodicityDTF.setFieldValue(DateFieldType.SECOND, 0);
    }

    /**
     * Populate the controls when in the "edit state".
     * 
     * @param inputMessage
     *            Selected input message.
     */
    private void populateControlsForEdit(InputMessage im) {
        userInputMessage = im;
        selectedMessageType = null;

        changeMsgTypeBtn.setEnabled(false);

        // Input message name.
        msgNameTF.setText(userInputMessage.getName());
        msgNameTF.setEditable(false);
        msgNameTF.setBackground(getDisplay().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));

        /*
         * Message type controls.
         */
        getMessageType(userInputMessage.getAfosid());
        if (selectedMessageType == null) {
            areaSelectionBtn.setEnabled(false);
            submitMsgBtn.setEnabled(false);
        } else {
            areaSelectionBtn.setEnabled(true);
            submitMsgBtn.setEnabled(true);
        }

        updateMessageTypeControls();

        /*
         * Input Message controls.
         */
        interruptChk.setSelection(userInputMessage.getInterrupt());
        alertChk.setSelection(userInputMessage.getAlertTone());
        confirmChk.setSelection(userInputMessage.getConfirm());

        boolean messageActive = userInputMessage.getActive();
        activeRdo.setSelection(messageActive);
        inactiveRdo.setSelection(!messageActive);

        /*
         * TODO : need to determine if the input message is from file or
         * microphone and set the control appropriately
         * 
         * get information to allow the user to play back recorded voice for
         * view/edit text.
         */

        // Creation, Expiration, Effective date time fields.
        creationDTF.setDateTimeSpinners(userInputMessage.getCreationTime());
        effectiveDTF.setDateTimeSpinners(userInputMessage.getEffectiveTime());
        expirationDTF.setDateTimeSpinners(userInputMessage.getExpirationTime());

        // Update periodicity
        String peridicityStr = userInputMessage.getPeriodicity();
        if (peridicityStr == null || peridicityStr.length() == 0
                || peridicityStr.length() != 8) {
            periodicityDTF.setFieldValue(DateFieldType.DAY, 0);
            periodicityDTF.setFieldValue(DateFieldType.HOUR, 0);
            periodicityDTF.setFieldValue(DateFieldType.MINUTE, 0);
            periodicityDTF.setFieldValue(DateFieldType.SECOND, 0);
        } else {
            int[] periodicityValues = BmhUtils
                    .splitDateTimeString(peridicityStr);
            periodicityDTF.setFieldValue(DateFieldType.DAY,
                    periodicityValues[0]);
            periodicityDTF.setFieldValue(DateFieldType.HOUR,
                    periodicityValues[1]);
            periodicityDTF.setFieldValue(DateFieldType.MINUTE,
                    periodicityValues[2]);
            periodicityDTF.setFieldValue(DateFieldType.SECOND,
                    periodicityValues[3]);
        }
    }

    /**
     * Get the message type associated with the AFOS Id passed in.
     * 
     * @param afosId
     *            AFOS Id.
     */
    private void getMessageType(String afosId) {
        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

        try {
            selectedMessageType = msgTypeDataMgr.getMessageType(afosId);
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);

            StringBuilder msg = new StringBuilder();
            msg.append("The message type for AFOS ID: ");
            msg.append(afosId).append("\n");
            msg.append("could not be read in.");

            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING | SWT.OK,
                    "Error Retrieving Message Type", msg.toString());

            selectedMessageType = null;
            return;
        }
    }
}
