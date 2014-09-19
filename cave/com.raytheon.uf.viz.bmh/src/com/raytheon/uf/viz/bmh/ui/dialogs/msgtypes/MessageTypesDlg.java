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
import java.util.Map;
import java.util.Set;

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
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.CreateEditMsgTypesDlg.DialogType;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;
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
 * Aug 8, 2014   #3490     lvenable    Updated populate table method call.
 * Aug 12, 2014  #3490     lvenable    Added relationship code and convenience methods.
 * Aug 15, 2014  #3490     lvenable    Sort the list of message types.
 * Aug 18, 2014   3411     mpduff      Add validation.
 * Aug 22, 2014   3411     lvenable    Update rename and made some tweaks.
 * Sep 18, 2014  #3587     bkowal      Notify a user when a message type is a trigger
 *                                     before they are allowed to remove it.
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
    private final List<Control> msgTypeControls = new ArrayList<Control>();

    /** Message Data Type Manager */
    private final MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

    // protected InputTextDlg inputDlg;

    private Set<String> existingNames = new HashSet<String>();

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     */
    public MessageTypesDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Message Types Dialog", parentShell, SWT.DIALOG_TRIM
                | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                | CAVE.MODE_INDEPENDENT);
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
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Message Type Manager");

        retrieveDataFromDB();

        createMessageTypesGroup();
        createBottomActionButtons();

        populateMessageTypeTable(false);
    }

    /**
     * Create the message types group.
     */
    private void createMessageTypesGroup() {
        Group msgAvailableGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        msgAvailableGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
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
                        DialogType.CREATE, messageTypeList);
                cemd.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue == null) {
                            return;
                        }
                        MessageType mt = (MessageType) returnValue;
                        messageTypeList.add(mt);
                        Collections.sort(messageTypeList,
                                new MsgTypeAfosComparator());
                        populateMessageTypeTable(true);
                    }
                });
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

                MessgaeTypeAfosValidator mtValidator = new MessgaeTypeAfosValidator(
                        existingNames);

                InputTextDlg inputDlg = new InputTextDlg(shell,
                        "Rename Message Type",
                        "Type in a new message type name:", mtValidator, true);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            String name = (String) returnValue;
                            renameMessageType(name);
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
                        DialogType.EDIT, messageTypeList,
                        getSelectedMessageType());
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
                handleDeleteMessageType();
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
                int selectedIndex = msgAvailTableComp.getSelectedIndex();

                if (selectedIndex < 0) {
                    return;
                }

                MessageType msgType = messageTypeList.get(selectedIndex);
                ViewMessageTypeDlg viewMessageTypeInfo = new ViewMessageTypeDlg(
                        shell, msgType);
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
        if (isDisposed()) {
            return true;
        }

        return false;
    }

    /**
     * Retrieve the data from the database.
     */
    private void retrieveDataFromDB() {

        try {
            messageTypeList = msgTypeDataMgr
                    .getMessageTypes(new MsgTypeAfosComparator());
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);
        }

        existingNames.clear();

        for (MessageType mt : messageTypeList) {
            existingNames.add(mt.getAfosid());
        }
    }

    /**
     * Get the message type selected in the table.
     * 
     * @return The message type.
     */
    private MessageType getSelectedMessageType() {
        if (msgAvailTableComp.getSelectedIndex() >= 0) {
            return messageTypeList.get(msgAvailTableComp.getSelectedIndex());
        }

        return null;
    }

    /**
     * Handle deleting the message type.
     */
    private void handleDeleteMessageType() {

        int index = msgAvailTableComp.getSelectedIndex();
        if (index < 0) {
            return;
        }
        MessageType mt = messageTypeList.get(index);

        /*
         * Determine if the Message Type is associated with a trigger for any
         * programs.
         */
        ProgramDataManager pdm = new ProgramDataManager();
        List<Program> triggeredPrograms = null;
        try {
            triggeredPrograms = pdm.getProgramsWithTrigger(mt);
        } catch (Exception e) {
            statusHandler.error("Error checking for programs with trigger: '"
                    + mt.getAfosid() + "'!", e);
        }

        if (triggeredPrograms == null || triggeredPrograms.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Do you wish to delete message type ")
                    .append(mt.getAfosid()).append("?");
            int result = DialogUtility.showMessageBox(getParent().getShell(),
                    SWT.ICON_WARNING | SWT.OK | SWT.CANCEL, "Confirm Delete",
                    sb.toString());

            if (result == SWT.CANCEL) {
                return;
            }
        } else {
            StringBuilder sb = new StringBuilder("Message Type ");
            sb.append(mt.getAfosid());
            sb.append(" is currently a Trigger for the following programs:\n\n");
            for (Program triggeredProgram : triggeredPrograms) {
                sb.append(triggeredProgram.getName());
                sb.append("\n");
            }
            sb.append("\nAre you sure you want to delete message type ");
            sb.append(mt.getAfosid());
            sb.append("?");

            int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.YES | SWT.NO, "Remove Message Type", sb.toString());
            if (result == SWT.NO) {
                /*
                 * Do not remove anything.
                 */
                return;
            }
        }

        try {
            msgTypeDataMgr.deleteMessageType(mt);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to remove message type " + mt.getAfosid()
                            + " from the database!", e);
        }

        retrieveDataFromDB();
        populateMessageTypeTable(true);
    }

    private void renameMessageType(String newName) {
        int index = msgAvailTableComp.getSelectedIndex();
        if (index < 0) {
            return;
        }
        MessageType mt = messageTypeList.get(index);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Do you wish to rename message type ")
                    .append(mt.getAfosid()).append(" to ").append(newName)
                    .append("?");
            int result = DialogUtility.showMessageBox(getParent().getShell(),
                    SWT.ICON_WARNING | SWT.OK | SWT.CANCEL, "Confirm Rename",
                    sb.toString());

            if (result == SWT.CANCEL) {
                return;
            }

            mt.setAfosid(newName);
            msgTypeDataMgr.saveMessageType(mt);
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);
        }

        retrieveDataFromDB();
        populateMessageTypeTable(true);
    }

    private boolean isValid(String newName) {
        boolean valid = MessageTypeUtils.validateAfosId(newName);
        if (valid) {
            valid = MessageTypeUtils.isUnique(newName, messageTypeList);
            if (!valid) {
                String message = "Invalid name/AfosID.\n\n" + newName
                        + " is already being used.\n\n" + "Enter another name";

                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                        "Invalid Name", message);
                return false;

            }
        } else {
            String message = "Invalid name/AfosID.\n\nMust be 7-9 alphanumeric characters "
                    + "with no spaces or special characters.";
            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                    "Invalid Name", message);
            return false;
        }

        return true;
    }

    /**
     * Populate the Message Type table.
     * 
     * @param replaceTableItems
     *            True to replace the existing items in the table, false to
     *            completely rebuild the table.
     */
    private void populateMessageTypeTable(boolean replaceTableItems) {
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

        if (replaceTableItems) {
            msgAvailTableComp.replaceTableItems(messageTypeTableData);
        } else {
            msgAvailTableComp.populateTable(messageTypeTableData);
        }

        if (msgAvailTableComp.getItemCount() > 0) {
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
