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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.request.InputMessageRequest;
import com.raytheon.uf.common.bmh.request.InputMessageRequest.InputMessageAction;
import com.raytheon.uf.common.bmh.request.InputMessageResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to select input messages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014  #3728     lvenable     Initial creation
 * Oct 18, 2014  #3728     lvenable     Hooked into database and returns the selected
 *                                      input message.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SelectInputMsgDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectMessageTypeDlg.class);

    /** Table containing the emergency message types. */
    private GenericTable inputMsgTable;

    /** OK button. */
    private Button okBtn;

    /** Message Type table data. */
    private TableData inputMsgTableData = null;

    /** List of all the input messages. */
    private List<InputMessage> inputMessageList = null;

    /**
     * 
     * @param parentShell
     */
    public SelectInputMsgDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

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
        setText("Select Input Message");

        createInputMsgTable();
        createOkCancelButtons();

        retrieveInputMessages();
        populateInputMsgTable();

        if (inputMsgTable.getItemCount() > 0) {
            okBtn.setEnabled(true);
        }
    }

    /**
     * Create the input message table.
     */
    private void createInputMsgTable() {
        Group inputTableGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        inputTableGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        inputTableGroup.setLayoutData(gd);
        inputTableGroup.setText(" Input Messages: ");

        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;

        inputMsgTable = new GenericTable(inputTableGroup, tableStyle, 650, 250);

        inputMsgTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                okBtn.setEnabled(selectionCount > 0);
            }
        });
    }

    /**
     * Create the OK and Cancel buttons.
     */
    private void createOkCancelButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 80;
        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
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

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
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
        int index = inputMsgTable.getSelectedIndex();

        if (index < 0) {
            return;
        }
        InputMessage im = inputMessageList.get(index);

        InputMessage fullInputMessage = null;

        try {
            fullInputMessage = getInputMessageById(im.getId());

            if (fullInputMessage == null) {
                StringBuilder msg = new StringBuilder();

                msg.append("No data retrieved for: ");
                msg.append(im.getName()).append("\n\n");
                msg.append("Click OK to close the selection dialog or Cancel to go back and try again.");

                int choice = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                        "No Data Returned", msg.toString());

                if (choice == SWT.CANCEL) {
                    return;
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving input message from the database: ", e);

            StringBuilder msg = new StringBuilder();

            msg.append("Error retrieving: ");
            msg.append(im.getName()).append("\n\n");
            msg.append("Click OK to close the selection dialog or Cancel to go back and try again.");

            int choice = DialogUtility.showMessageBox(getShell(),
                    SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                    "Date Retrieve Error", msg.toString());

            if (choice == SWT.CANCEL) {
                return;
            }
        }

        setReturnValue(fullInputMessage);

        close();
    }

    /**
     * Populate the message type table.
     */
    private void populateInputMsgTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Name", 250);
        columnNames.add(tcd);
        tcd = new TableColumnData("AFOS ID", 100);
        columnNames.add(tcd);
        tcd = new TableColumnData("Creation Date");
        columnNames.add(tcd);

        inputMsgTableData = new TableData(columnNames);
        populateInputMsgTableData();
        inputMsgTable.populateTable(inputMsgTableData);
    }

    /**
     * Populate the message type table data.
     */
    private void populateInputMsgTableData() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        for (InputMessage im : inputMessageList) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(im.getName()));
            trd.addTableCellData(new TableCellData(im.getAfosid()));
            trd.addTableCellData(new TableCellData(dateFmt.format(im
                    .getCreationTime().getTime())));

            inputMsgTableData.addDataRow(trd);
        }
    }

    /**
     * Retrieve the Input Messages from the database with only the ID, Name,
     * Afos Id, and Creation times populate.
     */
    private void retrieveInputMessages() {

        InputMessageRequest imRequest = new InputMessageRequest();
        imRequest.setAction(InputMessageAction.ListIdNameAfosCreation);
        InputMessageResponse imResponse = null;

        try {
            imResponse = (InputMessageResponse) BmhUtils.sendRequest(imRequest);
            inputMessageList = imResponse.getInputMessageList();

            if (inputMessageList == null) {
                inputMessageList = Collections.emptyList();
            }

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving input messages from the database: ", e);
            return;
        }
    }

    /**
     * Get the Input Message from the database using the primary key Id.
     * 
     * @param id
     *            Primary Key Id.
     * @return The input message.
     */
    private InputMessage getInputMessageById(int id) throws Exception {
        InputMessageRequest imRequest = new InputMessageRequest();
        imRequest.setAction(InputMessageAction.GetByPkId);
        imRequest.setPkId(id);

        InputMessageResponse imResponse = (InputMessageResponse) BmhUtils
                .sendRequest(imRequest);
        inputMessageList = imResponse.getInputMessageList();

        if (inputMessageList == null || inputMessageList.isEmpty()) {
            return null;
        }

        return inputMessageList.get(0);
    }
}