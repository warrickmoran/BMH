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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.NewEditTransmitterDlg.TransmitterEditType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Component displayed on the {@link CreateEditTransmitterLangDialog} dialog
 * used to manage {@link TransmitterLanguage}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 12, 2015 3809       bkowal      Initial creation
 * Jan 19, 2015 4011       bkowal      Implemented a delete option.
 * Jan 20, 2015 4011       bkowal      All languages can now be deleted.
 * Jan 22, 2015 3995       rjpeter     Update new TransmitterGroup check.
 * Feb 09, 2015 4082       bkowal      Added the ability to add languages to
 *                                     new Transmitter Groups.
 * Feb 16, 2015 4082       bkowal      Only disable edit, delete buttons based on
 *                                     Transmitter or Group selection.
 * Feb 17, 2015 4082       bkowal      The contents of the languages table should determine
 *                                     whether the add button is disabled/enabled when it is
 *                                     not forcefully disabled.
 * Mar 18, 2015 4213       bkowal      Keep track of the currently selected program.
 * Jun 24, 2015 4490       bkowal      Ensure that the {@link TransmitterLanguage}s remain sorted
 *                                     when added to the table.
 * Apr 04, 2016 5504       bkowal      Fix GUI sizing issues.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterLanguageComp {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TransmitterLanguageComp.class);

    private final TransmitterLanguageDataManager tldm = new TransmitterLanguageDataManager();

    private final TransmitterGroup group;

    private ProgramSummary selectedProgram;

    private final Shell shell;

    private Button addButton;

    private Button editButton;

    private Button deleteButton;

    private GenericTable languagesTable;

    private List<Language> unassignedLanguages = Collections.emptyList();

    private final SortedMap<Language, TransmitterLanguage> existingLanguagesMap = new TreeMap<>();

    private final Map<Language, TransmitterLanguage> archiveLanguagesMap = new HashMap<>(
            Language.values().length, 1.0f);

    public TransmitterLanguageComp(final Shell shell, TransmitterGroup group,
            final TransmitterEditType type) {
        this.shell = shell;
        /*
         * only associate existing groups with the languages if the user is
         * editing a group directly or if they are editing a standalone
         * transmitter.
         */
        if (type == TransmitterEditType.EDIT_TRANSMITTER_GROUP
                || (group != null && group.isStandalone())) {
            this.group = group;
        } else {
            this.group = new TransmitterGroup();
        }
        this.init(shell);
        this.retrieveLanguages();
    }

    private void init(final Shell shell) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        /* Create the framing component. */
        Group languagesGroup = new Group(shell, SWT.BORDER);
        languagesGroup.setText(" Languages ");
        languagesGroup.setLayout(gl);
        languagesGroup.setLayoutData(gd);

        final int buttonWidth = 80;

        /* Prepare buttons to add/edit languages */
        Composite languagesComp = new Composite(languagesGroup, SWT.NONE);
        gl = new GridLayout(3, false);
        languagesComp.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        languagesComp.setLayoutData(gd);

        /* Add Button */
        addButton = new Button(languagesComp, SWT.PUSH);
        addButton.setText("Add...");
        gd = new GridData();
        gd.widthHint = buttonWidth;
        addButton.setLayoutData(gd);
        /*
         * the add button will only be enabled if every system-recognized
         * language has not been associated with the transmitter.
         */
        this.addButton.setEnabled(false);
        this.addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddAction();
            }
        });

        /* Edit Button */
        editButton = new Button(languagesComp, SWT.PUSH);
        editButton.setText("Edit...");
        gd = new GridData();
        gd.widthHint = buttonWidth;
        editButton.setLayoutData(gd);
        /*
         * the edit button will only be enabled when a language has been
         * selected in the table.
         */
        this.editButton.setEnabled(false);
        this.editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleEditAction();
            }
        });

        /* Delete Button */
        deleteButton = new Button(languagesComp, SWT.PUSH);
        deleteButton.setText("Delete");
        gd = new GridData(buttonWidth, SWT.DEFAULT);
        deleteButton.setLayoutData(gd);
        /*
         * the delete button will only be enabled when a language has been
         * selected in the table and there is more than one language.
         */
        this.deleteButton.setEnabled(false);
        this.deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });

        /* Create the languages table. */
        this.languagesTable = new GenericTable(languagesGroup, SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE, 6);
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Language", 240);
        columnNames.add(tcd);
        tcd = new TableColumnData("Voice");
        columnNames.add(tcd);
        TableData languageTableData = new TableData(columnNames);
        this.languagesTable.populateTable(languageTableData);
        this.languagesTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                editButton.setEnabled(selectionCount == 1);
                deleteButton.setEnabled(selectionCount == 1);
            }
        });
    }

    public void enableGroupControls(boolean enabled) {
        if (enabled) {
            /*
             * edit and delete should only be enabled when a language is
             * actually selected.
             */
            if (this.archiveLanguagesMap.isEmpty() == false) {
                this.existingLanguagesMap.putAll(this.archiveLanguagesMap);
                this.archiveLanguagesMap.clear();
            }
            this.buildLanguagesTable();
        } else {
            /*
             * always disable edit and delete when disabled.
             */
            this.addButton.setEnabled(enabled);
            this.editButton.setEnabled(enabled);
            this.deleteButton.setEnabled(enabled);
            this.languagesTable.removeAllTableItems();
            this.archiveLanguagesMap.putAll(this.existingLanguagesMap);
            this.existingLanguagesMap.clear();
        }
    }

    private void retrieveLanguages() {
        unassignedLanguages = new ArrayList<>(Language.values().length);
        for (Language language : Language.values()) {
            unassignedLanguages.add(language);
        }

        if ((this.group == null) || (group.getName() == null)
                || (group.getId() == 0)) {
            /*
             * this is a completely new Transmitter Group.
             */
            this.addButton.setEnabled(true);
            return;
        }

        List<TransmitterLanguage> languages = null;
        try {
            languages = this.tldm.getLanguagesForGroup(this.group);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to retrieve the languages associated with transmitter group: "
                            + this.group.getName() + ".", e);
            return;
        }

        /*
         * Determine if the user will be allowed to create new languages.
         */
        if (languages.size() < Language.values().length) {
            this.addButton.setEnabled(true);
        }

        /*
         * Store the language information for initial and subsequent language
         * table population.
         */
        for (TransmitterLanguage language : languages) {
            this.existingLanguagesMap.put(language.getLanguage(), language);
            unassignedLanguages.remove(language.getLanguage());
        }
        this.buildLanguagesTable();
    }

    /**
     * Re-populates the transmitter language table based on the contents of the
     * languages map at the time.
     */
    private void buildLanguagesTable() {
        TableData tableData = this.languagesTable.getTableData();
        /* reset the table */
        tableData.deleteAllRows();

        for (Language language : this.existingLanguagesMap.keySet()) {
            TransmitterLanguage tl = this.existingLanguagesMap.get(language);

            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(tl.getLanguage().toString()));
            trd.addTableCellData(new TableCellData(tl.getVoice().getVoiceName()));
            trd.setData(language);
            tableData.addDataRow(trd);
        }
        this.languagesTable.populateTable(tableData);
        this.addButton.setEnabled(CollectionUtils
                .isNotEmpty(this.unassignedLanguages));
        this.editButton.setEnabled(false);
        this.deleteButton.setEnabled(false);
    }

    private void handleAddAction() {
        CreateEditTransmitterLangDialog dialog = new CreateEditTransmitterLangDialog(
                this.shell, this.unassignedLanguages, this.group,
                this.selectedProgram);
        dialog.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if ((returnValue == null)
                        || ((returnValue instanceof TransmitterLanguage) == false)) {
                    return;
                }

                handleNewLanguage((TransmitterLanguage) returnValue);
            }
        });
        dialog.open();
    }

    private void handleNewLanguage(TransmitterLanguage transmitterLanguage) {
        /**
         * Only save the {@link TransmitterLanguage} if the associated
         * {@link TransmitterGroup} also exists.
         */
        if (transmitterLanguage.getTransmitterGroup() != null
                && transmitterLanguage.getTransmitterGroup().getId() != 0) {
            try {
                transmitterLanguage = this.tldm
                        .saveLanguage(transmitterLanguage);
            } catch (Exception e) {
                statusHandler.error("Failed to save the transmitter language: "
                        + transmitterLanguage.toString() + ".", e);
                return;
            }
        }

        this.unassignedLanguages.remove(transmitterLanguage.getLanguage());
        this.existingLanguagesMap.put(transmitterLanguage.getLanguage(),
                transmitterLanguage);

        this.buildLanguagesTable();
    }

    private void handleEditAction() {
        CreateEditTransmitterLangDialog dialog = new CreateEditTransmitterLangDialog(
                this.shell, this.getSelectedLanguage(), this.group,
                this.selectedProgram);
        dialog.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof TransmitterLanguage) {
                    handleUpdatedLanguage((TransmitterLanguage) returnValue);
                }
            }
        });
        dialog.open();
    }

    private void handleDeleteAction() {
        /*
         * Verify that the user wants to remove the Transmitter Language.
         */
        TransmitterLanguage tl = this.getSelectedLanguage();
        StringBuilder sb = new StringBuilder(
                "Are you sure you want to delete language: ");
        sb.append(tl.getLanguage().toString());
        if (tl.getTransmitterGroup() != null
                && tl.getTransmitterGroup().getName() != null) {
            sb.append(" for Transmitter ");
            sb.append(tl.getTransmitterGroup().getName()).append("?");
        } else {
            /*
             * name will be NULL for an unsaved Transmitter/Group.
             */
            sb.append("?");
        }

        int option = DialogUtility.showMessageBox(this.shell, SWT.ICON_QUESTION
                | SWT.YES | SWT.NO, "Transmitter Language - Delete",
                sb.toString());
        if (option != SWT.YES) {
            return;
        }

        if (tl.getTransmitterGroup() != null
                && tl.getTransmitterGroup().getId() != 0) {
            try {
                this.tldm.deleteLanguage(tl);
            } catch (Exception e) {
                statusHandler.error("Failed to delete transmitter language: "
                        + tl.toString() + ".", e);
                return;
            }
        }
        this.existingLanguagesMap.remove(tl.getLanguage());
        this.unassignedLanguages.add(tl.getLanguage());

        this.buildLanguagesTable();
    }

    private void handleUpdatedLanguage(TransmitterLanguage transmitterLanguage) {
        if (transmitterLanguage.getTransmitterGroup() != null
                && transmitterLanguage.getTransmitterGroup().getId() != 0) {
            try {
                transmitterLanguage = this.tldm
                        .saveLanguage(transmitterLanguage);
            } catch (Exception e) {
                statusHandler.error(
                        "Failed to update the transmitter language: "
                                + transmitterLanguage.toString() + ".", e);
                return;
            }
        }
        this.existingLanguagesMap.put(transmitterLanguage.getLanguage(),
                transmitterLanguage);

        this.buildLanguagesTable();
    }

    private TransmitterLanguage getSelectedLanguage() {
        /*
         * Determine what the selected transmitter language is. There should
         * only be one selected row.
         */
        TableRowData trd = this.languagesTable.getSelection().get(0);
        Language language = (Language) trd.getData();

        return this.existingLanguagesMap.get(language);
    }

    /**
     * Returns a {@link List} of unsaved {@link TransmitterLanguage}s that are
     * present in the {@link #languagesTable} {@link Table}.
     * 
     * @return a {@link List} of unsaved {@link TransmitterLanguage}s that are
     *         present in the {@link #languagesTable} {@link Table}
     */
    public List<TransmitterLanguage> getUnsavedTransmitterLanguages() {
        if (this.existingLanguagesMap.values().isEmpty()) {
            return Collections.emptyList();
        }

        List<TransmitterLanguage> unsavedLanguages = new ArrayList<>(
                this.existingLanguagesMap.values().size());
        for (TransmitterLanguage tl : this.existingLanguagesMap.values()) {
            if (tl.getId().getTransmitterGroup() != null
                    && tl.getId().getTransmitterGroup().getId() > 0) {
                continue;
            }
            /*
             * eliminate the partial transmitter group because the actual
             * transmitter group will be set when the data is persisted to the
             * database.
             */
            tl.getId().setTransmitterGroup(null);
            unsavedLanguages.add(tl);
        }
        return unsavedLanguages;
    }

    /**
     * @param selectedProgram
     *            the selectedProgram to set
     */
    public void setSelectedProgram(ProgramSummary selectedProgram) {
        this.selectedProgram = selectedProgram;
    }
}