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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.bmh.request.AbstractBMHSystemConfigRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Further abstraction of {@link ConfigNotification} used to indicate the
 * statistic associated with the {@link #statisticKey} should be finalized and
 * persisted.
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
public class AbstractTraceableSystemConfigNotification extends
        ConfigNotification {

    @DynamicSerializeElement
    private String statisticKey;

    /**
     * Empty constructor for {@link DynamicSerialize}.
     */
    public AbstractTraceableSystemConfigNotification() {
    }

    /**
     * @param type
     */
    public AbstractTraceableSystemConfigNotification(ConfigChangeType type,
            AbstractBMHSystemConfigRequest request) {
        super(type);
        this.statisticKey = request.getStatisticKey();
    }

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