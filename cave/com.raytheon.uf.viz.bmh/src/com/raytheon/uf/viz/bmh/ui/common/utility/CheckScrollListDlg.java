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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CheckScrollListDlg extends CaveSWTDialog {

    /** Scrolled composite. */
    private ScrolledComposite scrolledComp;

    /** Button composite holding all of the check boxes. */
    private Composite checkBtnComp;

    /** Class containing check list data. */
    private CheckListData checkListData;

    /** Array of checkbox buttons. */
    private List<Button> checkboxArray = new ArrayList<Button>();

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
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

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
        createSelectControls();
        createBottomButtons();
    }

    /**
     * Create the list of check box controls.
     */
    private void createCheckboxControls() {

        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        comp.setLayoutData(gd);

        Label messageLbl = new Label(comp, SWT.NONE);
        messageLbl.setText(msgText);

        scrolledComp = new ScrolledComposite(comp, SWT.BORDER | SWT.H_SCROLL
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

    }

    /**
     * Create the select/unselect controls.
     */
    private void createSelectControls() {

        // Determine if the controls should be displayed.
        if (!showSelectControls) {
            return;
        }

        Composite buttonComp = new Composite(shell, SWT.NONE);
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

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
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
     * Handle the OK button action.
     */
    private void handleOkayAction() {
        CheckListData cld = new CheckListData();

        for (Button btn : checkboxArray) {
            cld.addDataItem(btn.getText(), btn.getSelection());
        }

        setReturnValue(cld);
        close();
    }
}
