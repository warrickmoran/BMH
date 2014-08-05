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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.NewDictionaryDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.PronunciationBuilderDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverter.WordType;
import com.raytheon.uf.viz.bmh.voice.NeoSpeechPhonemeMapping;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to convert legacy dictionaries to the new Neospeech {@link Dictionary}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014     3355   mpduff      Initial creation
 * Jul 21, 2014     3407   mpduff      Removed unneeded parameter to 
 *                                     PronunciationBuilderDlg
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class LegacyDictionaryConverterDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(LegacyDictionaryConverterDlg.class);

    private final String NEW_DICT = "New Dictionary...";

    /** Column Names */
    private final String[] COLUMN_NAMES = { "Converted", "Word/Phrase",
            "Legacy Phoneme" };

    private final String YES = "Yes";

    private final String NO = "No";

    /**
     * Dialog for building the individual phonemes making up a pronunciation
     */
    private PronunciationBuilderDlg pronunciationBuilderDlg;

    /** Table Composite */
    private WordTableComp tableComp;

    /** Word label */
    private Label wordValueLbl;

    /** Word type combo */
    private Combo wordTypeCombo;

    /** Legacy phoneme label */
    private Label legacyValueLbl;

    /** NeoSpeech formatted phoneme text field */
    private Text neoValueTxt;

    /** Table data object */
    private TableData tableData;

    /** Legacy dictionary file */
    private final File dictionaryFile;

    /** Save word button */
    private Button saveWordBtn;

    /** The dictionary manager */
    private final DictionaryManager dictionaryManager;

    /** Generate phoneme button */
    private Button generateBtn;

    /** Legacy dictionary converter object */
    private final LegacyDictionaryConverter converter;

    /** Legacy dictionary label */
    private Label legacyDictLbl;

    /** NeoSpeech dictionary combo */
    private Combo dictCombo;

    /** Table Columns */
    private ArrayList<TableColumnData> columns;

    /** The selected word */
    private String selectedWord;

    /** The NeoSpeech formatted phoneme */
    private String neoPhoneme;

    /** Word Group */
    private Group wordGrp;

    /** Pronounce word button */
    private Button playBtn;

    /** Play phoneme button */
    private Button phonemeBtn;

    /** Edit phoneme button */
    private Button editPhonemeBtn;

    /** Selected table index */
    public int selectedIndex;

    /**
     * List of dictionary names
     */
    private List<String> dictionaryNames;

    /** The selected dictionary */
    private Dictionary selectedDictionary;

    /**
     * Constructor
     * 
     * @param parentShell
     *            Parent Shell
     * @param dictionaryPath
     *            Path to legacy dictionary file
     */
    public LegacyDictionaryConverterDlg(Shell parentShell, String dictionaryPath) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Legacy Dictionary Converter");

        dictionaryFile = new File(dictionaryPath);
        NeoSpeechPhonemeMapping voice = new NeoSpeechPhonemeMapping();
        dictionaryManager = new DictionaryManager(voice);
        converter = new LegacyDictionaryConverter(voice);
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
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        createDictionaryComp(mainComp);

        createWordTable(mainComp);

        createWordDetailSection(mainComp);

        createColumns();
        openLegacyDictionary();
    }

    /**
     * Create the dictionary composite
     * 
     * @param comp
     *            parent composite
     */
    private void createDictionaryComp(Composite comp) {
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite labelComp = new Composite(comp, SWT.NONE);
        labelComp.setLayout(gl);
        labelComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        legacyDictLbl = new Label(labelComp, SWT.NONE);
        legacyDictLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label saveToLbl = new Label(labelComp, SWT.NONE);
        saveToLbl.setText("Save to Dictionary: ");

        dictCombo = new Combo(labelComp, SWT.SINGLE);
        dictCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseDictionary();
            }
        });

        try {
            dictionaryNames = dictionaryManager.getAllBMHDictionaryNames();
            Collections.sort(dictionaryNames);
            dictionaryNames.add(0, NEW_DICT);

            dictCombo.setItems(dictionaryNames
                    .toArray(new String[dictionaryNames.size()]));
        } catch (Exception e1) {
            statusHandler.error("Unable to query for available Dictionaries.",
                    e1);
        }
    }

    /**
     * Create the table holding the legacy dictionary entries
     * 
     * @param comp
     *            parent composite
     */
    private void createWordTable(Composite comp) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 250;
        tableComp = new WordTableComp(comp, SWT.BORDER | SWT.V_SCROLL);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);
    }

    /**
     * Create the selected word details section.
     * 
     * @param comp
     */
    private void createWordDetailSection(Composite comp) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        wordGrp = new Group(comp, SWT.BORDER);
        wordGrp.setText(" Word Details ");
        wordGrp.setLayout(gl);
        wordGrp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label wordLbl = new Label(wordGrp, SWT.NONE);
        wordLbl.setText("Word/Phrase: ");
        wordLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite c = new Composite(wordGrp, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        wordValueLbl = new Label(c, SWT.NONE);
        wordValueLbl.setLayoutData(gd);

        gd = new GridData(125, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        playBtn = new Button(c, SWT.PUSH);
        playBtn.setText("Pronounce Word");
        playBtn.setLayoutData(gd);
        playBtn.setEnabled(false);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectedWord != null) {
                    BmhUtils.playText(wordValueLbl.getText());
                }
            }
        });

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label legacyLbl = new Label(wordGrp, SWT.NONE);
        legacyLbl.setText("Legacy Phoneme: ");
        legacyLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 85;
        legacyValueLbl = new Label(wordGrp, SWT.NONE);
        legacyValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label legacyTypeLbl = new Label(wordGrp, SWT.NONE);
        legacyTypeLbl.setText("Legacy Phoneme Type: ");
        legacyTypeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        wordTypeCombo = new Combo(wordGrp, SWT.SINGLE);
        wordTypeCombo.setLayoutData(gd);
        wordTypeCombo.setItems(WordType.getTypes());
        wordTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                generateBtn.setEnabled(true);
            }
        });

        gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
        gl = new GridLayout(1, false);
        Composite neoComp = new Composite(wordGrp, SWT.NONE);
        neoComp.setLayout(gl);
        neoComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label neoLbl = new Label(neoComp, SWT.NONE);
        neoLbl.setText("Phoneme: ");
        neoLbl.setLayoutData(gd);

        gd = new GridData(105, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        phonemeBtn = new Button(neoComp, SWT.PUSH);
        phonemeBtn.setText("Play Phoneme");
        phonemeBtn.setLayoutData(gd);
        phonemeBtn.setEnabled(false);
        phonemeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (neoPhoneme != null) {
                    BmhUtils.playBriefPhoneme(neoPhoneme);
                }
            }
        });

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.heightHint = 75;
        gd.widthHint = 375;
        gd.verticalSpan = 2;
        neoValueTxt = new Text(wordGrp, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL
                | SWT.BORDER);
        neoValueTxt.setEditable(false);
        neoValueTxt.setLayoutData(gd);
        neoValueTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                editPhonemeBtn
                        .setEnabled(neoValueTxt.getText().trim().length() > 0);
            }
        });

        gl = new GridLayout(5, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        Composite btnComp = new Composite(wordGrp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(140, SWT.DEFAULT);
        generateBtn = new Button(btnComp, SWT.PUSH);
        generateBtn.setText("Generate Phoneme...");
        generateBtn.setLayoutData(gd);
        generateBtn.setEnabled(false);
        generateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // auto-generate a phoneme
                autoGenPhoneme();
            }
        });

        gd = new GridData(125, SWT.DEFAULT);
        editPhonemeBtn = new Button(btnComp, SWT.PUSH);
        editPhonemeBtn.setText("Edit Phoneme...");
        editPhonemeBtn.setLayoutData(gd);
        editPhonemeBtn.setEnabled(false);
        editPhonemeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Open the Pronunciation Builder dialog
                pronunciationBuilderDlg = new PronunciationBuilderDlg(
                        getShell(), wordValueLbl.getText());
                pronunciationBuilderDlg.setSsmlSnippet(neoValueTxt.getText());
                neoPhoneme = (String) pronunciationBuilderDlg.open();
                if (neoPhoneme != null) {
                    neoValueTxt.setText(neoPhoneme);
                }
            }
        });

        gd = new GridData(95, SWT.DEFAULT);
        saveWordBtn = new Button(btnComp, SWT.PUSH);
        saveWordBtn.setText("Save Word");
        saveWordBtn.setLayoutData(gd);
        saveWordBtn.setEnabled(false);
        saveWordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveWord();
            }
        });
    }

    /**
     * Reload the table.
     */
    private void reloadTable() {
        tableComp.populateTable(tableData);
    }

    /**
     * Populate the table
     */
    private void populateTable() {
        List<String> lines = null;
        if (dictionaryFile.exists()) {
            try {
                lines = FileUtils.readLines(dictionaryFile);
            } catch (IOException e) {
                statusHandler
                        .error("Unable to read "
                                + dictionaryFile.getAbsolutePath(), e);
                return;
            }

            tableData = new TableData(columns);

            for (String line : lines) {
                /*
                 * Ignore comment lines starting with #, the first line of each
                 * dictionary file (starts with \!), and blank lines
                 */
                if (!line.startsWith("#") && !line.startsWith("\\!")
                        && !line.isEmpty()) {
                    String[] lineParts = null;
                    // Substitution line detected
                    if (line.contains(",")) {
                        // Split on ',' if sub file, else check for space
                        lineParts = line.split(",");
                    } else {
                        // Split on whitespace
                        lineParts = line.split("\\s");
                    }

                    TableRowData row = new TableRowData();
                    TableCellData cell = new TableCellData(NO);
                    row.addTableCellData(cell);
                    cell = new TableCellData(lineParts[0]);
                    row.addTableCellData(cell);
                    cell = new TableCellData(lineParts[1]);
                    row.addTableCellData(cell);
                    tableData.addDataRow(row);
                }
            }

            this.tableComp.populateTable(tableData);
        }
    }

    /**
     * Set up dialog for the selected dictionary
     */
    private void chooseDictionary() {
        if (dictCombo.getText().equals(NEW_DICT)) {
            NewDictionaryDlg dlg = new NewDictionaryDlg(shell);
            Dictionary dict = (Dictionary) dlg.open();
            if (dict != null) {
                dictionaryNames.add(dict.getName());
                Collections.sort(dictionaryNames);
                dictCombo.setItems(dictionaryNames
                        .toArray(new String[dictionaryNames.size()]));
                dictCombo.setItem(0, NEW_DICT);
                dictCombo.select(dictionaryNames.indexOf(dict.getName()));
                try {
                    dictionaryManager.createDictionary(dict);
                    if (wordValueLbl.getText().length() >= 0) {
                        saveWordBtn.setEnabled(true);
                    }
                    updateConvertStatus();
                } catch (Exception e) {
                    statusHandler.error(
                            "Error creating dictionary: " + dict.getName(), e);
                }
            } else {
                saveWordBtn.setEnabled(false);
            }
        } else {
            try {
                selectedDictionary = dictionaryManager.getDictionary(dictCombo
                        .getText());
            } catch (Exception e) {
                statusHandler.error(
                        "Error getting dictionary: " + dictCombo.getText(), e);
            }

            boolean status = true;
            if (wordValueLbl.getText().length() <= 0) {
                status = false;
            }
            saveWordBtn.setEnabled(status);
            updateConvertStatus();
        }
    }

    /**
     * Open the legacy dictionary and populate the table
     */
    private void openLegacyDictionary() {
        populateTable();
        legacyDictLbl.setText("Converting: " + dictionaryFile.getName());
        enableButtons(false);
    }

    /**
     * Create the table columns
     */
    private void createColumns() {
        columns = new ArrayList<TableColumnData>(3);
        int width = 50;
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
        this.neoValueTxt.setText("");
        selectedWord = tableItem.getText(1);
        if (tableItem.getText(0).equalsIgnoreCase(YES)) {
            Word word = selectedDictionary.getWord(selectedWord);
            this.neoValueTxt.setText(word.getSubstitute());
        }

        wordValueLbl.setText(selectedWord);
        if (selectedWord.contains("@")) {
            wordTypeCombo.select(wordTypeCombo.indexOf(WordType.DYNAMIC
                    .getType()));
        } else if (selectedWord.contains(".")) {
            wordTypeCombo.select(wordTypeCombo.indexOf(WordType.URL.getType()));
        } else {
            wordTypeCombo.select(wordTypeCombo.indexOf(WordType.PRONUNCIATION
                    .getType()));
        }

        generateBtn.setEnabled(true);

        String legacyPhoneme = tableItem.getText(2);
        legacyValueLbl.setText(legacyPhoneme);

        boolean convert = true;
        if ((dictCombo.getText().length() <= 0)
                || (this.neoValueTxt.getText().length() == 0)) {
            convert = false;
        }
        this.saveWordBtn.setEnabled(convert);
        enableButtons(true);
    }

    /**
     * Auto-generate a phoneme
     */
    private void autoGenPhoneme() {
        String phoneme = converter.convertWordOrPhoneme(
                this.wordValueLbl.getText(), wordTypeCombo.getText(),
                this.legacyValueLbl.getText());

        this.pronunciationBuilderDlg = new PronunciationBuilderDlg(getShell(),
                wordValueLbl.getText(), phoneme);
        neoPhoneme = (String) pronunciationBuilderDlg.open();
        if (neoPhoneme != null) {
            neoValueTxt.setText(neoPhoneme);
            saveWordBtn.setEnabled(true);
        }
    }

    /**
     * Enable/disable buttons
     * 
     * @param enable
     */
    private void enableButtons(boolean enable) {
        this.playBtn.setEnabled(enable);
        this.phonemeBtn.setEnabled(enable);
    }

    /**
     * Save the converted word.
     */
    private void saveWord() {
        if (dictCombo.getText().length() == 0) {
            String message = "A destination dictionary must be selected via the\n"
                    + "\"Save to Dictionary selection\".";
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Choose Dictionary", message);
            return;
        }
        Word word = new Word();
        word.setWord(selectedWord);
        word.setSubstitute(neoPhoneme);

        selectedDictionary.getWords().add(word);

        try {
            dictionaryManager.saveDictionary(selectedDictionary);
            for (TableRowData row : tableData.getTableRows()) {
                if (selectedWord.equalsIgnoreCase(row.getTableCellData().get(1)
                        .getCellText())) {
                    row.getTableCellData().get(0).setCellText(YES);
                    updateConvertStatus();
                    return;
                }
            }
        } catch (Exception e) {
            statusHandler.error("Error saving word: " + selectedWord, e);
        }
    }

    /**
     * Update convert status
     */
    private void updateConvertStatus() {
        if (selectedDictionary != null) {
            for (TableRowData row : tableData.getTableRows()) {
                if (selectedDictionary.containsWord(row.getTableCellData()
                        .get(1).getCellText())) {
                    row.getTableCellData().get(0).setCellText(YES);
                } else {
                    row.getTableCellData().get(0).setCellText(NO);
                }
            }

            tableComp.updateTable(tableData);
            tableComp.select(selectedIndex);
        }
    }

    /**
     * Table composite class
     */
    private class WordTableComp extends TableComp {

        public WordTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle, true, true);
        }

        @Override
        public void select(int selectedIndex) {
            table.select(selectedIndex);
            table.showSelection();
            table.layout();
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // no-op
        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            TableItem[] tableItems = table.getSelection();
            selectedIndex = table.getSelectionIndex();
            tableSelectionAction(tableItems[0]);
        }

        @Override
        public void updateTable(TableData tableData) {
            table.removeAll();

            if (table.getColumnCount() != 3) {
                createColumns();
            }

            for (TableRowData rowData : tableData.getTableRows()) {
                TableItem ti = new TableItem(table, SWT.NONE);
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
