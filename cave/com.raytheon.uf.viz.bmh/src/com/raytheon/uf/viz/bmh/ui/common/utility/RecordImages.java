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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * Class that generates the Record, Stop, and Play images.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014  #3329     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class RecordImages {

    /** Parent composite. */
    private Composite parent;

    /** Parent display. */
    private Display parentDisplay;

    /** Array of record images. */
    private Image[] recordImages;

    /** Width and height of the images in pixels. */
    private int imgWidthHeight = 17;

    /** Enumeration of record states. */
    public enum RecordAction {
        RECORD, STOP, PLAY;
    };

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite.
     */
    public RecordImages(Composite parent) {
        this.parent = parent;
        parentDisplay = parent.getDisplay();

        addDisposeToParent();
        createImageArray();
        drawImages();
    }

    /**
     * Add a dispose listener to the parent composite so the image can get
     * disposed when this class is no longer used.
     */
    private void addDisposeToParent() {
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (Image img : recordImages) {
                    img.dispose();
                }
            }
        });
    }

    /**
     * Create an array of record images.
     */
    private void createImageArray() {
        recordImages = new Image[RecordAction.values().length];

        for (int i = 0; i < RecordAction.values().length; i++) {
            recordImages[i] = new Image(parentDisplay, imgWidthHeight,
                    imgWidthHeight);
        }
    }

    /**
     * Draw all of the images.
     */
    private void drawImages() {
        drawImage(new RGB(255, 0, 1), RecordAction.RECORD);
        drawImage(new RGB(255, 255, 255), RecordAction.STOP);
        drawImage(new RGB(0, 255, 1), RecordAction.PLAY);
    }

    /**
     * Draw the image specified by the state passed in.
     * 
     * @param transparentColor
     *            RGB of the transparent color.
     * @param recAction
     *            Record action.
     */
    private void drawImage(RGB transparentColor, RecordAction recAction) {
        Image tmpImg;
        GC gc;
        tmpImg = new Image(parent.getDisplay(), imgWidthHeight, imgWidthHeight);
        gc = new GC(tmpImg);
        Color c = new Color(parentDisplay, transparentColor);
        gc.setBackground(c);
        gc.fillRectangle(0, 0, imgWidthHeight, imgWidthHeight);
        gc.dispose();

        ImageData idata = tmpImg.getImageData();

        int transparentPixelColor = idata.palette.getPixel(c.getRGB());
        c.dispose();

        idata.transparentPixel = transparentPixelColor;
        Image transparentIdeaImage = new Image(parent.getDisplay(), idata);

        gc = new GC(transparentIdeaImage);

        /*
         * Draw the record image associated with the state passed in.
         */
        if (recAction == RecordAction.RECORD) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_RED));
            gc.fillOval(1, 1, imgWidthHeight - 2, imgWidthHeight - 2);
        } else if (recAction == RecordAction.STOP) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] pointArray = new int[] { 1, 1, imgWidthHeight - 1, 1,
                    imgWidthHeight - 1, imgWidthHeight - 1, 1,
                    imgWidthHeight - 1, 1, 1 };
            gc.fillPolygon(pointArray);
        } else if (recAction == RecordAction.PLAY) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_GREEN));
            int[] pointArray = new int[] { 1, 1, imgWidthHeight - 2,
                    (imgWidthHeight / 2) + 1, 1, imgWidthHeight - 2, 1, 1 };
            gc.fillPolygon(pointArray);
        }

        gc.dispose();

        recordImages[recAction.ordinal()] = transparentIdeaImage;
        tmpImg.dispose();
    }

    /**
     * Get the image associated with the record state.
     * 
     * @param recAction
     *            Record action.
     * @return Record image.
     */
    public Image getImage(RecordAction recAction) {
        return recordImages[recAction.ordinal()];
    }
}
