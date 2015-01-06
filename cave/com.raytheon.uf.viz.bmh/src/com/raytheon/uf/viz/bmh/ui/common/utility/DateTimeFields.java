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

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.dialogs.AwipsCalendar;

/**
 * Composite holding spinner fields for date/time and periodicity
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 3, 2014    3431     mpduff      Initial creation
 * Jul 29, 2014  #3420     lvenable    Added capability to enable/disable controls and to
 *                                     retrieve the data from the controls.
 * Aug 12, 2014  #3490     lvenable    Refactored code and added more functionality.
 * Aug 15, 2014   3411     mpduff      Add getFormattedValue()
 * Oct 13, 2014  #3728     lvenable    Fixed zero-based month in the spinner and added a safety
 *                                     check for periodicity.
 * Oct 18, 2014  #3728     lvenable    Added capability to set the spinner values using Calendar or
 *                                     by individual field.
 * Oct 26, 2014  #3712     bkowal      Fixed how hours are handled. Added a method to update multiple
 *                                     dtf fields at once and function to return mapping to java Calendar
 *                                     fields.
 * Oct 27, 2014  #3712     bkowal      Fixed how hours are handled when retrieving data
 *                                     from the dtf spinners.
 * Nov 01, 2014   3784     mpduff      Added getBackingCalendar()
 * Dec 09, 2014   3892     bkowal      Listen for spinner modifications instead of selections.
 *                                     Listening for modifications handles the case where
 *                                     the time is set programatically in addition to when
 *                                     the user manually alters the time.
 * Jan 02, 2014   3833     lvenable    Removed the calendar icon since it was never used.  Can be put
 *                                     back in later if needed.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DateTimeFields extends Composite {

    /**
     * Types of fields allowed
     */
    public enum DateFieldType {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND;
    }

    /**
     * Map of {@link DateFieldType} -> {@link Spinner}
     */
    private final Map<DateFieldType, Spinner> spinners = new LinkedHashMap<DateFieldType, Spinner>();

    /** Map of DateTimeFields and assigned values. */
    private final Map<DateFieldType, Integer> fieldValuesMap;

    /** Map of Date/Time field types and the associated calendar type. */
    private Map<DateFieldType, Integer> dfTypeToCalMap;

    /**
     * Default to current time
     */
    private final boolean setToday;

    /**
     * Show the Calendar icon
     */
    private final boolean showCalendarIcon;

    /**
     * {@link Calendar} object backing this dialog
     */
    private Calendar calendar = TimeUtil.newGmtCalendar();

    /**
     * Display flag, true to display periodicity, false otherwise
     */
    private final boolean displayAsPeriodicity;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite
     * @param fieldValuesMap
     *            Map of field types and assigned values.
     * @param setToday
     *            set current time flag
     * @param showCalendarIcon
     *            show calendar icon flag
     * @param displayAsPeriodicity
     *            display as periodicity flag
     */
    public DateTimeFields(Composite parent,
            Map<DateFieldType, Integer> fieldValuesMap, boolean setToday,
            boolean showCalendarIcon, boolean displayAsPeriodicity) {
        super(parent, SWT.NONE);
        this.fieldValuesMap = fieldValuesMap;
        this.setToday = setToday;
        this.showCalendarIcon = showCalendarIcon;

        /*
         * If there is a month and/or year field present int the fieldValuesMap
         * then periodicity should not be true. This is a safety check to make
         * sure the controls operate correctly.
         */
        if ((this.fieldValuesMap.containsKey(DateFieldType.MONTH) || this.fieldValuesMap
                .containsKey(DateFieldType.MONTH)) && (displayAsPeriodicity)) {
            this.displayAsPeriodicity = false;
        } else {
            this.displayAsPeriodicity = displayAsPeriodicity;
        }

        init();
    }

    /**
     * Initialize the gui
     */
    private void init() {
        populateDataFieldToCalendar();

        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        if (showCalendarIcon) {
            gl = new GridLayout(fieldValuesMap.size() + 1, false);
        } else {
            gl = new GridLayout(fieldValuesMap.size(), false);
        }
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Composite fieldComp = new Composite(this, SWT.NONE);
        fieldComp.setLayout(gl);
        fieldComp.setLayoutData(gd);

        createSpinners(fieldComp);

        if (setToday) {
            setToCurrentTime();
        } else {
            setToSpecifiedTime();
        }

        if (showCalendarIcon) {
            Button calBtn = new Button(fieldComp, SWT.PUSH);
            calBtn.setText("Calendar...");
            calBtn.setLayoutData(new GridData(55, SWT.DEFAULT));
            calBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // TODO - implement this correctly when run inside cave
                    new AwipsCalendar(getShell());

                }
            });
        }
    }

    /**
     * Create the spinners
     * 
     * @param c
     *            The parent composite for the spinners
     */
    private void createSpinners(Composite c) {
        for (DateFieldType type : fieldValuesMap.keySet()) {
            Spinner spnr = new Spinner(c, SWT.BORDER);
            if (type == DateFieldType.YEAR) {
                spnr.setLayoutData(new GridData(33, SWT.DEFAULT));
                spnr.setTextLimit(4);
            } else {
                spnr.setLayoutData(new GridData(17, SWT.DEFAULT));
                spnr.setTextLimit(2);
            }
            spnr.setData(type);

            spinners.put(type, spnr);

            switch (type) {
            case HOUR:
                spnr.setMinimum(0);
                spnr.setMaximum(23);
                break;
            case MINUTE:
            case SECOND:
                spnr.setMinimum(0);
                spnr.setMaximum(59);
                break;
            case YEAR:
                spnr.setMaximum(2200);
                break;
            case MONTH:
                spnr.setMaximum(12);
                spnr.setMinimum(1);
                break;
            case DAY:

                if (displayAsPeriodicity) {
                    spnr.setMinimum(0);
                    spnr.setMaximum(99);
                } else {
                    spnr.setMinimum(1);
                    spnr.setMaximum(31);
                }
                break;
            }
            spnr.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Spinner src = (Spinner) e.getSource();
                    spinnerModifiedAction(src);
                }
            });
        }
    }

    /**
     * Set to current time
     */
    private void setToCurrentTime() {
        calendar = TimeUtil.newGmtCalendar();

        for (DateFieldType dft : DateFieldType.values()) {
            if (spinners.containsKey(dft)) {
                if (displayAsPeriodicity && dft == DateFieldType.DAY) {
                    spinners.get(dft).setSelection(0);
                } else {
                    spinners.get(dft).setSelection(
                            calendar.get(dfTypeToCalMap.get(dft)));
                }
            }
        }
    }

    /**
     * Set the fields to the specified time provided for the field type.
     */
    private void setToSpecifiedTime() {
        calendar = TimeUtil.newGmtCalendar();

        for (DateFieldType dft : DateFieldType.values()) {
            if (spinners.containsKey(dft)) {
                if (fieldValuesMap.get(dft) != null) {
                    spinners.get(dft).setSelection(fieldValuesMap.get(dft));
                } else {

                    if (displayAsPeriodicity && dft == DateFieldType.DAY) {
                        spinners.get(dft).setSelection(0);
                    } else {
                        int value = calendar.get(dfTypeToCalMap.get(dft));
                        if (dft == DateFieldType.MONTH) {
                            value += 1;
                        }
                        spinners.get(dft).setSelection(value);
                    }
                }
            }
        }
    }

    /**
     * Spinner modification action handler
     * 
     * @param spinner
     *            The modified Spinner
     */
    private void spinnerModifiedAction(Spinner spinner) {
        for (DateFieldType type : fieldValuesMap.keySet()) {
            switch (type) {
            case DAY:
                if (this.displayAsPeriodicity
                        || !fieldValuesMap.containsKey(DateFieldType.MONTH)) {
                    calendar.set(Calendar.DAY_OF_MONTH, spinners.get(type)
                            .getSelection());
                } else {
                    // This case handled under month
                }
                break;
            case HOUR:
                calendar.set(Calendar.HOUR_OF_DAY, spinners.get(type)
                        .getSelection());
                break;
            case MINUTE:
                calendar.set(Calendar.MINUTE, spinners.get(type).getSelection());
                break;
            case MONTH:
                calendar.set(Calendar.YEAR, spinners.get(DateFieldType.YEAR)
                        .getSelection());
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.MONTH, spinners.get(DateFieldType.MONTH)
                        .getSelection() - 1);
                int daysInMonth = calendar
                        .getActualMaximum(Calendar.DAY_OF_MONTH);

                Spinner daySpinner = spinners.get(DateFieldType.DAY);
                if (daySpinner != null) {
                    daySpinner.setMaximum(daysInMonth);
                    if (daySpinner.getSelection() > daysInMonth) {
                        daySpinner.setSelection(daysInMonth);
                    }

                    calendar.set(Calendar.DAY_OF_MONTH,
                            spinners.get(DateFieldType.DAY).getSelection());
                }
                break;
            case SECOND:
                calendar.set(Calendar.SECOND, spinners.get(type).getSelection());
                break;
            case YEAR:
                calendar.set(Calendar.YEAR, spinners.get(type).getSelection());
                break;
            default:
                break;
            }
        }
    }

    /**
     * Get the values from the spinner controls. They are mapped to the
     * DateFieldType.
     * 
     * @return Map of DateFieldType to values.
     */
    public Map<DateFieldType, Integer> getDateTimeValues() {
        Map<DateFieldType, Integer> valueMap = new HashMap<DateFieldType, Integer>();

        for (DateFieldType dft : spinners.keySet()) {
            valueMap.put(dft, spinners.get(dft).getSelection());
        }

        return valueMap;
    }

    /**
     * Gets the values from the spinner controls. They are mapped to the
     * {@link Calendar#get(int)} fields.
     * 
     * @return
     */
    public Map<Integer, Integer> getCalDateTimeValues() {
        Map<Integer, Integer> valueMap = new HashMap<>();

        for (DateFieldType dft : spinners.keySet()) {
            int fieldValue = spinners.get(dft).getSelection();
            // special case for month. JavaDoc Calendar month is 0 - 11
            if (dft == DateFieldType.MONTH) {
                --fieldValue;
            }
            valueMap.put(this.dfTypeToCalMap.get(dft), fieldValue);
        }

        return valueMap;
    }

    /**
     * Get the spinners formatted as appended, 2 digit values
     * 
     * @return
     */
    public String getFormattedValue() {
        StringBuilder sb = new StringBuilder();
        for (DateFieldType dft : spinners.keySet()) {
            int selection = spinners.get(dft).getSelection();
            if (selection < 10) {
                sb.append("0");
            }
            sb.append(selection);
        }
        return sb.toString();
    }

    /**
     * Enable or disable the spinner controls.
     * 
     * @param enable
     *            True to enable, false to disable.
     */
    public void enableControls(boolean enable) {
        for (DateFieldType dft : spinners.keySet()) {
            spinners.get(dft).setEnabled(enable);
        }
    }

    /**
     * Map the Date/Time field to the calendar field.
     */
    private void populateDataFieldToCalendar() {
        dfTypeToCalMap = new HashMap<DateFieldType, Integer>();

        dfTypeToCalMap.put(DateFieldType.YEAR, Calendar.YEAR);
        dfTypeToCalMap.put(DateFieldType.MONTH, Calendar.MONTH);
        dfTypeToCalMap.put(DateFieldType.DAY, Calendar.DAY_OF_MONTH);
        dfTypeToCalMap.put(DateFieldType.HOUR, Calendar.HOUR_OF_DAY);
        dfTypeToCalMap.put(DateFieldType.MINUTE, Calendar.MINUTE);
        dfTypeToCalMap.put(DateFieldType.SECOND, Calendar.SECOND);
    }

    /**
     * Set the value of the spinner with the specified value. If the value is
     * out side the min/max range of the spinner then nothing is changed.
     * 
     * @param fieldType
     *            Date/Time field type.
     * @param value
     *            New value.
     */
    public void setFieldValue(DateFieldType fieldType, int value) {
        Spinner spnr = spinners.get(fieldType);

        if (validValueForSpinner(spnr, value) == false) {
            return;
        }

        spnr.setSelection(value);
    }

    /**
     * Updates any spinners referenced in the specified {@link Map}.
     * 
     * @param fieldValuesMap
     *            the specified {@link Map}.
     */
    public void setFieldValues(Map<DateFieldType, Integer> fieldValuesMap) {
        for (DateFieldType dtf : fieldValuesMap.keySet()) {
            this.setFieldValue(dtf, fieldValuesMap.get(dtf));
        }
    }

    /**
     * Set the spinners with the data from the calendar. If the values are not
     * valid for the spinner no action is taken.
     * 
     * @param cal
     *            Calendar.
     */
    public void setDateTimeSpinners(Calendar cal) {
        /*
         * Loop over the spinners and set them with the values from the
         * calendar.
         */
        for (DateFieldType dft : fieldValuesMap.keySet()) {
            Spinner spnr = spinners.get(dft);
            int calendarFieldValue = cal.get(dfTypeToCalMap.get(dft));
            // special case for month. JavaDoc Calendar month is 0 - 11
            if (dft == DateFieldType.MONTH) {
                ++calendarFieldValue;
            }
            if (validValueForSpinner(spnr, calendarFieldValue)) {
                spnr.setSelection(calendarFieldValue);
            }
        }
    }

    /**
     * Validate if the value is in the spinner's range.
     * 
     * @param spnr
     *            Spinner control.
     * @param value
     *            Value.
     * @return True if the value is in the range, false otherwise.
     */
    private boolean validValueForSpinner(Spinner spnr, int value) {
        if ((spnr == null) || (value < spnr.getMinimum())
                || (value > spnr.getMaximum())) {
            return false;
        }

        return true;
    }

    /**
     * Get a copy of the backing calendar object.
     * 
     * @return Calendar A copy of the backing Calendar object
     */
    public Calendar getBackingCalendar() {
        Calendar returnCal = TimeUtil.newGmtCalendar();
        returnCal.setTimeInMillis(calendar.getTimeInMillis());
        returnCal.set(Calendar.SECOND, 0);
        returnCal.set(Calendar.MILLISECOND, 0);

        return returnCal;
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());

        Map<DateFieldType, Integer> tmpFieldMap = new LinkedHashMap<DateFieldType, Integer>();
        // tmpFieldMap.put(DateFieldType.YEAR, 1969);
        // tmpFieldMap.put(DateFieldType.MONTH, 2);
        // tmpFieldMap.put(DateFieldType.DAY, 3);
        // tmpFieldMap.put(DateFieldType.HOUR, 4);
        // tmpFieldMap.put(DateFieldType.MINUTE, 5);
        // tmpFieldMap.put(DateFieldType.SECOND, 6);

        tmpFieldMap.put(DateFieldType.YEAR, null);
        tmpFieldMap.put(DateFieldType.MONTH, null);
        tmpFieldMap.put(DateFieldType.DAY, null);
        tmpFieldMap.put(DateFieldType.HOUR, null);
        tmpFieldMap.put(DateFieldType.MINUTE, null);
        tmpFieldMap.put(DateFieldType.SECOND, null);

        DateTimeFields dtf = new DateTimeFields(shell, tmpFieldMap, false,
                false, false);
        System.out.println(dtf.getFormattedValue());
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}