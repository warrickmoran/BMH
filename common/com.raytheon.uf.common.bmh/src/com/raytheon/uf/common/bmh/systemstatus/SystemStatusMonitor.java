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
package com.raytheon.uf.common.bmh.systemstatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.raytheon.uf.common.bmh.notify.status.BmhEdexStatus;
import com.raytheon.uf.common.bmh.notify.status.CommsManagerStatus;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.PeriodicStatusMessage;
import com.raytheon.uf.common.bmh.notify.status.TTSStatus;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Object to hold and monitor the status of various BMH components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 18, 2014  3817     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class SystemStatusMonitor {

    private static int PERIODIC_TIMEOUT_MS = 60 * 1000;

    @DynamicSerializeElement
    private Map<String, DacHardwareStatusNotification> dacStatus;

    @DynamicSerializeElement
    private Map<String, CommsManagerStatus> commsStatus;

    @DynamicSerializeElement
    private Map<String, BmhEdexStatus> edexStatus;

    @DynamicSerializeElement
    private Map<String, TTSStatus> ttsStatus;

    public SystemStatusMonitor() {
        this(null);
    }

    public SystemStatusMonitor(SystemStatusMonitor other) {
        if (other == null) {
            dacStatus = new HashMap<>();
            commsStatus = new HashMap<>(4);
            edexStatus = new HashMap<>(4);
            ttsStatus = new HashMap<>(4);
        } else {
            dacStatus = new HashMap<>(other.getDacStatus());
            commsStatus = new HashMap<>(other.getCommsStatus());
            edexStatus = new HashMap<>(other.getEdexStatus());
            ttsStatus = new HashMap<>(other.getTtsStatus());
        }
    }

    public Map<String, DacHardwareStatusNotification> getDacStatus() {
        synchronized (dacStatus) {
            return new HashMap<>(dacStatus);
        }
    }

    public void setDacStatus(
            Map<String, DacHardwareStatusNotification> dacStatus) {
        this.dacStatus = new HashMap<>(dacStatus);
    }

    public Map<String, CommsManagerStatus> getCommsStatus() {
        synchronized (commsStatus) {
            return new HashMap<>(commsStatus);
        }
    }

    public void setCommsStatus(Map<String, CommsManagerStatus> commsStatus) {
        this.commsStatus = new HashMap<>(commsStatus);
    }

    public Map<String, BmhEdexStatus> getEdexStatus() {
        synchronized (edexStatus) {
            return new HashMap<>(edexStatus);
        }
    }

    public void setEdexStatus(Map<String, BmhEdexStatus> edexStatus) {
        this.edexStatus = new HashMap<>(edexStatus);
    }

    public Map<String, TTSStatus> getTtsStatus() {
        synchronized (ttsStatus) {
            return new HashMap<>(ttsStatus);
        }
    }

    public void setTtsStatus(Map<String, TTSStatus> ttsStatus) {
        this.ttsStatus = new HashMap<>(ttsStatus);
    }

    public void handleStatusMessage(Object statusMessage) {
        if (statusMessage instanceof DacHardwareStatusNotification) {
            updateDacHardwareStatus((DacHardwareStatusNotification) statusMessage);
        } else if (statusMessage instanceof BmhEdexStatus) {
            updateEdexStatus((BmhEdexStatus) statusMessage);
        } else if (statusMessage instanceof CommsManagerStatus) {
            updateCommsManagerStatus((CommsManagerStatus) statusMessage);
        } else if (statusMessage instanceof TTSStatus) {
            updateTtsStatus((TTSStatus) statusMessage);
        }
    }

    public void updateDacHardwareStatus(DacHardwareStatusNotification dacStatus) {
        synchronized (this.dacStatus) {
            this.dacStatus.put(dacStatus.getTransmitterGroup(), dacStatus);
        }
    }

    public void updateEdexStatus(BmhEdexStatus edexStatus) {
        synchronized (this.edexStatus) {
            this.edexStatus.put(edexStatus.getHost(), edexStatus);
        }
    }

    private void updateCommsManagerStatus(CommsManagerStatus commsStatus) {
        synchronized (this.commsStatus) {
            this.commsStatus.put(commsStatus.getHost(), commsStatus);
        }
    }

    private void updateTtsStatus(TTSStatus ttsStatus) {
        synchronized (this.ttsStatus) {
            this.ttsStatus.put(ttsStatus.getHost(), ttsStatus);
        }
    }

    private boolean isGroupConnectedtoComms(String transmitterGroupName) {
        synchronized (this.commsStatus) {
            for (CommsManagerStatus commsStatus : this.commsStatus.values()) {
                if (commsStatus
                        .containsConnectedTransmitterGroup(transmitterGroupName)) {
                    if (isExpired(commsStatus)) {
                        this.commsStatus.remove(commsStatus.getHost());
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public DacHardwareStatusNotification getDacHardwareStatus(
            String transmitterGroupName) {
        if (isGroupConnectedtoComms(transmitterGroupName)) {
            synchronized (dacStatus) {
                return dacStatus.get(transmitterGroupName);
            }
        } else {
            return null;
        }
    }

    public Collection<CommsManagerStatus> getConnectedCommsManagers() {
        synchronized (commsStatus) {
            removeExpired(commsStatus.values());
            return new HashSet<>(commsStatus.values());
        }
    }

    public Collection<BmhEdexStatus> getConnectedEdices() {
        synchronized (edexStatus) {
            removeExpired(edexStatus.values());
            return new HashSet<>(edexStatus.values());
        }
    }

    public boolean isDacConnected(String transmitterGroupName) {
        synchronized (dacStatus) {
            return dacStatus.containsKey(transmitterGroupName);
        }
    }

    public boolean isCommsManagerConnected(String host) {
        synchronized (commsStatus) {
            return commsStatus.containsKey(host);
        }
    }

    public boolean isEdexConnected(String host) {
        synchronized (edexStatus) {
            return edexStatus.containsKey(host);
        }
    }

    public boolean isTtsConnected(String host) {
        if (isEdexConnected(host)) {
            synchronized (ttsStatus) {
                TTSStatus ttsStatus = this.ttsStatus.get(host);
                if (ttsStatus != null) {
                    return ttsStatus.isConnected();
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static void removeExpired(
            Collection<? extends PeriodicStatusMessage> statusObjects) {
        Iterator<? extends PeriodicStatusMessage> it = statusObjects.iterator();
        while (it.hasNext()) {
            if (isExpired(it.next())) {
                it.remove();
            }
        }
    }

    private static boolean isExpired(PeriodicStatusMessage statusObject) {
        return statusObject.getStatusTime() + PERIODIC_TIMEOUT_MS < System
                .currentTimeMillis();
    }

    /*
     * TODO some sort of listener pattern.
     */
}
