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

import java.util.List;
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link TransmitterLanguage} Response Object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 29, 2014 3568       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class TransmitterLanguageResponse {

    @DynamicSerializeElement
    private List<TransmitterLanguage> transmitterLanguages;

    public void addTransmitterLanguage(TransmitterLanguage transmitterLanguage) {
        if (this.transmitterLanguages == null) {
            this.transmitterLanguages = new ArrayList<>();
        }
        this.transmitterLanguages.add(transmitterLanguage);
    }

    /**
     * @return the transmitterLanguages
     */
    public List<TransmitterLanguage> getTransmitterLanguages() {
        return transmitterLanguages;
    }

    /**
     * @param transmitterLanguages
     *            the transmitterLanguages to set
     */
    public void setTransmitterLanguages(
            List<TransmitterLanguage> transmitterLanguages) {
        this.transmitterLanguages = transmitterLanguages;
    }
}