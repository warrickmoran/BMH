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
package com.raytheon.uf.common.bmh.same;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Maps an nws three letter site identifier to an icao for use as a same code
 * originator. The default implementation prepends a 'K' to the siteId except
 * for a few hardcoded exceptions to this rule.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jan 26, 2015  3359     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public interface SAMEIcaoMapper {

    public CharSequence getIcao(String siteId);

    public static final SAMEIcaoMapper DEFAULT = new SAMEIcaoMapper(){

        private final Map<String, String> icaoOverrides = new HashMap<String, String>(
                32);

        {
            icaoOverrides.put("AER", "PAFC");
            icaoOverrides.put("ALU", "PAFC");
            icaoOverrides.put("ACR", "PACR");
            icaoOverrides.put("AFC", "PAFC");
            icaoOverrides.put("AFG", "PAFG");
            icaoOverrides.put("AJK", "PAJK");
            icaoOverrides.put("GUM", "PGUM");
            icaoOverrides.put("HFO", "PHFO");
            icaoOverrides.put("SJU", "TJSJ");
        }

        @Override
        public CharSequence getIcao(String siteId) {
            if (icaoOverrides.containsKey(siteId)) {
                return icaoOverrides.get(siteId);
            }
            StringBuilder result = new StringBuilder(4);
            result.append('K');
            result.append(siteId);
            return result;
        }
    };
}
