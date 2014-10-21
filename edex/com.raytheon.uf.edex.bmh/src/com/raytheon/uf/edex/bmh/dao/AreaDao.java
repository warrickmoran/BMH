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

import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;

/**
 * 
 * DAO for {@link Area} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 25, 2014  3283     bsteffen    Initial creation
 * Jul 17, 2014  3406     mpduff      Added getAllAreas()
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class AreaDao extends AbstractBMHDao<Area, Integer> {

    public AreaDao() {
        super(Area.class);
    }

    public AreaDao(boolean operational) {
        super(operational, Area.class);
    }

    /**
     * Looks up the Area for the given areaCode.
     * 
     * @param areaCode
     * @return
     */
    public Area getByAreaCode(final String areaCode) {
        @SuppressWarnings("unchecked")
        List<Area> types = (List<Area>) findByNamedQueryAndNamedParam(
                Area.GET_AREA_FOR_CODE,
                new String[] { "areaCode" },
                new Object[] { areaCode });
        if ((types != null) && !types.isEmpty()) {
            return types.get(0);
        }

        return null;
    }

    public List<Area> getAllAreas() {
        List<Area> areaList = this.loadAll();
        if (areaList == null) {
            return Collections.emptyList();
        }
        return areaList;
    }
}
