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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Metadata associated with a live broadcast message. It is never actually
 * written to disk; so, it does not include any of the XML annotations. It
 * exists to provide information to the {@link DefaultMessageLogger} so that it
 * will be able to log live broadcast message activity.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 1, 2015  4825       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class LiveBroadcastMessage implements ITraceable {

    @DynamicSerializeElement
    private String user;

    @DynamicSerializeElement
    private String messageType;

    @DynamicSerializeElement
    private String SAMEtone;

    @DynamicSerializeElement
    protected String traceId;

    /**
     * 
     */
    public LiveBroadcastMessage() {
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     *            the messageType to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the sAMEtone
     */
    public String getSAMEtone() {
        return SAMEtone;
    }

    /**
     * @param sAMEtone
     *            the sAMEtone to set
     */
    public void setSAMEtone(String sAMEtone) {
        SAMEtone = sAMEtone;
    }

    @Override
    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}