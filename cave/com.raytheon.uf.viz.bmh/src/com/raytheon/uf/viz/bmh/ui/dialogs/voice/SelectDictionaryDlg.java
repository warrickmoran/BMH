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
package com.raytheon.uf.viz.bmh.ui.dialogs.voice;

import java.util.ArrayList;
import java.util.List;

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

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Allows the user to select a {@link Dictionary}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2014 3618       bkowal      Initial creation
 * Aug 04, 2015 4424       bkowal      Ensured {@link #dictionaryLanguage} would
 *                                     not be set to {@code null}.
 * Apr 04, 2016 5504       bkowal      Fix GUI sizing issues.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SelectDictionaryDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectDictionaryDlg.class);

    private final DictionaryManager dm = new DictionaryManager();

    /**
     * The {@link Language} the selected {@link Dictionary} must be associated
     * with.
     */
    private final Language dictionaryLanguage;

    /**
     * Table containing the {@link Dictionary}(ies) that can be selected.
     */
    private GenericTable dictionaryTable;

    private TableData dictionaryTableData;

    /**
     * OK button.
     */
    private Button okBtn;

    /**
     * If specified, this {@link Dictionary} will not be included in the
     * selection list.
     */
    private Dictionary filterDictionary = null;

    public SelectDictionaryDlg(Shell parentShell,
            final Language dictionaryLanguage) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        if (dictionaryLanguage == null) {
            throw new IllegalArgumentException(
                    "Required argument dictionaryLanguage cannot be NULL.");
        }
        this.dictionaryLanguage = dictionaryLanguage;
    }

    /**
     * @param filterDictionary
     *            the filterDictionary to set
     */
    public void setFilterDictionary(Dictionary filterDictionary) {
        this.filterDictionary = filterDictionary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
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
        setText("Select Dictionary");

        this.createDictionariesTable();
        this.createOkCancelButtons();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#opened()
     */
    @Override
    public void opened() {
        this.populateDictionaryTable();
    }

    /**
     * Creates a list of dictionaries that can be selected.
     */
    private void createDictionariesTable() {
        Label selectLbl = new Label(shell, SWT.NONE);
        selectLbl.setText("Select a Dictionary:");

        this.dictionaryTable = new GenericTable(this.shell, SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE, 12, 40);
        this.dictionaryTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount == 1) {
                    okBtn.setEnabled(true);
                } else {
                    okBtn.setEnabled(false);
                }
            }
        });
    }

    /**
     * Create OK & Cancel buttons.
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

    private void populateDictionaryTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(1);
        columnNames.add(new TableColumnData("Dictionary Name"));

        this.dictionaryTableData = new TableData(columnNames);

        /**
         * Retrieve the dictionaries and add them to the table.
         */
        List<Dictionary> dictionaries = null;
        try {
            dictionaries = this.dm
                    .getNonNationalDictionariesForLanguage(this.dictionaryLanguage);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to retrieve the available dictionaries.", e);
        }

        if (dictionaries == null || dictionaries.isEmpty()) {
            return;
        }

        for (Dictionary dictionary : dictionaries) {
            if (this.filterDictionary != null
                    && this.filterDictionary.equals(dictionary)) {
                continue;
            }

            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(dictionary.getName()));
            trd.setData(dictionary);

            this.dictionaryTableData.addDataRow(trd);
        }
        this.dictionaryTable.populateTable(this.dictionaryTableData);
    }

    /**
     * Handle the OK button action.
     */
    private void handleOkAction() {
        /*
         * Retrieve the associated dictionary from the selected row.
         */
        Dictionary dictionary = (Dictionary) this.dictionaryTable
                .getSelection().get(0).getData();

        this.setReturnValue(dictionary);

        close();
    }
}