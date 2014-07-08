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
 * The current implementation contains a hard coded mapping, depending on the
 * level of configurability this may be moved to a config file in the future.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2014  3285     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SAMEStateCodes {

    private final Map<String, Integer> stateCodes = getStateCodes();

    private static Map<String, Integer> getStateCodes() {
        Map<String, Integer> result = new HashMap<String, Integer>(128);
        result.put("AL", 1);
        result.put("AK", 2);
        result.put("AZ", 4);
        result.put("AR", 5);
        result.put("CA", 6);
        result.put("CO", 8);
        result.put("CT", 9);
        result.put("DE", 10);
        result.put("DC", 11);
        result.put("FL", 12);
        result.put("GA", 13);
        result.put("HI", 15);
        result.put("ID", 16);
        result.put("IL", 17);
        result.put("IN", 18);
        result.put("IA", 19);
        result.put("KS", 20);
        result.put("KY", 21);
        result.put("LA", 22);
        result.put("ME", 23);
        result.put("MD", 24);
        result.put("MA", 25);
        result.put("MI", 26);
        result.put("MN", 27);
        result.put("MS", 28);
        result.put("MO", 29);
        result.put("MT", 30);
        result.put("NE", 31);
        result.put("NV", 32);
        result.put("NH", 33);
        result.put("NJ", 34);
        result.put("NM", 35);
        result.put("NY", 36);
        result.put("NC", 37);
        result.put("ND", 38);
        result.put("OH", 39);
        result.put("OK", 40);
        result.put("OR", 41);
        result.put("PA", 42);
        result.put("RI", 44);
        result.put("SC", 45);
        result.put("SD", 46);
        result.put("TN", 47);
        result.put("TX", 48);
        result.put("UT", 49);
        result.put("VT", 50);
        result.put("VA", 51);
        result.put("WA", 53);
        result.put("WV", 54);
        result.put("WI", 55);
        result.put("WY", 56);
        result.put("PZ", 57);
        result.put("PK", 58);
        result.put("PH", 59);
        result.put("PS", 61);
        result.put("PM", 65);
        result.put("GU", 66);
        result.put("MP", 69);
        result.put("PR", 72);
        result.put("AN", 73);
        result.put("AM", 75);
        result.put("GM", 77);
        result.put("VI", 78);
        result.put("LS", 91);
        result.put("LM", 92);
        result.put("LH", 93);
        result.put("LC", 94);
        result.put("LE", 96);
        result.put("LO", 97);
        result.put("SL", 98);
        return result;
    }

    public Integer getStateCode(String state) {
        return stateCodes.get(state);
    }
}
