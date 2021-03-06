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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.ISuiteSelection;
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
 * Jul 20, 2014  3174      lvenable     Initial creation
 * Jul 24, 2014  3433      lvenable     Updated for Suite manager
 * Jul 27, 2014  3420      lvenable     Code clean up.
 * Aug 01, 2014  3479      lvenable     Added additional capability and cleaned up code.
 * Aug 06, 2014  3490      lvenable     Refactored and added additional functionality.
 * Aug 12, 2014  3490      lvenable     Updated populate table method call and added additional
 *                                      functionality so this class could be used in multiple places.
 * Aug 15, 2014  3490      lvenable     Reworked to use updated interface and allow the suite table to
 *                                      re-populate without rebuilding the table.
 * Aug 18, 2014  3490      lvenable     Added callback calls for actions on the suites.
 * Aug 21, 2014  3490      lvenable     Added capability when creating new programs.
 * Aug 25, 2014  3490      lvenable     Method to set the selected program.
 * Sep 10, 2014  3490      lvenable     Fixed existing suite name problem.
 * Oct 15, 2014  3716      bkowal       Display 'Remove' instead of 'Delete' when within the
 *                                      Broadcast Program Dialog.
 * Oct 26, 2014  3750      mpduff       Get updates from filtered list.
 * Oct 28, 2014  3750      lvenable     Updated the selected suite when selecting the table index via suite ID.
 * Nov 17, 2014  3698      rferrel      Added checks to allow only 1 GENERAL type suite in a program.
 * Dec 09, 2014  3906      lvenable     Added method to change the grid data.
 * Dec 13, 2014  3833      lvenable     Fixed selection to get items from correct list.
 * Mar 24, 2015  4307      rferrel      Do not create filter controls for new program.
 * Apr 28, 2015  4428      rferrel      Clean up CreateEditSuiteDlg constructors
 * Mar 25, 2016  5504      bkowal       Fix GUI sizing issues.
 * Apr 04, 2016  5504      bkowal       Updated for compatibility with TableComp changes.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SuiteConfigGroup extends Composite {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /** Suite group. */
    private Group suiteGroup;

    /** Suite table. */
    private SuiteTable suiteTable;

    /** Parent composite. */
    private final Composite parentComp;

    /** Array of Suite controls.. */
    private final List<Control> suiteControls = new ArrayList<Control>();

    /** Suite table data. */
    private TableData suiteTableData = null;

    /** List of suite data. */
    private List<Suite> suiteList = null;

    /** List of suite data. */
    private final List<Suite> filteredSuiteList = new ArrayList<Suite>();

    /** Selection callback. */
    private ISuiteSelection suiteSelectionCB;

    /** Relationship image. */
    private Image relationshipImg;

    /** Enumeration of suite group types. */
    public enum SuiteGroupType {
        BROADCAST_PROGRAM, NEW_PROGRAM, SUITE_MGR, ADD_COPY_EXITING;
    };

    /** Suite group type. */
    private SuiteGroupType suiteGroupType = SuiteGroupType.SUITE_MGR;

    private final int desiredNumRows;

    /** Suite category type. */
    private SuiteType suiteCatType = null;

    /** The selected program associated with the suites. Can be a null value. */
    private Program selectedProgram = null;

    /**
     * The selected suite in the table. If there are multiple items selected
     * then it will be the first suite selected.
     */
    private Suite selectedSuite = null;

    /** Current Selected Suites in the display. */
    private final List<Suite> existingSuites;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param suiteGroupText
     *            Text to display in the group.
     * @param suiteGroupType
     *            Type that the suite group is used for.
     * @param selectedProgram
     *            Program associated with the suites. Can be null.
     * @param tableWidth
     *            Table width.
     * @param tableHeight
     *            Table height.
     */
    public SuiteConfigGroup(Composite parentComp, String suiteGroupText,
            SuiteGroupType suiteGroupType, Program selectedProgram,
            int desiredNumRows) {
        super(parentComp, SWT.NONE);

        this.parentComp = parentComp;
        this.suiteGroupType = suiteGroupType;
        this.desiredNumRows = desiredNumRows;
        this.selectedProgram = selectedProgram;
        if (selectedProgram == null) {
            this.existingSuites = new ArrayList<>();
        } else {
            this.existingSuites = selectedProgram.getSuites();
        }

        init(suiteGroupText);
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
     * Set the selected program.
     * 
     * @param p
     *            The program that was selected.
     */
    public void setSelectedProgram(Program p) {
        selectedProgram = p;
    }

    /**
     * Method to change the horizontal and vertical fill styles.
     * 
     * @param horizontal
     *            Horizontal alignment.
     * @param vertical
     *            Vertical alignment.
     * @param horizontalExcess
     *            Horizontal excess flag.
     * @param verticalExcess
     *            Vertical excess flag.
     */
    public void setFillStyleOnTable(int horizontal, int vertical,
            boolean horizontalExcess, boolean verticalExcess) {
        ((GridData) this.getLayoutData()).horizontalAlignment = horizontal;
        ((GridData) this.getLayoutData()).verticalAlignment = vertical;
        ((GridData) this.getLayoutData()).grabExcessHorizontalSpace = horizontalExcess;
        ((GridData) this.getLayoutData()).grabExcessVerticalSpace = verticalExcess;
    }

    /**
     * Initialize method.
     */
    private void init(String suiteGroupText) {
        GridLayout gl = new GridLayout(1, false);
        this.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.setLayoutData(gd);

        suiteGroup = new Group(this, SWT.SHADOW_OUT);
        gl = new GridLayout(1, false);
        suiteGroup.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        suiteGroup.setLayoutData(gd);
        suiteGroup.setText(suiteGroupText);

        if (suiteGroupType != SuiteGroupType.NEW_PROGRAM) {
            createFilterControls();
        }

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

                if (btn.getSelection() == false) {
                    return;
                }

                if (btn.getData() != null) {
                    suiteCatType = (SuiteType) btn.getData();
                } else {
                    suiteCatType = null;
                }
                populateSuiteTable(false);

                if (suiteSelectionCB != null) {
                    int[] selIdices = suiteTable.getSelectedIndices();
                    if (selIdices.length > 0) {
                        suiteSelectionCB.suiteSelected(filteredSuiteList
                                .get(selIdices[0]));
                    }
                }
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
        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI;
        suiteTable = new SuiteTable(suiteGroup, tableStyle, desiredNumRows);
        suiteTable.setMultipleSelection(false);
        suiteTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    enableControls(true);
                    if (suiteSelectionCB != null) {
                        suiteSelectionCB.suiteSelected(filteredSuiteList
                                .get(suiteTable.getSelectedIndices()[0]));
                    }
                } else {
                    enableControls(false);
                    suiteSelectionCB.suiteSelected(null);
                }
                updateSelectedSuite();
            }
        });
    }

    /**
     * Create suite controls.
     */
    private void createSuiteControls() {

        int outerColumns = 2;
        int innerColumns = 0;

        /*
         * If the suite group type is BROADCAST_PROGRAM then show the following
         * controls: New, Add Existing, Edit, and Delete.
         * 
         * If the suite group type is SUITE_MGR then show the following
         * controls: New, Copy, Rename, Edit, Delete, and Relationship.
         * 
         * If the suite group type is ADD_COPY_EXITING then only show the
         * Relationship button.
         */
        if (suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM) {
            innerColumns = 4;
            outerColumns = 1;
        } else if (suiteGroupType == SuiteGroupType.NEW_PROGRAM) {
            innerColumns = 3;
            outerColumns = 1;
        } else if (suiteGroupType == SuiteGroupType.SUITE_MGR) {
            innerColumns = 5;
        }

        Composite suiteControlComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(outerColumns, false);
        suiteControlComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteControlComp.setLayoutData(gd);

        Composite onlyButtonsComposite = null;
        if (innerColumns != 0) {
            onlyButtonsComposite = new Composite(suiteControlComp, SWT.NONE);
            gl = new GridLayout(innerColumns, true);
            gl.marginWidth = 0;
            onlyButtonsComposite.setLayout(gl);
            gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
            onlyButtonsComposite.setLayoutData(gd);
        }

        final int buttonMinimumWidth = getShell().getDisplay().getDPI().x;
        if (onlyButtonsComposite != null
                && suiteGroupType != SuiteGroupType.ADD_COPY_EXITING) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button addNewSuiteBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            addNewSuiteBtn.setText("New...");
            addNewSuiteBtn.setLayoutData(gd);
            addNewSuiteBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean showProgramControls = (suiteGroupType == SuiteGroupType.SUITE_MGR);

                    Set<String> existingNames = null;

                    if (suiteSelectionCB != null) {
                        existingNames = suiteSelectionCB.getSuiteNames();
                    } else {
                        existingNames = new HashSet<String>();
                    }

                    CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                            .getShell(), DialogType.CREATE,
                            showProgramControls, selectedProgram, null,
                            existingNames);
                    csd.setCloseCallback(new ICloseCallback() {
                        @Override
                        public void dialogClosed(Object returnValue) {

                            if (returnValue instanceof Suite) {
                                if (suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM
                                        || suiteGroupType == SuiteGroupType.NEW_PROGRAM) {
                                    List<Suite> array = new ArrayList<Suite>();
                                    array.add((Suite) returnValue);
                                    suiteSelectionCB.addedSuites(array);
                                } else if (suiteGroupType == SuiteGroupType.SUITE_MGR) {
                                    if (selectedSuite != null) {
                                        suiteSelectionCB
                                                .suitesUpdated((Suite) returnValue);
                                    }
                                }
                            }
                        }
                    });
                    csd.open();
                }
            });
        }

        if (onlyButtonsComposite != null
                && suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM
                || suiteGroupType == SuiteGroupType.NEW_PROGRAM) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button addExistingBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            addExistingBtn.setText("Add Existing...");
            addExistingBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    /*
                     * Get the existing suite names to check against so that
                     * duplicate suites will not be added and to allow only one
                     * suite with type GENERAL.
                     */

                    AddSuitesDlg asd = new AddSuitesDlg(getShell(), suiteList);
                    asd.setCloseCallback(new ICloseCallback() {
                        @Override
                        public void dialogClosed(Object returnValue) {
                            try {
                                if ((returnValue != null)
                                        && ((suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM) || (suiteGroupType == SuiteGroupType.NEW_PROGRAM))) {
                                    AddSuitesDlg.AddSuitesResults results = (AddSuitesDlg.AddSuitesResults) returnValue;
                                    List<Suite> suites = results.getSuites();
                                    Suite oldGeneralSuite = results
                                            .getOldGeneralSuite();

                                    if (oldGeneralSuite != null) {
                                        // Remove old GENERAL Suite.
                                        if (selectedProgram != null) {
                                            selectedProgram
                                                    .removeSuite(oldGeneralSuite);
                                        } else {
                                            suiteSelectionCB
                                                    .deleteSuite(oldGeneralSuite);
                                        }
                                    }
                                    existingSuites.addAll(suites);
                                    suiteSelectionCB.addedSuites(suites);
                                }
                            } catch (ClassCastException ex) {
                                statusHandler
                                        .handle(Priority.ERROR,
                                                "Dialog returned results in the wrong format. ",
                                                ex);
                            }
                        }
                    });
                    asd.open();
                }
            });
        } else if (onlyButtonsComposite != null
                && suiteGroupType == SuiteGroupType.SUITE_MGR) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button copyBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            copyBtn.setText("Copy...");
            copyBtn.setLayoutData(gd);
            copyBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (selectedSuite != null) {
                        suiteSelectionCB.copySuite(selectedSuite);
                    }
                }
            });
            suiteControls.add(copyBtn);

            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button renameSuiteBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            renameSuiteBtn.setText("Rename...");
            renameSuiteBtn.setLayoutData(gd);
            renameSuiteBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (selectedSuite != null) {
                        suiteSelectionCB.renameSuite(selectedSuite);
                    }
                }
            });
            suiteControls.add(renameSuiteBtn);
        }

        if (onlyButtonsComposite != null
                && (suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM || suiteGroupType == SuiteGroupType.SUITE_MGR)) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button editSuiteBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            editSuiteBtn.setText("Edit...");
            editSuiteBtn.setLayoutData(gd);
            editSuiteBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean showProgramControls = (suiteGroupType == SuiteGroupType.SUITE_MGR);
                    CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                            .getShell(), DialogType.EDIT, showProgramControls,
                            selectedProgram, selectedSuite, null);

                    csd.setCloseCallback(new ICloseCallback() {
                        @Override
                        public void dialogClosed(Object returnValue) {

                            if (returnValue != null
                                    && returnValue instanceof Suite) {

                                if (suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM) {
                                    selectedSuite = (Suite) returnValue;
                                    suiteSelectionCB
                                            .suitesUpdated((Suite) returnValue);
                                } else if (suiteGroupType == SuiteGroupType.SUITE_MGR) {
                                    if (selectedSuite != null) {
                                        suiteSelectionCB
                                                .suitesUpdated((Suite) returnValue);
                                    }
                                }
                            }
                        }
                    });

                    csd.open();
                }
            });
            suiteControls.add(editSuiteBtn);
        }

        if (onlyButtonsComposite != null
                && (suiteGroupType == SuiteGroupType.BROADCAST_PROGRAM
                        || suiteGroupType == SuiteGroupType.SUITE_MGR || suiteGroupType == SuiteGroupType.NEW_PROGRAM)) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.minimumWidth = buttonMinimumWidth;
            Button deleteSuiteBtn = new Button(onlyButtonsComposite, SWT.PUSH);
            if (suiteGroupType != SuiteGroupType.BROADCAST_PROGRAM) {
                deleteSuiteBtn.setText("Delete...");
            } else {
                deleteSuiteBtn.setText("Remove...");
            }
            deleteSuiteBtn.setLayoutData(gd);
            deleteSuiteBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (selectedSuite != null) {
                        existingSuites.remove(selectedSuite);
                        suiteSelectionCB.deleteSuite(selectedSuite);
                    }
                }
            });
            suiteControls.add(deleteSuiteBtn);
        }

        if (suiteGroupType == SuiteGroupType.SUITE_MGR
                || suiteGroupType == SuiteGroupType.ADD_COPY_EXITING) {
            /*
             * Relationship button
             */
            ImageDescriptor id;
            id = AbstractUIPlugin.imageDescriptorFromPlugin(
                    Activator.PLUGIN_ID, "icons/Relationship.png");
            relationshipImg = id.createImage();

            Button relationshipBtn = new Button(suiteControlComp, SWT.PUSH);
            relationshipBtn.setImage(relationshipImg);
            relationshipBtn.setToolTipText("View Suite relationships");
            relationshipBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int selectedIndex = suiteTable.getSelectedIndex();

                    if (selectedIndex < 0) {
                        return;
                    }

                    Suite selectSuite = filteredSuiteList.get(selectedIndex);
                    ViewSuiteDlg vsd = new ViewSuiteDlg(parentComp.getShell(),
                            selectSuite);
                    vsd.open();
                }
            });

            if (suiteGroupType == SuiteGroupType.ADD_COPY_EXITING) {
                gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
            } else {
                gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
            }
            relationshipBtn.setLayoutData(gd);

            suiteControls.add(relationshipBtn);
        }
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

        return filteredSuiteList.get(suiteTable.getSelectedIndex());
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
     * Select the suite in the table based on suite ID. If the suite ID is not
     * found then select the first suite.
     * 
     * @param suiteId
     *            suite ID.
     */
    public void selectSuiteInTable(int suiteId) {
        suiteTable.deselectAll();

        int index = 0;
        for (Suite s : filteredSuiteList) {
            if (s.getId() == suiteId) {
                suiteTable.select(index);
                updateSelectedSuite();
                return;
            }
            ++index;
        }

        suiteTable.select(0);
        updateSelectedSuite();
    }

    /**
     * Update what suite has been selected.
     */
    private void updateSelectedSuite() {
        if (suiteTable.getSelectedIndex() >= 0) {
            selectedSuite = filteredSuiteList
                    .get(suiteTable.getSelectedIndex());
        } else {
            selectedSuite = null;
        }
    }

    /**
     * Set the table to be multiple selection (true) or single selection
     * (false). This will only work if the table is initialized with a style of
     * SWT.MULTI.
     * 
     * @param multipleSelection
     *            True for multiple selection, false for single.
     */
    public void setMultipleSelection(boolean multipleSelection) {
        suiteTable.setMultipleSelection(multipleSelection);
    }

    /**
     * Get the selected suites in the table.
     * 
     * @return List of selected suites.
     */
    public List<Suite> getSelectedSuites() {
        List<Suite> selectedSuites = new ArrayList<Suite>();

        int[] indices = suiteTable.getSelectedIndices();

        for (int i = 0; i < indices.length; i++) {
            selectedSuites.add(filteredSuiteList.get(indices[i]));
        }

        return selectedSuites;
    }

    /**
     * Initialize the tables columns. This is used for situations when the table
     * starts off empty and the columns need to be present. This can only be
     * done once.
     */
    public void initializeTableColumns() {
        if (suiteTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Suite Name", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Category");
            columnNames.add(tcd);

            suiteTableData = new TableData(columnNames);
            suiteTable.populateTable(suiteTableData);
        }
    }

    /**
     * Populate the suite table using the list of suites.
     * 
     * @param suiteList
     *            List of suites.
     */
    public void populateSuiteTable(List<Suite> suiteList,
            boolean replaceTableItems) {
        this.suiteList = suiteList;
        populateSuiteTable(replaceTableItems);
    }

    /**
     * Populate the Suite table.
     */
    private void populateSuiteTable(boolean replaceTableItems) {

        if (suiteTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Suite Name", 250);
            columnNames.add(tcd);
            tcd = new TableColumnData("Category");
            columnNames.add(tcd);

            suiteTableData = new TableData(columnNames);
        } else {
            suiteTableData.deleteAllRows();
        }

        populateSuiteTableData();

        if (replaceTableItems) {
            suiteTable.replaceTableItems(suiteTableData);
        } else {
            suiteTable.populateTable(suiteTableData);
        }

        if (suiteTable.getItemCount() > 0) {
            if (!replaceTableItems) {
                suiteTable.select(0);
            }
            updateSelectedSuite();
        }
        enableSuiteControls();
    }

    /**
     * Populate the suite table data.
     */
    private void populateSuiteTableData() {

        if (suiteList == null) {
            return;
        }

        filteredSuiteList.clear();

        for (Suite suite : suiteList) {
            if (suiteCatType == null || suite.getType() == suiteCatType) {
                filteredSuiteList.add(suite);
                TableRowData trd = new TableRowData();

                trd.addTableCellData(new TableCellData(suite.getName()));
                trd.addTableCellData(new TableCellData(suite.getType().name()));

                suiteTableData.addDataRow(trd);
            }
        }
    }
}
