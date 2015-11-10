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
package com.raytheon.uf.edex.bmh.dac;

import java.util.regex.Pattern;

/**
 * Used to extract current settings from the currently configured DAC that there
 * is not a associated BMH setting for.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2015  5113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class OrionPatterns {

    private static final String IP6ADDR_REGEX = "<input type=\"text\" name=\"(ip6addr)\" SIZE=\"39\" MAXLENGTH=\"39\" value=\"(.+?)\">";

    public static final Pattern ip6addrPattern = Pattern.compile(IP6ADDR_REGEX);

    private static final String PRE6FIX_REGEX = "<input type=\"text\" name=\"(pre6fix)\" SIZE=\"3\" MAXLENGTH=\"3\" value=\"(.+?)\">";

    public static final Pattern pre6fixPattern = Pattern.compile(PRE6FIX_REGEX);

    private static final String TXIPV6ADDR_REGEX = "<input type=\"text\" name=\"(txIpv6Addr)\" SIZE=\"39\" MAXLENGTH=\"39\" value=\"(.+?)\">";

    public static final Pattern txipv6addrPattern = Pattern
            .compile(TXIPV6ADDR_REGEX);

    private static final String MILLIWATT_ON_REGEX = "<input type=\"radio\" name=\"(mw[1-4])\" value=\"(on)\" checked>";

    public static final Pattern milliwattOnPattern = Pattern
            .compile(MILLIWATT_ON_REGEX);

    private static final String MILLIWATT_OFF_REGEX = "<input type=\"radio\" name=\"(mw[1-4])\" value=\"(off)\" checked>";

    public static final Pattern milliwattOffPattern = Pattern
            .compile(MILLIWATT_OFF_REGEX);

    public static final Pattern[] ORION_PARAM_PATTERNS = new Pattern[] {
            ip6addrPattern, pre6fixPattern, txipv6addrPattern,
            milliwattOnPattern, milliwattOffPattern };

    protected OrionPatterns() {
    }
}