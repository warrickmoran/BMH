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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Message information/details dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 4, 2014     3432       mpduff     Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class MessageDetailsDlg extends CaveSWTDialog {
    private final String[] COMBO_VALUES = { "Broadcast Areas", "Programs",
            "Suites", "Transmitters" };

    /**
     * The Stack Layout.
     */
    private final StackLayout stackLayout = new StackLayout();

    private BroadcastAreaTableComp broadcastAreaTableComp;

    /**
     * The Stack Composite.
     */
    private Composite stackComp;

    private TransmitterTableComp transmitterTableComp;

    private SuiteTableComp suiteTableComp;

    private ProgramTableComp programTableComp;

    public MessageDetailsDlg(Shell parent) {

        super(parent, SWT.DIALOG_TRIM, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT);

        setText("Message Details/Information");
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite upperComp = new Composite(shell, SWT.NONE);
        upperComp.setLayout(gl);
        upperComp.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        Composite labelComp = new Composite(upperComp, SWT.NONE);
        labelComp.setLayout(gl);
        labelComp.setLayoutData(gd);

        buildLabels(labelComp);

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite stackComp = new Composite(upperComp, SWT.NONE);
        stackComp.setLayout(gl);
        stackComp.setLayoutData(gd);
        buildStackLayout(stackComp);

        buildMessageArea();

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 75;
        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        close.setLayoutData(gd);
        close.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    private void buildLabels(Composite comp) {
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label typeLbl = new Label(comp, SWT.NONE);
        typeLbl.setText("Message Type: ");
        typeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label typeValueLbl = new Label(comp, SWT.NONE);
        // typeValueLbl.setText(message.getType());
        typeValueLbl.setText("Message Type");
        typeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label titleLbl = new Label(comp, SWT.NONE);
        titleLbl.setText("Message Title: ");
        titleLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label titleValueLbl = new Label(comp, SWT.NONE);
        // titleValueLbl.setText(message.getTitle());
        titleValueLbl.setText("Message Title");
        titleValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label dispositionLbl = new Label(comp, SWT.NONE);
        dispositionLbl.setText("Disposition: ");
        dispositionLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label dispositionValueLbl = new Label(comp, SWT.NONE);
        // dispositionValueLbl.setText(message.getDisposition());
        dispositionValueLbl.setText("Message Disposition");
        dispositionValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label periodicityLbl = new Label(comp, SWT.NONE);
        periodicityLbl.setText("Periodicity\n(DD:HH:MM): ");
        periodicityLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label periodicityValueLbl = new Label(comp, SWT.NONE);
        // periodicityValueLbl.setText(message.getPeriodicity().toString());
        periodicityValueLbl.setText("02:12:00:00");
        periodicityValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label timeZoneLbl = new Label(comp, SWT.NONE);
        timeZoneLbl.setText("Time Zone: ");
        timeZoneLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label timeZoneValueLbl = new Label(comp, SWT.NONE);
        // timeZoneValueLbl.setText(message.getTimeZone().getID());
        timeZoneValueLbl.setText("GMT");
        timeZoneValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label createTimeLbl = new Label(comp, SWT.NONE);
        createTimeLbl.setText("Creation Time: ");
        createTimeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label createTimeValueLbl = new Label(comp, SWT.NONE);
        // createTimeValueLbl.setText(message.getCreateTimeString());
        createTimeValueLbl.setText("Create time string");
        createTimeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label effectiveTimeLbl = new Label(comp, SWT.NONE);
        effectiveTimeLbl.setText("Effective Time: ");
        effectiveTimeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label effectiveTimeValueLbl = new Label(comp, SWT.NONE);
        // effectiveTimeValueLbl.setText(message.getEffectiveTimeString());
        effectiveTimeValueLbl.setText("Effective time string");
        effectiveTimeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label expirationTimeLbl = new Label(comp, SWT.NONE);
        expirationTimeLbl.setText("Expiration Time: ");
        expirationTimeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label expirationTimeValueLbl = new Label(comp, SWT.NONE);
        // expirationTimeValueLbl.setText(message.getExpirationTimeString());
        expirationTimeValueLbl.setText("Expiration time string");
        expirationTimeValueLbl.setLayoutData(gd);
    }

    private void buildStackLayout(Composite comp) {
        GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Combo combo = new Combo(comp, SWT.NONE);
        combo.setLayoutData(gd);
        combo.setItems(COMBO_VALUES);
        combo.select(0);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // COMBO_VALUES = { "Broadcast Areas", "Programs",
                // "Suites", "Transmitters" };
                String selection = ((Combo) e.getSource()).getText();
                if (selection.equals(COMBO_VALUES[0])) {
                    stackLayout.topControl = broadcastAreaTableComp;
                } else if (selection.equals(COMBO_VALUES[1])) {
                    stackLayout.topControl = programTableComp;
                } else if (selection.equals(COMBO_VALUES[2])) {
                    stackLayout.topControl = suiteTableComp;
                } else if (selection.equals(COMBO_VALUES[3])) {
                    stackLayout.topControl = transmitterTableComp;
                }
                stackComp.layout();
            }
        });

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);
        stackComp = new Composite(comp, SWT.NONE);
        stackComp.setLayout(stackLayout);
        stackComp.setLayoutData(gd);

        // Broadcast Area
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        broadcastAreaTableComp = new BroadcastAreaTableComp(stackComp,
                SWT.BORDER | SWT.V_SCROLL);
        broadcastAreaTableComp.setLayout(gl);
        broadcastAreaTableComp.setLayoutData(gd);

        populateBroadcastAreaTable();

        // Transmitters - name/program
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        transmitterTableComp = new TransmitterTableComp(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        transmitterTableComp.setLayout(gl);
        transmitterTableComp.setLayoutData(gd);

        populateTransmitterTable();

        // Suites containing this message
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        suiteTableComp = new SuiteTableComp(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        suiteTableComp.setLayout(gl);
        suiteTableComp.setLayoutData(gd);

        populateSuiteTable();

        // Programs containing this message
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        programTableComp = new ProgramTableComp(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        programTableComp.setLayout(gl);
        programTableComp.setLayoutData(gd);

        populateProgramTable();

        stackLayout.topControl = broadcastAreaTableComp;
    }

    private void buildMessageArea() {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label msgLbl = new Label(shell, SWT.NONE);
        msgLbl.setText("Message Text:");
        msgLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 125;
        Text text = new Text(shell, SWT.BORDER | SWT.V_SCROLL);
        text.setLayoutData(gd);
        text.setLayoutData(gd);
        // text.setText(message.getMessageText());
        text.setText("Message text here");
    }

    private void populateBroadcastAreaTable() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Area Code", 75);
        TableColumnData tcd2 = new TableColumnData("Broadcast Area");
        columns.add(tcd);
        columns.add(tcd2);

        // TODO get real data
        TableData td = new TableData(columns);

        broadcastAreaTableComp.populateTable(td);
    }

    private void populateTransmitterTable() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 75);
        TableColumnData tcd2 = new TableColumnData("Program");
        columns.add(tcd);
        columns.add(tcd2);

        // TODO
        TableData td = new TableData(columns);
        transmitterTableComp.populateTable(td);
    }

    private void populateSuiteTable() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 75);
        TableColumnData tcd2 = new TableColumnData("Category");
        columns.add(tcd);
        columns.add(tcd2);

        // TODO
        TableData td = new TableData(columns);
        suiteTableComp.populateTable(td);
    }

    private void populateProgramTable() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 75);
        columns.add(tcd);

        // TODO
        TableData td = new TableData(columns);
        programTableComp.populateTable(td);
    }

    private class BroadcastAreaTableComp extends TableComp {

        public BroadcastAreaTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // No op
        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            // No op
        }
    }

    private class TransmitterTableComp extends TableComp {

        public TransmitterTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // No op
        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            // No op
        }
    }

    private class SuiteTableComp extends TableComp {

        public SuiteTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // No op
        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            // No op
        }
    }

    private class ProgramTableComp extends TableComp {

        public ProgramTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // No op
        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            // No op
        }
    }
}
