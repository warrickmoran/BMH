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
package com.raytheon.uf.edex.bmh.dactransmit;

import java.util.Map;
import java.util.HashMap;

/**
 * Enum defining the modes that a dac session can operate within.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2014  3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum DAC_MODE {
    OPERATIONAL("OP"), MAINTENANCE("MA");

    private static final Map<String, DAC_MODE> lookupMap;

    static {
        lookupMap = new HashMap<>(DAC_MODE.values().length, 1.0f);
        for (DAC_MODE dacMode : DAC_MODE.values()) {
            lookupMap.put(dacMode.getArg(), dacMode);
        }
    }

    private String arg;

    private DAC_MODE(String arg) {
        this.arg = arg;
    }

    public String getArg() {
        return this.arg;
    }

    public static DAC_MODE lookupMode(final String arg) {
        if (lookupMap.containsKey(arg) == false) {
            throw new IllegalArgumentException(arg
                    + " is not recognized as a valid dac mode!");
        }
        return lookupMap.get(arg);
    }
}