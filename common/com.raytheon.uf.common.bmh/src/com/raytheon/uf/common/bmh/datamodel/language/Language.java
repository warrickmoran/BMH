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
package com.raytheon.uf.common.bmh.datamodel.language;

/**
 * Language enum.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Feb 18, 2015 4136       bkowal      Changed the {@link #SPANISH} {@link #identifier} to
 *                                     "SPA" for consistency.
 * Feb 24, 2015 4157       bkowal      Added {@link #isoCode}.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public enum Language {
    // Language Name: 0-English, 1-Spanish, default - English
    ENGLISH("ENG", "en"), SPANISH("SPA", "es");

    public static final int LENGTH = 7;

    // Internal state
    private final String identifier;

    /*
     * Reference: http://www.w3schools.com/tags/ref_language_codes.asp
     */
    private final String isoCode;

    // Constructor
    private Language(String identifier, String isoCode) {
        this.identifier = identifier;
        this.isoCode = isoCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIsoCode() {
        return isoCode;
    }
}
