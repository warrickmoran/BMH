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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

/**
 * Composite with a scale and spinner linked together.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2014    3630    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ScaleSpinnerComp extends Composite {

    private int startVal = 0;

    private int endVal = 1;

    private String lblText = "";

    private Scale valueScale;

    private Spinner valueSpnr;

    private int offset = 0;

    private int minScaleWidth = 150;

    private int spinnerWidth = 25;

    /**
     * Constructor
     * 
     * @param parentComp
     *            The parent composite
     * @param startVal
     *            The starting (lowest) value
     * @param endVal
     *            The ending (highest) value
     * @param labelText
     *            Text for the label
     */
    public ScaleSpinnerComp(Composite parentComp, int startVal, int endVal,
            String labelText) {
        this(parentComp, startVal, endVal, null, 150, 25);
    }

    /**
     * Constructor
     * 
     * @param parentComp
     *            The parent composite
     * @param startVal
     *            The starting (lowest) value
     * @param endVal
     *            The ending (highest) value
     * @param spinnerWidth
     *            The width of the spinner widget
     */
    public ScaleSpinnerComp(Composite parentComp, int startVal, int endVal,
            int spinnerWidth) {
        this(parentComp, startVal, endVal, null, 150, spinnerWidth);
    }

    /**
     * Constructor
     * 
     * @param parentComp
     *            The parent composite
     * @param startVal
     *            The starting (lowest) value
     * @param endVal
     *            The ending (highest) value
     */
    public ScaleSpinnerComp(Composite parentComp, int startVal, int endVal) {
        this(parentComp, startVal, endVal, null, 150, 25);
    }

    /**
     * Constructor
     * 
     * @param parentComp
     *            The parent composite
     * @param startVal
     *            The starting (lowest) value
     * @param endVal
     *            The ending (highest) value
     * @param labelText
     *            Text for the label
     * @param scaleMinWidth
     *            Minimum width of the scale
     * @param spinnerWidth
     *            The width of the spinner widget
     */
    public ScaleSpinnerComp(Composite parentComp, int startVal, int endVal,
            String lblText, int scaleMinWidth, int spinnerWidth) {
        super(parentComp, SWT.NONE);

        this.startVal = startVal;
        this.endVal = endVal;

        if (lblText != null) {
            this.lblText = lblText;
        }

        if (this.startVal > this.endVal) {
            this.startVal = this.endVal;
        }

        if (minScaleWidth > 0) {
            this.minScaleWidth = scaleMinWidth;
        }

        if (spinnerWidth > 0) {
            this.spinnerWidth = spinnerWidth;
        }

        if (startVal < 0) {
            offset = 0 - startVal;
        }

        init();
    }

    private void init() {
        GridLayout gl = new GridLayout(3, false);
        gl.marginWidth = 2;
        gl.marginHeight = 2;
        gl.horizontalSpacing = 10;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        createControls();
    }

    private void createControls() {
        if (lblText != null) {
            Label scaleLbl = new Label(this, SWT.CENTER);
            scaleLbl.setText(lblText);
        }

        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gd.minimumWidth = minScaleWidth;
        valueScale = new Scale(this, SWT.HORIZONTAL);
        valueScale.setMinimum(startVal + offset);
        valueScale.setMaximum(endVal + offset);
        valueScale.setIncrement(1);
        valueScale.setPageIncrement(1);
        valueScale.setLayoutData(gd);
        valueScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                valueSpnr.setSelection(valueScale.getSelection() - offset);
            }
        });

        gd = new GridData(spinnerWidth, SWT.DEFAULT);
        valueSpnr = new Spinner(this, SWT.BORDER);
        valueSpnr.setValues(startVal, startVal, endVal, 0, 1, 5);
        valueSpnr.setLayoutData(gd);
        valueSpnr.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                valueScale.setSelection(valueSpnr.getSelection() + offset);
            }
        });
    }

    /**
     * Get the selected value.
     * 
     * @return The selected value
     */
    public int getSelectedValue() {
        return valueSpnr.getSelection();
    }

    /**
     * Set the selected value.
     * 
     * @param value
     *            The value
     */
    public void setSelectedValue(int value) {
        valueSpnr.setSelection(value);
        valueScale.setSelection(valueSpnr.getSelection() + offset);
    }
}
