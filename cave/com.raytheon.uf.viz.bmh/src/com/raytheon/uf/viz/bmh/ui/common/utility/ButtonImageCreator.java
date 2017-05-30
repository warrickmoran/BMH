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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * This class will generate an image given the width, height, color, and text.
 * This is mostly used for creating images for buttons.
 * 
 * NOTE: The images that are created will be disposed of when the parent
 * composite is disposed. There is no need to disposed of the images that are
 * created in this class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 10, 2014  #3610     lvenable     Initial creation
 * Feb 24, 2017  #6030     bkowal       Added {@link #generateImage(Button, String, RGB)} to
 *                                      calculate a resolution-independent height/width.
 * 
 * </pre>
 * 
 * @author lvenable
 */
public class ButtonImageCreator {

    private final Composite parentComp;

    /** Parent display. */
    private Display parentDisplay;

    /** Font data for tool tip text. */
    private FontData fontData = null;

    /**
     * List of created images that will be disposed when the parent composite is
     * disposed.
     */
    private List<Image> createdImages = new ArrayList<Image>();

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     */
    public ButtonImageCreator(Composite parentComp) {
        this.parentComp = parentComp;
        parentDisplay = parentComp.getDisplay();
        fontData = new FontData("Monospace", 10, SWT.NORMAL);
        addDisposeToParent(parentComp);
    }

    /**
     * Add a dispose listener to the parent composite so the images are disposed
     * when the parent composite is disposed.
     * 
     * @param parent
     *            Parent composite.
     */
    private void addDisposeToParent(Composite parent) {
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (Image img : createdImages) {
                    img.dispose();
                }

                createdImages.clear();
            }
        });
    }

    /**
     * Set the font data for the text displayed on the image.
     * 
     * @param fd
     */
    public void setFontData(FontData fd) {
        this.fontData = fd;
    }

    /**
     * Generate the image with the text centered in the middle of the image.
     * 
     * NOTE: The image that is passed back will be disposed of when the parent
     * composite for this class is disposed. There is no need to dispose of the
     * image.
     * 
     * @param text
     *            Text to be drawn on the image.
     * @param rgb
     *            Color of the image background.
     * @return The generated image.
     */
    public Image generateImage(final String text, final RGB rgb) {
        return generateImage(text, rgb, 0, 0);
    }

    /**
     * Generate the image with the text centered in the middle of the image.
     * Optional horizontal and/or vertical padding can be added that will be
     * even distributed between the left and right side of the image and the top
     * and bottom of the image respectively.
     * 
     * NOTE: The image that is passed back will be disposed of when the parent
     * composite for this class is disposed. There is no need to dispose of the
     * image.
     * 
     * @param text
     *            Text to be drawn on the image.
     * @param rgb
     *            Color of the image background.
     * @param widthPadding
     *            the horizontal padding amount to apply evenly distributed
     *            between the right and left of the image.
     * @param heightPadding
     *            the vertical padding amount to apply evenly distributed
     *            between the top and bottom of the image.
     * @return The generated image.
     */
    public Image generateImage(final String text, final RGB rgb,
            final int widthPadding, final int heightPadding) {
        /*
         * Calculate the width and height in a resolution independent way.
         */
        Font tmpFont = null;
        tmpFont = new Font(parentDisplay, fontData);
        GC gc = new GC(parentComp);
        gc.setFont(tmpFont);

        final int imageHeight = gc.getFontMetrics().getHeight() + heightPadding;
        final int imageWidth = gc.textExtent(text).x + widthPadding;

        gc.dispose();
        tmpFont.dispose();

        return generateImage(imageWidth, imageHeight, text, rgb);
    }

    /**
     * Generate the image with the text centered in the middle of the image.
     * 
     * NOTE: The image that is passed back will be disposed of when the parent
     * composite for this class is disposed. There is no need to dispose of the
     * image.
     * 
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @param text
     *            Text to be drawn on the image.
     * @param rgb
     *            Color of the image background.
     * @return The generated image.
     */
    public Image generateImage(int width, int height, String text, RGB rgb) {
        return generateImage(width, height, text, rgb, fontData);
    }

    /**
     * Generate the image with the text centered in the middle of the image.
     * 
     * NOTE: The image that is passed back will be disposed of when the parent
     * composite for this class is disposed. There is no need to dispose of the
     * image.
     * 
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @param text
     *            Text to be drawn on the image.
     * @param rgb
     *            Color of the image background.
     * @param fd
     *            Font data for the text drawn on the image.
     * @return The generated image.
     */
    public Image generateImage(int imgWidth, int imgHeight, String text,
            RGB rgb, FontData fd) {
        Image tmpImg;
        GC gc;
        Font tmpFont = new Font(parentDisplay, fd);
        Color tmpColor = new Color(parentDisplay, rgb);

        tmpImg = new Image(parentDisplay, imgWidth, imgHeight);
        gc = new GC(tmpImg);

        gc.setFont(tmpFont);
        Point textExtent = gc.stringExtent(text);

        gc.setBackground(tmpColor);
        gc.fillRectangle(0, 0, imgWidth, imgHeight);

        int textXCoord = (imgWidth / 2) - (textExtent.x / 2);
        int textYCoord = (imgHeight / 2) - (textExtent.y / 2);

        gc.drawText(text, textXCoord, textYCoord, true);

        gc.dispose();
        tmpFont.dispose();
        createdImages.add(tmpImg);
        return tmpImg;
    }
}
