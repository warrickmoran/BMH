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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * Transmitter Utility Class
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 8, 2014     3173    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterUtils {
    private final String NEWLINE = System.getProperty("line.separator");

    private final String TAB = "\t";

    /**
     * Get a string showing the transmitter group details
     * 
     * @param tGroup
     *            The group to show details for
     * @return The string of details
     */
    public String getTransmitterGroupDetails(TransmitterGroup tGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("Transmitter Group Name: ").append(tGroup.getName())
                .append(NEWLINE);
        sb.append("DAC #: ").append(tGroup.getDac()).append(NEWLINE);
        sb.append("Time Zone: ").append(tGroup.getTimeZone()).append(NEWLINE);
        sb.append("Disable Silence Alarm: ").append(tGroup.getSilenceAlarm())
                .append(NEWLINE);
        sb.append("No Daylight Savings Time Observed: ")
                .append(tGroup.getDaylightSaving()).append(NEWLINE);

        for (Transmitter t : tGroup.getTransmitters()) {
            sb.append(NEWLINE);
            sb.append(getTransmitterDetails(t, true));
        }

        return sb.toString();
    }

    /**
     * Get a string showing the transmitter details with the transmitter details
     * NOT indented
     * 
     * @param t
     *            The transmitter to show details for
     * @return The string of details
     */
    public String getTransmitterDetails(Transmitter t) {
        return this.getTransmitterDetails(t, false);
    }

    /**
     * Get a string showing the transmitter details with the transmitter details
     * indented if indent param true
     * 
     * @param t
     *            The transmitter to show details for
     * @param indent
     *            true to indent the details, false to not indent
     * @return The string of details
     */
    public String getTransmitterDetails(Transmitter t, boolean indent) {
        StringBuilder sb = new StringBuilder();
        indent(sb, indent);

        sb.append("Transmitter Name: ").append(t.getName()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Transmitter is ").append(t.getTxStatus().name())
                .append(" and ").append(t.getTxMode().name()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Mnemonic: ").append(t.getMnemonic()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Frequency: ").append(t.getFrequency()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Call Sign: ").append(t.getCallSign()).append(NEWLINE);
        indent(sb, indent);

        sb.append("DAC Port#: ").append(t.getDacPort()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Location: ").append(t.getLocation()).append(NEWLINE);
        indent(sb, indent);

        sb.append("Service Area: ").append(t.getServiceArea()).append(NEWLINE);
        indent(sb, indent);

        sb.append("FIPS Code: ").append(t.getFipsCode()).append(NEWLINE);
        indent(sb, indent);

        return sb.toString();
    }

    /**
     * Add a tab to the end of the string if indent param is true
     * 
     * @param sb
     *            The StringBuilder
     * @param indent
     *            true to indent
     * @return StringBuilder with a tab at the end if indent is true
     */
    private StringBuilder indent(StringBuilder sb, boolean indent) {
        if (indent) {
            sb.append(TAB);
        }

        return sb;
    }
}
