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

/**
 * Indicates that messages in a playlist could not be re-ordered based on mrd
 * follows rules due to the inability to move a message and dependencies without
 * violating an existing mrd follows rule.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 13, 2015 4484       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ImpossibleFollowsException extends Exception {

    private static final long serialVersionUID = -1415482571884461058L;

    /**
     * @param message
     */
    public ImpossibleFollowsException(final long follower, final long follows) {
        super(buildImpossibleExceptionText(follower, follows));
    }

    private static final String buildImpossibleExceptionText(
            final long follower, final long follows) {
        /*
         * TODO: implement a way to find and provide additional information
         * about the dependency that prevents the follow from happening.
         */
        StringBuilder sb = new StringBuilder("Message ");
        sb.append(follower).append(" cannot follow Message ").append(follows)
                .append(".");

        return sb.toString();
    }
}