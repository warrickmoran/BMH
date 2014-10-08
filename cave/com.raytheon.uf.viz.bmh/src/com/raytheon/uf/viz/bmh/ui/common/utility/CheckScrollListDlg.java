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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * A dialog that displays a scroll list of checkboxes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  #3360      lvenable     Initial creation
 * Jul 27, 2014 #3420      lvenable     Refactor to separate the scrolled check boxes to
 *                                      another class.
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CheckScrollListDlg extends CaveSWTDialog {

    /** Class containing check list data. */
    private CheckListData checkListData;

    /** Message text that will be displayed above the list of check boxes. */
    private String msgText;

    /** Width of the scrolled composite. */
    private int compWidth = 0;

    /** Height of the scrolled composite. */
    private int compHeight = 0;

    /**
     * Flag indicating of the Select All and Unselect All controls should be
     * shown.
     */
    private boolean showSelectControls = false;

    /** Scrolled composite of check box controls. */
    private CheckScrollListComp checkScrollListComp;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param title
     *            Dialog title.
     * @param msgText
     *            Message text that will be displayed above the list of check
     *            boxes.
     * @param checkListData
     *            Data used to create the list of check boxes.
     * @param showSelectControls
     *            Flag indicating of the Select All and Unselect All controls
     *            should be shown.
     */
    public CheckScrollListDlg(Shell parentShell, String title, String msgText,
            CheckListData checkListData, boolean showSelectControls) {
        this(parentShell, title, msgText, checkListData, showSelectControls,
                225, 250);
    }

    /**
     * 
     * @param parentShell
     *            Parent shell.
     * @param title
     *            Dialog title.
     * @param msgText
     *            Message text that will be displayed above the list of check
     *            boxes.
     * @param checkListData
     *            Data used to create the list of check boxes.
     * @param showSelectControls
     *            Flag indicating of the Select All and Unselect All controls
     *            should be shown.
     * @param width
     *            Width of the scroll composite containing the check boxes.
     * @param height
     *            Height of the scroll composite containing the check boxes.
     */
    public CheckScrollListDlg(Shell parentShell, String title, String msgText,
            CheckListData checkListData, boolean showSelectControls, int width,
            int height) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.checkListData = checkListData;
        this.showSelectControls = showSelectControls;
        this.msgText = msgText;
        compWidth = width;
        compHeight = height;

        setText(title);
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createCheckboxControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createBottomButtons();
    }

    /**
     * Create the list of check box controls.
     */
    private void createCheckboxControls() {

        checkScrollListComp = new CheckScrollListComp(shell, msgText,
                checkListData, showSelectControls, compWidth, compHeight);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText(" OK ");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkayAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText(" Cancel ");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Handle the OK button action.
     */
    private void handleOkayAction() {
        CheckListData cld = checkScrollListComp.getCheckedItems();

        setReturnValue(cld);
        close();
    }
}
