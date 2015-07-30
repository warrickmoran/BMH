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
package com.raytheon.uf.edex.bmh.stats;

/**
 * Generic POJO to store information retrieved by the message delivery
 * procedure.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2015 4686       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DeliveryStats {

    private final double expectedCount;

    private final double actualCount;

    private final double percentage;

    /**
     * 
     */
    public DeliveryStats(double expectedCount, double actualCount,
            double percentage) {
        this.expectedCount = expectedCount;
        this.actualCount = actualCount * 100;
        this.percentage = percentage;
    }

    /**
     * @return the expectedCount
     */
    public double getExpectedCount() {
        return expectedCount;
    }

    /**
     * @return the actualCount
     */
    public double getActualCount() {
        return actualCount;
    }

    /**
     * @return the percentage
     */
    public double getPercentage() {
        return percentage;
    }
}