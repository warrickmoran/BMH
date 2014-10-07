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
package com.raytheon.uf.common.bmh;

import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;

/**
 * Representation of the components that a time message is composed of.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 6, 2014  3642       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum TIME_MSG_TOKENS {
    HOUR("hour", new HourRetriever()), MINUTE("minute", new MinuteRetriever()), PERIOD(
            "period", new PeriodRetriever()), TIME_ZONE("zone",
            new ZoneRetriever());

    private static final Map<String, TIME_MSG_TOKENS> lookupMap;
    static {
        lookupMap = new HashMap<>(values().length);
        for (TIME_MSG_TOKENS token : values()) {
            lookupMap.put(token.getIdentifier(), token);
        }
    }

    private String identifier;

    private ITimeFieldRetriever calFieldRetriever;

    private TIME_MSG_TOKENS(final String identifier,
            ITimeFieldRetriever calFieldRetriever) {
        this.identifier = identifier;
        this.calFieldRetriever = calFieldRetriever;
    }

    public String getTokenValue(Calendar calendar) {
        return this.calFieldRetriever.getTimeField(calendar);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isSkipped(final String value) {
        return this.calFieldRetriever.isSkipped(value);
    }

    public static TIME_MSG_TOKENS lookupToken(String identifier) {
        return lookupMap.get(identifier);
    }
}