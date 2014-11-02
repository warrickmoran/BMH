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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
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
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.WxMessagesContent.CONTENT_TYPE;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Weather Messages dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 11, 2014  3610     lvenable    Initial creation
 * Sep 25, 2014  3620     bsteffen    Add seconds to periodicity.
 * Oct 08, 2014  3479     lvenable    Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  3728     lvenable    Fixed date/time field arguments and added a call back
 *                                    to the select message type dialog.
 * Oct 14, 2014  3728     lvenable    Added reading in a text file and displaying the Message
 *                                    Text Contents dialog.
 * Oct 15, 2014  3728     lvenable    Added code to populate message type controls.
 * Oct 15, 2014  3728     lvenable    Added New/Edit buttons and call to select input message.
 * Oct 18, 2014  3728     lvenable    Hooked in more functionality.
 * Oct 21, 2014  3728     lvenable    Added code for area selection and populating the input message controls.
 * Oct 21, 2014  3728     lvenable    Added Preview and Play buttons.
 * Oct 23, 2014  3748     bkowal      Support sending weather messages to the server so that
 *                                    they can be broadcasted (initial implementation).
 * Oct 26, 2014  3728     lvenable    Updated to call new contents dialog.
 * Oct 26, 2014  3748     bkowal      Updated to use information from the new
 *                                    contents dialog. Finished create "NEW"
 *                                    weather message.
 * Oct 28, 2014  3750     bkowal      Validate contents prior to submit.
 * Oct 31, 2014  3778     bsteffen    Do not clear the id when editing messages.
 * Nov 01, 2014  3784     mpduff      Set defaults on Message Type selection
 * Nov 1, 2014   3657     bkowal      Display a confirmation dialog to notify the user that
 *                                    SAME / Alert Tones will be played.
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

    /** Message text selected from file. */
    // private String messageContent = null;

    /** Audio data list. */
    private List<InputMessageAudioData> audioData = null;

    private WxMessagesContent content = null;

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
        this.handleNewAction();
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

                handleNewAction();
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

                        if (returnValue instanceof InputAudioMessage) {
                            InputAudioMessage im = (InputAudioMessage) returnValue;
                            populateControlsForEdit(im.getInputMessage(),
                                    im.getAudioDataList());
                        }
                    }
                });
                simd.open();
            }
        });

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
    }

    private void handleNewAction() {
        // we are only setting the fields that are required to update the
        // current input message during submit.
        InputMessage im = new InputMessage();
        im.setContent(StringUtils.EMPTY);
        Calendar creation = TimeUtil.newGmtCalendar();
        im.setCreationTime(creation);
        im.setEffectiveTime(creation);
        // for now just add 1 day to the creation for the expiration
        Calendar expire = TimeUtil.newCalendar(creation);
        expire.add(Calendar.DATE, 1);
        im.setExpirationTime(expire);
        resetControls();
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
        msgNameTF.setTextLimit(40);
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
                // clone the list to ensure that the original is maintained.
                List<InputMessageAudioData> audioList = (audioData != null) ? new ArrayList<>(
                        audioData) : new ArrayList<InputMessageAudioData>(1);

                MessageContentsDlg mcd = new MessageContentsDlg(shell,
                        audioList, userInputMessage.getContent(),
                        determineContentType(userInputMessage.getContent()));
                mcd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue == null) {
                            // cancelled
                            return;
                        }

                        if (returnValue instanceof WxMessagesContent == false) {
                            return;
                        }

                        content = (WxMessagesContent) returnValue;
                    }
                });
                mcd.open();
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
                handleSubmitAction();
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

    private boolean validate() {
        /*
         * verify that a name has been set.
         */
        if (this.msgNameTF.getText() == null
                || this.msgNameTF.getText().isEmpty()) {
            DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Message Name",
                            "Message Name is a required field. Please enter a Message Name.");
            return false;
        }

        /*
         * verify that a message type has been set.
         */
        if (this.selectedMessageType == null) {
            DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Message Type",
                            "A Message Type is required. Please select a Message Type.");
            return false;
        }

        /*
         * verify that transmitters have been selected.
         */
        if (this.sameTransmitters.getCheckedItems().getCheckedItems().isEmpty()) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - SAME",
                            "SAME Transmitters must be selected. It is possible that the Message Type you have selected has not been associated with any transmitters.");
            return false;
        }

        /*
         * verify that message contents have been set.
         */
        if (this.content != null && this.content.isComplete() == false) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Contents",
                            "No Message Contents have been provided. Please record a message or enter text. Click on the 'Contents' button to get started.");
            return false;
        }

        return true;
    }

    private void handleSubmitAction() {
        if (this.validate() == false) {
            return;
        }

        if (this.alertChk.getSelection()) {
            // alert the user that they are about to play same tones.
            int option = DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_WARNING | SWT.YES | SWT.NO,
                            "Emergency Override - Tone Playback",
                            this.selectedMessageType.getTitle()
                                    + " will activate SAME and/or Alert Tones! Would you like to continue?");
            if (option != SWT.YES) {
                return;
            }
        }

        NewBroadcastMsgRequest request = new NewBroadcastMsgRequest();

        this.userInputMessage.setName(this.msgNameTF.getText());
        this.userInputMessage.setLanguage(this.selectedMessageType.getVoice()
                .getLanguage());
        this.userInputMessage.setAfosid(this.selectedMessageType.getAfosid());
        this.userInputMessage.setCreationTime(this.updateCalFromDTF(
                this.userInputMessage.getCreationTime(),
                this.creationDTF.getCalDateTimeValues()));
        this.userInputMessage.setEffectiveTime(this.updateCalFromDTF(
                this.userInputMessage.getEffectiveTime(),
                this.effectiveDTF.getCalDateTimeValues()));
        this.userInputMessage.setExpirationTime(this.updateCalFromDTF(
                this.userInputMessage.getExpirationTime(),
                this.expirationDTF.getCalDateTimeValues()));
        if ("00000000".equals(this.periodicityDTF.getFormattedValue()) == false) {
            this.userInputMessage.setPeriodicity(this.periodicityDTF
                    .getFormattedValue());
        }
        // skip mrd
        this.userInputMessage.setActive(this.activeRdo.getSelection());
        this.userInputMessage.setConfirm(this.confirmChk.getSelection());
        this.userInputMessage.setInterrupt(this.interruptChk.getSelection());
        this.userInputMessage.setAlertTone(this.alertChk.getSelection());
        this.userInputMessage.setValidHeader(true);

        // handle content
        this.userInputMessage.setContent(this.content.getText());
        // audio?
        if (this.content.getContentType() == CONTENT_TYPE.AUDIO) {
            request.setMessageAudio(this.content.getAudio());
        }
        request.setInputMessage(this.userInputMessage);

        // TODO: need to handle input message area on message type selection.
        List<Transmitter> selectedTransmitters = new ArrayList<Transmitter>();
        for (String transmitterMnemonic : this.sameTransmitters
                .getCheckedItems().getCheckedItems()) {
            selectedTransmitters.add(this.transmitterMap
                    .get(transmitterMnemonic));
        }
        request.setSelectedTransmitters(selectedTransmitters);
        try {
            BmhUtils.sendRequest(request);
        } catch (Exception e) {
            statusHandler.error("Failed to submit the weather message.", e);
        }

        /*
         * TODO: most likely temporary. Hopefully, we receive feedback about the
         * dialog end state during the demo.
         */
        DialogUtility.showMessageBox(this.shell, SWT.ICON_INFORMATION | SWT.OK,
                "Weather Messages",
                "The weather message has been successfully submitted.");
    }

    /**
     * Handle the area selection action.
     */
    private void handleAreadSelectionAction() {
        AreaSelectionDlg dlg = null;

        // if input message id is not null and not a new input message object
        if (userInputMessage != null && userInputMessage.getId() != 0) {
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

        this.areaSelectionBtn.setEnabled(true);
        sameTransmitters.selectCheckboxes(cld);

        String periodicityDateTimeStr = selectedMessageType.getPeriodicity();

        Map<DateFieldType, Integer> periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(periodicityDateTimeStr);

        periodicityDTF.setFieldValues(periodicityMap);

        interruptChk.setSelection(selectedMessageType.isInterrupt());
        alertChk.setSelection(selectedMessageType.isAlert());
        confirmChk.setSelection(selectedMessageType.isConfirm());

        String durationStr = selectedMessageType.getDuration();
        Map<DateFieldType, Integer> durationMap = BmhUtils
                .generateDayHourMinuteSecondMap(durationStr);
        long durationMillis = BmhUtils.getDurationMilliseconds(durationMap);
        Calendar cal = effectiveDTF.getBackingCalendar();
        cal.add(Calendar.SECOND,
                (int) (durationMillis / TimeUtil.MILLIS_PER_SECOND));

        expirationDTF.setDateTimeSpinners(cal);
    }

    /**
     * Reset the controls to "default" values.
     */
    private void resetControls() {
        selectedMessageType = null;
        this.content = null;

        // Input message name.
        msgNameTF.setText("");
        msgNameTF.setEditable(true);

        /*
         * Message Type Controls
         */
        msgTypeLbl.setText("");
        msgTitleLbl.setText("");
        languageLbl.setText("");
        designationLbl.setText("");
        emergenyOverrideLbl.setText("");
        sameTransmitters.selectCheckboxes(false);

        changeMsgTypeBtn.setEnabled(true);

        /*
         * Input Message controls.
         */
        interruptChk.setSelection(false);
        alertChk.setSelection(false);
        confirmChk.setSelection(false);

        activeRdo.setSelection(true);
        inactiveRdo.setSelection(false);

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
    private void populateControlsForEdit(InputMessage im,
            List<InputMessageAudioData> audioList) {
        userInputMessage = im;
        selectedMessageType = null;

        // Get the list of audio data.
        audioData = audioList;

        changeMsgTypeBtn.setEnabled(false);

        // Input message name.
        if (userInputMessage.getName() != null) {
            msgNameTF.setText(userInputMessage.getName());
        }

        /*
         * Message type controls.
         */
        getMessageType(userInputMessage.getAfosid());
        if (selectedMessageType == null) {
            areaSelectionBtn.setEnabled(false);
            changeMsgTypeBtn.setEnabled(true);
        } else {
            areaSelectionBtn.setEnabled(true);
        }

        updateMessageTypeControls();

        /*
         * Input Message controls.
         */
        if (userInputMessage.getInterrupt() != null) {
            interruptChk.setSelection(userInputMessage.getInterrupt());
        }
        if (userInputMessage.getAlertTone() != null) {
            alertChk.setSelection(userInputMessage.getAlertTone());
        }
        if (userInputMessage.getConfirm() != null) {
            confirmChk.setSelection(userInputMessage.getConfirm());
        }

        if (userInputMessage.getActive() != null) {
            boolean messageActive = userInputMessage.getActive();
            activeRdo.setSelection(messageActive);
            inactiveRdo.setSelection(!messageActive);
        } else {
            activeRdo.setSelection(false);
            inactiveRdo.setSelection(true);
        }

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

        // handle message contents.
        WxMessagesContent msgContent = new WxMessagesContent(
                this.determineContentType(this.userInputMessage.getContent()));
        // there will always be text no matter what the content type is like
        msgContent.setText(this.userInputMessage.getContent());
        if (msgContent.getContentType() == CONTENT_TYPE.AUDIO) {
            // we only care about audio when the content type is actually audio.

            // all audio records will be the same so, we only need to get the
            // first one.
            // TODO: can we guarantee that the audio will exist with
            // user-generated
            // audio?
            msgContent.setAudio(this.audioData.get(0).getAudio());
        }
        this.content = msgContent;
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

    /**
     * Warning this method of determining the content type does have risks. The
     * only way to make this method completely safe would be to ban incoming
     * messages from other sources that start with: "Recorded by".
     * 
     * @return
     */
    private CONTENT_TYPE determineContentType(final String content) {
        final String recordedBy = "Recorded by";

        return content.trim().startsWith(recordedBy) ? CONTENT_TYPE.AUDIO
                : CONTENT_TYPE.TEXT;
    }

    /*
     * TODO: weather messages and broadcast schedule need to be updated to share
     * common aspects.
     */
    private Calendar updateCalFromDTF(Calendar currentCal,
            Map<Integer, Integer> fieldValuesMap) {
        for (Integer calField : fieldValuesMap.keySet()) {
            currentCal.set(calField, fieldValuesMap.get(calField));
        }

        return currentCal;
    }
}