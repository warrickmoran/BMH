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
 * Class that generates up and down arrow images
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  #3338      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class UpDownImages {

    /** Parent composite. */
    private Composite parent;

    /** Parent display. */
    private Display parentDisplay;

    /** Array of arrow images. */
    private Image[] arrowImages;

    /** Width and height of the images in pixels. */
    private int imgWidthHeight = 17;

    /** Enumeration of arrow states. */
    public enum Arrows {
        DOUBLE_UP, UP, UP_NO_TAIL, DOUBLE_DOWN, DOWN, DOWN_NO_TAIL;
    };

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite.
     */
    public UpDownImages(Composite parent) {
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
                for (Image img : arrowImages) {
                    img.dispose();
                }
            }
        });
    }

    /**
     * Create an array of record images.
     */
    private void createImageArray() {
        arrowImages = new Image[Arrows.values().length];

        for (int i = 0; i < Arrows.values().length; i++) {
            arrowImages[i] = new Image(parentDisplay, imgWidthHeight,
                    imgWidthHeight);
        }
    }

    /**
     * Draw all of the images.
     */
    private void drawImages() {
        drawImage(new RGB(0, 0, 1), Arrows.DOUBLE_UP);
        drawImage(new RGB(0, 0, 1), Arrows.UP);
        drawImage(new RGB(0, 0, 1), Arrows.UP_NO_TAIL);
        drawImage(new RGB(0, 0, 1), Arrows.DOWN);
        drawImage(new RGB(0, 0, 1), Arrows.DOUBLE_DOWN);
        drawImage(new RGB(0, 0, 1), Arrows.DOWN_NO_TAIL);
    }

    /**
     * Draw the image specified by the state passed in.
     * 
     * @param transparentColor
     *            RGB of the transparent color.
     * @param recAction
     *            Record action.
     */
    private void drawImage(RGB transparentColor, Arrows arrow) {
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
        if (arrow == Arrows.DOUBLE_UP) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] upArrow1 = new int[] { 8, 1, 14, 7, 2, 7, 8, 1 };
            gc.fillPolygon(upArrow1);
            int[] upArrow2 = new int[] { 8, 9, 14, 15, 2, 15, 8, 9 };
            gc.fillPolygon(upArrow2);
        } else if (arrow == Arrows.UP_NO_TAIL) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] upArrow1 = new int[] { 2, 12, 14, 12, 8, 5, 2, 12 };
            gc.fillPolygon(upArrow1);
        } else if (arrow == Arrows.UP) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] pointArray = new int[] { 8, 1, 15, 8, 11, 8, 11, 15, 6, 15,
                    6, 8, 1, 8, 8, 1 };
            gc.fillPolygon(pointArray);
        } else if (arrow == Arrows.DOWN) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] pointArray = new int[] { 6, 1, 11, 1, 11, 8, 15, 8, 8, 15, 1,
                    8, 6, 8, 6, 1 };
            gc.fillPolygon(pointArray);
        } else if (arrow == Arrows.DOUBLE_DOWN) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] upArrow1 = new int[] { 2, 1, 14, 1, 8, 7, 2, 1 };
            gc.fillPolygon(upArrow1);
            int[] upArrow2 = new int[] { 2, 9, 14, 9, 8, 15, 2, 9 };
            gc.fillPolygon(upArrow2);
        } else if (arrow == Arrows.DOWN_NO_TAIL) {
            gc.setAntialias(SWT.ON);
            gc.setBackground(parentDisplay.getSystemColor(SWT.COLOR_BLACK));
            int[] upArrow1 = new int[] { 2, 5, 14, 5, 8, 12, 2, 5 };
            gc.fillPolygon(upArrow1);
        }

        gc.dispose();

        arrowImages[arrow.ordinal()] = transparentIdeaImage;
        tmpImg.dispose();
    }

    /**
     * Get the arrow image using the provided arrow type.
     * 
     * @param arrow
     *            Arrow type.
     * @return The arrow image.
     */
    public Image getImage(Arrows arrow) {
        return arrowImages[arrow.ordinal()];
    }
}
