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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.PlaylistData;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * Settings to configure an Emergency Override transmission.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014 3845       bkowal      Initial creation
 * Dec 1, 2014  3797       bkowal      Implemented getTonesDuration.
 * Dec 12, 2014 3603       bsteffen    Updates to TonesGenerator.
 * Jan 26, 2015 3359       bsteffen    Use site id for same tones.
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EOBroadcastSettingsBuilder extends
        AbstractBroadcastSettingsBuilder {

    /*
     * Data Managers
     */
    private TransmitterDataManager tdm = new TransmitterDataManager();

    private ZonesAreasDataManager zadm = new ZonesAreasDataManager();

    /*
     * Used to identify the emergency override broadcast, build same tones,
     * construct an Input Message for broadcast re-scheduling
     */

    private final MessageType messageType;

    private Calendar effectiveTime;

    private Calendar expireTime;

    private boolean playAlertTones;

    private String areaCodeString;

    private List<Transmitter> transmitters;

    private final SimpleDateFormat sdf = new SimpleDateFormat(
            PlaylistData.PLAYLIST_DATE_FORMAT);

    /*
     * Used to store SAME tones mapped to a {@link TransmitterGroup}.
     */
    private Map<TransmitterGroup, byte[]> transmitterGroupToneMap;

    private byte[] endTonesAudio;

    /*
     * Used to track the longest playback time of the SAME/Alert Tones in
     * milliseconds. This delay will be used to calculate how long to delay the
     * playback of tones with a shorter duration to reduce the possibility of a
     * period of silence on transmitters with tones that finish sooner.
     */
    private long longestDuration;

    public EOBroadcastSettingsBuilder(final MessageType messageType,
            final List<Transmitter> selectedTransmitters,
            final boolean playAlertTones, final int hoursDuration,
            final int minutesDuration) throws Exception {
        super(BROADCASTTYPE.EO);
        this.messageType = messageType;
        this.transmitters = selectedTransmitters;
        this.playAlertTones = playAlertTones;
        this.initialize(selectedTransmitters, hoursDuration, minutesDuration);
    }

    private void initialize(final List<Transmitter> selectedTransmitters,
            final int hoursDuration, final int minutesDuration)
            throws Exception {
        /*
         * Calculate the expiration date/time based on the duration.
         */
        this.effectiveTime = TimeUtil.newGmtCalendar();
        this.calculateExpiration(hoursDuration, minutesDuration);

        /*
         * Track area mappings to transmitter groups.
         */
        Map<TransmitterGroup, Set<Area>> transmitterGroupAreaMap = new HashMap<>();
        /*
         * Used to build the area code string.
         */
        StringBuilder areaCodeStrBuilder = new StringBuilder();
        List<String> addedAreaCodes = new ArrayList<>();
        boolean firstAreaAdded = true;
        for (Transmitter transmitter : selectedTransmitters) {
            /*
             * Get the transmitter group that the transmitter is associated
             * with.
             */
            TransmitterGroup transmitterGroup = this.tdm
                    .getTransmitterGroupContainsTransmitter(transmitter);
            if (transmitterGroup == null) {
                throw new Exception(
                        "Unable to find the Transmitter Group that Transmitter: "
                                + transmitter.getMnemonic()
                                + " has been assigned to.");
            }

            /*
             * Get the areas associated with the transmitter.
             */
            List<Area> areas = this.zadm.getAreasForTransmitter(transmitter);

            /* Complete the mapping */
            if (transmitterGroupAreaMap.containsKey(transmitterGroup) == false) {
                transmitterGroupAreaMap.put(transmitterGroup,
                        new HashSet<Area>());
            }
            transmitterGroupAreaMap.get(transmitterGroup).addAll(areas);
            for (Area area : areas) {
                String areaCode = area.getAreaCode();
                if (addedAreaCodes.contains(areaCode)) {
                    continue;
                }
                if (firstAreaAdded) {
                    firstAreaAdded = false;
                } else {
                    areaCodeStrBuilder.append("-");
                }
                areaCodeStrBuilder.append(areaCode);
                addedAreaCodes.add(areaCode);
            }
        }

        this.areaCodeString = areaCodeStrBuilder.toString();
        this.setSelectedTransmitterGroups(transmitterGroupAreaMap.keySet());

        /*
         * Construct SAME/Alert mappings to transmitter groups.
         */
        this.transmitterGroupToneMap = new HashMap<>(this
                .getSelectedTransmitterGroups().size(), 1.0f);
        for (TransmitterGroup tg : this.getSelectedTransmitterGroups()) {
            byte[] tones = this.constructSAMEAlertTones(transmitterGroupAreaMap
                    .get(tg));
            long tonesDuration = tones.length / 160L * 20L;
            if (tonesDuration > this.longestDuration) {
                this.longestDuration = tonesDuration;
            }
            this.transmitterGroupToneMap.put(tg, tones);
        }

        /*
         * Acquire the end tones.
         */
        this.endTonesAudio = TonesGenerator.getEndOfMessageTones().array();
    }

    private void calculateExpiration(final int hoursDuration,
            final int minutesDuration) {
        final boolean durationSet = hoursDuration != 0 || minutesDuration != 0;
        this.expireTime = TimeUtil.newCalendar(this.effectiveTime);
        if (durationSet) {
            this.expireTime.add(Calendar.HOUR, hoursDuration);
            this.expireTime.add(Calendar.MINUTE, minutesDuration);
        } else {
            /*
             * default to one day when no duration has been defined.
             */
            this.expireTime.add(Calendar.DATE, 1);
        }
    }

    private byte[] constructSAMEAlertTones(Collection<Area> areas)
            throws ToneGenerationException {
        SAMEToneTextBuilder toneBuilder = new SAMEToneTextBuilder();
        toneBuilder.setEventFromAfosid(this.messageType.getAfosid());
        for (Area area : areas) {
            toneBuilder.addAreaFromUGC(area.getAreaCode());
        }
        toneBuilder.setEffectiveTime(this.effectiveTime);
        toneBuilder.setExpireTime(this.expireTime);
        toneBuilder.setNwsSiteId(LocalizationManager.getInstance().getSite());
        final String sameTone = toneBuilder.build().toString();

        // build the SAME tone
        return TonesGenerator.getSAMEAlertTones(sameTone, this.playAlertTones,
                true).array();
    }

    @Override
    protected BroadcastTransmitterConfiguration buildTransmitterConfiguration(
            TransmitterGroup tg) {
        BroadcastTransmitterConfiguration config = new BroadcastTransmitterConfiguration();
        config.setMessageId(this.messageType.getAfosid());
        config.setMessageTitle(this.messageType.getTitle());
        config.setMessageName("EMERGENCY OVERRIDE");
        config.setExpirationTime(sdf.format(this.expireTime.getTime()));
        config.setAlert((this.playAlertTones) ? BroadcastTransmitterConfiguration.TONE_SENT
                : BroadcastTransmitterConfiguration.TONE_NONE);
        config.setSame(BroadcastTransmitterConfiguration.TONE_SENT);
        config.setToneAudio(this.transmitterGroupToneMap.get(tg));
        /*
         * calculate audio duration. calculate delay based on audio duration.
         */
        long tonesDuration = config.getToneAudio().length / 160L * 20L;
        config.setDelayMilliseconds(this.longestDuration - tonesDuration);
        config.setEndToneAudio(this.endTonesAudio);

        return config;
    }

    @Override
    protected long getTonesDuration() {
        return this.longestDuration;
    }

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
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

    /**
     * @return the areaCodeString
     */
    public String getAreaCodeString() {
        return areaCodeString;
    }

    /**
     * @return the transmitters
     */
    public List<Transmitter> getTransmitters() {
        return transmitters;
    }
}