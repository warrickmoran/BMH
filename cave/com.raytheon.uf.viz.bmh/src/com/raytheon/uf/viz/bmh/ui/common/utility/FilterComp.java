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
import java.util.Date;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.viz.ui.dialogs.AwipsCalendar;

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

    /** Text field that displayed the start date. */
    private Text startDateText;

    /** Text field that displayed the end date. */
    private Text endDateText;

    /** Start date. */
    private Date startDate = TimeUtil.newGmtCalendar().getTime();

    /** Change start date button. */
    private Button changeStartDateBtn;

    /** End date. */
    private Date endDate = TimeUtil.newGmtCalendar().getTime();

    /** Change end date button. */
    private Button changeEndDateBtn;

    /** "to" label used between the start and end date controls. */
    private Label toLbl;

    /** Filter button. */
    private Button filterBtn;

    /** Label displaying the date filter choice. */
    private Label dateFilterChoiceLbl;

    /** Simple date format. */
    private SimpleDateFormat sdf;

    /**
     * Text pattern for filtering. Allows upper/lower case letters, digits,
     * period, underscores, and dashes.
     */
    Pattern textPattern = Pattern.compile("[A-Za-z0-9._-]+");

    /**
     * The calendar icon
     */
    private Image calendarIcon;

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

        if (sdf == null) {
            this.sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        } else {
            this.sdf = sdf;
        }

        init(displayDateRange);
    }

    /**
     * Initialize method.
     * 
     * @param displayDateRange
     *            Flag indicating if the date controls should be displayed.
     */
    private void init(boolean displayDateRange) {

        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (calendarIcon != null) {
                    calendarIcon.dispose();
                }
            }
        });

        loadCalendarImage();

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
        updateTextDateControls();
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
        filterLbl.setText("Filter: ");

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
        filterControlComp.setLayout(new GridLayout(8, false));
        filterControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                true, false));

        /*
         * Date label.
         */
        Label dateLbl = new Label(filterControlComp, SWT.NONE);
        dateLbl.setText("Date: ");

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

        /*
         * Text control for before, after, and start date for range.
         */
        gd = new GridData(180, SWT.DEFAULT);
        startDateText = new Text(filterControlComp, SWT.BORDER);
        startDateText.setEditable(false);
        startDateText.setLayoutData(gd);

        changeStartDateBtn = new Button(filterControlComp, SWT.PUSH);
        changeStartDateBtn.setImage(calendarIcon);
        changeStartDateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date d = displayCalendarDialog(startDate);
                if (d != null) {
                    startDate = d;
                    setTextControlDate(d, startDateText);
                }
            }
        });

        toLbl = new Label(filterControlComp, SWT.NONE);
        toLbl.setText("  to  ");

        /*
         * Text control for the end date for range.
         */
        gd = new GridData(180, SWT.DEFAULT);
        endDateText = new Text(filterControlComp, SWT.BORDER);
        endDateText.setEditable(false);
        endDateText.setLayoutData(gd);

        changeEndDateBtn = new Button(filterControlComp, SWT.PUSH);
        changeEndDateBtn.setImage(calendarIcon);
        changeEndDateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date d = displayCalendarDialog(endDate);
                if (d != null) {
                    endDate = d;
                    setTextControlDate(d, endDateText);
                }
            }
        });
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
                if (validateChoices()) {
                    FilterData filterData = new FilterData(
                            getSelectedTextFilterChoice(),
                            getSelectedDateFilterChoice());
                    filterData.setFilterText(filterText.getText().trim());
                    filterData.setCaseSensitive(caseSensitiveChk.getSelection());

                    if (startDate.after(endDate)
                            && getSelectedDateFilterChoice() == DateFilterChoice.RANGE) {
                        filterData.setStartDate(endDate);
                        filterData.setEndDate(startDate);
                    } else {
                        filterData.setStartDate(startDate);
                        filterData.setEndDate(endDate);
                    }

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
     * Load the calendar icon
     */
    private void loadCalendarImage() {
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/calendar-16.png");
        calendarIcon = id.createImage();

    }

    /**
     * Set the date/time in the text control passed in.
     * 
     * @param date
     *            The date/time.
     * @param textControl
     *            Text control.
     */
    private void setTextControlDate(Date date, Text textControl) {
        textControl.setText(sdf.format(date));
    }

    /**
     * Update the date controls depending on the filter choice selected.
     */
    private void updateDateControls() {
        DateFilterChoice dfc = getSelectedDateFilterChoice();

        if (dfc == DateFilterChoice.ALL) {
            dateFilterChoiceLbl.setText("");
            startDateText.setEnabled(false);
            changeStartDateBtn.setEnabled(false);
            toLbl.setEnabled(false);
            endDateText.setEnabled(false);
            changeEndDateBtn.setEnabled(false);
        } else if (dfc == DateFilterChoice.AFTER
                || dfc == DateFilterChoice.BEFORE) {
            dateFilterChoiceLbl.setText(dfc.getChoice() + ": ");
            startDateText.setEnabled(true);
            changeStartDateBtn.setEnabled(true);
            toLbl.setEnabled(false);
            endDateText.setEnabled(false);
            changeEndDateBtn.setEnabled(false);
        } else if (dfc == DateFilterChoice.RANGE) {
            dateFilterChoiceLbl.setText(dfc.getChoice() + ": ");
            startDateText.setEnabled(true);
            changeStartDateBtn.setEnabled(true);
            toLbl.setEnabled(true);
            endDateText.setEnabled(true);
            changeEndDateBtn.setEnabled(true);
        }
    }

    /**
     * Update the text date controls with the data in the start and end dates.
     */
    private void updateTextDateControls() {
        startDateText.setText(sdf.format(zeroSecondsMilliseconds(startDate)));
        endDateText.setText(sdf.format(zeroSecondsMilliseconds(endDate)));
    }

    /**
     * Display the AWIPS calendar dialog.
     * 
     * @param initialDate
     *            Date to set the calendar to.
     * @return
     */
    private Date displayCalendarDialog(Date initialDate) {

        if (initialDate == null) {
            initialDate = TimeUtil.newDate();
        }

        AwipsCalendar ac = new AwipsCalendar(getParent().getShell(),
                initialDate, 2);
        Date date = (Date) ac.open();

        if (date != null) {
            date = zeroSecondsMilliseconds(date);
        }

        return date;
    }

    /**
     * Set the seconds and milliseconds to zero on the date passed in.
     * 
     * @param date
     *            Date.
     * @return Date with the seconds and milliseconds zeroed out.
     */
    private Date zeroSecondsMilliseconds(Date date) {
        Calendar c = TimeUtil.newCalendar(date);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        date = c.getTime();

        return date;
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

    private boolean validateChoices() {

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
        }

        return true;
    }
}
