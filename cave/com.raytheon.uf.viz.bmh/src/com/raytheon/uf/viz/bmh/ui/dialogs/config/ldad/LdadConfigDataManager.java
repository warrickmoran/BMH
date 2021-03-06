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
import java.util.Set;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
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
 * Dec 4, 2014     3880    bkowal      Added getSupportedLdadEncodings.
 * Feb 16, 2015    4118    bkowal      Added {@link #convertAudio(BMHAudioFormat, byte[])}.
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

    public Set<BMHAudioFormat> getSupportedLdadEncodings() throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.RetrieveSupportedEncodings);
        LdadConfigResponse response = (LdadConfigResponse) BmhUtils
                .sendRequest(request);
        if (response.getEncodings() == null
                || response.getEncodings().isEmpty()) {
            return null;
        }

        return response.getEncodings();
    }

    /**
     * Converts the specified audio bytes from the specified source audio format
     * to the ulaw audio format.
     * 
     * @param sourceFormat
     *            the specified source audio format
     * @param audioBytes
     *            the specified audio bytes
     * @return the converted audio bytes
     * @throws Exception
     */
    public byte[] convertAudio(BMHAudioFormat sourceFormat, byte[] audioBytes)
            throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.ConvertAudio);
        request.setSourceFormat(sourceFormat);
        request.setAudioBytes(audioBytes);
        LdadConfigResponse response = (LdadConfigResponse) BmhUtils
                .sendRequest(request);

        return response.getConvertedAudio();
    }

    public void deleteLdadConfig(LdadConfig ldadConfig) throws Exception {
        LdadConfigRequest request = new LdadConfigRequest();
        request.setAction(LdadConfigAction.Delete);
        request.setLdadConfig(ldadConfig);

        BmhUtils.sendRequest(request);
    }
}