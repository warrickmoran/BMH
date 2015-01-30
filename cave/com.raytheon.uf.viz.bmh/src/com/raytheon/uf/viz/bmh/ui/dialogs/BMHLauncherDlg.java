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

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.notify.MessageBroadcastNotifcation;
import com.raytheon.uf.common.bmh.notify.MessageExpiredNotification;
import com.raytheon.uf.common.bmh.notify.MessageNotBroadcastNotification;
import com.raytheon.uf.common.bmh.request.CopyOperationalDbRequest;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.practice.PracticeKeepAliveJob;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcast.BroadcastLiveDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.BroadcastCycleDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.BroadcastCycleDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad.LdadConfigDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterAlignmentDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterConfigDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacConfigDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.DictionaryManagerDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverterDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride.EmergencyOverrideDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.areas.ListeningAreaDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.zones.ListeningZoneDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeAssocDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypesDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteManagerDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.StatusMonitorDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.voice.VoiceConfigDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.WeatherMessagesDlg;
import com.raytheon.uf.viz.bmh.ui.program.BroadcastProgramDlg;
import com.raytheon.uf.viz.bmh.ui.program.ImportLegacyDbDlg;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.CaveSWTDialogBase;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Main launcher dialog for BMH. This will launch all other dialogs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2014   3338     lvenable    Initial creation
 * Jul 08, 2014   3355     mpduff      Implement legacy dictionary converter
 * Jul 15, 2014   3387     lvenable    Implemented code for the abstract BMH dialog
 * Jul 17, 2014   3406     mpduff      Added Listening area and zone dialogs
 * Jul 21, 2014   3407     mpduff      Added DictionaryManagerDlg
 * Jul 27, 2014   3420     lvenable    Added Message types dialog.
 * Aug 04, 2014   3173     mpduff      Added Transmitter Config dialog.
 * Aug 17, 2014   3490     lvenable    Updated for disable silence alarm.
 * Aug 20, 2014   3411     mpduff      Added bringToTop for message dialog
 * Aug 21, 2014   3490     lvenable    Updated disable silence alarm to use transmitter group.
 * Aug 25, 2014   3490     lvenable    Disabled Status Dialog since it will be redesigned.
 * Sep 14, 2014   3610     lvenable    Added launching of Weather Messages dialog.
 * Sep 14, 2014   3630     mpduff      Add the Transmitter Alignment dialog.
 * Sep 19, 2014   3611     lvenable    Added launching of Emergency Override dialog.
 * Oct 06, 2014   3700     lvenable    Added code for force hiding the tool tip.
 * Oct 08, 2014   3687     bsteffen    Add menu item to copy operational db in practice mode.
 * Oct 08, 2014   3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 21, 2014   3687     bsteffen    Automatically notify edex when starting/stopping in practice mode.
 * Nov 07, 2014   3413     rferrel     Added authorization check on dialogs.
 * Nov 10, 2014   3381     bkowal      Updated to use the new LdadConfigDlg.
 * Nov 13, 2014   3803     bkowal      Eliminated NPE on operational db copy.
 * Nov 17, 2014   3808     bkowal      Add the Broadcast Live dialog.
 * Nov 19, 2014   3349     lvenable    Use the new System status dialog.
 * Dec 01, 2014   3698     rferrel     Add warning to copyOperationalDb.
 * Dec 10, 2014   3900     lvenable    Added some spacing and increased the size of the quick access
 * Dec 10, 2014   3824     rferrel     Add importLegacyDB.
 *                                     buttons to make the launcher dialog a bit larger.
 * Dec 15, 2014   3618     bkowal      Added the Voice Configuration dialog.
 * Jan 07, 2015   3931     bkowal      Added an option to import a BMH dictionary.
 * Jan 08, 2015   3821     bsteffen    Rename silenceAlarm to deadAirAlarm
 * Jan 13, 2015   3968     bkowal      Handle message confirmations.
 * Jan 14, 2015   3969     bkowal      Handle watch/warning not broadcast notifications.
 * Jan 30, 2015   4069     bkowal      Move Broadcast Live to the Messages menu.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class BMHLauncherDlg extends CaveSWTDialog implements
        INotificationObserver {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHLauncherDlg.class);

    private final BroadcastCycleDataManager dataManager = new BroadcastCycleDataManager();

    /** Transmitter menu. */
    private Menu transmittersMenu;

    /** Programs menu. */
    private Menu programsMenu;

    /** Message menu. */
    private Menu messagesMenu;

    /** System menu. */
    private Menu systemMenu;

    /** Maintenance menu. */
    private Menu maintenanceMenu;

    /** Weather messages image. */
    private Image weatherMessageImg;

    /** Emergency Override image. */
    private Image emergencyOverrideImg;

    /** Broadcast cycle image. */
    private Image broadcastCycleImg;

    /** Status dialog. */
    private StatusMonitorDlg statusDlg;

    /** Message type association dialog. */
    private MessageTypeAssocDlg msgTypeAssocDlg;

    /** Legacy converter dialog. */
    private LegacyDictionaryConverterDlg dictConverterDlg;

    /** LDAD config dialog */
    private LdadConfigDlg ldadConfigDlg;

    /** Broadcast Program dialog. */
    private BroadcastProgramDlg broadcastProgDlg;

    /** Suite manager dialog. */
    private SuiteManagerDlg suiteManagerDlg;

    /** Broadcast cycle dialog. */
    private BroadcastCycleDlg broadcastCycleDlg;

    /** Listening area configuration dialog */
    private ListeningAreaDlg listeningAreaDlg;

    /** Listening zone configuration dialog */
    private ListeningZoneDlg listeningZoneDlg;

    /** Message types dialog. */
    private MessageTypesDlg messageTypesDlg;

    /** Dictionary Manager Dialog */
    private DictionaryManagerDlg dictManagerDlg;

    /** Transmitter configuration dialog */
    private TransmitterConfigDlg transmitterConfigDlg;

    /** Weather Messages dialog. */
    private WeatherMessagesDlg weatherMessagesDlg;

    /** Emergency Override dialog. */
    private EmergencyOverrideDlg emergecyOverrideDlg;

    /** DAC configuration dialog. */
    private DacConfigDlg dacConfigDlg;

    /** Transmitter Alignment Dialog */
    private TransmitterAlignmentDlg transmitterAlignmentDlg;

    /** Broadcast Live Dialog. */
    private BroadcastLiveDlg broadcastLiveDlg;

    /** Import Legacy Dialog. */
    private ImportLegacyDbDlg importDbDlg;

    /** TTS Voice Configuration Dialog. */
    private VoiceConfigDialog voiceDlg;

    /**
     * This is a map that contains dialog that may require some sort of save
     * action before closing. These dialogs are reported to the user so they can
     * take action to save any changes from open dialogs.
     */
    private final Map<AbstractBMHDialog, String> dlgsToValidateCloseMap = new HashMap<AbstractBMHDialog, String>();

    /**
     * This is a set of dialogs that can be closed normally. This will also
     * contain dialogs that may be created off of the display and would normally
     * remain open if the main dialog is closed.
     */
    private final Set<CaveSWTDialogBase> dialogsSet = new HashSet<CaveSWTDialogBase>();

    /** Tool tip for the Broadcast Cycle button. */
    private CustomToolTip broadcastCycleTip;

    /** Tool tip for the Weather Messages button. */
    private CustomToolTip weatherMessagesTip;

    /** Tool tip for the Emergency Override button. */
    private CustomToolTip emergencyOverrideTip;

    private PracticeKeepAliveJob practiceJob;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public BMHLauncherDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK
                | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.INDEPENDENT_SHELL);
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            practiceJob = new PracticeKeepAliveJob();
            practiceJob.schedule();
        }
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 5;
        mainLayout.marginWidth = 5;
        mainLayout.horizontalSpacing = 10;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
        weatherMessageImg.dispose();
        emergencyOverrideImg.dispose();
        broadcastCycleImg.dispose();

        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getStatusDestination(), this);
    }

    @Override
    protected void opened() {
        /*
         * Launch the System status monitor after the BMH Launcher opens so the
         * system status dialog doesn't get tied to the Cave dialog.
         */
        launchStatusDialog();
        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getStatusDestination(), this);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("BMH Menu");

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {

                List<String> openDialogs = new ArrayList<String>();
                for (AbstractBMHDialog abd : dlgsToValidateCloseMap.keySet()) {
                    if (abd == null || abd.isDisposed()) {
                        continue;
                    }

                    openDialogs.add(dlgsToValidateCloseMap.get(abd));

                }

                if (openDialogs.size() > 0) {
                    e.doit = confirmCloseOpenDialogs(openDialogs);
                    if (e.doit && CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
                        e.doit = confirmClosePracticeMode();
                    }
                } else if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
                    e.doit = confirmClosePracticeMode();
                } else {
                    e.doit = confirmClose();
                }
            }
        });

        createMenuComp(shell);
        createQuickAccessButtons(shell);
    }

    /**
     * Confirm closing BMH when there are dialogs open that may require saving.
     * 
     * @param openDialogs
     *            List of open dialogs.
     * @return True to close BMH, false to keep it open.
     */
    private boolean confirmCloseOpenDialogs(List<String> openDialogs) {
        StringBuilder sb = new StringBuilder();
        sb.append("The following dialogs are open:\n\n");

        for (String str : openDialogs) {
            if (str != null) {
                sb.append(str).append(" Dialog\n");
            }
        }

        sb.append("\nDo you wish to close all dialogs and lose any unsaved changes?");

        MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK
                | SWT.CANCEL);
        mb.setText("Confirm Close");
        mb.setMessage(sb.toString());
        if (mb.open() == SWT.OK) {
            closeDialogs();
            return true;
        }

        return false;
    }

    /**
     * Confirm closing BMH
     * 
     * @return True to close BMH, false to keep it open.
     */
    private boolean confirmClose() {
        StringBuilder sb = new StringBuilder();
        sb.append("Do you wish to exit out of BMH?");

        MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK
                | SWT.CANCEL);
        mb.setText("Confirm Close");
        mb.setMessage(sb.toString());
        if (mb.open() == SWT.OK) {
            closeDialogs();
            return true;
        }
        return false;
    }

    /**
     * Confirm closing BMH when in practice mode
     * 
     * @return True to close BMH, false to keep it open.
     */
    private boolean confirmClosePracticeMode() {
        StringBuilder sb = new StringBuilder();
        sb.append("Would you like to shutdown the practice mode services running on the bmh server also?");

        MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES
                | SWT.NO | SWT.CANCEL);
        mb.setText("Prompt Close Server");
        mb.setMessage(sb.toString());
        int response = mb.open();
        if (response == SWT.YES) {
            practiceJob.shutdown();
        } else if (response == SWT.NO) {
            practiceJob.cancel();
        }
        return response != SWT.CANCEL;
    }

    /**
     * Close all of the dialogs that are open.
     */
    private void closeDialogs() {
        // Force close the dialogs that may need action before closing.
        for (AbstractBMHDialog abd : dlgsToValidateCloseMap.keySet()) {
            if (abd == null || abd.isDisposed()) {
                continue;
            }

            abd.forceClose();
        }

        // Close dialogs that do not require any action before closing.
        for (CaveSWTDialogBase dlg : dialogsSet) {
            dlg.close();
        }
    }

    /**
     * Create the composite that will contain the "menu" buttons.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createMenuComp(Composite mainComp) {

        UpDownImages udi = new UpDownImages(shell);

        final Composite buttonComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        gl.horizontalSpacing = 10;
        buttonComp.setLayout(gl);
        buttonComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        /*
         * Transmitters menu
         */
        createTransmittersMenu();
        final Button transmitterBtn = new Button(buttonComp, SWT.PUSH);
        transmitterBtn.setText("Transmitters");
        transmitterBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        transmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMenu(buttonComp, transmitterBtn, transmittersMenu);
            }
        });

        /*
         * Programs menu
         */
        createProgramsMenu();
        final Button programsBtn = new Button(buttonComp, SWT.PUSH);
        programsBtn.setText("Programs");
        programsBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        programsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMenu(buttonComp, programsBtn, programsMenu);
            }
        });

        /*
         * Messages menu
         */
        createMessagesMenu();
        final Button messagesBtn = new Button(buttonComp, SWT.PUSH);
        messagesBtn.setText("Messages");
        messagesBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        messagesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMenu(buttonComp, messagesBtn, messagesMenu);
            }
        });

        /*
         * System menu
         */
        createSystemMenu();
        final Button systemBtn = new Button(buttonComp, SWT.PUSH);
        systemBtn.setText("System");
        systemBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        systemBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMenu(buttonComp, systemBtn, systemMenu);
            }
        });

        /*
         * Maintenance menu
         */
        createMaintenanceMenu();
        final Button maintenanceBtn = new Button(buttonComp, SWT.PUSH);
        maintenanceBtn.setText("Maintenance");
        maintenanceBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        maintenanceBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMenu(buttonComp, maintenanceBtn, maintenanceMenu);
            }
        });
    }

    /**
     * Create the quick access buttons.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createQuickAccessButtons(Composite mainComp) {

        Composite buttonComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        gl.horizontalSpacing = 10;
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        /*
         * Weather Message
         */
        ImageDescriptor id;
        int buttonWidth = 50;
        GridData gd = new GridData(buttonWidth, SWT.DEFAULT);
        gd.horizontalIndent = 30;
        Button broadcastCycleBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/BroadcastCycle.png");
        broadcastCycleImg = id.createImage();
        broadcastCycleBtn.setImage(broadcastCycleImg);
        broadcastCycleBtn.setLayoutData(gd);
        broadcastCycleBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchBroadcastCycle();
                broadcastCycleTip.forceHideToolTip();
            }
        });
        broadcastCycleTip = new CustomToolTip(broadcastCycleBtn,
                "Broadcast Cycle");

        /*
         * Weather Message
         */
        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button weatherMessageBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/WeatherMessage.png");
        weatherMessageImg = id.createImage();
        weatherMessageBtn.setImage(weatherMessageImg);
        weatherMessageBtn.setLayoutData(gd);
        weatherMessageBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchWeatherMessages();
                weatherMessagesTip.forceHideToolTip();
            }
        });
        weatherMessagesTip = new CustomToolTip(weatherMessageBtn,
                "Weather Messages");

        /*
         * Emergency Override
         */
        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button emergencyBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/EmergencyOverride.png");
        emergencyOverrideImg = id.createImage();
        emergencyBtn.setImage(emergencyOverrideImg);
        emergencyBtn.setLayoutData(gd);
        emergencyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchEmergencyOverride();
                emergencyOverrideTip.forceHideToolTip();
            }
        });
        emergencyOverrideTip = new CustomToolTip(emergencyBtn,
                "Emergency Override");
    }

    /**
     * Create the Transmitter menu that will launch dialogs related to the
     * transmitter.
     */
    private void createTransmittersMenu() {
        transmittersMenu = new Menu(shell, SWT.POP_UP);

        /*
         * Transmitter
         */
        MenuItem transConfigMI = new MenuItem(transmittersMenu, SWT.PUSH);
        transConfigMI.setText("Transmitter Configuration...");
        transConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.TRANSMITTER_CONFIGURATION)) {
                    if (transmitterConfigDlg == null
                            || transmitterConfigDlg.isDisposed()) {
                        transmitterConfigDlg = new TransmitterConfigDlg(shell,
                                dlgsToValidateCloseMap);
                        transmitterConfigDlg.open();
                    } else {
                        transmitterConfigDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Transmitter alignment
         */
        MenuItem transAlignmentMI = new MenuItem(transmittersMenu, SWT.PUSH);
        transAlignmentMI.setText("Transmitter Alignment...");
        transAlignmentMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isAuthorized(DlgInfo.TRANSMITTER_ALIGNMENT)) {
                    if (transmitterAlignmentDlg == null
                            || transmitterAlignmentDlg.isDisposed()) {
                        transmitterAlignmentDlg = new TransmitterAlignmentDlg(
                                shell, dlgsToValidateCloseMap);
                        transmitterAlignmentDlg.open();
                    } else {
                        transmitterAlignmentDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Listening Areas
         */
        MenuItem listeningMI = new MenuItem(transmittersMenu, SWT.CASCADE);
        listeningMI.setText("Listening Area");

        Menu loadSubMenu = new Menu(shell, SWT.DROP_DOWN);
        listeningMI.setMenu(loadSubMenu);

        MenuItem listeningAreasMI = new MenuItem(loadSubMenu, SWT.PUSH);
        listeningAreasMI.setText("Listening Areas...");
        listeningAreasMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.LISTENING_AREAS)) {
                    if (listeningAreaDlg == null
                            || listeningAreaDlg.isDisposed()) {
                        listeningAreaDlg = new ListeningAreaDlg(shell,
                                dlgsToValidateCloseMap);
                        listeningAreaDlg.open();
                    } else {
                        listeningAreaDlg.bringToTop();
                    }
                }
            }
        });

        MenuItem listeningZonesMI = new MenuItem(loadSubMenu, SWT.PUSH);
        listeningZonesMI.setText("Listening Zones...");
        listeningZonesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.LISTENING_ZONES)) {
                    if (listeningZoneDlg == null
                            || listeningZoneDlg.isDisposed()) {
                        listeningZoneDlg = new ListeningZoneDlg(shell,
                                dlgsToValidateCloseMap);
                        listeningZoneDlg.open();
                    } else {
                        listeningZoneDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Disable Silence Alarm
         */
        MenuItem disableAlarmMI = new MenuItem(transmittersMenu, SWT.PUSH);
        disableAlarmMI.setText("Disable Silence Alarm...");
        disableAlarmMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.DISABLES_SILENCE_ALARM)) {
                    handleDisableSilenceAlarm();
                }
            }
        });

        /*
         * Broadcast Cycle
         */
        MenuItem broadcastCycleMI = new MenuItem(transmittersMenu, SWT.PUSH);
        broadcastCycleMI.setText("Broadcast Cycle...");
        broadcastCycleMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchBroadcastCycle();
            }
        });
    }

    /**
     * Create the Programs menu that will launch dialogs related to the
     * programs.
     */
    private void createProgramsMenu() {
        programsMenu = new Menu(shell, SWT.POP_UP);

        /*
         * Broadcast Programs
         */
        MenuItem broadcastProgramMI = new MenuItem(programsMenu, SWT.PUSH);
        broadcastProgramMI.setText("Broadcast Programs...");
        broadcastProgramMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.BROADCAST_PROGRAMS)) {
                    if (broadcastProgDlg == null
                            || broadcastProgDlg.isDisposed()) {
                        broadcastProgDlg = new BroadcastProgramDlg(getShell(),
                                dlgsToValidateCloseMap);
                        broadcastProgDlg.open();
                    } else {
                        broadcastProgDlg.bringToTop();
                    }
                }
            }
        });
    }

    /**
     * Create the Messages menu that will launch dialogs related to the message
     * types.
     */
    private void createMessagesMenu() {
        messagesMenu = new Menu(shell, SWT.POP_UP);

        /*
         * Suite Manager
         */
        MenuItem suiteManagerMI = new MenuItem(messagesMenu, SWT.PUSH);
        suiteManagerMI.setText("Suite Manager...");
        suiteManagerMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.SUITE_MANAGER)) {
                    if (suiteManagerDlg == null || suiteManagerDlg.isDisposed()) {
                        suiteManagerDlg = new SuiteManagerDlg(getShell(),
                                dlgsToValidateCloseMap);
                        suiteManagerDlg.open();
                    } else {
                        suiteManagerDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Message Types
         */
        MenuItem messageTypesMI = new MenuItem(messagesMenu, SWT.PUSH);
        messageTypesMI.setText("Message Types...");
        messageTypesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.MESSAGE_TYPE)) {
                    if (messageTypesDlg == null || messageTypesDlg.isDisposed()) {
                        messageTypesDlg = new MessageTypesDlg(getShell(),
                                dlgsToValidateCloseMap);
                        messageTypesDlg.open();
                    } else {
                        messageTypesDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Message Type Association
         */
        MenuItem messageTypesAssocMI = new MenuItem(messagesMenu, SWT.PUSH);
        messageTypesAssocMI.setText("Message Type Association...");
        messageTypesAssocMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.MESSAGE_TYPE_ASSOCIATION)) {
                    if (msgTypeAssocDlg == null || msgTypeAssocDlg.isDisposed()) {
                        msgTypeAssocDlg = new MessageTypeAssocDlg(getShell(),
                                dlgsToValidateCloseMap);
                        msgTypeAssocDlg.open();
                    } else {
                        msgTypeAssocDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Weather Messages
         */
        MenuItem weatherMessagesMI = new MenuItem(messagesMenu, SWT.PUSH);
        weatherMessagesMI.setText("Weather Messages...");
        weatherMessagesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchWeatherMessages();
            }
        });

        /*
         * Emergency Override
         */
        MenuItem emergencyOverrideMI = new MenuItem(messagesMenu, SWT.PUSH);
        emergencyOverrideMI.setText("Emergency Override...");
        emergencyOverrideMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchEmergencyOverride();
            }
        });
        
        /*
         * Broadcast Live
         */
        new MenuItem(this.messagesMenu, SWT.SEPARATOR);
        MenuItem broadcastLiveMI = new MenuItem(this.messagesMenu, SWT.PUSH);
        broadcastLiveMI.setText("Broadcast Live...");
        broadcastLiveMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.BROADCAST_LIVE)) {
                    if (broadcastLiveDlg == null
                            || broadcastLiveDlg.isDisposed() == true) {
                        broadcastLiveDlg = new BroadcastLiveDlg(
                                dlgsToValidateCloseMap, shell);
                        broadcastLiveDlg.open();
                    } else {
                        broadcastLiveDlg.bringToTop();
                    }
                }
            }
        });
    }

    /**
     * Create the System menu that will launch dialogs related to the system
     * functionality.
     */
    private void createSystemMenu() {
        systemMenu = new Menu(shell, SWT.POP_UP);

        /*
         * System Status
         */
        MenuItem systemStatusMI = new MenuItem(systemMenu, SWT.PUSH);
        systemStatusMI.setText("System Status...");
        systemStatusMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchStatusDialog();
            }
        });

        /*
         * Alert Monitor
         */
        MenuItem alertMonitorMI = new MenuItem(systemMenu, SWT.PUSH);
        alertMonitorMI.setText("Alert Monitor...");
        alertMonitorMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.ALERT_MONITOR)) {
                    DialogUtility.notImplemented(shell);
                }
            }
        });

        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            MenuItem copyOperationalDbMI = new MenuItem(systemMenu, SWT.PUSH);
            copyOperationalDbMI.setText("Copy Operational DB");
            copyOperationalDbMI.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    if (isAuthorized(DlgInfo.COPY_OPERATIONAL_DB)) {
                        copyOperationalDb();
                    }
                }
            });
        }

        MenuItem legacyImportMI = new MenuItem(systemMenu, SWT.PUSH);
        legacyImportMI.setText("Import Legacy DB...");
        legacyImportMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.IMPORT_LEGACY_DB)) {
                    importLegacyDB();
                }
            }
        });

    }

    /**
     * Create the Maintenance menu that will launch dialogs related to the
     * maintenance functionality.
     */
    private void createMaintenanceMenu() {
        maintenanceMenu = new Menu(shell, SWT.POP_UP);

        MenuItem ldadConfigMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        ldadConfigMI.setText("LDAD Configuration...");
        ldadConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.LDAD_CONFIGURATION)) {
                    if (ldadConfigDlg == null || ldadConfigDlg.isDisposed()) {
                        ldadConfigDlg = new LdadConfigDlg(
                                dlgsToValidateCloseMap, getShell());
                        ldadConfigDlg.open();
                    } else {
                        ldadConfigDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * Dictionary Manager Dialog
         */
        MenuItem dictionaryManagerMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        dictionaryManagerMI.setText("Manage Dictionaries...");
        dictionaryManagerMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchDictionaryManager();
            }
        });

        /*
         * Import BMH Dictionaries.
         */
        MenuItem importDictionaryMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        importDictionaryMI.setText("Import BMH Dictionary...");
        importDictionaryMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchBMHDictionaryImporter();
            }
        });

        /*
         * Convert Legacy Dictionary
         */
        MenuItem convertDictMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        convertDictMI.setText("Convert Legacy Dictionary...");
        convertDictMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                launchLegacyDictionaryConverter();
            }
        });

        /*
         * DAC Configuration
         */
        MenuItem dacConfigMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        dacConfigMI.setText("DAC Configuration...");
        dacConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.DAC_CONFIGURATION)) {
                    if (dacConfigDlg == null
                            || dacConfigDlg.isDisposed() == true) {
                        dacConfigDlg = new DacConfigDlg(shell,
                                dlgsToValidateCloseMap);
                        dacConfigDlg.open();
                    } else {
                        dacConfigDlg.bringToTop();
                    }
                }
            }
        });

        /*
         * TTS Voice Configuration
         */
        MenuItem voiceConfigMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        voiceConfigMI.setText("Voice Configuration...");
        voiceConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isAuthorized(DlgInfo.TTS_VOICE_CONFIGURATION)) {
                    if (voiceDlg == null || voiceDlg.isDisposed() == true) {
                        voiceDlg = new VoiceConfigDialog(
                                dlgsToValidateCloseMap, shell);
                        voiceDlg.open();
                    } else {
                        voiceDlg.bringToTop();
                    }
                }
            }
        });
    }

    /**
     * Display the popup menu associated with the specified button.
     * 
     * @param comp
     *            Composite when the button resides.
     * @param btn
     *            Button clicked.
     * @param menu
     *            Menu to be displayed.
     */
    private void displayMenu(Composite comp, Button btn, Menu menu) {
        Rectangle rect = btn.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = comp.toDisplay(pt);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }

    /**
     * Launch the {@link DictionaryManagerDlg}
     */
    private void launchDictionaryManager() {
        if (isAuthorized(DlgInfo.MANAGE_DICTIONARIES)) {
            if (this.dictManagerDlg == null || this.dictManagerDlg.isDisposed()) {
                dictManagerDlg = new DictionaryManagerDlg(shell,
                        this.dlgsToValidateCloseMap);
                dictManagerDlg.open();
            } else {
                dictManagerDlg.bringToTop();
            }
        }
    }

    /**
     * Allows the user to import a BMH dictionary using a XML file.
     */
    private void launchBMHDictionaryImporter() {
        /*
         * If the user can access the dictionary manager, they will be allowed
         * to import bmh dictionaries.
         */
        if (isAuthorized(DlgInfo.MANAGE_DICTIONARIES)) {
            FileDialog dialog = new FileDialog(this.shell, SWT.OPEN);
            dialog.setFilterPath(System.getProperty("user.home"));
            dialog.setFilterExtensions(new String[] { "*.xml" });
            dialog.setText("Import BMH Dictionary");
            String fileName = dialog.open();
            if (fileName == null) {
                return;
            }

            /**
             * Get the {@link Path} associated with the specified file and
             * verify that it exists.
             */
            Path importXMLPath = Paths.get(fileName);
            if (Files.exists(importXMLPath) == false) {
                statusHandler
                        .error("The BMH Dictionary Import has failed. The specified xml import file does not exist: "
                                + importXMLPath.toString());
                return;
            }
            /**
             * Open the {@link DictionaryManagerDlg} and start the import.
             */
            if (this.dictManagerDlg == null || this.dictManagerDlg.isDisposed()) {
                dictManagerDlg = new DictionaryManagerDlg(shell,
                        this.dlgsToValidateCloseMap);
                dictManagerDlg.open();
            } else {
                dictManagerDlg.bringToTop();
            }
            dictManagerDlg.initiateDictionaryImport(importXMLPath);
        }
    }

    /**
     * Launch the legacy dictionary converter
     */
    private void launchLegacyDictionaryConverter() {
        if (isAuthorized(DlgInfo.CONVERT_LEGACY_DICTIONARY)) {
            if (this.dictConverterDlg == null || dictConverterDlg.isDisposed()) {
                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                dialog.setText(DlgInfo.CONVERT_LEGACY_DICTIONARY.getTitle());
                String[] filterNames = new String[] { "Dictionary Files",
                        "All Files (*)" };
                String[] filterExtensions = new String[] { "*.dic;*.nat;", "*" };
                String filterPath = "/";
                dialog.setFilterNames(filterNames);
                dialog.setFilterExtensions(filterExtensions);
                dialog.setFilterPath(filterPath);
                String file = dialog.open();
                if (file != null && file.length() > 0) {
                    this.dictConverterDlg = new LegacyDictionaryConverterDlg(
                            getShell(), file);
                    dictConverterDlg.open();
                }
            } else {
                dictConverterDlg.bringToTop();
            }
        }
    }

    private void launchBroadcastCycle() {
        if (isAuthorized(DlgInfo.BROADCAST_CYCLE)) {
            if (broadcastCycleDlg == null || broadcastCycleDlg.isDisposed()) {
                broadcastCycleDlg = new BroadcastCycleDlg(getShell(),
                        dlgsToValidateCloseMap);
                broadcastCycleDlg.open();
            } else {
                broadcastCycleDlg.bringToTop();
            }
        }
    }

    private void launchWeatherMessages() {
        if (isAuthorized(DlgInfo.WEATHER_MESSAGES)) {
            if (weatherMessagesDlg == null || weatherMessagesDlg.isDisposed()) {
                weatherMessagesDlg = new WeatherMessagesDlg(getShell(),
                        dlgsToValidateCloseMap);
                weatherMessagesDlg.open();
            } else {
                weatherMessagesDlg.bringToTop();
            }
        }
    }

    private void launchEmergencyOverride() {
        if (isAuthorized(DlgInfo.EMERGENCY_OVERRIDE)) {
            if (emergecyOverrideDlg == null || emergecyOverrideDlg.isDisposed()) {
                emergecyOverrideDlg = new EmergencyOverrideDlg(getShell(),
                        dlgsToValidateCloseMap);
                emergecyOverrideDlg.open();
            } else {
                emergecyOverrideDlg.bringToTop();
            }
        }
    }

    /**
     * Launch the system status dialog.
     */
    private void launchStatusDialog() {
        if (statusDlg == null || statusDlg.isDisposed()) {
            statusDlg = new StatusMonitorDlg(getShell());
            statusDlg.setCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    dialogsSet.remove(statusDlg);
                }
            });
            statusDlg.open();
            dialogsSet.add(statusDlg);
        }
    }

    private void copyOperationalDb() {

        int result = DialogUtility
                .showMessageBox(
                        shell,
                        SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                        "Copy Operation data base",
                        "This will wipe out the current contents of your pratice data base.\n\nSelect OK to continue.");
        if (result != SWT.OK) {
            return;
        }
        /*
         * TODO this dialog prevents the user from using cave while they wait,
         * ideally it would only block BMH.
         */
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                this.getShell());

        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask("Copying Operational Database",
                            IProgressMonitor.UNKNOWN);
                    try {
                        BmhUtils.sendRequest(new CopyOperationalDbRequest());
                    } catch (Exception e) {
                        statusHandler.error("Failed to copy operational db", e);
                    }
                }

            });
        } catch (InvocationTargetException | InterruptedException e) {
            /*
             * Since no exception is thrown from run it is unlikely this ever
             * happens
             */
            statusHandler.error("Failed to copy operational db", e);
        }
    }

    private void handleDisableSilenceAlarm() {

        // TODO: Need to determine the silence alarms so we can check the
        // transmitters

        /*
         * Create the list of transmitters to be used with the
         * CheckScrollListDlg
         */
        CheckListData cld = new CheckListData();
        TransmitterDataManager tdm = new TransmitterDataManager();
        List<TransmitterGroup> transmitterGrps = null;

        try {
            transmitterGrps = tdm
                    .getTransmitterGroups(new TransmitterGroupPositionComparator());
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return;
        }

        for (TransmitterGroup tg : transmitterGrps) {
            cld.addDataItem(tg.getName(), !tg.getDeadAirAlarm());
        }

        /*
         * Create the check list dialog with the list of transmitters.
         */
        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                DlgInfo.DISABLES_SILENCE_ALARM.getTitle(),
                "Select Transmitter to Disable:", cld, true);
        checkListDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null && returnValue instanceof CheckListData) {
                    CheckListData listData = (CheckListData) returnValue;
                    handleTransmitterGroupUpdate(listData.getDataMap());
                }
            }
        });
        checkListDlg.open();
    }

    private void handleTransmitterGroupUpdate(Map<String, Boolean> dataMap) {

        if (dataMap.isEmpty()) {
            return;
        }

        TransmitterDataManager tdm = new TransmitterDataManager();
        List<TransmitterGroup> transmitterGrps = null;

        try {
            transmitterGrps = tdm.getTransmitterGroups();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return;
        }

        for (TransmitterGroup tg : transmitterGrps) {
            if (dataMap.containsKey(tg.getName())) {
                tg.setDeadAirAlarm(!dataMap.get(tg.getName()));
                try {
                    tdm.saveTransmitterGroup(tg);
                } catch (Exception e) {
                    statusHandler.error(
                            "Error saving updated transmitter group "
                                    + tg.getName() + " to the database: ", e);
                }
            }
        }
    }

    private void importLegacyDB() {
        if ((importDbDlg == null) || importDbDlg.isDisposed()) {
            importDbDlg = new ImportLegacyDbDlg(shell, dlgsToValidateCloseMap);
            importDbDlg.open();
        } else {
            importDbDlg.bringToTop();
        }
    }

    private boolean isAuthorized(DlgInfo dlgInfo) {
        return BmhUtils.isAuthorized(shell, dlgInfo);
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            Object o;
            try {
                o = message.getMessagePayload();
                if (o instanceof MessageBroadcastNotifcation) {
                    MessageBroadcastNotifcation notification = (MessageBroadcastNotifcation) o;

                    /*
                     * Retrieve the broadcast message associated with the
                     * notification.
                     */
                    BroadcastMsg msg = null;
                    try {
                        msg = this.dataManager.getBroadcastMessage(notification
                                .getBroadcastId());
                    } catch (Exception e) {
                        statusHandler.error(
                                "Failed to retrieve the broadcast message associated with notification: "
                                        + notification.toString() + ".", e);
                        return;
                    }
                    /*
                     * Broadcast message has been successfully retrieved.
                     * Construct the Input Message identifier;
                     */
                    final String identification = this
                            .buildInputMessageIdentifier(msg.getInputMessage()
                                    .getId(), msg.getInputMessage().getName(),
                                    msg.getInputMessage().getAfosid());

                    /*
                     * construct the AlertViz message.
                     */
                    StringBuilder sb = new StringBuilder(identification);
                    sb.append(" has been successfully broadcast on Transmitter ");
                    sb.append(notification.getTransmitterGroup()).append(".");
                    statusHandler.info(sb.toString());
                } else if (o instanceof MessageExpiredNotification) {
                    MessageExpiredNotification notification = (MessageExpiredNotification) o;

                    final String identification = this
                            .buildInputMessageIdentifier(notification.getId(),
                                    notification.getName(),
                                    notification.getAfosid());

                    /*
                     * construct the AlertViz message.
                     */
                    StringBuilder sb = new StringBuilder(
                            notification.getDesignation());
                    sb.append(" ").append(identification);
                    sb.append(" will never be broadcast because it was already expired upon arrival.");
                    statusHandler.info(sb.toString());
                } else if (o instanceof MessageNotBroadcastNotification) {
                    MessageNotBroadcastNotification notification = (MessageNotBroadcastNotification) o;

                    /*
                     * Retrieve the broadcast message associated with the
                     * notification.
                     */
                    BroadcastMsg msg = null;
                    try {
                        msg = this.dataManager.getBroadcastMessage(notification
                                .getBroadcastId());
                    } catch (Exception e) {
                        statusHandler.error(
                                "Failed to retrieve the broadcast message associated with notification: "
                                        + notification.toString() + ".", e);
                        return;
                    }
                    /*
                     * Broadcast message has been successfully retrieved.
                     * Construct the Input Message identifier;
                     */
                    final String identification = this
                            .buildInputMessageIdentifier(msg.getInputMessage()
                                    .getId(), msg.getInputMessage().getName(),
                                    msg.getInputMessage().getAfosid());

                    /*
                     * construct the AlertViz message.
                     */
                    StringBuilder sb = new StringBuilder(
                            notification.getDesignation());
                    sb.append(" ").append(identification);
                    sb.append(" expired before it could be broadcast on Transmitter ");
                    sb.append(notification.getTransmitterGroup()).append(".");

                    statusHandler.info(sb.toString());
                }
            } catch (NotificationException e) {
                statusHandler.error("Error processing BMH Notification!", e);
            }
        }
    }

    private String buildInputMessageIdentifier(final int id, final String name,
            final String afosid) {
        StringBuilder sb = new StringBuilder("InputMessage [id=");
        sb.append(id);
        sb.append(", name=").append(name);
        sb.append(", afosid=").append(afosid).append("]");

        return sb.toString();
    }
}