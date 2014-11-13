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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.request.LdadConfigRequest;
import com.raytheon.uf.common.bmh.request.LdadConfigRequest.LdadConfigAction;
import com.raytheon.uf.common.bmh.request.LdadConfigResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * LDAD Configuration Dialog's data manager class
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2014    3381    mpduff      Initial creation
 * Nov 13, 2014    3803    bkowal      Implemented.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class LdadConfigDataManager {
    public LdadConfig saveLdadConfig(LdadConfig ldadConfig) throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.Save);
        request.setLdadConfig(ldadConfig);

        return ((LdadConfigResponse) BmhUtils.sendRequest(request))
                .getLdadConfigurations().get(0);
    }

    public List<LdadConfig> getExistingConfigurationReferences()
            throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.RetrieveReferences);

        return ((LdadConfigResponse) BmhUtils.sendRequest(request))
                .getLdadConfigurations();
    }

    public LdadConfig getLdadConfig(long id) throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.RetrieveRecord);
        request.setId(id);

        return ((LdadConfigResponse) BmhUtils.sendRequest(request))
                .getLdadConfigurations().get(0);
    }

    public LdadConfig getLdadConfigByName(String name) throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.RetrieveRecordByName);
        request.setName(name);

        LdadConfigResponse response = (LdadConfigResponse) BmhUtils
                .sendRequest(request);
        if (response.getLdadConfigurations() == null
                || response.getLdadConfigurations().isEmpty()) {
            return null;
        }

        return response.getLdadConfigurations().get(0);
    }

    public void deleteLdadConfig(LdadConfig ldadConfig) throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.Delete);
        request.setLdadConfig(ldadConfig);

        BmhUtils.sendRequest(request);
    }
}