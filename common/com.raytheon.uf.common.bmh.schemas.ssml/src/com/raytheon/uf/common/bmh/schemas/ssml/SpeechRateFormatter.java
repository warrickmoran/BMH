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
package com.raytheon.uf.common.bmh.schemas.ssml;

/**
 * SSML Speech Rate Formatter Utility.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2015 4142       bkowal      Initial creation
 * Mar 27, 2015 4314       bkowal      Added constants for the min, max,
 *                                     and range.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SpeechRateFormatter {
    
    public static final int DEFAULT_RATE = 0;
    
    public static final int MIN_RATE = -99;
    
    public static final int MAX_RATE = 99;
    
    /* +1 to include 0  */
    public static final int NUM_RATES = (MAX_RATE * 2) + 1;

    private static final String RATE_POSITIVE_DISPLAY_PREFIX = "+";

    private static final String RATE_DISPLAY_SUFFIX = "%";

    /**
     * Constructor - protected so that this class cannot be instantiated
     */
    protected SpeechRateFormatter() {
    }

    /**
     * Formats the specified rate as a SSML prosody speech rate.
     * 
     * @param rate
     *            the rate to format.
     * @return the formatted rate.
     */
    public static final String formatSpeechRate(final int rate) {
        if (rate < -99 || rate > 99) {
            throw new IllegalArgumentException(
                    "An invalid speech rate has been specified! The speech rate must be between -99 and 99 inclusive.");
        }

        StringBuilder sb = new StringBuilder();
        if (rate >= 0) {
            sb.append(RATE_POSITIVE_DISPLAY_PREFIX);
        }
        sb.append(rate).append(RATE_DISPLAY_SUFFIX);

        return sb.toString();
    }
}