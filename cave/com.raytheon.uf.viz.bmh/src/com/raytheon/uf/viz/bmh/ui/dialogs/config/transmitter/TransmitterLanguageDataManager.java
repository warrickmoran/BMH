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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.request.StaticMsgValidationResult;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageRequest;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageRequest.TransmitterLanguageRequestAction;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * Data manager for the {@link TransmitterLanguage} data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 13, 2015 3809       bkowal      Initial creation
 * Jan 19, 2015 4011       bkowal      Added {@link #deleteLanguage(TransmitterLanguage)}.
 * Mar 12, 2015 4213       bkowal      Added methods to save / update static message types.
 * Apr 28, 2015 4248       bkowal      Added {@link #validateStaticMessageType(StaticMessageType, Language)}.
 * May 11, 2015 4476       bkowal      Removed methods deprecated by positional ordering of
 *                                     static message types.
 * Jul 06, 2015 4603       bkowal      {@link #validateStaticMessageType(StaticMessageType, Language)} now
 *                                     returns a {@link StaticMsgValidationResult}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterLanguageDataManager {
    /**
     * Returns a {@link List} of {@link TransmitterLanguage}s associated with
     * the specified {@link TransmitterGroup}.
     * 
     * @param transmitterGroup
     *            the specified {@link TransmitterGroup}
     * @return a {@link List} of {@link TransmitterLanguage}s
     */
    public List<TransmitterLanguage> getLanguagesForGroup(
            TransmitterGroup transmitterGroup) throws Exception {
        TransmitterLanguageRequest request = new TransmitterLanguageRequest();
        request.setAction(TransmitterLanguageRequestAction.GetTransmitterLanguagesForTransmitterGrp);
        request.setTransmitterGroup(transmitterGroup);

        TransmitterLanguageResponse response = (TransmitterLanguageResponse) BmhUtils
                .sendRequest(request);
        if (response.getTransmitterLanguages() == null) {
            return Collections.emptyList();
        }

        return response.getTransmitterLanguages();
    }

    /**
     * Saves or updates the specified {@link TransmitterLanguage} and returns an
     * updated version of the specified {@link TransmitterLanguage} after it has
     * been persisted to the database.
     * 
     * @param language
     *            the specified {@link TransmitterLanguage}
     * @return an updated version of the specified {@link TransmitterLanguage}
     * @throws Exception
     */
    public TransmitterLanguage saveLanguage(TransmitterLanguage language)
            throws Exception {
        TransmitterLanguageRequest request = new TransmitterLanguageRequest();
        request.setAction(TransmitterLanguageRequestAction.UpdateTransmitterLanguage);
        request.setTransmitterLanguage(language);

        TransmitterLanguageResponse response = (TransmitterLanguageResponse) BmhUtils
                .sendRequest(request);
        return response.getTransmitterLanguages().get(0);
    }

    public void deleteLanguage(TransmitterLanguage language) throws Exception {
        TransmitterLanguageRequest request = new TransmitterLanguageRequest();
        request.setAction(TransmitterLanguageRequestAction.DeleteTransmitterLanguage);
        request.setTransmitterLanguage(language);

        BmhUtils.sendRequest(request);
    }

    public StaticMsgValidationResult validateStaticMessageType(
            StaticMessageType staticMsgType, Language language)
            throws Exception {
        TransmitterLanguageRequest request = new TransmitterLanguageRequest();
        request.setAction(TransmitterLanguageRequestAction.ValidateStaticMsgType);
        request.setLanguage(language);
        request.setStaticMsgType(staticMsgType);

        return (StaticMsgValidationResult) BmhUtils.sendRequest(request);
    }
}