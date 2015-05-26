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

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link TransmitterLanguage} Request Object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 29, 2014  3568     bkowal      Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Jan 19, 2015  4011     bkowal      Added 
 *                                    {@link TransmitterLanguageRequestAction#DeleteTransmitterLanguage}.
 * Mar 13, 2015  4213     bkowal      Added fields to support saving and deleting static message types.
 * Apr 28, 2015  4248     bkowal      Added {@link TransmitterLanguageRequestAction#ValidateStaticMsgType} and
 *                                    {@link #language}.
 * May 11, 2015  4476     bkowal      Removed deprecated fields.
 * May 22, 2015  4481     bkowal      Added {@link TransmitterLanguageRequestAction#GetStaticMsgTypeForTransmitterGrpAndAfosId}
 *                                    and {@link #afosId}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterLanguageRequest extends AbstractBMHServerRequest {
    /*
     * Additional enums will be required dependent on how the
     * yet-to-be-implemented dialog
     */
    public enum TransmitterLanguageRequestAction {
        GetTransmitterLanguagesForTransmitterGrp, UpdateTransmitterLanguage, DeleteTransmitterLanguage, ValidateStaticMsgType,

        GetStaticMsgTypeForTransmitterGrpAndAfosId
    }

    @DynamicSerializeElement
    private TransmitterLanguageRequestAction action;

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @DynamicSerializeElement
    private TransmitterLanguage transmitterLanguage;

    @DynamicSerializeElement
    private StaticMessageType staticMsgType;

    @DynamicSerializeElement
    private Language language;

    @DynamicSerializeElement
    private String afosId;

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
     * @param action
     *            the action to set
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
     * @param transmitterGroup
     *            the transmitterGroup to set
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
     * @param transmitterLanguage
     *            the transmitterLanguage to set
     */
    public void setTransmitterLanguage(TransmitterLanguage transmitterLanguage) {
        this.transmitterLanguage = transmitterLanguage;
    }

    /**
     * @return the staticMsgType
     */
    public StaticMessageType getStaticMsgType() {
        return staticMsgType;
    }

    /**
     * @param staticMsgType
     *            the staticMsgType to set
     */
    public void setStaticMsgType(StaticMessageType staticMsgType) {
        this.staticMsgType = staticMsgType;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the afosId
     */
    public String getAfosId() {
        return afosId;
    }

    /**
     * @param afosId
     *            the afosId to set
     */
    public void setAfosId(String afosId) {
        this.afosId = afosId;
    }
}