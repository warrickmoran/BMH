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

import com.raytheon.uf.common.auth.req.AbstractPrivilegedRequest;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * 
 * Adds an operational flag to {@link IServerRequest}s so that they can be
 * executed in test/practice mode where necessary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 07, 2014  3687     bsteffen    Initial creation
 * Oct 13, 2014  3413     rferrel     Made AbstractPrivilegedRequest.
 * May 28, 2015  4429     rjpeter     Implement ITraceable
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public abstract class AbstractBMHServerRequest extends
        AbstractPrivilegedRequest implements ITraceable {

    @DynamicSerializeElement
    private boolean operational;

    @DynamicSerializeElement
    private String traceId;

    protected AbstractBMHServerRequest() {

    }

    protected AbstractBMHServerRequest(boolean operational) {
        this.operational = operational;
    }

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
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
