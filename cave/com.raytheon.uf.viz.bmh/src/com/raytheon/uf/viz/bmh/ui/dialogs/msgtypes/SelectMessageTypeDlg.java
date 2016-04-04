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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to select a message type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2014  #3610     lvenable     Initial creation
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  #3728     lvenable     Updated to return the fully populated message type object
 *                                      that was selected in the table.
 * Nov 10, 2014  #3381     bkowal       Updated to allow for single or multiple selection. Updated
 *                                      to use the SWT #getData and #setData.
 * Nov 13, 2014  #3803     bkowal       Added optional filtering capability.
 * Mar 12, 2015  #4213     bkowal       Slight refactoring to allow for extending.
 * Apr 04, 2016  #5504     bkowal       Fix GUI sizing issues.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SelectMessageTypeDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    protected final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /** Message type table listing all available message types. */
    private GenericTable msgTypeTable;

    /** Message Type table data. */
    private TableData msgTypeTableData = null;

    private final boolean allowMultipleSelection;

    /*
     * A list of message type id(s) to filter from the list - they will not be
     * displayed in the list. Primarily exists to ensure that components that
     * utilize this dialog will not have to check for duplicates in the message
     * type(s) that are returned by the dialog.
     */
    private Set<String> filteredMessageTypes;

    /**
     * OK button.
     */
    private Button okBtn;

    public SelectMessageTypeDlg(Shell parentShell) {
        this(parentShell, false);
    }

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public SelectMessageTypeDlg(Shell parentShell,
            boolean allowMultipleSelection) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        this.allowMultipleSelection = allowMultipleSelection;
        /*
         * Initialize to empty to eliminate the need for NULL checks.
         */
        this.filteredMessageTypes = Collections.emptySet();
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
        setText("Select Message Type");

        createMessageTypesTable();
        createOkCancelButtons();

        populateMsgTypeTable();
    }

    /**
     * Create message types table.
     */
    protected void createMessageTypesTable() {

        Label selectLbl = new Label(shell, SWT.NONE);
        selectLbl.setText("Select Message Type:");

        int tableSelection = this.allowMultipleSelection ? SWT.MULTI
                : SWT.SINGLE;

        msgTypeTable = new GenericTable(shell, SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL | tableSelection, 15);

        msgTypeTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                okBtn.setEnabled(selectionCount > 0);
            }
        });
    }

    /**
     * Create OK & Cancel buttons.
     */
    protected void createOkCancelButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        int buttonWidth = buttonComp.getDisplay().getDPI().x;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = buttonWidth;
        okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setEnabled(false);
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkAction();
            }
        });

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Handle the OK button action.
     */
    private void handleOkAction() {

        List<TableRowData> selectedTableRows = this.msgTypeTable.getSelection();

        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();
        List<MessageType> selectedMessageTypes = new ArrayList<>(
                selectedTableRows.size());
        for (TableRowData trd : selectedTableRows) {
            String afosId = trd.getData().toString();
            MessageType mt = null;
            try {
                // retrieve the latest version of the message type.
                mt = msgTypeDataMgr.getMessageType(afosId);
            } catch (Exception e) {
                statusHandler
                        .error("Error retrieving message type data from the database associated with afos id: "
                                + afosId, e);
                return;
            }
            selectedMessageTypes.add(mt);
        }

        if (this.allowMultipleSelection == false) {
            // support single selection mode.
            setReturnValue(selectedMessageTypes.get(0));
        } else {
            // support multiple selection mode.
            setReturnValue(selectedMessageTypes);
        }

        close();
    }

    /**
     * Populate the message type table.
     */
    protected void populateMsgTypeTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title", 250);
        columnNames.add(tcd);

        msgTypeTableData = new TableData(columnNames);
        populateMsgTypeTableData();
        msgTypeTable.populateTable(msgTypeTableData);

        okBtn.setEnabled(msgTypeTable.getItemCount() > 0);
        if (msgTypeTable.getItemCount() > 0) {
            this.msgTypeTable.select(0);
        }
    }

    protected void replaceMsgTypeTableDataRows(List<TableRowData> rows) {
        this.msgTypeTableData.deleteAllRows();

        for (TableRowData trd : rows) {
            final String afosid = (String) trd.getData();
            if (this.filteredMessageTypes.contains(afosid)) {
                continue;
            }

            this.msgTypeTableData.addDataRow(trd);
        }
        this.msgTypeTable.populateTable(this.msgTypeTableData);

        okBtn.setEnabled(msgTypeTable.getItemCount() > 0);
        if (msgTypeTable.getItemCount() > 0) {
            this.msgTypeTable.select(0);
        }
    }

    /**
     * Populate the message type table data.
     */
    private void populateMsgTypeTableData() {
        List<MessageType> messageTypeList = null;
        try {
            messageTypeList = this.retrieveMessageTypes();
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);
            return;
        }

        for (MessageType mt : messageTypeList) {
            if (this.filteredMessageTypes.contains(mt.getAfosid())) {
                continue;
            }

            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            trd.setData(mt.getAfosid());

            msgTypeTableData.addDataRow(trd);
        }
    }

    protected List<MessageType> retrieveMessageTypes() throws Exception {
        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

        return msgTypeDataMgr
                .getMsgTypesAfosIdTitle(new MsgTypeAfosComparator());
    }

    /**
     * @param filteredMessageTypes
     *            the filteredMessageTypes to set
     */
    public void setFilteredMessageTypes(List<String> filteredMessageTypes) {
        if (filteredMessageTypes == null || filteredMessageTypes.isEmpty()) {
            return;
        }
        this.filteredMessageTypes = new HashSet<>(filteredMessageTypes);
    }

    public void setFilteredMessageTypes(Set<String> filteredMessageTypes) {
        if (filteredMessageTypes == null || filteredMessageTypes.isEmpty()) {
            return;
        }
        this.filteredMessageTypes = filteredMessageTypes;
    }
}