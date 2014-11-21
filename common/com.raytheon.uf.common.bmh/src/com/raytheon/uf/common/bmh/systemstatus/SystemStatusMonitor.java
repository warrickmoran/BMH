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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raytheon.uf.common.bmh.notify.status.BmhEdexStatus;
import com.raytheon.uf.common.bmh.notify.status.CommsManagerStatus;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.status.PeriodicStatusMessage;
import com.raytheon.uf.common.bmh.notify.status.TTSStatus;
import com.raytheon.uf.common.bmh.systemstatus.ISystemStatusListener.BmhComponent;
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

    private transient Set<ISystemStatusListener> listeners = new CopyOnWriteArraySet<>();

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

    /**
     * @return the status of the dac for all currently connected transmitter
     *         groups. It is possible to know that a transmitter group is
     *         connected but still have not have a
     *         {@link DacHardwareStatusNotification}, in this case the entry in
     *         the map for that group will have a null value.
     */
    public Map<String, DacHardwareStatusNotification> getDacStatus() {
        Set<String> connectedGroups = getConnectedTransmitterGroups();
        synchronized (this.dacStatus) {
            Map<String, DacHardwareStatusNotification> dacStatus = new HashMap<>(
                    connectedGroups.size(), 1.0f);
            for (String group : connectedGroups) {
                dacStatus.put(group, this.dacStatus.get(group));
            }
            return dacStatus;
        }
    }

    public void setDacStatus(
            Map<String, DacHardwareStatusNotification> dacStatus) {
        this.dacStatus = new HashMap<>(dacStatus);
    }

    /**
     * @return a map of hostname to the current status of the comms manager
     *         running on that host.
     */
    public Map<String, CommsManagerStatus> getCommsStatus() {
        Map<String, CommsManagerStatus> commsStatus;
        synchronized (this.commsStatus) {
            commsStatus = new HashMap<>(this.commsStatus);
        }
        removeExpired(commsStatus.values());
        return commsStatus;
    }

    public void setCommsStatus(Map<String, CommsManagerStatus> commsStatus) {
        this.commsStatus = new HashMap<>(commsStatus);
    }

    /**
     * @return a map of hostname to the current status of the edex running on
     *         that host.
     */
    public Map<String, BmhEdexStatus> getEdexStatus() {
        Map<String, BmhEdexStatus> edexStatus;
        synchronized (this.edexStatus) {
            edexStatus = new HashMap<>(this.edexStatus);
        }
        removeExpired(edexStatus.values());
        return edexStatus;
    }

    public void setEdexStatus(Map<String, BmhEdexStatus> edexStatus) {
        this.edexStatus = new HashMap<>(edexStatus);
    }

    /**
     * @return a map of hostname to the current status of the tts connection for
     *         the edex on that host.
     */
    public Map<String, TTSStatus> getTtsStatus() {
        Collection<String> connectedEdices = getEdexStatus().keySet();
        synchronized (this.ttsStatus) {
            Map<String, TTSStatus> ttsStatus = new HashMap<>(
                    connectedEdices.size(), 1.0f);
            for (String group : connectedEdices) {
                dacStatus.put(group, this.dacStatus.get(group));
            }
            return ttsStatus;
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

    protected void updateDacHardwareStatus(
            DacHardwareStatusNotification dacStatus) {
        DacHardwareStatusNotification prev = null;
        synchronized (this.dacStatus) {
            prev = this.dacStatus.put(dacStatus.getTransmitterGroup(),
                    dacStatus);
        }
        if (prev == null || !prev.equals(dacStatus)) {
            notifyListeners(BmhComponent.DAC, dacStatus.getTransmitterGroup());
        }
    }

    protected void updateEdexStatus(BmhEdexStatus edexStatus) {
        BmhEdexStatus prev = null;
        synchronized (this.edexStatus) {
            prev = this.edexStatus.put(edexStatus.getHost(), edexStatus);
        }
        if (prev == null || !prev.equals(edexStatus)) {
            notifyListeners(BmhComponent.EDEX, edexStatus.getHost());
        }
    }

    protected void updateCommsManagerStatus(CommsManagerStatus commsStatus) {
        CommsManagerStatus prev = null;
        synchronized (this.commsStatus) {
            prev = this.commsStatus.put(commsStatus.getHost(), commsStatus);
        }
        if (prev == null || !prev.equals(commsStatus)) {
            notifyListeners(BmhComponent.CommsManager, commsStatus.getHost());
            // TODO might need to notify of new dacs.
        }
    }

    protected void updateTtsStatus(TTSStatus ttsStatus) {
        TTSStatus prev = null;
        synchronized (this.ttsStatus) {
            prev = this.ttsStatus.put(ttsStatus.getHost(), ttsStatus);
        }
        if (prev == null || !prev.equals(ttsStatus)) {
            notifyListeners(BmhComponent.TTS, ttsStatus.getHost());
        }
    }

    /**
     * Get all the transmitter groups that are running on dacs that are
     * currently communicating with a comms manager.
     * 
     * @return
     */
    public Set<String> getConnectedTransmitterGroups() {
        Set<String> connectedGroups = new HashSet<>();
        for (CommsManagerStatus commsStatus : this.getCommsStatus().values()) {
            Set<String> commsGroups = commsStatus
                    .getConnectedTransmitterGroups();
            if (commsGroups != null) {
                connectedGroups.addAll(commsGroups);
            }
        }
        return connectedGroups;
    }

    public DacHardwareStatusNotification getDacHardwareStatus(
            String transmitterGroupName) {
        return getDacStatus().get(transmitterGroupName);
    }

    public Collection<CommsManagerStatus> getConnectedCommsManagers() {
        return getCommsStatus().values();
    }

    public Collection<BmhEdexStatus> getConnectedEdices() {
        return getEdexStatus().values();
    }

    public Collection<TTSStatus> getConnectedTtsHosts() {
        return getTtsStatus().values();
    }

    public boolean isTransmitterGroupConnected(String transmitterGroupName) {
        return getDacStatus().containsKey(transmitterGroupName);
    }

    public boolean isCommsManagerConnected(String host) {
        return getCommsStatus().containsKey(host);
    }

    public boolean isEdexConnected(String host) {
        return getEdexStatus().containsKey(host);
    }

    public boolean isTtsConnected(String host) {
        return getTtsStatus().containsKey(host);
    }

    private void notifyListeners(BmhComponent component, String key) {
        for (ISystemStatusListener listener : listeners) {
            listener.systemStatusChanged(component, key);
        }
    }

    public void addListener(ISystemStatusListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ISystemStatusListener listener) {
        listeners.remove(listener);
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

}
