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
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.ISuiteSelection;
import com.raytheon.uf.viz.bmh.ui.program.AddSuitesDlg.SuiteDialogType;
import com.raytheon.uf.viz.bmh.ui.program.CreateEditSuiteDlg.DialogType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Group/Composite containing suite table and controls.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2014  #3174     lvenable     Initial creation
 * Jul 24, 2014  #3433     lvenable     Updated for Suite manager
 * Jul 27, 2014  #3420     lvenable     Code clean up.
 * Aug 01, 2014  #3479     lvenable     Added additional capability and cleaned up code.
 * Aug 06, 2014  #3490     lvenable    Refactored and added additional functionality.
 * Aug 8, 2014    #3490     lvenable    Updated populate table method call.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SuiteConfigGroup extends Composite {

    /** Prefix for the suite group text. */
    private String suiteGroupText = null;

    /** Suite group. */
    private Group suiteGroup;

    /** Suite table. */
    private SuiteTable suiteTable;

    /** Parent composite. */
    private Composite parentComp;

    /** Array of Suite controls.. */
    private List<Control> suiteControls = new ArrayList<Control>();

    /** Suite table data. */
    private TableData suiteTableData = null;

    /** List of suite data. */
    private List<Suite> suiteList;

    /** Selection callback. */
    private ISuiteSelection suiteSelectionCB;

    /** Relationship image. */
    private Image relationshipImg;

    /** Enumeration of suite group types. */
    public enum SuiteGroupType {
        PROGRAM, SUITE;
    };

    /** Suite group type. */
    private SuiteGroupType suiteGroupType = SuiteGroupType.SUITE;

    /** Suite category type. */
    private SuiteType suiteCatType = null;

    /** Table width. */
    private int tableWidth = 0;

    /** Table height. */
    private int tableHeight = 0;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param suiteGroupText
     *            Text to display in the group.
     * @param suiteGroupType
     *            Type that the suite group is used for.
     */
    public SuiteConfigGroup(Composite parentComp, String suiteGroupText,
            SuiteGroupType suiteGroupType) {
        this(parentComp, suiteGroupText, suiteGroupType, 400, 150);
    }

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param suiteGroupText
     *            Text to display in the group.
     * @param suiteGroupType
     *            Type that the suite group is used for.
     * @param tableWidth
     *            Table width.
     * @param tableHeight
     *            Table height.
     */
    public SuiteConfigGroup(Composite parentComp, String suiteGroupText,
            SuiteGroupType suiteGroupType, int tableWidth, int tableHeight) {
        super(parentComp, SWT.NONE);

        this.parentComp = parentComp;
        this.suiteGroupText = suiteGroupText;
        this.suiteGroupType = suiteGroupType;
        this.tableWidth = tableWidth;
        this.tableHeight = tableHeight;

        init();
    }

    /**
     * Add the callback action for the selected suite.
     * 
     * @param suiteSelectionCB
     *            Callback action.
     */
    public void setCallBackAction(ISuiteSelection suiteSelectionCB) {
        this.suiteSelectionCB = suiteSelectionCB;
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        this.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayoutData(gd);

        suiteGroup = new Group(this, SWT.SHADOW_OUT);
        gl = new GridLayout(1, false);
        suiteGroup.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteGroup.setLayoutData(gd);
        suiteGroup.setText(suiteGroupText);

        createFilterControls();
        createTable();
        createSuiteControls();

        enableControls(suiteTable.hasSelectedItems());

        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (relationshipImg != null) {
                    relationshipImg.dispose();
                }
            }
        });
    }

    /**
     * Create the filter controls.
     */
    private void createFilterControls() {
        Composite filterComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        filterComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        filterComp.setLayoutData(gd);

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                Button btn = (Button) e.widget;

                if (btn.getData() != null) {
                    suiteCatType = (SuiteType) btn.getData();
                } else {
                    suiteCatType = null;
                }
                populateSuiteTable();
            }
        };

        Label filerLbl = new Label(filterComp, SWT.NONE);
        filerLbl.setText("Filter Category: ");

        int indent = 10;

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterAllRdo = new Button(filterComp, SWT.RADIO);
        filterAllRdo.setText("All");
        filterAllRdo.setSelection(true);
        filterAllRdo.setLayoutData(gd);
        filterAllRdo.setData(null);
        filterAllRdo.addSelectionListener(sa);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterGeneralRdo = new Button(filterComp, SWT.RADIO);
        filterGeneralRdo.setText("General");
        filterGeneralRdo.setLayoutData(gd);
        filterGeneralRdo.setData(SuiteType.GENERAL);
        filterGeneralRdo.addSelectionListener(sa);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterHighRdo = new Button(filterComp, SWT.RADIO);
        filterHighRdo.setText("High");
        filterHighRdo.setLayoutData(gd);
        filterHighRdo.setData(SuiteType.HIGH);
        filterHighRdo.addSelectionListener(sa);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterExclusiveRdo = new Button(filterComp, SWT.RADIO);
        filterExclusiveRdo.setText("Exclusive");
        filterExclusiveRdo.setLayoutData(gd);
        filterExclusiveRdo.setData(SuiteType.EXCLUSIVE);
        filterExclusiveRdo.addSelectionListener(sa);
    }

    /**
     * Create the suite table.
     */
    private void createTable() {
        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
        suiteTable = new SuiteTable(suiteGroup, tableStyle, tableWidth,
                tableHeight);

        suiteTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    enableControls(true);
                    if (suiteSelectionCB != null) {
                        suiteSelectionCB.suiteSelected(suiteList.get(suiteTable
                                .getSelectedIndices()[0]));
                    }
                } else {
                    enableControls(false);
                    suiteSelectionCB.suiteSelected(null);
                }
            }
        });
    }

    /**
     * Create suite controls.
     */
    private void createSuiteControls() {

        int numberOfColumns = 0;
        final boolean programControls;
        if (suiteGroupType == SuiteGroupType.PROGRAM) {
            programControls = true;
            numberOfColumns = 4;
        } else {
            programControls = false;
            numberOfColumns = 6;
        }

        Composite suiteControlComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(numberOfColumns, false);
        suiteControlComp.setLayout(gl);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        suiteControlComp.setLayoutData(gd);

        int minButtonWidth = 80;

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button addNewSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        addNewSuiteBtn.setText("New...");
        addNewSuiteBtn.setLayoutData(gd);
        addNewSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                        .getShell(), DialogType.CREATE, programControls);
                csd.open();
            }
        });

        if (suiteGroupType == SuiteGroupType.PROGRAM) {
            gd = new GridData();
            gd.minimumWidth = minButtonWidth;
            Button addExistingBtn = new Button(suiteControlComp, SWT.PUSH);
            addExistingBtn.setText(" Add Existing... ");
            addExistingBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    AddSuitesDlg asd = new AddSuitesDlg(getShell(),
                            SuiteDialogType.ADD_COPY);
                    asd.open();
                }
            });
            suiteControls.add(addExistingBtn);
        } else {
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
            suiteControls.add(copyBtn);

            gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
            gd.widthHint = minButtonWidth;
            Button renameSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
            renameSuiteBtn.setText("Rename...");
            renameSuiteBtn.setLayoutData(gd);
            renameSuiteBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    InputTextDlg inputDlg = new InputTextDlg(parentComp
                            .getShell(), "Rename Suite",
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
            suiteControls.add(renameSuiteBtn);
        }

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button editSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        editSuiteBtn.setText("Edit...");
        editSuiteBtn.setLayoutData(gd);
        editSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                        .getShell(), DialogType.EDIT, programControls);
                csd.open();
            }
        });
        suiteControls.add(editSuiteBtn);

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button deleteSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        deleteSuiteBtn.setText("Delete");
        deleteSuiteBtn.setLayoutData(gd);
        deleteSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });
        suiteControls.add(deleteSuiteBtn);

        if (suiteGroupType == SuiteGroupType.SUITE) {
            /*
             * Relationship button
             */
            ImageDescriptor id;
            id = AbstractUIPlugin.imageDescriptorFromPlugin(
                    Activator.PLUGIN_ID, "icons/Relationship.png");
            relationshipImg = id.createImage();

            gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
            Button relationshipBtn = new Button(suiteControlComp, SWT.PUSH);
            relationshipBtn.setImage(relationshipImg);
            relationshipBtn.setToolTipText("View message type relationships");
            relationshipBtn.setLayoutData(gd);
            relationshipBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ViewSuiteDlg vsd = new ViewSuiteDlg(parentComp.getShell());
                    vsd.open();
                }
            });
            suiteControls.add(relationshipBtn);
        }
    }

    /**
     * Action taken when a table items is removed.
     */
    private void handleDeleteAction() {

        // TODO: delete the suite
        // select an available item

        enableControls(suiteTable.hasSelectedItems());
    }

    /**
     * Enable/Disable the suite controls.
     * 
     * @param enable
     *            True to enable, false to disable the controls.
     */
    private void enableControls(boolean enable) {
        for (Control ctrl : suiteControls) {
            ctrl.setEnabled(enable);
        }
    }

    /**
     * Get the suite selected in the table.
     * 
     * @return The selected suite or null if nothing is selected.
     */
    public Suite getSelectedSuite() {
        if (!suiteTable.hasSelectedItems()) {
            return null;
        }

        return suiteList.get(suiteTable.getSelectedIndex());
    }

    /**
     * Enable/Disable the suite controls.
     */
    private void enableSuiteControls() {
        for (Control ctrl : suiteControls) {
            ctrl.setEnabled(suiteTable.getSelectedIndex() >= 0);
        }
    }

    /**
     * Update the suite name in the Group text.
     * 
     * @param suiteName
     */
    public void updateSuiteGroupText(String newSuiteGroupText) {
        suiteGroup.setText(newSuiteGroupText);
    }

    /**
     * Populate the suite table using the list of suites.
     * 
     * @param suiteList
     *            List of suites.
     */
    public void populateSuiteTable(List<Suite> suiteList) {
        this.suiteList = suiteList;
        populateSuiteTable();
    }

    /**
     * Populate the Suite table.
     */
    public void populateSuiteTable() {

        if (suiteTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Suite Name", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Category");
            columnNames.add(tcd);

            suiteTableData = new TableData(columnNames);
        } else {
            suiteTableData.deleteAllRows();
        }

        populateSuiteTableData();
        suiteTable.populateTable(suiteTableData);

        if (suiteTable.getItemCount() > 0) {
            suiteTable.select(0);
        }
        enableSuiteControls();
    }

    /**
     * Populate the suite table data.
     */
    private void populateSuiteTableData() {
        for (Suite suite : suiteList) {
            if (suiteCatType == null || suite.getType() == suiteCatType) {
                TableRowData trd = new TableRowData();

                trd.addTableCellData(new TableCellData(suite.getName()));
                trd.addTableCellData(new TableCellData(suite.getType().name()));

                suiteTableData.addDataRow(trd);
            }
        }
    }
}
