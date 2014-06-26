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
package com.raytheon.uf.edex.bmh.dao;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.edex.database.DataAccessLayerException;

/**
 * 
 * DAO for {@link InputMessage} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageDao extends AbstractBMHDao<InputMessage, Integer> {

    public InputMessageDao() {
        super(InputMessage.class);
    }

    /**
     * Search the database for any messages which can be considered duplicates
     * of this message.
     * 
     * @param message
     *            InputMessage to find duplicates.
     * @return true if duplicates exist, false otherwise
     * @see InputMessage#equalsExceptId(Object)
     */
    public boolean checkDuplicate(InputMessage message)
            throws DataAccessLayerException {
        // TODO generate a named query.
        return false;
    }

}
