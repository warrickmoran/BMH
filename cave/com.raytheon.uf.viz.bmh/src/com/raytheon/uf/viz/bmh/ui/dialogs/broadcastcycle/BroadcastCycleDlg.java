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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;

/**
 * Broadcast cycle dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2014   3432      mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleDlg extends AbstractBMHDialog {

    private final String TITLE = "Broadcast Cycle";

    /** Table Data */
    private TableData tableData;

    /** The selected transmitter */
    // TODO remove this default value
    private String selectedTransmitter = "Trans0";

    /** The program */
    // TODO this needs to be set by use selection
    private String program;

    /** Selected suite */
    private String selectedSuite;

    /** The data manager */
    private final BroadcastCycleDataManager dataManager;

    /** The table composite */
    private BroadcastCycleTableComp tableComp;

    /** The list of transmitters */
    private org.eclipse.swt.widgets.List transmitterList;

    /** DAC value label */
    private Label dacValueLbl;

    /** Port value label */
    private Label portValueLbl;

    /** Program value label */
    private Label progValueLbl;

    /** Suite value label */
    private Label suiteValueLbl;

    /** Suite category value label */
    private Label suiteCatValueLbl;

    /** program/suite left composite */
    private Composite leftProgSuiteComp;

    /** Color manager */
    private BroadcastCycleColorManager colorManager;

    /** Suite list dialog */
    private SuiteListDlg changeSuiteDlg;

    /** Periodic message dialog */
    private PeriodicMessagesDlg periodicMsgDlg;

    /** Message details dialog */
    private MessageDetailsDlg detailsDlg;

    /**
     * Constructor.
     * 
     * @param parent
     *            The parent shell
     * @param dlgMap
     *            The opend dialogs map
     */
    public BroadcastCycleDlg(Shell parent, Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Broadcast Cycle Dialog", parent, SWT.DIALOG_TRIM,
                CAVE.INDEPENDENT_SHELL | CAVE.PERSPECTIVE_INDEPENDENT);
        this.dataManager = new BroadcastCycleDataManager();
        setText(TITLE);
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.horizontalSpacing = 0;
        mainLayout.verticalSpacing = 0;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        // initialize colors
        colorManager = new BroadcastCycleColorManager(getShell().getDisplay());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        createMenus();
        createTransmitterList(mainComp);

        gd = new GridData(SWT.DEFAULT, SWT.FILL, false, false);
        gl = new GridLayout(1, false);
        gl.marginBottom = 0;
        Composite broadcastComp = new Composite(mainComp, SWT.NONE);
        broadcastComp.setLayout(gl);
        broadcastComp.setLayoutData(gd);

        createUpperSection(broadcastComp);
        createTable(broadcastComp);
        createMessageText(broadcastComp);
        createBottomButtons(broadcastComp);
        populateTableData();

        this.transmitterList.setItems(getTestTransmitters());
    }

    private void createMenus() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem file = new MenuItem(menuBar, SWT.CASCADE);
        file.setText("&File");

        Menu filemenu = new Menu(shell, SWT.DROP_DOWN);
        file.setMenu(filemenu);

        MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
        exitItem.setText("E&xit");
        exitItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        MenuItem help = new MenuItem(menuBar, SWT.CASCADE);
        help.setText("&Help");

        Menu helpmenu = new Menu(shell, SWT.DROP_DOWN);
        help.setMenu(helpmenu);

        MenuItem getHelpItem = new MenuItem(helpmenu, SWT.PUSH);
        getHelpItem.setText("&About");

        shell.setMenuBar(menuBar);
    }

    private void createTransmitterList(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Group transGrp = new Group(comp, SWT.BORDER);
        transGrp.setText(" Transmitters ");
        transGrp.setLayout(gl);
        transGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 125;
        transmitterList = new org.eclipse.swt.widgets.List(transGrp,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.BORDER);
        transmitterList.setLayoutData(gd);
    }

    private void createUpperSection(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(5, false);
        Group transmitterGrp = new Group(comp, SWT.NONE);
        transmitterGrp.setText(" Selected Transmitter ");
        transmitterGrp.setLayout(gl);
        transmitterGrp.setLayoutData(gd);

        // Transmitter name
        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gl = new GridLayout(2, false);
        Composite nameComp = new Composite(transmitterGrp, SWT.NONE);
        nameComp.setLayout(gl);
        nameComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label tranNameLbl = new Label(nameComp, SWT.NONE);
        tranNameLbl.setText("Transmitter: ");
        tranNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label tranNameValueLbl = new Label(nameComp, SWT.NONE);
        tranNameValueLbl.setText(selectedTransmitter);
        tranNameValueLbl.setLayoutData(gd);

        // Monitor inline
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        Button monitorBtn = new Button(transmitterGrp, SWT.CHECK);
        monitorBtn.setText("Monitor In-line  ");
        monitorBtn.setLayoutData(gd);

        // Transmitter time zone
        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gl = new GridLayout(2, false);
        Composite tzComp = new Composite(transmitterGrp, SWT.NONE);
        tzComp.setLayout(gl);
        tzComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, false, false);
        Label timeZoneLbl = new Label(tzComp, SWT.NONE);
        timeZoneLbl.setText("Time Zone: ");
        timeZoneLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label timeZoneValueLbl = new Label(tzComp, SWT.NONE);
        timeZoneValueLbl.setText("UTC");
        timeZoneValueLbl.setLayoutData(gd);

        // Transmitter DAC
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        gl = new GridLayout(2, false);
        Composite dacComp = new Composite(transmitterGrp, SWT.NONE);
        dacComp.setLayout(gl);
        dacComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, false, false);
        Label dacLbl = new Label(dacComp, SWT.NONE);
        dacLbl.setText("DAC #: ");
        dacLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        dacValueLbl = new Label(dacComp, SWT.NONE);
        dacValueLbl.setText("2");
        dacValueLbl.setLayoutData(gd);

        // Transmitter port
        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gl = new GridLayout(2, false);
        Composite portComp = new Composite(transmitterGrp, SWT.NONE);
        portComp.setLayout(gl);
        portComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, false, false);
        Label portLbl = new Label(portComp, SWT.NONE);
        portLbl.setText("Port #: ");
        portLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        portValueLbl = new Label(portComp, SWT.NONE);
        portValueLbl.setText("3");
        portValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Composite progSuiteLegendComp = new Composite(comp, SWT.NONE);
        progSuiteLegendComp.setLayout(gl);
        progSuiteLegendComp.setLayoutData(gd);

        // Program/Suite group
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        gl.horizontalSpacing = 4;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Group progSuiteGrp = new Group(progSuiteLegendComp, SWT.NONE);
        progSuiteGrp.setText(" Program/Suite ");
        progSuiteGrp.setLayout(gl);
        progSuiteGrp.setLayoutData(gd);

        // Left Comp
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        leftProgSuiteComp = new Composite(progSuiteGrp, SWT.NONE);
        leftProgSuiteComp.setLayout(gl);
        leftProgSuiteComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label progLbl = new Label(leftProgSuiteComp, SWT.NONE);
        progLbl.setText("Program: ");
        progLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        progValueLbl = new Label(leftProgSuiteComp, SWT.NONE);
        progValueLbl.setLayoutData(gd);
        progValueLbl.setText("Program Name");

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label suiteLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteLbl.setText("Suite: ");
        suiteLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        suiteValueLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteValueLbl.setLayoutData(gd);
        suiteValueLbl.setText("Suite Name");

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label suiteCatLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteCatLbl.setText("Suite Category: ");
        suiteCatLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        suiteCatValueLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteCatValueLbl.setLayoutData(gd);
        suiteCatValueLbl.setText("TBD");

        // Right comp
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite rightComp = new Composite(progSuiteGrp, SWT.NONE);
        rightComp.setLayout(gl);
        rightComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label cycleDurLbl = new Label(rightComp, SWT.NONE);
        cycleDurLbl.setText("Cycle Duration: ");
        cycleDurLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        Label cycleDurValueLbl = new Label(rightComp, SWT.NONE);
        cycleDurValueLbl.setText("TBD");
        cycleDurValueLbl.setLayoutData(gd);

        // Change suite button
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.CENTER;
        Button changeSuiteBtn = new Button(rightComp, SWT.PUSH);
        changeSuiteBtn.setText("Change Suite...");
        changeSuiteBtn.setLayoutData(gd);
        changeSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleChangeSuiteAction();
            }
        });

        // Legend
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(2, false);
        gl.horizontalSpacing = 4;
        gl.verticalSpacing = 4;
        gl.marginHeight = 0;
        gl.marginWidth = 3;
        Composite legendComp = new Composite(progSuiteLegendComp, SWT.NONE);
        legendComp.setLayout(gl);
        legendComp.setLayoutData(gd);

        // Transmit time group
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Group transmitTimeGrp = new Group(legendComp, SWT.BORDER);
        transmitTimeGrp.setText(" Transmit Time ");
        transmitTimeGrp.setLayout(gl);
        transmitTimeGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite actComp = new Composite(transmitTimeGrp, SWT.NONE);
        actComp.setBackground(colorManager.getActualTransmitTimeColor());
        actComp.setLayout(gl);
        actComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Label lblActual = new Label(actComp, SWT.NONE);
        lblActual.setText(" Actual ");
        lblActual.setBackground(colorManager.getActualTransmitTimeColor());
        lblActual.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite predComp = new Composite(transmitTimeGrp, SWT.NONE);
        predComp.setBackground(colorManager.getPredictedTransmitTimeColor());
        predComp.setLayout(gl);
        predComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Label lblPredicted = new Label(predComp, SWT.NONE);
        lblPredicted
                .setBackground(colorManager.getPredictedTransmitTimeColor());
        lblPredicted.setText("Predicted");
        lblPredicted.setLayoutData(gd);

        // Message type group
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(2, false);
        Group msgTypeGrp = new Group(legendComp, SWT.BORDER);
        msgTypeGrp.setText(" Message Type ");
        msgTypeGrp.setLayout(gl);
        msgTypeGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite intComp = new Composite(msgTypeGrp, SWT.NONE);
        intComp.setBackground(colorManager.getInterruptColor());
        intComp.setLayout(gl);
        intComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Label interruptLbl = new Label(intComp, SWT.NONE);
        interruptLbl.setBackground(colorManager.getInterruptColor());
        interruptLbl.setText(" Interrupt ");
        interruptLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite perComp = new Composite(msgTypeGrp, SWT.NONE);
        perComp.setBackground(colorManager.getPeriodicColor());
        perComp.setLayout(gl);
        perComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Label periodicLbl = new Label(perComp, SWT.NONE);
        periodicLbl.setBackground(colorManager.getPeriodicColor());
        periodicLbl.setText(" Periodic ");
        periodicLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        gl = new GridLayout(1, false);
        Composite reComp = new Composite(msgTypeGrp, SWT.NONE);
        reComp.setBackground(colorManager.getReplaceColor());
        reComp.setLayout(gl);
        reComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        Label replaceLbl = new Label(reComp, SWT.NONE);
        replaceLbl.setBackground(colorManager.getReplaceColor());
        replaceLbl.setText(" MRD/MAT Replace ");
        replaceLbl.setLayoutData(gd);
    }

    private void createTable(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 150;
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.marginWidth = 0;
        tableComp = new BroadcastCycleTableComp(comp, SWT.BORDER | SWT.V_SCROLL
                | SWT.SINGLE, true, true);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);
    }

    private void createMessageText(Composite comp) {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label l = new Label(comp, SWT.NONE);
        l.setText("Message Text:");
        l.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 90;
        Text text = new Text(comp, SWT.BORDER | SWT.V_SCROLL);
        text.setLayoutData(gd);
        text.setLayoutData(gd);
    }

    private void populateTableData() {
        tableData = dataManager.getTableData(selectedTransmitter, program,
                selectedSuite, SuiteType.HIGH);

        tableComp.populateTable(tableData);
    }

    private void createBottomButtons(Composite comp) {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(4, false);
        gl.marginBottom = 0;
        Composite btnComp = new Composite(comp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        int btnWidth = 135;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button detailBtn = new Button(btnComp, SWT.PUSH);
        detailBtn.setText("Message Details...");
        detailBtn.setLayoutData(gd);
        detailBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleMessageDetails();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button periodicBtn = new Button(btnComp, SWT.PUSH);
        periodicBtn.setText("Periodic Messages...");
        periodicBtn.setLayoutData(gd);
        periodicBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handlePeriodicAction();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button copyBtn = new Button(btnComp, SWT.PUSH);
        copyBtn.setText("Copy Message...");
        copyBtn.setLayoutData(gd);
        copyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleCopyAction();
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button expireBtn = new Button(btnComp, SWT.PUSH);
        expireBtn.setText("Expire/Delete");
        expireBtn.setLayoutData(gd);
        expireBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleExpireAction();
            }
        });
    }

    private void updateTransmitter() {
        String selection = transmitterList.getItem(transmitterList
                .getSelectionIndex());
        String[] parts = selection.split(" - ");
        this.selectedTransmitter = parts[0];
        this.program = parts[1];
        setText(TITLE + ": " + selectedTransmitter);

    }

    private void handleMessageDetails() {
        if (detailsDlg == null || detailsDlg.isDisposed()) {
            detailsDlg = new MessageDetailsDlg(getShell());
            detailsDlg.open();
        } else {
            detailsDlg.bringToTop();
        }
    }

    private void handlePeriodicAction() {
        if (periodicMsgDlg == null || periodicMsgDlg.isDisposed()) {
            periodicMsgDlg = new PeriodicMessagesDlg(getShell());
            periodicMsgDlg.open();
        } else {
            periodicMsgDlg.bringToTop();
        }
    }

    private void handleCopyAction() {
        System.out.println("Copy Action...");
    }

    private void handleExpireAction() {
        System.out.println("Expire Action...");
    }

    private void handleChangeSuiteAction() {
        String[] selection = null;
        List<Suite> suiteList = dataManager.getSuites(selectedTransmitter);
        if (changeSuiteDlg == null || changeSuiteDlg.isDisposed()) {
            changeSuiteDlg = new SuiteListDlg(getShell(), suiteList);
            selection = (String[]) changeSuiteDlg.open();
            if (selection == null) {
                // User clicked cancel
                return;
            }
        } else {
            changeSuiteDlg.bringToTop();
        }
        updateSuiteData(selection);
    }

    private void updateSuiteData(String[] data) {
        if (data[0] != null && data[0].length() > 0) {
            this.suiteValueLbl.setText(data[0]);
            this.suiteCatValueLbl.setText(data[1]);
            this.leftProgSuiteComp.layout();
        }
    }

    @Override
    protected void disposed() {
        super.disposed();
        colorManager.dispose();
    }

    @Override
    public boolean okToClose() {
        // TODO fix this
        return true;
    }

    // TODO Delete this method
    private String[] getTestTransmitters() {
        String[] t = new String[23];

        for (int i = 0; i < 23; i++) {
            t[i] = "Trans" + i + " - Transmitter" + i;
        }

        return t;
    }
}
