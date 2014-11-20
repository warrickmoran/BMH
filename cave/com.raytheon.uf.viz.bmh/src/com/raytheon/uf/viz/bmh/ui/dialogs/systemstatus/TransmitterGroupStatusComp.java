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

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.StatusImages.StatusImage;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterGrpInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterInfo;

/**
 * Create the transmitter group & transmitter controls or the stand alone
 * transmitter controls.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2014            lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class TransmitterGroupStatusComp extends Composite {

    /** Transmitter group information. */
    private TransmitterGrpInfo transmitterGrpInfo;

    /** Status images. */
    private StatusImages statusImages;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param transmitterGrpInfo
     *            Transmitter group information.
     * @param statusImages
     *            Status images.
     */
    public TransmitterGroupStatusComp(Composite parentComp,
            TransmitterGrpInfo transmitterGrpInfo, StatusImages statusImages) {
        super(parentComp, SWT.NONE);

        this.transmitterGrpInfo = transmitterGrpInfo;
        this.statusImages = statusImages;

        setupCompositeLayout();

        if (this.transmitterGrpInfo.isStandalone()) {
            createStandAloneTransmitterControls();
        } else {
            createTransmitterGroupControls();
        }
    }

    /**
     * Setup the composite layout.
     */
    private void setupCompositeLayout() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 1;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalIndent = 20;
        this.setLayout(gl);
        this.setLayoutData(gd);
    }

    /**
     * Create the transmitter group controls.
     */
    private void createTransmitterGroupControls() {
        Composite xmitGrpComp = new Composite(this, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        xmitGrpComp.setLayout(gl);
        xmitGrpComp.setLayoutData(gd);

        /*
         * Silent Alarm
         */
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 16;
        Label silentAlarmLbl = new Label(xmitGrpComp, SWT.NONE);
        silentAlarmLbl.setLayoutData(gd);
        if (transmitterGrpInfo.isSilenceAlarm()) {
            // silentAlarmLbl.setImage(silentAlarmImg);
            silentAlarmLbl.setImage(statusImages
                    .getStatusImage(StatusImage.SilentAlarm));
        }

        /*
         * Transmitter group & name
         */
        gd = new GridData();
        gd.widthHint = 53;
        Label transmitterGrpImgLbl = new Label(xmitGrpComp, SWT.NONE);
        transmitterGrpImgLbl.setLayoutData(gd);
        transmitterGrpImgLbl.setImage(statusImages
                .getStatusImage(StatusImage.TransmitterGrp));
        createTransmitterGroupToolTip(transmitterGrpImgLbl);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        Label transmitterGrpDescLbl = new Label(xmitGrpComp, SWT.NONE);
        transmitterGrpDescLbl.setLayoutData(gd);

        transmitterGrpDescLbl.setText(transmitterGrpInfo.getGroupName());

        /*
         * Create the transmitters.
         */
        SortedMap<Integer, List<TransmitterInfo>> transInfoMap = transmitterGrpInfo
                .getTransmitterInfoMap();

        gl = new GridLayout(3, false);
        gl.marginHeight = 1;
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalIndent = 60;
        Composite xmitComp = new Composite(this, SWT.NONE);
        xmitComp.setLayout(gl);
        xmitComp.setLayoutData(gd);

        boolean addSeparator = false;
        Iterator<Integer> iter = transInfoMap.keySet().iterator();
        while (iter.hasNext()) {
            List<TransmitterInfo> transInfoList = transInfoMap.get(iter.next());

            addSeparator = (iter.hasNext()) ? true : false;

            createTransmitterForGroupControls(xmitComp, transInfoList,
                    addSeparator);
        }
    }

    /**
     * Create the transmitter controls for the group. This is only called if the
     * group contains more than one transmitter.
     * 
     * @param xmitComp
     *            Transmitter composite.
     * @param transmitterInfoList
     *            List of transmitter information.
     * @param addSeparator
     *            Flag indicating if a separator should be added to the
     *            transmitter composite.
     */
    private void createTransmitterForGroupControls(Composite xmitComp,
            List<TransmitterInfo> transmitterInfoList, boolean addSeparator) {

        Iterator<TransmitterInfo> iter = transmitterInfoList.iterator();
        while (iter.hasNext()) {

            TransmitterInfo ti = iter.next();

            GridData gd = new GridData();
            gd.widthHint = 20;
            Label transmitterImgLbl = new Label(xmitComp, SWT.NONE);
            transmitterImgLbl.setLayoutData(gd);

            if (ti.getTxStatus() == TxStatus.ENABLED) {
                transmitterImgLbl.setImage(statusImages
                        .getStatusImage(StatusImage.Transmitter));
            } else {
                transmitterImgLbl.setImage(statusImages
                        .getStatusImage(StatusImage.TransmitterDisabled));
            }
            createTransmitterToolTip(transmitterImgLbl, ti, false);

            // gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
            Label transmitterDescLbl = new Label(xmitComp, SWT.NONE);
            // transmitterDescLbl.setLayoutData(gd);
            transmitterDescLbl.setText(ti.getMnemonic());

            gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
            Label transmitterPortLbl = new Label(xmitComp, SWT.NONE);
            transmitterPortLbl.setLayoutData(gd);
            transmitterPortLbl.setText(" - Port: " + ti.getDacPort());

            if (iter.hasNext() == false && addSeparator) {
                DialogUtility.addSeparator(xmitComp, SWT.HORIZONTAL);
            }
        }
    }

    /**
     * Create controls for a stand-alone transmitter.
     */
    private void createStandAloneTransmitterControls() {

        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 1;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalIndent = 20;
        this.setLayout(gl);
        this.setLayoutData(gd);

        SortedMap<Integer, List<TransmitterInfo>> transmitterInfoMap = transmitterGrpInfo
                .getTransmitterInfoMap();
        TransmitterInfo transmitterInfo = transmitterInfoMap.get(
                transmitterInfoMap.firstKey()).get(0);

        Composite xmitComp = new Composite(this, SWT.NONE);
        gl = new GridLayout(4, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        xmitComp.setLayout(gl);
        xmitComp.setLayoutData(gd);

        /*
         * Silent Alarm
         */
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 16;
        Label silentAlarmLbl = new Label(xmitComp, SWT.NONE);
        silentAlarmLbl.setLayoutData(gd);
        if (transmitterGrpInfo.isSilenceAlarm()) {
            silentAlarmLbl.setImage(statusImages
                    .getStatusImage(StatusImage.SilentAlarm));
        }

        /*
         * Transmitter & transmitter mnemonic.
         */
        gd = new GridData();
        gd.widthHint = 20;
        Label transmitterImgLbl = new Label(xmitComp, SWT.NONE);
        transmitterImgLbl.setLayoutData(gd);

        if (transmitterInfo.getTxStatus() == TxStatus.ENABLED) {
            transmitterImgLbl.setImage(statusImages
                    .getStatusImage(StatusImage.Transmitter));
        } else {
            transmitterImgLbl.setImage(statusImages
                    .getStatusImage(StatusImage.TransmitterDisabled));
        }
        createTransmitterToolTip(transmitterImgLbl, transmitterInfo,
                transmitterGrpInfo.isSilenceAlarm());

        gd = new GridData();
        gd.widthHint = 30;
        Label transmitterDescLbl = new Label(xmitComp, SWT.NONE);
        transmitterDescLbl.setLayoutData(gd);
        transmitterDescLbl.setText(transmitterInfo.getMnemonic());

        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        Label transmitterPortLbl = new Label(xmitComp, SWT.NONE);
        transmitterPortLbl.setLayoutData(gd);
        transmitterPortLbl.setText(" - Port: "
                + checkForNull(transmitterInfo.getDacPort()));
    }

    /**
     * Create the tooltip for the transmitter group label.
     * 
     * @param lbl
     *            Transmitter group label.
     */
    private void createTransmitterGroupToolTip(Label lbl) {
        StringBuilder sb = new StringBuilder();

        String silentAlarm = (transmitterGrpInfo.isSilenceAlarm()) ? "Yes"
                : "No";

        sb.append("Name         : ").append(transmitterGrpInfo.getGroupName())
                .append("\n");
        sb.append("Silent Alarm : ").append(silentAlarm);

        new CustomToolTip(lbl, sb.toString());
    }

    /**
     * Create the tooltip for the individual transmitter.
     * 
     * @param lbl
     *            Transmitter label.
     * @param transmitterInfo
     *            Transmitter information.
     * @param silentAlarm
     *            Silence Alarm flag.
     */
    private void createTransmitterToolTip(Label lbl,
            TransmitterInfo transmitterInfo, boolean silentAlarm) {
        StringBuilder sb = new StringBuilder();

        sb.append("Mnemonic  : ")
                .append(checkForNull(transmitterInfo.getMnemonic()))
                .append("\n");
        sb.append("Name      : ")
                .append(checkForNull(transmitterInfo.getName())).append("\n");
        sb.append("Call Sign : ")
                .append(checkForNull(transmitterInfo.getCallSign()))
                .append("\n");
        sb.append("Dac Port  : ")
                .append(checkForNull(transmitterInfo.getDacPort()))
                .append("\n");
        sb.append("Status    : ")
                .append(checkForNull(transmitterInfo.getTxStatus()))
                .append("\n");

        sb.append("Silence Alarm : ").append((silentAlarm) ? "Yes" : "No");

        new CustomToolTip(lbl, sb.toString());
    }

    /**
     * Check for null object.
     * 
     * @param obj
     *            Object to check.
     * @return If null, return "N/A". Otherwise return the object toString().
     */
    private String checkForNull(Object obj) {
        if (obj == null) {
            return "N/A";
        }

        return obj.toString();
    }
}