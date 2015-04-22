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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Further abstraction of a {@link AbstractBMHServerRequest} used to indicate
 * that a statistic should be generated for the associated configuration change.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 21, 2015 4397       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public abstract class AbstractBMHSystemConfigRequest extends
        AbstractBMHServerRequest {

    /**
     * Client generated key associated with the generated statistic.
     */
    @DynamicSerializeElement
    private String statisticKey;

    /**
     * Constructor
     */
    public AbstractBMHSystemConfigRequest() {
    }

    /**
     * Constructor
     * 
     * @param operational
     *            boolean flag indicating whether or not this is an operational
     *            request.
     */
    public AbstractBMHSystemConfigRequest(boolean operational) {
        super(operational);
    }

    /**
     * Returns true if the {@link AbstractBMHSystemConfigRequest} will actually
     * alter system configuration. Returns false otherwise.
     * 
     * @return a boolean indicating whether or not the
     *         {@link AbstractBMHSystemConfigRequest} will actually alter system
     *         configuration.
     */
    public abstract boolean isSystemConfigChange();

    /**
     * @return the statisticKey
     */
    public String getStatisticKey() {
        return statisticKey;
    }

    /**
     * @param statisticKey
     *            the statisticKey to set
     */
    public void setStatisticKey(String statisticKey) {
        this.statisticKey = statisticKey;
    }
}