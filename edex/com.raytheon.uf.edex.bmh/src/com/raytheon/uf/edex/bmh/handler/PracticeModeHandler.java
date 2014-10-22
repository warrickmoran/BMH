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

import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.PracticeModeConfigNotification;
import com.raytheon.uf.common.bmh.request.PracticeModeRequest;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;

/**
 * 
 * Handle requests to start or stop practice mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 21, 2014  2687     bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PracticeModeHandler extends
        AbstractBMHServerRequestHandler<PracticeModeRequest> {

    @Override
    public Object handleRequest(PracticeModeRequest request) throws Exception {
        if (request.isOperational()) {
            throw new UnsupportedOperationException(
                    "Cannot copy operational db while in operational mode.");
        }
        ConfigChangeType type = null;
        if (request.isRunPracticeMode()) {
            type = ConfigChangeType.Update;
        } else {
            type = ConfigChangeType.Delete;
        }
        BmhMessageProducer
        .sendConfigMessage(new PracticeModeConfigNotification(
                type), false);
        return Boolean.TRUE;
    }


}
