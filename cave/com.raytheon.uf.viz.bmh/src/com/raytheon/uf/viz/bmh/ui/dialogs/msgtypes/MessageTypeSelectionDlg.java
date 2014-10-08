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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to select a {@link MessageType} from a list of all available
 * {@link MessageType}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014    3411    mpduff      Initial creation
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class MessageTypeSelectionDlg extends CaveSWTDialog {

    private final List<MessageType> messageTypeList;

    private MsgTypeTable tableComp;

    private TableData messageTypeTableData;

    private final MessageType selectedMessageType;

    private int selectedIndex = 0;

    public MessageTypeSelectionDlg(Shell parentShell,
            List<MessageType> messageTypeList, MessageType selectedMessageType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Select Message");
        this.messageTypeList = messageTypeList;
        this.selectedMessageType = selectedMessageType;
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
        tableComp = new MsgTypeTable(shell, tableStyle, 600, 200);

        populateMessageTypeTable(false);
    }

    /**
     * Populate the Message Type table.
     * 
     * @param replaceTableItems
     *            True to replace the existing items in the table, false to
     *            completely rebuild the table.
     */
    private void populateMessageTypeTable(boolean replaceTableItems) {
        if (tableComp.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Message Type", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Message Title");
            columnNames.add(tcd);
            messageTypeTableData = new TableData(columnNames);
        } else {
            messageTypeTableData.deleteAllRows();
        }

        populateMessageTypeTableData();

        if (replaceTableItems) {
            tableComp.replaceTableItems(messageTypeTableData);
        } else {
            tableComp.populateTable(messageTypeTableData);
        }

        tableComp.select(selectedIndex);

        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite c = new Composite(shell, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button okBtn = new Button(c, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<TableRowData> rowList = tableComp.getSelection();
                setReturnValue(rowList.get(0).getData());
                close();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(c, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Populate the Message Type table data.
     */
    private void populateMessageTypeTableData() {
        for (MessageType mt : messageTypeList) {
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            trd.setData(mt);
            messageTypeTableData.addDataRow(trd);
            if (selectedMessageType != null && selectedMessageType.equals(mt)) {
                selectedIndex = messageTypeTableData.getTableRows().size() - 1;
            }
        }
    }
}
