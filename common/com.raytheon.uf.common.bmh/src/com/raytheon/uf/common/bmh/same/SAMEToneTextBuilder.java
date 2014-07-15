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
package com.raytheon.uf.common.bmh.same;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Build the text for a SAME(Specific Area Message Encoding) tone. This builder
 * should be populated with various data values and then {@link #build()} should
 * be called to construct the SAME text. This is a list of the fields required
 * for {@link #build()} to succeed along with the methods that can be used to
 * set each field.
 * 
 * <ol>
 * <li>originator or originator mapper({@link #setOriginator(String)}
 * {@link #setOriginatorMapper(SAMEOriginatorMapper)}).
 * <li>event code({@link #setEvent(String)} {@link #setEventFromAfosid(String)}
 * ).
 * <li>area({@link #addArea(String)} {@link #setStateCodes(SAMEStateCodes)}
 * {@link #addAreaFromUGC(String)}).
 * <li>effective time({@link #setEffectiveTime(Calendar)}).
 * <li>purge time or expire time({@link #setPurgeTime(int, int)}
 * {@link #setExpireTime(Calendar)}).
 * <li>originator office({@link #setOriginatorOffice(CharSequence)} {
 * {@link #setNwsIcao(String)}).
 * </ol>
 * 
 * More information about the SAME format can be found on the <a
 * href="http://www.nws.noaa.gov/directives/sym/pd01017012curr.pdf">NWS
 * documentation page</a>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 03, 2014  3285     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SAMEToneTextBuilder {

    private static final String START_CODE = "ZCZC";

    private static final String END_CODE = "NNNN";

    private static final String SEP = "-";

    public static final String CIVIL_ORIGINATOR = "CIV";

    public static final String NWS_ORIGINATOR = "WXR";

    private String originator;

    private SAMEOriginatorMapper originatorMapper;

    private String event;

    private SAMEStateCodes stateCodes;

    private List<CharSequence> area = new ArrayList<>(31);

    private Calendar effectiveTime;

    private Integer purgeHours;

    private Integer purgeMinutes;

    private Calendar expireTime;

    private CharSequence originatorOffice;

    /**
     * The Originator header code block indicates who initiated the message. The
     * only originator codes are:
     * 
     * <pre>
     * ORIGINATOR                         ORG CODE 
     * Broadcast station or cable system  EAS
     * Civil authorities                  CIV
     * National Weather Service           WXR
     * Primary Entry Point System         PEP
     * </pre>
     * 
     * The originator is not needed if an originator mapper is provided instead
     * using {@link #setOriginatorMapper(SAMEOriginatorMapper)} .
     * 
     * @param originator
     *            the originator
     * @see #setOriginatorMapper(SAMEOriginatorMapper)
     */
    public void setOriginator(String originator) {
        this.originator = originator;
    }

    /**
     * Set the lookup object for determining originator based off event codes.
     * This field is only necessary if originator is never set, if originator is
     * set then this is ignored.
     * 
     * @param mapper
     * @see #setOriginator(String)
     * @see #setEvent(String)
     */
    public void setOriginatorMapper(SAMEOriginatorMapper mapper) {
        this.originatorMapper = mapper;
    }

    /**
     * The Event header code block identifies the type of Event and information
     * contained in the Voice message, if a Voice message is sent. The Event
     * code may be sent with or without a WAT or Voice message as an alerting
     * function only. It also may be sent as a control code for some NWR system
     * control functions.
     * 
     * @param event
     *            header code block.
     * @see #setEventFromAfosid(String)
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Alternative to {@link #setEvent(String)} that parses it from an afosid.
     * 
     * @param afosid
     *            an afosid.
     * @see #setEvent(String)
     */
    public void setEventFromAfosid(String afosid) {
        this.event = afosid.substring(3, 6);
    }

    /**
     * The Geographical Area header code block identifies the geographic area
     * affected by the NWR SAME message. Each location code uniquely identifies
     * a geographical area. A message may contain up to 31 Geographical Area
     * blocks.
     * 
     * @param area
     *            the PSSCCC code block.
     * @see #addAreaFromUGC(String)
     * @throws IllegalStateException
     *             if too many areas are provided(only 31 can be encoded.
     */
    public void addArea(CharSequence area) throws IllegalStateException {
        if (this.area.size() >= 31) {
            throw new IllegalStateException("Too many areas were provided("
                    + this.area.size() + " >= 31)");
        }
        this.area.add(area);
    }

    /**
     * When using {@link #addAreaFromUGC(String)} a {@link SAMEStateCodes}
     * object must be provided to look up the appropriate state codes. If
     * addAreaFromUGC is not used then this is not necessary.
     * 
     * @param stateCodes
     *            lookup object for state codes.
     * @see #addAreaFromUGC(String)
     */
    public void setStateCodes(SAMEStateCodes stateCodes) {
        this.stateCodes = stateCodes;
    }

    /**
     * Add a new area by parsing the UGC value and converting it to the correct
     * format. {@link #setStateCodes(SAMEStateCodes)} must be called before this
     * method is used. Any UGC value provided must be for an area and not for a
     * zone.
     * 
     * @param ugc
     *            Universal Generic Code
     * @see #addArea(String)
     * @see #setStateCodes(SAMEStateCodes)
     * @throws IllegalStateException
     *             if too many areas are provided(only 31 can be encoded or if
     *             no state codes are set.
     * @throws IllegalArgumentException
     *             if any portion of the UGC is not recognized.
     */
    public void addAreaFromUGC(String ugc) {
        if (stateCodes == null) {
            throw new IllegalStateException(
                    "State codes must be provided to process UGCs.");
        }
        StringBuilder area = new StringBuilder(6);
        char p = ugc.charAt(2);
        if (p == 'C') {
            area.append("0");
        } else if (Character.isDigit(p)) {
            area.append(p);
        } else if (p == 'Z') {
            throw new IllegalArgumentException("Zones are not supported");
        } else {
            throw new IllegalArgumentException("Unrecognized county portion: "
                    + p);
        }
        Integer stateCode = stateCodes.getStateCode(ugc.substring(0, 2));
        if (stateCode == null) {
            throw new IllegalArgumentException("Unrecognized state: "
                    + ugc.substring(0, 2));
        }
        area.append(String.format("%02d", stateCode));
        area.append(ugc.substring(3));
        addArea(area);
    }

    /**
     * This header code block identifies the Julian Calendar date and the time
     * the message was originally disseminated in hours and minutes using the 24
     * hour Coordinated Universal Time (UTC) clock. This method must be called
     * before {@link #build()}
     * 
     * @param effectiveTime
     *            the time this message becomes effective.
     */
    public void setEffectiveTime(Calendar effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * The Purge time header code block identifies the purge time of the message
     * expressed in a delta time from the issue time in 15 minute segments up to
     * one hour, then in 30 minute segments beyond one hour up to six hours;
     * i.e. + 0015-, +0030-, +0045-, +0100-, +0430-, +0600-. This delta time,
     * when added to the issue time, specifies when the message is no longer
     * valid and should be purged from the system, not to be used again. It is
     * important to note that the valid or purge time of the NWSI 10-1712
     * OCTOBER 3, 2011 A-6 message will not always equal the event expiration
     * time. For most short-term events such as tornadoes and severe
     * thunderstorms, the two times will most often be identical. For longer
     * duration events such as a hurricane or winter storm that may not end for
     * many hours or days, the valid time in the code only applies to that
     * message, and is not an indicator when the threat is over. This block is
     * always preceded by "+".
     * 
     * If {@link #setExpireTime(Calendar)} is used then a purge time is not
     * necessary and will be calculated based off the effective and expire
     * times.
     * 
     * @param hours
     *            number of full hours before purge
     * @param minutes
     *            number of minutes(in addition to hours) before purge
     * @see #setEffectiveTime(Calendar)
     * @see #setExpireTime(Calendar)
     */
    public void setPurgeTime(int hours, int minutes) {
        this.purgeHours = hours;
        this.purgeMinutes = minutes;
    }

    /**
     * Set the expiration time. This field is an alternative to
     * {@link #setPurgeTime(int, int)} and will be ignored if setPurgeTime is
     * used.
     * 
     * @param expireTime
     *            the time this message stops being effective.
     * @see #setEffectiveTime(Calendar)
     * @see #setExpireTime(Calendar)
     */
    public void setExpireTime(Calendar expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * This header code block identifies the originator of the message, or in
     * the case of the EAS, that of a station re-broadcasting the message. NWS
     * offices use the International Civil Aviation Organization (ICAO) location
     * identifiers (first four letters), e.g., KDTX/NWS for Detroit, MI and
     * KTOP/NWS for Topeka, KS. Radio and television stations use the stations
     * call sign such as KFAB/AM or WDAF/FM.
     * 
     * @param originatorOffice
     * @see #setNwsIcao(String)
     */
    public void setOriginatorOffice(CharSequence originatorOffice) {
        this.originatorOffice = originatorOffice;
    }

    /**
     * A short form of {@link #setOriginatorOffice(CharSequence)} that takes
     * only the nws icao and appends "/NWS" to build a valid office identifier.
     * 
     * @param icao
     * @see #setOriginatorOffice(CharSequence)
     */
    public void setNwsIcao(String icao) {
        StringBuilder originatorOffice = new StringBuilder(8);
        originatorOffice.append(icao);
        originatorOffice.append("/NWS");
        setOriginatorOffice(originatorOffice);
    }

    /**
     * build a SAME tone text message from the information provided to this
     * object. If there is not enough information to build a message then an
     * {@link IllegalStateException} will be thrown.
     * 
     * @return the SAME tone text message
     */
    public CharSequence build() throws IllegalStateException {
        validate();
        StringBuilder text = new StringBuilder(80);
        text.append(START_CODE);
        text.append(SEP).append(originator);
        text.append(SEP).append(event);
        for (CharSequence code : area) {
            text.append(SEP).append(code);
        }
        try (Formatter formatter = new Formatter(text)) {
            formatter.format("+%02d%02d", purgeHours, purgeMinutes);
            formatter.format("-%1$tj%1$tH%1$tM", effectiveTime);
        }
        text.append(SEP).append(originatorOffice);
        text.append(SEP).append(END_CODE);
        return text;
    }

    private void validate() throws IllegalStateException {
        if (event == null) {
            throw new IllegalStateException("Must set event.");
        }
        if (originator == null) {
            if (originatorMapper == null) {
                throw new IllegalStateException(
                        "Must set originator or originator mapper.");
            } else {
                originator = originatorMapper.getOriginator(event);
            }
        }
        if (area.isEmpty()) {
            throw new IllegalStateException("Must specify at least one area.");
        }
        if (effectiveTime == null) {
            throw new IllegalStateException("Must set effective time.");
        }
        if (purgeHours == null || purgeMinutes == null) {
            if (expireTime == null) {
                throw new IllegalStateException(
                        "Must set purge or expire time.");
            } else {
                long diffMillis = expireTime.getTimeInMillis()
                        - effectiveTime.getTimeInMillis();
                int diffMinutes = (int) (diffMillis / TimeUtil.MILLIS_PER_MINUTE);
                purgeHours = diffMinutes / TimeUtil.MINUTES_PER_HOUR;
                purgeMinutes = diffMinutes - purgeHours
                        * TimeUtil.MINUTES_PER_HOUR;
                if (purgeHours >= 6) {
                    purgeHours = 6;
                    purgeMinutes = 0;
                } else if (purgeHours >= 1) {
                    if (purgeMinutes > 30) {
                        purgeMinutes = 0;
                        purgeHours += 1;
                    } else if (purgeMinutes != 0) {
                        purgeMinutes = 30;
                    }
                } else {
                    if (purgeMinutes > 45) {
                        purgeMinutes = 0;
                        purgeHours += 1;
                    } else if (purgeMinutes > 30) {
                        purgeMinutes = 45;
                    } else if (purgeMinutes > 15) {
                        purgeMinutes = 30;
                    } else if (purgeMinutes != 0) {
                        purgeMinutes = 15;
                    }
                }
            }
        }
        if (originatorOffice == null) {
            throw new IllegalStateException("Must set originator office.");
        }
    }
}