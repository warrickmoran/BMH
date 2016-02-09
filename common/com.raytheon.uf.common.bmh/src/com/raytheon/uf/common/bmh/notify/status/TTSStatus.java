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
package com.raytheon.uf.common.bmh.notify.status;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Status message sent from edex to communicate the state of the tts connection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 20, 2014  3817     bsteffen    Initial creation
 * Jan 26, 2015  4020     bkowal      Added {@link #edexHost}.
 * Jan 27, 2015  4029     bkowal      Added {@link #equals(Object)}.
 * Feb 09, 2016  5082     bkowal      Updates for Apache commons lang 3.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class TTSStatus {

    /**
     * The EDEX host that is connected to and interacting with the TTS Server.
     */
    @DynamicSerializeElement
    private String edexHost;

    @DynamicSerializeElement
    private String host;

    @DynamicSerializeElement
    private boolean connected;

    public TTSStatus() {

    }

    public TTSStatus(String edexHost, String host, boolean connected) {
        this.edexHost = edexHost;
        this.host = host;
        this.connected = connected;
    }

    /**
     * @return the edexHost
     */
    public String getEdexHost() {
        return edexHost;
    }

    /**
     * @param edexHost
     *            the edexHost to set
     */
    public void setEdexHost(String edexHost) {
        this.edexHost = edexHost;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TTSStatus other = (TTSStatus) obj;

        EqualsBuilder eq = new EqualsBuilder();
        eq.append(this.edexHost, other.edexHost);
        eq.append(this.host, other.host);
        eq.append(this.connected, other.connected);

        return eq.isEquals();
    }
}
