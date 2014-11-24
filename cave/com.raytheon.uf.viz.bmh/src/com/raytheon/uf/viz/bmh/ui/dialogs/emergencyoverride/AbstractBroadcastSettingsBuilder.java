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
package com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride;

import java.util.Collection;

import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.LiveBroadcastRecordPlaybackDlg;

/**
 * Common broadcast settings provided to the
 * {@link LiveBroadcastRecordPlaybackDlg} that specifies the
 * {@link TransmitterGroup} that the live broadcast will be played on.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014 3845       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBroadcastSettingsBuilder {

    private final BROADCASTTYPE type;

    private Collection<TransmitterGroup> selectedTransmitterGroups;

    public AbstractBroadcastSettingsBuilder(final BROADCASTTYPE type) {
        this.type = type;
    }

    /**
     * Builds a {@link LiveBroadcastStartCommand} that will be sent to the Comms
     * Manager to attempt to request resources for a live broadcast.
     * 
     * @return the {@link LiveBroadcastStartCommand} that is constructed.
     */
    public LiveBroadcastStartCommand buildBroadcastStartCommand() {
        LiveBroadcastStartCommand command = new LiveBroadcastStartCommand();
        command.setType(this.type);
        command.setMsgSource(MSGSOURCE.VIZ);
        for (TransmitterGroup tg : this.selectedTransmitterGroups) {
            BroadcastTransmitterConfiguration config = this
                    .buildTransmitterConfiguration(tg);
            config.setTransmitterGroup(tg);
            command.addTransmitterConfiguration(config);
        }

        return command;
    }

    /**
     * Builds a {@link BroadcastTransmitterConfiguration} for a single
     * {@link TransmitterGroup}. The {@link BroadcastTransmitterConfiguration}
     * includes information that will be used to configure
     * {@link TransmitterGroup} for a live broadcast.
     * 
     * @param tg
     * @return
     */
    protected abstract BroadcastTransmitterConfiguration buildTransmitterConfiguration(
            TransmitterGroup tg);

    /**
     * @return the type
     */
    public BROADCASTTYPE getType() {
        return type;
    }

    /**
     * @param selectedTransmitterGroups
     *            the selectedTransmitterGroups to set
     */
    protected void setSelectedTransmitterGroups(
            Collection<TransmitterGroup> selectedTransmitterGroups) {
        this.selectedTransmitterGroups = selectedTransmitterGroups;
    }

    /**
     * @return the selectedTransmitterGroups
     */
    public Collection<TransmitterGroup> getSelectedTransmitterGroups() {
        return selectedTransmitterGroups;
    }
}