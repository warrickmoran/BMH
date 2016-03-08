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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
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
 * transmitters for a specific message. The widget is a
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
 * Apr 07, 2014  4304     rferrel      Added {@link #getAffectedTransmitters(boolean)}.
 * May 04, 2015  4447     bkowal       Added {@link #overrideMessageTypeSAME(Set)}.
 * Jan 05, 2016  4997     bkowal       Allow toggling between transmitters/groups.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SAMETransmitterSelector {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EmergencyOverrideDlg.class);

    private final TransmitterDataManager tdm = new TransmitterDataManager();

    protected Button groupToggleBtn;

    protected final CheckScrollListComp checks;

    protected final Map<String, Transmitter> transmitterMap = new HashMap<>();

    protected CheckListData transmitterCLD;

    protected final Map<String, TransmitterGroup> transmitterGroupMap = new HashMap<>();

    protected CheckListData transmitterGroupCLD;

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
        final Composite sameComposite = new Composite(parentComp, SWT.NONE);
        sameComposite.setLayout(new GridLayout(1, false));

        this.buildTransmitterGroupListData();
        this.groupToggleBtn = new Button(sameComposite, SWT.CHECK);
        this.groupToggleBtn.setText("Transmitter Group");
        this.groupToggleBtn.setSelection(true);
        this.groupToggleBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleGroupsTransmitters();
            }
        });
        checks = new CheckScrollListComp(sameComposite, "SAME: ",
                this.transmitterGroupCLD, showSelectControls, width, height);
    }

    private void toggleGroupsTransmitters() {
        boolean groupsSelected = this.groupToggleBtn.getSelection();
        CheckListData replacementCLD = (groupsSelected) ? this.transmitterGroupCLD
                : this.transmitterCLD;

        /*
         * Determine which checkboxes should be checked when switching back and
         * forth based on what is currently selected.
         */
        CheckListData currentlyCheckedItems = this.checks.getCheckedItems();
        CheckListData itemsToCheck = new CheckListData();
        if (groupsSelected) {
            /*
             * currently selected items are transmitters.
             */
            for (String checked : currentlyCheckedItems.getCheckedItems()) {
                Transmitter t = this.transmitterMap.get(checked);
                if (t == null) {
                    continue;
                }
                itemsToCheck.addDataItem(t.getTransmitterGroup().getName(),
                        true);
            }
        } else {
            /*
             * currently selected items are transmitter groups. Note: if the
             * user intentionally toggles back and forth between transmitter and
             * transmitter groups unnecessarily, all transmitters associated
             * with a group will be checked even if a subset of the transmitters
             * were only originally checked before the toggle back and forth.
             */
            for (String checked : currentlyCheckedItems.getCheckedItems()) {
                TransmitterGroup tg = this.transmitterGroupMap.get(checked);
                if (tg == null
                        || CollectionUtils.isEmpty(tg.getTransmitterList())) {
                    continue;
                }
                for (Transmitter t : tg.getTransmitters()) {
                    itemsToCheck.addDataItem(t.getMnemonic(), true);
                }
            }
        }

        this.checks.replaceCheckboxes(replacementCLD);

        this.refreshDisabled();
        this.checks.selectCheckboxes(itemsToCheck);
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
        this.selectTransmitterCheckboxes(convertTransmittersToMnemonics(transmitters));
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
        List<String> checkedItems = checks.getCheckedItems().getCheckedItems();
        Set<String> trxMnemonics = null;
        if (this.groupToggleBtn.getSelection()) {
            trxMnemonics = new HashSet<>();
            for (String checked : checkedItems) {
                TransmitterGroup tg = this.transmitterGroupMap.get(checked);
                if (tg == null
                        || CollectionUtils.isEmpty(tg.getTransmitterList())) {
                    continue;
                }
                for (Transmitter t : tg.getTransmitters()) {
                    trxMnemonics.add(t.getMnemonic());
                }
            }
        } else {
            trxMnemonics = new HashSet<>(checkedItems);
        }

        return new HashSet<String>(trxMnemonics);
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
     * 
     * @return affectedTransmitters
     */
    public Set<Transmitter> getAffectedTransmitters() {
        return getAffectedTransmitters(false);
    }

    /**
     * Get the transmitters which are affected by the current state of this
     * object. This is determined based off the area data, message type and
     * interrupt, and is not determined by user interaction. This determination
     * must be done internally to determine which check boxes to enable and is
     * made public for convenience.
     * 
     * @param ignoreInterrupt
     *            - do not consider the interrupt state
     * @return affectedTransmitters
     */
    public Set<Transmitter> getAffectedTransmitters(boolean ignoreInterrupt) {
        /*
         * ensure that the user will only be able to select Transmitters for
         * SAME tone playback associated with the area that was selected.
         */
        Set<Transmitter> affected = getTransmittersInArea();
        if (ignoreInterrupt || !interrupt) {
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
            Set<Transmitter> transSet = messageType.getSameTransmitters();
            this.selectTransmitterCheckboxes(convertTransmittersToMnemonics(transSet));
        }
    }

    public void overrideMessageTypeSAME(Set<String> sameTransmitterSet) {
        this.checks.selectCheckboxes(false);
        if (CollectionUtils.isEmpty(sameTransmitterSet)) {
            return;
        }
        this.selectTransmitterCheckboxes(sameTransmitterSet);
    }

    /**
     * Select the Transmitter or Transmitter Group checkboxes associated with
     * the specified {@link Set} of transmitter mnemonics. In the case that
     * group toggle is enabled, this function will perform the necessary
     * mnemonic to transmitter group mapping.
     * 
     * @param sameTransmitterSet
     *            the specified {@link Set} of transmitter mnemonics
     */
    private void selectTransmitterCheckboxes(Set<String> sameTransmitterSet) {
        if (CollectionUtils.isEmpty(sameTransmitterSet)) {
            return;
        }

        CheckListData cld = new CheckListData();
        Set<String> selections = null;
        if (this.groupToggleBtn.getSelection()) {
            selections = new HashSet<>();
            for (String mnemonic : sameTransmitterSet) {
                Transmitter t = this.transmitterMap.get(mnemonic);
                if (t == null) {
                    continue;
                }
                selections.add(t.getTransmitterGroup().getName());
            }
        } else {
            selections = sameTransmitterSet;
        }
        for (String select : selections) {
            cld.addDataItem(select, true);
        }
        this.checks.selectCheckboxes(cld);
    }

    protected void refreshDisabled() {
        checks.setHelpText(getAffectedTransmitterStatus().getMessage());
        checks.enableCheckboxes(this.determineEnabled());
    }

    private Set<String> determineEnabled() {
        Set<String> mnemonics = this.getAffectedTransmitterMnemonics();
        Set<String> enabled = null;
        if (this.groupToggleBtn.getSelection()) {
            enabled = new HashSet<>();
            for (String mnemonic : mnemonics) {
                Transmitter t = this.transmitterMap.get(mnemonic);
                if (t == null) {
                    continue;
                }
                enabled.add(t.getTransmitterGroup().getName());
            }
        } else {
            enabled = mnemonics;
        }

        return enabled;
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

    protected void buildTransmitterGroupListData() {
        this.transmitterCLD = new CheckListData();
        this.transmitterGroupCLD = new CheckListData();

        try {
            List<TransmitterGroup> transmitterGroups = tdm
                    .getTransmitterGroups(new PositionComparator());
            if (transmitterGroups == null || transmitterGroups.isEmpty()) {
                return;
            }
            Set<Transmitter> allTransmitters = new HashSet<>();
            for (TransmitterGroup tg : transmitterGroups) {
                this.transmitterGroupCLD.addDataItem(tg.getName(), false);
                this.transmitterGroupMap.put(tg.getName(), tg);
                allTransmitters.addAll(tg.getTransmitters());
            }
            if (allTransmitters.isEmpty()) {
                return;
            }
            /*
             * sort the transmitters for use in a checklist.
             */
            List<Transmitter> sortedTransmitters = new ArrayList<>(
                    allTransmitters);
            Collections.sort(sortedTransmitters,
                    new TransmitterMnemonicComparator());
            for (Transmitter t : sortedTransmitters) {
                this.transmitterCLD.addDataItem(t.getMnemonic(), false);
                this.transmitterMap.put(t.getMnemonic(), t);
            }
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving transmitter group data from the database.",
                            e);
        }
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
