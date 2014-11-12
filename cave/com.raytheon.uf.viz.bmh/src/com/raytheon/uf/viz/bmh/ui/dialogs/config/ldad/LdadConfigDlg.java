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

import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadConfigDlg extends AbstractBMHDialog {

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
                SWT.DIALOG_TRIM | SWT.MIN, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
        super.setText("LDAD Configuration");
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

        ldadConfigTable = new GenericTable(ldadConfigGroup, 650, 150);
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
        // TODO: enable / disable based on table selection.
        editButton.setEnabled(false);

        deleteButton = new Button(ldadConfigBtnsComposite, SWT.PUSH);
        deleteButton.setText("Delete...");
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 80;
        deleteButton.setLayoutData(gd);
        // TODO: enable / disable based on table selection.
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

    private void handleNewAction() {
        CreateEditLdadConfigDlg createLdadConfigDlg = new CreateEditLdadConfigDlg(
                this.shell);
        createLdadConfigDlg.open();
        // TODO: handle dialog close.
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
}