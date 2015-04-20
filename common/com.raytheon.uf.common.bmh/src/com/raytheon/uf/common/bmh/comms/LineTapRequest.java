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
package com.raytheon.uf.common.bmh.comms;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Request sent to the comms manager when opening a line tap socket to indicate
 * which group should be monitored.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 01, 2014  2487     bsteffen    Initial creation
 * Apr 14, 2015  4394     bkowal      Added {@link #dacReceiveAddress}, {@link #receivePort},
 *                                    and {@link #channels}. {@link #transmitterGroup} is just
 *                                    descriptive text now.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 * @see LineTapDisconnect
 */
@DynamicSerialize
public class LineTapRequest {

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private String dacReceiveAddress;

    @DynamicSerializeElement
    private int receivePort;

    @DynamicSerializeElement
    private int channel;

    public LineTapRequest() {
    }

    public LineTapRequest(String transmitterGroup, String dacReceiveAddress,
            int receivePort, int channel) {
        this.transmitterGroup = transmitterGroup;
        this.dacReceiveAddress = dacReceiveAddress;
        this.receivePort = receivePort;
        this.channel = channel;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the dacReceiveAddress
     */
    public String getDacReceiveAddress() {
        return dacReceiveAddress;
    }

    /**
     * @param dacReceiveAddress
     *            the dacReceiveAddress to set
     */
    public void setDacReceiveAddress(String dacReceiveAddress) {
        this.dacReceiveAddress = dacReceiveAddress;
    }

    /**
     * @return the receivePort
     */
    public int getReceivePort() {
        return receivePort;
    }

    /**
     * @param receivePort
     *            the receivePort to set
     */
    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    /**
     * @return the channels
     */
    public int getChannel() {
        return channel;
    }

    /**
     * @param channels
     *            the channels to set
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }

}
