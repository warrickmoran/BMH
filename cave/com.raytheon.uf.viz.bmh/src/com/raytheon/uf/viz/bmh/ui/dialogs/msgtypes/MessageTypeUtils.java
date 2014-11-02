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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;

/**
 * Common {@link MessageType} utilities
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014   3411     mpduff      Initial creation
 * Nov 02, 2014   3783     lvenable    Changed to use set of AFOS Ids.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class MessageTypeUtils {

    /**
     * Validate that the afosId
     * 
     * @param afosId
     *            The afosId to validate
     * @return true if valid, false if not
     */
    public static boolean validateAfosId(String afosId) {
        afosId = afosId.trim();

        if (!afosId.matches("[A-Z0-9]{7,9}")) {
            return false;
        }

        return true;
    }

    /**
     * Check if the provided message name is unique in relation to the provided
     * list of {@link MessageType}s
     * 
     * @param messageName
     *            The message name to check
     * @param messageTypeList
     *            List of messages to compare against
     * @return true if unique, false if not
     */
    public static boolean isUnique(String messageName,
            Set<String> messageTypeAfosIds) {

        if (messageTypeAfosIds.contains(messageName)) {
            return false;
        }

        return true;
    }
}
