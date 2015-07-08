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
package com.raytheon.uf.edex.bmh.dactransmit.ipc;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message sent from comms manager to dac transmit to indicate the decibel
 * target has been altered for a transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014 3532       bkowal      Initial creation
 * Sep 4, 2014  3532       bkowal      Change to support a single decibel target
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ChangeDecibelTarget {

    /*
     * The new decibel range to use.
     */
    @DynamicSerializeElement
    private double audioDbTarget;

    @DynamicSerializeElement
    private double sameDbTarget;

    @DynamicSerializeElement
    private double alertDbTarget;

    /**
     * Constructor.
     */
    public ChangeDecibelTarget() {
    }

    public ChangeDecibelTarget(double audioDbTarget, double sameDbTarget,
            double alertDbTarget) {
        this.audioDbTarget = audioDbTarget;
        this.sameDbTarget = sameDbTarget;
        this.alertDbTarget = alertDbTarget;
    }

    /**
     * @return the dbTarget
     */
    public double getAudioDbTarget() {
        return audioDbTarget;
    }

    /**
     * @param audioDbTarget
     *            the dbTarget to set
     */
    public void setAudioDbTarget(double audioDbTarget) {
        this.audioDbTarget = audioDbTarget;
    }

    /**
     * @return the sameDbTarget
     */
    public double getSameDbTarget() {
        return sameDbTarget;
    }

    /**
     * @param sameDbTarget
     *            the sameDbTarget to set
     */
    public void setSameDbTarget(double sameDbTarget) {
        this.sameDbTarget = sameDbTarget;
    }

    /**
     * @return the alertDbTarget
     */
    public double getAlertDbTarget() {
        return alertDbTarget;
    }

    /**
     * @param alertDbTarget
     *            the alertDbTarget to set
     */
    public void setAlertDbTarget(double alertDbTarget) {
        this.alertDbTarget = alertDbTarget;
    }
}