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
package com.raytheon.uf.viz.bmh.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Transmitter Group and Transmitter details dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 8, 2014     3173    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DetailsDlg extends CaveSWTDialog {
    /** The details String */
    private final String detailsStr;

    /** Dialog Width */
    private final int dialogWidth;

    /** Dialog Height */
    private final int dialogHeight;

    /** The Font */
    private Font textFont;

    /**
     * Constructor set to default size of 450 x 350
     * 
     * @param parentShell
     *            Parent Shell
     * @param detailsStr
     *            The details text
     * @param titleStr
     *            The dialog title text
     */
    public DetailsDlg(Shell parentShell, String detailsStr, String titleStr) {
        this(parentShell, detailsStr, titleStr, 450, 350);
    }

    /**
     * Constructor set to user defined size
     * 
     * @param parentShell
     *            Parent Shell
     * @param detailsStr
     *            The details text
     * @param titleStr
     *            The dialog title text
     * @param dialogWidth
     *            The width
     * @param dialogHeight
     *            The height
     */
    public DetailsDlg(Shell parentShell, String detailsStr, String titleStr,
            int dialogWidth, int dialogHeight) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.INDEPENDENT_SHELL
                | CAVE.DO_NOT_BLOCK);
        setText(titleStr);
        this.detailsStr = detailsStr;
        this.dialogHeight = dialogHeight;
        this.dialogWidth = dialogWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setMinimumSize(300, 300);
        createTextControl();
        createCloseButton();
        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (textFont != null) {
                    textFont.dispose();
                }
            }
        });
    }

    /**
     * Create the text control.
     */
    private void createTextControl() {
        textFont = new Font(shell.getDisplay(), "Monospace", 10, SWT.NORMAL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = dialogHeight;
        gd.widthHint = dialogWidth;
        StyledText stText = new StyledText(shell, SWT.BORDER | SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL);
        stText.setFont(textFont);
        stText.setLayoutData(gd);
        stText.setEditable(false);

        stText.setText(detailsStr);
    }

    /**
     * Create the close button.
     */
    private void createCloseButton() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }
}
