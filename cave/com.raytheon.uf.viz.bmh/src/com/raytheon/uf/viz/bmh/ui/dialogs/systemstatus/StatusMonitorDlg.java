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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.config.DacConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacTransmitterStatusData;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.StatusDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterGrpInfo;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog that displays all of the data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014  3349      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class StatusMonitorDlg extends CaveSWTDialog implements
        INotificationObserver {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(StatusMonitorDlg.class);

    /**
     * Status data manager that manages all of the data for displaying the
     * status.
     */
    private StatusDataManager sdm;

    /** The status images for the DAC and transmitters. */
    private StatusImages statusImages;

    /** BMH DAC Status */
    private final String BMH_DAC_STATUS = "BMH.DAC.Status";

    /** BMH config */
    private final String BMH_CONFIG = "BMH.Config";

    /** BMH Practice DAC Status */
    private final String BMH_PRACTICE_DAC_STATUS = "BMH.Practice.DAC.Status";

    /** BMH practice config */
    private final String BMH_PRACTICE_CONFIG = "BMH.Practice.Config";

    /** DAC/Transmitter composite. */
    private Composite dacTransmittersComp = null;

    /** Scrolled composite for the DACs and transmitters. */
    private ScrolledComposite scrolledComp;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public StatusMonitorDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        setText("System Status");
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.verticalSpacing = 3;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
        if (CAVEMode.getMode() == CAVEMode.OPERATIONAL) {
            NotificationManagerJob.removeObserver(BMH_DAC_STATUS, this);
            NotificationManagerJob.removeObserver(BMH_CONFIG, this);
        } else {
            NotificationManagerJob
                    .removeObserver(BMH_PRACTICE_DAC_STATUS, this);
            NotificationManagerJob.removeObserver(BMH_PRACTICE_CONFIG, this);
        }
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());

        if (CAVEMode.getMode() == CAVEMode.OPERATIONAL) {
            NotificationManagerJob.addObserver(BMH_DAC_STATUS, this);
            NotificationManagerJob.addObserver(BMH_CONFIG, this);
        } else {
            NotificationManagerJob.addObserver(BMH_PRACTICE_DAC_STATUS, this);
            NotificationManagerJob.addObserver(BMH_PRACTICE_CONFIG, this);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {

        statusImages = new StatusImages(shell);

        sdm = new StatusDataManager();

        scrolledComp = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 1;
        scrolledComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 330;
        gd.heightHint = 500;
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
     * Populate the DAC/Transmitter controls.
     */
    private void populateDacTransmitterControls() {
        List<Dac> dacList = getDacList();
        List<TransmitterGroup> tgList = getTransmitterGroups();
        DacTransmitterStatusData dtsd = sdm.createDacTransmitterStatusData(
                dacList, tgList);

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
        noDacTransMittersLbl
                .setText("Transmitters/Groups not assigned to a DAC:");

        List<TransmitterGrpInfo> noDacTgiList = dtsd.getNoDacTransGrpInfoList();

        for (TransmitterGrpInfo tgi : noDacTgiList) {
            TransmitterGroupStatusComp tgsc = new TransmitterGroupStatusComp(
                    dacTransmittersComp, tgi, statusImages);
        }
    }

    /**
     * Repopulate the DAC/Transmitter controls.
     */
    private void repopulateDacTransmitters() {
        Control[] childControls = dacTransmittersComp.getChildren();

        for (Control ctrl : childControls) {
            ctrl.dispose();
        }

        populateDacTransmitterControls();

        dacTransmittersComp.layout();
        scrolledComp.layout();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.jms.notification.INotificationObserver#
     * notificationArrived
     * (com.raytheon.uf.common.jms.notification.NotificationMessage[])
     */
    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {
                Object o = message.getMessagePayload();
                System.out.println(o.getClass().getName());
                if ((o instanceof TransmitterGroupConfigNotification)
                        || (o instanceof DacConfigNotification)) {

                    VizApp.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            repopulateDacTransmitters();
                        }
                    });

                } else if (o instanceof DacHardwareStatusNotification) {
                    // TODO : implement this functionality when Ben's code is
                    // available.
                    System.out
                            .println("DAC hardware status notification change.");
                }

            } catch (NotificationException e) {
                statusHandler.error("Error processing update notification", e);
            }
        }
    }
}
