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
package com.raytheon.uf.edex.bmh.staticmsg;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.schemas.ssml.Prosody;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.common.bmh.schemas.ssml.SpeechRateFormatter;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.tts.NeoSpeechConstants;

/**
 * Used to generate and store the SSML that is used to generate the time message
 * audio for a specific {@link Language}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 26, 2015 4314       bkowal      Initial creation
 * Oct 06, 2015 4904       bkowal      Set the neospeech volume in the SSML.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SSMLTimeCache {

    /* SSML Properties */
    private static final String SAY_AS_INTERPRET_AS = "time";

    private static final String SAY_AS_FORMAT = "hms12";

    private static final String TIME_COLON = ":";

    private final Language language;

    // Table<SpeechRate, Hour, SSML>
    private Table<Integer, Integer, String> ssmlHourTable = HashBasedTable
            .create(SpeechRateFormatter.NUM_RATES,
                    TimeMessagesGenerator.MAX_HOUR);

    // Table<SpeechRate, Minute, SSML>
    private Table<Integer, Integer, String> ssmlMinuteTable = HashBasedTable
            .create(SpeechRateFormatter.NUM_RATES,
                    TimeMessagesGenerator.MAX_MINUTE);

    // Table<SpeechRate, Period, SSML>
    private Table<Integer, String, String> ssmlPeriodTable = HashBasedTable
            .create(SpeechRateFormatter.NUM_RATES,
                    TimeMessagesGenerator.TIME_PERIODS.length);

    // Table<SpeechRate, TimeZone(Short), SSML>
    private Table<Integer, String, String> ssmlTimeZoneTable = HashBasedTable
            .create(SpeechRateFormatter.NUM_RATES, BMHTimeZone.values().length);

    public SSMLTimeCache(Language language) throws BMHConfigurationException {
        this.language = language;
        this.generate();
    }

    private void generate() throws BMHConfigurationException {
        // iterate over the recognized rates of speech.
        for (int rate = SpeechRateFormatter.MIN_RATE; rate <= SpeechRateFormatter.MAX_RATE; rate++) {
            final String prosodyRate = SpeechRateFormatter
                    .formatSpeechRate(rate);

            this.constructHourSSML(rate, prosodyRate);
            this.constructMinuteSSML(rate, prosodyRate);
            this.constructPeriodSSML(rate, prosodyRate);
            this.constructTimeZoneSSML(rate, prosodyRate);
        }
    }

    private void constructHourSSML(final int speechRate,
            final String prosodyRate) throws BMHConfigurationException {
        for (int hh = 1; hh <= TimeMessagesGenerator.MAX_HOUR; hh++) {
            String ssml = null;
            try {
                ssml = this.constructSSML(prosodyRate, Integer.toString(hh));
            } catch (SSMLConversionException e) {
                StringBuilder sb = new StringBuilder(
                        "Failed to generate SSML for hour: ");
                sb.append(hh).append(" with speech rate: ").append(prosodyRate)
                        .append(".");

                throw new BMHConfigurationException(sb.toString(), e);
            }

            this.ssmlHourTable.put(speechRate, hh, ssml);
        }
    }

    private void constructMinuteSSML(final int speechRate,
            final String prosodyRate) throws BMHConfigurationException {
        for (int mm = 1; mm <= TimeMessagesGenerator.MAX_MINUTE; mm++) {
            String ssml = null;
            String timeStr = TIME_COLON
                    + StringUtils.leftPad(Integer.toString(mm), 2, "0");
            try {
                ssml = this.constructSSMLSayAs(prosodyRate, timeStr);
            } catch (SSMLConversionException e) {
                StringBuilder sb = new StringBuilder(
                        "Failed to generate SSML for minute: ");
                sb.append(mm).append(" with speech rate: ").append(prosodyRate)
                        .append(".");

                throw new BMHConfigurationException(sb.toString(), e);
            }

            this.ssmlMinuteTable.put(speechRate, mm, ssml);
        }
    }

    private void constructPeriodSSML(final int speechRate,
            final String prosodyRate) throws BMHConfigurationException {
        for (String timePeriod : TimeMessagesGenerator.TIME_PERIODS) {
            String ssml = null;
            try {
                ssml = this.constructSSMLSayAs(prosodyRate, timePeriod);
            } catch (SSMLConversionException e) {
                StringBuilder sb = new StringBuilder(
                        "Failed to generate SSML for time period: ");
                sb.append(timePeriod).append(" with speech rate: ")
                        .append(prosodyRate).append(".");

                throw new BMHConfigurationException(sb.toString(), e);
            }

            this.ssmlPeriodTable.put(speechRate, timePeriod, ssml);
        }
    }

    private void constructTimeZoneSSML(final int speechRate,
            final String prosodyRate) throws BMHConfigurationException {
        /*
         * Daylight Savings Time options as an array for iterative convenience
         */
        final boolean[] dstOptions = { false, true };

        for (BMHTimeZone tz : BMHTimeZone.values()) {
            for (boolean dstOption : dstOptions) {
                String tzShort = tz.getShortDisplayName(dstOption);
                /*
                 * Verify that we have not already generated SSML for this
                 * timezone.
                 */
                if (this.ssmlTimeZoneTable.contains(speechRate, tzShort)) {
                    /*
                     * already have SSML for this time zone.
                     */
                    continue;
                }
                String tzLong = tz.getLongDisplayName(dstOption);
                String ssml = null;
                try {
                    ssml = this.constructSSML(prosodyRate, tzLong);
                } catch (SSMLConversionException e) {
                    StringBuilder sb = new StringBuilder(
                            "Failed to generate SSML for time zone: ");
                    sb.append(tzLong).append(" with speech rate: ")
                            .append(prosodyRate).append(".");

                    throw new BMHConfigurationException(sb.toString(), e);
                }

                this.ssmlTimeZoneTable.put(speechRate, tzShort, ssml);
            }
        }
    }

    private String constructSSMLSayAs(final String prosodyRate,
            final String text) throws SSMLConversionException {
        SSMLDocument ssmlDocument = new SSMLDocument(this.language);
        Prosody prosody = ssmlDocument.getFactory().createProsody();
        prosody.setRate(prosodyRate);
        prosody.setVolume(NeoSpeechConstants.getVolume());
        SayAs sayAsTag = ssmlDocument.getFactory().createSayAs();
        sayAsTag.setInterpretAs(SAY_AS_INTERPRET_AS);
        sayAsTag.setFormat(SAY_AS_FORMAT);
        sayAsTag.setContent(text);
        prosody.getContent().add(sayAsTag);
        ssmlDocument.getRootTag().getContent().add(prosody);

        return ssmlDocument.toSSML();
    }

    private String constructSSML(final String prosodyRate, final String text)
            throws SSMLConversionException {
        SSMLDocument ssmlDocument = new SSMLDocument(this.language);

        Prosody prosody = ssmlDocument.getFactory().createProsody();
        prosody.setRate(prosodyRate);
        prosody.setVolume(NeoSpeechConstants.getVolume());
        prosody.getContent().add(text);
        ssmlDocument.getRootTag().getContent().add(prosody);

        return ssmlDocument.toSSML();
    }

    public Iterator<Integer> getHourIteratorForSpeechRate(final int speechRate) {
        if (this.ssmlHourTable.containsRow(speechRate) == false) {
            throw new IllegalArgumentException(
                    "No cached hour ssml exists for speech rate: "
                            + SpeechRateFormatter.formatSpeechRate(speechRate)
                            + ".");
        }

        return this.ssmlHourTable.row(speechRate).keySet().iterator();
    }

    public Iterator<Integer> getMinuteIteratorForSpeechRate(final int speechRate) {
        if (this.ssmlMinuteTable.containsRow(speechRate) == false) {
            throw new IllegalArgumentException(
                    "No cached minute ssml exists for speech rate: "
                            + SpeechRateFormatter.formatSpeechRate(speechRate)
                            + ".");
        }

        return this.ssmlMinuteTable.row(speechRate).keySet().iterator();
    }

    public String getHourSSML(final int speechRate, final int hour) {
        return this.ssmlHourTable.get(speechRate, hour);
    }

    public String getMinuteSSML(final int speechRate, final int minute) {
        return this.ssmlMinuteTable.get(speechRate, minute);
    }

    public String getPeriodSSML(final int speechRate, final String period) {
        return this.ssmlPeriodTable.get(speechRate, period);
    }

    public String getTimezoneSSML(final int speechRate, final String timezone) {
        return this.ssmlTimeZoneTable.get(speechRate, timezone);
    }
}