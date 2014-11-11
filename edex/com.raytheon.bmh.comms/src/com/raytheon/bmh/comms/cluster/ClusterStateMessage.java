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
package com.raytheon.bmh.comms.cluster;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message used to send a list of connected dacs between comms manager cluster
 * members.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 24, 2014  3485     bsteffen    Initial creation
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class ClusterStateMessage {

    @DynamicSerialize
    public static class ClusterDacTransmitKey {

        @DynamicSerializeElement
        private String inputDirectory;

        @DynamicSerializeElement
        private int dataPort;

        @DynamicSerializeElement
        private String dacAddress;

        public ClusterDacTransmitKey() {

        }

        public ClusterDacTransmitKey(DacTransmitKey key) {
            this.inputDirectory = key.getInputDirectory();
            this.dacAddress = key.getDacAddress();
            this.dataPort = key.getDataPort();
        }

        public String getInputDirectory() {
            return inputDirectory;
        }

        public void setInputDirectory(String inputDirectory) {
            this.inputDirectory = inputDirectory;
        }

        public int getDataPort() {
            return dataPort;
        }

        public void setDataPort(int dataPort) {
            this.dataPort = dataPort;
        }

        public String getDacAddress() {
            return dacAddress;
        }

        public void setDacAddress(String dacAddress) {
            this.dacAddress = dacAddress;
        }

        public DacTransmitKey toKey() {
            return new DacTransmitKey(inputDirectory, dataPort, dacAddress);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((dacAddress == null) ? 0 : dacAddress.hashCode());
            result = prime * result + dataPort;
            result = prime
                    * result
                    + ((inputDirectory == null) ? 0 : inputDirectory.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClusterDacTransmitKey other = (ClusterDacTransmitKey) obj;
            if (dacAddress == null) {
                if (other.dacAddress != null)
                    return false;
            } else if (!dacAddress.equals(other.dacAddress))
                return false;
            if (dataPort != other.dataPort)
                return false;
            if (inputDirectory == null) {
                if (other.inputDirectory != null)
                    return false;
            } else if (!inputDirectory.equals(other.inputDirectory))
                return false;
            return true;
        }

    }

    @DynamicSerializeElement
    private List<ClusterDacTransmitKey> keys = new ArrayList<>();

    @DynamicSerializeElement
    private List<ClusterDacTransmitKey> requestedKeys = new ArrayList<>();

    public List<ClusterDacTransmitKey> getKeys() {
        return keys;
    }

    public void setKeys(List<ClusterDacTransmitKey> keys) {
        this.keys = keys;
    }

    public void add(DacTransmitKey key) {
        if (keys == null) {
            keys = new ArrayList<>();
        }
        keys.add(new ClusterDacTransmitKey(key));
    }

    public void remove(DacTransmitKey key) {
        if (keys == null) {
            return;
        }
        keys.remove(new ClusterDacTransmitKey(key));
    }

    public boolean contains(DacTransmitKey key) {
        return contains(new ClusterDacTransmitKey(key));
    }

    public boolean contains(ClusterDacTransmitKey key) {
        if (keys == null) {
            return false;
        }
        return keys.contains(key);
    }

    public List<ClusterDacTransmitKey> getRequestedKeys() {
        return requestedKeys;
    }

    public void setRequestedKeys(List<ClusterDacTransmitKey> requestedKeys) {
        this.requestedKeys = requestedKeys;
    }

    public void addRequest(DacTransmitKey key) {
        if (requestedKeys == null) {
            requestedKeys = new ArrayList<>();
        }
        requestedKeys.add(new ClusterDacTransmitKey(key));
    }

    public void removeRequest(DacTransmitKey key) {
        if (requestedKeys == null) {
            return;
        }
        requestedKeys.remove(new ClusterDacTransmitKey(key));
    }

    public boolean containsRequest(DacTransmitKey key) {
        return containsRequest(new ClusterDacTransmitKey(key));
    }

    public boolean containsRequest(ClusterDacTransmitKey key) {
        if (requestedKeys == null) {
            return false;
        }
        return requestedKeys.contains(key);
    }

}
