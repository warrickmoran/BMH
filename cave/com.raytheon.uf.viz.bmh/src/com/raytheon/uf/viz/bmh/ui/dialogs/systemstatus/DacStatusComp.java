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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.StatusImages.StatusImage;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.DacInfo.DAC_VOLTAGE;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterGrpInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data.TransmitterInfo;

/**
 * Composite containing the images and controls displaying the status of the
 * DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 17, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Additional status updates.
 * Jan 27, 2015  4029      bkowal       Buffer size is now included in
 *                                      the tooltip.
 * Jan 29, 2015  4029      bkowal       Simplified Dac Voltage reporting.
 * Apr 01, 2015  4219      bsteffen     Allow multiple transmitter groups with no ports assigned.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class DacStatusComp extends Composite {

    /** DAC information data class. */
    private DacInfo dacInfo;

    /**
     * Status images - class that has the images used for displaying the status.
     */
    private StatusImages statusImages;

    /** DAC image. */
    private Image dacImage = null;

    /** Label used to display the DAC image. */
    private Label dacImgLbl;

    /** DAC label font. */
    private Font dacLabelFont = null;

    /** DAC information label. */
    private Label dacInfoLbl;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     * @param dacInfo
     *            DAC information data class.
     * @param statusImages
     *            Status images.
     */
    public DacStatusComp(Composite parentComp, DacInfo dacInfo,
            StatusImages statusImages) {
        super(parentComp, SWT.NONE);

        this.dacInfo = dacInfo;
        this.statusImages = statusImages;

        init();
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 1;
        gl.verticalSpacing = 3;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        dacImage = statusImages.getStatusImage(StatusImage.Dac);

        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (dacImage != null) {
                    dacImage.dispose();
                }

                if (dacLabelFont != null) {
                    dacLabelFont.dispose();
                }
            }
        });

        // Create the DAC controls.
        createDacControls();

        List<TransmitterGrpInfo> transmitterGrpInfoList = dacInfo
                .getTransmitterGrpInfoList();

        Set<Integer> activePorts = new HashSet<Integer>();

        /*
         * Loop over the transmitters/group associated with this DAC.
         */
        for (TransmitterGrpInfo tgi : transmitterGrpInfoList) {
            // Create the transmitters/groups controls.
            new TransmitterGroupStatusComp(this, tgi, statusImages);

            // Find the assigned ports
            List<TransmitterInfo> transmittersList = tgi
                    .getTransmitterInfoList();
            for (TransmitterInfo ti : transmittersList) {
                if ((ti.getDacPort() != null) && ti.getDacPort() > 0
                        && ti.getDacPort() <= 4) {
                    activePorts.add(ti.getDacPort());
                }
            }
        }

        // Color the port(s) that have transmitters and color the DAC image
        // based on the status of the DAC.

        boolean goodDacStatus = validateDacStatus();
        statusImages.changeDacStatus(activePorts, dacImage, goodDacStatus);
        dacImgLbl.setImage(dacImage);

        if (!goodDacStatus) {
            dacInfoLbl
                    .setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
        }
    }

    /**
     * Create the DAC controls to display the images and status.
     */
    private void createDacControls() {
        Composite dacComp = new Composite(this, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 3;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        dacComp.setLayout(gl);
        dacComp.setLayoutData(gd);

        dacImgLbl = new Label(dacComp, SWT.NONE);
        dacImgLbl.setImage(statusImages.getStatusImage(StatusImage.Dac));
        createToolTip(dacImgLbl);

        gd = new GridData();
        gd.horizontalIndent = 5;
        dacInfoLbl = new Label(dacComp, SWT.NONE);
        dacInfoLbl.setText(dacInfo.getDacName());
        dacInfoLbl.setLayoutData(gd);

        FontData fd = dacInfoLbl.getFont().getFontData()[0];
        fd.setStyle(SWT.BOLD);
        dacLabelFont = new Font(getDisplay(), fd);
        dacInfoLbl.setFont(dacLabelFont);
    }

    /**
     * Validate the DAC status.
     * 
     * @return
     */
    private boolean validateDacStatus() {
        // TODO : add more validation

        if (dacInfo.getPsu1Voltage() != DAC_VOLTAGE.OK
                || dacInfo.getPsu2Voltage() != DAC_VOLTAGE.OK) {
            return false;
        }

        return true;
    }

    /**
     * Create the tool tip test use when hovering over the DAC image.
     * 
     * @param lbl
     *            Label containing the DAC image.
     */
    private void createToolTip(Label lbl) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name    : ").append(dacInfo.getDacName()).append("\n");
        sb.append("Address : ").append(dacInfo.getDacAddress()).append("\n");
        sb.append("PSU1 Voltage : ")
                .append(dacInfo.getPsu1Voltage().toString()).append("\n");
        sb.append("PSU2 Voltage : ")
                .append(dacInfo.getPsu2Voltage().toString()).append("\n");

        new CustomToolTip(lbl, sb.toString());
    }
}
