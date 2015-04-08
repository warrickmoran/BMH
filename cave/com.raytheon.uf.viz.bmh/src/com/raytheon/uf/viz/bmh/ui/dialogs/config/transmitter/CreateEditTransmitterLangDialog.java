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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.voice.SelectDictionaryDlg;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Dialog used to create or update a {@link TransmitterLanguage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 13, 2015 3809       bkowal      Initial creation
 * Feb 12, 2015 4082       bkowal      Handle the case when Transmitter Languages cannot be
 *                                     saved because the associated group does not exist.
 * Feb 18, 2015 4142       bkowal      Added {@link RateOfSpeechComp} to the dialog.
 * Feb 19, 2015 4142       bkowal      Retrieve/update the rate of speech in the
 *                                     selected {@link TransmitterLanguage}.
 * Feb 24, 2015 4157       bkowal      Supply a {@link Language} to the {@link RateOfSpeechComp}.
 * Feb 24, 2015 4082       bkowal      Prevent duplicate voice entries as a result of re-selection
 *                                     of the currently selected language.
 * Mar 12, 2015 4213       bkowal      Support static message types.
 * Mar 26, 2015 4213       bkowal      Handle the case when no static message types exist.
 * Mar 31, 2015 4248       rjpeter     Update to use PositionOrdered returns.
 * Apr 08, 2015 4248       bkowal      Static Message Types are now only changed when the user saves
 *                                     the associated Transmitter Language.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CreateEditTransmitterLangDialog extends CaveSWTDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditTransmitterLangDialog.class);

    private final VoiceDataManager vdm = new VoiceDataManager();

    private final ProgramDataManager pdm = new ProgramDataManager();

    private static final String CREATE_TITLE = "Create Transmitter Language";

    private static final String EDIT_TITLE = "Edit Transmitter Language";

    private static final String SELECT_LANGUAGE = "Select a Language";

    private static final String SELECT_VOICE = "Select a Voice";

    private final TransmitterGroup transmitterGroup;

    private final ProgramSummary selectedProgram;

    private TransmitterLanguage transmitterLanguage;

    private final List<Language> unassignedLanguages;

    /**
     * flag indicating whether or not a language can actually be saved to the
     * database based on whether the associated Transmitter Group currently
     * exists or not. If the group does not exist (new group or when a
     * transmitter is transitioned to standalone), the languages will be saved
     * when the group is saved instead of independently being saved.
     */
    private final boolean saveCapable;

    private final Map<String, Integer> voiceNameIdentifierMap = new HashMap<>();

    /*
     * Allows the user to select an available language when creating a
     * transmitter language.
     */
    private Combo languageCombo;

    /*
     * the language that is currently selected for the transmitter language.
     * will be NULL is a language has not been selected yet.
     */
    private Language selectedLanguage;

    private TtsVoice selectedVoice;

    /*
     * Allows the user to select an available voice. The voices that are
     * displayed in this combo will be based on the selected language. If a
     * language has not been selected yet, no voices will be displayed.
     */
    private Combo voiceCombo;

    /*
     * Used for dictionary management.
     */
    private Label selectedDictionaryLabel;

    private Button changeBtn;

    /*
     * the dictionary that is currently selected for the transmitter language.
     * will be NULL if a dictionary has not been selected yet.
     */
    private Dictionary selectedDictionary;

    private RateOfSpeechComp rateOfSpeechComp;

    /*
     * Static Message Type Components
     */
    private MsgTypeTable staticMsgTable;

    private Button addMsgTypeButton;

    private Button editMsgTypeButton;

    private Button deleteMsgTypeButton;

    private List<StaticMessageType> newStaticMessageTypes;

    public CreateEditTransmitterLangDialog(Shell parentShell,
            List<Language> unassignedLanguages,
            TransmitterGroup transmitterGroup, ProgramSummary selectedProgram) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.unassignedLanguages = unassignedLanguages;
        this.transmitterGroup = transmitterGroup;
        this.selectedProgram = selectedProgram;
        this.setText(CREATE_TITLE);
        this.saveCapable = ((transmitterGroup != null) && (transmitterGroup
                .getId() != 0));
    }

    public CreateEditTransmitterLangDialog(Shell parentShell,
            TransmitterLanguage transmitterLanguage,
            TransmitterGroup transmitterGroup, ProgramSummary selectedProgram) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.transmitterLanguage = transmitterLanguage;
        this.unassignedLanguages = Collections.emptyList();
        this.transmitterGroup = transmitterGroup;
        this.selectedProgram = selectedProgram;
        this.setText(EDIT_TITLE);
        this.saveCapable = ((transmitterGroup != null) && (transmitterGroup
                .getId() != 0));
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
        this.createAttributeFields(shell);
        this.populateDialog();

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        this.createBottomButtons(shell);
    }

    private void createAttributeFields(final Shell shell) {
        /* Create the frame. */
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        Group attributesGroup = new Group(shell, SWT.BORDER);
        attributesGroup.setText(" Attributes ");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        /* The composite for the fields. */
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        final Composite attributesComp = new Composite(attributesGroup,
                SWT.NONE);
        attributesComp.setLayout(gl);
        attributesComp.setLayoutData(gd);

        /*
         * language field: read-only when this is an existing transmitter
         * language
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label languageLabel = new Label(attributesComp, SWT.NONE);
        languageLabel.setText("Language:");
        languageLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        gd.widthHint = 180;
        if (this.transmitterLanguage == null) {
            /*
             * This is a new transmitter language. Allow the user to select a
             * language.
             */
            this.languageCombo = new Combo(attributesComp, SWT.BORDER
                    | SWT.READ_ONLY);
            this.languageCombo.setLayoutData(gd);

            this.languageCombo.add(SELECT_LANGUAGE);
            this.languageCombo.select(0);

            for (Language language : this.unassignedLanguages) {
                this.languageCombo.add(language.toString());
            }
            this.languageCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    handleLanguageSelection();
                }
            });
        } else {
            /*
             * This is an existing transmitter language. Just display the
             * associated language.
             */
            Label displayLanguageLabel = new Label(attributesComp, SWT.NONE);
            displayLanguageLabel.setText(this.transmitterLanguage.getLanguage()
                    .toString());
            displayLanguageLabel.setLayoutData(gd);
        }

        /*
         * voice field: limited to the voices associated with the selected
         * language.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label voiceLabel = new Label(attributesComp, SWT.NONE);
        voiceLabel.setText("Voice:");
        voiceLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        this.voiceCombo = new Combo(attributesComp, SWT.BORDER | SWT.READ_ONLY);
        this.voiceCombo.setLayoutData(gd);
        this.voiceCombo.setEnabled(false);
        this.voiceCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleVoiceSelection();
            }
        });

        /*
         * Dictionary field. No limits.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label dictionaryLabel = new Label(attributesComp, SWT.NONE);
        dictionaryLabel.setText("Dictionary:");
        dictionaryLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 200;
        selectedDictionaryLabel = new Label(attributesComp, SWT.BORDER);
        selectedDictionaryLabel.setLayoutData(gd);

        /*
         * Dictionary selection button. Only enabled when a language has been
         * selected.
         */
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        changeBtn = new Button(attributesComp, SWT.PUSH);
        changeBtn.setText("Change...");
        changeBtn.setLayoutData(gd);
        changeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleChangeAction();
            }
        });
        changeBtn.setEnabled(false);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label rateOfSpeechLabel = new Label(attributesComp, SWT.NONE);
        rateOfSpeechLabel.setText("Rate of Speech:");
        rateOfSpeechLabel.setLayoutData(gd);

        this.rateOfSpeechComp = new RateOfSpeechComp(attributesComp, 2);

        this.createStaticMessageTypesGroup();
    }

    private void createStaticMessageTypesGroup() {
        final Group staticMsgTypesGroup = new Group(this.shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        staticMsgTypesGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        staticMsgTypesGroup.setLayoutData(gd);
        staticMsgTypesGroup.setText("Static Message Type(s):");

        /* Controls */
        Composite staticMsgTypeComposite = new Composite(staticMsgTypesGroup,
                SWT.NONE);
        gl = new GridLayout(3, false);
        staticMsgTypeComposite.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        staticMsgTypeComposite.setLayoutData(gd);

        /* Add Button */
        addMsgTypeButton = new Button(staticMsgTypeComposite, SWT.PUSH);
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
        addMsgTypeButton.setEnabled(false);

        /* Edit Button */
        editMsgTypeButton = new Button(staticMsgTypeComposite, SWT.PUSH);
        editMsgTypeButton.setText("Edit...");
        gd = new GridData();
        gd.widthHint = 80;
        editMsgTypeButton.setLayoutData(gd);
        editMsgTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMsgTypeEditAction();
            }
        });
        editMsgTypeButton.setEnabled(false);

        /* Delete Button */
        deleteMsgTypeButton = new Button(staticMsgTypeComposite, SWT.PUSH);
        deleteMsgTypeButton.setText("Delete...");
        gd = new GridData();
        gd.widthHint = 80;
        deleteMsgTypeButton.setLayoutData(gd);
        deleteMsgTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMsgTypeDeleteAction();
            }
        });
        deleteMsgTypeButton.setEnabled(false);

        /*
         * Static Message Types table.
         */
        staticMsgTable = new MsgTypeTable(staticMsgTypesGroup, 350, 100);
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Message Type", 100);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title", 200);
        columnNames.add(tcd);
        TableData selectedMsgTypeTableData = new TableData(columnNames);
        staticMsgTable.populateTable(selectedMsgTypeTableData);
        staticMsgTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    editMsgTypeButton.setEnabled(true);
                    deleteMsgTypeButton.setEnabled(true);
                } else {
                    editMsgTypeButton.setEnabled(false);
                    deleteMsgTypeButton.setEnabled(false);
                }
            }
        });
    }

    private void handleMsgTypeAddAction() {
        SelectStaticMsgTypeDialog selectStaticMsgTypeDlg = new SelectStaticMsgTypeDialog(
                this.shell, this.getExistingStaticMessageTypes(),
                this.selectedVoice, this.selectedProgram);
        selectStaticMsgTypeDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof MessageType) {
                    newStaticMsgTypeSelected((MessageType) returnValue);
                }
            }
        });
        selectStaticMsgTypeDlg.open();
    }

    private List<StaticMessageType> getExistingStaticMessageTypes() {
        if (this.transmitterLanguage != null) {
            if (this.transmitterLanguage.getStaticMessageTypes() == null) {
                return Collections.emptyList();
            }
            return this.transmitterLanguage.getOrderedStaticMessageTypes();
        } else {
            if (this.newStaticMessageTypes != null) {
                return this.newStaticMessageTypes;
            }
        }

        return Collections.emptyList();
    }

    private void newStaticMsgTypeSelected(MessageType messageType) {
        CreateEditStaticMsgTypeDialog staticMsgTypeDlg = new CreateEditStaticMsgTypeDialog(
                this.shell, messageType);
        staticMsgTypeDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof StaticMessageType) {
                    saveUpdateStaticMsgType((StaticMessageType) returnValue);
                }
            }
        });
        staticMsgTypeDlg.open();
    }

    private void handleMsgTypeEditAction() {
        /*
         * Retrieve the static message type from the selected row.
         */
        StaticMessageType staticMessageType = (StaticMessageType) this.staticMsgTable
                .getSelection().get(0).getData();
        CreateEditStaticMsgTypeDialog staticMsgTypeDlg = new CreateEditStaticMsgTypeDialog(
                this.shell, staticMessageType);
        staticMsgTypeDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof StaticMessageType) {
                    saveUpdateStaticMsgType((StaticMessageType) returnValue);
                }
            }
        });
        staticMsgTypeDlg.open();
    }

    private void saveUpdateStaticMsgType(StaticMessageType staticMsgType) {
        if (this.transmitterLanguage == null) {
            /*
             * This is a new transmitter language. Static Message Types will be
             * saved when the transmitter language is saved.
             */
            if (this.newStaticMessageTypes == null) {
                this.newStaticMessageTypes = new ArrayList<>();
            }
            this.newStaticMessageTypes.add(staticMsgType);
            this.addStaticMsgTypeToTable(staticMsgType);
        } else {
            boolean update = true;
            /*
             * Is this a new or existing static message type?
             */
            if (staticMsgType.getTransmitterLanguage() == null) {
                /*
                 * this is a new static message type.
                 */
                update = false;
            }

            /*
             * Determine if this static message type is a candidate for
             * broadcasting.
             */
            StringBuilder sb = new StringBuilder();
            if (this.selectedProgram == null) {
                // we have no way to complete verification.
                sb.append("No program has been selected. Unable to determine if static message type: ");
                sb.append(staticMsgType.getMsgTypeSummary().getAfosid())
                        .append(" can be successfully scheduled for broadcast.");
            } else {
                boolean recognized = true;
                try {
                    List<String> staticAfosIds = this.pdm
                            .getStaticAfosIdsForProgram(this.selectedProgram);
                    recognized = staticAfosIds.contains(staticMsgType
                            .getMsgTypeSummary().getAfosid());
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to determine the static message type(s) associated with Program: "
                                    + this.selectedProgram.getName() + ".", e);
                }

                if (recognized == false) {
                    sb.append("Static message type ").append(
                            staticMsgType.getMsgTypeSummary().getAfosid());
                    sb.append(" will not be successfully scheduled for broadcast because it is not part of the selected program.");
                }
            }

            final String msg = sb.toString();
            if (msg.isEmpty() == false) {
                /*
                 * This is just to notify the user. However, it will not
                 * actually prevent them from creating a Static MessageType.
                 */
                DialogUtility.showMessageBox(this.shell, SWT.ICON_WARNING
                        | SWT.OK,
                        "Transmitter Language - Static Message Types", msg);
            }

            this.transmitterLanguage.addStaticMessageType(staticMsgType);
            if (update) {
                this.updateStaticMsgTypeToTable(staticMsgType);
            } else {
                this.addStaticMsgTypeToTable(staticMsgType);
            }
        }
    }

    private void handleMsgTypeDeleteAction() {
        StaticMessageType staticMessageType = (StaticMessageType) this.staticMsgTable
                .getSelection().get(0).getData();

        StringBuilder sb = new StringBuilder(
                "Are you sure you want to delete static message type: ");
        sb.append(staticMessageType.getMsgTypeSummary().getAfosid())
                .append("?");
        int option = DialogUtility.showMessageBox(this.shell, SWT.ICON_QUESTION
                | SWT.YES | SWT.NO, "Static Message Type - Delete",
                sb.toString());
        if (option != SWT.YES) {
            return;
        }

        /*
         * has the Transmitter Language been created yet?
         */
        if (this.transmitterLanguage == null) {
            /*
             * the transmitter language has not been created yet - just need to
             * remove references to the static message type in the dialog.
             */
            Iterator<StaticMessageType> staticMsgIterator = this.newStaticMessageTypes
                    .iterator();
            while (staticMsgIterator.hasNext()) {
                if (staticMsgIterator
                        .next()
                        .getMsgTypeSummary()
                        .getAfosid()
                        .equals(staticMessageType.getMsgTypeSummary()
                                .getAfosid())) {
                    staticMsgIterator.remove();
                    break;
                }
            }
        } else {
            this.transmitterLanguage.removeStaticMessageType(staticMessageType);
        }

        this.removeStaticMsgTypeFromTable(staticMessageType);
    }

    private void populateDialog() {
        if (this.transmitterLanguage == null) {
            return;
        }

        this.selectedLanguage = this.transmitterLanguage.getLanguage();
        this.populateVoices();
        this.voiceCombo.setText(this.transmitterLanguage.getVoice()
                .getVoiceName());

        this.selectedDictionary = this.transmitterLanguage.getDictionary();
        if (this.selectedDictionary != null) {
            this.selectedDictionaryLabel.setText(this.selectedDictionary
                    .getName());
        }
        this.changeBtn.setEnabled(true);

        this.handleVoiceSelection();

        this.rateOfSpeechComp.setInitialRateOfSpeech(this.transmitterLanguage
                .getSpeechRate());

        this.addMsgTypeButton.setEnabled((this.selectedLanguage != null)
                && ((this.transmitterGroup != null) && (this.transmitterGroup
                        .getId() > 0)));
        if ((this.transmitterLanguage == null)
                || (this.transmitterLanguage.getStaticMessageTypes() == null)
                || this.transmitterLanguage.getStaticMessageTypes().isEmpty()) {
            return;
        }
        this.populateStaticMsgTypes();
    }

    private void populateStaticMsgTypes() {
        TableData tableData = this.staticMsgTable.getTableData();
        for (StaticMessageType smt : this.transmitterLanguage
                .getOrderedStaticMessageTypes()) {
            MessageTypeSummary messageTypeSummary = smt.getMsgTypeSummary();

            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(messageTypeSummary
                    .getAfosid()));
            trd.addTableCellData(new TableCellData(messageTypeSummary
                    .getTitle()));
            trd.setData(smt);
            tableData.addDataRow(trd);
        }

        this.staticMsgTable.populateTable(tableData);
    }

    public void addStaticMsgTypeToTable(StaticMessageType smt) {
        TableData tableData = this.staticMsgTable.getTableData();

        MessageTypeSummary messageTypeSummary = smt.getMsgTypeSummary();

        TableRowData trd = new TableRowData();
        trd.addTableCellData(new TableCellData(messageTypeSummary.getAfosid()));
        trd.addTableCellData(new TableCellData(messageTypeSummary.getTitle()));
        trd.setData(smt);
        tableData.addDataRow(trd);

        this.staticMsgTable.populateTable(tableData);
    }

    public void updateStaticMsgTypeToTable(StaticMessageType smt) {
        for (TableRowData trd : this.staticMsgTable.getTableData()
                .getTableRows()) {
            if (((StaticMessageType) trd.getData()).getMsgTypeSummary()
                    .getAfosid().equals(smt.getMsgTypeSummary().getAfosid())) {
                trd.setData(smt);
                break;
            }
        }
    }

    public void removeStaticMsgTypeFromTable(StaticMessageType smt) {
        TableRowData trdToRemove = null;
        for (TableRowData trd : this.staticMsgTable.getTableData()
                .getTableRows()) {
            if (((StaticMessageType) trd.getData()).getMsgTypeSummary()
                    .getAfosid().equals(smt.getMsgTypeSummary().getAfosid())) {
                trdToRemove = trd;
                break;
            }
        }

        if (trdToRemove != null) {
            this.staticMsgTable.getTableData().deleteRow(trdToRemove);
            this.staticMsgTable.populateTable(this.staticMsgTable
                    .getTableData());
        }
        this.editMsgTypeButton.setEnabled(false);
        this.deleteMsgTypeButton.setEnabled(false);
    }

    private void handleLanguageSelection() {
        final String languageText = this.languageCombo.getText();
        if (SELECT_LANGUAGE.equals(languageText)) {
            selectedLanguage = null;
            /*
             * clear the voice and dictionary selections. wait for the user to
             * select a language.
             */
            this.voiceCombo.setItems(new String[] {});
            this.voiceCombo.setEnabled(false);
            this.voiceNameIdentifierMap.clear();

            selectedDictionary = null;
            this.selectedDictionaryLabel.setText("");
            this.changeBtn.setEnabled(false);
            return;
        }

        /*
         * a language has been selected.
         */
        selectedLanguage = null;
        for (Language language : Language.values()) {
            if (language.toString().equals(languageText)) {
                selectedLanguage = language;
                break;
            }
        }

        /*
         * Retrieve applicable voices and populate the voice combo.
         */
        this.populateVoices();

        /*
         * Enable dictionary selection.
         */
        this.changeBtn.setEnabled(true);
    }

    private void populateVoices() {
        /*
         * Remove any existing elements from the voice combo.
         */
        this.voiceCombo.setItems(new String[] {});
        this.voiceCombo.add(SELECT_VOICE);
        List<TtsVoice> voices = null;
        try {
            voices = this.vdm.getIdentfiersForLanguage(selectedLanguage);
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve the voices for language: "
                    + selectedLanguage.toString() + ".", e);
            return;
        }
        for (TtsVoice voice : voices) {
            this.voiceCombo.add(voice.getVoiceName());
            this.voiceNameIdentifierMap.put(voice.getVoiceName(),
                    voice.getVoiceNumber());
        }
        this.voiceCombo.setEnabled(true);
        this.voiceCombo.select(0);
    }

    private void handleChangeAction() {
        SelectDictionaryDlg selectDictDlg = new SelectDictionaryDlg(this.shell,
                this.selectedLanguage);
        selectDictDlg.setFilterDictionary(this.selectedDictionary);
        selectDictDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if ((returnValue == null)
                        || ((returnValue instanceof Dictionary) == false)) {
                    return;
                }

                selectedDictionary = (Dictionary) returnValue;
                selectedDictionaryLabel.setText(selectedDictionary.getName());
            }
        });
        selectDictDlg.open();
    }

    private void createBottomButtons(final Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveUpdateBtn = new Button(comp, SWT.PUSH);
        final String saveText = (this.saveCapable) ? "Save" : "OK";
        saveUpdateBtn.setText(saveText);
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
                setReturnValue(null);
                close();
            }
        });
    }

    private boolean validate() {
        if (this.transmitterLanguage == null) {
            /*
             * verify that a language has been specified.
             */
            if (SELECT_LANGUAGE.equals(this.languageCombo.getText())) {
                DialogUtility
                        .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                                "Transmitter Language - Language",
                                "Language is a required field. Please select a Language.");
                return false;
            }
        }

        /*
         * verify that a voice has been selected.
         */
        if (this.selectedVoice == null) {
            DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                    "Transmitter Language - Voice",
                    "Voice is a required field. Please select a Voice.");
            return false;
        }

        if ((this.newStaticMessageTypes != null)
                && (this.newStaticMessageTypes.isEmpty() == false)) {
            /*
             * determine which message types will actually be successfully
             * scheduled based on the selected program.
             */
            StringBuilder sb = new StringBuilder();
            // has a program been selected.
            if (this.selectedProgram == null) {
                /*
                 * Unable to determine if any of the message types will actually
                 * be scheduled because no program has been selected yet.
                 */
                sb.append("No program has been selected. Unable to determine if the following static message types will be successfully scheduled:");
                for (StaticMessageType staticMsgType : this.newStaticMessageTypes) {
                    sb.append("\n").append(
                            staticMsgType.getMsgTypeSummary().getAfosid());
                }
            } else {
                /*
                 * Determine which static message types are recognized by the
                 * program.
                 */
                List<String> unrecognizedMsgTypes = new ArrayList<>(
                        this.newStaticMessageTypes.size());
                try {
                    List<String> staticAfosIds = this.pdm
                            .getStaticAfosIdsForProgram(this.selectedProgram);
                    for (StaticMessageType smt : this.newStaticMessageTypes) {
                        if (staticAfosIds.contains(smt.getMsgTypeSummary()
                                .getAfosid()) == false) {
                            unrecognizedMsgTypes.add(smt.getMsgTypeSummary()
                                    .getAfosid());
                        }
                    }

                    if (unrecognizedMsgTypes.isEmpty() == false) {
                        sb.append("The following static message types will not be scheduled because they are not part of the selected program:");
                        for (String umt : unrecognizedMsgTypes) {
                            sb.append("\n").append(umt);
                        }
                    }
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to determine the static message type(s) associated with Program: "
                                    + this.selectedProgram.getName() + ".", e);
                    return false;
                }
            }

            String msg = sb.toString();
            if (msg.isEmpty() == false) {
                /*
                 * This is just to notify the user. However, it will not
                 * actually prevent them from creating a Transmitter Language.
                 */
                DialogUtility.showMessageBox(this.shell, SWT.ICON_WARNING
                        | SWT.OK,
                        "Transmitter Language - Static Message Types", msg);
            }
        }

        return true;
    }

    private void handleSaveAction() {
        if (this.validate() == false) {
            return;
        }

        /*
         * Determine if we are creating a new transmitter language or updating
         * an existing transmitter language.
         */
        if (this.transmitterLanguage == null) {
            /*
             * new transmitter language. set the fields that would never change
             * on an existing transmitter language.
             */
            this.transmitterLanguage = new TransmitterLanguage();
            TransmitterLanguagePK pk = new TransmitterLanguagePK();
            pk.setTransmitterGroup(this.transmitterGroup);
            pk.setLanguage(this.selectedLanguage);
            this.transmitterLanguage.setId(pk);
            if (this.newStaticMessageTypes != null) {
                this.transmitterLanguage
                        .setOrderedStaticMessageTypes(this.newStaticMessageTypes);
            }
        }

        /*
         * voice
         */
        this.transmitterLanguage.setVoice(this.selectedVoice);

        /*
         * dictionary
         */
        this.transmitterLanguage.setDictionary(this.selectedDictionary);

        /*
         * rate of speech.
         */
        this.transmitterLanguage.setSpeechRate(this.rateOfSpeechComp
                .getSelectedRateOfSpeech());

        setReturnValue(this.transmitterLanguage);
        close();
    }

    /**
     * Handles voice selections. Keeps the {@link RateOfSpeechComp} sample voice
     * in sync with the selected voice.
     */
    private void handleVoiceSelection() {
        if (SELECT_VOICE.equals(this.voiceCombo.getText())) {
            this.rateOfSpeechComp.setSampleVoice(-1, null);
            this.selectedVoice = null;
        } else {
            final int voiceId = this.voiceNameIdentifierMap.get(this.voiceCombo
                    .getText());

            this.rateOfSpeechComp
                    .setSampleVoice(voiceId, this.selectedLanguage);
            // do we need to retrieve a voice?
            if ((this.selectedVoice != null)
                    && (this.selectedVoice.getVoiceNumber() == voiceId)) {
                return;
            }
            // retrieve the voice.
            try {
                this.selectedVoice = this.vdm.getVoiceById(voiceId);
            } catch (Exception e) {
                statusHandler.error(
                        "Failed to retrieve the voice associated with id: "
                                + voiceId + ".", e);
                this.selectedVoice = null;
            }

            this.addMsgTypeButton.setEnabled(this.selectedVoice != null);
        }
    }
}