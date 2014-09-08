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

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * {@link TransmitterLanguage} Request Object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 29, 2014 3568       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterLanguageRequest implements IServerRequest {
    /*
     * Additional enums will be required dependent on how the
     * yet-to-be-implemented dialog
     */
    public enum TransmitterLanguageRequestAction {
        GetTransmitterLanguagesForTransmitterGrp, UpdateTransmitterLanguage
    }

    @DynamicSerializeElement
    private TransmitterLanguageRequestAction action;

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @DynamicSerializeElement
    private TransmitterLanguage transmitterLanguage;

    /**
     * 
     */
    public TransmitterLanguageRequest() {
    }

    /**
     * @return the action
     */
    public TransmitterLanguageRequestAction getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(TransmitterLanguageRequestAction action) {
        this.action = action;
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the transmitterLanguage
     */
    public TransmitterLanguage getTransmitterLanguage() {
        return transmitterLanguage;
    }

    /**
     * @param transmitterLanguage the transmitterLanguage to set
     */
    public void setTransmitterLanguage(TransmitterLanguage transmitterLanguage) {
        this.transmitterLanguage = transmitterLanguage;
    }
}