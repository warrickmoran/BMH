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
import java.util.List;
import java.util.Map;

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

import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.LdadConfigNotification;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * LDAD Configuration Dialog. Allows the user to add, remove, and edit ldad
 * configurations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 10, 2014 3381       bkowal      Initial creation
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Nov 13, 2014  3803      bkowal      Implemented.
 * Nov 18, 2014  3807      bkowal      Use BMHJmsDestinations.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadConfigDlg extends AbstractBMHDialog implements
        INotificationObserver {

    private final static IUFStatusHandler statusHandler = UFStatus
            .getHandler(LdadConfigDlg.class);

    /**
     * Data manager
     */
    private final LdadConfigDataManager dataManager = new LdadConfigDataManager();

    private Group ldadConfigGroup;

    private GenericTable ldadConfigTable;

    private Button newButton;

    private Button editButton;

    private Button deleteButton;

    private static enum LDAD_CONFIG_COLUMNS {
        // Name Column (comments exist for readability after Java formatting)
        NAME("Name", 150),
        // Host Column
        HOST("Host", 150),
        // Directory Column
        DIRECTORY("Directory", 225),
        // Encoding Column
        ENCODING("Encoding", 20);

        private String columnText;

        private int columnWidth;

        private LDAD_CONFIG_COLUMNS(String columnText, int columnWidth) {
            this.columnText = columnText;
            this.columnWidth = columnWidth;
        }

        public String getColumnText() {
            return this.columnText;
        }

        public int getColumnWidth() {
            return this.columnWidth;
        }
    }

    public LdadConfigDlg(Map<AbstractBMHDialog, String> map, Shell parentShell) {
        super(map, DlgInfo.LDAD_CONFIGURATION.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
        super.setText(DlgInfo.LDAD_CONFIGURATION.getTitle());
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
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#opened()
     */
    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());

        this.populateDialog();

        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);
    }

    @Override
    protected void disposed() {
        super.disposed();

        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);
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
        this.createLdadConfigTable();
        this.createNewEditButtons();
        this.createBottomButton();
    }

    private void createLdadConfigTable() {
        ldadConfigGroup = new Group(this.shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        ldadConfigGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        ldadConfigGroup.setLayoutData(gd);

        ldadConfigTable = new GenericTable(ldadConfigGroup, SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE, 650, 150);
        ldadConfigTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                boolean enabled = (selectionCount == 1);

                editButton.setEnabled(enabled);
                deleteButton.setEnabled(enabled);
            }
        });
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(
                LDAD_CONFIG_COLUMNS.values().length);
        for (int i = 0; i < LDAD_CONFIG_COLUMNS.values().length; i++) {
            LDAD_CONFIG_COLUMNS colDef = LDAD_CONFIG_COLUMNS.values()[i];
            columnNames.add(new TableColumnData(colDef.getColumnText(), colDef
                    .getColumnWidth()));
        }

        TableData ldadConfigTableData = new TableData(columnNames);
        ldadConfigTable.populateTable(ldadConfigTableData);
    }

    private void createNewEditButtons() {
        Composite ldadConfigBtnsComposite = new Composite(this.ldadConfigGroup,
                SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        ldadConfigBtnsComposite.setLayout(gl);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        ldadConfigBtnsComposite.setLayoutData(gd);

        newButton = new Button(ldadConfigBtnsComposite, SWT.PUSH);
        newButton.setText("New...");
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 80;
        newButton.setLayoutData(gd);
        newButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleNewAction();
            }
        });

        editButton = new Button(ldadConfigBtnsComposite, SWT.PUSH);
        editButton.setText("Edit...");
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        gd.widthHint = 80;
        editButton.setLayoutData(gd);
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleEditAction();
            }
        });
        editButton.setEnabled(false);

        deleteButton = new Button(ldadConfigBtnsComposite, SWT.PUSH);
        deleteButton.setText("Delete...");
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 80;
        deleteButton.setLayoutData(gd);
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });
        deleteButton.setEnabled(false);
    }

    private void createBottomButton() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        Button closeButton = new Button(comp, SWT.PUSH);
        closeButton.setText("Close");
        gd = new GridData();
        gd.widthHint = 80;
        closeButton.setLayoutData(gd);
        closeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    private void populateDialog() {
        /*
         * Retrieve references to any existing ldad config records.
         */
        List<LdadConfig> existingLdadRecords = null;
        try {
            existingLdadRecords = this.dataManager
                    .getExistingConfigurationReferences();
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to retrieve the existing ldad config records.", e);
            return;
        }

        if (existingLdadRecords == null || existingLdadRecords.isEmpty()) {
            // no existing ldad configuration exists.
            return;
        }

        this.addRecordsToTable(existingLdadRecords);
    }

    private synchronized void addRecordToTable(LdadConfig record) {
        TableRowData trd = new TableRowData();
        trd.setData(record.getId());
        trd.addTableCellData(new TableCellData(record.getName()));
        trd.addTableCellData(new TableCellData(record.getHost()));
        trd.addTableCellData(new TableCellData(record.getDirectory()));
        trd.addTableCellData(new TableCellData(record.getEncoding()
                .getExtension()));
        this.ldadConfigTable.getTableData().addDataRow(trd);
    }

    private synchronized void addRecordsToTable(List<LdadConfig> records) {
        for (LdadConfig record : records) {
            this.addRecordToTable(record);
        }
        this.rebuildLdadTable();
    }

    private void rebuildLdadTable() {
        this.ldadConfigTable.populateTable(this.ldadConfigTable.getTableData());
        boolean enabled = this.ldadConfigTable.getSelection().isEmpty() == false;
        this.editButton.setEnabled(enabled);
        this.deleteButton.setEnabled(enabled);
    }

    private void handleNewAction() {
        CreateEditLdadConfigDlg createLdadConfigDlg = new CreateEditLdadConfigDlg(
                this.shell);
        this.handleCreateEditDlgClose(createLdadConfigDlg);
    }

    private void handleEditAction() {
        LdadConfig ldadConfig = this.getSelectedLdadConfig();
        if (ldadConfig == null) {
            // errors already handled and reported.
            return;
        }
        CreateEditLdadConfigDlg createLdadConfigDlg = new CreateEditLdadConfigDlg(
                this.shell, ldadConfig);
        this.handleCreateEditDlgClose(createLdadConfigDlg);
    }

    private void handleDeleteAction() {
        LdadConfig ldadConfig = this.getSelectedLdadConfig();
        if (ldadConfig == null) {
            // errors already handled and reported.
            return;
        }

        // confirm that the user wants to delete the ldad configuration.
        StringBuilder dialogMsg = new StringBuilder(
                "Are you sure you want to delete ldad configuration: ");
        dialogMsg.append(ldadConfig.getName());
        dialogMsg.append("?");
        int option = DialogUtility.showMessageBox(this.shell, SWT.ICON_QUESTION
                | SWT.YES | SWT.NO, "Ldad Config - Delete Config",
                dialogMsg.toString());
        if (option == SWT.NO) {
            return;
        }

        try {
            this.dataManager.deleteLdadConfig(ldadConfig);
        } catch (Exception e) {
            statusHandler.error("Failed to delete the " + ldadConfig.getName()
                    + " ldad configuration.", e);
            return;
        }

        this.ldadConfigTable.getTableData().deleteRow(
                this.ldadConfigTable.getSelection().get(0));
        this.rebuildLdadTable();
    }

    private void handleCreateEditDlgClose(
            CreateEditLdadConfigDlg createLdadConfigDlg) {
        createLdadConfigDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof LdadConfig == false) {
                    return;
                }

                handleSaveUpdateInternal((LdadConfig) returnValue);
            }
        });
        createLdadConfigDlg.open();
    }

    /**
     * Determines if a table row already exists for the specified record and
     * updates it when it does already exist.
     * 
     * @param ldadConfig
     *            the specified record.
     * @return true if the table was updated; false, otherwise
     */
    private synchronized boolean checkRecordUpdate(LdadConfig ldadConfig) {
        for (TableRowData trd : this.ldadConfigTable.getTableData()
                .getTableRows()) {
            if ((Long) trd.getData() == ldadConfig.getId() == false) {
                continue;
            }

            trd.getTableCellData().get(0).setCellText(ldadConfig.getName());
            trd.getTableCellData().get(1).setCellText(ldadConfig.getHost());
            trd.getTableCellData().get(2)
                    .setCellText(ldadConfig.getDirectory());
            trd.getTableCellData().get(3)
                    .setCellText(ldadConfig.getEncoding().getExtension());

            return true;
        }

        return false;
    }

    private void handleSaveUpdateInternal(LdadConfig ldadConfig) {
        if (checkRecordUpdate(ldadConfig) == false) {
            addRecordToTable(ldadConfig);
        }
        rebuildLdadTable();
    }

    private void handleRemoveInternal(final long id) {
        TableRowData trdToRemove = null;
        for (TableRowData trd : this.ldadConfigTable.getTableData()
                .getTableRows()) {
            if ((Long) trd.getData() == id) {
                trdToRemove = trd;
                break;
            }
        }

        if (trdToRemove != null) {
            this.ldadConfigTable.getTableData().deleteRow(trdToRemove);
            this.rebuildLdadTable();
        }
    }

    private LdadConfig getSelectedLdadConfig() {
        return this.getSpecifiedLdadConfig((Long) this.ldadConfigTable
                .getSelection().get(0).getData());
    }

    private LdadConfig getSpecifiedLdadConfig(long id) {
        /**
         * These retrievals could optionally be cached so that re-selections of
         * a particular record will not require re-retrieval?
         */
        LdadConfig ldadConfig = null;
        try {
            ldadConfig = this.dataManager.getLdadConfig(id);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to retrieve the ldad config record associated with id: "
                            + id + ".", e);
            return null;
        }

        if (ldadConfig == null) {
            statusHandler
                    .error("Failed to find the ldad config record associated with id: "
                            + id + ".");
        }

        return ldadConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog#okToClose()
     */
    @Override
    public boolean okToClose() {
        return true;
    }

    private synchronized void handleLdadNotification(
            LdadConfigNotification notification) {

        final long id = notification.getId();

        /*
         * Determine what action to take based on the type of notification.
         */
        if (notification.getType() == ConfigChangeType.Update) {
            final LdadConfig ldadConfig = this.getSpecifiedLdadConfig(id);
            if (ldadConfig == null) {
                return;
            }

            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    handleSaveUpdateInternal(ldadConfig);
                }
            });
            return;
        }

        /*
         * Currently the only other option is the delete operation. Determine if
         * there is a table row that needs to be removed.
         */
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                handleRemoveInternal(id);
            }
        });
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                if (o instanceof LdadConfigNotification) {
                    LdadConfigNotification notification = (LdadConfigNotification) o;
                    this.handleLdadNotification(notification);
                }
            } catch (Exception e) {
                statusHandler.error("Error processing notification", e);
            }
        }
    }
}