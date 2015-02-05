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
package com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.DacConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.status.BmhEdexStatus;
import com.raytheon.uf.common.bmh.notify.status.CommsManagerStatus;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.DacVoiceStatus;
import com.raytheon.uf.common.bmh.systemstatus.ISystemStatusListener;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacTransmitterStatusData;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.StatusDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterGrpInfo;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * Composite that displays the status of the BMH system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Added listeners and process status code.
 * Dec 04, 2014  #3287     lvenable     Added fix for practice code color & layout of status
 *                                      messages on change.
 * Jan 13, 2015  #3844     bsteffen     Fix dialog disposed error after closing.
 * Jan 26, 2015  #4020     bkowal       Updated {@link #printVizStatusMonitorVariables()}.
 * Jan 27, 2015  #4029     bkowal       Use {@link BMHJmsDestinations}.
 * Feb 06, 2015  #4019     lvenable     Changed class to be a composite and made other changes
 *                                      so it can be embedded into a dialog.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class StatusMonitorComp extends Composite implements
        INotificationObserver, ISystemStatusListener {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(StatusMonitorComp.class);

    /**
     * Status data manager that manages all of the data for displaying the
     * status.
     */
    private StatusDataManager sdm;

    /** The status images for the DAC and transmitters. */
    private StatusImages statusImages;

    /** DAC/Transmitter composite. */
    private Composite dacTransmittersComp = null;

    /** Scrolled composite for the DACs and transmitters. */
    private ScrolledComposite scrolledComp;

    /** Viz Status Monitor. */
    private VizStatusMonitor vizStatusMonitor;

    /** List of DACs */
    private List<Dac> dacList = null;

    /** List of Transmitter Groups. */
    private List<TransmitterGroup> tgList = null;

    /** Process Status group container. */
    private Group processStatusGrp;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public StatusMonitorComp(Shell parentShell) {
        super(parentShell, SWT.NONE);

        init();
    }

    protected void disposedAction() {
        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getStatusDestination(), this);
        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);

        // Remove the listener and dispose of the VizStatusMonitor.
        vizStatusMonitor.removeListener(this);
        vizStatusMonitor.dispose();
    }

    private void init() {

        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                disposedAction();
            }
        });

        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getStatusDestination(), this);
        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getBMHConfigDestination(), this);

        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        this.setLayout(gl);
        this.setLayoutData(gd);

        createDacTransmitterStatusControls();
        createProcessStatusControlGroup();

    }

    /**
     * Create the DAC & Transmitter Group status controls.
     */
    private void createDacTransmitterStatusControls() {
        dacList = getDacList();
        tgList = getTransmitterGroups();

        vizStatusMonitor = new VizStatusMonitor();
        vizStatusMonitor.addListener(this);

        // TODO : remove when testing is done.
        // printVizStatusMonitorVariables();

        statusImages = new StatusImages(this);

        sdm = new StatusDataManager();

        Group dacXmitGrp = new Group(this, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        dacXmitGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        dacXmitGrp.setLayoutData(gd);
        dacXmitGrp.setText("DAC/Transmitter Status");

        scrolledComp = new ScrolledComposite(dacXmitGrp, SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL);
        gl = new GridLayout(1, false);
        gl.verticalSpacing = 1;
        scrolledComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 400;
        scrolledComp.setLayoutData(gd);

        dacTransmittersComp = new Composite(scrolledComp, SWT.NONE);
        dacTransmittersComp.setLayout(new GridLayout(1, false));
        dacTransmittersComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, true));

        populateDacTransmitterControls();

        scrolledComp.setExpandHorizontal(true);
        scrolledComp.setExpandVertical(true);
        scrolledComp.setContent(dacTransmittersComp);
        scrolledComp.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                scrolledComp.setMinSize(dacTransmittersComp.computeSize(
                        SWT.DEFAULT, SWT.DEFAULT));
            }
        });
        scrolledComp.layout();
    }

    /**
     * Create the Group that will contain the Process Status controls.
     */
    private void createProcessStatusControlGroup() {
        processStatusGrp = new Group(this, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        processStatusGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        processStatusGrp.setLayoutData(gd);
        processStatusGrp.setText("Process Status");

        populateProcessStatusControls();
    }

    /**
     * Populate the Process Status controls.
     */
    private void populateProcessStatusControls() {
        SortedSet<String> transmitterGroups = new TreeSet<>();

        for (TransmitterGroup tg : tgList) {
            transmitterGroups.add(tg.getName());
        }

        new ProcessStatusComp(processStatusGrp, vizStatusMonitor,
                transmitterGroups);
    }

    /**
     * Populate the DAC/Transmitter controls.
     */
    private void populateDacTransmitterControls() {
        DacTransmitterStatusData dtsd = sdm.createDacTransmitterStatusData(
                dacList, tgList, vizStatusMonitor.getDacStatus());

        Map<Integer, DacInfo> dacInfoMap = dtsd.getDacInfoMap();

        Iterator<Integer> iter = dacInfoMap.keySet().iterator();
        while (iter.hasNext() == true) {
            new DacStatusComp(dacTransmittersComp, dacInfoMap.get(iter.next()),
                    statusImages);

            if (iter.hasNext()) {
                DialogUtility.addSeparator(dacTransmittersComp, SWT.HORIZONTAL);
            }
        }

        DialogUtility.addSeparator(dacTransmittersComp, SWT.HORIZONTAL);

        /*
         * Show the Transmitters that have no associated DACs
         */
        Label noDacTransMittersLbl = new Label(dacTransmittersComp, SWT.NONE);
        noDacTransMittersLbl.setText("Not assigned to a DAC:");

        List<TransmitterGrpInfo> noDacTgiList = dtsd.getNoDacTransGrpInfoList();

        for (TransmitterGrpInfo tgi : noDacTgiList) {
            new TransmitterGroupStatusComp(dacTransmittersComp, tgi,
                    statusImages);
        }
    }

    /**
     * Repopulate the DAC/Transmitter controls.
     */
    private void repopulateDacTransmitterStatus() {
        if (dacTransmittersComp.isDisposed()) {
            return;
        }

        /*
         * Repopulate the DAC/Transmitter controls
         */
        Control[] childControls = dacTransmittersComp.getChildren();

        for (Control ctrl : childControls) {
            ctrl.dispose();
        }

        dacList = getDacList();
        tgList = getTransmitterGroups();
        populateDacTransmitterControls();

        dacTransmittersComp.layout();
        scrolledComp.layout();
        getShell().redraw();
    }

    private void repopulateProcessStatus() {
        if (this.processStatusGrp.isDisposed()) {
            return;
        }

        /*
         * Repopulate the Process Status controls
         */
        Control[] childControls = processStatusGrp.getChildren();
        for (Control ctrl : childControls) {
            ctrl.dispose();
        }

        populateProcessStatusControls();
        processStatusGrp.layout();
        getShell().layout();
        getShell().redraw();
    }

    /**
     * Get the list of DACs.
     * 
     * @return List of DACs.
     */
    private List<Dac> getDacList() {
        DacDataManager ddm = new DacDataManager();
        List<Dac> dacs = null;

        try {
            dacs = ddm.getDacs();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving the list of available DACs: ", e);
            return Collections.emptyList();
        }

        if (dacs == null) {
            return Collections.emptyList();
        }

        return dacs;
    }

    /**
     * Get the transmitter groups.
     * 
     * @return A list of the transmitter groups.
     */
    private List<TransmitterGroup> getTransmitterGroups() {
        TransmitterDataManager tdm = new TransmitterDataManager();
        List<TransmitterGroup> tgList = null;

        try {
            tgList = tdm.getTransmitterGroups();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving the list of available DACs: ", e);
            return Collections.emptyList();
        }

        if (tgList == null) {
            return Collections.emptyList();
        }

        return tgList;
    }

    /**
     * Notification when something changes in the database for transmitter
     * groups or DACs.
     */
    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                if ((o instanceof TransmitterGroupConfigNotification)
                        || (o instanceof DacConfigNotification)) {

                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            repopulateDacTransmitterStatus();
                        }
                    });

                }
            } catch (NotificationException e) {
                statusHandler.error("Error processing update notification", e);
            }
        }
    }

    /**
     * System status changed notification.
     */
    @Override
    public void systemStatusChanged(BmhComponent component, String key) {
        if (component == BmhComponent.DAC) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    repopulateDacTransmitterStatus();
                }
            });
        } else {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    repopulateProcessStatus();
                }
            });
        }
    }

    // ****************************************************************************
    // ****************************************************************************
    // TODO : REMOVE THIS TESTING IS DONE
    // ****************************************************************************
    // ****************************************************************************
    private void printVizStatusMonitorVariables() {

        Map<String, DacHardwareStatusNotification> ds = vizStatusMonitor
                .getDacStatus();

        System.out
                .println("********** DacHardwareStatusNotification *********************");
        for (String s : ds.keySet()) {
            System.out.println("Dac Status Key: " + s);
            DacHardwareStatusNotification dhsn = ds.get(s);
            System.out.println("--- PS1 Voltage: " + dhsn.getPsu1Voltage());
            System.out.println("--- PS2 Voltage: " + dhsn.getPsu2Voltage());
            System.out.println("--- Buffer size: " + dhsn.getBufferSize());
            System.out.println("--- Transmitter Group: "
                    + dhsn.getTransmitterGroup());

            // TODO : status voice should be used to determine if there is
            // silence from the dac and should display an "A"

            DacVoiceStatus[] dvsArray = dhsn.getVoiceStatus();
            for (DacVoiceStatus dvs : dvsArray) {
                System.out.println("****** Dac Status Voice:" + dvs);
                System.out
                        .println("****** Dac Status Voice name:" + dvs.name());
            }

            int[] validChannels = dhsn.getValidChannels();

            for (int i : validChannels) {
                System.out.println(">>>> Valid Channel: " + i);
            }
        }

        System.out.println("\n\n********** Connected *********************");

        Map<String, BmhEdexStatus> edexStatusMap = vizStatusMonitor
                .getEdexStatus();

        for (String s : edexStatusMap.keySet()) {
            System.out.println("Host: " + s);
            System.out.println("EDEX: " + vizStatusMonitor.isEdexConnected(s));
            System.out.println("Comms Mgr: "
                    + vizStatusMonitor.isCommsManagerConnected(s));

            for (TransmitterGroup tg : tgList) {
                System.out.println("--- DAC connected: "
                        + tg.getName()
                        + " - "
                        + vizStatusMonitor.isTransmitterGroupConnected(s,
                                tg.getName()));
            }
        }

        System.out
                .println("\n\n********** Connected Comm Managers *********************");

        Collection<CommsManagerStatus> commMgrStatusArray = vizStatusMonitor
                .getConnectedCommsManagers();

        for (CommsManagerStatus cms : commMgrStatusArray) {
            Set<String> connectedXmitGrps = cms.getConnectedTransmitterGroups();
            for (String s : connectedXmitGrps) {
                System.out.println("Connected Transmitter Group: " + s);
            }
        }
    }
}
