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
package com.raytheon.uf.viz.bmh.ui.dialogs.suites;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.program.AddSuitesDlg;
import com.raytheon.uf.viz.bmh.ui.program.AddSuitesDlg.SuiteDialogType;
import com.raytheon.uf.viz.bmh.ui.program.CreateEditSuiteDlg;
import com.raytheon.uf.viz.bmh.ui.program.CreateEditSuiteDlg.DialogType;
import com.raytheon.uf.viz.bmh.ui.program.SuiteTable;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Main Suite Manager dialog for managing suites that are a part of program or
 * stand-alone.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2014  #3433     lvenable     Initial creation
 * Jul 27, 2014  #3420     lvenable     Updated to use relationship button.
 * Aug 03, 2014  #3479      lvenable    Updated code for validator changes.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SuiteManagerDlg extends AbstractBMHDialog {

    /** Suite table. */
    private SuiteTable suiteTable;

    /** More information button. */
    private Button relationshipBtn;

    /** Edit suites button. */
    private Button editSuiteBtn;

    /** Rename suite button. */
    private Button renameSuiteBtn;

    /** Relationship image. */
    private Image relationshipImg;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public SuiteManagerDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Suite Manager Dialog", parentShell, SWT.DIALOG_TRIM
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
        setText("Suite Manager");

        Group suiteGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        suiteGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteGroup.setLayoutData(gd);
        suiteGroup.setText(" Available Suites: ");

        createFilterControls(suiteGroup);
        createTable(suiteGroup);
        createSuiteControls(suiteGroup);
        createBottomButtons();
    }

    /**
     * Create the filter controls.
     */
    private void createFilterControls(Group suiteGroup) {
        Composite filterComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        filterComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        filterComp.setLayoutData(gd);

        Label filerLbl = new Label(filterComp, SWT.NONE);
        filerLbl.setText("Filter Category: ");

        int indent = 10;

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterAllRdo = new Button(filterComp, SWT.RADIO);
        filterAllRdo.setText("All");
        filterAllRdo.setSelection(true);
        filterAllRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterGeneralRdo = new Button(filterComp, SWT.RADIO);
        filterGeneralRdo.setText("General");
        filterGeneralRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterHighRdo = new Button(filterComp, SWT.RADIO);
        filterHighRdo.setText("High");
        filterHighRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterExclusiveRdo = new Button(filterComp, SWT.RADIO);
        filterExclusiveRdo.setText("Exclusive");
        filterExclusiveRdo.setLayoutData(gd);

    }

    /**
     * Create the suite table.
     */
    private void createTable(Group suiteGroup) {
        suiteTable = new SuiteTable(suiteGroup, 550, 150);
        populateSuiteTable();
    }

    /**
     * Create suite controls.
     */
    private void createSuiteControls(Group suiteGroup) {
        Composite suiteControlComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(6, false);
        suiteControlComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteControlComp.setLayoutData(gd);

        int minButtonWidth = 80;

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.widthHint = minButtonWidth;
        Button addNewSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        addNewSuiteBtn.setText("New...");
        addNewSuiteBtn.setLayoutData(gd);
        addNewSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(shell,
                        DialogType.CREATE, true);
                csd.open();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = minButtonWidth;
        Button copyBtn = new Button(suiteControlComp, SWT.PUSH);
        copyBtn.setText(" Copy... ");
        copyBtn.setLayoutData(gd);
        copyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddSuitesDlg asd = new AddSuitesDlg(getShell(),
                        SuiteDialogType.COPY_ONLY);
                asd.open();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = minButtonWidth;
        renameSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        renameSuiteBtn.setText("Rename...");
        renameSuiteBtn.setLayoutData(gd);
        renameSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputTextDlg inputDlg = new InputTextDlg(shell, "Rename Suite",
                        "Type in a new suite name:", null);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        // TODO: implement code.
                        // Need to validate the input.
                        if (returnValue != null
                                && returnValue instanceof String) {
                            String name = (String) returnValue;
                            System.out.println("Suite name = " + name);
                        }
                    }
                });
                inputDlg.open();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = minButtonWidth;
        editSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        editSuiteBtn.setText("Edit...");
        editSuiteBtn.setLayoutData(gd);
        editSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(shell,
                        DialogType.EDIT, true);
                csd.open();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = minButtonWidth;
        Button deleteSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        deleteSuiteBtn.setText("Delete");
        deleteSuiteBtn.setLayoutData(gd);

        /*
         * Relationship button
         */
        ImageDescriptor id;
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/Relationship.png");
        relationshipImg = id.createImage();

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        relationshipBtn = new Button(suiteControlComp, SWT.PUSH);
        relationshipBtn.setImage(relationshipImg);
        relationshipBtn.setToolTipText("View message type relationships");
        relationshipBtn.setLayoutData(gd);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
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

    @Override
    public boolean okToClose() {
        // TODO - add code to check if it can close.
        return true;
    }

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    private void populateSuiteTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Suite Name", 200);
        columnNames.add(tcd);
        tcd = new TableColumnData("Category");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Suite - 1"));
        trd.addTableCellData(new TableCellData("General"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Suite - 2"));
        trd.addTableCellData(new TableCellData("Exclusive"));

        td.addDataRow(trd);

        suiteTable.populateTable(td);
    }
}
