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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.utility.ListMoveAction;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to reorder Groups/Transmitters within the UI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 7, 2014     3173    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class GroupTransmitterOrderDlg extends CaveSWTDialog {

    /** List Control */
    private List uiList;

    /** List of items to put in the uiList */
    private final java.util.List<String> listItems;

    /** The action class */
    private ListMoveAction listMoveAction;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            The parent shell
     * @param title
     *            dialog title
     * @param listItems
     *            list of items
     */
    public GroupTransmitterOrderDlg(Shell parentShell, String title,
            java.util.List<String> listItems) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        setText(title);
        this.listItems = listItems;
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Group group = new Group(shell, SWT.BORDER);
        group.setText(" " + this.getText() + " ");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 200;
        gd.heightHint = 250;
        uiList = new List(group, SWT.BORDER);
        uiList.setLayoutData(gd);

        uiList.setItems(listItems.toArray(new String[0]));

        createUpDownButtons(group);
        createBottomButtons();
        listMoveAction = new ListMoveAction(uiList);
    }

    private void createUpDownButtons(Group group) {
        GridData gd;
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite btnComp = new Composite(group, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(55, SWT.DEFAULT);
        Button upBtn = new Button(btnComp, SWT.PUSH);
        upBtn.setText("Up");
        upBtn.setLayoutData(gd);
        upBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listMoveAction.moveUp();
            }
        });

        gd = new GridData(55, SWT.DEFAULT);
        Button downBtn = new Button(btnComp, SWT.PUSH);
        downBtn.setText("Down");
        downBtn.setLayoutData(gd);
        downBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listMoveAction.moveDown();
            }
        });
    }

    private void createBottomButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveBtn = new Button(btnComp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                save();
                close();
            }
        });
        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Cancel");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    protected void save() {
        setReturnValue(uiList.getItems());
    }
}
