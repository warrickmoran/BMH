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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
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
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.WeatherMessagesDlg;
import com.raytheon.uf.viz.bmh.ui.program.BroadcastProgramDlg;
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
 * Jul 07, 2014  #3338     lvenable    Initial creation
 * Jul 08, 2014   3355     mpduff      Implement legacy dictionary converter
 * Jul 15, 2014  #3387     lvenable    Implemented code for the abstract BMH dialog
 * Jul 17, 2014   3406     mpduff      Added Listening area and zone dialogs
 * Jul 21, 2014   3407     mpduff      Added DictionaryManagerDlg
 * Jul 27, 2014  #3420     lvenable    Added Message types dialog.
 * Aug 04, 2014   3173     mpduff      Added Transmitter Config dialog.
 * Aug 17, 2014  #3490     lvenable    Updated for disable silence alarm.
 * Aug 20, 2014   3411     mpduff      Added bringToTop for message dialog
 * Aug 21, 2014  #3490     lvenable    Updated disable silence alarm to use transmitter group.
 * Aug 25, 2014  #3490     lvenable    Disabled Status Dialog since it will be redesigned.
 * Sep 14, 2014  #3610     lvenable    Added launching of Weather Messages dialog.
 * Sep 14, 2014   3630     mpduff      Add the Transmitter Alignment dialog.
 * Sep 19, 2014  #3611     lvenable    Added launching of Emergency Override dialog.
 * Oct 06, 2014  #3700     lvenable    Added code for force hiding the tool tip.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class BMHLauncherDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHLauncherDlg.class);

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
    // private SystemStatusDlg statusDlg;

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

    /** Tool tip for the Weahter Messages button. */
    private CustomToolTip weatherMessagesTip;

    /** Tool tip for the Emergency Override button. */
    private CustomToolTip emergencyOverrideTip;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public BMHLauncherDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK
                | CAVE.MODE_INDEPENDENT | CAVE.INDEPENDENT_SHELL);
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
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
                } else {
                    e.doit = confirmClose();
                }
            }
        });

        createMenuComp(shell);
        createQuickAccessButtons(shell);

        /*
         * TODO : implement after the demo with the redesigned dialog
         */
        // statusDlg = new SystemStatusDlg(getParent());
        // statusDlg.open();
        // dialogsSet.add(statusDlg);
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
                sb.append(str).append("\n");
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
        buttonComp.setLayout(new GridLayout(5, false));
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
        buttonComp.setLayout(new GridLayout(3, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        /*
         * Weather Message
         */
        ImageDescriptor id;
        GridData gd = new GridData();
        gd.horizontalIndent = 20;
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
        Button weatherMessageBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/WeatherMessage.png");
        weatherMessageImg = id.createImage();
        weatherMessageBtn.setImage(weatherMessageImg);
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
        Button emergencyBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/EmergencyOverride.png");
        emergencyOverrideImg = id.createImage();
        emergencyBtn.setImage(emergencyOverrideImg);
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
                if (transmitterConfigDlg == null
                        || transmitterConfigDlg.isDisposed()) {
                    transmitterConfigDlg = new TransmitterConfigDlg(shell,
                            dlgsToValidateCloseMap);
                    transmitterConfigDlg.open();
                } else {
                    transmitterConfigDlg.bringToTop();
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
                if (transmitterAlignmentDlg == null
                        || transmitterAlignmentDlg.isDisposed()) {
                    transmitterAlignmentDlg = new TransmitterAlignmentDlg(
                            shell, dlgsToValidateCloseMap);
                    transmitterAlignmentDlg.open();
                } else {
                    transmitterAlignmentDlg.bringToTop();
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
                if (listeningAreaDlg == null || listeningAreaDlg.isDisposed()) {
                    listeningAreaDlg = new ListeningAreaDlg(shell,
                            dlgsToValidateCloseMap);
                    listeningAreaDlg.open();
                } else {
                    listeningAreaDlg.bringToTop();
                }
            }
        });

        MenuItem listeningZonesMI = new MenuItem(loadSubMenu, SWT.PUSH);
        listeningZonesMI.setText("Listening Zones...");
        listeningZonesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (listeningZoneDlg == null || listeningZoneDlg.isDisposed()) {
                    listeningZoneDlg = new ListeningZoneDlg(shell,
                            dlgsToValidateCloseMap);
                    listeningZoneDlg.open();
                } else {
                    listeningZoneDlg.bringToTop();
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
                handleDisableSilenceAlarm();
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
                if (broadcastProgDlg == null || broadcastProgDlg.isDisposed()) {
                    broadcastProgDlg = new BroadcastProgramDlg(getShell(),
                            dlgsToValidateCloseMap);
                    broadcastProgDlg.open();
                } else {
                    broadcastProgDlg.bringToTop();
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
                if (suiteManagerDlg == null || suiteManagerDlg.isDisposed()) {
                    suiteManagerDlg = new SuiteManagerDlg(getShell(),
                            dlgsToValidateCloseMap);
                    suiteManagerDlg.open();
                } else {
                    suiteManagerDlg.bringToTop();
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
                if (messageTypesDlg == null || messageTypesDlg.isDisposed()) {
                    messageTypesDlg = new MessageTypesDlg(getShell(),
                            dlgsToValidateCloseMap);
                    messageTypesDlg.open();
                } else {
                    messageTypesDlg.bringToTop();
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
                if (msgTypeAssocDlg == null || msgTypeAssocDlg.isDisposed()) {
                    msgTypeAssocDlg = new MessageTypeAssocDlg(getShell(),
                            dlgsToValidateCloseMap);
                    msgTypeAssocDlg.open();
                } else {
                    msgTypeAssocDlg.bringToTop();
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
                /*
                 * TODO : implement new redesigned dialog
                 */
                DialogUtility
                        .showMessageBox(shell, SWT.ICON_INFORMATION | SWT.OK,
                                "Redesign Needed",
                                "The status dialog is being redesigned and will be available at a later date.");
                // if (statusDlg == null || statusDlg.isDisposed()) {
                // statusDlg = new SystemStatusDlg(shell);
                // statusDlg.open();
                // }
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
                DialogUtility.notImplemented(shell);
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
                if (ldadConfigDlg == null || ldadConfigDlg.isDisposed()) {
                    ldadConfigDlg = new LdadConfigDlg(getShell());
                    ldadConfigDlg.open();
                } else {
                    ldadConfigDlg.bringToTop();
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
         * Convert Legacy Dictionary
         */
        MenuItem dacConfigMI = new MenuItem(maintenanceMenu, SWT.PUSH);
        dacConfigMI.setText("DAC Configuration...");
        dacConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dacConfigDlg == null || dacConfigDlg.isDisposed() == true) {
                    dacConfigDlg = new DacConfigDlg(shell,
                            dlgsToValidateCloseMap);
                    dacConfigDlg.open();
                } else {
                    dacConfigDlg.bringToTop();
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
        if (this.dictManagerDlg == null || this.dictManagerDlg.isDisposed()) {
            dictManagerDlg = new DictionaryManagerDlg(shell,
                    this.dlgsToValidateCloseMap);
            dictManagerDlg.open();
        } else {
            dictManagerDlg.bringToTop();
        }
    }

    /**
     * Launch the legacy dictionary converter
     */
    private void launchLegacyDictionaryConverter() {
        if (this.dictConverterDlg == null || dictConverterDlg.isDisposed()) {
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
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

    private void launchBroadcastCycle() {
        if (broadcastCycleDlg == null || broadcastCycleDlg.isDisposed()) {
            broadcastCycleDlg = new BroadcastCycleDlg(getShell(),
                    dlgsToValidateCloseMap);
            broadcastCycleDlg.open();
        } else {
            broadcastCycleDlg.bringToTop();
        }
    }

    private void launchWeatherMessages() {
        if (weatherMessagesDlg == null || weatherMessagesDlg.isDisposed()) {
            weatherMessagesDlg = new WeatherMessagesDlg(getShell(),
                    dlgsToValidateCloseMap);
            weatherMessagesDlg.open();
        } else {
            weatherMessagesDlg.bringToTop();
        }
    }

    private void launchEmergencyOverride() {
        if (emergecyOverrideDlg == null || emergecyOverrideDlg.isDisposed()) {
            emergecyOverrideDlg = new EmergencyOverrideDlg(getShell(),
                    dlgsToValidateCloseMap);
            emergecyOverrideDlg.open();
        } else {
            emergecyOverrideDlg.bringToTop();
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
            transmitterGrps = tdm.getTransmitterGroups();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
            return;
        }

        for (TransmitterGroup tg : transmitterGrps) {
            cld.addDataItem(tg.getName(), tg.getSilenceAlarm());
        }

        /*
         * Create the check list dialog with the list of transmitters.
         */
        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                "Disable Silence Alarm", "Select Transmitter to Disable:", cld,
                true);
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
                tg.setSilenceAlarm(dataMap.get(tg.getName()));

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
}
