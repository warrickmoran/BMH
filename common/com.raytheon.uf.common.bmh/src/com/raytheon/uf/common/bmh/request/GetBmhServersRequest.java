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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.localization.msgs.GetServersRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * {@link GetServersRequest} for bmh edex. Bmh edex should not respond to a
 * normal GetServersRequest because then it looks like an ordinary request edex.
 * BMH Edex does need the ability to return the various BMH servers, which can
 * be handled exactly like a GetServersRequest. This request serves that purpose
 * and can be handled by the same request handler that is used on request edex.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class GetBmhServersRequest extends GetServersRequest {
    /* No code is needed */
}
