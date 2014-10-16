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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.request.ForceSuiteChangeRequest;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.MonitorInlineThread.DisconnectListener;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

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
 * Aug 04, 2014  2487      bsteffen    Hook up the monitor inline checkbox.
 * Aug 14, 2014  3432      mpduff      Hook up details dialog
 * Aug 23, 2014  3432      mpduff      Add initial table population
 * Aug 25, 2014  3558      rjpeter     Updated to call getEnabledTransmitterGroupList.
 * Sep 25, 2014  3589      dgilling    Hook up Change Suite feature.
 * Oct 05, 2014  3647      mpduff      Changes to color manager.
 * Oct 08, 2014  #3479     lvenable    Added wrap and multi to the text control style and made it 
 *                                     read-only.  Moved populating the message text to the opened()
 *                                     method. Also added resize capability.
 * Oct 11, 2014  3725      mpduff      Remove the Copy Messages button.
 * Oct 15, 2014  3716      bkowal      Listen for and update based on Program Config Changes.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleDlg extends AbstractBMHDialog implements
        DisconnectListener, INotificationObserver {
    private final String NA = "N/A";

    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss");

    private final String BMH_DAC_STATUS = "BMH.DAC.Status";

    private final String BMH_CONFIG = "BMH.Config";

    private final String TITLE = "Broadcast Cycle";

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BroadcastCycleDlg.class);

    /** Table Data */
    private TableData tableData;

    /** Transmitter name -> TransmitterGroup map */
    private final Map<String, TransmitterGroup> transmitterGroupNameMap = new HashMap<>();;

    /** A Program Object */
    private Program programObj;

    /** The data manager */
    private final BroadcastCycleDataManager dataManager;

    /** The table composite */
    private BroadcastCycleTableComp tableComp;

    /** The list of transmitters */
    private org.eclipse.swt.widgets.List transmitterList;

    /** The checkbox to enable inline monitoring. */
    private Button monitorBtn;

    /**
     * The thread that is currently running to monitor the transmission or null
     * if is disabled.
     */
    private MonitorInlineThread monitorThread;

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

    /** Selected transmitter */
    private Label transmitterNameLbl;

    /** The selected transmitterGroup object */
    private TransmitterGroup selectedTransmitterGroupObject;

    /** Time Zone value label */
    private Label timeZoneValueLbl;

    /** Playlist data object */
    private PlaylistData playlistData;

    /** Message text area */
    private StyledText messageTextArea;

    /** The currently selected transmitter */
    private String selectedTransmitterGrp;

    /** Cycle duration value label */
    private Label cycleDurValueLbl;

    private String selectedSuite;

    /**
     * Constructor.
     * 
     * @param parent
     *            The parent shell
     * @param dlgMap
     *            The opend dialogs map
     */
    public BroadcastCycleDlg(Shell parent, Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Broadcast Cycle Dialog", parent, SWT.DIALOG_TRIM
                | SWT.RESIZE, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT);
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

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());

        /*
         * Populate the transmitters and table. This code needs to be in the
         * opened() method so the message text control is already packed when
         * setting the text. Otherwise the control will stretch to try and fit
         * the message if it is really long.
         */
        populateTransmitters();

        // TODO connect to topic
        NotificationManagerJob.addObserver(BMH_DAC_STATUS, this);
        NotificationManagerJob.addObserver(BMH_CONFIG, this);

        initialTablePopulation();
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
        colorManager = new BroadcastCycleColorManager(getShell());
        playlistData = new PlaylistData(getColumns(), getShell());
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

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        gl.marginBottom = 0;
        Composite broadcastComp = new Composite(mainComp, SWT.NONE);
        broadcastComp.setLayout(gl);
        broadcastComp.setLayoutData(gd);

        createUpperSection(broadcastComp);
        createTable(broadcastComp);
        createMessageText(broadcastComp);
        createBottomButtons(broadcastComp);
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
        help.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DialogUtility.notImplemented(getShell());
            }
        });

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
        transmitterList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateOnTransmitterChange();
            }
        });
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
        gd.widthHint = 55;
        transmitterNameLbl = new Label(nameComp, SWT.NONE);
        transmitterNameLbl.setLayoutData(gd);

        // Monitor inline
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        monitorBtn = new Button(transmitterGrp, SWT.CHECK);
        monitorBtn.setText("Monitor In-line  ");
        monitorBtn.setLayoutData(gd);
        monitorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent se) {
                handleMonitorInlineEvent();
            }
        });

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
        gd.widthHint = 125;
        timeZoneValueLbl = new Label(tzComp, SWT.NONE);
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
        gd.widthHint = 50;
        dacValueLbl = new Label(dacComp, SWT.NONE);
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
        gd.widthHint = 50;
        portValueLbl = new Label(portComp, SWT.NONE);
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
        gd.widthHint = 155;
        progValueLbl = new Label(leftProgSuiteComp, SWT.NONE);
        progValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label suiteLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteLbl.setText("Suite: ");
        suiteLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gd.widthHint = 250;
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
        gd.widthHint = 75;
        cycleDurValueLbl = new Label(rightComp, SWT.NONE);
        cycleDurValueLbl.setLayoutData(gd);
        cycleDurValueLbl.setText(NA);

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
        tableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                handleTableSelection();
            }
        });
        this.tableData = new TableData(getColumns());
        tableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);
        tableComp.populateTable(tableData);
    }

    private void createMessageText(Composite comp) {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label l = new Label(comp, SWT.NONE);
        l.setText("Message Text:");
        l.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 90;
        messageTextArea = new StyledText(comp, SWT.BORDER | SWT.WRAP
                | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
        messageTextArea.setLayoutData(gd);
    }

    private void initialTablePopulation() {
        try {
            PlaylistDataStructure dataStruct = dataManager
                    .getPlaylistDataForTransmitter(selectedTransmitterGrp);
            if (dataStruct != null) {
                playlistData.setData(selectedTransmitterGrp, dataStruct);
                tableData = playlistData
                        .getUpdatedTableData(selectedTransmitterGrp);
                tableComp.populateTable(tableData);
                if (tableData.getTableRowCount() > 0) {
                    tableComp.select(0);
                }
                handleTableSelection();
            }
        } catch (Exception e) {
            statusHandler.error("Error getting initial playback data", e);
        }
    }

    /**
     * Populate the transmitter list box
     */
    private void populateTransmitters() {
        try {
            List<TransmitterGroup> transmitterGroupObjectList = dataManager
                    .getEnabledTransmitterGroupList();
            Collections.sort(transmitterGroupObjectList,
                    new TransmitterGroupPositionComparator());
            String[] tNames = new String[transmitterGroupObjectList.size()];
            int idx = 0;
            for (TransmitterGroup tg : transmitterGroupObjectList) {
                tNames[idx] = tg.getName();
                idx++;
                transmitterGroupNameMap.put(tg.getName(), tg);
            }

            transmitterList.setItems(tNames);
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database.", e);
        }
        if (transmitterList.getItemCount() > 0) {
            transmitterList.select(0);
            updateOnTransmitterChange();
        }
    }

    private void createBottomButtons(Composite comp) {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(3, false);
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

    /**
     * Create the column objects for the Broadcast Cycle table.
     * 
     * @return List of {@link TableColumnData}
     */
    private List<TableColumnData> getColumns() {
        List<TableColumnData> columns = new ArrayList<>(8);
        columns.add(new TableColumnData("Transmit Time", 125));
        columns.add(new TableColumnData("Message Id", 100));
        columns.add(new TableColumnData("Message Title", 225));
        columns.add(new TableColumnData("MRD"));
        columns.add(new TableColumnData("Expiration Time", 125));
        columns.add(new TableColumnData("Alert"));
        columns.add(new TableColumnData("SAME"));
        columns.add(new TableColumnData("Play Count"));

        return columns;
    }

    /**
     * Called when user selects another Transmitter
     */
    private void updateOnTransmitterChange() {
        String selection = transmitterList.getItem(transmitterList
                .getSelectionIndex());
        String[] parts = selection.split(" - ");
        selectedTransmitterGrp = parts[0];
        setText(TITLE + ": " + selectedTransmitterGrp);
        transmitterNameLbl.setText(selectedTransmitterGrp);

        selectedTransmitterGroupObject = transmitterGroupNameMap
                .get(selectedTransmitterGrp);

        this.timeZoneValueLbl.setText(selectedTransmitterGroupObject
                .getTimeZone());

        Set<Integer> portSet = new TreeSet<>();
        for (Transmitter t : selectedTransmitterGroupObject.getTransmitters()) {
            if (t.getDacPort() != null) {
                portSet.add(t.getDacPort());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i : portSet) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(i);
        }

        String dacPort = sb.toString();
        this.portValueLbl.setText(dacPort);

        String dac = String.valueOf(selectedTransmitterGroupObject.getDac());
        if (dac.equalsIgnoreCase("null")) {
            dac = NA;
        }
        this.dacValueLbl.setText(dac);

        this.retrieveProgram();

        messageTextArea.setText("");
        initialTablePopulation();
        tableData = playlistData.getUpdatedTableData(selectedTransmitterGrp);
        tableComp.populateTable(tableData);
        tableComp.select(0);
        handleTableSelection();
        handleMonitorInlineEvent();
        suiteValueLbl.setText("");
        cycleDurValueLbl.setText("");
    }

    private void retrieveProgram() {
        try {
            programObj = dataManager
                    .getProgramForTransmitterGroup(selectedTransmitterGroupObject);
            if (programObj != null) {
                progValueLbl.setText(programObj.getName());
            }
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database", e);
        }
    }

    /**
     * Handler for Message Details button
     */
    private void handleMessageDetails() {
        if ((detailsDlg == null) || detailsDlg.isDisposed()) {
            try {
                List<TableRowData> selectionList = tableComp.getSelection();
                if (selectionList.isEmpty()) {
                    return;
                }
                TableRowData selection = selectionList.get(0);
                BroadcastCycleTableDataEntry dataEntry = (BroadcastCycleTableDataEntry) selection
                        .getData();
                String afosId = selection.getTableCellData().get(1)
                        .getCellText();
                MessageType messageType = dataManager.getMessageType(afosId);
                BroadcastMsg broadcastMsg = dataManager
                        .getBroadcastMessage(dataEntry.getBroadcastId());

                detailsDlg = new MessageDetailsDlg(getShell(), messageType,
                        broadcastMsg);
                detailsDlg.open();
            } catch (Exception e) {
                statusHandler.error("ERROR accessing BMH Database", e);
            }
        } else {
            detailsDlg.bringToTop();
        }
    }

    private void handleMonitorInlineEvent() {
        if (monitorThread != null) {
            monitorThread.removeDisconnectListener(BroadcastCycleDlg.this);
            monitorThread.cancel();
            monitorThread = null;
        }
        if (monitorBtn.getSelection()) {
            String tName = transmitterList.getSelection()[0];
            tName = tName.split("-")[0];
            TransmitterGroup selectedTransmitterGroup = transmitterGroupNameMap
                    .get(tName.trim());
            monitorThread = new MonitorInlineThread(
                    selectedTransmitterGroup.getName());
            monitorThread.addDisconnectListener(BroadcastCycleDlg.this);
            monitorThread.start();
        }
    }

    /**
     * Handler for periodic message button
     */
    private void handlePeriodicAction() {
        DialogUtility.notImplemented(getShell());
        // TODO pass in group and suite
        // if ((periodicMsgDlg == null) || periodicMsgDlg.isDisposed()) {
        // periodicMsgDlg = new PeriodicMessagesDlg(getShell(), selectedSuite,
        // selectedTransmitterGrp);
        // periodicMsgDlg.open();
        // } else {
        // periodicMsgDlg.bringToTop();
        // }
    }

    private void handleExpireAction() {
        // TODO
        DialogUtility.notImplemented(getShell());
    }

    /**
     * Suite change handler
     */
    private void handleChangeSuiteAction() {
        Suite selection = null;
        List<Suite> suiteList = programObj.getSuites();
        if ((changeSuiteDlg == null) || changeSuiteDlg.isDisposed()) {
            changeSuiteDlg = new SuiteListDlg(getShell(), suiteList);
            selection = (Suite) changeSuiteDlg.open();
        } else {
            changeSuiteDlg.bringToTop();
        }

        if (selection != null) {
            final TransmitterGroup group = selectedTransmitterGroupObject;
            final Suite suite = selection;

            statusHandler.info("User requested to change to suite ["
                    + suite.getName() + "] for transmitter group ["
                    + group.getName() + "].");

            /*
             * FIXME: If and when BMH viz code has a common method for making
             * async Thrift requests, replace this code with a call to that
             * common method.
             */
            Job requestJob = new Job("ForceSuiteChangeJob") {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    ForceSuiteChangeRequest request = new ForceSuiteChangeRequest(
                            group, suite);
                    try {
                        BmhUtils.sendRequest(request);
                    } catch (Exception e) {
                        statusHandler.error("Unable to change suites to ["
                                + suite.getName() + "].", e);
                    }
                    return Status.OK_STATUS;
                }
            };
            requestJob.setSystem(true);
            requestJob.schedule();
        }
    }

    /**
     * Display the message text
     */
    private void handleTableSelection() {
        List<TableRowData> selectionList = tableComp.getSelection();
        if (CollectionUtils.isNotEmpty(selectionList)) {
            TableRowData trd = selectionList.get(0);
            BroadcastCycleTableDataEntry entry = (BroadcastCycleTableDataEntry) trd
                    .getData();
            String content = entry.getInputMsg().getContent();
            this.messageTextArea.setText(content);
        }
    }

    @Override
    protected void disposed() {
        super.disposed();
        if (monitorThread != null) {
            monitorThread.cancel();
            monitorThread.removeDisconnectListener(this);
            monitorThread = null;
        }
        NotificationManagerJob.removeObserver(BMH_DAC_STATUS, this);
    }

    @Override
    public boolean okToClose() {
        // TODO fix this
        return true;
    }

    @Override
    public void disconnected(Throwable error) {
        if (error != null) {
            getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!monitorBtn.isDisposed()) {
                        monitorBtn.setSelection(false);
                    }
                    if (monitorThread != null) {
                        monitorThread
                                .removeDisconnectListener(BroadcastCycleDlg.this);
                        monitorThread = null;
                    }
                }
            });
        }
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                if (o instanceof PlaylistSwitchNotification) {
                    final PlaylistSwitchNotification notification = (PlaylistSwitchNotification) o;
                    playlistData.handlePLaylistSwitchNotification(notification);
                    if (notification.getTransmitterGroup().equals(
                            selectedTransmitterGrp)) {
                        tableData = playlistData
                                .getUpdatedTableData(notification
                                        .getTransmitterGroup());
                        updateTable(tableData);
                        this.selectedSuite = notification.getSuiteName();
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                suiteValueLbl.setText(selectedSuite);
                                String time = timeFormatter.format(new Date(
                                        notification.getPlaybackCycleTime()));
                                cycleDurValueLbl.setText(time);
                            }
                        });
                    }
                } else if (o instanceof MessagePlaybackStatusNotification) {
                    MessagePlaybackStatusNotification notification = (MessagePlaybackStatusNotification) o;
                    playlistData.handlePlaybackStatusNotification(notification);
                    if (notification.getTransmitterGroup().equals(
                            selectedTransmitterGrp)) {
                        tableData = playlistData
                                .getUpdatedTableData(notification
                                        .getTransmitterGroup());
                        updateTable(tableData);
                    }
                } else if (o instanceof ProgramConfigNotification) {
                    ProgramConfigNotification pgmConfigNotification = (ProgramConfigNotification) o;
                    /*
                     * does this apply to the program we have selected?
                     */
                    if (this.programObj.getId() != pgmConfigNotification
                            .getId()) {
                        return;
                    }
                    /*
                     * Refresh the selected program object.
                     */
                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            retrieveProgram();

                            if (changeSuiteDlg != null
                                    && changeSuiteDlg.isDisposed() == false) {
                                changeSuiteDlg.updateSuites(programObj
                                        .getSuites());
                            }
                        }
                    });
                }
            } catch (NotificationException e) {
                statusHandler.error("Error processing update notification", e);
            }
        }
    }

    /**
     * Update the table.
     * 
     * @param tableData
     *            The updated TableData
     */
    private void updateTable(final TableData tableData) {
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                tableComp.populateTable(tableData);
            }
        });
    }
}
