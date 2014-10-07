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

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;

/**
 * 
 * Data access Object for {@link Dac}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  3486     bsteffen    Initial creation
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacDao extends AbstractBMHDao<Dac, String> {

    public DacDao() {
        super(Dac.class);
    }

    public DacDao(boolean operational) {
        super(operational, Dac.class);
    }

}
