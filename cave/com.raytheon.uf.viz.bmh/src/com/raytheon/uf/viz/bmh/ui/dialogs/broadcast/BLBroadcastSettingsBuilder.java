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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcast;

import java.util.List;

import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride.AbstractBroadcastSettingsBuilder;

/**
 * Settings to configure a Broadcast Live transmission.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014 3845       bkowal      Initial creation
 * Dec 1, 2014  3797       bkowal      Implemented getTonesDuration.
 * May 4, 2015  4394       bkowal      Tone playback text is now in
 *                                     {@link INonStandardBroadcast}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BLBroadcastSettingsBuilder extends
        AbstractBroadcastSettingsBuilder {
    private final String DEFAULT_FILLER = "-";

    /**
     * Constructor
     * 
     * @param selectedTransmitterGroups
     *            transmitter groups that the broadcast will be transmitted to
     */
    public BLBroadcastSettingsBuilder(
            List<TransmitterGroup> selectedTransmitterGroups) {
        super(BROADCASTTYPE.BL);
        this.setSelectedTransmitterGroups(selectedTransmitterGroups);
    }

    @Override
    protected BroadcastTransmitterConfiguration buildTransmitterConfiguration(
            TransmitterGroup tg) {
        BroadcastTransmitterConfiguration config = new BroadcastTransmitterConfiguration();
        config.setTransmitterGroup(tg);
        config.setMessageId(DEFAULT_FILLER);
        config.setMessageTitle(DEFAULT_FILLER);
        config.setMessageName("BROADCAST LIVE");
        config.setExpirationTime(DEFAULT_FILLER);
        config.setAlert(INonStandardBroadcast.TONE_NONE);
        config.setSame(INonStandardBroadcast.TONE_NONE);

        return config;
    }

    @Override
    protected long getTonesDuration() {
        /*
         * There are never any tones for Broadcast Live.
         */
        return 0;
    }
}