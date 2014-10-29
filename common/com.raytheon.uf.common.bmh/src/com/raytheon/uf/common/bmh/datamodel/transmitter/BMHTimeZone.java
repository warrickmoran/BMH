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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Time zone "enum" that encapsulates all the necessary information about time
 * zones for BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2014  #3617     dgilling     Initial creation
 * Oct 28, 2014  #3750     bkowal       Added method to get short timezone name
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public enum BMHTimeZone {

    UTC(TimeZone.getTimeZone("UTC"), "UNIVERSAL COORDINATED TIME", 0), ATLANTIC(
            TimeZone.getTimeZone("Canada/Atlantic"), "ATLANTIC", 2), ATLANTIC_NO_DST(
            TimeZone.getTimeZone("America/Puerto_Rico"), "ATLANTIC", 1), EASTERN(
            TimeZone.getTimeZone("US/Eastern"), "EASTERN", 4), EASTERN_NO_DST(
            TimeZone.getTimeZone("GMT-5"), "EASTERN", 3), CENTRAL(TimeZone
            .getTimeZone("US/Central"), "CENTRAL", 6), CENTRAL_NO_DST(TimeZone
            .getTimeZone("GMT-6"), "CENTRAL", 5), MOUNTAIN(TimeZone
            .getTimeZone("US/Mountain"), "MOUNTAIN", 8), MOUNTAIN_NO_DST(
            TimeZone.getTimeZone("GMT-7"), "MOUNTAIN", 7), PACIFIC(TimeZone
            .getTimeZone("US/Pacific"), "PACIFIC", 10), PACIFIC_NO_DST(TimeZone
            .getTimeZone("GMT-8"), "PACIFIC", 9), ALASKA(TimeZone
            .getTimeZone("US/Alaska"), "ALASKA", 12), ALASKA_NO_DST(TimeZone
            .getTimeZone("GMT-9"), "ALASKA", 11), ALEUTIAN(TimeZone
            .getTimeZone("US/Aleutian"), "HAWAII-ALEUTIAN", 13), ALEUTIAN_NO_DST(
            TimeZone.getTimeZone("US/Hawaii"), "HAWAII-ALEUTIAN", -1), GUAM(
            TimeZone.getTimeZone("Pacific/Guam"), "GUAM", 14), SOMOA(TimeZone
            .getTimeZone("US/Samoa"), "SOMOA", -1);

    private static final Set<String> NO_DST_ZONES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("GUAM", "SOMOA",
                    "UNIVERSAL COORDINATED TIME")));

    private final TimeZone tz;

    private final String uiName;

    private final int legacyTzCode;

    private BMHTimeZone(TimeZone tz, String uiName, int legacyTzCode) {
        this.tz = tz;
        this.uiName = uiName;
        this.legacyTzCode = legacyTzCode;
    }

    public TimeZone getTz() {
        return tz;
    }

    public String getShortDisplayName() {
        // TODO: do we need to check to see if we are in daylight savings time?
        return this.tz.getDisplayName(
                this.tz.inDaylightTime(Calendar.getInstance().getTime()),
                TimeZone.SHORT);
    }

    public static BMHTimeZone getTimeZoneByID(final String id) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if (tz.getTz().getID().equals(id)) {
                return tz;
            }
        }

        throw new IllegalArgumentException("No BMHTimeZone enum value for id: "
                + id);
    }

    public String getUiName() {
        return uiName;
    }

    public int getLegacyTzCode() {
        return legacyTzCode;
    }

    /**
     * Get the list of unique selections for time zones for the UI.
     * 
     * @return The set of unique UI time zone names returned as a String array.
     */
    public static String[] getUISelections() {
        LinkedHashSet<String> uniqueValues = new LinkedHashSet<>();
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            uniqueValues.add(tz.getUiName());
        }

        return uniqueValues.toArray(new String[uniqueValues.size()]);
    }

    /**
     * Takes the UI selections of a time zone name and whether or not that time
     * zone observes daylight savings time and returns the {@code TimeZone}
     * instance that corresponds to those selections.
     * 
     * @param uiName
     *            The UI name of the time zone desired.
     * @param observesDST
     *            Whether or not this time zone observes DST.
     * @return the {@code TimeZone} instance that corresponds to the specified
     *         UI selections.
     */
    public static TimeZone getTimeZoneFromUI(String uiName, boolean observesDST) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if ((uiName.equalsIgnoreCase(tz.getUiName()))
                    && (observesDST == tz.getTz().observesDaylightTime())) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for uiName: " + uiName
                        + ", observesDST: " + observesDST);
    }

    /**
     * Returns the corresponding {@code TimeZone} based on the specified legacy
     * system time zone code.
     * 
     * @param legacyCode
     *            The legacy time zone code from the CRS configuration.
     * @return The {@code TimeZone} instance that maps to the specified code
     *         value.
     */
    public static TimeZone getLegacyTimeZone(int legacyCode) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if (legacyCode == tz.getLegacyTzCode()) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for legacyCode: " + legacyCode);
    }

    /**
     * Retrieves the proper {@code TimeZone} instance based on the specified
     * time zone and whether that time zone should observe daylight savings
     * time.
     * 
     * @param baseTZ
     *            The "base" time zone. Uses the raw offset to help map to the
     *            proper time zone.
     * @param observesDST
     *            Whether or not the time zone observes DST.
     * @return The {@code TimeZone} that corresponds to the specified
     *         parameters.
     */
    public static TimeZone getTimeZone(TimeZone baseTZ, boolean observesDST) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if ((baseTZ.getRawOffset() == tz.getTz().getRawOffset())
                    && (observesDST == tz.getTz().observesDaylightTime())) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for baseTZ: " + baseTZ.getID()
                        + ", observesDST: " + observesDST);
    }

    /**
     * Returns the UI name for the specified {@code TimeZone}.
     * 
     * @param tz
     *            The {@code TimeZone} to retrieve the UI name for.
     * @return The zone's UI name. Multiple time zones may have the same UI
     *         name.
     */
    public static String getTimeZoneUIName(TimeZone tz) {
        for (BMHTimeZone zone : BMHTimeZone.values()) {
            if (tz.equals(zone.getTz())) {
                return zone.getUiName();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for time zone: " + tz.getID());
    }

    /**
     * Returns whether or not the specified UI time zone name is forced to
     * always disable daylight savings time.
     * 
     * @param uiTzName
     *            The UI name of the time zone.
     * @return True, if this time zone never observes DST. False, if it can.
     */
    public static boolean isForcedNoDst(String uiTzName) {
        return NO_DST_ZONES.contains(uiTzName);
    }
}
