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
package com.raytheon.uf.viz.bmh;

import com.raytheon.uf.common.bmh.jms.AbstractBMHJMSDestinations;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * JMS destination provider for BMH Viz components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHJmsDestinations extends AbstractBMHJMSDestinations {

    private static final BMHJmsDestinations instance = new BMHJmsDestinations();

    /**
     * 
     */
    protected BMHJmsDestinations() {
    }

    public static String getDacStatusDestination() {
        return instance
                .getDacStatusURI(CAVEMode.getMode() == CAVEMode.OPERATIONAL);
    }

    public static String getBMHConfigDestination() {
        return instance
                .getBMHConfigURI(CAVEMode.getMode() == CAVEMode.OPERATIONAL);
    }
}