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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.request.DacRequest;
import com.raytheon.uf.common.bmh.request.DacRequest.DacRequestAction;
import com.raytheon.uf.common.bmh.request.DacResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * Data Access Manager for DAC Configuration Dlg.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 18, 2014     3699   mpduff      Initial creation
 * Oct 23, 2014     3687   bsteffen    add methods to get name and id.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DacDataManager {

    /**
     * Get all {@link Dac}s
     * 
     * @return List of Dacs
     * @throws Exception
     */
    public List<Dac> getDacs() throws Exception {
        DacRequest request = new DacRequest();
        request.setAction(DacRequestAction.GetAllDacs);

        DacResponse response = (DacResponse) BmhUtils.sendRequest(request);

        return response.getDacList();
    }

    public Integer getDacIdByName(String name) throws Exception {
        if (name == null) {
            return null;
        }
        /* TODO some sort of short term caching */
        for (Dac dac : getDacs()) {
            if (dac.getName().equals(name)) {
                return dac.getId();
            }
        }
        return null;
    }

    public String getDacNameById(Integer id) throws Exception {
        if (id == null) {
            return null;
        }
        /* TODO some sort of short term caching */
        for (Dac dac : getDacs()) {
            if (dac.getId() == id) {
                return dac.getName();
            }
        }
        return null;
    }

    /**
     * Save the {@link Dac}
     * 
     * @param dac
     *            The Dac to save
     * @return the saved dac
     * @throws Exception
     */
    public Dac saveDac(Dac dac) throws Exception {
        DacRequest request = new DacRequest();
        request.setAction(DacRequestAction.SaveDac);
        request.setDac(dac);
        DacResponse response = (DacResponse) BmhUtils.sendRequest(request);
        return response.getDacList().get(0);
    }

    /**
     * Delete the {@link Dac}
     * 
     * @param dac
     *            The Dac to delete
     * @throws Exception
     */
    public void deleteDac(Dac dac) throws Exception {
        DacRequest request = new DacRequest();
        request.setAction(DacRequestAction.DeleteDac);
        request.setDac(dac);
        BmhUtils.sendRequest(request);
    }

}
