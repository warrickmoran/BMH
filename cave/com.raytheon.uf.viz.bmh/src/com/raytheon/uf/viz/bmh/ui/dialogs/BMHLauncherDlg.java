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

import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad.LdadConfigDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverterDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeAssocDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.SystemStatusDlg;
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
 *                                     functionality.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class BMHLauncherDlg extends CaveSWTDialog {

    /** Plugin name. */
    private final String PLUGIN = "com.raytheon.uf.viz.bmh";

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
    private SystemStatusDlg statusDlg;

    /** Message type association dialog. */
    private MessageTypeAssocDlg msgTypeAssocDlg;

    /** Legacy converter dialog. */
    private LegacyDictionaryConverterDlg dictConverterDlg;

    private LdadConfigDlg ldadConfigDlg;

    /**
     * This is a map that contains dialog that may require some sort of save
     * action before closing. These dialogs are reported to the user so they can
     * take action to save any changes from open dialogs.
     */
    private Map<AbstractBMHDialog, String> dlgsToValidateCloseMap = new HashMap<AbstractBMHDialog, String>();

    /**
     * This is a set of dialogs that can be closed normally. This will also
     * contain dialogs that may be created off of the display and would normally
     * remain open if the main dialog is closed.
     */
    private Set<CaveSWTDialogBase> dialogsSet = new HashSet<CaveSWTDialogBase>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public BMHLauncherDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK
                | CAVE.MODE_INDEPENDENT);
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

        statusDlg = new SystemStatusDlg(getParent());
        statusDlg.open();
        dialogsSet.add(statusDlg);
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
        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/BroadcastCycle.png");
        broadcastCycleImg = id.createImage();
        broadcastCycleBtn.setImage(broadcastCycleImg);
        broadcastCycleBtn.setLayoutData(gd);
        broadcastCycleBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            }
        });
        new CustomToolTip(broadcastCycleBtn, "Broadcast Cycle");

        /*
         * Weather Message
         */
        Button weatherMessageBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/WeatherMessage.png");
        weatherMessageImg = id.createImage();
        weatherMessageBtn.setImage(weatherMessageImg);
        weatherMessageBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            }
        });
        new CustomToolTip(weatherMessageBtn, "Weather Element");

        /*
         * Emergency Override
         */
        Button emergencyBtn = new Button(buttonComp, SWT.PUSH);
        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/EmergencyOverride.png");
        emergencyOverrideImg = id.createImage();
        emergencyBtn.setImage(emergencyOverrideImg);
        emergencyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            }
        });
        new CustomToolTip(emergencyBtn, "Emergency Override");
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
        transConfigMI.setText("Transmitter Conifguration...");
        transConfigMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

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
            }
        });

        MenuItem listeningZonesMI = new MenuItem(loadSubMenu, SWT.PUSH);
        listeningZonesMI.setText("Listening Zones...");
        listeningZonesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
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
         * Message Types
         */
        MenuItem messageTypesMI = new MenuItem(messagesMenu, SWT.PUSH);
        messageTypesMI.setText("Message Types...");
        messageTypesMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

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
                if (statusDlg == null || statusDlg.isDisposed()) {
                    statusDlg = new SystemStatusDlg(shell);
                    statusDlg.open();
                }
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

    private void handleDisableSilenceAlarm() {

        // TODO - REMOVE DUMMY CODE
        CheckListData cld = new CheckListData();
        boolean checked = true;
        for (int i = 0; i < 30; i++) {
            checked = true;
            if (i % 2 == 0) {
                checked = false;
            }
            cld.addDataItem("Transmitter:" + i, checked);
        }

        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                "Disable Silence Alarm", "Select Transmitter to Disable:", cld,
                true);
        checkListDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null && returnValue instanceof CheckListData) {
                    CheckListData listData = (CheckListData) returnValue;
                    Map<String, Boolean> dataMap = listData.getDataMap();
                    for (String str : dataMap.keySet()) {
                        System.out.println("Type = " + str + "\t Selected: "
                                + dataMap.get(str));
                    }
                }
            }
        });
    }
}
