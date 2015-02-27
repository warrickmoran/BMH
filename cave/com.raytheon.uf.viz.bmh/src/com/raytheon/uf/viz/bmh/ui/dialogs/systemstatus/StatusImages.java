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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.viz.bmh.Activator;

/**
 * Contains the images used with the system status monitor dialog. This contains
 * all of the transmitter, DAC images.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 17, 2014  3349      lvenable     Initial creation
 * Nov 23, 2014  #3287     lvenable     Added image for a silent alarm and
 *                                      if it's disabled.
 * Feb 27, 2015  #3962     rferrel      Added image for decommissioned.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class StatusImages {

    /** Enumeration of record states. */
    public enum StatusImage {
        Dac, Transmitter, TransmitterGrp, TransmitterDisabled, Alarm, DisabledSilentAlarm, AlarmPlusDisabledSilentAlarm, Decommissioned;
    };

    /** Transmitter image. */
    private Image transmitterImg;

    /** Silent Alarm image. */
    private Image disabledSilentAlarmImg;

    /** Disabled Silent Alarm image. */
    private Image disabledTransmitterImg;

    /** Alarm image. */
    private Image alarmImg;

    /** Transmitter group image. */
    private Image transmitterGrpImg;

    private Image alarmPlusDisabledSilentAlarmImg;

    /** Decommissioned Image */
    private Image decommissionedImage;

    /** Map of images. */
    private Map<StatusImage, Image> imageMap = new HashMap<StatusImage, Image>();

    /** Map of rectangle use to draw the DAC data ports. */
    private Map<Integer, Rectangle> portRecMap = new HashMap<Integer, Rectangle>();

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     */
    public StatusImages(Composite parentComp) {
        init(parentComp);
    }

    /**
     * Initialize method.
     * 
     * @param parentComp
     *            Parent composite.
     */
    private void init(Composite parentComp) {

        /*
         * Add a dispose listener to the parent composite. The images will be
         * disposed of when the parent gets disposed.
         */
        parentComp.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {

                for (Image img : imageMap.values()) {
                    img.dispose();
                }
                imageMap.clear();
            }
        });

        ImageDescriptor id;

        // Alarm
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/alarm.png");
        alarmImg = id.createImage();
        imageMap.put(StatusImage.Alarm, alarmImg);

        // Decommissioned
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/decommissioned.png");
        decommissionedImage = id.createImage();
        imageMap.put(StatusImage.Decommissioned, decommissionedImage);

        // Transmitter
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/xmit.png");
        transmitterImg = id.createImage();
        imageMap.put(StatusImage.Transmitter, transmitterImg);

        // Disabled transmitter
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/xmit_disabled.png");
        disabledTransmitterImg = id.createImage();
        imageMap.put(StatusImage.TransmitterDisabled, disabledTransmitterImg);

        // Silent Alarm
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/disabledSilentAlarm.png");
        disabledSilentAlarmImg = id.createImage();
        imageMap.put(StatusImage.DisabledSilentAlarm, disabledSilentAlarmImg);

        // Transmitter group
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/xmit_grp.png");
        transmitterGrpImg = id.createImage();
        imageMap.put(StatusImage.TransmitterGrp, transmitterGrpImg);

        // Alarm active with SilentAlarm disabled
        id = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/alarmDisabledSilent.png");
        alarmPlusDisabledSilentAlarmImg = id.createImage();
        imageMap.put(StatusImage.AlarmPlusDisabledSilentAlarm,
                alarmPlusDisabledSilentAlarmImg);
    }

    /**
     * Create a DAC image.
     * 
     * @return DAC image.
     */
    private Image createDacImage() {
        Display display = Display.getCurrent();

        Image dacImg = new Image(display, 48, 12);

        /*
         * Create the port rectangles
         */
        if (portRecMap.isEmpty()) {
            // Port 1
            Rectangle rec = new Rectangle(4, 4, 5, 4);
            portRecMap.put(1, rec);

            // Port 2
            rec = new Rectangle(16, 4, 5, 4);
            portRecMap.put(2, rec);

            // Port 3
            rec = new Rectangle(27, 4, 5, 4);
            portRecMap.put(3, rec);

            // Port 4
            rec = new Rectangle(39, 4, 5, 4);
            portRecMap.put(4, rec);
        }

        changeDacStatus(null, dacImg, true);

        return dacImg;
    }

    /**
     * Change the DAC image to reflect the active ports and if the status of the
     * DAC is good (gray) or bad (red).
     * 
     * @param activePorts
     *            Set of active ports.
     * @param img
     *            Image to change.
     * @param isGood
     *            True if the dac status is good, false otherwise.
     */
    public void changeDacStatus(Set<Integer> activePorts, Image img,
            boolean isGood) {

        if (img == null) {
            return;
        }

        Display display = Display.getCurrent();
        GC gc = new GC(img);
        gc.setAntialias(SWT.OFF);

        // Fill with black
        gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.fillRectangle(0, 0, 48, 12);

        /*
         * If the status is good then paint the image gray, if bad then paint
         * the image red.
         */
        if (isGood) {
            gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
            gc.fillRectangle(1, 1, 46, 10);
        } else {
            gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
            gc.fillRectangle(1, 1, 46, 10);
        }

        // Fill with black (the 4 ports outline
        gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.fillRectangle(2, 2, 9, 8);
        gc.fillRectangle(14, 2, 9, 8);
        gc.fillRectangle(25, 2, 9, 8);
        gc.fillRectangle(37, 2, 9, 8);

        // Fill with dark gray (the 4 ports outline
        gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
        gc.fillRectangle(3, 3, 7, 6);
        gc.fillRectangle(15, 3, 7, 6);
        gc.fillRectangle(26, 3, 7, 6);
        gc.fillRectangle(38, 3, 7, 6);

        // Set all the ports to white.
        gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        for (Rectangle r : portRecMap.values()) {
            gc.fillRectangle(r);
        }

        // Color the active ports green.
        if (activePorts != null) {
            gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
            for (Integer i : portRecMap.keySet()) {
                if (activePorts.contains(i)) {
                    gc.fillRectangle(portRecMap.get(i));
                }
            }
        }

        gc.dispose();
    }

    /**
     * Get the image associated with the Status Image value passed in.
     * 
     * NOTE: All images will automatically be disposed except for the DAC image.
     * Since that image changes it will be up to the class using the DAC image
     * to dispose it.
     * 
     * @param si
     *            Status Image class.
     * @return Image.
     */
    public Image getStatusImage(StatusImage si) {
        if (si == StatusImage.Dac) {
            Image dacImage = createDacImage();
            return dacImage;
        }

        return imageMap.get(si);
    }
}
