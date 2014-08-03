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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.CreateEditMsgTypesDlg.DialogType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Main dialog to manage the message types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 27, 2014  #3420     lvenable     Initial creation
 * Aug 03, 2014  #3479      lvenable    Updated code for validator changes.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageTypesDlg extends AbstractBMHDialog {

    /** Table containing the message types that are available. */
    private MsgTypeTable msgAvailTableComp;

    /** Button to delete the selected message types. */
    private Button deleteBtn;

    /** Button to edit the selected message type. */
    private Button editBtn;

    /** Button to rename the selected message type. */
    private Button renameBtn;

    /** Button to show the relationship between message types, suites, etc. */
    private Button relationshipBtn;

    /** Relationship image. */
    private Image relationshipImg;

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     */
    public MessageTypesDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "MEssage Types Dialog", parentShell, SWT.DIALOG_TRIM
                | SWT.MIN, CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);
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
        relationshipImg.dispose();
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Message Type Manager");

        createMessageTypesGroup();
        createBottomActionButtons();
    }

    /**
     * Create the message types group.
     */
    private void createMessageTypesGroup() {
        Group msgAvailableGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        msgAvailableGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgAvailableGrp.setLayoutData(gd);
        msgAvailableGrp.setText(" All Message Types: ");

        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
        msgAvailTableComp = new MsgTypeTable(msgAvailableGrp, tableStyle, 600,
                200);

        populateMsgTypeReplaceTable();

        msgAvailTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                boolean enable = (selectionCount > 0) ? true : false;
                enableControls(enable);
            }
        });

        /*
         * Create the action buttons for the table.
         */
        Composite buttonComp = new Composite(msgAvailableGrp, SWT.NONE);
        gl = new GridLayout(5, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(gd);

        int buttonWidth = 70;

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.widthHint = buttonWidth;
        Button newBtn = new Button(buttonComp, SWT.PUSH);
        newBtn.setText("New...");
        newBtn.setLayoutData(gd);
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditMsgTypesDlg cemd = new CreateEditMsgTypesDlg(shell,
                        DialogType.CREATE);
                cemd.open();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gd.widthHint = buttonWidth;
        renameBtn = new Button(buttonComp, SWT.PUSH);
        renameBtn.setText("Rename...");
        renameBtn.setLayoutData(gd);
        renameBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputTextDlg inputDlg = new InputTextDlg(shell,
                        "Rename Message Type",
                        "Type in a new message type name:", null);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            String name = (String) returnValue;
                            System.out.println("Name = " + name);
                        }
                    }
                });
                inputDlg.open();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gd.widthHint = buttonWidth;
        editBtn = new Button(buttonComp, SWT.PUSH);
        editBtn.setText("Edit...");
        editBtn.setLayoutData(gd);
        editBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditMsgTypesDlg cemd = new CreateEditMsgTypesDlg(shell,
                        DialogType.EDIT);
                cemd.open();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        gd.widthHint = buttonWidth;
        deleteBtn = new Button(buttonComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.setLayoutData(gd);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        /*
         * Relationship button
         */
        ImageDescriptor id;
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/Relationship.png");
        relationshipImg = id.createImage();

        relationshipBtn = new Button(buttonComp, SWT.PUSH);
        relationshipBtn.setImage(relationshipImg);
        relationshipBtn.setToolTipText("View message type relationships");
        relationshipBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ViewMessageTypeDlg viewMessageTypeInfo = new ViewMessageTypeDlg(
                        shell);
                viewMessageTypeInfo.open();
            }
        });
    }

    /**
     * Create the bottom Close button.
     */
    private void createBottomActionButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Enable controls.
     * 
     * @param enable
     *            Flag to enable/disable the controls.
     */
    private void enableControls(boolean enable) {
        editBtn.setEnabled(enable);
        deleteBtn.setEnabled(enable);
        relationshipBtn.setEnabled(enable);
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
        msgAvailTableComp.populateTable(td);

    }
}
