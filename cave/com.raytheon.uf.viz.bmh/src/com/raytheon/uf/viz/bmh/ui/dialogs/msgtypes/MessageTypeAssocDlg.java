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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Dialog that displays the message types associations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2014  #3330     lvenable    Initial creation
 * Jul 15, 2014  #3387     lvenable    Implemented abstract BMH dialog.
 * Aug 15, 2014   3411     mpduff      populated.
 * Sep 11, 2014   3355     mpduff      Post demo cleanup (change label)
 * Oct 08, 2014  #3479     lvenable    Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  3654      rjpeter     Updated to use MessageTypeSummary.
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Nov 18, 2014  3746      rjpeter     Refactored MessageTypeReplacement.
 * Mar 24, 2015  4306      rferrel     No longer have default selected message type.
 * Jun 05, 2015  4490      rjpeter     Updated constructor.
 * Jul 22, 2015  4676      bkowal      Ensure that message types that have already been selected
 *                                     are not added to the available list.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageTypeAssocDlg extends AbstractBMHDialog {
    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageTypeAssocDlg.class);

    private final String[] COLUMN_NAMES = new String[] { "Message Type",
            "Message Title" };

    /** Selected Message Type Label. */
    private Label msgTypeSelectedLbl;

    /** Label displaying the title for the selected message type. */
    private Label msgTitleLbl;

    /** Group containing the table of message type to be replaced. */
    private Group msgReplaceGrp;

    /** Table containing the selected message types to be replaced. */
    private MsgTypeTable msgReplaceTableComp;

    /** Prefix text for the replacement group title. */
    private final String msgReplaceGrpTextPrefix = " Message Type ";

    /** Suffix text for the replacement group title. */
    private final String msgReplaceGrpTextSuffix = " replaces the following: ";

    /** Button to add message types. */
    private Button addMsgTypesBtn;

    /** Button to remove message types. */
    private Button removeMsgTypesBtn;

    /** Save button. */
    private Button saveBtn;

    /** Table containing the message types that are available. */
    private MsgTypeTable msgAvailTableComp;

    private MessageTypeDataManager dataManager;

    private List<MessageType> messageTypeList;

    private TableData availableMessageTableData;

    private TableData selectedMessageTableData;

    private MessageType selectedMessageType;

    private List<String> selectedAfosIds;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public MessageTypeAssocDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                | CAVE.PERSPECTIVE_INDEPENDENT);
        setText(DlgInfo.MESSAGE_TYPE_ASSOCIATION.getTitle());
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
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createMessageTypeControls();
        createMsgReplaceGroup();
        createAddRemoveButtons();
        createAvailMsgGroup();
        createBottomButtons();

        loadData();
    }

    /**
     * Create the message type controls.
     */
    private void createMessageTypeControls() {
        Composite msgTypeComp = new Composite(shell, SWT.NONE);
        msgTypeComp.setLayout(new GridLayout(5, false));
        msgTypeComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Button selectBtn = new Button(msgTypeComp, SWT.PUSH);
        selectBtn.setText(" Select... ");
        selectBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSelection();
            }
        });

        Label msgTypeLbl = new Label(msgTypeComp, SWT.NONE);
        msgTypeLbl.setText("   Message Type:");

        GridData gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 115;
        msgTypeSelectedLbl = new Label(msgTypeComp, SWT.NONE);
        msgTypeSelectedLbl.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = 10;
        Label titleLbl = new Label(msgTypeComp, SWT.NONE);
        titleLbl.setText("Title:");
        titleLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        msgTitleLbl = new Label(msgTypeComp, SWT.NONE);
        msgTitleLbl.setLayoutData(gd);
    }

    /**
     * Create the replace message types group and table control.
     */
    private void createMsgReplaceGroup() {
        msgReplaceGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        msgReplaceGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        msgReplaceGrp.setLayoutData(gd);
        msgReplaceGrp.setText(msgReplaceGrpTextPrefix);

        msgReplaceTableComp = new MsgTypeTable(msgReplaceGrp, 550, 100);
        msgReplaceTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectedMessageType != null && selectionCount > 0) {
                    removeMsgTypesBtn.setEnabled(true);
                } else {
                    removeMsgTypesBtn.setEnabled(false);
                }
            }
        });

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData(COLUMN_NAMES[0], 150);
        columnNames.add(tcd);
        tcd = new TableColumnData(COLUMN_NAMES[1]);
        columnNames.add(tcd);
        selectedMessageTableData = new TableData(columnNames);
    }

    /**
     * Create the add/remove buttons.
     */
    private void createAddRemoveButtons() {
        UpDownImages udi = new UpDownImages(shell);

        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(new GridLayout(2, false));
        btnComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        int buttonWidth = 80;
        GridData gd;

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        addMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        addMsgTypesBtn.setImage(udi.getImage(Arrows.UP));
        addMsgTypesBtn.setText("Add");
        addMsgTypesBtn.setToolTipText("Add selected message types");
        addMsgTypesBtn.setLayoutData(gd);
        addMsgTypesBtn.setEnabled(false);
        addMsgTypesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelectedMessage();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        removeMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        removeMsgTypesBtn.setImage(udi.getImage(Arrows.DOWN));
        removeMsgTypesBtn.setText("Remove");
        removeMsgTypesBtn.setToolTipText("Remove selected message types");
        removeMsgTypesBtn.setLayoutData(gd);
        removeMsgTypesBtn.setEnabled(false);
        removeMsgTypesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedMessage();
            }
        });
    }

    /**
     * Create the available message types group and table control.
     */
    private void createAvailMsgGroup() {
        Group availMsgGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        availMsgGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        availMsgGrp.setLayoutData(gd);
        availMsgGrp.setText(" Available Message Types: ");

        msgAvailTableComp = new MsgTypeTable(availMsgGrp, 550, 100);
        msgAvailTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectedMessageType != null && selectionCount > 0) {
                    addMsgTypesBtn.setEnabled(true);
                } else {
                    addMsgTypesBtn.setEnabled(false);
                }
            }
        });

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData(COLUMN_NAMES[0], 150);
        columnNames.add(tcd);
        tcd = new TableColumnData(COLUMN_NAMES[1]);
        columnNames.add(tcd);
        availableMessageTableData = new TableData(columnNames);
    }

    /**
     * Create the bottom Save and Cancel buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(3, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        saveBtn = new Button(buttonComp, SWT.PUSH);
        saveBtn.setText(" Save ");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (save()) {
                    close();
                }
            }
        });
        saveBtn.setEnabled(false);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText(" Cancel ");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Open the MessageType selection dialog
     */
    private void handleSelection() {
        MessageTypeSelectionDlg dlg = new MessageTypeSelectionDlg(getShell(),
                messageTypeList, selectedMessageType);
        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                MessageType mt = (MessageType) returnValue;
                if (mt != null) {
                    selectedMessageType = mt;
                    populateSelection();
                    populateMsgTypeAvailTable();
                }
            }
        });

        dlg.open();
    }

    private void populateSelection() {
        msgTypeSelectedLbl.setText(selectedMessageType.getAfosid());
        msgTitleLbl.setText(selectedMessageType.getTitle());
        msgReplaceGrp.setText(msgReplaceGrpTextPrefix
                + selectedMessageType.getAfosid() + msgReplaceGrpTextSuffix);

        selectedMessageTableData.deleteAllRows();
        this.selectedAfosIds = new ArrayList<>(selectedMessageType
                .getReplacementMsgs().size());
        for (MessageTypeSummary replace : selectedMessageType
                .getReplacementMsgs()) {
            TableRowData row = new TableRowData();
            row.addTableCellData(new TableCellData(replace.getAfosid()));
            row.addTableCellData(new TableCellData(replace.getTitle()));
            row.setData(replace);
            selectedMessageTableData.addDataRow(row);
            this.selectedAfosIds.add(replace.getAfosid());
        }

        msgReplaceTableComp.populateTable(selectedMessageTableData);
        saveBtn.setEnabled(true);
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
     * Add the selected message to the selected message table
     */
    private void addSelectedMessage() {
        List<TableRowData> selectedList = msgAvailTableComp.getSelection();
        for (TableRowData trd : selectedList) {
            selectedMessageTableData.addDataRow(trd);
            availableMessageTableData.deleteRow(trd);
        }

        msgReplaceTableComp.populateTable(selectedMessageTableData);
        this.msgAvailTableComp.populateTable(this.availableMessageTableData);
    }

    /**
     * Remove the selected message from the selected table
     */
    private void removeSelectedMessage() {
        List<TableRowData> selectedList = msgReplaceTableComp.getSelection();
        for (TableRowData trd : selectedList) {
            selectedMessageTableData.deleteRow(trd);
            availableMessageTableData.addDataRow(trd);
        }

        msgReplaceTableComp.populateTable(selectedMessageTableData);
        this.msgAvailTableComp.populateTable(this.availableMessageTableData);
    }

    /**
     * Populate the available message table
     */
    private void populateMsgTypeAvailTable() {
        availableMessageTableData.deleteAllRows();
        for (MessageType mt : messageTypeList) {
            if (this.selectedMessageType.getAfosid().equals(mt.getAfosid())
                    || this.selectedAfosIds.contains(mt.getAfosid())) {
                continue;
            }
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            trd.setData(mt.getSummary());
            availableMessageTableData.addDataRow(trd);
        }

        msgAvailTableComp.populateTable(availableMessageTableData);
    }

    /**
     * Load the data that backs this dialog
     */
    private void loadData() {
        dataManager = new MessageTypeDataManager();
        try {
            messageTypeList = dataManager
                    .getMessageTypes(new MsgTypeAfosComparator());
        } catch (Exception e) {
            statusHandler.error("Error getting MessageType data.", e);
            messageTypeList = Collections.emptyList();
        }
    }

    /**
     * Save the data.
     * 
     * @return true if successful
     */
    private boolean save() {
        List<TableRowData> rows = selectedMessageTableData.getTableRows();
        Set<MessageTypeSummary> set = new HashSet<>();
        for (TableRowData trd : rows) {
            set.add(((MessageTypeSummary) trd.getData()));
        }

        selectedMessageType.setReplacementMsgs(set);

        try {
            selectedMessageType = dataManager
                    .saveMessageType(selectedMessageType);
        } catch (Exception e) {
            statusHandler.error("Error saving MessageType "
                    + selectedMessageType.getAfosid(), e);
            return false;
        }
        return true;
    }
}
