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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * HTTP Parameters used to POST to Orion to configure a DAC.
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

public final class OrionPostParams {

    /*
     * Orion Credentials.
     * 
     * OK to store these in source as plaintext? Or do we require some EDEX
     * setup step so that EDEX could read this information from an encrypted
     * configuration file? Note: these credentials will only be used to scrape
     * dac configuration pages to retrieve the default information that the user
     * does not provide - primarily the IPv6 information as well as the Digital
     * Milliwatt information.
     */
    private static final String ORION_USERNAME = "admin";

    private static final String ORION_PASSWORD = "orion1";

    /*
     * Server Endpoints
     */
    public static final String HTTPS = "https://";

    public static final String CGI_BIN = "cgi-bin";

    /*
     * Used to retrieve information about a DAC.
     */
    public static final String INDEX = "index";

    /*
     * Used to configure a DAC.
     */
    public static final String TEST = "test";

    /*
     * Login Params.
     */
    private static final String LOGIN_ACTION = "signon";

    private static final String PARAM_ACTION = "action";

    private static final String PARAM_PASS = "pass";

    private static final List<NameValuePair> loginForm;

    static {
        /*
         * Construct the information that will be used for all existing dac
         * configuration retrieval.
         */
        loginForm = new ArrayList<>(3);
        loginForm.add(new BasicNameValuePair(PARAM_ACTION, LOGIN_ACTION));
        loginForm.add(new BasicNameValuePair(PARAM_ACTION, ORION_USERNAME));
        loginForm.add(new BasicNameValuePair(PARAM_PASS, ORION_PASSWORD));
    }

    /*
     * Configure Params.
     * 
     * These are all of the parameters tracked by BMH.
     */
    public static final String IP_TYPE_IPV4 = "IPv4";

    public static final String REBOOT_YES = "Yes";

    public static final String SUB_SUBMIT = "Submit";

    public static final String PARAM_IPTYPE = "IpType";

    public static final String PARAM_IP_ADDRESS = "ipAddr";

    public static final String PARAM_NETMASK = "netMask";

    public static final String PARAM_GATEWAY = "gateway";

    public static final String PARAM_IP6ADDR = "ip6addr";

    public static final String PARAM_PRE6FIX = "pre6fix";

    public static final String PARAM_JITTER = "Jitter";

    public static final String PARAM_TXIPADDR = "txIpAddr";

    public static final String PARAM_TXIPV6ADDR = "txIpv6Addr";

    public static final String PARAM_TXPORT = "txPort";

    public static final String PARAM_PORT = "port%s";

    public static final String PARAM_LEVEL = "level%s";

    public static final String PARAM_SUB = "sub";

    public static final String PARAM_REBOOT = "Reboot";

    protected OrionPostParams() {
    }

    public static List<NameValuePair> getLogin() {
        return loginForm;
    }
}