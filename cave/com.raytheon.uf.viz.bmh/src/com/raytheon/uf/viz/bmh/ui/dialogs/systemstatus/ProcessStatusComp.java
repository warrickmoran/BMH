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
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.bmh.notify.status.BmhEdexStatus;
import com.raytheon.uf.common.bmh.notify.status.TTSStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;

/**
 * Composite that displayed the process status information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 22, 2014            lvenable    Initial creation
 * Dec 03, 2014  3876      lvenable    Added null check.
 * Jan 26, 2015  4020      bkowal      Specify the comms host when looking up
 *                                     active transmitter groups.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ProcessStatusComp extends Composite {

    /** Viz Status Monitor. */
    private VizStatusMonitor vizStatusMonitor;

    /** Set of Transmitter Group names. */
    private SortedSet<String> transmitterGroups;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param vizStatusMonitor
     *            Viz Status Monitor.
     * @param transmitterGroups
     *            Transmitter group names.
     */
    public ProcessStatusComp(Composite parentComp,
            VizStatusMonitor vizStatusMonitor,
            SortedSet<String> transmitterGroups) {
        super(parentComp, SWT.NONE);

        this.vizStatusMonitor = vizStatusMonitor;
        this.transmitterGroups = transmitterGroups;

        init();
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 1;
        gl.verticalSpacing = 7;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        Map<String, BmhEdexStatus> besMap = vizStatusMonitor.getEdexStatus();
        SortedSet<String> hostMap = new TreeSet<>();
        for (String s : besMap.keySet()) {
            hostMap.add(s);
        }

        for (String s : hostMap) {
            /*
             * Host
             */
            Label hostLbl = new Label(this, SWT.NONE);
            hostLbl.setText("Host: " + s);

            /*
             * EDEX and Comms manager
             */
            Composite edexCommsComp = new Composite(this, SWT.NONE);
            edexCommsComp.setLayout(new GridLayout(2, false));
            edexCommsComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                    true, false));

            gd = new GridData();
            gd.horizontalIndent = 15;
            Label edexDescLbl = new Label(edexCommsComp, SWT.NONE);
            edexDescLbl.setText("BMH EDEX: ");
            edexDescLbl.setLayoutData(gd);

            Label edexLbl = new Label(edexCommsComp, SWT.NONE);
            String edexConnected = (vizStatusMonitor.isEdexConnected(s)) ? "Connected"
                    : "Not Connected";
            edexLbl.setText(edexConnected);

            gd = new GridData();
            gd.horizontalIndent = 15;
            Label commsMgrDescLbl = new Label(edexCommsComp, SWT.NONE);
            commsMgrDescLbl.setText("Comms Mgr : ");
            commsMgrDescLbl.setLayoutData(gd);

            Label commsMgrLbl = new Label(edexCommsComp, SWT.NONE);
            String commsConnected = (vizStatusMonitor
                    .isCommsManagerConnected(s)) ? "Connected"
                    : "Not Connected";
            commsMgrLbl.setText(commsConnected);

            /*
             * Transmitters/Group associated with the Comms manager.
             */
            for (String transGrpName : transmitterGroups) {
                if (vizStatusMonitor.isTransmitterGroupConnected(s,
                        transGrpName)) {
                    gd = new GridData();
                    gd.horizontalIndent = 35;
                    Label transGrpLbl = new Label(this, SWT.NONE);
                    transGrpLbl.setText("Xmit/Group: " + transGrpName);
                    transGrpLbl.setLayoutData(gd);
                }
            }
        }

        DialogUtility.addSeparator(this, SWT.HORIZONTAL);

        /*
         * TTS Status
         */
        Label ttsLbl = new Label(this, SWT.NONE);
        ttsLbl.setText("TTS Hosts: ");

        Collection<TTSStatus> ttsStatusList = vizStatusMonitor
                .getConnectedTtsHosts();

        for (TTSStatus tts : ttsStatusList) {

            String host = null;

            if (tts == null || tts.getHost() == null) {
                host = "N/A";
            } else {
                host = tts.getHost();
            }

            gd = new GridData();
            gd.horizontalIndent = 10;
            Label ttsStatusLbl = new Label(this, SWT.NONE);
            ttsStatusLbl.setText("Host: " + host);
        }
    }
}