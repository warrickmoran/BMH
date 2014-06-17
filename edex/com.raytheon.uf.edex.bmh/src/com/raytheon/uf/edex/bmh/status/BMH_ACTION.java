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
package com.raytheon.uf.edex.bmh.status;

import java.util.Map;
import java.util.HashMap;

/**
 * An enum representing the various actions that can be completed to notify a
 * user that an event has occurred.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014 3291       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum BMH_ACTION {
    /*
     * Take the default action based on priority.
     */
    ACTION_DEFAULT(null),
    /*
     * Log the message.
     */
    ACTION_LOG("LOG"),
    /*
     * Send the message to AlertViz.
     */
    ACTION_ALERTVIZ("AV"),
    /*
     * Send the message to AlertViz and play an audio file.
     */
    ACTION_ALERTVIZ_AUDIO("AV_AUDIO");

    private static final Map<String, BMH_ACTION> lookupMap;

    static {
        lookupMap = new HashMap<String, BMH_ACTION>();

        for (BMH_ACTION bmhAction : BMH_ACTION.values()) {
            if (bmhAction.getIdentifier() != null) {
                lookupMap.put(bmhAction.getIdentifier(), bmhAction);
            }
        }
    }

    private String identifier;

    private BMH_ACTION(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public static BMH_ACTION lookup(String identifier) {
        identifier = identifier.toUpperCase();
        if (lookupMap.containsKey(identifier) == false) {
            return BMH_ACTION.ACTION_DEFAULT;
        }
        return lookupMap.get(identifier);
    }
}