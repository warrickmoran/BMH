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
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle.PlaylistData;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.AreaSelectionSaveData;
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
 * Mar 16, 2015 4244       bsteffen    Use areas from area selection dialog,
 *                                     only send same tones to selected transmitters.
 * Apr 01, 2015 4339       bkowal      Notify users when EO Same Tones will be truncated.
 * Apr 02, 2015 4352       rferrel     When no area specified for a transmitter include all
 *                                      its areas.
 * May 4, 2015  4394       bkowal      Tone playback text is now in
 *                                     {@link INonStandardBroadcast}.
 * MAy 05, 2015 4463       bkowal      Use the originator associated with the selected
 *                                     {@link MessageType}.
 * May 12, 2015 4248       rjpeter     Fix misspelling.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EOBroadcastSettingsBuilder extends
        AbstractBroadcastSettingsBuilder {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EOBroadcastSettingsBuilder.class);

    /*
     * Data Managers
     */
    private final TransmitterDataManager tdm = new TransmitterDataManager();

    /*
     * Used to identify the emergency override broadcast, build same tones,
     * construct an Input Message for broadcast re-scheduling
     */

    private final MessageType messageType;

    private Calendar effectiveTime;

    private Calendar expireTime;

    private final boolean playAlertTones;

    private String areaCodeString;

    private final SimpleDateFormat sdf = new SimpleDateFormat(
            PlaylistData.PLAYLIST_DATE_FORMAT);

    /*
     * Used to store SAME tones mapped to a {@link TransmitterGroup}.
     */
    private Map<TransmitterGroup, byte[]> transmitterGroupToneMap;

    /*
     * Groups that whose entry in transmitterGroupToneMap contains SAME tones,
     * these items need end tones.
     */
    private final Set<TransmitterGroup> sameGroups = new HashSet<>();

    private byte[] endTonesAudio;

    /*
     * Used to track the longest playback time of the SAME/Alert Tones in
     * milliseconds. This delay will be used to calculate how long to delay the
     * playback of tones with a shorter duration to reduce the possibility of a
     * period of silence on transmitters with tones that finish sooner.
     */
    private long longestDuration;

    public EOBroadcastSettingsBuilder(final MessageType messageType,
            final Set<Transmitter> selectedTransmitters,
            Set<Transmitter> sameTransmitters, AreaSelectionSaveData areaData,
            final boolean playAlertTones, final int hoursDuration,
            final int minutesDuration) throws Exception {
        super(BROADCASTTYPE.EO);
        this.messageType = messageType;
        this.playAlertTones = playAlertTones;
        this.initialize(selectedTransmitters, sameTransmitters, areaData,
                hoursDuration, minutesDuration);
    }

    private void initialize(final Set<Transmitter> selectedTransmitters,
            Set<Transmitter> sameTransmitters, AreaSelectionSaveData areaData,
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
        for (String areaOrZone : areaData.getSelectedAreaZoneCodes()) {
            if (areaCodeStrBuilder.length() > 0) {
                areaCodeStrBuilder.append("-");
            }
            areaCodeStrBuilder.append(areaOrZone);
        }

        for (Transmitter transmitter : selectedTransmitters) {
            if (transmitter.getTxStatus() != TxStatus.ENABLED) {
                continue;
            }
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

            Set<Area> areasForThisTransmitter = new HashSet<Area>();
            if (sameTransmitters.contains(transmitter)) {
                for (Area area : areaData.getAreas()) {
                    if (area.getTransmitters().contains(transmitter)) {
                        areasForThisTransmitter.add(area);
                    }
                }

                for (Zone zone : areaData.getZones()) {
                    for (Area area : zone.getAreas()) {
                        if (area.getTransmitters().contains(transmitter)) {
                            areasForThisTransmitter.add(area);
                        }
                    }
                }
                if (areasForThisTransmitter.isEmpty()) {
                    /* Assume all areas for the transmitter should be covered. */
                    ZonesAreasDataManager zadm = new ZonesAreasDataManager();
                    for (Area area : zadm.getAreasForTransmitter(transmitter)) {
                        areasForThisTransmitter.add(area);
                    }
                }
            }

            /* Complete the mapping */
            if (transmitterGroupAreaMap.containsKey(transmitterGroup) == false) {
                transmitterGroupAreaMap.put(transmitterGroup,
                        areasForThisTransmitter);
            } else {
                transmitterGroupAreaMap.get(transmitterGroup).addAll(
                        areasForThisTransmitter);
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
            Set<Area> areas = transmitterGroupAreaMap.get(tg);
            byte[] tones;
            if (areas.isEmpty()) {
                if (playAlertTones) {
                    tones = TonesGenerator.getOnlyAlertTones().array();
                } else {
                    tones = new byte[0];
                }
            } else {
                tones = this.constructSAMEAlertTones(areas);
                sameGroups.add(tg);
            }
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
        List<String> ugcs = new ArrayList<>(areas.size());
        for (Area area : areas) {
            ugcs.add(area.getAreaCode());
        }
        toneBuilder.setEventFromAfosid(this.messageType.getAfosid());
        toneBuilder.setOriginator(this.messageType.getOriginator());
        toneBuilder.addAreasFromUGC(ugcs);
        String invalidAreas = toneBuilder.summarizeInvalidAreas();
        String overLimitAreas = toneBuilder.summarizeOverLimitAreas();
        if (invalidAreas.isEmpty() == false
                || overLimitAreas.isEmpty() == false) {
            statusHandler.error("Failed to add all areas to the SAME Message. "
                    + overLimitAreas + " " + invalidAreas);
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
        config.setAlert((this.playAlertTones) ? INonStandardBroadcast.TONE_SENT
                : INonStandardBroadcast.TONE_NONE);
        config.setSame(INonStandardBroadcast.TONE_SENT);
        config.setToneAudio(this.transmitterGroupToneMap.get(tg));
        /*
         * calculate audio duration. calculate delay based on audio duration.
         */
        long tonesDuration = config.getToneAudio().length / 160L * 20L;
        config.setDelayMilliseconds(this.longestDuration - tonesDuration);
        if (sameGroups.contains(tg)) {
            config.setEndToneAudio(this.endTonesAudio);
        } else {
            config.setEndToneAudio(new byte[0]);
        }

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
}