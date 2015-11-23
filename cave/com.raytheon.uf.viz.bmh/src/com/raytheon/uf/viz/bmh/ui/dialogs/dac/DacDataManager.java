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
import com.raytheon.uf.common.bmh.request.DacConfigRequest;
import com.raytheon.uf.common.bmh.request.DacConfigResponse;
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
 * Nov 7, 2014      3630   bkowal      add method to get entire dac object by id.
 * Nov 09, 2015     5113   bkowal      Added {@link #validateDacUniqueness(Dac)}.
 * Nov 12, 2015     5113   bkowal      Added {@link #configureSaveDac(Dac, boolean, String)}.
 * Nov 23, 2015     5113   bkowal      Added {@link #syncWithDAC(Dac)}.
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
        DacResponse response = this.getDacsAndSyncStatus();
        return response.getDacList();
    }

    public DacResponse getDacsAndSyncStatus() throws Exception {
        DacRequest request = new DacRequest();
        request.setAction(DacRequestAction.GetAllDacs);

        return (DacResponse) BmhUtils.sendRequest(request);
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

    public Dac getDacById(Integer id) throws Exception {
        if (id == null) {
            return null;
        }
        for (Dac dac : getDacs()) {
            if (dac.getId() == id) {
                return dac;
            }
        }
        return null;
    }

    public String getDacNameById(Integer id) throws Exception {
        Dac dac = this.getDacById(id);
        return dac == null ? null : dac.getName();
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
     * Automatically re-configures a DAC associated with the specified
     * {@link Dac} record. The specified {@link Dac} will also be saved.
     * 
     * @param dac
     *            the {@link Dac} to save; provides configuration information to
     *            use to configure a {@link Dac}.
     * @param reboot
     *            boolean flag indicating whether the DAC should be rebooted
     *            post re-configuration
     * @param configAddress
     *            the address of the DAC to configure. Will be the {@link Dac}
     *            address if the associated DAC was previously configured.
     * @throws Exception
     */
    public DacConfigResponse configureSaveDac(Dac dac, final boolean reboot,
            final String configAddress) throws Exception {
        DacConfigRequest request = new DacConfigRequest();
        request.setAction(DacRequestAction.SaveDac);
        request.setDac(dac);
        request.setReboot(reboot);
        request.setConfigAddress(configAddress);
        return (DacConfigResponse) BmhUtils.sendRequest(request);
    }

    /**
     * Updates a {@link Dac} so that it will match the associated DAC. Should be
     * utilized to synchronize a DAC and {@link Dac} when the out-of-sync
     * notifications are received.
     * 
     * @param dac
     *            the {@link Dac} to sync
     * @return the synchronized {@link Dac} if successful as well as an updated
     *         list of de-synced {@link Dac}s.
     * @throws Exception
     */
    public DacConfigResponse syncWithDAC(Dac dac) throws Exception {
        DacConfigRequest request = new DacConfigRequest();
        request.setAction(DacRequestAction.SaveDac);
        request.setDac(dac);
        request.setSync(true);
        return (DacConfigResponse) BmhUtils.sendRequest(request);
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

    public List<Dac> validateDacUniqueness(final Dac dac) throws Exception {
        DacRequest request = new DacRequest();
        request.setAction(DacRequestAction.ValidateUnique);
        request.setDac(dac);
        DacResponse response = (DacResponse) BmhUtils.sendRequest(request);
        return response.getDacList();
    }
}
