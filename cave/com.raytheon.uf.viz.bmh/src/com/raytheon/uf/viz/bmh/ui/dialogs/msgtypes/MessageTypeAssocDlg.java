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

import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;

/**
 * Dialog that displays the message types associations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2014  #3330     lvenable     Initial creation
 * Jul 15, 2014  #3387     lvenable     Implemented abstract BMH dialog.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageTypeAssocDlg extends AbstractBMHDialog {

    /** Message Types combo box. */
    private Combo msgTypeCbo;

    /** Label displaying the title for the selected message type. */
    private Label msgTitleLbl;

    /** Group containing the table of message type to be replaced. */
    private Group msgReplaceGrp;

    /** Table containing the selected message types to be replaced. */
    private MsgTypeTable msgReplaceTableComp;

    /** Prefix text for the replacement group title. */
    private final String msgReplaceGrpPrefix = " Message Types replaced by: ";

    /** Button to add message types. */
    private Button addMsgTypesBtn;

    /** Button to remove message types. */
    private Button removeMsgTypesBtn;

    /** Save button. */
    private Button saveBtn;

    /** Table containing the message types that are available. */
    private MsgTypeTable msgAvailTableComp;

    /** Dialog Title. */
    private static final String dialogTitle = "Message Type Association";

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public MessageTypeAssocDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, dialogTitle, parentShell, SWT.DIALOG_TRIM,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);
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
    protected void initializeComponents(Shell shell) {
        setText(dialogTitle);

        createMessageTypeControls();
        createMsgReplaceGroup();
        createAddRemoveButtons();
        createAvailMsgGroup();
        createBottomButtons();

        handleMssageTypeChange();
    }

    /**
     * Create the message type controls.
     */
    private void createMessageTypeControls() {
        Composite msgTypeComp = new Composite(shell, SWT.NONE);
        msgTypeComp.setLayout(new GridLayout(4, false));
        msgTypeComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Label msgTypeLbl = new Label(msgTypeComp, SWT.NONE);
        msgTypeLbl.setText("Message Type: ");

        GridData gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 200;
        msgTypeCbo = new Combo(msgTypeComp, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        msgTypeCbo.setLayoutData(gd);
        msgTypeCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMssageTypeChange();
            }
        });

        populateCombo();

        gd = new GridData();
        gd.horizontalIndent = 20;
        Label titleLbl = new Label(msgTypeComp, SWT.NONE);
        titleLbl.setText("Title: ");
        titleLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        msgTitleLbl = new Label(msgTypeComp, SWT.NONE);
        msgTitleLbl.setLayoutData(gd);
        msgTitleLbl.setText("Message Title goes here...");
    }

    /**
     * Create the replace message types group and table control.
     */
    private void createMsgReplaceGroup() {
        msgReplaceGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        msgReplaceGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgReplaceGrp.setLayoutData(gd);
        msgReplaceGrp.setText(msgReplaceGrpPrefix);

        msgReplaceTableComp = new MsgTypeTable(msgReplaceGrp, 550, 100);

        populateMsgTypeReplaceTable();

        msgReplaceTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    removeMsgTypesBtn.setEnabled(true);
                } else {
                    removeMsgTypesBtn.setEnabled(false);
                }
            }
        });
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

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        removeMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        removeMsgTypesBtn.setImage(udi.getImage(Arrows.DOWN));
        removeMsgTypesBtn.setText("Remove");
        removeMsgTypesBtn.setToolTipText("Remove selected message types");
        removeMsgTypesBtn.setLayoutData(gd);
        removeMsgTypesBtn.setEnabled(false);
    }

    /**
     * Create the available message types group and table control.
     */
    private void createAvailMsgGroup() {
        Group availMsgGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        availMsgGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        availMsgGrp.setLayoutData(gd);
        availMsgGrp.setText(" Available Message Types: ");

        List<String> columnNames = new ArrayList<String>();
        columnNames.add("Message Type");
        columnNames.add("Message Title");
        msgAvailTableComp = new MsgTypeTable(availMsgGrp, 550, 100);

        populateMsgTypeAvailTable();

        msgAvailTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    addMsgTypesBtn.setEnabled(true);
                } else {
                    addMsgTypesBtn.setEnabled(false);
                }
            }
        });
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
                close();
            }
        });

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
     * Action performed when the message type changes.
     */
    private void handleMssageTypeChange() {
        msgTitleLbl.setText(msgTypeCbo.getItem(msgTypeCbo.getSelectionIndex())
                + " (Title)");
        msgReplaceGrp.setText(msgReplaceGrpPrefix + " "
                + msgTypeCbo.getItem(msgTypeCbo.getSelectionIndex()));
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

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    private void populateCombo() {
        for (int i = 0; i < 20; i++) {
            msgTypeCbo.add("MessageType:" + i);
        }
        msgTypeCbo.select(0);
    }

    private void populateMsgTypeReplaceTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 1"));
        trd.addTableCellData(new TableCellData("MessageType - 1 - Description"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 2"));
        trd.addTableCellData(new TableCellData("MessageType - 2 - Description"));

        td.addDataRow(trd);
        msgReplaceTableComp.populateTable(td);

    }

    private void populateMsgTypeAvailTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 3"));
        trd.addTableCellData(new TableCellData("MessageType - 3 - Description"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 4"));
        trd.addTableCellData(new TableCellData("MessageType - 4 - Description"));

        td.addDataRow(trd);

        msgAvailTableComp.populateTable(td);
    }
}
