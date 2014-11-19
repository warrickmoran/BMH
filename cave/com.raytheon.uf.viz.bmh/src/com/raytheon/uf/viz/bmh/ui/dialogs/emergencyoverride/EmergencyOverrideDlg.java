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
package com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.ButtonImageCreator;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListComp;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionSaveData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeAfosComparator;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.LiveBroadcastRecordPlaybackDlg;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Emergency Override dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 16, 2014  #3611     lvenable     Initial creation
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 10, 2014  #3656     bkowal       Initial transmit capability implementation.
 * Oct 14, 2014  #3728     lvenable     Change table to single selection and fixed the duration
 *                                      spinners since they are a special case.
 * Oct 16, 2014  #3657     bkowal       Implemented Message Type and Area Selection.
 * Oct 17, 2014  #3655     bkowal       Create a {@link LiveBroadcastSettings} based on
 *                                      the dialog information for the Live Broadcast.
 * Oct 22, 2014  #3745     lvenable     Fixed UELE with the Area Selection Dialog.
 * Oct 26, 2014  #3712     bkowal       Implemented dialog interaction for rebroadcast
 *                                      scheduling.
 * Nov 1, 2014   #3657     bkowal       Display a confirmation dialog to notify the user that
 *                                      SAME / Alert Tones will be played.
 * Nov 3, 2014   #3655     bkowal       Enable Duration label. Fix for duration population
 *                                      based on message type.
 * Nov 11, 2014  3413      rferrel      Use DlgInfo to get title.
 * Nov 17, 2014  3808      bkowal       Initial support for broadcast live.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class EmergencyOverrideDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EmergencyOverrideDlg.class);

    /** Table containing the emergency message types. */
    private GenericTable emerMsgTypeTable;

    /** Emergency Message Type table data. */
    private TableData msgTypeTableData = null;

    /** List of emergency override messages. */
    private List<MessageType> emerOverrideMsgTypes;

    /** List of SAME transmitters. */
    /*
     * Currently all selections will be overwritten whenever the user changes
     * the selected areas via the Area Selection Dialog or whenever the user
     * selects a new message type. This will undo any changes that the user
     * manually made to the selection.
     */
    private CheckScrollListComp sameTransmitters;

    /** Map of all the transmitters. */
    private final Map<String, Transmitter> transmitterMap = new HashMap<>();

    /** Selected message type. */
    private MessageType selectedMsgType = null;

    /** Area selection button. */
    private Button areaSelectionBtn;

    /** Transmit button. */
    private Button transmitBtn;

    /** Alert check box. */
    private Button alertChk;

    /** Auto schedule check box. */
    private Button autoScheduleChk;

    /** Duration label. */
    private Label durationLbl;

    /** Duration hour spinner. */
    private Spinner durHourSpnr;

    /** Duration minute spinner. */
    private Spinner durMinuteSpnr;

    /** Maximum number of hours for the duration. */
    private final int hourMax = 6;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public EmergencyOverrideDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.EMERGENCY_OVERRIDE.getTitle(), parentShell,
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
        setText(DlgInfo.EMERGENCY_OVERRIDE.getTitle());

        createMainControls();
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
     * Create the main controls.
     */
    private void createMainControls() {
        Composite mainControlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        mainControlComp.setLayout(gl);
        mainControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        createMessageTypeTable(mainControlComp);
        createMiddleActionControls(mainControlComp);
        createSameTransmitterControls(mainControlComp);

        populateEmergencyMsgTypeTable();
    }

    /**
     * Create the emergency override table.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createMessageTypeTable(Composite mainComp) {
        Group messageTypeGroup = new Group(mainComp, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        messageTypeGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        messageTypeGroup.setLayoutData(gd);
        messageTypeGroup.setText(" Emergency Message Types: ");

        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
        emerMsgTypeTable = new GenericTable(messageTypeGroup, tableStyle, 400,
                175);

        emerMsgTypeTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    // only one item can be selected.
                    handleMsgTypeSelection();
                }
            }
        });
    }

    /**
     * Create the area, transmit and other controls in the middle of the dialog.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createMiddleActionControls(Composite mainComp) {
        ButtonImageCreator bic = new ButtonImageCreator(shell);

        Composite controlComp = new Composite(mainComp, SWT.BORDER
                | SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalIndent = 5;
        controlComp.setLayout(gl);
        controlComp.setLayoutData(gd);

        Point imageWidthHeight = new Point(220, 35);

        /*
         * Area Selection button
         */
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        areaSelectionBtn = new Button(controlComp, SWT.PUSH);
        areaSelectionBtn.setLayoutData(gd);

        // Setup the Font data.
        FontData fd = areaSelectionBtn.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.setHeight(20);
        bic.setFontData(fd);

        Image areaSelectionImg = bic.generateImage(imageWidthHeight.x,
                imageWidthHeight.y, "Area Selection", new RGB(255, 255, 0));
        areaSelectionBtn.setImage(areaSelectionImg);
        areaSelectionBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAreaSelection();
            }
        });

        /*
         * Alert
         */
        gd = new GridData();
        gd.verticalIndent = 5;
        alertChk = new Button(controlComp, SWT.CHECK);
        alertChk.setText("Alert");
        alertChk.setLayoutData(gd);

        /*
         * Auto Schedule
         */
        gd = new GridData();
        gd.verticalIndent = 5;
        autoScheduleChk = new Button(controlComp, SWT.CHECK);
        autoScheduleChk.setText("Auto Schedule");
        autoScheduleChk.setLayoutData(gd);

        Composite autoSchedComp = new Composite(controlComp, SWT.SHADOW_OUT);
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        autoSchedComp.setLayout(gl);
        autoSchedComp.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = 15;
        durationLbl = new Label(autoSchedComp, SWT.CENTER);
        durationLbl.setText("Duration\n(HHMM)");
        durationLbl.setLayoutData(gd);

        gd = new GridData(30, SWT.DEFAULT);
        gd.horizontalIndent = 10;
        durHourSpnr = new Spinner(autoSchedComp, SWT.BORDER);
        durHourSpnr.setLayoutData(gd);
        durHourSpnr.setTextLimit(2);
        durHourSpnr.setMinimum(0);
        durHourSpnr.setMaximum(hourMax);
        durHourSpnr.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (durHourSpnr.getSelection() == hourMax) {
                    durMinuteSpnr.setSelection(0);
                    durMinuteSpnr.setEnabled(false);
                } else {
                    durMinuteSpnr.setEnabled(true);
                }
            }
        });

        durMinuteSpnr = new Spinner(autoSchedComp, SWT.BORDER);
        durMinuteSpnr.setLayoutData(new GridData(30, SWT.DEFAULT));
        durMinuteSpnr.setTextLimit(2);
        durMinuteSpnr.setMinimum(0);
        durMinuteSpnr.setMaximum(59);

        /*
         * Transmit button
         */
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        transmitBtn = new Button(controlComp, SWT.PUSH);
        transmitBtn.setLayoutData(gd);

        Image transmitImg = bic.generateImage(imageWidthHeight.x,
                imageWidthHeight.y, "Transmit", new RGB(0, 235, 0));
        transmitBtn.setImage(transmitImg);
        transmitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTransmitAction();
            }
        });
    }

    /**
     * Create the SAME transmitter controls.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createSameTransmitterControls(Composite mainComp) {
        CheckListData cld = createTransmitterListData();

        sameTransmitters = new CheckScrollListComp(mainComp,
                "SAME Transmitter: ", cld, false, 100, 165);
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

        if (selectedMsgType != null) {
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
     * Populate the Emergency message type table.
     */
    private void populateEmergencyMsgTypeTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 120);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);

        msgTypeTableData = new TableData(columnNames);

        populateMsgTypeTableData();
        emerMsgTypeTable.populateTable(msgTypeTableData);
    }

    /**
     * Populate the message type table data.
     */
    private void populateMsgTypeTableData() {
        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

        try {
            emerOverrideMsgTypes = msgTypeDataMgr
                    .getEmergencyOverrideMsgTypes(new MsgTypeAfosComparator());
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving emergency override message type data from the database: ",
                            e);
            return;
        }

        for (MessageType mt : emerOverrideMsgTypes) {
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            trd.setData(mt);
            msgTypeTableData.addDataRow(trd);
        }
    }

    private void handleMsgTypeSelection() {
        List<TableRowData> selections = this.emerMsgTypeTable.getSelection();
        this.selectedMsgType = (MessageType) selections.get(0).getData();

        // Update Alert checkbox
        this.alertChk.setSelection(this.selectedMsgType.isAlert());

        // Update the duration
        Map<DateFieldType, Integer> timeMap = BmhUtils
                .generateDayHourMinuteSecondMap(this.selectedMsgType
                        .getDuration());
        final int durationHour = timeMap.get(DateFieldType.HOUR);
        if (durationHour > hourMax) {
            this.durHourSpnr.setSelection(hourMax);
            this.durMinuteSpnr.setSelection(0);
            this.durMinuteSpnr.setEnabled(false);
        } else {
            this.durHourSpnr.setSelection(durationHour);
            final int durationMinute = timeMap.get(DateFieldType.MINUTE);
            // assuming the data insertion / update validation will ensure that
            // the minute is within the proper range.
            this.durMinuteSpnr.setSelection(durationMinute);
            this.durMinuteSpnr.setEnabled(true);
        }

        // Update the selected transmitters
        this.sameTransmitters
                .selectCheckboxes(this.createTransmitterListData());
    }

    /**
     * Display the area selection dialog.
     */
    private void handleAreaSelection() {
        AreaSelectionDlg areaSelectionDlg = new AreaSelectionDlg(this.shell,
                this.selectedMsgType);
        areaSelectionDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                AreaSelectionSaveData areaData = (AreaSelectionSaveData) returnValue;
                sameTransmitters.selectCheckboxes(false);
                CheckListData cld = sameTransmitters.getCheckedItems();

                for (Transmitter selectedTransmitter : areaData
                        .getAffectedTransmitters()) {
                    cld.getDataMap().put(selectedTransmitter.getMnemonic(),
                            true);
                }

                sameTransmitters.selectCheckboxes(cld);
            }
        });

        areaSelectionDlg.open();
    }

    private void handleTransmitAction() {
        if (this.validateSelections() == false) {
            return;
        }

        // get the selected transmitters.
        List<String> transmitterNames = this.sameTransmitters.getCheckedItems()
                .getCheckedItems();
        List<Transmitter> transmitters = new ArrayList<>(
                transmitterNames.size());
        for (String transmitterName : transmitterNames) {
            /*
             * only transmitters associated with the selected message type
             * should be available for selection.
             */
            transmitters.add(this.transmitterMap.get(transmitterName));
        }

        final LiveBroadcastSettings settings = new LiveBroadcastSettings();
        try {
            settings.populate(this.selectedMsgType, transmitters, null,
                    this.alertChk.getSelection(),
                    this.durHourSpnr.getSelection(),
                    this.durMinuteSpnr.getSelection());
        } catch (Exception e) {
            statusHandler.error("Failed to configure the live broadcast!", e);
            return;
        }

        // alert the user that they are about to play same tones.
        int option = DialogUtility
                .showMessageBox(
                        this.shell,
                        SWT.ICON_WARNING | SWT.YES | SWT.NO,
                        "Emergency Override - Tone Playback",
                        this.selectedMsgType.getTitle()
                                + " will activate SAME and/or Alert Tones! Would you like to continue?");
        if (option != SWT.YES) {
            return;
        }

        LiveBroadcastRecordPlaybackDlg dlg = new LiveBroadcastRecordPlaybackDlg(
                this.shell, 120, settings);
        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof ByteBuffer == false) {
                    return;
                }

                /*
                 * We will need the audio that was recorded for potential
                 * rebroadcast scheduling.
                 */
                handleTransmitComplete((ByteBuffer) returnValue, settings);
            }
        });
        dlg.open();
    }

    private boolean validateSelections() {
        /* Message Type always has to be selected based on how the table works. */

        /*
         * In legacy code, an entire day was added for a duration of 0 hours 0
         * minutes - CI_emerg.c.
         */

        /*
         * Verify that at least one transmitter has been selected.
         */
        if (this.sameTransmitters.getCheckedItems().getCheckedItems().size() <= 0) {
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Emergency Override - Transmitters",
                            "No transmitters have been selected. At least one transmitter must be selected.");
            return false;
        }

        return true;
    }

    private void handleTransmitComplete(ByteBuffer recordedAudio,
            final LiveBroadcastSettings settings) {

        if (this.autoScheduleChk.getSelection()) {
            this.handleMessageScheduling(recordedAudio, settings,
                    this.buildInputMsg(settings));
        } else {
            this.handleManualMsgScheduling(recordedAudio, settings);
        }
    }

    private void handleManualMsgScheduling(final ByteBuffer recordedAudio,
            final LiveBroadcastSettings settings) {
        InputMessage inputMsg = this.buildInputMsg(settings);

        MessageScheduleDlg scheduleDlg = new MessageScheduleDlg(this.shell,
                inputMsg, settings.getSelectedMessageType());
        scheduleDlg.setCloseCallback(new ICloseCallback() {

            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    // indicates Cancel was clicked.
                    statusHandler
                            .info("The rebroadcasting process has been cancelled. The live broadcast will not be rebroadcasted.");

                    return;
                }

                if (returnValue instanceof InputMessage == false) {
                    // unlikely (impossible?) scenario
                    return;
                }

                handleMessageScheduling(recordedAudio, settings,
                        (InputMessage) returnValue);
            }
        });
        scheduleDlg.open();
    }

    private void handleMessageScheduling(final ByteBuffer recordedAudio,
            final LiveBroadcastSettings settings,
            final InputMessage inputMessage) {
        NewBroadcastMsgRequest request = new NewBroadcastMsgRequest();
        request.setInputMessage(inputMessage);
        request.setMessageAudio(recordedAudio.array());
        request.setSelectedTransmitters(new ArrayList<>(settings
                .getSelectedTransmitters()));
        try {
            BmhUtils.sendRequest(request);
        } catch (Exception e) {
            statusHandler.error("Failed to schedule the rebroadcast.", e);
        }
    }

    private InputMessage buildInputMsg(final LiveBroadcastSettings settings) {
        /*
         * Generate a message name.
         */
        final String generatedMsgName = "LiveMsg-"
                + Long.toString(System.currentTimeMillis());

        InputMessage inputMsg = new InputMessage();
        inputMsg.setName(generatedMsgName);
        inputMsg.setLanguage(settings.getSelectedMessageType().getVoice()
                .getLanguage());
        inputMsg.setAfosid(settings.getSelectedMessageType().getAfosid());
        inputMsg.setCreationTime(TimeUtil.newGmtCalendar());
        inputMsg.setEffectiveTime(settings.getEffectiveTime());
        inputMsg.setPeriodicity(settings.getSelectedMessageType()
                .getPeriodicity());
        // no default mrd
        inputMsg.setActive(true);
        // default confirm to FALSE to prevent NPE
        inputMsg.setConfirm(false);
        // the initial broadcast was already an interrupt.
        inputMsg.setInterrupt(false);
        // already played SAME and Alert tones during the initial broadcast.
        inputMsg.setAlertTone(false);
        inputMsg.setNwrsameTone(false);
        inputMsg.setAreaCodes(settings.getAreaCodes());
        inputMsg.setExpirationTime(settings.getExpireTime());
        inputMsg.setContent(StringUtils.EMPTY);
        inputMsg.setValidHeader(true);

        return inputMsg;
    }
}