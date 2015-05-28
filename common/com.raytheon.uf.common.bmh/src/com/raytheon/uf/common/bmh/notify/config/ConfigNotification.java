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

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Base class for sending notification of bmh configuration changes. Specific
 * types of changes will subclass this.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 18, 2014  3486     bsteffen    Initial creation
 * May 28, 2015  4429     rjpeter     Implement ITraceable
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public abstract class ConfigNotification implements ITraceable {

    public static enum ConfigChangeType {
        /* Update is used for creation or changes */
        Update,

        Delete;
    }

    @DynamicSerializeElement
    private ConfigChangeType type;

    @DynamicSerializeElement
    private String traceId;

    public ConfigNotification() {
    }

    public ConfigNotification(ConfigChangeType type, ITraceable traceable) {
        this.type = type;
        this.traceId = traceable.getTraceId();
    }

    public ConfigChangeType getType() {
        return type;
    }

    public void setType(ConfigChangeType type) {
        this.type = type;
    }

    /**
     * @return the traceId
     */
    @Override
    public String getTraceId() {
        return traceId;
    }

    /**
     * @param traceId
     *            the traceId to set
     */
    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

}
