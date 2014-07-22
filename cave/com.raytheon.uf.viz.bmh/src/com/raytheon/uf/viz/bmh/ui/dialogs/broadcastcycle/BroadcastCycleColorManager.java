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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

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

    // TODO Pass in the shell of the dialog calling this and add dispose
    // listener, then get rid of finalize method
    public BroadcastCycleColorManager(Display display) {
        actualTransmitTimeColor = new Color(display, 154, 205, 50);
        interruptColor = new Color(display, 255, 0, 0);
        replaceColor = new Color(display, 0, 191, 255);
        predictedTransmitTimeColor = new Color(display, 240, 230, 140);
        periodicColor = new Color(display, 128, 128, 0);
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    public void dispose() {
        this.actualTransmitTimeColor.dispose();
        this.interruptColor.dispose();
        this.periodicColor.dispose();
        this.predictedTransmitTimeColor.dispose();
        this.replaceColor.dispose();
    }
}
