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
package com.raytheon.uf.common.bmh.broadcast;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Abstract representation of a message used to trigger and report on on demand
 * broadcasts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2014 3655       bkowal      Initial creation
 * Nov 10, 2014 3630       bkowal      Re-factor to support on-demand broadcasting.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public abstract class AbstractOnDemandBroadcastMessage implements
        IOnDemandBroadcastMsg {

    @DynamicSerializeElement
    private MSGSOURCE msgSource;

    @DynamicSerializeElement
    private Boolean status;

    /**
     * TODO: need to use {@link TransmitterGroup}. Note: the GUIs also need to
     * be updated to reference the Transmitter Group instead of a Transmitter.
     */
    @DynamicSerializeElement
    private List<Transmitter> transmitterGroups;

    /**
     * 
     */
    public AbstractOnDemandBroadcastMessage() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage#getMsgSource()
     */
    @Override
    public MSGSOURCE getMsgSource() {
        return this.msgSource;
    }

    public void setMsgSource(MSGSOURCE msgSource) {
        this.msgSource = msgSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage#getStatus()
     */
    @Override
    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage#
     * getTransmitterGroups()
     */
    @Override
    public List<Transmitter> getTransmitterGroups() {
        return this.transmitterGroups;
    }

    public void setTransmitterGroups(List<Transmitter> transmitterGroups) {
        this.transmitterGroups = transmitterGroups;
    }

    public void addTransmitter(Transmitter transmitter) {
        if (this.transmitterGroups == null) {
            this.transmitterGroups = new ArrayList<>();
        }
        this.transmitterGroups.add(transmitter);
    }

    public void addAllTransmitter(Collection<Transmitter> transmitters) {
        if (this.transmitterGroups == null) {
            this.transmitterGroups = new ArrayList<>();
        }
        this.transmitterGroups.addAll(transmitters);
    }
}