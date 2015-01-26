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
 * Convert a state, territory, or marine area abbreviation into a number for
 * encoding in a SAME area. The abbreviation comes from the first 2 characters
 * of a UGC. This is the definition of the numerical value from the SAME
 * documentation:
 * 
 * <pre>
 * The State, Territory and Offshore (Marine Area) portion (SS) of the
 * Geographical Area header code block is the number associated with the state,
 * territory, or offshore areas as defined by the Federal Communication
 * Commission (FCC) Report and Order released February 26, 2002. The
 * authoritative source of state and territory codes to be used in this field
 * is “FEDERAL INFORMATION PROCESSING STANDARD (FIPS) 6-4, COUNTIES AND
 * EQUIVALENT ENTITIES OF THE UNITED STATES, ITS POSSESSIONS, AND ASSOCIATED
 * AREAS”, dated 31 Aug 1990, incorporating all current Change Notices [refer
 * to: http://www.itl.nist.gov/fipspubs/fip6-4.htm ]. Refer to the following
 * Internet URL for the listing of Marine Area “SS” codes–
 * http://www.nws.noaa.gov/geodata/catalog/wsom/html/marinenwreas.htm
 * 
 * The corresponding files are available in the table row titled "Coastal &
 * Offshore Marine Area & Zone Codes, including Marine Synopses, for NWR (NOAA
 * Weather Radio)". Click on the "Download Compressed Files" link to view or
 * retrieve the most recent data set.
 * </pre>
 * 
 * The default implementation contains a hard coded mapping so it can be loaded
 * without any access to specific config files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2014  3285     bsteffen    Initial creation
 * Jan 26, 2015  3359     bsteffen    Switch to interface with default implementation.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public interface SAMEStateCodes {

    public Integer getStateCode(String state);

    public static final SAMEStateCodes DEFAULT = new SAMEStateCodes() {

        private final Map<String, Integer> stateCodes = new HashMap<String, Integer>(
                128);
        {
            stateCodes.put("AL", 1);
            stateCodes.put("AK", 2);
            stateCodes.put("AZ", 4);
            stateCodes.put("AR", 5);
            stateCodes.put("CA", 6);
            stateCodes.put("CO", 8);
            stateCodes.put("CT", 9);
            stateCodes.put("DE", 10);
            stateCodes.put("DC", 11);
            stateCodes.put("FL", 12);
            stateCodes.put("GA", 13);
            stateCodes.put("HI", 15);
            stateCodes.put("ID", 16);
            stateCodes.put("IL", 17);
            stateCodes.put("IN", 18);
            stateCodes.put("IA", 19);
            stateCodes.put("KS", 20);
            stateCodes.put("KY", 21);
            stateCodes.put("LA", 22);
            stateCodes.put("ME", 23);
            stateCodes.put("MD", 24);
            stateCodes.put("MA", 25);
            stateCodes.put("MI", 26);
            stateCodes.put("MN", 27);
            stateCodes.put("MS", 28);
            stateCodes.put("MO", 29);
            stateCodes.put("MT", 30);
            stateCodes.put("NE", 31);
            stateCodes.put("NV", 32);
            stateCodes.put("NH", 33);
            stateCodes.put("NJ", 34);
            stateCodes.put("NM", 35);
            stateCodes.put("NY", 36);
            stateCodes.put("NC", 37);
            stateCodes.put("ND", 38);
            stateCodes.put("OH", 39);
            stateCodes.put("OK", 40);
            stateCodes.put("OR", 41);
            stateCodes.put("PA", 42);
            stateCodes.put("RI", 44);
            stateCodes.put("SC", 45);
            stateCodes.put("SD", 46);
            stateCodes.put("TN", 47);
            stateCodes.put("TX", 48);
            stateCodes.put("UT", 49);
            stateCodes.put("VT", 50);
            stateCodes.put("VA", 51);
            stateCodes.put("WA", 53);
            stateCodes.put("WV", 54);
            stateCodes.put("WI", 55);
            stateCodes.put("WY", 56);
            stateCodes.put("PZ", 57);
            stateCodes.put("PK", 58);
            stateCodes.put("PH", 59);
            stateCodes.put("PS", 61);
            stateCodes.put("PM", 65);
            stateCodes.put("GU", 66);
            stateCodes.put("MP", 69);
            stateCodes.put("PR", 72);
            stateCodes.put("AN", 73);
            stateCodes.put("AM", 75);
            stateCodes.put("GM", 77);
            stateCodes.put("VI", 78);
            stateCodes.put("LS", 91);
            stateCodes.put("LM", 92);
            stateCodes.put("LH", 93);
            stateCodes.put("LC", 94);
            stateCodes.put("LE", 96);
            stateCodes.put("LO", 97);
            stateCodes.put("SL", 98);
    }

        @Override
    public Integer getStateCode(String state) {
        return stateCodes.get(state);
    }

    };
}
