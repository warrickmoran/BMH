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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;

/**
 * This dialog will display a summary of the events that occurred during an
 * automatic DAC configuration. The event list will indicate whether the
 * configuration process was successful or not. When unsuccessful, the
 * {@link #actionText} field on this dialog will display the recommended action
 * to prevent the failure in the future.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2015 5113       bkowal      Initial creation
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class DacConfigEventDlg extends AbstractBMHDialog {

    private final String DATETIME_COL_HEADER = "Date/Time";

    private final String EVENT_COL_HEADER = "Event";

    private final Color failureColor;

    private GenericTable eventTable;

    private StyledText actionText;

    protected DacConfigEventDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        this.setText("DAC Configuration Events");
        this.failureColor = new Color(shell.getDisplay(), 255, 0, 0);
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
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void disposed() {
        if (this.failureColor != null) {
            this.failureColor.dispose();
        }
    }

    @Override
    public boolean okToClose() {
        return true;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Group eventGrp = new Group(shell, SWT.SHADOW_OUT);
        eventGrp.setText(" Configuration Events ");
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        eventGrp.setLayout(gl);
        eventGrp.setLayoutData(gd);

        eventTable = new GenericTable(eventGrp, SWT.SINGLE, 400, 125);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        Label actionLabel = new Label(eventGrp, SWT.NONE);
        actionLabel.setText("Recommended Action:");
        actionLabel.setLayoutData(gd);

        actionText = new StyledText(eventGrp, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 400;
        gd.heightHint = 80;
        actionText.setLayoutData(gd);
        actionText.setWordWrap(true);

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        Button closeBtn = new Button(comp, SWT.PUSH);
        closeBtn.setText("Close");
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 90;
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /*
     * TODO: wait for http://lightning.omaha.us.ray.com:8080/#/c/9336/ where the
     * dac config event type has been declared.
     */
    private void populateEventTable() {
        List<TableColumnData> columns = new ArrayList<>(2);
        columns.add(new TableColumnData(DATETIME_COL_HEADER, 125));
        columns.add(new TableColumnData(EVENT_COL_HEADER, 275, SWT.LEFT));

        TableData td = new TableData(columns);
    }
}