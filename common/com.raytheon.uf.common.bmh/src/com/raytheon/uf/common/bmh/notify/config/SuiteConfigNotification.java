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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.AbstractBMHSystemConfigRequest;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Notification that is used when a {@link Suite} is created, updated or
 * deleted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 04, 2014  3554     bsteffen    Initial creation
 * Mar 25, 2015  4213     bkowal      Added {@link #associatedEnabledTransmitterGroups}.
 * Apr 22, 2015  4397     bkowal      Extend {@link AbstractTraceableSystemConfigNotification}.
 * May 13, 2015, 4429     rferrel     Implement {@link ITraceable}.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class SuiteConfigNotification extends
        AbstractTraceableSystemConfigNotification implements ITraceable {

    @DynamicSerializeElement
    private int id;

    @DynamicSerializeElement
    private transient String traceId;

    /**
     * Used to provide a {@link List} of the {@link TransmitterGroup}s that may
     * be affected by alterations to the {@link Suite}. Primarily used when a
     * {@link Suite} is deleted because the original relation will be completely
     * lost.
     */
    @DynamicSerializeElement
    private List<TransmitterGroup> associatedEnabledTransmitterGroups;

    public SuiteConfigNotification() {
        super();
    }

    public SuiteConfigNotification(ConfigChangeType type,
            AbstractBMHSystemConfigRequest request, Suite suite) {
        super(type, request);
        this.id = suite.getId();
    }

    public SuiteConfigNotification(ConfigChangeType type,
            AbstractBMHSystemConfigRequest request, Suite suite,
            List<TransmitterGroup> associatedEnabledTransmitterGroups) {
        this(type, request, suite);
        this.associatedEnabledTransmitterGroups = associatedEnabledTransmitterGroups;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the associatedEnabledTransmitterGroups
     */
    public List<TransmitterGroup> getAssociatedEnabledTransmitterGroups() {
        return associatedEnabledTransmitterGroups;
    }

    /**
     * @param associatedEnabledTransmitterGroups
     *            the associatedEnabledTransmitterGroups to set
     */
    public void setAssociatedEnabledTransmitterGroups(
            List<TransmitterGroup> associatedEnabledTransmitterGroups) {
        this.associatedEnabledTransmitterGroups = associatedEnabledTransmitterGroups;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

}
