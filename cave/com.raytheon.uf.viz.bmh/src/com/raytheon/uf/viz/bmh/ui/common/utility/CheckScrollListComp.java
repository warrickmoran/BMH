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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * Class containing a scrolled composite with check box controls.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 27, 2014 #3420      lvenable     Initial creation - part of refactor.
 * Oct 15, 2014 #3728      lvenable     Added ability to lock the controls from being edited and
 *                                      allow selection/deselection of all the check boxes via
 *                                      method call.
 * Dec 15, 2014 #3876      bkowal       Added {@link #reset()} and {@link #enableCheckboxes(List)}.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CheckScrollListComp extends Composite {

    /** Class containing check list data. */
    private CheckListData checkListData;

    /** Array of check box buttons. */
    private List<Button> checkboxArray = new ArrayList<Button>();

    /** Width of the scrolled composite. */
    private int compWidth = 0;

    /** Height of the scrolled composite. */
    private int compHeight = 0;

    /** Message text that will be displayed above the list of check boxes. */
    private String msgText;

    /** Scrolled composite. */
    private ScrolledComposite scrolledComp;

    /**
     * Flag indicating if the Select All and Unselect All controls should be
     * shown.
     */
    private boolean showSelectControls = false;

    /** Button composite holding all of the check boxes. */
    private Composite checkBtnComp;

    /** Lock the controls from being changed. */
    private boolean lockControls = false;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param msgText
     *            Message text.
     * @param checkListData
     *            Check list data for the check boxes.
     * @param showSelectControls
     *            Flag indicating if the select controls should be shown.
     * @param width
     *            Width of the scrolled composite.
     * @param height
     *            Height of the scrolled composite.
     */
    public CheckScrollListComp(Composite parentComp, String msgText,
            CheckListData checkListData, boolean showSelectControls, int width,
            int height) {

        this(parentComp, msgText, checkListData, showSelectControls, width,
                height, false);
    }

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param msgText
     *            Message text.
     * @param checkListData
     *            Check list data for the check boxes.
     * @param showSelectControls
     *            Flag indicating if the select controls should be shown.
     * @param width
     *            Width of the scrolled composite.
     * @param height
     *            Height of the scrolled composite.
     * @param lockControls
     *            Lock the controls from being edited. If lockControls is true
     *            then the select/unselect controls are not displayed.
     */
    public CheckScrollListComp(Composite parentComp, String msgText,
            CheckListData checkListData, boolean showSelectControls, int width,
            int height, boolean lockControls) {
        super(parentComp, SWT.NONE);

        this.checkListData = checkListData;

        this.showSelectControls = showSelectControls;
        this.msgText = msgText;
        compWidth = width;
        compHeight = height;
        this.lockControls = lockControls;

        init();
    }

    /**
     * Initialize class.
     */
    private void init() {

        this.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.setLayoutData(gd);

        if (lockControls) {
            this.setEnabled(false);
        }

        Label messageLbl = new Label(this, SWT.NONE);
        messageLbl.setText(msgText);

        scrolledComp = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 1;
        scrolledComp.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = compWidth;
        gd.heightHint = compHeight;
        scrolledComp.setLayoutData(gd);

        checkBtnComp = new Composite(scrolledComp, SWT.NONE);
        checkBtnComp.setLayout(new GridLayout(1, false));
        checkBtnComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Map<String, Boolean> dataMap = checkListData.getDataMap();

        for (String str : dataMap.keySet()) {
            Button checkBox = new Button(checkBtnComp, SWT.CHECK);
            checkBox.setText(str);
            checkBox.setSelection(dataMap.get(str));
            checkboxArray.add(checkBox);
        }

        scrolledComp.setExpandHorizontal(true);
        scrolledComp.setExpandVertical(true);
        scrolledComp.setContent(checkBtnComp);
        scrolledComp.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                scrolledComp.setMinSize(checkBtnComp.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT));
            }
        });
        scrolledComp.layout();

        createSelectControls();
    }

    /**
     * Create the select/unselect controls.
     */
    private void createSelectControls() {

        /*
         * Determine if the controls should be displayed. If the controls are
         * locked then it doesn't make sense to give the user the ability to
         * select/unselect the checkboxed.
         */
        if (!showSelectControls || lockControls) {
            return;
        }

        Composite buttonComp = new Composite(this, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 90;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button selectAllBtn = new Button(buttonComp, SWT.PUSH);
        selectAllBtn.setText(" Select All ");
        selectAllBtn.setLayoutData(gd);
        selectAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectAll(true);
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button unselectAllBtn = new Button(buttonComp, SWT.PUSH);
        unselectAllBtn.setText(" Unselect All ");
        unselectAllBtn.setLayoutData(gd);
        unselectAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectAll(false);
            }
        });

    }

    /**
     * Select or unselect the controls.
     * 
     * @param selected
     *            True to select all controls, false to unselect.
     */
    private void selectAll(boolean selected) {
        for (Button btn : checkboxArray) {
            btn.setSelection(selected);
        }
    }

    /**
     * Get the selected (checked) items.
     * 
     * @return Check list data class with only the selected items.
     */
    public CheckListData getCheckedItems() {
        CheckListData cld = new CheckListData();

        for (Button btn : checkboxArray) {
            cld.addDataItem(btn.getText(), btn.getSelection());
        }

        return cld;
    }

    /**
     * This method will unselect all of the checkboxes and then select the ones
     * that are in the check list data.
     * 
     * @param cld
     *            Check list data.
     */
    public void selectCheckboxes(CheckListData cld) {
        if (cld == null) {
            return;
        }

        Map<String, Boolean> cldDataMap = cld.getDataMap();

        for (Button btn : checkboxArray) {
            btn.setSelection(false);

            if (cldDataMap.containsKey(btn.getText())) {
                btn.setSelection(cldDataMap.get(btn.getText()));
            }
        }
    }

    /**
     * Select or unselect all of the check boxes.
     * 
     * @param selectAll
     *            True to select all, false to unselect all of the checkboxes.
     */
    public void selectCheckboxes(boolean selectAll) {
        for (Button btn : checkboxArray) {
            btn.setSelection(selectAll);
        }
    }

    /**
     * Used to reset the controls back to their default state - enabled and
     * unchecked.
     */
    public void reset() {
        for (Button btn : checkboxArray) {
            btn.setEnabled(true);
            btn.setSelection(false);
        }
    }

    /**
     * Selectively enable/disable checkboxes based on whether or not they have
     * one of the specified identifiers.
     * 
     * @param identifiers
     *            the specified identifiers
     */
    public void enableCheckboxes(final List<String> identifiers) {
        for (Button btn : checkboxArray) {
            if (identifiers.contains(btn.getText())) {
                btn.setEnabled(true);
            } else {
                btn.setEnabled(false);
                /* ensure that the checkbox has not been checked. */
                btn.setSelection(false);
            }
        }
    }
}