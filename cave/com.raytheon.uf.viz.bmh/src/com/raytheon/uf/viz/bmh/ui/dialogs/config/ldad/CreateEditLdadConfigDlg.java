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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * LDAD (Data Push) configuration dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2014    3381    mpduff      Initial creation
 * Nov 10, 2014    3381    bkowal      Dialog redesign.
 * Nov 13, 2014    3803    bkowal      Implemented dialog.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class CreateEditLdadConfigDlg extends CaveSWTDialog {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditLdadConfigDlg.class);

    /*
     * localhost will be the only valid host in practice mode.
     */
    private static final String PRACTICE_HOST = "localhost";

    private static final String CREATE_TITLE = "Create New LDAD Configuration";

    private static final String EDIT_TITLE = "Edit LDAD Configuration";

    /**
     * Data managers
     */
    private final LdadConfigDataManager dataManager = new LdadConfigDataManager();

    private final DictionaryManager dictionaryMgr = new DictionaryManager();

    private final VoiceDataManager vdm = new VoiceDataManager();

    /**
     * Name text field
     */
    private Text nameTxt;

    /**
     * Host text field
     */
    private Text hostTxt;

    /**
     * Directory text field
     */
    private Text directoryTxt;

    /**
     * Voice selection combo box
     */
    private Combo voiceCbo;

    /**
     * Dictionary selection combo box
     */
    private Combo dictCbo;

    /**
     * Encoding selection combo box
     */
    private Combo encodingCbo;

    private Button addMsgTypeButton;

    private Button removeMsgTypeButton;

    private MsgTypeTable selectedMsgTableComp;

    private LdadConfig ldadConfig;

    /**
     * Construtor.
     * 
     * @param parentShell
     *            The parent
     */
    public CreateEditLdadConfigDlg(Shell parentShell) {
        this(parentShell, null);
    }

    /**
     * Construtor.
     * 
     * @param parentShell
     *            The parent
     * @param ldadConfig
     *            An existing {@link LdadConfig}
     */
    public CreateEditLdadConfigDlg(Shell parentShell, LdadConfig ldadConfig) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.ldadConfig = ldadConfig;
        setText(this.ldadConfig == null ? CREATE_TITLE : EDIT_TITLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite selectionComp = new Composite(shell, SWT.NONE);
        selectionComp.setLayout(gl);
        selectionComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label hostLbl = new Label(selectionComp, SWT.NONE);
        hostLbl.setText("Configuration:");
        hostLbl.setLayoutData(gd);

        createConfigComp(shell);

        createBottomButtons();

        this.populateDialog();
    }

    /**
     * Create the configuration composite.
     * 
     * @param shell
     *            The shell
     */
    private void createConfigComp(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite configComp = new Composite(shell, SWT.BORDER);
        configComp.setLayout(gl);
        configComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label nameLbl = new Label(configComp, SWT.NONE);
        nameLbl.setText("Name:");
        nameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        nameTxt = new Text(configComp, SWT.BORDER);
        nameTxt.setLayoutData(gd);
        nameTxt.setTextLimit(40);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label hostLbl = new Label(configComp, SWT.NONE);
        hostLbl.setText("Host:");
        hostLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        hostTxt = new Text(configComp, SWT.BORDER);
        hostTxt.setLayoutData(gd);
        hostTxt.setTextLimit(60);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label directoryLbl = new Label(configComp, SWT.NONE);
        directoryLbl.setText("Directory:");
        directoryLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        directoryTxt = new Text(configComp, SWT.BORDER);
        directoryTxt.setLayoutData(gd);
        directoryTxt.setTextLimit(250);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label encodingLbl = new Label(configComp, SWT.NONE);
        encodingLbl.setText("Encoding:");
        encodingLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        encodingCbo = new Combo(configComp, SWT.SINGLE | SWT.READ_ONLY);
        encodingCbo.setLayoutData(gd);
        encodingCbo.select(0);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label voiceLbl = new Label(configComp, SWT.NONE);
        voiceLbl.setText("Voice:");
        voiceLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        voiceCbo = new Combo(configComp, SWT.SINGLE | SWT.READ_ONLY);
        voiceCbo.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label dictLbl = new Label(configComp, SWT.NONE);
        dictLbl.setText("Dictionary:");
        dictLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalAlignment = SWT.LEFT;
        dictCbo = new Combo(configComp, SWT.SINGLE);
        dictCbo.setLayoutData(gd);

        /* selected message types */
        this.createSelectedMsgTypesGroup();
    }

    private void createSelectedMsgTypesGroup() {
        final Group selectedMsgTypesGroup = new Group(this.shell,
                SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        selectedMsgTypesGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        selectedMsgTypesGroup.setLayoutData(gd);
        selectedMsgTypesGroup.setText("Selected Message Type(s):");

        /* Controls */
        Composite selectedMsgTypeComposite = new Composite(
                selectedMsgTypesGroup, SWT.NONE);
        gl = new GridLayout(2, false);
        selectedMsgTypeComposite.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        selectedMsgTypeComposite.setLayoutData(gd);

        /* Add Button */
        addMsgTypeButton = new Button(selectedMsgTypeComposite, SWT.PUSH);
        addMsgTypeButton.setText("Add...");
        gd = new GridData();
        gd.widthHint = 80;
        addMsgTypeButton.setLayoutData(gd);
        addMsgTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMsgTypeAddAction();
            }
        });

        /* Remove Button */
        removeMsgTypeButton = new Button(selectedMsgTypeComposite, SWT.PUSH);
        removeMsgTypeButton.setText("Remove...");
        gd = new GridData();
        gd.widthHint = 80;
        removeMsgTypeButton.setLayoutData(gd);
        removeMsgTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMsgTypeRemoveAction();
            }
        });
        removeMsgTypeButton.setEnabled(false);

        /* Table */
        selectedMsgTableComp = new MsgTypeTable(selectedMsgTypesGroup, 450, 100);
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title", 250);
        columnNames.add(tcd);
        TableData selectedMsgTypeTableData = new TableData(columnNames);
        selectedMsgTableComp.populateTable(selectedMsgTypeTableData);
        selectedMsgTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    removeMsgTypeButton.setEnabled(true);
                } else {
                    removeMsgTypeButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * Create the OK, Apply, Cancel buttons
     */
    private void createBottomButtons() {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveUpdateBtn = new Button(comp, SWT.PUSH);
        saveUpdateBtn.setText("Save");
        saveUpdateBtn.setLayoutData(gd);
        saveUpdateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSaveAction();
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
     * 
     */
    private void populateDialog() {
        this.populateVoiceList();
        this.populateEncodingCombo();
        this.populateDictCombo();

        if (this.ldadConfig == null) {
            return;
        }

        this.nameTxt.setText(this.ldadConfig.getName());
        if (CAVEMode.getMode() == CAVEMode.OPERATIONAL) {
            this.hostTxt.setText(this.ldadConfig.getHost());
        } else {
            /*
             * The host cannot be changed in practice mode.
             */
            this.hostTxt.setText(PRACTICE_HOST);
            this.hostTxt.setEnabled(false);
        }
        this.directoryTxt.setText(this.ldadConfig.getDirectory());
        this.encodingCbo.setText(this.ldadConfig.getEncoding().getExtension());
        this.voiceCbo.setText(this.ldadConfig.getVoice().getVoiceName());
        if (this.ldadConfig.getDictionary() != null) {
            this.dictCbo.setText(this.ldadConfig.getDictionary().getName());
        }
        this.addSelectedMsgType(this.ldadConfig.getMessageTypes());
    }

    private void populateVoiceList() {
        List<TtsVoice> voices = null;

        try {
            voices = vdm.getAllVoices();
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve the available voices.", e);
        }

        if (voices != null) {
            List<String> voiceList = new ArrayList<String>(voices.size());
            for (TtsVoice voice : voices) {
                voiceList.add(voice.getVoiceName());
                voiceCbo.setData(voice.getVoiceName(), voice);
            }

            Collections.sort(voiceList);

            voiceCbo.setItems(voiceList.toArray(new String[voiceList.size()]));
            voiceCbo.select(0);
        }
    }

    /**
     * Populate the encoding selection combo
     */
    private void populateEncodingCombo() {
        for (BMHAudioFormat bmhAudioFormat : BMHAudioFormat.values()) {
            this.encodingCbo.add(bmhAudioFormat.getExtension());
        }
    }

    /**
     * Populate the dictionary selection combo
     */
    private void populateDictCombo() {
        List<String> dictionaryNames = null;
        try {
            dictionaryNames = dictionaryMgr.getAllBMHDictionaryNames();
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to retrieve the available dictionaries.", e);
            return;
        }

        if (dictionaryNames == null || dictionaryNames.isEmpty()) {
            // no dictionaries.
            return;
        }

        Collections.sort(dictionaryNames);
        // TODO: need to update if we switch to a numeric dictionary id.
        for (String dictionaryName : dictionaryNames) {
            this.dictCbo.add(dictionaryName);
        }
    }

    private void handleMsgTypeAddAction() {
        this.addMsgTypeButton.setEnabled(false);
        SelectMessageTypeDlg dlg = new SelectMessageTypeDlg(this.shell, true);
        if (this.selectedMsgTableComp.getTableData().getTableRows().isEmpty() == false) {
            /*
             * Construct the msg type filter.
             */
            List<String> msgTypeIds = new ArrayList<>(this.selectedMsgTableComp
                    .getTableData().getTableRows().size());
            for (TableRowData trd : this.selectedMsgTableComp.getTableData()
                    .getTableRows()) {
                msgTypeIds
                        .add(((MessageTypeSummary) trd.getData()).getAfosid());
            }
            dlg.setFilteredMessageTypes(msgTypeIds);
        }
        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                addMsgTypeButton.setEnabled(true);
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof List<?> == false) {
                    return;
                }

                addSelectedMsgType((List<?>) returnValue);
            }
        });
        dlg.open();
    }

    private void handleMsgTypeRemoveAction() {
        this.removeMsgTypeButton.setEnabled(false);

        /*
         * Iterate through the selections and remove the matching message type
         * summaries from the table data.
         */
        for (TableRowData trd : this.selectedMsgTableComp.getSelection()) {
            this.selectedMsgTableComp.getTableData().deleteRow(trd);
        }
        this.selectedMsgTableComp.populateTable(this.selectedMsgTableComp
                .getTableData());
    }

    private void handleSaveAction() {
        if (this.valid() == false) {
            return;
        }

        if (this.ldadConfig == null) {
            this.ldadConfig = new LdadConfig();
        } else {
            /*
             * reset the selected message types. hibernate currently handles
             * deleting associations with message types that are no longer in
             * the list.
             */
            this.ldadConfig.getMessageTypes().clear();
        }
        this.ldadConfig.setName(this.nameTxt.getText());
        this.ldadConfig.setHost(this.hostTxt.getText());
        this.ldadConfig.setDirectory(this.directoryTxt.getText());
        /* add message types. */
        for (TableRowData trd : this.selectedMsgTableComp.getTableData()
                .getTableRows()) {
            if (trd.getData() instanceof MessageTypeSummary == false) {
                continue;
            }
            this.ldadConfig.addMessageType((MessageTypeSummary) trd.getData());
        }
        if (this.dictCbo.getSelectionIndex() != -1) {
            Dictionary dictionary = null;
            try {
                dictionary = this.dictionaryMgr.getDictionary(this.dictCbo
                        .getText());
            } catch (Exception e) {
                statusHandler.error("Failed to retrieve the dictionary named: "
                        + this.dictCbo.getText(), e);
                return;
            }

            if (dictionary == null) {
                statusHandler.error("Failed to find the dictionary named: "
                        + this.dictCbo.getText());
                return;
            }
            this.ldadConfig.setDictionary(dictionary);
        }
        /* tts voice */
        this.ldadConfig.setVoice((TtsVoice) this.voiceCbo.getData(this.voiceCbo
                .getText()));
        this.ldadConfig.setEncoding(BMHAudioFormat
                .lookupByExtension(this.encodingCbo.getText()));

        LdadConfig savedLdadConfig = null;
        try {
            savedLdadConfig = this.dataManager.saveLdadConfig(this.ldadConfig);
        } catch (Exception e) {
            statusHandler.error("Failed to save the ldad configuration!", e);
            return;
        }

        this.setReturnValue(savedLdadConfig);
        this.close();
    }

    private void addSelectedMsgType(Collection<?> messageTypes) {
        TableData tableData = this.selectedMsgTableComp.getTableData();
        for (Object object : messageTypes) {
            MessageTypeSummary messageTypeSummary = null;
            if (object instanceof MessageType) {
                messageTypeSummary = ((MessageType) object).getSummary();
            } else if (object instanceof MessageTypeSummary) {
                messageTypeSummary = (MessageTypeSummary) object;
            }
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(messageTypeSummary
                    .getAfosid()));
            trd.addTableCellData(new TableCellData(messageTypeSummary
                    .getTitle()));
            trd.setData(messageTypeSummary);
            tableData.addDataRow(trd);
        }

        this.selectedMsgTableComp.populateTable(tableData);
    }

    /**
     * Validate user's data
     * 
     * @return true if valid
     */
    private boolean valid() {
        boolean valid = true;

        /**
         * Using {@link LinkedList} to maintain the order of the validation
         * errors.
         */
        List<String> validationActionItems = new LinkedList<>();

        Color red = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
        if (nameTxt.getText().trim().length() <= 0) {
            nameTxt.setBackground(red);
            validationActionItems
                    .add("Name is a required field. Please enter a name.");
            valid = false;
        } else {
            nameTxt.setBackground(null);
        }

        // verify that the name does not conflict with an existing ldad config
        LdadConfig existingConfig = null;
        try {
            existingConfig = this.dataManager.getLdadConfigByName(this.nameTxt
                    .getText());
        } catch (Exception e) {
            statusHandler
                    .error("Failed to verify the uniqueness of the ldad configuration.");
            return false;
        }
        if (existingConfig != null
                && (this.ldadConfig == null || existingConfig.getId() != this.ldadConfig
                        .getId())) {
            nameTxt.setBackground(red);
            valid = false;
            validationActionItems
                    .add("Ldad Configuration with the name "
                            + existingConfig.getName()
                            + " already exists. Each ldad configuration must have a unique name.");
        } else {
            nameTxt.setBackground(null);
        }

        if (hostTxt.getText().trim().length() <= 0) {
            hostTxt.setBackground(red);
            validationActionItems
                    .add("Host is a required field. Please enter a host.");
            valid = false;
        } else {
            hostTxt.setBackground(null);
        }

        if (directoryTxt.getText().trim().length() <= 0) {
            directoryTxt.setBackground(red);
            validationActionItems
                    .add("Directory is a required field. Please enter a directory.");
            valid = false;
        } else {
            directoryTxt.setBackground(null);
        }

        if (this.encodingCbo.getSelectionIndex() == -1) {
            this.encodingCbo.setBackground(red);
            validationActionItems
                    .add("Encoding is a required field. Please select an encoding.");
            valid = false;
        } else {
            this.encodingCbo.setBackground(null);
        }

        if (this.voiceCbo.getSelectionIndex() == -1) {
            this.voiceCbo.setBackground(red);
            validationActionItems
                    .add("Voice is a required field. Please select a voice.");
            valid = false;
        } else {
            this.voiceCbo.setBackground(null);
        }

        if (this.selectedMsgTableComp.getTableData().getTableRows().isEmpty()) {
            validationActionItems
                    .add("A Message Type is required. Please add a Message Type.");
            valid = false;
        }

        if (valid) {
            nameTxt.setBackground(null);
            hostTxt.setBackground(null);
            directoryTxt.setBackground(null);
            this.encodingCbo.setBackground(null);
            this.voiceCbo.setBackground(null);
        } else {
            StringBuilder validationDtlsMsg = new StringBuilder(
                    "Validation of the Ldad Config has failed. Please address the following issues:\n");
            for (String vai : validationActionItems) {
                validationDtlsMsg.append("\n- ");
                validationDtlsMsg.append(vai);
            }
            DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                    "Ldad Configuration - Validation Failed",
                    validationDtlsMsg.toString());
        }
        return valid;
    }
}
