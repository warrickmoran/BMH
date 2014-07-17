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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;

/**
 * Dao class for transmitter groups.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jul 17, 2014 3406       mpduff      Added getAllTransmitters()
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class TransmitterDao extends AbstractBMHDao<Transmitter, Integer> {
    public TransmitterDao() {
        super(Transmitter.class);
    }

    public List<Transmitter> getAllTransmitters() {
        List<Object> results = this.loadAll();
        List<Transmitter> tList = new ArrayList<Transmitter>(results.size());
        for (Object o : results) {
            tList.add((Transmitter) o);
        }

        return tList;
    }
}
