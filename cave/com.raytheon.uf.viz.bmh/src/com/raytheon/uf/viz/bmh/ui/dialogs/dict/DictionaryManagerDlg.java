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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;

/**
 * Main dialog for managing BMH dictionaries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2014   3407     mpduff      Initial creation
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * Aug 05, 2014 3175       rjpeter     Reload dictionary on edit word.
 * Aug 8, 2014    #3490    lvenable    Removed Override on update method.
 * Aug 28, 2014    3432    mpduff      Only open one new dictionary dialog
 * Sep 28, 2014   3407     mpduff      Fix button states for certain situations
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Dec 16, 2014  3618      bkowal      Disable delete when a national dictionary
 *                                     is selected.
 * Jan 05, 2014  3618      bkowal      Specify the {@link Dictionary} for deletion.
 * Jan 06, 2015  3931      bkowal      Implemented {@link Dictionary} export.
 * Jan 07, 2014  3931      bkowal      Implemented a {@link Dictionary} import capability.
 * Jan 28, 2015  4045      bkowal      Use the new {@link DictionaryManager} constructor.
 * Jun 05, 2015  4490      rjpeter     Updated constructor.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
public class DictionaryManagerDlg extends AbstractBMHDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DictionaryManagerDlg.class);

    /** Table column names */
    private final String[] COLUMN_NAMES = { "Word/Phrase",
            "Phoneme/Pronunciation" };

    private final String SELECT_DICTIONARY = "Select a Dictionary";

    /** Dictionary data manager */
    private final DictionaryManager dictionaryManager;

    /** Dictionary combo */
    private Combo dictCombo;

    /** Dictionary table composite */
    private DictionaryTableComp dictionaryTableComp;

    /** Table Columns */
    private ArrayList<TableColumnData> columns;

    /** Dictionary Table data */
    private TableData tableData;

    /** Create a dictionary button **/
    private Button newDictionaryBtn;

    /** Delete the dictionary button */
    private Button deleteDictionaryBtn;

    /** Delete word button */
    private Button deleteWordBtn;

    /** The currently selected dictionary */
    private Dictionary selectedDictionary;

    /** The new word button */
    private Button newWordBtn;

    /** The edit word button */
    private Button editWordBtn;

    /** The dictionary export button **/
    private Button exportDictionaryBtn;

    /** The new/edit word dialog */
    private NewEditWordDlg wordDlg;

    private NewDictionaryDlg newDictDlg;

    /**
     * boolean flag indicating whether or not a dictionary import is ongoing.
     * Used to ensure that users do not close the dialog during an import.
     */
    private volatile boolean importInProcess = false;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell
     */
    public DictionaryManagerDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                | CAVE.DO_NOT_BLOCK);
        setText(DlgInfo.MANAGE_DICTIONARIES.getTitle());
        dictionaryManager = new DictionaryManager();
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createDictSelectionComp();
        createWordTable();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        buildTable();

        GridData gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button closeBtn = new Button(shell, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        populateDictionaryCombo(null);
    }

    /**
     * Create the dictionary selection controls
     */
    private void createDictSelectionComp() {
        GridLayout gl = new GridLayout(4, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label lbl = new Label(comp, SWT.NONE);
        lbl.setText("Dictionary:  ");
        lbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        dictCombo = new Combo(comp, SWT.SINGLE | SWT.READ_ONLY);
        dictCombo.setLayoutData(gd);
        dictCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dictionarySelectAction();
            }
        });

        gd = new GridData(120, SWT.DEFAULT);
        newDictionaryBtn = new Button(comp, SWT.PUSH);
        newDictionaryBtn.setText("New Dictionary...");
        newDictionaryBtn.setLayoutData(gd);
        newDictionaryBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newDictionaryAction();
            }
        });

        gd = new GridData(120, SWT.DEFAULT);
        deleteDictionaryBtn = new Button(comp, SWT.PUSH);
        deleteDictionaryBtn.setText("Delete Dictionary");
        deleteDictionaryBtn.setLayoutData(gd);
        deleteDictionaryBtn.setEnabled(false);
        deleteDictionaryBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteDictionaryAction();
            }
        });
    }

    /**
     * Create the word table
     */
    private void createWordTable() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 250;
        dictionaryTableComp = new DictionaryTableComp(shell, SWT.BORDER
                | SWT.V_SCROLL);
        dictionaryTableComp.setLayout(gl);
        dictionaryTableComp.setLayoutData(gd);

        gl = new GridLayout(4, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        int btnWidth = 95;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        newWordBtn = new Button(comp, SWT.PUSH);
        newWordBtn.setText("New Word...");
        newWordBtn.setLayoutData(gd);
        newWordBtn.setEnabled(false);
        newWordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newWord();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        editWordBtn = new Button(comp, SWT.PUSH);
        editWordBtn.setText("Edit Word...");
        editWordBtn.setLayoutData(gd);
        editWordBtn.setEnabled(false);
        editWordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editWord();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        deleteWordBtn = new Button(comp, SWT.PUSH);
        deleteWordBtn.setText("Delete Word");
        deleteWordBtn.setLayoutData(gd);
        deleteWordBtn.setEnabled(false);
        deleteWordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteWord();
            }
        });

        gd = new GridData(150, SWT.DEFAULT);
        exportDictionaryBtn = new Button(comp, SWT.PUSH);
        exportDictionaryBtn.setText("Export Dictionary...");
        exportDictionaryBtn.setLayoutData(gd);
        exportDictionaryBtn.setEnabled(false);
        exportDictionaryBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDictionaryExportAction();
            }
        });
    }

    /**
     * New dictionary action
     */
    private void newDictionaryAction() {
        if (newDictDlg == null || newDictDlg.isDisposed()) {
            newDictDlg = new NewDictionaryDlg(shell);
            Dictionary dict = (Dictionary) newDictDlg.open();
            if (dict != null) {
                try {
                    dictionaryManager.createDictionary(dict);
                } catch (Exception e) {
                    statusHandler.error(
                            "Error creating dictionary: " + dict.getName(), e);
                    return;
                }
                populateDictionaryCombo(dict.getName());
            }
        } else {
            newDictDlg.bringToTop();
        }
    }

    /**
     * Delete dictionary action
     */
    private void deleteDictionaryAction() {
        String name = dictCombo.getText();
        if (name.equals(SELECT_DICTIONARY)) {
            return;
        }

        String msg = "Are you sure you wish to delete dictionary \"" + name
                + "\"?";
        int answer = DialogUtility.showMessageBox(getShell(), SWT.ICON_QUESTION
                | SWT.YES | SWT.NO, "Delete Dictionary?", msg);
        if (answer == SWT.NO) {
            return;
        }

        try {
            dictionaryManager.deleteDictionary(this.selectedDictionary);
            dictCombo.remove(name);
            tableData.getTableRows().clear();
            dictionaryTableComp.updateTable(tableData);
            dictCombo.select(0);
            dictionarySelectAction();
        } catch (Exception e) {
            statusHandler.error("Unable to delete dictionary, " + name, e);
            return;
        }
    }

    /**
     * Action handler for dictionary selection via combo
     */
    private void dictionarySelectAction() {
        String name = dictCombo.getText();
        if (!name.equals(SELECT_DICTIONARY)) {
            try {
                selectedDictionary = dictionaryManager.getDictionary(name);
            } catch (Exception e) {
                statusHandler.error("Error getting dictionary " + name, e);
                return;
            }
            /* users will not be able to delete national dictionaries */
            this.deleteDictionaryBtn.setEnabled(this.selectedDictionary
                    .isNational() == false);
            this.newWordBtn.setEnabled(true);
            this.exportDictionaryBtn.setEnabled(this.selectedDictionary
                    .getWords().isEmpty() == false);
            populateTable();
        } else {
            deleteDictionaryBtn.setEnabled(false);
            newWordBtn.setEnabled(false);
            editWordBtn.setEnabled(false);
            deleteWordBtn.setEnabled(false);
            this.exportDictionaryBtn.setEnabled(false);
            selectedDictionary = null;
            populateTable();
        }
    }

    /**
     * Populate the table
     */
    private void populateTable() {
        this.tableData = new TableData(columns);
        tableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);

        if (selectedDictionary != null) {
            for (Word word : selectedDictionary.getWords()) {
                TableRowData row = new TableRowData();
                row.setData(word);
                TableCellData tcd = new TableCellData(word.getWord());
                row.addTableCellData(tcd);

                tcd = new TableCellData(word.getSubstitute());
                row.addTableCellData(tcd);
                tableData.addDataRow(row);
            }
        }
        this.dictionaryTableComp.updateTable(tableData);
    }

    /**
     * Build the table
     */
    private void buildTable() {
        createColumns();
        tableData = new TableData(columns);
        this.dictionaryTableComp.populateTable(tableData);
    }

    /**
     * Populate the dictionary combo with a new dictionary name
     * 
     * @param name
     *            Name of new dictionary
     */
    private void populateDictionaryCombo(String name) {
        try {
            List<String> dictionaryNames = dictionaryManager
                    .getAllBMHDictionaryNames();
            Collections.sort(dictionaryNames);
            dictionaryNames.add(0, SELECT_DICTIONARY);
            dictCombo.setItems(dictionaryNames
                    .toArray(new String[dictionaryNames.size()]));
            if (name == null) {
                dictCombo.select(0);
            } else {
                int idx = dictCombo.indexOf(name);
                if ((idx >= 0) && (idx < dictCombo.getItemCount())) {
                    dictCombo.select(idx);
                    dictionarySelectAction();
                } else {
                    dictCombo.select(0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to access BMH Dictionaries.", e);
        }
    }

    /**
     * Delete the selected word.
     */
    private void deleteWord() {
        List<TableRowData> rowList = dictionaryTableComp.getSelection();
        if (!rowList.isEmpty()) {
            TableRowData row = rowList.get(0);
            String word = row.getTableCellData().get(0).getCellText();
            String msg = "Are you sure you want to delete " + word + "?";
            int response = DialogUtility.showMessageBox(getShell(),
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Delete Word?", msg);
            if (response == SWT.NO) {
                return;
            }

            Word toDelete = null;
            for (Word w : selectedDictionary.getWords()) {
                if (w.getWord().equalsIgnoreCase(word)) {
                    toDelete = w;
                    if (toDelete.getDictionary() == null) {
                        toDelete.setDictionary(selectedDictionary);
                    }
                    break;
                }
            }
            try {
                dictionaryManager.deleteWord(toDelete);
            } catch (Exception e) {
                statusHandler.error(
                        "Failed to delete word: " + toDelete.getWord(), e);
                return;
            }
            selectedDictionary.getWords().remove(toDelete);

            // update table
            tableData.deleteRow(row);
            dictionaryTableComp.updateTable(tableData);

            deleteWordBtn.setEnabled(false);
            editWordBtn.setEnabled(false);

            return;
        }
    }

    /**
     * Create a new word
     */
    private void newWord() {
        this.wordDlg = new NewEditWordDlg(getShell(), selectedDictionary);
        Word word = (Word) wordDlg.open();
        if (word == null) {
            // User canceled
            return;
        }

        /*
         * Reload the dictionary. Need to reload here in case user creates a new
         * word that already exists (selects new word button and creates a word
         * that is already in the dictionary)
         */
        dictionarySelectAction();
        editWordBtn.setEnabled(false);
        deleteWordBtn.setEnabled(false);
    }

    /**
     * Edit the selected word
     */
    private void editWord() {
        List<TableRowData> rowList = dictionaryTableComp.getSelection();
        if (!rowList.isEmpty()) {
            TableRowData row = rowList.get(0);
            Word backingWord = (Word) row.getData();
            Word word = new Word();
            word.setWord(backingWord.getWord());
            word.setSubstitute(backingWord.getSubstitute());
            word.setId(backingWord.getId());
            word.setDictionary(backingWord.getDictionary());
            this.wordDlg = new NewEditWordDlg(getShell(), word,
                    selectedDictionary);
            word = (Word) wordDlg.open();
            if (word == null) {
                // User canceled
                return;
            }
            /*
             * Reload the dictionary. Need to reload here in case user renamed a
             * word to a word that already existed.
             */
            dictionarySelectAction();
            populateTable();
            editWordBtn.setEnabled(false);
            deleteWordBtn.setEnabled(false);
        }
    }

    /**
     * Export the selected {@link Dictionary} to a user specified file.
     */
    private void handleDictionaryExportAction() {
        FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
        dialog.setText("Export BMH Dictionary");
        dialog.setFilterPath(System.getProperty("user.home"));
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String fileName = dialog.open();
        if (fileName == null) {
            return;
        }
        fileName = fileName.trim();
        if (fileName.endsWith(".xml") == false) {
            fileName += ".xml";
        }

        Path dictionaryExportPath = Paths.get(fileName);
        /**
         * Verify that all containing directories exist and create them if
         * needed.
         */
        if (Files.exists(dictionaryExportPath.getParent()) == false) {
            try {
                Files.createDirectories(dictionaryExportPath.getParent());
            } catch (IOException e) {
                statusHandler.error(
                        "Failed to create the dictionary export, cannot create directory: "
                                + dictionaryExportPath.getParent().toString(),
                        e);
                return;
            }
        }

        /**
         * Create the {@link Dictionary} export XML file.
         */
        try {
            JAXB.marshal(this.selectedDictionary,
                    Files.newOutputStream(dictionaryExportPath));
        } catch (DataBindingException | IOException e) {
            statusHandler.error("Failed to create the dictionary export: "
                    + dictionaryExportPath.toString(), e);
        }
    }

    /**
     * Create the table columns
     */
    private void createColumns() {
        columns = new ArrayList<TableColumnData>(3);
        int width = 125;
        for (String name : COLUMN_NAMES) {
            TableColumnData tc = new TableColumnData(name, width);
            tc.setAlignment(SWT.LEFT);
            columns.add(tc);
            width = 100;
        }
    }

    /**
     * Action to perform when selection is made in the table
     * 
     * @param tableItem
     *            The selected table item
     */
    private void tableSelectionAction(TableItem tableItem) {
        editWordBtn.setEnabled(true);
        deleteWordBtn.setEnabled(true);
    }

    @Override
    public boolean okToClose() {
        if (this.importInProcess) {
            return false;
        }

        if ((wordDlg == null) || wordDlg.isDisposed()) {
            return true;
        }

        return false;
    }

    public void initiateDictionaryImport(final Path importXMLPath) {
        this.importInProcess = true;

        /**
         * Disable all GUI controls for the duration of the import attempt.
         */
        this.dictCombo.setEnabled(false);
        this.newDictionaryBtn.setEnabled(false);
        this.deleteDictionaryBtn.setEnabled(false);
        this.newWordBtn.setEnabled(false);
        this.editWordBtn.setEnabled(false);
        this.deleteWordBtn.setEnabled(false);
        this.exportDictionaryBtn.setEnabled(false);

        Dictionary importedDictionary = null;
        try {
            importedDictionary = JAXB.unmarshal(
                    Files.newInputStream(importXMLPath), Dictionary.class);
        } catch (DataBindingException | IOException e) {
            statusHandler.error(
                    "The BMH Dictionary Import has failed. Failed to read the import xml file: "
                            + importXMLPath.toString(), e);

            this.importFailed();
            return;
        }

        /**
         * Verify the uniqueness of the {@link Dictionary} name.
         */
        boolean unique = false;
        try {
            unique = this.dictionaryManager
                    .verifyNameUniqueness(importedDictionary.getName());
        } catch (Exception e) {
            statusHandler
                    .error("The BMH Dictionary Import has failed. Failed to determine if the imported dictionary has a unique name: "
                            + importedDictionary.toString(), e);
            this.importFailed();
            return;
        }
        /**
         * If the {@link Dictionary} name is not unique, allow the user to
         * rename the dictionary.
         */
        if (unique == false) {
            RenameDictionaryDlg renameDlg = new RenameDictionaryDlg(this.shell,
                    importedDictionary);
            DialogUtility
                    .showMessageBox(
                            this.shell,
                            SWT.ICON_ERROR | SWT.OK,
                            "Dictionary Manager - Import BMH Dictionary",
                            "A Dictionary with name: "
                                    + importedDictionary.getName()
                                    + " already exists. Please enter a new name for the dictionary import.");
            /*
             * currently not using ICloseCallback because we want everything to
             * remain local to this method.
             */
            Object returnValue = renameDlg.open();
            if (returnValue == null
                    || returnValue instanceof Dictionary == false) {
                importedDictionary = null;
            } else {
                importedDictionary = (Dictionary) returnValue;
            }
            /**
             * handle potential import cancellation.
             */
            if (importedDictionary == null) {
                statusHandler
                        .info("The BMH Dictionary Import has been cancelled by the user.");
                this.importFailed();
                return;
            }
        }

        /**
         * The {@link Dictionary} name is unique. Create the {@link Dictionary}.
         */
        try {
            this.dictionaryManager.createDictionary(importedDictionary);
        } catch (Exception e) {
            statusHandler.error(
                    "The BMH Dictionary Import has failed. Failed to create dictionary: "
                            + importedDictionary.toString(), e);
            this.importFailed();
            return;
        }

        /**
         * The imported {@link Dictionary} was successfully created. Select it.
         */
        this.importInProcess = false;
        // Re-enable controls that are normally always enabled.
        this.dictCombo.setEnabled(true);
        this.newDictionaryBtn.setEnabled(true);
        populateDictionaryCombo(importedDictionary.getName());
    }

    private void importFailed() {
        this.importInProcess = false;
        this.dictCombo.setEnabled(true);
        this.newDictionaryBtn.setEnabled(true);
        this.dictionarySelectAction();
    }

    /**
     * Dictionary table composite class.
     */
    private class DictionaryTableComp extends TableComp {
        public DictionaryTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);

        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // no-op

        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            TableItem[] tableItems = table.getSelection();
            tableSelectionAction(tableItems[0]);
        }

        /**
         * Update the table with the new tableData.
         * 
         * @param tableData
         *            The new TableData
         */
        public void updateTable(TableData tableData) {
            table.removeAll();

            if (table.getColumnCount() != 2) {
                createColumns();
            }

            tableData.sortData();
            for (TableRowData rowData : tableData.getTableRows()) {
                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setData(rowData);
                List<TableCellData> cellDataList = rowData.getTableCellData();
                for (int i = 0; i < cellDataList.size(); i++) {
                    TableCellData cellData = cellDataList.get(i);
                    ti.setText(i, cellData.getDisplayString());
                    ti.setBackground(i, cellData.getBackgroundColor());
                    ti.setForeground(i, cellData.getForegroundColor());
                }
            }

            for (TableColumn tc : table.getColumns()) {
                tc.pack();
            }
        }
    }
}
