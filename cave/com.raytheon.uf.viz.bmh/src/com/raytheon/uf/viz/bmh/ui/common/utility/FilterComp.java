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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.widgets.DateTimeSpinner;

/**
 * Composite containing filter controls for text and date.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2014  3833      lvenable     Initial creation
 * May 03, 2016  5602      bkowal       Use {@link DateTimeSpinner}.
 * May 04, 2016  5602      bkowal       Utilizing {@link DateTimeSpinner} enhancements.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class FilterComp extends Composite {

    /** Choice for filtering text. */
    public enum TextFilterChoice {
        STARTS_WITH("Starts With"), ENDS_WITH("Ends With"), CONTAINS("Contains");

        private String choice;

        TextFilterChoice(String choiceStr) {
            choice = choiceStr;
        }

        public String getChoice() {
            return choice;
        }
    }

    /** Choice for filtering the date. */
    public enum DateFilterChoice {
        ALL("All"), BEFORE("Before"), AFTER("After"), RANGE("Range");

        private String choice;

        DateFilterChoice(String choiceStr) {
            choice = choiceStr;
        }

        public String getChoice() {
            return choice;
        }
    }

    /** Filter text control. */
    private Text filterText;

    /** Text filter choice combo box. */
    private Combo textFilterChoiceCbo;

    /** Date filter choice combo box. */
    private Combo dateFilterChoiceCbo;

    /** Case sensitive check box. */
    private Button caseSensitiveChk;

    /** Filter action method called when the data needs to be filtered. */
    private IFilterAction filterAction = null;

    /** Widget used to set the start date to use. **/
    private DateTimeSpinner startDateSpinner;

    /** Widget used to set the end date to use. **/
    private DateTimeSpinner endDateSpinner;

    /** "to" label used between the start and end date controls. */
    private Label toLbl;

    /** Filter button. */
    private Button filterBtn;

    /** Label displaying the date filter choice. */
    private Label dateFilterChoiceLbl;

    /**
     * Text pattern for filtering. Allows upper/lower case letters, digits,
     * period, underscores, and dashes.
     */
    Pattern textPattern = Pattern.compile("[A-Za-z0-9._-]+");

    /**
     * Using this constructor will set the date time format to be: yyyy MM dd
     * HH:mm:ss
     * 
     * @param parentComp
     *            Parent composite.
     * @param displayDateRange
     *            Flag to display the date range.
     * @param filterAction
     *            Action taken when filtering.
     */
    public FilterComp(Composite parentComp, boolean displayDateRange,
            IFilterAction filterAction) {
        this(parentComp, displayDateRange, null, filterAction);
    }

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param displayDateRange
     *            Flag to display the date range.
     * @param sdf
     *            Date/Time format for the date/time fields. If null, the
     *            default is yyyy MM dd HH:mm:ss
     * @param filterAction
     *            Action taken when filtering.
     */
    public FilterComp(Composite parentComp, boolean displayDateRange,
            SimpleDateFormat sdf, IFilterAction filterAction) {
        super(parentComp, SWT.NONE);

        this.filterAction = filterAction;
        init(displayDateRange);
    }

    /**
     * Initialize method.
     * 
     * @param displayDateRange
     *            Flag indicating if the date controls should be displayed.
     */
    private void init(boolean displayDateRange) {
        GridLayout gl = new GridLayout(1, false);
        this.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.setLayoutData(gd);

        Group filterGroup = new Group(this, SWT.SHADOW_OUT);
        gl = new GridLayout(1, false);
        filterGroup.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        filterGroup.setLayoutData(gd);
        filterGroup.setText(" Filter Controls: ");

        createTextFilterControls(filterGroup);
        if (displayDateRange) {
            createDateFilterControls(filterGroup);
        }
        DialogUtility.addSeparator(filterGroup, SWT.HORIZONTAL);
        createFilterButton(filterGroup);

        updateDateControls();
    }

    /**
     * Create the text filter controls for filtering text.
     * 
     * @param filterGroup
     *            Group container.
     */
    private void createTextFilterControls(Group filterGroup) {
        Composite filterControlComp = new Composite(filterGroup, SWT.NONE);
        filterControlComp.setLayout(new GridLayout(4, false));
        filterControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                true, false));

        Label filterLbl = new Label(filterControlComp, SWT.NONE);
        filterLbl.setText("Name: ");

        /*
         * Filter text control.
         */
        GridData gd = new GridData(200, SWT.DEFAULT);
        filterText = new Text(filterControlComp, SWT.BORDER | SWT.SEARCH
                | SWT.ICON_CANCEL);
        filterText.setLayoutData(gd);

        /*
         * Combo box with the text filtering choices.
         */
        gd = new GridData();
        gd.minimumWidth = 70;
        gd.horizontalIndent = 15;
        textFilterChoiceCbo = new Combo(filterControlComp, SWT.DROP_DOWN
                | SWT.READ_ONLY);
        textFilterChoiceCbo.setLayoutData(gd);

        for (TextFilterChoice fc : TextFilterChoice.values()) {
            textFilterChoiceCbo.add(fc.getChoice());
        }
        textFilterChoiceCbo.select(0);

        /*
         * Case Sensitive check box.
         */
        gd = new GridData();
        gd.horizontalIndent = 15;
        caseSensitiveChk = new Button(filterControlComp, SWT.CHECK);
        caseSensitiveChk.setText("Case Sensitive");
    }

    /**
     * Create the date filter controls.
     * 
     * @param filterGroup
     *            Group container.
     */
    private void createDateFilterControls(Group filterGroup) {
        Composite filterControlComp = new Composite(filterGroup, SWT.NONE);
        filterControlComp.setLayout(new GridLayout(6, false));
        filterControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                true, false));

        /*
         * Date label.
         */
        Label dateLbl = new Label(filterControlComp, SWT.NONE);
        dateLbl.setText("Creation Date: ");

        /*
         * Combo box of date choices.
         */
        GridData gd = new GridData();
        gd.widthHint = 110;
        dateFilterChoiceCbo = new Combo(filterControlComp, SWT.DROP_DOWN
                | SWT.READ_ONLY);
        dateFilterChoiceCbo.setLayoutData(gd);
        dateFilterChoiceCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDateControls();
            }
        });

        for (DateFilterChoice fc : DateFilterChoice.values()) {
            dateFilterChoiceCbo.add(fc.getChoice());
        }
        dateFilterChoiceCbo.select(0);

        /*
         * Date label for filter choice. This will describe how the date
         * controls will be used.
         */
        gd = new GridData(80, SWT.DEFAULT);
        gd.horizontalIndent = 15;
        dateFilterChoiceLbl = new Label(filterControlComp, SWT.RIGHT);
        dateFilterChoiceLbl.setLayoutData(gd);

        final Calendar initialDate = TimeUtil.newGmtCalendar();

        /*
         * Text control for before, after, and start date for range.
         */
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        /*
         * Need to keep a reference to the date/time passed to the constructor
         * because the current implementation only supports returning a String
         * representation of the currently selected date/time - DR #5602.
         */
        startDateSpinner = new DateTimeSpinner(filterControlComp, initialDate,
                6);
        startDateSpinner.setLayoutData(gd);
        startDateSpinner.setEnabled(false);

        toLbl = new Label(filterControlComp, SWT.NONE);
        toLbl.setText("  to  ");

        /*
         * Text control for the end date for range.
         */
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        endDateSpinner = new DateTimeSpinner(filterControlComp, initialDate, 6);
        endDateSpinner.setLayoutData(gd);
        endDateSpinner.setEnabled(false);
    }

    /**
     * Create the filter and clear buttons.
     * 
     * @param filterGroup
     *            Filter group.
     */
    private void createFilterButton(Group filterGroup) {
        Composite buttonComp = new Composite(filterGroup, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = 90;
        filterBtn = new Button(buttonComp, SWT.PUSH);
        filterBtn.setText("Filter");
        filterBtn.setLayoutData(gd);
        filterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Calendar startDate = startDateSpinner.getSelection();
                Calendar endDate = endDateSpinner.getSelection();

                if (validateChoices(startDate, endDate)) {
                    FilterData filterData = new FilterData(
                            getSelectedTextFilterChoice(),
                            getSelectedDateFilterChoice());
                    filterData.setFilterText(filterText.getText().trim());
                    filterData.setCaseSensitive(caseSensitiveChk.getSelection());

                    filterData.setStartDate(startDate.getTime());
                    filterData.setEndDate(endDate.getTime());

                    filterAction.filterAction(filterData);
                }
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = 90;
        Button clearBtn = new Button(buttonComp, SWT.PUSH);
        clearBtn.setText("Clear");
        clearBtn.setLayoutData(gd);
        clearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                filterText.setText("");
                filterAction.clearFilter();
            }
        });
    }

    /**
     * Update the date controls depending on the filter choice selected.
     */
    private void updateDateControls() {
        DateFilterChoice dfc = getSelectedDateFilterChoice();

        if (dfc == DateFilterChoice.ALL) {
            dateFilterChoiceLbl.setText("");
            startDateSpinner.setEnabled(false);
            toLbl.setEnabled(false);
            endDateSpinner.setEnabled(false);
        } else if (dfc == DateFilterChoice.AFTER
                || dfc == DateFilterChoice.BEFORE) {
            dateFilterChoiceLbl.setText(dfc.getChoice() + ": ");
            startDateSpinner.setEnabled(true);
            toLbl.setEnabled(false);
            endDateSpinner.setEnabled(false);
        } else if (dfc == DateFilterChoice.RANGE) {
            dateFilterChoiceLbl.setText(dfc.getChoice() + ": ");
            startDateSpinner.setEnabled(true);
            toLbl.setEnabled(true);
            endDateSpinner.setEnabled(true);
        }
    }

    /**
     * Get the date filter choice that has been selected.
     * 
     * @return The date filter choice.
     */
    private DateFilterChoice getSelectedDateFilterChoice() {
        DateFilterChoice dfc = DateFilterChoice.ALL;

        String dateChoiceStr = dateFilterChoiceCbo.getItem(dateFilterChoiceCbo
                .getSelectionIndex());

        for (DateFilterChoice choice : DateFilterChoice.values()) {
            if (choice.getChoice().equals(dateChoiceStr)) {
                dfc = choice;
                break;
            }
        }

        return dfc;
    }

    /**
     * Get the text filter choice that has been selected.
     * 
     * @return The text filter choice.
     */
    private TextFilterChoice getSelectedTextFilterChoice() {
        TextFilterChoice tfc = TextFilterChoice.CONTAINS;

        String textChoiceStr = textFilterChoiceCbo.getItem(textFilterChoiceCbo
                .getSelectionIndex());

        for (TextFilterChoice choice : TextFilterChoice.values()) {
            if (choice.getChoice().equals(textChoiceStr)) {
                tfc = choice;
                break;
            }
        }

        return tfc;
    }

    private boolean validateChoices(Calendar startDate, Calendar endDate) {
        if (filterText.getText().trim().length() != 0) {
            if (textPattern.matcher(filterText.getText()).matches() == false) {
                DialogUtility
                        .showMessageBox(
                                getShell(),
                                SWT.ICON_WARNING | SWT.OK,
                                "Filter Text Error",
                                "The text should contain alphanumeric characters, numbers, periods, dashes, or underscores.  No spaces.");
                return false;
            }
        }

        if (getSelectedDateFilterChoice() == DateFilterChoice.RANGE) {
            if (startDate.equals(endDate)) {
                DialogUtility
                        .showMessageBox(
                                getShell(),
                                SWT.ICON_WARNING | SWT.OK,
                                "Filter Date Error",
                                "The Range of dates/times are the same.  Please enter in two different dates/times.");
                return false;
            }

            if (startDate.after(endDate)) {
                DialogUtility
                        .showMessageBox(getShell(), SWT.ICON_ERROR | SWT.OK,
                                "Input Message Filter",
                                "Invalid date/time range. Start Date must be before End Date.");
                return false;
            }
        }

        return true;
    }
}
