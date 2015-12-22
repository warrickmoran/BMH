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
package com.raytheon.uf.common.bmh.stats;

import java.util.Collections;
import java.util.Map;

import com.raytheon.uf.common.stats.StatisticsEvent;

/**
 * Placeholder statistic record so that the statistic will be selectable in
 * CAVE.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2015 4686       bkowal      Initial creation
 * Dec 21, 2015 5218       rjpeter     Added toString
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DeliveryPercentFiller extends StatisticsEvent {

    private static final long serialVersionUID = 3916541707871173777L;

    private String summary;

    private double pctSuccess;

    /**
     * 
     */
    public DeliveryPercentFiller() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#getFieldUnitMap()
     */
    @Override
    protected Map<String, String> getFieldUnitMap() {
        return Collections.emptyMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#finalizeEvent()
     */
    @Override
    public void finalizeEvent() {
        // Do Nothing.
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the pctSuccess
     */
    public double getPctSuccess() {
        return pctSuccess;
    }

    /**
     * @param pctSuccess
     *            the pctSuccess to set
     */
    public void setPctSuccess(double pctSuccess) {
        this.pctSuccess = pctSuccess;
    }

    @Override
    public String toString() {
        return new StringBuilder("DeliveryPercentFiller [summary=")
                .append(summary).append(", pctSuccess=").append(pctSuccess)
                .append("]").toString();
    }

}