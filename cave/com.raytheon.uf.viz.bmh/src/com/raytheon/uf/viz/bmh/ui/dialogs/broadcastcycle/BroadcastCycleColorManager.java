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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Color manager for the Broadcast Cycle Dialog. The creator of this class is
 * responsible for disposing this class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014   3432     mpduff      Initial creation
 * Oct 05, 2014   3647     mpduff      Add dispose listener to provided shell
 * Oct 21, 2014   3655     bkowal      Added a color to signify live broadcasts.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleColorManager {

    private final Color actualTransmitTimeColor;

    private final Color interruptColor;

    private final Color replaceColor;

    private final Color predictedTransmitTimeColor;

    private final Color periodicColor;

    private final Color liveBroadcastColor;

    public BroadcastCycleColorManager(Shell shell) {
        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        Display display = shell.getDisplay();
        actualTransmitTimeColor = new Color(display, 154, 205, 50);
        interruptColor = new Color(display, 255, 0, 0);
        replaceColor = new Color(display, 0, 191, 255);
        predictedTransmitTimeColor = new Color(display, 240, 230, 140);
        periodicColor = new Color(display, 128, 128, 0);
        liveBroadcastColor = new Color(display, 255, 165, 0);
    }

    /**
     * @return the actualTransmitTimeColor
     */
    public Color getActualTransmitTimeColor() {
        return actualTransmitTimeColor;
    }

    /**
     * @return the interruptColor
     */
    public Color getInterruptColor() {
        return interruptColor;
    }

    /**
     * @return the replaceColor
     */
    public Color getReplaceColor() {
        return replaceColor;
    }

    /**
     * @return the predictedTransmitTimeColor
     */
    public Color getPredictedTransmitTimeColor() {
        return predictedTransmitTimeColor;
    }

    /**
     * @return the periodicColor
     */
    public Color getPeriodicColor() {
        return periodicColor;
    }

    /**
     * @return the liveBroadcastColor
     */
    public Color getLiveBroadcastColor() {
        return liveBroadcastColor;
    }

    private void dispose() {
        if (actualTransmitTimeColor != null
                && !actualTransmitTimeColor.isDisposed()) {
            this.actualTransmitTimeColor.dispose();
        }
        if (interruptColor != null && !interruptColor.isDisposed()) {
            this.interruptColor.dispose();
        }
        if (periodicColor != null && !periodicColor.isDisposed()) {
            this.periodicColor.dispose();
        }
        if (predictedTransmitTimeColor != null
                && !predictedTransmitTimeColor.isDisposed()) {
            this.predictedTransmitTimeColor.dispose();
        }
        if (replaceColor != null && !replaceColor.isDisposed()) {
            this.replaceColor.dispose();
        }
        if (this.liveBroadcastColor != null
                && this.liveBroadcastColor.isDisposed() == false) {
            this.liveBroadcastColor.dispose();
        }
    }
}
