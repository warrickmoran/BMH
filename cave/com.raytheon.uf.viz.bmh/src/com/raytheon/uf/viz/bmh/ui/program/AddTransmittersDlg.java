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
package com.raytheon.uf.viz.bmh.ui.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to display the list of transmitters that are available.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014  #3174     lvenable     Initial creation
 * Aug 23, 2014  #3490     lvenable     Hook up transmitter data.
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 09, 2014  #3646     rferrel      Convert tableComp to GenericTable.
 * Dec 07, 2014   3846     mpduff       Populate hashmap internally, handle state of add button
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class AddTransmittersDlg extends CaveSWTDialog {

    private final String NA = "N/A";

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AddTransmittersDlg.class);

    /** Program name. */
    private final String programName;

    /** Table listing the available transmitters. */
    private GenericTable tableComp;

    /** List of transmitter groups. */
    private List<TransmitterGroup> transmitterGrps = null;

    /** Map of transmitter group name (key) and program name (value). */
    private final Map<String, String> transGrpProgramMap = new HashMap<>();

    /** List of selected transmitters. */
    private final List<TransmitterGroup> selectedTransmitters = new ArrayList<TransmitterGroup>();

    /** Add button */
    private Button addBtn;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param programName
     *            Program name.
     */
    public AddTransmittersDlg(Shell parentShell, String programName) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN, CAVE.DO_NOT_BLOCK
                | CAVE.PERSPECTIVE_INDEPENDENT);
        this.programName = programName;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 2;
        mainLayout.marginWidth = 2;

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
        setText("Add Transmitters");

        retrieveTransmitterData();

        createTransmitterTable();
        createBottomButtons();
    }

    /**
     * Create the transmitter table.
     */
    private void createTransmitterTable() {
        GridData gd = new GridData();
        gd.horizontalIndent = 5;
        Label selectLbl = new Label(shell, SWT.NONE);
        selectLbl.setText("Select transmitter(s) to add to progam "
                + programName + ": ");
        selectLbl.setLayoutData(gd);

        ITableActionCB callback = new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                enableAddBtn(selectionCount);
            }
        };
        tableComp = new GenericTable(shell, 650, 150);
        tableComp.setCallbackAction(callback);

        populateTransmitterTable();
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        addBtn = new Button(buttonComp, SWT.PUSH);
        addBtn.setText(" Add ");
        addBtn.setLayoutData(gd);
        addBtn.setEnabled(false);
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (confirmMessage()) {
                    setReturnValue(selectedTransmitters);
                    close();
                }
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
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Confirm adding transmitters.
     * 
     * @return True to add the transmitters.
     */
    private boolean confirmMessage() {
        int[] indices = tableComp.getSelectedIndices();

        selectedTransmitters.clear();

        for (int i = 0; i < indices.length; i++) {
            selectedTransmitters.add(transmitterGrps.get(indices[i]));
        }

        StringBuilder sb = new StringBuilder();
        for (TransmitterGroup tg : selectedTransmitters) {
            if (transGrpProgramMap.containsKey(tg.getName())) {
                if (!transGrpProgramMap.get(tg.getName()).equals(NA)) {
                    sb.append(tg.getName()).append("\n");
                }
            }
        }

        if (sb.length() > 0) {

            sb.insert(0,
                    "The following transmitters/groups have assigned programs:\n\n");
            sb.append("\nDo you wish to continue assigning the program "
                    + programName + " to these transmitters/groups?");

            int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.OK | SWT.CANCEL, "Replace Program", sb.toString());

            if (result == SWT.CANCEL) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the transmitter group data from the database.
     */
    private void retrieveTransmitterData() {
        TransmitterDataManager tdm = new TransmitterDataManager();

        try {
            transmitterGrps = tdm.getTransmitterGroups();

            Collections.sort(transmitterGrps,
                    new TransmitterGroupPositionComparator());

            for (TransmitterGroup tg : transmitterGrps) {
                if (transGrpProgramMap.containsKey(tg.getName())) {
                    continue;
                }
                if (tg.getProgramSummary() != null) {
                    transGrpProgramMap.put(tg.getName(), tg.getProgramSummary()
                            .getName());
                } else {
                    transGrpProgramMap.put(tg.getName(), NA);
                }
            }

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return;
        }
    }

    /**
     * Populate the transmitter/group table.
     */
    private void populateTransmitterTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Group/Transmitter", 130);
        columnNames.add(tcd);
        tcd = new TableColumnData("Transmitter(s)", 250);
        columnNames.add(tcd);
        tcd = new TableColumnData("Assigned Program");
        columnNames.add(tcd);
        TableData transGrpTableData = new TableData(columnNames);

        for (TransmitterGroup tg : transmitterGrps) {

            // If the program is the current program selected then don't put it
            // in the list.
            if (programName.equals(transGrpProgramMap.get(tg.getName()))) {
                continue;
            }

            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(tg.getName()));

            List<Transmitter> transList = tg.getTransmitterList();
            StringBuilder sb = new StringBuilder();
            for (Transmitter t : transList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(t.getName());
            }

            trd.addTableCellData(new TableCellData(sb.toString()));

            if (transGrpProgramMap.containsKey(tg.getName())) {
                trd.addTableCellData(new TableCellData(transGrpProgramMap
                        .get(tg.getName())));
            } else {
                trd.addTableCellData(new TableCellData("No Assigned Program"));
            }

            transGrpTableData.addDataRow(trd);
        }

        tableComp.populateTable(transGrpTableData);
    }

    /**
     * Enable Add button if one or more rows selected
     * 
     * @param tableSelectionCount
     *            number of rows selected
     */
    private void enableAddBtn(int tableSelectionCount) {
        addBtn.setEnabled(tableSelectionCount > 0);
    }
}
