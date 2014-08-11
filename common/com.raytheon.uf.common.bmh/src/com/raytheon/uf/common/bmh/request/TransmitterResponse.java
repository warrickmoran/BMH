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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link Transmitter} Response Object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014   3173     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterResponse {

    @DynamicSerializeElement
    private List<TransmitterGroup> transmitterGroupList;

    @DynamicSerializeElement
    private Transmitter transmitter;

    @DynamicSerializeElement
    private List<Transmitter> transmitterList;

    /**
     * @return the transmitterGroupList
     */
    public List<TransmitterGroup> getTransmitterGroupList() {
        return transmitterGroupList;
    }

    /**
     * @param transmitterGroupList
     *            the transmitterGroupList to set
     */
    public void setTransmitterGroupList(
            List<TransmitterGroup> transmitterGroupList) {
        this.transmitterGroupList = transmitterGroupList;
    }

    /**
     * @return the transmitter
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }

    /**
     * @param transmitter
     *            the transmitter to set
     */
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    /**
     * @return the transmitterList
     */
    public List<Transmitter> getTransmitterList() {
        return transmitterList;
    }

    /**
     * @param transmitterList
     *            the transmitterList to set
     */
    public void setTransmitterList(List<Transmitter> transmitterList) {
        this.transmitterList = transmitterList;
    }
}
