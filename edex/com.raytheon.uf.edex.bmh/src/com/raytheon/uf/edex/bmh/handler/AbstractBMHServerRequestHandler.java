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
package com.raytheon.uf.edex.bmh.handler;

import com.raytheon.uf.common.auth.exception.AuthorizationException;
import com.raytheon.uf.common.auth.user.IUser;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.edex.auth.req.AbstractPrivilegedRequestHandler;
import com.raytheon.uf.edex.auth.resp.AuthorizationResponse;

/**
 * This abstract class implements {@link AbstractPrivilegedRequestHandler}'s
 * authorized method for {@link AbstractBMHServerRequest}. The
 * {@link AbstractPrivilegedRequestHandler}'s handleRequest method must be
 * implemented its sub-classes. Each sub-class should handle a request that is a
 * sub-class of {@link AbstractBMHServerRequest}. The authorized method is a
 * default to allow a handler to obtain the user id for logging purposes. This
 * can be overridden such as in the {@link BmhAuthorizationRequestHandler}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 13, 2014 #3413      rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public abstract class AbstractBMHServerRequestHandler<T extends AbstractBMHServerRequest>
        extends AbstractPrivilegedRequestHandler<T> {

    @Override
    public AuthorizationResponse authorized(IUser user, T request)
            throws AuthorizationException {
        return new AuthorizationResponse(true);
    }
}
