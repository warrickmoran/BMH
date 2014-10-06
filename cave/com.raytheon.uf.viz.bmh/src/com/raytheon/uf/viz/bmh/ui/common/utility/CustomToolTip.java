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

import java.awt.Toolkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This is a custom "tool tip" which utilizes a SWT shell and label to create a
 * tool tip.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014 #3338      lvenable     Initial creation
 * Aug 23, 2014 #3490      lvenable     Add a fix for finding the total screen width of
 *                                      multiple monitors.
 * Oct 06, 2014  #3700     lvenable     Added a force hide that will dispose of the tool tip text.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CustomToolTip {

    /** Display object. */
    private Display display = null;

    /** Tool tip shell. */
    private Shell tipShell = null;

    /** Tool tip label. */
    private Label tipLabel = null;

    /** Tool tip text. */
    private String tipText = null;

    /** Font data for tool tip text. */
    private FontData tipFontData = null;

    /** Control that will "display" the tool tip. */
    private Control tipControl = null;

    /** Foreground color for the tool tip text. */
    private RGB tipForegroundClr = Display.getCurrent()
            .getSystemColor(SWT.COLOR_INFO_FOREGROUND).getRGB();

    /** Background color for the tool tip text. */
    private RGB tipBackgroundClr = Display.getCurrent()
            .getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();

    /** Width of the screen. */
    private int screenWidth = 0;

    /** Height of the screen. */
    private int screenHeight = 0;

    /** Rectangle bounds of the tip control. */
    private Rectangle tipControlRect = null;

    /**
     * Constructor.
     * 
     * @param tipControl
     *            Control associated with the tool tip.
     * @param tipText
     *            Text to be display in the tool tip.
     */
    public CustomToolTip(Control tipControl, String tipText) {
        this.display = tipControl.getDisplay();
        this.tipControl = tipControl;
        this.tipText = tipText;

        int screenRes = Toolkit.getDefaultToolkit().getScreenSize().width;

        tipFontData = new FontData("Monospace", 10, SWT.NORMAL);
        screenWidth = screenRes;
        screenHeight = this.display.getPrimaryMonitor().getBounds().height;

        setupMouseListeners();
        setupPaintListeners();
    }

    /**
     * Set the tool tip text.
     * 
     * @param tipText
     *            Text for the tool tip.
     */
    public void setText(String tipText) {
        this.tipText = tipText;
    }

    /**
     * Font data for the tool tip text.
     * 
     * @param tipFontData
     */
    public void setFontData(FontData tipFontData) {
        this.tipFontData = tipFontData;
    }

    /**
     * Set the foreground color of the tool tip text.
     * 
     * @param fgColor
     *            RGB color of the foreground.
     */
    public void setForegroundColor(RGB fgColor) {
        this.tipForegroundClr = fgColor;
    }

    /**
     * Set the background color of the tool tip text.
     * 
     * @param bgColor
     *            RGB color of the background.
     */
    public void setBackgroundColor(RGB bgColor) {
        this.tipBackgroundClr = bgColor;
    }

    /**
     * Set up the mouse listeners for the mouse hovering on the tip control and
     * exiting out of the tip control.
     */
    private void setupMouseListeners() {

        tipControl.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (tipShell == null) {
                    return;
                }

                if (tipControlRect != null
                        && tipControlRect.contains(Display.getCurrent()
                                .getCursorLocation())) {

                    tipShell.dispose();
                    tipShell = null;
                    tipLabel = null;
                    tipControlRect = null;
                }
            }
        });

        tipControl.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                forceHideToolTip();
            }

            @Override
            public void mouseHover(MouseEvent e) {
                // If there is no text then don't display the tool tip.
                if (tipText == null || tipText.length() == 0) {
                    return;
                }

                if (tipControl != null) {
                    if (tipShell != null && !tipShell.isDisposed()) {
                        tipShell.dispose();
                    }

                    // Make a rectangle of the bounds of the tip control.
                    // This needs to be done every time in case the dialog
                    // has been moved.
                    Rectangle tmpRect = tipControl.getBounds();
                    Point pt = tipControl.toDisplay(0, 0);

                    tipControlRect = new Rectangle(pt.x, pt.y, tmpRect.width,
                            tmpRect.height);

                    // Create the fonts and colors.
                    Font textFont = new Font(display, tipFontData);
                    Color bgClr = new Color(display, tipBackgroundClr);
                    Color fgClr = new Color(display, tipForegroundClr);

                    /*
                     * Create the tip shell and the label that will display the
                     * information.
                     */
                    tipShell = new Shell(tipControl.getShell(), SWT.ON_TOP
                            | SWT.NO_FOCUS | SWT.TOOL);
                    tipShell.setBackground(bgClr);
                    FillLayout layout = new FillLayout();
                    layout.marginWidth = 2;
                    tipShell.setLayout(layout);
                    tipLabel = new Label(tipShell, SWT.NONE);
                    tipLabel.setFont(textFont);
                    tipLabel.setForeground(fgClr);
                    tipLabel.setBackground(bgClr);
                    tipLabel.setText(tipText);
                    tipLabel.addMouseTrackListener(new MouseTrackAdapter() {
                        @Override
                        public void mouseExit(MouseEvent e) {
                            tipShell.dispose();
                        }
                    });

                    Point size = tipShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    Rectangle rect = tipControl.getBounds();

                    Point coord = display.map(tipControl.getParent(), null,
                            rect.x, rect.y + tipControl.getSize().y);

                    if ((coord.y + size.y) > screenHeight) {
                        coord.y = coord.y - ((coord.y + size.y) - screenHeight);
                    }

                    if ((coord.x + size.x) > screenWidth) {
                        coord.x = coord.x - ((coord.x + size.x) - screenWidth);
                    }

                    if (coord.x < 0) {
                        coord.x = 0;
                    }

                    tipShell.setBounds(coord.x, coord.y, size.x, size.y);
                    tipShell.setVisible(true);
                    textFont.dispose();
                    bgClr.dispose();
                    fgClr.dispose();
                }
            }
        });
    }

    /**
     * Convenience method to hide (read: dispose) of the tool tip for the
     * situations when a dialog pops up when the mouse is over the controls that
     * has this tool tip.
     */
    public void forceHideToolTip() {
        if (tipShell == null) {
            return;
        }

        if (tipControlRect != null
                && tipControlRect.contains(Display.getCurrent()
                        .getCursorLocation())) {

            tipShell.dispose();
            tipShell = null;
            tipLabel = null;
            tipControlRect = null;
        }
    }

    /**
     * Setup the paint listener for the tip control.
     */
    private void setupPaintListeners() {
        tipControl.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (tipShell != null && tipShell.isDisposed() == false) {
                    tipLabel.setText(tipText);
                    tipShell.pack();
                }
            }
        });
    }
}
