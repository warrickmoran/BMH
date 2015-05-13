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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.BroadcastMsgRequest;
import com.raytheon.uf.common.bmh.request.BroadcastMsgRequest.BroadcastMessageAction;
import com.raytheon.uf.common.bmh.request.BroadcastMsgResponse;
import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.bmh.request.InputMessageAudioResponse;
import com.raytheon.uf.common.bmh.request.InputMessageRequest;
import com.raytheon.uf.common.bmh.request.InputMessageRequest.InputMessageAction;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ImportedByUtils;
import com.raytheon.uf.viz.bmh.RecordedByUtils;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.ButtonImageCreator;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.SAMETransmitterSelector;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionSaveData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.InputMessageSequence.SEQUENCE_DIRECTION;
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
 * Nov 02, 2014  3785     mpduff      Set Same Transmitter values.
 * Nov 3, 2014   3785     bkowal      Fix the weather messages dialog.
 * Nov 03, 2014  3781     dgilling    Set SAME tone flag on input messages.
 * Nov 4, 2014   3778     bsteffen    Change the id after a submit.
 * Nov 11, 2014  3413     rferrel     Use DlgInfo to get title.
 * Nov 12, 2014  3823     bkowal      Use the stored wx message content.
 * Nov 17, 2014  3793     bsteffen    Add same transmitters to input message.
 * Nov 18, 2014  3829     bkowal      Use WxMessagesContent for all content tracking.
 * Nov 22, 2014  3796     mpduff      Checks for areas on unsaved messages.
 * Dec 02, 2014  3877     lvenable    Added null checks.
 * Dec 02, 2014  3876     lvenable    Added check for area selection.
 * Dec 03, 2014  3876     lvenable    Added null check & cleared out area codes if they are removed.
 * Dec 02, 2014  3614     bsteffen    Do not report success when submit fails.
 * Dec 09, 2014  3893     lvenable    Fixed the area selection call to not repopulate the
 *                                    areas/zones/transmitters if they were previously removed.
 * Dec 09, 2014  3909     bkowal      Use {@link RecordedByUtils}.
 * Dec 15, 2014  3876     bkowal      Only use the checked SAME Transmitters for
 *                                    SAME Tones. Use affected Transmitters to
 *                                    determine where a message should be directed.
 * Jan 13, 2015  3876     lvenable    Update input message to use the area/zone codes from the selected
 *                                    message type if the user didn't change the area selection.
 * Jan 16, 2015  4005     bkowal      Determine area/zone codes at the time of the message type
 *                                    selection or existing input message selection.
 * Jan 20, 2015  4010     bkowal      Updated to pass accurate information to the
 *                                    Area Selection Dialog.
 * Feb 10, 2015  4085     bkowal      Prevent users from creating weather messages associated
 *                                    with static message types.
 * Feb 11, 2015  4115     bkowal      Confirm submission of expired messages.
 * Feb 11, 2015  4044     rjpeter     Only select transmitters whose program contains the message type.
 * Feb 12, 2015  4113     bkowal      Users can now advance through the input messages using a previous
 *                                    and next button.
 * Feb 16, 2015  4118     bkowal      Check for imported audio when determining message type.
 * Mar 03, 2015  4211     bkowal      Do not allow users to submit expired messages.
 * Mar 03, 2015  4212     bkowal      Display the Tone Playback confirmation for any combination
 *                                    of SAME and Alert tones.
 * Mar 05, 2015  4222     bkowal      Allow users to create messages that never expire.
 * Mar 10, 2015  4249     rferrel     Better help message for SAME disabled transmitters.
 * Mar 11, 2015  4254     rferrel     Better dialog message for bad words or duplicate message.
 * Mar 16, 2015  4244     bsteffen    Extract same transmitter logic into SAMETransmitterSelector.
 * Mar 17, 2015  4160     bsteffen    Check if tones have played when modifying an existing message.
 * Mar 18, 2015  4282     rferrel     Added Close button and editStatus flag.
 *                                    Add modification checks.
 *                                    Keep in edit mode when Next/Prev fails to retrieve message.
 * Apr 16, 2014  4408     rferrel     Put in edit state after submitting a new message.
 * Apr 20, 2015  4420     rferrel     When in edit mode display read only Area Selection dialog.
 * Apr 29, 2015  4451     bkowal      Message contents will now only be accessible when a Message
 *                                    Type has been selected.
 * May 04, 2015  4447     bkowal      SAME Transmitters assigned to an existing {@link InputMessage}
 *                                    will override SAME Transmitters assigned to a {@link MessageType}.
 * May 08, 2015  4477     bkowal      Disable the Contents button when a "New" message is started.
 * May 08, 2015  4429     rferrel     {@link #handleSubmitAction()} now sets traceId for the request.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class WeatherMessagesDlg extends AbstractBMHDialog implements
        KeyListener {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WeatherMessagesDlg.class);

    /** Buttons used to advance through sequenced records when loaded. */
    private Button prevSequenceBtn;

    private Label sequenceLbl;

    private Button nextSequenceBtn;

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

    /** Checkbox used to indicate that a message does not expire. */
    private Button noExpireChk;

    /** Periodicity date/time field. */
    private DateTimeFields periodicityDTF;

    /** List of SAME transmitters. */
    private SAMETransmitterSelector sameTransmitters;

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

    /** Area data from the Area Selection dialog. */
    private AreaSelectionSaveData areaData;

    /** Selected Message Type */
    private MessageType selectedMessageType = null;

    /** Input message selected by the user. */
    private InputMessage userInputMessage = null;

    /** Button used to change the message type. */
    private Button changeMsgTypeBtn;

    /** Wx Message Content. */
    private WxMessagesContent content = null;

    /** When true performing an edit. */
    private boolean editStatus = true;

    /**
     * Keeps track of the available input messages that the user will be able to
     * move back and forth through.
     */
    private InputMessageSequence messageSequence;

    /* Unmodifed current message. */
    private InputMessage currentIm = null;

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
        super(dlgMap, DlgInfo.WEATHER_MESSAGES.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
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
        super.initializeComponents(shell);
        setText(DlgInfo.WEATHER_MESSAGES.getTitle());

        createHeaderButtons();
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
        if (isModified()) {
            String msg = "Closing will lose changes.\nSelect OK to continue.";
            int choice = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.OK | SWT.CANCEL, "Close Weather Message", msg);
            return choice == SWT.OK;
        }
        return true;
    }

    /**
     * Create the new, edit, and message navigation buttons.
     */
    private void createHeaderButtons() {
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(new GridLayout(5, false));
        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        int buttonWidth = 70;
        GridData gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button newBtn = new Button(btnComp, SWT.PUSH);
        newBtn.setText("New");
        newBtn.setLayoutData(gd);
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (isModified()) {
                    String msg = "Creating a new weather message will lose existing changes.\nOK to continue?";
                    int choice = DialogUtility.showMessageBox(shell,
                            SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                            "New Weather Message", msg);

                    if (choice != SWT.OK) {
                        return;
                    }
                }

                handleNewAction();
                /*
                 * disable the sequenced message buttons when the user interacts
                 * with a message that is not in the sequence.
                 */
                setEditStatus(false);
                sequenceLbl.setText("");
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button editBtn = new Button(btnComp, SWT.PUSH);
        editBtn.setText("Edit...");
        editBtn.setLayoutData(gd);
        editBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isModified()) {
                    String msg = "Editing a weather message will lose existing changes.\nSelect OK to continue.";
                    int choice = DialogUtility.showMessageBox(shell,
                            SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                            "Edit Weather Message", msg);

                    if (choice != SWT.OK) {
                        return;
                    }
                }

                SelectInputMsgDlg simd = new SelectInputMsgDlg(shell);
                simd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue == null) {
                            return;
                        }

                        if (returnValue instanceof InputMessageSequence) {
                            messageSequence = (InputMessageSequence) returnValue;
                            setEditStatus(true);
                            loadMessageFromSequence();
                            nextSequenceBtn.forceFocus();
                        }
                    }
                });
                simd.open();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        gd.horizontalIndent = 15;
        prevSequenceBtn = new Button(btnComp, SWT.PUSH);
        prevSequenceBtn.setText("< Prev");
        prevSequenceBtn.setLayoutData(gd);
        prevSequenceBtn.setEnabled(false);
        prevSequenceBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                continueSequence(SEQUENCE_DIRECTION.LEFT);
            }
        });
        prevSequenceBtn.addKeyListener(this);

        gd = new GridData(95, SWT.DEFAULT);
        sequenceLbl = new Label(btnComp, SWT.CENTER);
        sequenceLbl.setLayoutData(gd);

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        nextSequenceBtn = new Button(btnComp, SWT.PUSH);
        nextSequenceBtn.setText("Next >");
        nextSequenceBtn.setLayoutData(gd);
        nextSequenceBtn.setEnabled(false);
        nextSequenceBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                continueSequence(SEQUENCE_DIRECTION.RIGHT);
            }
        });
        nextSequenceBtn.addKeyListener(this);

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
    }

    /**
     * When set to true dilog is editing a message else dialog is creating a
     * message. Performs enabling/disabling of various components when status is
     * changed.
     * 
     * @param editStatus
     */
    public void setEditStatus(boolean editStatus) {
        if (this.editStatus != editStatus) {
            this.editStatus = editStatus;
            prevSequenceBtn.setEnabled(editStatus);
            nextSequenceBtn.setEnabled(editStatus);
            msgNameTF.setEnabled(!editStatus);
            changeMsgTypeBtn.setEnabled(!editStatus);
            interruptChk.setEnabled(!editStatus);
            alertChk.setEnabled(!editStatus);
            confirmChk.setEnabled(!editStatus);
            creationDTF.setEnabled(!editStatus);
            effectiveDTF.setEnabled(!editStatus);
            sameTransmitters.setAllowEnableTransmitters(!editStatus);
        }
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
        areaData = null;
        resetControls();
        sameTransmitters.reset();
        this.populateControlsForEdit(im, null);
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
                /*
                 * Identify all static message type(s) and ensure that they are
                 * filtered out.
                 */
                final MessageTypeDataManager mtdm = new MessageTypeDataManager();
                final Set<String> staticAfosIds;
                try {
                    staticAfosIds = mtdm.getStaticMessageAfosIds();
                } catch (Exception e1) {
                    statusHandler
                            .error("Failed to retrieve the afos ids associated with static message types.",
                                    e1);
                    return;
                }

                SelectMessageTypeDlg selectMsgTypeDlg = new SelectMessageTypeDlg(
                        shell);
                selectMsgTypeDlg.setFilteredMessageTypes(new ArrayList<>(
                        staticAfosIds));
                selectMsgTypeDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null) {
                            selectedMessageType = (MessageType) returnValue;
                            sameTransmitters.reset();
                            updateMessageTypeControls(null);
                        } else {
                            if (selectedMessageType == null) {
                                areaSelectionBtn.setEnabled(false);
                                contentsBtn.setEnabled(false);
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
        gd = new GridData(SWT.RIGHT, SWT.TOP, true, true);
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

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        noExpireChk = new Button(expirationDTFComp, SWT.CHECK);
        noExpireChk.setText("No Expiration");
        noExpireChk.setLayoutData(gd);
        noExpireChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (noExpireChk.getSelection()) {
                    expirationDTF.setEnabled(false);
                    userInputMessage.setExpirationTime(null);
                } else {
                    expirationDTF.setEnabled(true);
                    /*
                     * this will eventually be updated to match whatever is in
                     * the spinners before the message is submitted.
                     */
                    userInputMessage.setExpirationTime(expirationDTF
                            .getBackingCalendar());
                }
            }
        });

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
                handleAreaSelectionAction();
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
        sameTransmitters = new SAMETransmitterSelector(mainComp, true, 80, 250);
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
        interruptChk.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                sameTransmitters.setInterrupt(interruptChk.getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

        });

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
        /*
         * Disable the Contents button until a message type has been selected.
         */
        this.contentsBtn.setEnabled(false);

        FontData fd = contentsBtn.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.setHeight(24);
        bic.setFontData(fd);

        Image img = bic.generateImage(250, 40, "Contents", new RGB(0, 235, 0));
        contentsBtn.setImage(img);
        contentsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageContentsDlg mcd = new MessageContentsDlg(shell, content
                        .getAudioDataList(), content.getText(), content
                        .getContentType(), selectedMessageType.getVoice()
                        .getVoiceNumber());
                mcd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue == null) {
                            // cancelled
                            return;
                        }

                        if ((returnValue instanceof WxMessagesContent) == false) {
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
        GridLayout gl = new GridLayout(2, false);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, false,
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

        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (okToClose()) {
                    close();
                }
            }
        });
    }

    private boolean validate() {
        /*
         * verify that a name has been set.
         */
        if ((this.msgNameTF.getText() == null)
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
         * Set the NWR same tone flag if no transmitters were selected.
         */
        if (this.sameTransmitters.isEmpty()) {
            userInputMessage.setNwrsameTone(false);
        } else {
            userInputMessage.setNwrsameTone(true);
        }

        /*
         * Check if the input message has selected area/zone codes. If not then
         * create the area/zone codes string and set it in the input message.
         */
        if (((userInputMessage.getAreaCodes() == null) || userInputMessage
                .getAreaCodes().isEmpty())
                && ((this.userInputMessage.getSelectedTransmitters() == null) || this.userInputMessage
                        .getSelectedTransmitters().isEmpty())) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Area Selection",
                            "Area/Zone/Transmitter codes must be selected. Please add them by clicking the Area Selection button.");
            return false;
        }

        /*
         * verify that message contents have been set.
         */
        if ((this.content != null) && (this.content.isComplete() == false)) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Contents",
                            "No Message Contents have been provided. Please record a message or enter text. Click on the 'Contents' button to get started.");
            return false;
        }

        IStatus transmitterStatus = sameTransmitters
                .getAffectedTransmitterStatus();
        if (transmitterStatus.getSeverity() == IStatus.ERROR) {
            DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                    "Weather Messages - Area Selection",
                    transmitterStatus.getMessage());
            return false;
        }

        /*
         * verify that the message has not already expired: 1) expiration time
         * is < the current time or 2) effective time and expiration time are
         * the same.
         */
        if (this.noExpireChk.getSelection()) {
            /* message never expires */

            // messages that include SAME tones must have an expiration
            // date/time
            if (sameTransmitters.isEmpty() == false) {
                DialogUtility
                        .showMessageBox(
                                this.shell,
                                SWT.ICON_ERROR | SWT.OK,
                                "Weather Messages - No Expiration",
                                "An expiration date/time must be set for Message "
                                        + this.msgNameTF.getText()
                                        + " for the SAME Tones. Please set an expiration date/time.");
                return false;
            }

            return true;
        }
        long effectiveTime = this.effectiveDTF.getBackingCalendar()
                .getTimeInMillis();
        long expirationTime = this.expirationDTF.getBackingCalendar()
                .getTimeInMillis();
        if ((expirationTime == effectiveTime)
                || (expirationTime <= System.currentTimeMillis())) {
            /*
             * Do not allow a user to submit an expired message.
             */
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Expired",
                            "Message "
                                    + this.msgNameTF.getText()
                                    + " has already expired. Please update the expiration date/time.");
            return false;
        }

        return true;
    }

    private void handleSubmitAction() {

        if (this.validate() == false) {
            return;
        }

        if (this.verifyTones() == false) {
            return;
        }

        if (!isModified()) {
            String msg = "No changes to submit.";
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Edit Weather Message", msg);

            return;
        }

        NewBroadcastMsgRequest request = new NewBroadcastMsgRequest();
        String traceId = BmhUtils.genererateTraceId(this.getClass());
        request.setTraceId(traceId);

        this.areaSelectionBtn.setEnabled(true);
        this.userInputMessage.setValidHeader(true);

        if (this.content.getContentType() == CONTENT_TYPE.AUDIO) {
            request.setMessageAudio(this.content.getAudioDataList().get(0)
                    .getAudio());
        }
        request.setInputMessage(this.userInputMessage);

        /*
         * FIXME: Determine method to pass user-selected SAME transmitters
         * through entire process so PlaylistManager can use it to write the
         * proper SAME tone string.
         */
        userInputMessage.setNwrsameTone(!sameTransmitters.isEmpty());

        if (Boolean.TRUE.equals(userInputMessage.getNwrsameTone())) {
            userInputMessage.setSameTransmitterSet(sameTransmitters
                    .getSAMETransmitterMnemonics());
        }

        request.setSelectedTransmitters(new ArrayList<>(sameTransmitters
                .getAffectedTransmitters()));
        try {
            Object result = BmhUtils.sendRequest(request);
            if (result instanceof Integer) {
                userInputMessage.setId((Integer) result);
                if (!editStatus) {
                    /*
                     * Put in edit mode filtered to just the newly submited
                     * message.
                     */
                    messageSequence = new InputMessageSequence(0,
                            new int[] { userInputMessage.getId() });
                    setEditStatus(true);
                    loadMessageFromSequence();
                    nextSequenceBtn.forceFocus();
                }

            }
        } catch (Exception e) {
            String msg = ExceptionUtils.getRootCauseMessage(e);
            if (msg != null) {
                // Strip msg of the leading Exception: tag.
                msg = msg.substring(msg.indexOf(":") + 1).trim();
            }

            if ((msg == null) || msg.isEmpty()) {
                statusHandler.handle(Priority.WARN,
                        "Failed to submit the weather message.", e);
                DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR
                        | SWT.OK, "Weather Messages",
                        "Failed to submit the weather message.");
            } else {
                DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR
                        | SWT.OK, "Weather Messages",
                        "Failed to submit the weather message:\n\n" + msg);
            }
            return;
        }

        /*
         * TODO: most likely temporary. Hopefully, we receive feedback about the
         * dialog end state during the demo.
         */
        DialogUtility.showMessageBox(this.shell, SWT.ICON_INFORMATION | SWT.OK,
                "Weather Messages",
                "The weather message has been successfully submitted.");

        currentIm = new InputMessage(userInputMessage);

        if (this.nextSequenceBtn.isEnabled()) {
            /*
             * enable message sequencing via the arrow buttons if sequenced
             * messages have been loaded.
             */
            this.nextSequenceBtn.forceFocus();
        }
    }

    /**
     * Modify userInputMessage to current GUI values.
     */
    private void updateUserInputMessage() {
        String name = this.msgNameTF.getText().trim();
        this.userInputMessage.setName(name);

        if (selectedMessageType != null) {
            this.userInputMessage.setLanguage(this.selectedMessageType
                    .getVoice().getLanguage());
            this.userInputMessage.setAfosid(this.selectedMessageType
                    .getAfosid());
        } else {
            this.userInputMessage.setLanguage(null);
            this.userInputMessage.setAfosid(null);
        }
        this.userInputMessage.setCreationTime(this.updateCalFromDTF(
                this.userInputMessage.getCreationTime(),
                this.creationDTF.getCalDateTimeValues()));
        this.userInputMessage.setEffectiveTime(this.updateCalFromDTF(
                this.userInputMessage.getEffectiveTime(),
                this.effectiveDTF.getCalDateTimeValues()));
        if (this.noExpireChk.getSelection()) {
            this.userInputMessage.setExpirationTime(null);
        } else {
            this.userInputMessage.setExpirationTime(this.updateCalFromDTF(
                    this.userInputMessage.getExpirationTime(),
                    this.expirationDTF.getCalDateTimeValues()));
        }
        if ("00000000".equals(this.periodicityDTF.getFormattedValue()) == false) {
            this.userInputMessage.setPeriodicity(this.periodicityDTF
                    .getFormattedValue());
        } else {
            this.userInputMessage.setPeriodicity(null);
        }
        // skip mrd
        this.userInputMessage.setActive(this.activeRdo.getSelection());
        this.userInputMessage.setConfirm(this.confirmChk.getSelection());
        this.userInputMessage.setInterrupt(this.interruptChk.getSelection());
        this.userInputMessage.setAlertTone(this.alertChk.getSelection());

        /*
         * Handle content. When an Audio is used the contents is changed to
         * start with #Recorded or #Import followed by who did the action, if
         * needed link to imported file and timestamp. This is sufficent to pick
         * up any changes and allow Submit Message.
         */
        this.userInputMessage.setContent(this.content.getText());

    }

    /*
     * Check if tones will play and if so make sure the user knows.
     * 
     * @return true if tones aren't playing or the user has agreed to the tones.
     * False if tones will play and the user doens't want them to.
     */
    protected boolean verifyTones() {
        boolean isAlert = this.alertChk.getSelection();
        if (isAlert || !sameTransmitters.isEmpty()) {
            if (userInputMessage.getId() != 0) {
                BroadcastMsgRequest req = new BroadcastMsgRequest();
                req.setAction(BroadcastMessageAction.GET_MESSAGE_BY_INPUT_ID);
                req.setMessageId((long) userInputMessage.getId());
                try {
                    BroadcastMsgResponse response = (BroadcastMsgResponse) BmhUtils
                            .sendRequest(req);

                    boolean allToned = true;
                    for (BroadcastMsg broadcastMessage : response
                            .getMessageList()) {
                        if ((!isAlert && !broadcastMessage.isPlayedSameTone())
                                || !broadcastMessage.isPlayedAlertTone()) {
                            allToned = false;
                            break;
                        }
                    }
                    if (allToned) {
                        return true;
                    }
                } catch (Exception e) {
                    statusHandler.handle(Priority.WARN,
                            "Unable to check if message has played tones.", e);
                }
            }
            // alert the user that they are about to play same tones.
            int option = DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_WARNING | SWT.YES | SWT.NO,
                            "Weather Messages - Tone Playback",
                            this.selectedMessageType.getTitle()
                                    + " will activate SAME and/or Alert Tones! Would you like to continue?");
            if (option != SWT.YES) {
                return false;
            }
        }
        return true;
    }

    /**
     * Handle the area selection action.
     */
    private void handleAreaSelectionAction() {
        AreaSelectionDlg dlg = new AreaSelectionDlg(getShell(), this.areaData,
                editStatus);
        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                areaData = (AreaSelectionSaveData) returnValue;
                updateInputMsgAreas();
                sameTransmitters.setAreaData(areaData);
            }
        });
        dlg.open();
    }

    /**
     * Set the message type controls using the selected message type.
     */
    private void updateMessageTypeControls(final ValidatedMessage validatedMsg) {
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

        this.areaSelectionBtn.setEnabled(true);
        this.contentsBtn.setEnabled(true);

        String periodicityDateTimeStr = selectedMessageType.getPeriodicity();

        Map<DateFieldType, Integer> periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(periodicityDateTimeStr);

        periodicityDTF.setFieldValues(periodicityMap);

        interruptChk.setSelection(selectedMessageType.isInterrupt());
        sameTransmitters.setInterrupt(selectedMessageType.isInterrupt());
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

        if (validatedMsg == null) {
            // eliminate any existing area codes.
            this.userInputMessage.setAreaCodes(null);

            /**
             * New Input Message: Retrieve any areas associated with the
             * selected message type and pre-populate the information for the
             * dialog.
             */
            areaData = new AreaSelectionSaveData(selectedMessageType);
            this.updateInputMsgAreas();
        } else {
            /**
             * Existing Input Message: Extract the area information from the
             * existing input message. If we were to just use the areas
             * associated with the message type, we risk violating the integrity
             * of the input message because the user may have added other areas
             * to the input message instead of just using the default.
             */
            this.areaData = new AreaSelectionSaveData();
            AreaSelectionData areaSelectionData = new AreaSelectionData();
            try {
                areaSelectionData.populate();
            } catch (Exception e) {
                statusHandler.error("Error accessing BMH area/zone data", e);
                return;
            }
            Set<Transmitter> listOfAffectedTransmitters = new HashSet<Transmitter>();

            for (String code : this.userInputMessage.getAreaCodeSet()) {
                if (code.charAt(2) == 'Z') {
                    this.areaData.addZone(areaSelectionData.getZonesMap().get(
                            code));
                } else {
                    this.areaData.addArea(areaSelectionData.getAllAreaCodes()
                            .get(code));
                }
            }

            if ((this.userInputMessage.getSelectedTransmitters() != null)
                    && (this.userInputMessage.getSelectedTransmitters()
                            .isEmpty() == false)) {
                for (Transmitter t : this.userInputMessage
                        .getSelectedTransmitters()) {
                    this.areaData.addTransmitter(t);
                }
            }

            /**
             * Determine the affected transmitters based on the destination
             * transmitters in the validated message.
             */
            for (TransmitterGroup tg : validatedMsg.getTransmitterGroups()) {
                listOfAffectedTransmitters.addAll(tg.getTransmitters());
            }
            this.areaData.setAffectedTransmitters(listOfAffectedTransmitters);
        }

        sameTransmitters.setAreaData(areaData);
        sameTransmitters.setMessageType(selectedMessageType);
        if (this.userInputMessage.getId() != 0) {
            /*
             * this is an existing input message. use the SAME transmitters
             * specified in the input message instead of the transmitters
             * specified for the message type.
             */
            this.sameTransmitters.overrideMessageTypeSAME(this.userInputMessage
                    .getSameTransmitterSet());
        }
    }

    /**
     * Reset the controls to "default" values.
     */
    private void resetControls() {
        selectedMessageType = null;
        this.content = null;

        setEditStatus(false);

        // Input message name.
        msgNameTF.setText("");

        /*
         * Message Type Controls
         */
        msgTypeLbl.setText("");
        msgTitleLbl.setText("");
        languageLbl.setText("");
        designationLbl.setText("");
        emergenyOverrideLbl.setText("");
        setEditStatus(false);

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
     * Builds a {@link String} of area codes based on the
     * {@link AreaSelectionSaveData} and adds the area code {@link String} to
     * the {@link InputMessage}. Will also add any {@link Transmitter}s that
     * have been individually selected to the {@link InputMessage}.
     */
    private void updateInputMsgAreas() {
        if (this.areaData == null) {
            return;
        }

        Set<String> areaZoneCodes = areaData.getSelectedAreaZoneCodes();
        if ((areaZoneCodes == null) || areaZoneCodes.isEmpty()) {
            userInputMessage.setAreaCodes("");
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter = areaZoneCodes.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());

                if (iter.hasNext()) {
                    sb.append("-");
                }
            }
            this.userInputMessage.setAreaCodes(sb.toString());
        }
        if ((this.areaData.getTransmitters() != null)
                && (this.areaData.getTransmitters().isEmpty() == false)) {
            this.userInputMessage.setSelectedTransmitters(this.areaData
                    .getTransmitters());
        } else {
            /*
             * default to NULL when there are not any selected transmitters.
             */
            this.userInputMessage.setSelectedTransmitters(null);
        }
    }

    /**
     * Populate the controls when in the "edit state".
     * 
     * @param inputMessage
     *            Selected input message.
     * 
     * @param audioList
     *            List of InputMesageAudioData
     * 
     * @param validatedMessage
     *            The ValidatedMessage
     */
    private void populateControlsForEdit(InputMessage im, InputAudioMessage iam) {
        final ValidatedMessage vm = (iam == null) ? null : iam
                .getValidatedMsg();
        final List<InputMessageAudioData> audioDataList = (iam == null) ? null
                : iam.getAudioDataList();

        userInputMessage = im;

        selectedMessageType = null;
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
            this.contentsBtn.setEnabled(false);
            changeMsgTypeBtn.setEnabled(true);
        } else {
            areaSelectionBtn.setEnabled(true);
        }

        updateMessageTypeControls(vm);

        /*
         * Input Message controls.
         */
        if (userInputMessage.getInterrupt() != null) {
            interruptChk.setSelection(userInputMessage.getInterrupt());
            sameTransmitters.setInterrupt(userInputMessage.getInterrupt());
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
        if (userInputMessage.getCreationTime() != null) {
            creationDTF.setDateTimeSpinners(userInputMessage.getCreationTime());
        }

        if (userInputMessage.getEffectiveTime() != null) {
            effectiveDTF.setDateTimeSpinners(userInputMessage
                    .getEffectiveTime());
        }

        if (userInputMessage.getExpirationTime() != null) {
            this.noExpireChk.setSelection(false);
            expirationDTF.setDateTimeSpinners(userInputMessage
                    .getExpirationTime());
        } else {
            if (this.userInputMessage.getId() != 0
                    && this.userInputMessage.isValidHeader()) {
                this.noExpireChk.setSelection(true);
                this.expirationDTF.setEnabled(false);
            }
        }

        // Update periodicity
        String peridicityStr = userInputMessage.getPeriodicity();
        if ((peridicityStr == null) || (peridicityStr.length() == 0)
                || (peridicityStr.length() != 8)) {
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
        msgContent.setAudioDataList(audioDataList);

        this.content = msgContent;
        submitMsgBtn.setEnabled(true);

        // Do the update logic so currentIm will be correct for modified checks.
        updateUserInputMessage();
        currentIm = new InputMessage(userInputMessage);
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
        return (RecordedByUtils.isMessage(content) || ImportedByUtils
                .isMessage(content)) ? CONTENT_TYPE.AUDIO : CONTENT_TYPE.TEXT;
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

    private void continueSequence(final SEQUENCE_DIRECTION direction) {
        if (isModified()) {
            String msg = "Changing weather message will lose existing changes.\nSelect OK to continue.";
            int choice = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.OK | SWT.CANCEL, "Edit Weather Message", msg);

            if (choice != SWT.OK) {
                return;
            }
        }
        this.messageSequence.advanceSequence(direction);
        this.loadMessageFromSequence();
    }

    private void loadMessageFromSequence() {
        if (this.messageSequence == null) {
            return;
        }

        int id = this.messageSequence.getCurrentSequence();
        if (id == -1) {
            statusHandler
                    .error("No input message sequencing information exists.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.messageSequence.getCurrentIndex() + 1).append(" of ")
                .append(this.messageSequence.getMaxSequence());
        this.sequenceLbl.setText(sb.toString());

        InputAudioMessage im = null;

        try {
            im = this.getInputMessageById(id);
        } catch (Exception e) {
            handleNewAction();
            setEditStatus(true);
            submitMsgBtn.setEnabled(false);
            currentIm = null;
            String msg = "Failed to retrieve the Input Message with id: " + id
                    + ".\n\n" + BmhUtils.getRootCauseMessage(e);
            DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                    "Weather Message", msg);
            return;
        }
        sameTransmitters.reset();
        populateControlsForEdit(im.getInputMessage(), im);
    }

    /**
     * Get the Input Message from the database using the primary key Id.
     * 
     * @param id
     *            Primary Key Id.
     * @return The input message and list of audio.
     */
    private InputAudioMessage getInputMessageById(int id) throws Exception {
        InputMessageRequest imRequest = new InputMessageRequest();
        imRequest.setAction(InputMessageAction.GetByPkId);
        imRequest.setPkId(id);

        InputAudioMessage inputAudioMessageData = null;

        InputMessageAudioResponse imResponse = (InputMessageAudioResponse) BmhUtils
                .sendRequest(imRequest);
        List<InputMessage> inputMessages = imResponse.getInputMessageList();

        if (inputMessages == null || inputMessages.isEmpty()) {
            return inputAudioMessageData;
        }

        // Create the input audio message data and populate the object.
        inputAudioMessageData = new InputAudioMessage();

        inputAudioMessageData.setInputMessage(inputMessages.get(0));

        // Check if the the audio is null. If it is then set it up to be an
        // empty list.
        List<InputMessageAudioData> audioDataList = imResponse
                .getAudioDataList();
        if (imResponse.getAudioDataList() == null) {
            audioDataList = Collections.emptyList();
        }

        inputAudioMessageData.setAudioDataList(audioDataList);
        inputAudioMessageData.setValidatedMsg(imResponse.getValidatedMessage());

        return inputAudioMessageData;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Do Nothing.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        /*
         * if the left arrow key or the right arrow key has been released, set
         * the focus to to corresponding sequence button and advance the message
         * sequence.
         */
        if (e.keyCode == SWT.ARROW_LEFT) {
            this.prevSequenceBtn.forceFocus();
            this.continueSequence(SEQUENCE_DIRECTION.LEFT);
        } else if (e.keyCode == SWT.ARROW_RIGHT) {
            this.nextSequenceBtn.forceFocus();
            this.continueSequence(SEQUENCE_DIRECTION.RIGHT);
        }
    }

    /**
     * Update the user input message from the GUI and determine if it is
     * different from the current unmodified input message.
     * 
     * @return true when modified
     */
    private boolean isModified() {
        updateUserInputMessage();
        return (currentIm != null) && !currentIm.equals(userInputMessage);
    }
}