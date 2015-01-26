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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Determine the default originator from an eventCode. The default
 * implementation uses a hardcoded set of civil event codes and all other codes
 * originate from the NWS.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 07, 2014  3285     bsteffen    Initial creation
 * Jan 26, 2015  3359     bsteffen    Switch to interface with default implementation.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public interface SAMEOriginatorMapper {
    
    public String getOriginator(String eventCode);

    public static final SAMEOriginatorMapper DEFAULT = new SAMEOriginatorMapper() {

        private final Set<String> civilianEvents = new HashSet<>(Arrays.asList(
                "ADR", "CAE", "CEM", "EVI", "NIC", "AVA", "AVW", "CDW", "EQW",
                "FRW", "HMW", "LEW", "LAE", "TOE", "NUW", "RHW", "SPW", "VOW",
                "NPT", "NMN"));

        @Override
        public String getOriginator(String eventCode) {
            if (civilianEvents.contains(eventCode)) {
                return SAMEToneTextBuilder.CIVIL_ORIGINATOR;
            } else {
                return SAMEToneTextBuilder.NWS_ORIGINATOR;
            }
        }
    };

}
