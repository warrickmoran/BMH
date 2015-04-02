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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;

/**
 * 
 * A {@link Comparator} for {@link PlaylistMessage}s that puts them in the
 * correct order based off the {@link SuiteMessage}s in a {@link Suite}.
 * Messages with the same type are sorted off of the creation time of the input
 * message. Messages with the same input message creation time are sorted based
 * of input message id, messages with the same input message id are equal.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 08, 2014  3864     bsteffen    Initial creation
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 02, 2015  4228     rjpeter     Updated to call getOrderedSuiteMessages.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BroadcastMsgSuiteOrderComparator implements
        Comparator<BroadcastMsg> {

    private final Map<String, Integer> positionMap;

    public BroadcastMsgSuiteOrderComparator(Suite suite) {
        List<SuiteMessage> suiteMessages = suite.getOrderedSuiteMessages();
        this.positionMap = new HashMap<>((suiteMessages.size() * 3) / 2);
        for (int i = 0; i < suiteMessages.size(); i += 1) {
            positionMap.put(suiteMessages.get(i).getAfosid(), i);
        }
    }

    @Override
    public int compare(BroadcastMsg m1, BroadcastMsg m2) {
        int retVal = Integer.compare(indexOf(m1), indexOf(m2));
        if (retVal == 0) {
            InputMessage i1 = m1.getInputMessage();
            InputMessage i2 = m2.getInputMessage();
            retVal = i1.getCreationTime().compareTo(i2.getCreationTime());
            if (retVal == 0) {
                retVal = Integer.compare(i1.getId(), i2.getId());
            }
        }
        return retVal;
    }

    private int indexOf(BroadcastMsg message) {
        Integer result = positionMap.get(message.getAfosid());
        if (result == null) {
            return -1;
        } else {
            return result;
        }

    }

}
