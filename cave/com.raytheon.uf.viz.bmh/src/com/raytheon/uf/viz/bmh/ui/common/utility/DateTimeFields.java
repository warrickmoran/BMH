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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.Activator;
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
    private final Map<DateFieldType, Spinner> spinners = new HashMap<DateFieldType, Spinner>();

    /**
     * {@link List} of {@link DateFieldType}
     */
    private final List<DateFieldType> fieldList;

    /**
     * Default to current time
     */
    private final boolean setToday;

    /**
     * Show the Calendar icon
     */
    private final boolean showCalendarIcon;

    /**
     * The calendar icon
     */
    private Image calendarIcon;

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
     * @param fieldList
     *            List of field types to display
     * @param setToday
     *            set current time flag
     * @param showCalendarIcon
     *            show calendar icon flag
     * @param displayAsPeriodicity
     *            display as periodicity flag
     */
    public DateTimeFields(Composite parent, List<DateFieldType> fieldList,
            boolean setToday, boolean showCalendarIcon,
            boolean displayAsPeriodicity) {
        super(parent, SWT.NONE);
        this.fieldList = fieldList;
        this.setToday = setToday;
        this.showCalendarIcon = showCalendarIcon;
        this.displayAsPeriodicity = displayAsPeriodicity;

        init();
    }

    /**
     * Initialize the gui
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        if (showCalendarIcon) {
            gl = new GridLayout(fieldList.size() + 1, false);
        } else {
            gl = new GridLayout(fieldList.size(), false);
        }
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Composite fieldComp = new Composite(this, SWT.NONE);
        fieldComp.setLayout(gl);
        fieldComp.setLayoutData(gd);

        createSpinners(fieldComp);

        if (setToday) {
            setToCurrentTime();
        }

        if (showCalendarIcon) {
            loadImage();
            Button calBtn = new Button(fieldComp, SWT.PUSH);
            calBtn.setText("Calendar...");
            // calBtn.setImage(calendarIcon);
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
        for (DateFieldType type : fieldList) {
            Spinner s = new Spinner(c, SWT.BORDER);
            if (type == DateFieldType.YEAR) {
                s.setLayoutData(new GridData(33, SWT.DEFAULT));
                s.setTextLimit(4);
            } else {
                s.setLayoutData(new GridData(17, SWT.DEFAULT));
                s.setTextLimit(2);
            }
            s.setData(type);
            s.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Spinner src = (Spinner) e.getSource();
                    spinnerSelectionAction(src);
                }
            });

            spinners.put(type, s);
            switch (type) {
            case HOUR:
            case MINUTE:
            case SECOND:
                s.setMinimum(0);
                s.setMaximum(59);
                break;
            case YEAR:
                s.setMaximum(2200);
                break;
            case MONTH:
                s.setMaximum(12);
                s.setMinimum(1);
                break;
            case DAY:
                s.setMinimum(1);
                if (displayAsPeriodicity) {
                    s.setMaximum(99);
                } else {
                    s.setMaximum(31);
                }
                break;
            }
        }
    }

    /**
     * Set to current time
     */
    private void setToCurrentTime() {
        calendar = TimeUtil.newGmtCalendar();

        if (spinners.containsKey(DateFieldType.YEAR)) {
            spinners.get(DateFieldType.YEAR).setSelection(
                    calendar.get(Calendar.YEAR));
        }
        if (spinners.containsKey(DateFieldType.MONTH)) {
            spinners.get(DateFieldType.MONTH).setSelection(
                    calendar.get(Calendar.MONTH) + 1);
        }
        if (spinners.containsKey(DateFieldType.DAY)) {
            spinners.get(DateFieldType.DAY).setSelection(
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        if (spinners.containsKey(DateFieldType.HOUR)) {
            spinners.get(DateFieldType.HOUR).setSelection(
                    calendar.get(Calendar.HOUR_OF_DAY));
        }
        if (spinners.containsKey(DateFieldType.MINUTE)) {
            spinners.get(DateFieldType.MINUTE).setSelection(
                    calendar.get(Calendar.MINUTE));
        }
        if (spinners.containsKey(DateFieldType.SECOND)) {
            spinners.get(DateFieldType.SECOND).setSelection(
                    calendar.get(Calendar.SECOND));
        }
    }

    /**
     * Load the calendar icon
     */
    private void loadImage() {
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/calendar-16.png");
        calendarIcon = id.createImage();

    }

    @Override
    public void dispose() {
        super.dispose();
        if (calendarIcon != null) {
            calendarIcon.dispose();
        }
    }

    /**
     * Spinner selection action handler
     * 
     * @param spinner
     *            The selected Spinner
     */
    private void spinnerSelectionAction(Spinner spinner) {
        for (DateFieldType type : fieldList) {
            switch (type) {
            case DAY:
                if (this.displayAsPeriodicity
                        || !fieldList.contains(DateFieldType.MONTH)) {
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

        // for debugging
        System.out.println(calendar.getTime());
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());
        List<DateFieldType> fieldList = new ArrayList<DateFieldType>();
        // fieldList.add(DateFieldType.YEAR);
        // fieldList.add(DateFieldType.MONTH);
        fieldList.add(DateFieldType.DAY);
        fieldList.add(DateFieldType.HOUR);
        fieldList.add(DateFieldType.MINUTE);
        fieldList.add(DateFieldType.SECOND);
        new DateTimeFields(shell, fieldList, true, false, true);

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