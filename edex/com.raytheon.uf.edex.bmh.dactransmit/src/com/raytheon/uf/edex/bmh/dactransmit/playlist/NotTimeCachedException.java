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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;

/**
 * Used to indicate that expected time data could not be found in the cache.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2014  3642       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class NotTimeCachedException extends Exception {
    private static final long serialVersionUID = 8293457806294164042L;

    /**
     * Constructor
     * 
     * @param token
     *            the {@link TIME_MSG_TOKENS} identifying the expected cache
     *            item
     * @param tokenValue
     *            the identifier associated with the expected cache item
     */
    public NotTimeCachedException(final TIME_MSG_TOKENS token,
            final String tokenValue) {
        super(buildExceptionText(token, tokenValue));
    }

    private static String buildExceptionText(final TIME_MSG_TOKENS token,
            final String tokenValue) {
        StringBuilder exceptionTxt = new StringBuilder(
                "Unable to find cached time audio associated with ");
        exceptionTxt.append(token.getIdentifier());
        exceptionTxt.append(" ");
        exceptionTxt.append(tokenValue);
        exceptionTxt.append("!");

        return exceptionTxt.toString();
    }
}
