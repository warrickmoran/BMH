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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterMnemonicComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride.EmergencyOverrideDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionSaveData;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;

/**
 * 
 * Provides a widget and a variety of methods for dealing with the same
 * transmitters for a sepcifc message. The widget is a
 * {@link CheckScrollListComp} containing all possible transmitters, individual
 * check boxes are enabled/disabled according to the state of this object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Mar 16, 2015  4244     bsteffen     Initial creation
 * Mar 18, 2015  4282     rferrel      Added method setAllowEnableTransmitters.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SAMETransmitterSelector {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EmergencyOverrideDlg.class);

    protected final CheckScrollListComp checks;

    protected final Map<String, Transmitter> transmitterMap = new HashMap<>();

    protected MessageType messageType;

    protected AreaSelectionSaveData areaData;

    protected boolean interrupt;

    protected transient Set<Transmitter> transmittersWithMessageType;

    /**
     * Create a new selector, the widget is added to the provided composite and
     * an initial list of transmitters is generated.
     */
    public SAMETransmitterSelector(Composite parentComp,
            boolean showSelectControls, int width, int height) {
        checks = new CheckScrollListComp(parentComp, "SAME: ",
                createTransmitterListData(), showSelectControls, width, height);
    }

    /** Reset all state, this will disable the checkboxes. */
    public void reset() {
        this.areaData = null;
        this.interrupt = false;
        setMessageType(null);
    }

    /**
     * If false do not allow enabling of SAME transmitters.
     * 
     * @see CheckScrollListComp#setAllowEnable(boolean)
     * 
     * @param allowEnable
     */
    public void setAllowEnableTransmitters(boolean allowEnable) {
        checks.setAllowEnable(allowEnable);
    }

    /**
     * Set a message type. This sets the SAMETransmitters to
     * {@link MessageType#getSameTransmitters()}. If interrupt is false then the
     * message type is used to only allow same transmitters that have a program
     * containing the specified message type. This setter will update widget
     * state and must be called on the SWT UI thread.
     */
    public void setMessageType(MessageType messageType) {
        if (this.messageType != messageType) {
            this.messageType = messageType;
            transmittersWithMessageType = null;
            selectFromMessageType();
            refreshDisabled();
        }
    }

    /**
     * Set the area data. The area data is used to filter SAME transmitter
     * selection to only those transmitters within the area.
     */
    public void setAreaData(AreaSelectionSaveData areaData) {
        this.areaData = areaData;
        refreshDisabled();
    }

    /**
     * Set interrupt, when interrupt is true then SAME transmitters are not
     * filtered based off transmitters that have a program containing the
     * specified message type.
     */
    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
        refreshDisabled();
    }

    /**
     * Set which same transmitters should be selected.
     */
    public void setSAMETransmitters(Set<Transmitter> transmitters) {
        CheckListData cld = new CheckListData();
        for (Transmitter transmitter : transmitters) {
            cld.addDataItem(transmitter.getMnemonic(), true);
        }
        checks.selectCheckboxes(cld);
    }

    /**
     * Get the transmitters that SAME Tones should be sent to.
     */
    public Set<Transmitter> getSAMETransmitters() {
        return convertMnemonicsToTransmitters(getSAMETransmitterMnemonics());
    }

    /**
     * Get the mnemonics of the transmitters that SAME Tones should be sent to.
     */
    public Set<String> getSAMETransmitterMnemonics() {
        return new HashSet<String>(checks.getCheckedItems().getCheckedItems());
    }

    /**
     * @return true if no transmitters should be setn SAME tones.
     */
    public boolean isEmpty() {
        return getSAMETransmitterMnemonics().isEmpty();
    }

    /**
     * Get the transmitters which are affected by the current state of this
     * object. This is determined based off the area data, message type and
     * interrupt, and is not determined by user interaction. This determination
     * must be done internally to determine which check boxes to enable and is
     * made public for convenience.
     */
    public Set<Transmitter> getAffectedTransmitters() {
        /*
         * ensure that the user will only be able to select Transmitters for
         * SAME tone playback associated with the area that was selected.
         */
        Set<Transmitter> affected = getTransmittersInArea();
        if (!interrupt) {
            /* Not an interrupt, further limit selection by program */
            affected.retainAll(getTransmittersWithMessageType());
        }
        return affected;
    }

    /**
     * Get the mnemonics of the transmitters which are affected by the current
     * state of this object.
     * 
     * @see #getAffectedTransmitters()
     */
    public Set<String> getAffectedTransmitterMnemonics() {
        return convertTransmittersToMnemonics(getAffectedTransmitters());
    }

    /**
     * Return the status of the affected transmitters. This is provided for
     * consistent error messages throughout the UI. Internally this is used to
     * provide tool tips but the same messages may be used to display status to
     * the user. The only 2 components of the status that matter are the
     * severity and the message. The severity will be one of OK, WARNING, or
     * ERROR. OK indicates that all transmitters are available for sending same
     * tones. Warning indicates that some transmitters are disabled and the
     * message will provide details on why those transmitters cannot send SAME
     * tones. An error indicates that the current message type, area data, and
     * interrupt state are not applicable to any transmitters.
     */
    public IStatus getAffectedTransmitterStatus() {
        int severity;
        String message;

        Set<Transmitter> affectedTransmitters = getAffectedTransmitters();
        if (affectedTransmitters.size() == transmitterMap.size()) {
            severity = IStatus.OK;
            message = null;
        } else if (!affectedTransmitters.isEmpty()) {
            severity = IStatus.WARNING;
            if (interrupt) {
                message = "A disabled transmitter does not intersect with the selected area.";
            } else {
                message = "A disabled transmitter does not intersect with the selected area or its program list does not contain selected message type.";
            }
        } else {
            severity = IStatus.ERROR;
            if (messageType == null) {
                message = "No message type is selected.";
            } else if (interrupt) {
                message = "The selected area does not contain any transmitters.";
            } else if (getTransmittersWithMessageType().isEmpty()) {
                message = "Message Type [" + messageType.getAfosid()
                        + "] does not belong to any programs.";
            } else {
                message = "Message Type ["
                        + messageType.getAfosid()
                        + "] does not map to any program assigned to selected areas.";
            }
        }
        return new Status(severity, Activator.PLUGIN_ID, message);
    }

    protected void selectFromMessageType() {
        if (messageType == null) {
            checks.selectCheckboxes(false);
        } else {
            CheckListData cld = new CheckListData();
            Set<Transmitter> transSet = messageType.getSameTransmitters();
            if (transSet != null) {
                for (Transmitter t : transSet) {
                    cld.addDataItem(t.getMnemonic(), transSet.contains(t));
                }
            }
            checks.selectCheckboxes(cld);
        }
    }

    protected void refreshDisabled() {
        checks.setHelpText(getAffectedTransmitterStatus().getMessage());
        checks.enableCheckboxes(getAffectedTransmitterMnemonics());
    }

    protected Set<Transmitter> getTransmittersInArea() {
        if (areaData == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<>(areaData.getAffectedTransmitters());
        }
    }

    protected Set<Transmitter> getTransmittersWithMessageType() {
        Set<Transmitter> transmittersWithMessageType = this.transmittersWithMessageType;
        if (transmittersWithMessageType == null) {
            if (messageType == null) {
                transmittersWithMessageType = Collections.emptySet();
            } else {
                ProgramDataManager pdm = new ProgramDataManager();
                try {
                    List<Transmitter> transmitters = pdm
                            .getTransmittersForMsgType(messageType);
                    transmittersWithMessageType = new HashSet<>(transmitters);
                } catch (Exception e) {
                    statusHandler
                            .error("Error occurred retrieving valid transmitters for message type",
                                    e);
                }
            }
            this.transmittersWithMessageType = transmittersWithMessageType;
        }
        return new HashSet<>(transmittersWithMessageType);
    }

    protected CheckListData createTransmitterListData() {
        CheckListData cld = new CheckListData();

        TransmitterDataManager tdm = new TransmitterDataManager();
        try {
            List<Transmitter> transmitters = tdm.getTransmitters();
            if (transmitters == null) {
                return cld;
            }
            for (Transmitter t : transmitters) {
                cld.addDataItem(t.getMnemonic(), false);
                transmitterMap.put(t.getMnemonic(), t);
            }
            Collections.sort(transmitters, new TransmitterMnemonicComparator());
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving transmitter data from the database: ", e);
        }
        return cld;
    }

    protected final Set<Transmitter> convertMnemonicsToTransmitters(
            Set<String> mnemonics) {
        Set<Transmitter> transmitters = new HashSet<Transmitter>(
                mnemonics.size(), 1.0f);
        for (String mnemonic : mnemonics) {
            if (transmitterMap.containsKey(mnemonic)) {
                transmitters.add(transmitterMap.get(mnemonic));
            }
        }
        return transmitters;

    }

    protected static Set<String> convertTransmittersToMnemonics(
            Set<Transmitter> transmitters) {
        Set<String> mnemonics = new HashSet<String>(transmitters.size(), 1.0f);
        for (Transmitter transmitter : transmitters) {
            mnemonics.add(transmitter.getMnemonic());
        }
        return mnemonics;
    }

}
