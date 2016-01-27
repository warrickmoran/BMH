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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import com.raytheon.uf.common.bmh.notify.config.DacNotSyncNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.request.DacResponse;
import com.raytheon.uf.common.bmh.systemstatus.ISystemStatusListener;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
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
 * Mar 06, 2015  #4241     rjpeter      Put in retrieval job.
 * Mar 10, 2015  #4219     bsteffen     Reset min size on scrolledComp when transmitters change.
 * Apr 01, 2015  4219      bsteffen     Allow multiple transmitter groups with no ports assigned.
 * Dec 01, 2015  5113      bkowal       Report when a BMH Dac and a DAC are no longer in sync.
 * Jan 25, 2016  4997      bkowal       Prevent potential NPE when no DACs are found.
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

    private final long UPDATE_INTERVAL = TimeUtil.MILLIS_PER_SECOND;

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

    /** Process Status group container. */
    private Group processStatusGrp;

    /** RetrievalJob for looking up data from server and updating display. */
    private final StatusRetrievalJob retrievalJob = new StatusRetrievalJob();

    /** Retrieval type for the retrieval job */
    private enum RetrieveType {
        DacTransmitter, Process, Both
    }

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

        DacResponse dacResponse = this.getDacsAndSyncStatus();
        List<Dac> dacList = Collections.emptyList();
        List<Integer> desyncedDacs = Collections.emptyList();
        if (dacResponse != null) {
            dacList = dacResponse.getDacList();
            desyncedDacs = dacResponse.getDesyncedDacs();
        }

        List<TransmitterGroup> tgList = getTransmitterGroups();

        createDacTransmitterStatusControls(dacList, tgList, desyncedDacs);
        createProcessStatusControlGroup(tgList);
    }

    /**
     * Create the DAC & Transmitter Group status controls.
     * 
     * @param dacList
     * @param tgList
     */
    private void createDacTransmitterStatusControls(List<Dac> dacList,
            List<TransmitterGroup> tgList, List<Integer> desyncedDacs) {

        vizStatusMonitor = new VizStatusMonitor();
        vizStatusMonitor.addListener(this);

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

        populateDacTransmitterControls(dacList, tgList, desyncedDacs);

        scrolledComp.setExpandHorizontal(true);
        scrolledComp.setExpandVertical(true);
        scrolledComp.setContent(dacTransmittersComp);
        scrolledComp.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                scrolledComp.setMinSize(dacTransmittersComp.computeSize(
                        SWT.DEFAULT, SWT.DEFAULT));
            }
        });
        scrolledComp.layout();
    }

    /**
     * Create the Group that will contain the Process Status controls.
     * 
     * @param tgList
     */
    private void createProcessStatusControlGroup(List<TransmitterGroup> tgList) {
        processStatusGrp = new Group(this, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        processStatusGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        processStatusGrp.setLayoutData(gd);
        processStatusGrp.setText("Process Status");

        populateProcessStatusControls(tgList);
    }

    /**
     * Populate the Process Status controls.
     * 
     * @param tgList
     */
    private void populateProcessStatusControls(List<TransmitterGroup> tgList) {
        SortedSet<String> transmitterGroups = new TreeSet<>();

        for (TransmitterGroup tg : tgList) {
            transmitterGroups.add(tg.getName());
        }

        new ProcessStatusComp(processStatusGrp, vizStatusMonitor,
                transmitterGroups);
    }

    /**
     * Populate the DAC/Transmitter controls.
     * 
     * @param dacList
     * @param tgList
     */
    private void populateDacTransmitterControls(List<Dac> dacList,
            List<TransmitterGroup> tgList, List<Integer> desyncedDacs) {
        DacTransmitterStatusData dtsd = sdm.createDacTransmitterStatusData(
                dacList, tgList, desyncedDacs, vizStatusMonitor);

        Map<Integer, DacInfo> dacInfoMap = dtsd.getDacInfoMap();

        Iterator<DacInfo> iter = dacInfoMap.values().iterator();
        while (iter.hasNext() == true) {
            new DacStatusComp(dacTransmittersComp, iter.next(), statusImages);

            if (iter.hasNext()) {
                DialogUtility.addSeparator(dacTransmittersComp, SWT.HORIZONTAL);
            }
        }

        /*
         * Show the Transmitters that have no associated DACs
         */

        List<TransmitterGrpInfo> noDacTgiList = dtsd.getNoDacTransGrpInfoList();

        if (!noDacTgiList.isEmpty()) {
            DialogUtility.addSeparator(dacTransmittersComp, SWT.HORIZONTAL);
            Label noDacTransMittersLbl = new Label(dacTransmittersComp,
                    SWT.NONE);
            noDacTransMittersLbl.setText("Not assigned to a DAC:");
            for (TransmitterGrpInfo tgi : noDacTgiList) {
                new TransmitterGroupStatusComp(dacTransmittersComp, tgi,
                        statusImages);
            }
        }
    }

    /**
     * Repopulate the DAC/Transmitter controls.
     * 
     * @param dacList
     * @param tgList
     */
    private void repopulateDacTransmitterStatus(List<Dac> dacList,
            List<TransmitterGroup> tgList, List<Integer> desyncedDacs) {
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

        populateDacTransmitterControls(dacList, tgList, desyncedDacs);

        dacTransmittersComp.layout();
        scrolledComp.setMinSize(dacTransmittersComp.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        scrolledComp.layout();
        getShell().redraw();
    }

    private void repopulateProcessStatus(List<TransmitterGroup> tgList) {
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

        populateProcessStatusControls(tgList);
        processStatusGrp.layout();
        getShell().layout();
        getShell().redraw();
    }

    /**
     * Get the {@link List} of all BMH {@link Dac}s as well as a {@link List} of
     * the BMH {@link Dac}s that are no longer in sync with their associated
     * DAC.
     * 
     * @return a {@link DacResponse} containing the required information.
     */
    private DacResponse getDacsAndSyncStatus() {
        DacDataManager ddm = new DacDataManager();
        DacResponse response = null;

        try {
            response = ddm.getDacsAndSyncStatus();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving the list of available DACs: ", e);
        }

        return response;
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
                        || (o instanceof DacConfigNotification)
                        || (o instanceof DacNotSyncNotification)) {
                    retrievalJob.scheduleRetrieval(RetrieveType.DacTransmitter);
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
            retrievalJob.scheduleRetrieval(RetrieveType.DacTransmitter);
        } else {
            retrievalJob.scheduleRetrieval(RetrieveType.Process);
        }
    }

    /**
     * Internal job to handle looking up data from edex and ensuring we don't
     * update the display more than once a second.
     */
    private class StatusRetrievalJob extends Job {
        private final Object lock = new Object();

        private RetrieveType nextType;

        /**
         * @param name
         */
        public StatusRetrievalJob() {
            super("Retrieving Status Data");
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            RetrieveType type;
            synchronized (lock) {
                type = nextType;
                nextType = null;
            }

            if (type == null) {
                return Status.OK_STATUS;
            }

            /* only need dacList if doing a DacTransmitter status */
            final List<Dac> dacList;
            final List<Integer> desyncedDacs;
            DacResponse dacResponse = (!RetrieveType.Process.equals(type) ? getDacsAndSyncStatus()
                    : null);
            if (dacResponse != null) {
                dacList = dacResponse.getDacList();
                desyncedDacs = dacResponse.getDesyncedDacs();
            } else {
                dacList = Collections.emptyList();
                desyncedDacs = null;
            }
            final List<TransmitterGroup> tgList = getTransmitterGroups();
            final RetrieveType runType = type;

            VizApp.runSync(new Runnable() {
                @Override
                public void run() {
                    if (!RetrieveType.Process.equals(runType)) {
                        repopulateDacTransmitterStatus(dacList, tgList,
                                desyncedDacs);
                    }
                    if (!RetrieveType.DacTransmitter.equals(runType)) {
                        repopulateProcessStatus(tgList);
                    }
                }
            });

            return Status.OK_STATUS;
        }

        protected void scheduleRetrieval(RetrieveType type) {
            synchronized (lock) {
                if (nextType == null) {
                    nextType = type;
                    schedule(UPDATE_INTERVAL);
                } else if (!type.equals(nextType)) {
                    nextType = RetrieveType.Both;
                }
            }
        }
    }
}
