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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Dac response object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 27, 2014    3173    mpduff      Initial creation
 * Oct 19, 2014    3699    mpduff      Added addDac method.
 * Nov 23, 2015    5113    bkowal      Added {@link #desyncedDacs}.
 * May 09, 2016    5630    rjpeter     Remove DAC Sync.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class DacResponse {

    @DynamicSerializeElement
    private List<Dac> dacList;

    /**
     * @return the dacList
     */
    public List<Dac> getDacList() {
        return dacList;
    }

    /**
     * @param dacList
     *            the dacList to set
     */
    public void setDacList(List<Dac> dacList) {
        this.dacList = dacList;
    }

    public void addDac(Dac dac) {
        if (dacList == null) {
            dacList = new ArrayList<>();
        }

        dacList.add(dac);
    }
}
