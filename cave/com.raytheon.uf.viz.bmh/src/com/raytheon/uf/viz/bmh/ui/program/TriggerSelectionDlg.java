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
package com.raytheon.uf.viz.bmh.ui.program;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.widgets.duallist.DualList;
import com.raytheon.viz.ui.widgets.duallist.DualListConfig;

/**
 * Dialog containing a {@link DualList} used for trigger message type selection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2016 5562       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TriggerSelectionDlg extends CaveSWTDialog {

    private DualList triggerDualList;

    private final List<String> triggerAfosids;

    private final List<String> availableAfosids;

    public TriggerSelectionDlg(Shell parent, final List<String> triggerAfosids,
            List<String> availableAfosids) {
        super(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Trigger Selection");
        this.triggerAfosids = triggerAfosids;
        this.availableAfosids = availableAfosids;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainControlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        mainControlComp.setLayout(gl);
        mainControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        final DualListConfig dualListConfig = new DualListConfig();
        dualListConfig.setSelectedListLabel("Triggers:");
        dualListConfig.setAvailableListLabel("Avail Msg Types:");
        dualListConfig.setSelectedList(triggerAfosids);
        dualListConfig.setFullList(availableAfosids);

        triggerDualList = new DualList(mainControlComp, SWT.NONE,
                dualListConfig);

        createBottomButtons(mainControlComp);
    }

    private void createBottomButtons(Composite mainControlComp) {
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        Composite buttonsComp = new Composite(mainControlComp, SWT.NONE);
        GridLayout gl = new GridLayout(2, true);
        buttonsComp.setLayout(gl);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        buttonsComp.setLayoutData(gd);

        final int minimumBtnWidth = buttonsComp.getDisplay().getDPI().x;

        Button okBtn = new Button(buttonsComp, SWT.PUSH);
        okBtn.setText("OK");
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumBtnWidth;
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                okAction();
            }
        });

        Button cancelBtn = new Button(buttonsComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumBtnWidth;
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelAction();
            }
        });
    }

    private void okAction() {
        final String[] selected = triggerDualList.getSelectedListItems();
        Set<String> selectedSet = null;
        if (selected.length == 0) {
            selectedSet = Collections.emptySet();
        } else {
            selectedSet = new HashSet<>(selected.length, 1.0f);
            for (String selectedAfosId : selected) {
                selectedSet.add(selectedAfosId);
            }
        }
        setReturnValue(selectedSet);
        close();
    }

    private void cancelAction() {
        setReturnValue(null);
        close();
    }
}