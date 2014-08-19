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
 * Message sent from comms manager to dac transmit to indicate a different
 * decibel range.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014 3532       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ChangeDecibelRange {

    /*
     * The new decibel range to use.
     */
    @DynamicSerializeElement
    private double dbMin;

    @DynamicSerializeElement
    private double dbMax;

    /**
     * Constructor.
     */
    public ChangeDecibelRange() {
    }

    public ChangeDecibelRange(double dbMin, double dbMax) {
        this.dbMin = dbMin;
        this.dbMax = dbMax;
    }

    /**
     * @return the dbMin
     */
    public double getDbMin() {
        return dbMin;
    }

    /**
     * @param dbMin
     *            the dbMin to set
     */
    public void setDbMin(double dbMin) {
        this.dbMin = dbMin;
    }

    /**
     * @return the dbMax
     */
    public double getDbMax() {
        return dbMax;
    }

    /**
     * @param dbMax
     *            the dbMax to set
     */
    public void setDbMax(double dbMax) {
        this.dbMax = dbMax;
    }
}