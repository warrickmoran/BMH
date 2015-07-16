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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Wraps a {@link Text} widget to toggle it between an editable and non-editable
 * state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 9, 2015  4636       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ToggleEditDisplayTextComp {

    public enum Mode {
        DISPLAY, EDIT
    }

    private final Composite parent;

    private final int fieldWidth;

    private String initialValue;

    private Mode mode;

    private Text text;

    public ToggleEditDisplayTextComp(Composite parent, int fieldWidth,
            String initialValue) {
        this(parent, fieldWidth, initialValue, Mode.DISPLAY);
    }

    public ToggleEditDisplayTextComp(Composite parent, int fieldWidth,
            String initialValue, Mode mode) {
        this.parent = parent;
        this.fieldWidth = fieldWidth;
        this.initialValue = initialValue;
        this.mode = mode;

        this.render();
    }

    private void render() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = this.fieldWidth;
        this.text = new Text(this.parent, SWT.BORDER);
        this.text.setText(this.initialValue);
        this.text.setLayoutData(gd);

        this.syncWithMode();
    }

    public void setValue(String value) {
        this.initialValue = value;
        this.text.setText(value);
        this.mode = Mode.DISPLAY;

        this.syncWithMode();
    }

    public void applyChanges() {
        if (this.mode != Mode.EDIT) {
            throw new IllegalStateException(
                    "Field updates can only be applied in edit mode.");
        }
        this.initialValue = this.text.getText();

        this.mode = Mode.DISPLAY;
        this.syncWithMode();
    }

    public void toggleMode() {
        this.mode = (this.mode == Mode.DISPLAY) ? Mode.EDIT : Mode.DISPLAY;
        this.syncWithMode();
    }

    private void syncWithMode() {
        if (this.mode == Mode.EDIT) {
            this.text.setEditable(true);
            this.text.setBackground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_WHITE));
        } else {
            this.text.setEditable(false);
            this.text.setBackground(this.parent.getBackground());
        }
    }

    public String getCurrentValue() {
        return this.text.getText();
    }

    public boolean hasChanged() {
        return this.initialValue.equals(this.text.getText()) == false;
    }
}