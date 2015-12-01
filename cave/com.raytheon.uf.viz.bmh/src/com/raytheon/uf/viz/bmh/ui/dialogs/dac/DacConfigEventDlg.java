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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.dac.DacConfigEvent;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
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
 * Nov 12, 2015 5113       bkowal      Updated to display the recommended action.
 * Dec 01, 2015 5113       bkowal      Allow for Enter -> ... -> Enter creation for new
 *                                     DACs using the generated configuration.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class DacConfigEventDlg extends AbstractBMHDialog {

    private final String DATETIME_COL_HEADER = "Date/Time";

    private final String EVENT_COL_HEADER = "Event";

    private final List<DacConfigEvent> events;

    private final String commonAction;

    private GenericTable eventTable;

    private StyledText actionText;

    private Button closeBtn;

    protected DacConfigEventDlg(Shell parentShell,
            final List<DacConfigEvent> events, final String commonAction) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.setText("DAC Configuration Events");
        this.events = events;
        this.commonAction = commonAction;
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
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

        closeBtn = new Button(comp, SWT.PUSH);
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

        populateEventTable();
    }

    private void populateEventTable() {
        List<TableColumnData> columns = new ArrayList<>(2);
        TableColumnData tableColumnData = new TableColumnData(
                DATETIME_COL_HEADER, 125);
        tableColumnData.setPack(true);
        columns.add(tableColumnData);
        tableColumnData = new TableColumnData(EVENT_COL_HEADER, 275, SWT.LEFT);
        tableColumnData.setPack(true);
        columns.add(tableColumnData);

        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        boolean success = true;

        List<TableCellData> rowCells;
        TableData td = new TableData(columns);
        for (int i = 0; i < events.size(); i++) {
            rowCells = new ArrayList<>(2);
            DacConfigEvent event = events.get(i);

            TableCellData tcd = new TableCellData(sdf.format(event
                    .getEventDate().getTime()));
            if (event.isError()) {
                tcd.setForegroundColor(this.getDisplay().getSystemColor(
                        SWT.COLOR_RED));
            }
            rowCells.add(tcd);

            tcd = new TableCellData(event.getMessage());
            if (event.isError()) {
                tcd.setForegroundColor(this.getDisplay().getSystemColor(
                        SWT.COLOR_RED));
                success = false;
            }
            rowCells.add(tcd);

            TableRowData trd = new TableRowData(rowCells);
            td.addDataRow(trd);
            trd.setData(event);
        }

        eventTable.populateTable(td);
        if (this.commonAction == null) {
            eventTable.setCallbackAction(new ITableActionCB() {
                @Override
                public void tableSelectionChange(int selectionCount) {
                    if (selectionCount != 1) {
                        return;
                    }

                    TableRowData selectedTrd = eventTable.getSelection().get(0);
                    DacConfigEvent event = (DacConfigEvent) selectedTrd
                            .getData();
                    if (event == null) {
                        actionText.setText(StringUtils.EMPTY);
                    } else {
                        actionText.setText(event.getAction());
                    }
                }
            });
        } else {
            this.actionText.setText(commonAction);
        }

        if (success) {
            /*
             * Allow for easy, convenient: Enter -> Enter -> Enter
             * configuration.
             * 
             * Only if successful.
             */
            this.closeBtn.forceFocus();
        }
    }
}