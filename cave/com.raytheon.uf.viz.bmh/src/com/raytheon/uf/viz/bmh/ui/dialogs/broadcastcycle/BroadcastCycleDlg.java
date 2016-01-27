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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.TimeTextFragment;
import com.raytheon.uf.common.bmh.broadcast.ExpireBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.notify.DacTransmitShutdownNotification;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupIdentifier;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.bmh.request.ForceSuiteChangeRequest;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.dialogs.notify.BMHDialogNotificationManager;
import com.raytheon.uf.viz.bmh.dialogs.notify.IDialogNotification;
import com.raytheon.uf.viz.bmh.dialogs.notify.IDialogNotificationListener;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.MonitorInlineThread.DisconnectListener;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackCompleteNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioRecordPlaybackNotification;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

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
 * Oct 17, 2014  3687      bsteffen    Support practice servers.
 * Oct 21, 2014  3655      bkowal      Added support for live broadcast messages.
 * Oct 23, 2014  3687      bsteffen    Display dac name instead of id.
 * Oct 26, 2014  3750      mpduff      Maintain the selected row.
 * Oct 27, 2014  3712      bkowal      Update labels after live broadcast start/end.
 * Nov 01, 2014  3782      mpduff      Added message name column and made table grow with dialog
 * Nov 01, 2014  3655      bkowal      Prevent NPE.
 * Nov 04, 2014  3792      lvenable    Colored the Emergency Override label blue and fixed a NPE.
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Nov 12, 2014  3816      lvenable    Fixed transmitter label size.
 * Nov 15, 2014  3818      mpduff      Allow multiple message detail dialogs to be open.
 * Nov 17, 2014  3808      bkowal      Support broadcast live.
 * Nov 18, 2014  3807      bkowal      Use BMHJmsDestinations.
 * Nov 19, 2014  3817      bsteffen    Use status queue for more than just dacs.
 * Nov 21, 2014  3845      bkowal      {@link LiveBroadcastSwitchNotification} now includes the
 *                                     full {@link TransmitterGroup}.
 * Nov 30, 2014  3752      mpduff      Populate Suite name, suite category, and cycle duration on startup.
 * Dec 09, 2014  3904      bkowal      Disable inline monitoring whenever a {@link AudioRecordPlaybackNotification}
 *                                     is received.
 * Dec 11, 2014  3895      lvenable    Changed time to GMT.
 * Dec 13, 2014  3843      mpduff      Implement periodic messages.
 * Dec 16, 2014  3753      bsteffen    Add popup when suite change fails.
 * Dec 18, 2014  3865      bsteffen    Implement Expire/Delete
 * Jan 13, 2015  3843      bsteffen    Enhance Periodic Messages Dialog.
 * Jan 15, 2015  3844      bsteffen    Handle unusuals states with less NPE.
 * Jan 19, 2015  3929      lvenable    Added safety checks if the program object is null.
 * Feb 02, 2015  4044      bsteffen    Confirm expire/delete.
 * Feb 05, 2015  4090      bkowal      Update the suite category whenever the playlist
 *                                     changes.
 * Feb 10, 2015  4106      bkowal      Support caching live broadcast information.
 * Feb 11, 2015  4088      bkowal      Update the listed transmitters to reflect the
 *                                     active transmitters as they are changed.
 * Feb 16, 2015  4112      bkowal      Re-enable inline monitoring if it was previously enabled when
 *                                     a {@link AudioPlaybackCompleteNotification} is received.
 * Feb 16, 2015  4118      bkowal      Check for disposal of components updated
 *                                     asynchronously.
 * Feb 26, 2015  4187      rjpeter     Added isDisposed checks.
 * Mar 13, 2015  4270      rferrel     Update display when selected group modified or removed.
 * Mar 16, 2015  4285      bkowal      Fix transmitter list updates. Handle both 4270 and 4285.
 * Mar 31, 2015  4340      bkowal      Update the suite list dialog based on the currently
 *                                     selected suite.
 * Apr 01, 2015  4349      rferrel     Checks to prevent exceptions when no enabled transmitters.
 * Apr 02, 2015  4248      rjpeter     Removed TransmitterGroupPositionComparator.
 * Apr 14, 2015  4394      bkowal      Display all configured transmitters with color coding.
 * Apr 15, 2015  4293      bkowal      Use {@link ExpireBroadcastMsgRequest} when only specific
 *                                     broadcast messages have been expired.
 * Apr 20, 2015  4397      bkowal      Set the expiration request time on the {@link NewBroadcastMsgRequest}.
 * Apr 21, 2015  4423      rferrel     Added {@link #timeZoneValueLbl} to display transmitter's time zone.
 * Apr 21, 2015  4394      bkowal      Purge any cached playlist information whenever a
 *                                     {@link DacTransmitShutdownNotification} is received.
 * Apr 29, 2015  4394      bkowal      Support {@link INonStandardBroadcast}.
 * Apr 29, 2015  4449      bkowal      Specify all {@link Transmitter}s in the {@link NewBroadcastMsgRequest}
 *                                     when a message is expired on all.
 * May 04, 2015  4449      bkowal      Only allow the user to expire messages on transmitters that it has
 *                                     actually been scheduled on.
 * May 19, 2015  4482      rjpeter     Added handling of ResetNotification.
 * May 22, 2015  4481      bkowal      Message contents can now be constructed dynamically for messages
 *                                     with dynamic text.
 * May 28, 2015  4490      bkowal      Handle unplayed messages with dynamic text.
 * Jun 02, 2015  4369      rferrel     Handle {@link NoPlaybackMessageNotification}.
 * Jun 05, 2015  4490      rjpeter     Updated constructor.
 * Jun 12, 2015  4482      rjpeter     Added DO_NOT_BLOCK.
 * Jun 18, 2015  4490      bkowal      Force reload an audio stream when the configuration
 *                                     of the selected dac is altered.
 * Jun 19, 2015  4481      bkowal      Set the time zone on the {@link SimpleDateFormat}.
 * Jun 22, 2015  4481      bkowal      Display the timezone display id in the time zone field.
 * Jul 13, 2015  4636      bkowal      Handle initial maintenance playback statuses.
 * Jan 05, 2016  4997      bkowal      Allow toggling between transmitters/groups.
 * Jan 27, 2016  5160      rjpeter     Left align MRD column.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleDlg extends AbstractBMHDialog implements
        DisconnectListener, INotificationObserver, IDialogNotificationListener {
    private final String NA = "N/A";

    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss");

    private final String dynamicTimeFormat = "h:mm a";

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BroadcastCycleDlg.class);

    /** Table Data */
    private TableData tableData;

    /** A Program Object */
    private Program programObj;

    /** The data manager */
    private final BroadcastCycleDataManager dataManager;

    /** The dac data manager */
    private final DacDataManager dacDataManager;

    /** The table composite */
    private BroadcastCycleTableComp tableComp;

    /** The list of transmitters */
    private Table transmitterTable;

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

    /** Selected transmitter */
    private Label transmitterNameLbl;

    /** Selected transmitter's time zone. */
    private Label timeZoneValueLbl;

    /** The selected transmitterGroup object */
    private TransmitterGroup selectedTransmitterGroupObject;

    /** Playlist data object */
    private PlaylistData playlistData;

    /** Message text area */
    private StyledText messageTextArea;

    /** The currently selected transmitter */
    private String selectedTransmitterGrp;

    private String selectedTransmitter;

    /** Cycle duration value label */
    private Label cycleDurValueLbl;

    private String selectedSuite;

    private Button messageDetailBtn;

    private int selectedRow = 0;

    private String cycleDurationTime;

    /** Emergency override font. */
    private Font liveBroadcastFont;

    private final String eoText = "Emergency Override";

    private final String blText = "Broadcast Live";

    /** Map of BroadcastMessage id to MessageDetailsDlg for that id */
    private final Map<Long, MessageDetailsDlg> detailsMap = new HashMap<>();

    /**
     * Flag used to track the state of the inline selection prior to audio
     * playback from an external location.
     */
    private volatile boolean inlinePreviouslyEnabled;

    /**
     * "lock" used to ensure that the dialog does not respond to monitor inline
     * events and disposal events simultaneously.
     */
    private final Object disposalLock = new Object();

    /**
     * checkbox used to toggle between displaying transmitters/groups for
     * selection.
     */
    protected Button groupToggleBtn;

    /**
     * Constructor.
     * 
     * @param parent
     *            The parent shell
     */
    public BroadcastCycleDlg(Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        this.dataManager = new BroadcastCycleDataManager();
        this.dacDataManager = new DacDataManager();
        setText(DlgInfo.BROADCAST_CYCLE.getTitle());
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

        BMHDialogNotificationManager.getInstance().register(this);

        /*
         * Populate the transmitters and table. This code needs to be in the
         * opened() method so the message text control is already packed when
         * setting the text. Otherwise the control will stretch to try and fit
         * the message if it is really long.
         */
        populateTransmitters(true);

        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getStatusDestination(), this);
        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);

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

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        gl.marginBottom = 0;
        Composite broadcastComp = new Composite(mainComp, SWT.NONE);
        broadcastComp.setLayout(gl);
        broadcastComp.setLayoutData(gd);

        createUpperSection(broadcastComp);
        createTable(broadcastComp);
        createMessageText(broadcastComp);
        createBottomButtons(broadcastComp);

        FontData fd = progValueLbl.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        liveBroadcastFont = new Font(getDisplay(), fd);
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
        this.groupToggleBtn = new Button(transGrp, SWT.CHECK);
        this.groupToggleBtn.setText("Transmitter Group");
        this.groupToggleBtn.setSelection(true);
        this.groupToggleBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleGroupsTransmitters();
            }
        });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 125;
        this.transmitterTable = new Table(transGrp, SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.SINGLE | SWT.BORDER);
        this.transmitterTable.setLayoutData(gd);
        transmitterTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                closeChangeSuiteDlg();
                updateOnTransmitterChange(true);
            }
        });
    }

    private void toggleGroupsTransmitters() {
        this.populateTransmitters(false);
        transmitterTable.select(getSelectedIndex());
        updateOnTransmitterChange(false);
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
        gd.widthHint = 250;
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
        gd.horizontalIndent = 10;
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
        dacLbl.setText("DAC: ");
        dacLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.widthHint = 90;
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

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        Label suiteCatLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteCatLbl.setText("Suite Category: ");
        suiteCatLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gd.widthHint = 250;
        suiteCatValueLbl = new Label(leftProgSuiteComp, SWT.NONE);
        suiteCatValueLbl.setLayoutData(gd);

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
            IPlaylistData playlistDataRecord = this.dataManager
                    .getPlaylistDataForTransmitter(selectedTransmitterGrp);
            PlaylistDataStructure dataStruct = null;
            LiveBroadcastSwitchNotification liveNotification = null;
            MaintenanceMessagePlayback maintNotification = null;
            if (playlistDataRecord instanceof PlaylistDataStructure) {
                dataStruct = (PlaylistDataStructure) playlistDataRecord;
            } else if (playlistDataRecord instanceof LiveBroadcastSwitchNotification) {
                liveNotification = (LiveBroadcastSwitchNotification) playlistDataRecord;
                dataStruct = liveNotification.getActualPlaylist();
            } else if (playlistDataRecord instanceof MaintenanceMessagePlayback) {
                maintNotification = (MaintenanceMessagePlayback) playlistDataRecord;
                this.playlistData
                        .handleMaintenanceNotification((MaintenanceMessagePlayback) playlistDataRecord);
            }

            if (dataStruct != null) {
                if (selectedTransmitterGrp != null) {
                    playlistData.setData(selectedTransmitterGrp, dataStruct);
                }
                this.selectedSuite = dataStruct.getSuiteName();
            }

            if ((liveNotification == null) && (dataStruct != null)) {
                if (this.selectedSuite != null) {
                    suiteValueLbl.setText(this.selectedSuite);
                } else {
                    /*
                     * A Transmitter in maintenance mode or that is disabled
                     * will not have an associated suite.
                     */
                    suiteValueLbl.setText(StringUtils.EMPTY);
                }
                cycleDurValueLbl.setText(timeFormatter.format(new Date(
                        dataStruct.getPlaybackCycleTime())));
                suiteCatValueLbl.setText(this.getCategoryForCurrentSuite());

                if (selectedTransmitterGrp != null) {
                    tableData = playlistData
                            .getUpdatedTableData(selectedTransmitterGrp);
                }
                tableComp.populateTable(tableData);
                if (tableData.getTableRowCount() > 0) {
                    tableComp.select(0);
                }
                handleTableSelection();
            } else {
                INonStandardBroadcast notification = null;
                if (liveNotification != null) {
                    playlistData
                            .handleLiveBroadcastSwitchNotification(liveNotification);
                    notification = liveNotification;
                } else {
                    this.playlistData
                            .handleMaintenanceNotification(maintNotification);
                    notification = maintNotification;
                }
                TableData tableData = playlistData
                        .getNonStandardTableData(notification);
                this.updateDisplayForNonStandardBroadcast(tableData,
                        notification);
            }
        } catch (Exception e) {
            statusHandler.error("Error getting initial playback data", e);
        }
    }

    /**
     * Populate the transmitter list box
     */
    private void populateTransmitters(boolean select0thTransmitter) {
        try {
            List<TransmitterGroup> transmitterGroupObjectList = dataManager
                    .getConfiguredTransmitterGroupList();
            Collections.sort(transmitterGroupObjectList,
                    new PositionComparator());
            if (this.transmitterTable.getItemCount() > 0) {
                this.transmitterTable.removeAll();
            }
            for (TransmitterGroup tg : transmitterGroupObjectList) {
                List<Transmitter> transmitterObjectList = tg
                        .getOrderedConfiguredTransmittersList();

                if (this.groupToggleBtn.getSelection()) {
                    TableItem ti = new TableItem(this.transmitterTable, 0);
                    ti.setText(tg.getName());
                    /*
                     * Find the first enabled transmitter. If none are enabled,
                     * find the first transmitter in maintenance mode. If none
                     * are in maintenance mode, choose the first transmitter in
                     * the group.
                     */
                    Set<Transmitter> enabledTransmitters = tg
                            .getTransmitterWithStatus(TxStatus.ENABLED);
                    if (CollectionUtils.isNotEmpty(enabledTransmitters)) {
                        ti.setData(enabledTransmitters.iterator().next());
                        continue;
                    }
                    // check for maintenance
                    Set<Transmitter> maintTransmitters = tg
                            .getTransmitterWithStatus(TxStatus.MAINT);
                    if (CollectionUtils.isNotEmpty(maintTransmitters)) {
                        ti.setData(maintTransmitters.iterator().next());
                        ti.setBackground(this.getDisplay().getSystemColor(
                                SWT.COLOR_DARK_BLUE));
                        ti.setForeground(this.getDisplay().getSystemColor(
                                SWT.COLOR_WHITE));
                        continue;
                    }
                    // just retrieve the first in the list.
                    ti.setData(tg.getTransmitterList().get(0));
                    ti.setBackground(this.getDisplay().getSystemColor(
                            SWT.COLOR_RED));
                    ti.setForeground(this.getDisplay().getSystemColor(
                            SWT.COLOR_LIST_FOREGROUND));
                } else {
                    for (Transmitter t : transmitterObjectList) {
                        TableItem ti = new TableItem(this.transmitterTable, 0);
                        ti.setText(t.getMnemonic());
                        ti.setData(t);
                        if (t.getTxStatus() == TxStatus.DISABLED) {
                            ti.setBackground(this.getDisplay().getSystemColor(
                                    SWT.COLOR_RED));
                            ti.setForeground(this.getDisplay().getSystemColor(
                                    SWT.COLOR_LIST_FOREGROUND));
                        } else if (t.getTxStatus() == TxStatus.MAINT) {
                            ti.setBackground(this.getDisplay().getSystemColor(
                                    SWT.COLOR_DARK_BLUE));
                            ti.setForeground(this.getDisplay().getSystemColor(
                                    SWT.COLOR_WHITE));
                        }
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database.", e);
        }
        if (select0thTransmitter) {
            if (this.transmitterTable.getItemCount() > 0) {
                this.transmitterTable.select(0);
                updateOnTransmitterChange(select0thTransmitter);
            }
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
        messageDetailBtn = new Button(btnComp, SWT.PUSH);
        messageDetailBtn.setText("Message Details...");
        messageDetailBtn.setLayoutData(gd);
        messageDetailBtn.addSelectionListener(new SelectionAdapter() {
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
        columns.add(new TableColumnData("Transmit Time (UTC)", 125));
        columns.add(new TableColumnData("Message Id", 100));
        columns.add(new TableColumnData("Message Title", 225));
        columns.add(new TableColumnData("Message Name", 175));
        columns.add(new TableColumnData("MRD", 20, SWT.LEFT));
        columns.add(new TableColumnData("Expiration Time (UTC)", 125));
        columns.add(new TableColumnData("Alert"));
        columns.add(new TableColumnData("SAME"));
        columns.add(new TableColumnData("Play Count"));

        return columns;
    }

    /**
     * Called when user selects another Transmitter
     * 
     * @param reloadAudioStream
     *            boolean indicating whether or not the audio stream should
     *            potentially be re-initialized based on the state of the
     *            monitor inline checkbox
     */
    private void updateOnTransmitterChange(boolean reloadAudioStream) {
        if (this.transmitterTable.getSelectionCount() <= 0) {
            return;
        }
        Transmitter transmitter = (Transmitter) this.transmitterTable.getItem(
                this.transmitterTable.getSelectionIndex()).getData();
        if (reloadAudioStream == false) {
            synchronized (this.disposalLock) {
                /*
                 * Determine if the audio stream needs to be reloaded either
                 * because the dac or the dac port has been altered.
                 */
                reloadAudioStream = (this.monitorThread != null)
                        && ((this.selectedTransmitterGroupObject != null) && ((transmitter
                                .getTransmitterGroup().getDac() != this.selectedTransmitterGroupObject
                                .getDac()) || (this.monitorThread.getChannel() != transmitter
                                .getDacPort())));
            }
        }
        selectedTransmitterGroupObject = transmitter.getTransmitterGroup();
        selectedTransmitterGrp = selectedTransmitterGroupObject.getName();
        this.selectedTransmitter = transmitter.getMnemonic();
        setText(DlgInfo.BROADCAST_CYCLE.getTitle() + ": "
                + selectedTransmitterGrp + " - Channel "
                + transmitter.getDacPort());
        transmitterNameLbl.setText(selectedTransmitterGrp);
        timeZoneValueLbl.setText(BMHTimeZone.getTimeZoneByID(
                this.selectedTransmitterGroupObject.getTimeZone())
                .getDisplayId());

        if ((selectedTransmitterGrp != null)
                && (selectedTransmitterGrp.isEmpty() == false)) {
            transmitterNameLbl.setToolTipText(selectedTransmitterGrp);
        } else {
            transmitterNameLbl.setToolTipText(null);
        }

        this.portValueLbl.setText(Integer.toString(transmitter.getDacPort()));

        String dac = null;
        try {
            dac = new DacDataManager()
                    .getDacNameById(selectedTransmitterGroupObject.getDac());
        } catch (Exception e) {
            statusHandler.error("Error retreiving DAC information", e);
            dac = "<error>";
        }
        if (dac == null) {
            dac = NA;
        }
        this.dacValueLbl.setText(dac);

        this.retrieveProgram();

        messageTextArea.setText("");
        INonStandardBroadcast notification = playlistData
                .getNonStandardBroadcast(selectedTransmitterGrp);
        if (notification == null) {
            initialTablePopulation();
            tableData = playlistData
                    .getUpdatedTableData(selectedTransmitterGrp);
            tableComp.populateTable(tableData);
            handleTableSelection();
        } else {
            tableData = playlistData.getNonStandardTableData(notification);
            this.updateDisplayForNonStandardBroadcast(tableData, notification);
        }
        if (reloadAudioStream) {
            handleMonitorInlineEvent();
        }
    }

    private void retrieveProgram() {
        try {
            programObj = dataManager
                    .getProgramForTransmitterGroup(selectedTransmitterGroupObject);
            if (programObj != null) {
                setProgramLabelTextFontAndColor(programObj.getName());
            }
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database", e);
        }
    }

    /**
     * Handler for Message Details button
     */
    private void handleMessageDetails() {
        try {
            List<TableRowData> selectionList = tableComp.getSelection();
            if (selectionList.isEmpty()) {
                return;
            }
            TableRowData selection = selectionList.get(0);
            BroadcastCycleTableDataEntry dataEntry = (BroadcastCycleTableDataEntry) selection
                    .getData();
            String afosId = selection.getTableCellData().get(1).getCellText();
            MessageType messageType = dataManager.getMessageType(afosId);
            BroadcastMsg broadcastMsg = dataManager
                    .getBroadcastMessage(dataEntry.getBroadcastId());
            if (broadcastMsg == null) {
                String message = "ERROR accessing BMH Database, unable to load any details for message "
                        + dataEntry.getBroadcastId();
                statusHandler.debug(message);
                MessageBox mb = new MessageBox(getShell(), SWT.OK
                        | SWT.ICON_ERROR);
                mb.setText("Failed to Load Message Details.");
                mb.setMessage(message);
                mb.open();
                return;
            }
            long key = broadcastMsg.getId();
            MessageDetailsDlg dlg = detailsMap.get(key);
            if (dlg != null) {
                dlg.bringToTop();
            } else {
                MessageDetailsDlg detailsDlg = new MessageDetailsDlg(
                        getShell(), messageType, broadcastMsg);
                detailsDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        long key = (long) returnValue;
                        detailsMap.remove(key);
                    }
                });
                detailsDlg.open();
                detailsMap.put(key, detailsDlg);
            }
        } catch (Exception e) {
            statusHandler.error("ERROR accessing BMH Database", e);
        }
    }

    private void handleMonitorInlineEvent() {
        synchronized (this.disposalLock) {
            if (monitorThread != null) {
                monitorThread.removeDisconnectListener(BroadcastCycleDlg.this);
                monitorThread.cancel();
                monitorThread = null;
            }
            if (monitorBtn.isDisposed()) {
                return;
            }
            if (monitorBtn.getSelection()
                    && (this.transmitterTable.getSelectionCount() != 0)) {
                Transmitter transmitter = (Transmitter) this.transmitterTable
                        .getItem(this.transmitterTable.getSelectionIndex())
                        .getData();
                final int dacId = transmitter.getTransmitterGroup().getDac();

                final Dac dac;
                try {
                    dac = this.dacDataManager.getDacById(dacId);
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to retrieve the information for dac: "
                                    + dacId + ".", e);
                    this.monitorBtn.setSelection(false);
                    return;
                }

                monitorThread = new MonitorInlineThread(transmitter
                        .getTransmitterGroup().getName(),
                        dac.getReceiveAddress(), dac.getReceivePort(),
                        transmitter.getDacPort());
                monitorThread.addDisconnectListener(BroadcastCycleDlg.this);
                monitorThread.start();
            }
        }
    }

    /**
     * Handler for periodic message button
     */
    private void handlePeriodicAction() {
        if (selectedTransmitterGrp == null) {
            return;
        }
        if ((periodicMsgDlg == null) || periodicMsgDlg.isDisposed()) {
            periodicMsgDlg = new PeriodicMessagesDlg(getShell(), dataManager,
                    playlistData, selectedTransmitterGrp);
            periodicMsgDlg.open();
        } else {
            periodicMsgDlg.bringToTop();
        }
    }

    private void handleExpireAction() {
        List<TableRowData> selectionList = tableComp.getSelection();
        if (selectionList.isEmpty()) {
            return;
        }
        TableRowData selection = selectionList.get(0);
        BroadcastCycleTableDataEntry dataEntry = (BroadcastCycleTableDataEntry) selection
                .getData();
        try {
            BroadcastMsg broadcastMsg = dataManager
                    .getBroadcastMessage(dataEntry.getBroadcastId());
            if (broadcastMsg == null) {
                String message = "Error expiring message "
                        + dataEntry.getBroadcastId()
                        + ": No broadcast message found.";
                statusHandler.debug(message);
                MessageBox mb = new MessageBox(getShell(), SWT.OK
                        | SWT.ICON_ERROR);
                mb.setText("Failed to Expire Message.");
                mb.setMessage(message);
                mb.open();
                return;
            }
            final InputMessage inputMessage = broadcastMsg.getInputMessage();
            List<BroadcastMsg> messages = dataManager
                    .getBroadcastMessagesForInputMessage(inputMessage.getId());
            Iterator<BroadcastMsg> it = messages.iterator();
            while (it.hasNext()) {
                if (it.next().getForcedExpiration()) {
                    /*
                     * Exclude individual broadcast messages that have already
                     * been expired.
                     */
                    it.remove();
                }
            }
            if (messages.size() == 1) {
                this.expireMessageOneDestination(inputMessage, broadcastMsg);
                return;
            }

            CheckListData cld = new CheckListData();

            int scheduledCount = 0;
            BroadcastMsg scheduledBroadcastMsg = null;
            final Set<Transmitter> allTransmittersSet = new HashSet<>();
            final Map<String, BroadcastMsg> transmitterGrpToBroadcastMsgMap = new HashMap<>();
            for (BroadcastMsg message : messages) {
                TransmitterGroup transmitterGroup = message
                        .getTransmitterGroup();
                /*
                 * Determine if the message has actually been scheduled for
                 * broadcast.
                 */
                boolean scheduled = false;
                try {
                    scheduled = this.dataManager
                            .isMessageScheduledForBroadcast(
                                    transmitterGroup.getName(), message.getId());
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder(
                            "Failed to determine if broadcast message: ");
                    sb.append(message.getId())
                            .append(" has been scheduled for broadcast on transmitter: ");
                    sb.append(transmitterGroup.getName()).append(".");

                    statusHandler.error(sb.toString(), e);
                }
                if (scheduled == false) {
                    /*
                     * Note: if the user expires the message on all Transmitters
                     * that the message has been expired on, the message will be
                     * permanently expired and it will not be broadcast on other
                     * transmitters that it has not previously been scheduled on
                     * even if they are enabled.
                     */
                    continue;
                }

                scheduledBroadcastMsg = message;
                ++scheduledCount;

                transmitterGrpToBroadcastMsgMap.put(transmitterGroup.getName(),
                        message);
                cld.addDataItem(transmitterGroup.getName(), true);
                allTransmittersSet.addAll(transmitterGroup.getTransmitters());
            }

            if (scheduledCount == 1) {
                /*
                 * Only one message has actually been scheduled for broadcast.
                 */
                this.expireMessageOneDestination(inputMessage,
                        scheduledBroadcastMsg);
                return;
            }

            String dialogText = "Expiring " + inputMessage.getName()
                    + "\n\nSelect Transmitter Groups:";

            CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                    "Expire Selection", dialogText, cld, true);
            checkListDlg.setCloseCallback(new ICloseCallback() {
                @Override
                public void dialogClosed(Object returnValue) {
                    if ((returnValue != null)
                            && (returnValue instanceof CheckListData)) {
                        handleExpireDialogCallback(inputMessage,
                                (CheckListData) returnValue,
                                transmitterGrpToBroadcastMsgMap,
                                allTransmittersSet);
                    }
                }

            });
            checkListDlg.open();
        } catch (Exception e) {
            statusHandler.error(
                    "Error expiring message: " + dataEntry.getBroadcastId(), e);
        }
    }

    private void expireMessageOneDestination(InputMessage inputMessage,
            BroadcastMsg broadcastMsg) throws Exception {
        String message = "Are you sure you want to Expire/Delete "
                + inputMessage.getName() + "?";
        MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.CANCEL
                | SWT.ICON_QUESTION);
        mb.setText("Confirm Expire/Delete");
        mb.setMessage(message);
        if (mb.open() == SWT.OK) {
            NewBroadcastMsgRequest request = new NewBroadcastMsgRequest(
                    System.currentTimeMillis());
            inputMessage.setActive(false);
            request.setInputMessage(inputMessage);
            request.setSelectedTransmitters(new ArrayList<>(broadcastMsg
                    .getTransmitterGroup().getTransmitters()));
            BmhUtils.sendRequest(request);
        }
    }

    private void handleExpireDialogCallback(InputMessage inputMessage,
            CheckListData data,
            Map<String, BroadcastMsg> transmitterGrpToBroadcastMsgMap,
            Set<Transmitter> allTransmittersSet) {
        if (data.allChecked()) {
            NewBroadcastMsgRequest request = new NewBroadcastMsgRequest(
                    System.currentTimeMillis());
            inputMessage.setActive(false);
            request.setInputMessage(inputMessage);
            request.setSelectedTransmitters(new ArrayList<>(allTransmittersSet));
            this.sendExpireRequest(request);
        } else {
            List<BroadcastMsg> messagesToExpire = new ArrayList<>();
            for (Entry<String, Boolean> entry : data.getDataMap().entrySet()) {
                if (entry.getValue()) {
                    messagesToExpire.add(transmitterGrpToBroadcastMsgMap
                            .get(entry.getKey()));
                }
            }
            if (messagesToExpire.isEmpty()) {
                return;
            }
            ExpireBroadcastMsgRequest request = new ExpireBroadcastMsgRequest();
            request.setExpiredBroadcastMsgs(messagesToExpire);
            this.sendExpireRequest(request);
        }
    }

    private void sendExpireRequest(AbstractBMHServerRequest request) {
        try {
            BmhUtils.sendRequest(request);
        } catch (Exception e) {
            statusHandler.error("Error expiring message.", e);
        }
    }

    /**
     * Suite change handler
     */
    private void handleChangeSuiteAction() {
        if (programObj == null) {
            return;
        }
        Suite selection = null;
        List<Suite> suiteList = programObj.getSuites();
        if ((changeSuiteDlg == null) || changeSuiteDlg.isDisposed()) {
            changeSuiteDlg = new SuiteListDlg(getShell(), suiteList,
                    this.selectedSuite);
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
                        Boolean result = (Boolean) BmhUtils
                                .sendRequest(request);
                        if (result == false) {
                            handleChangeSuiteFailure(request.getSelectedSuite());
                        }
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
     * Show a MessageBox indicating that a change suite has failed.
     * 
     * @param suite
     *            The suite that failed to start.
     */
    private void handleChangeSuiteFailure(final Suite suite) {
        VizApp.runSync(new Runnable() {

            @Override
            public void run() {
                MessageBox mb = new MessageBox(getShell(), SWT.OK
                        | SWT.ICON_WARNING);
                mb.setText("Failed to Change Suite.");
                mb.setMessage(suite.getName()
                        + " does not contain any valid messages and will not be played.");
                mb.open();
            }

        });
    }

    /**
     * Display the message text
     */
    private void handleTableSelection() {
        List<TableRowData> selectionList = tableComp.getSelection();
        if (CollectionUtils.isNotEmpty(selectionList)) {
            this.selectedRow = tableComp.getSelectedIndex();
            TableRowData trd = selectionList.get(0);
            BroadcastCycleTableDataEntry entry = (BroadcastCycleTableDataEntry) trd
                    .getData();

            // If the entry is null then don't update the text as there will be
            // other text displayed.
            if ((entry != null) && (entry.getInputMsg() != null)) {
                String content = null;
                if (entry.isDynamic()) {
                    content = this.buildDynamicContent(entry.getInputMsg(),
                            entry.getTransmitTime());
                } else {
                    content = entry.getInputMsg().getContent();
                }
                this.messageTextArea.setText(content);
            } else {
                this.messageTextArea
                        .setText("*** Unable to load message text. ***");
            }
        } else {
            this.messageTextArea.setText(StringUtils.EMPTY);
        }
    }

    public String buildDynamicContent(InputMessage inputMsg,
            Calendar predictedTime) {
        if (predictedTime == null) {
            return inputMsg.getContent();
        }
        BMHTimeZone tz = BMHTimeZone
                .getTimeZoneByID(this.selectedTransmitterGroupObject
                        .getTimeZone());
        predictedTime.setTimeZone(tz.getTz());
        SimpleDateFormat sdf = new SimpleDateFormat(this.dynamicTimeFormat);
        /*
         * {@link Date} does not have the correct timezone after we set it in
         * the {@link Calendar}; so, it also needs to be set on the {@link
         * SimpleDateFormat}.
         */
        sdf.setTimeZone(tz.getTz());
        final String formattedTime = sdf.format(predictedTime.getTime()) + " "
                + tz.getLongDisplayName();
        return inputMsg.getContent().replace(TimeTextFragment.TIME_PLACEHOLDER,
                formattedTime);
    }

    @Override
    protected void disposed() {
        synchronized (this.disposalLock) {
            super.disposed();
            /*
             * dialog is closing; ensure that the dialog will never attempt to
             * re-enable inline monitoring.
             */
            this.inlinePreviouslyEnabled = false;
            if (monitorThread != null) {
                monitorThread.cancel();
                monitorThread.removeDisconnectListener(this);
                monitorThread = null;
            }

            BMHDialogNotificationManager.getInstance().unregister(this);

            NotificationManagerJob.removeObserver(
                    BMHJmsDestinations.getStatusDestination(), this);
            NotificationManagerJob.removeObserver(
                    BMHJmsDestinations.getBMHConfigDestination(), this);

            if (liveBroadcastFont != null) {
                liveBroadcastFont.dispose();
            }
        }
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
        if (isDisposed()) {
            return;
        }

        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                if (o instanceof PlaylistSwitchNotification) {
                    final PlaylistSwitchNotification notification = (PlaylistSwitchNotification) o;
                    playlistData.handlePlaylistSwitchNotification(notification);
                    if (notification.getTransmitterGroup().equals(
                            selectedTransmitterGrp)) {
                        tableData = playlistData
                                .getUpdatedTableData(notification
                                        .getTransmitterGroup());
                        cycleDurationTime = timeFormatter.format(new Date(
                                notification.getPlaybackCycleTime()));
                        updateTable(tableData);
                        this.selectedSuite = notification.getSuiteName();

                        final String currentSuiteCategory = this
                                .getCategoryForCurrentSuite();
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                if (messageDetailBtn.isDisposed()) {
                                    return;
                                }

                                messageDetailBtn.setEnabled(true);
                                if (programObj == null) {
                                    setProgramLabelTextFontAndColor("Unknown");
                                } else {
                                    setProgramLabelTextFontAndColor(programObj
                                            .getName());
                                }
                                suiteValueLbl.setText(selectedSuite);
                                /*
                                 * Update the suite category.
                                 */
                                suiteCatValueLbl.setText(currentSuiteCategory);
                                cycleDurValueLbl.setText(cycleDurationTime);

                                if ((periodicMsgDlg != null)
                                        && !periodicMsgDlg.isDisposed()) {
                                    periodicMsgDlg.populateTableData();
                                }

                                if ((changeSuiteDlg != null)
                                        && (changeSuiteDlg.isDisposed() == false)) {
                                    changeSuiteDlg
                                            .updateActiveSuite(selectedSuite);
                                }
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
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                if (messageDetailBtn.isDisposed()) {
                                    return;
                                }

                                messageDetailBtn.setEnabled(true);
                                if ((periodicMsgDlg != null)
                                        && !periodicMsgDlg.isDisposed()) {
                                    periodicMsgDlg.populateTableData();
                                }
                            }
                        });
                    }
                } else if (o instanceof ProgramConfigNotification) {
                    ProgramConfigNotification pgmConfigNotification = (ProgramConfigNotification) o;
                    /*
                     * does this apply to the program we have selected?
                     */
                    if ((this.programObj == null)
                            || (this.programObj.getId() != pgmConfigNotification
                                    .getId())) {
                        return;
                    }
                    /*
                     * Refresh the selected program object.
                     */
                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            retrieveProgram();

                            if ((changeSuiteDlg != null)
                                    && (changeSuiteDlg.isDisposed() == false)) {
                                changeSuiteDlg.updateSuites(programObj
                                        .getSuites());
                            }
                        }
                    });
                } else if (o instanceof LiveBroadcastSwitchNotification) {
                    final LiveBroadcastSwitchNotification notification = (LiveBroadcastSwitchNotification) o;
                    playlistData
                            .handleLiveBroadcastSwitchNotification(notification);
                    if (notification.getTransmitterGroup().getName()
                            .equals(this.selectedTransmitterGrp) == false) {
                        return;
                    }

                    if (notification.getBroadcastState() == STATE.STARTED) {
                        final TableData liveTableData = this.playlistData
                                .getNonStandardTableData(notification);

                        VizApp.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (isDisposed()) {
                                    return;
                                }

                                updateDisplayForNonStandardBroadcast(
                                        liveTableData, notification);
                            }
                        });
                    } else {
                        updateTable(tableData);
                        final String currentSuiteCategory = this
                                .getCategoryForCurrentSuite();
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                if (messageDetailBtn.isDisposed()) {
                                    return;
                                }

                                messageDetailBtn.setEnabled(true);
                                setProgramLabelTextFontAndColor(programObj
                                        .getName());
                                if (selectedSuite != null) {
                                    suiteValueLbl.setText(selectedSuite);
                                } else {
                                    suiteValueLbl.setText("");
                                }
                                suiteCatValueLbl.setText(currentSuiteCategory);
                                if (cycleDurationTime != null) {
                                    cycleDurValueLbl.setText(cycleDurationTime);
                                } else {
                                    cycleDurValueLbl.setText("");
                                }
                            }
                        });
                    }
                } else if (o instanceof TransmitterGroupConfigNotification) {
                    TransmitterGroupConfigNotification notification = (TransmitterGroupConfigNotification) o;
                    this.updateDisplayForTransmitterGrpConfigChange(notification);
                } else if (o instanceof ResetNotification) {
                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            populateTransmitters(true);
                            closeChangeSuiteDlg();
                        }
                    });
                } else if (o instanceof DacTransmitShutdownNotification) {
                    DacTransmitShutdownNotification notification = (DacTransmitShutdownNotification) o;
                    final String group = notification.getTransmitterGroup();

                    playlistData.purgeData(group);
                    if (group.equals(this.selectedTransmitterGrp)) {
                        VizApp.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                initialTablePopulation();
                            }
                        });
                    }
                } else if (o instanceof MaintenanceMessagePlayback) {
                    final MaintenanceMessagePlayback notification = (MaintenanceMessagePlayback) o;

                    this.playlistData
                            .handleMaintenanceNotification(notification);

                    if (notification.getTransmitterGroup().equals(
                            this.selectedTransmitterGrp) == false) {
                        return;
                    }

                    final TableData liveTableData = this.playlistData
                            .getNonStandardTableData(notification);

                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            if (isDisposed()) {
                                return;
                            }

                            updateDisplayForNonStandardBroadcast(liveTableData,
                                    notification);
                        }
                    });
                } else if (o instanceof NoPlaybackMessageNotification) {
                    NoPlaybackMessageNotification notification = (NoPlaybackMessageNotification) o;
                    String group = notification.getGroupName();
                    this.playlistData.purgeData(group);
                    if (group.equals(this.selectedTransmitterGrp)) {
                        tableData = this.playlistData
                                .getUpdatedTableData(group);
                        updateTable(tableData);
                    }
                }
            } catch (NotificationException e) {
                statusHandler.error("Error processing update notification", e);
            }
        }
    }

    /**
     * Determines and returns the category associated with the currently
     * selected suite.
     * 
     * @return the category associated with the currently selected suite.
     */
    private String getCategoryForCurrentSuite() {
        String suiteCategory = NA;
        if ((this.selectedSuite == null) || this.selectedSuite.isEmpty()) {
            return "NA";
        }
        SuiteDataManager sdm = new SuiteDataManager();
        try {
            Suite suite = sdm.getSuiteByName(selectedSuite);
            if (suite != null) {
                suiteCategory = suite.getType().name();
            } else {
                /*
                 * is this an interrupt?
                 */
                if (this.selectedSuite.startsWith("Interrupt")) {
                    /*
                     * set the category to interrupt.
                     */
                    suiteCategory = Suite.SuiteType.INTERRUPT.name();
                }
            }
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve suite: "
                    + this.selectedSuite + ".", e);
            /*
             * the suite category will show up as N/A, continue.
             */
        }

        return suiteCategory;
    }

    private void updateDisplayForNonStandardBroadcast(
            final TableData broadcastTableData,
            final INonStandardBroadcast notification) {
        this.messageDetailBtn.setEnabled(false);
        suiteValueLbl.setText("N/A");
        cycleDurValueLbl.setText("N/A");
        suiteCatValueLbl.setText("");
        if (notification instanceof LiveBroadcastSwitchNotification) {
            BROADCASTTYPE type = ((LiveBroadcastSwitchNotification) notification)
                    .getType();
            if (type == BROADCASTTYPE.EO) {
                setProgramLabelTextFontAndColor(eoText);
            } else if (type == BROADCASTTYPE.BL) {
                setProgramLabelTextFontAndColor(blText);
            }
        } else if (notification instanceof MaintenanceMessagePlayback) {
            cycleDurationTime = timeFormatter.format(new Date(
                    ((MaintenanceMessagePlayback) notification)
                            .getMessageDuration()));
            cycleDurValueLbl.setText(cycleDurationTime);
        }

        tableComp.populateTable(broadcastTableData);
        if (tableComp.getSelectedIndex() == -1) {
            tableComp.select(selectedRow);
        }
        // Do NOT invoke handleTableSelection.

        messageTextArea.setText(StringUtils.EMPTY);
    }

    private void updateDisplayForTransmitterGrpConfigChange(
            final TransmitterGroupConfigNotification notification) {
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                boolean selectionRemoved = false;
                populateTransmitters(false);
                /*
                 * does it affect the transmitter that we are currently
                 * listening to?
                 */
                for (TransmitterGroupIdentifier identifier : notification
                        .getIdentifiers()) {
                    final String grp = identifier.getName();
                    int indx = getSelectedIndex();
                    if (indx != -1) {
                        /*
                         * The group already in the list. Update if it is the
                         * selected group.
                         */
                        if (grp.equals(selectedTransmitterGrp)) {
                            // Program may have been changed.
                            closeChangeSuiteDlg();
                        }
                        continue;
                    }

                    /*
                     * is the group that was removed, the currently selected
                     * group?
                     */
                    if (grp.equals(selectedTransmitterGrp)) {
                        selectionRemoved = true;
                    }

                    /*
                     * the group was removed, eliminate any references to it.
                     */
                    playlistData.purgeData(grp);
                }

                /*
                 * determine which transmitter in the list should be selected
                 * provided that there are transmitters available to select.
                 */
                if (transmitterTable.getItemCount() > 0) {
                    if (selectionRemoved || (selectedTransmitterGrp == null)) {
                        transmitterTable.select(0);
                        closeChangeSuiteDlg();
                        updateOnTransmitterChange(true);
                    } else {
                        /*
                         * select the transmitter that is "currently" selected
                         * at its "new" location.
                         */
                        transmitterTable.select(getSelectedIndex());
                        updateOnTransmitterChange(false);
                    }

                } else {
                    clearTransmitterGroupSelection();
                }
            }
        });
    }

    private int getSelectedIndex() {
        int indx = -1;
        for (int i = 0; i < transmitterTable.getItemCount(); i++) {
            if (transmitterTable.getItem(i).getText(0)
                    .equals(selectedTransmitter)
                    || (this.groupToggleBtn.getSelection() && transmitterTable
                            .getItem(i).getText(0)
                            .equals(selectedTransmitterGrp))) {
                indx = i;
                break;
            }
        }
        return indx;
    }

    /**
     * Clear all transmitter group information.
     */
    private void clearTransmitterGroupSelection() {
        selectedTransmitterGrp = null;
        this.selectedTransmitter = null;
        programObj = null;
        dacValueLbl.setText("");
        portValueLbl.setText("");
        tableComp
                .populateTable(new TableData(new ArrayList<TableColumnData>()));
        messageTextArea.setText("");
        handleMonitorInlineEvent();
    }

    /**
     * Force close of the Change Suite dialog.
     */
    private void closeChangeSuiteDlg() {
        if ((changeSuiteDlg != null) && !changeSuiteDlg.isDisposed()) {
            /*
             * Prevent multiple popups when notification received mutliple
             * times.
             */
            SuiteListDlg csd = changeSuiteDlg;
            changeSuiteDlg = null;
            MessageBox mb = new MessageBox(csd.getShell(), SWT.OK);
            mb.setText("Change Suite");
            mb.setMessage("This dialog must close due to changes in the transmitter.");
            mb.open();
            csd.close();
        }
    }

    /**
     * Set the Program label to reflect the correct font and color based on
     * emergency override.
     * 
     * @param text
     *            Test to put in the label.
     */
    private void setProgramLabelTextFontAndColor(String text) {
        if (progValueLbl.isDisposed()) {
            return;
        }

        if (eoText.equals(text) || blText.equals(text)) {
            progValueLbl.setFont(liveBroadcastFont);
            progValueLbl.setForeground(getDisplay().getSystemColor(
                    SWT.COLOR_BLUE));

        } else {
            progValueLbl.setFont(null);
            progValueLbl.setForeground(null);
        }

        progValueLbl.setText(text);
    }

    /**
     * Update the table.
     * 
     * @param tableData
     *            The updated TableData
     */
    private void updateTable(final TableData tableData) {
        if (tableComp.isDisposed()) {
            return;
        }
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                tableComp.populateTable(tableData);
                tableComp.select(selectedRow);
                handleTableSelection();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.dialogs.notify.IDialogNotificationListener#
     * notificationArrived
     * (com.raytheon.uf.viz.bmh.dialogs.notify.IDialogNotification)
     */
    @Override
    @Subscribe
    public void notificationArrived(IDialogNotification notification) {
        if (monitorBtn.isDisposed()) {
            return;
        }
        if (notification instanceof AudioRecordPlaybackNotification) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    if (monitorBtn.getSelection()) {
                        inlinePreviouslyEnabled = true;
                        monitorBtn.setSelection(false);
                        handleMonitorInlineEvent();
                    }
                }
            });
        } else if (notification instanceof AudioPlaybackCompleteNotification) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    if (inlinePreviouslyEnabled) {
                        inlinePreviouslyEnabled = false;
                        monitorBtn.setSelection(true);
                        handleMonitorInlineEvent();
                    }
                }
            });
        }
    }
}
