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

import java.util.LinkedList;
import java.util.List;

import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;

/**
 * Used to identify the different types of static messages the system will need
 * to support.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 29, 2014 3568       bkowal      Initial creation
 * Oct 2, 2014  3642       bkowal      Added support for static time fragments.
 * Nov 13, 2014 3717       bsteffen    Simplify isStaticMsgType
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageIdentifierUtil {

    public static final Designation stationDesignation = Designation.StationID;

    public static final Designation timeDesignation = Designation.TimeAnnouncement;

    public static final String TIME_PLACEHOLDER = "[__TIME__]";

    public static TIME_MSG_TOKENS[] timeContentFormat = new TIME_MSG_TOKENS[] {
            TIME_MSG_TOKENS.HOUR, TIME_MSG_TOKENS.MINUTE, TIME_MSG_TOKENS.PERIOD,
            TIME_MSG_TOKENS.TIME_ZONE };

    /**
     * 
     */
    protected StaticMessageIdentifierUtil() {
    }

    public static boolean isStaticMsgType(MessageType messageType) {
        return messageType.getDesignation().isStatic();
    }

    public static String getText(MessageType mt, TransmitterLanguage tl) {
        if (mt.getDesignation() == Designation.StationID) {
            return tl.getStationIdMsg();
        } else if (mt.getDesignation() == Designation.TimeAnnouncement) {
            List<TimeTextFragment> timeMsgFragments = getTimeMsgFragments(tl);
            StringBuilder timeMsgFullText = new StringBuilder();
            boolean first = true;
            for (TimeTextFragment timeMsgFragment : timeMsgFragments) {
                if (first == false) {
                    timeMsgFullText.append(" "); // prevent run-on words
                }
                timeMsgFullText.append(timeMsgFragment.getText());
                first = false;
            }

            return timeMsgFullText.toString();
        }

        return null;
    }

    /**
     * Constructs and returns a list of fragments that the time message is
     * composed of. The fragment list will always include
     * {@link StaticMessageIdentifierUtil#TIME_PLACEHOLDER} to indicate where
     * the current time should be located within the message.
     * 
     * @param tl
     *            the {@link TransmitterLanguage} that will be used to construct
     *            the time message.
     * @return a list of the message fragments that the time msg is composed of
     */
    public static List<TimeTextFragment> getTimeMsgFragments(
            TransmitterLanguage tl) {
        List<TimeTextFragment> timeMsgFragments = new LinkedList<>();
        timeMsgFragments.add(new TimeTextFragment(tl.getTimeMsgPreamble()
                .trim()));
        timeMsgFragments.add(TimeTextFragment.constructPlaceHolderFragment());
        if (tl.getTimeMsgPostamble() != null
                && tl.getTimeMsgPostamble().isEmpty() == false) {
            timeMsgFragments.add(new TimeTextFragment(tl.getTimeMsgPostamble()
                    .trim()));
        }

        return timeMsgFragments;
    }
}