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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.same.SAMEOriginatorMapper;
import com.raytheon.uf.common.bmh.same.SAMEStateCodes;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionData;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * Used to store information that will be used to configure a live broadcast
 * session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2014 3655       bkowal      Initial creation
 * Oct 21, 2014 3655       bkowal      Calculate the tone duration delays.
 * Oct 21, 2014 3655       bkowal      Updated to include more information that
 *                                     will be used to populate a 
 *                                     {@link LiveBroadcastSwitchNotification}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastSettings {

    private MessageType selectedMessageType;

    private Map<Transmitter, List<Area>> selectedTransmitterAreasMap;

    private boolean playAlertTones;

    private int durationHours;

    private int durationMinutes;

    private final SAMEOriginatorMapper originatorMapping = new SAMEOriginatorMapper();

    private final SAMEStateCodes stateCodes = new SAMEStateCodes();

    private Calendar effectiveTime;

    private Calendar expireTime;

    /**
     * 
     */
    public LiveBroadcastSettings() {
    }

    public void populate(final MessageType selectedMessageType,
            final List<Transmitter> selectedTransmitters,
            final boolean playAlertTones, final int durationHours,
            final int durationMinutes) throws Exception {
        this.selectedMessageType = selectedMessageType;
        this.populateTransmitterAreaMap(selectedTransmitters);
        this.playAlertTones = playAlertTones;
        this.durationHours = durationHours;
        this.durationMinutes = durationMinutes;

        final boolean durationSet = this.durationHours != 0
                || this.durationMinutes != 0;
        this.effectiveTime = TimeUtil.newGmtCalendar();
        this.expireTime = TimeUtil.newCalendar(this.effectiveTime);
        if (durationSet == false) {
            /*
             * increase by one day when the duration has not been specified.
             */
            this.expireTime.add(Calendar.DATE, 1);
        } else {
            /*
             * increase by the duration when specified.
             */
            this.expireTime.add(Calendar.HOUR, this.durationHours);
            this.expireTime.add(Calendar.MINUTE, this.durationMinutes);
        }
    }

    private void populateTransmitterAreaMap(
            final List<Transmitter> selectedTransmitters) throws Exception {
        AreaSelectionData areaData = new AreaSelectionData();
        areaData.populate();

        this.selectedTransmitterAreasMap = new HashMap<>(
                (int) Math.ceil(selectedTransmitters.size() / 0.75));
        for (Transmitter transmitter : selectedTransmitters) {
            List<Area> areas = areaData.getAreasForTransmitter(transmitter);
            if (areas == null) {
                throw new Exception(
                        "Unable to find the area(s) for Transmitter: "
                                + transmitter.getMnemonic() + "!");
            }
            this.selectedTransmitterAreasMap.put(transmitter, areas);
        }
    }

    public long getTransmitterSAMETones(
            Map<Transmitter, byte[]> transmitterToneMap) throws Exception {
        long longestDuration = 0;
        for (Transmitter transmitter : this.selectedTransmitterAreasMap
                .keySet()) {
            SAMEToneTextBuilder toneBuilder = new SAMEToneTextBuilder();
            toneBuilder.setOriginatorMapper(originatorMapping);
            toneBuilder.setStateCodes(stateCodes);
            toneBuilder
                    .setEventFromAfosid(this.selectedMessageType.getAfosid());
            for (Area area : this.selectedTransmitterAreasMap.get(transmitter)) {
                toneBuilder.addAreaFromUGC(area.getAreaCode());
            }
            toneBuilder.setEffectiveTime(this.effectiveTime);
            toneBuilder.setExpireTime(this.expireTime);
            // TODO this needs to be read from configuration.
            toneBuilder.setNwsIcao("K"
                    + LocalizationManager.getInstance().getSite());
            final String sameTone = toneBuilder.build().toString();

            // convert the same tone
            byte[] tonesAudio = TonesGenerator.getSAMEAlertTones(sameTone,
                    this.playAlertTones).array();
            long duration = tonesAudio.length / 160L * 20L;
            if (duration > longestDuration) {
                longestDuration = duration;
            }
            transmitterToneMap.put(transmitter, tonesAudio);
        }

        return longestDuration;
    }

    /**
     * @return the selectedMessageType
     */
    public MessageType getSelectedMessageType() {
        return selectedMessageType;
    }

    /**
     * @return the playAlertTones
     */
    public boolean isPlayAlertTones() {
        return playAlertTones;
    }

    /**
     * @return the effectiveTime
     */
    public Calendar getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * @return the expireTime
     */
    public Calendar getExpireTime() {
        return expireTime;
    }
}