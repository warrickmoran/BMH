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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.raytheon.uf.common.bmh.StaticMessageIdentifier;
import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
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
 * Feb 10, 2015 4085       bkowal      Refactor into common
 *                                     {@link StaticMessageIdentifier}.
 * Mar 13, 2015 4213       bkowal      Recognize {@link StaticMessageType}s.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageIdentifierUtil extends StaticMessageIdentifier {

    public static final String TIME_PLACEHOLDER = "[__TIME__]";

    public static TIME_MSG_TOKENS[] timeContentFormat = new TIME_MSG_TOKENS[] {
            TIME_MSG_TOKENS.HOUR, TIME_MSG_TOKENS.MINUTE,
            TIME_MSG_TOKENS.PERIOD, TIME_MSG_TOKENS.TIME_ZONE };

    /**
     * 
     */
    protected StaticMessageIdentifierUtil() {
    }

    public static String getText(StaticMessageType staticMsgType) {
        final Designation mtDesignation = staticMsgType.getMsgTypeSummary()
                .getDesignation();
        if (mtDesignation == Designation.StationID) {
            return staticMsgType.getTextMsg1();
        } else if (mtDesignation == Designation.TimeAnnouncement) {
            List<TimeTextFragment> timeMsgFragments = getTimeMsgFragments(staticMsgType);
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
            StaticMessageType staticMsgType) {
        List<TimeTextFragment> timeMsgFragments = new LinkedList<>();
        String textMsg1 = staticMsgType.getTextMsg1();
        textMsg1 = (textMsg1 == null) ? null : textMsg1.trim();
        if (textMsg1 != null && textMsg1.isEmpty() == false) {
            timeMsgFragments.add(new TimeTextFragment(textMsg1));
        }
        timeMsgFragments.add(TimeTextFragment.constructPlaceHolderFragment());
        String textMsg2 = staticMsgType.getTextMsg2();
        textMsg2 = (textMsg2 == null) ? null : textMsg2.trim();
        if (textMsg2 != null && textMsg2.isEmpty() == false) {
            timeMsgFragments.add(new TimeTextFragment(textMsg2));
        }

        /*
         * there must be at least two fragments; otherwise, we are only left
         * with a fragment consisting of just the time, itself.
         */
        if (timeMsgFragments.size() < 2) {
            return Collections.emptyList();
        }

        return timeMsgFragments;
    }
}