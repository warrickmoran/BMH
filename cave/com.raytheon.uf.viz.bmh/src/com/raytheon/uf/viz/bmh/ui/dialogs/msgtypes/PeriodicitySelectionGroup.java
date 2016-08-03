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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;

/**
 * Common component for the management of periodicity. Displays widgets that the
 * user can interact with to view or alter periodicity properties.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2016 5766       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class PeriodicitySelectionGroup {

    private static final String PERIODICITY = "Periodicity";

    private static final String TIME_LABEL = "Time (DDHHMMSS):";

    private static final String CYCLE_LABEL = String.format("Cycle (%d-%d):",
            MessageType.MIN_PERIODICITY_CYCLES,
            MessageType.MAX_PERIODICITY_CYLES);

    private final Group group;

    private Button periodicityTimeBtn;

    private DateTimeFields periodicityDTF;

    private Button periodicityCycleBtn;

    private Spinner periodicityCycleSpinner;

    public PeriodicitySelectionGroup(Composite parent) {
        group = new Group(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        group.setLayout(gl);
        group.setText(PERIODICITY);
        init();
    }

    private void init() {
        /*
         * Create the Date/Time-based Periodicity controls.
         */
        GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        periodicityTimeBtn = new Button(group, SWT.RADIO);
        periodicityTimeBtn.setText(TIME_LABEL);
        periodicityTimeBtn.setLayoutData(gd);
        periodicityTimeBtn.setSelection(true);
        periodicityTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handlePeriodicityTypeChange();
            }
        });

        Map<DateFieldType, Integer> periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(null);
        periodicityDTF = new DateTimeFields(group, periodicityMap, false, true);

        /*
         * Create the Cycle-based Periodicity controls.
         */
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        periodicityCycleBtn = new Button(group, SWT.RADIO);
        periodicityCycleBtn.setText(CYCLE_LABEL);
        periodicityCycleBtn.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        periodicityCycleSpinner = new Spinner(group, SWT.BORDER);
        periodicityCycleSpinner.setMinimum(MessageType.MIN_PERIODICITY_CYCLES);
        periodicityCycleSpinner.setMaximum(MessageType.MAX_PERIODICITY_CYLES);
        periodicityCycleSpinner.setEnabled(false);
        periodicityCycleSpinner.setLayoutData(gd);
        periodicityCycleSpinner.setEnabled(false);
    }

    public void populate(final String periodicity, final Integer cycles) {
        Map<DateFieldType, Integer> periodicityMap = BmhUtils
                .generateDayHourMinuteSecondMap(periodicity);
        periodicityDTF.setFieldValues(periodicityMap);

        int selectedCycles = (cycles == null) ? MessageType.MIN_PERIODICITY_CYCLES
                : cycles;
        periodicityCycleSpinner.setSelection(selectedCycles);
        periodicityTimeBtn.setSelection(cycles == null);
        periodicityCycleBtn.setSelection(cycles != null);
        handlePeriodicityTypeChange();
    }

    private void handlePeriodicityTypeChange() {
        periodicityDTF.setEnabled(periodicityTimeBtn.getSelection());
        periodicityCycleSpinner.setEnabled(periodicityCycleBtn.getSelection());
    }

    public void setLayoutData(Object layoutData) {
        group.setLayoutData(layoutData);
    }

    public String getPeriodicityTime() {
        if (periodicityTimeBtn.getSelection()) {
            return periodicityDTF.getFormattedValue();
        }
        return MessageType.DEFAULT_NO_PERIODICITY;
    }

    public Integer getPeriodicityCycles() {
        if (periodicityCycleBtn.getSelection()) {
            return periodicityCycleSpinner.getSelection();
        }
        return null;
    }
}