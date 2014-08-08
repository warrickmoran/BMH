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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest.MessageTypeAction;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
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
 * Jul 27, 2014  #3420     lvenable    Initial creation
 * Aug 03, 2014  #3479     lvenable    Updated code for validator changes.
 * Aug 5, 2014   #3490     lvenable    Updated to populate table.
 * Aug 8, 2014    #3490     lvenable    Updated populate table method call.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageTypesDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageTypesDlg.class);

    /** Table containing the message types that are available. */
    private MsgTypeTable msgAvailTableComp;

    /** Relationship image. */
    private Image relationshipImg;

    /** List of message types. */
    private List<MessageType> messageTypeList;

    /** Message type table data. */
    private TableData messageTypeTableData;

    /** Array of Message Type controls.. */
    private List<Control> msgTypeControls = new ArrayList<Control>();

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     */
    public MessageTypesDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Message Types Dialog", parentShell, SWT.DIALOG_TRIM
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

        retrieveDataFromDB();

        createMessageTypesGroup();
        createBottomActionButtons();

        populateMessageTypeTable();
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
        Button renameBtn = new Button(buttonComp, SWT.PUSH);
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
        msgTypeControls.add(renameBtn);

        gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gd.widthHint = buttonWidth;
        Button editBtn = new Button(buttonComp, SWT.PUSH);
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
        msgTypeControls.add(editBtn);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        gd.widthHint = buttonWidth;
        Button deleteBtn = new Button(buttonComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.setLayoutData(gd);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        msgTypeControls.add(deleteBtn);

        /*
         * Relationship button
         */
        ImageDescriptor id;
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/Relationship.png");
        relationshipImg = id.createImage();

        Button relationshipBtn = new Button(buttonComp, SWT.PUSH);
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
        msgTypeControls.add(relationshipBtn);
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
        for (Control ctrl : msgTypeControls) {
            ctrl.setEnabled(enable);
        }
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
     * Retrieve the data from the database.
     */
    private void retrieveDataFromDB() {
        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.AllMessageTypes);
        MessageTypeResponse mtResponse = null;

        try {
            mtResponse = (MessageTypeResponse) BmhUtils.sendRequest(mtRequest);
            messageTypeList = mtResponse.getMessageTypeList();
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);
        }
    }

    /**
     * Populate the Message Type table.
     */
    private void populateMessageTypeTable() {
        if (msgAvailTableComp.hasTableData() == false) {
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
        msgAvailTableComp.populateTable(messageTypeTableData);

        if (msgAvailTableComp.getItemCount() > 0) {
            msgAvailTableComp.select(0);
            enableControls(true);
        } else {
            enableControls(false);
        }
    }

    /**
     * Populate the Message Type table data.
     */
    private void populateMessageTypeTableData() {
        for (MessageType mt : messageTypeList) {
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            messageTypeTableData.addDataRow(trd);
        }
    }
}
