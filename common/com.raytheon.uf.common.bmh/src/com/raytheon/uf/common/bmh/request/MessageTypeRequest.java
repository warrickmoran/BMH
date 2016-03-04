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
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message Type data request object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 22, 2014  3411     mpduff      Initial creation
 * Aug 05, 2014  3490     lvenable    Updated action
 * Aug 14, 2014  3432     mpduff      Added Afosid
 * Aug 17, 2014  3490     lvenable    Updated action and added messageType.
 * Sep 15, 2014  3610     lvenable    Added GetAfosIdTitle.
 * Sep 19, 2014  3611     lvenable    Added emergency override.
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Oct 23, 2014  3728     lvenable    Added GetAfosDesignation and designation variable.
 * Mar 13, 2015  4213     bkowal      Added {@link MessageTypeAction#GetByDesignationAndLanguage}
 *                                    and {@link #language}.
 * Apr 22, 2015  4397     bkowal      Extend {@link AbstractBMHSystemConfigRequest}.
 * Jan 27, 2016  5160     rjpeter     Added {@link MessageTypeAction#GetDemoMsgAfosIds};
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class MessageTypeRequest extends AbstractBMHSystemConfigRequest {
    public enum MessageTypeAction {
        AllMessageTypes, Delete, Save, GetByAfosId, GetByPkId, GetAfosIdTitle, GetEmergencyOverrideMsgTypes, GetAfosDesignation, GetByDesignationAndLanguage, GetDemoMsgAfosIds;
    }

    @DynamicSerializeElement
    private MessageTypeAction action;

    @DynamicSerializeElement
    private MessageType messageType;

    @DynamicSerializeElement
    private String afosId;

    @DynamicSerializeElement
    private long pkId;

    @DynamicSerializeElement
    private boolean emergencyOverride;

    @DynamicSerializeElement
    private Designation designation;

    @DynamicSerializeElement
    private Language language;

    /**
     * @return the action
     */
    public MessageTypeAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(MessageTypeAction action) {
        this.action = action;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
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

    /**
     * @return the pkId
     */
    public long getPkId() {
        return pkId;
    }

    /**
     * @param pkId
     *            the pkId to set
     */
    public void setPkId(long pkId) {
        this.pkId = pkId;
    }

    public boolean isEmergencyOverride() {
        return emergencyOverride;
    }

    public void setEmergencyOverride(boolean emergencyOverride) {
        this.emergencyOverride = emergencyOverride;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.request.AbstractBMHSystemConfigRequest#
     * isSystemConfigChange()
     */
    @Override
    public boolean isSystemConfigChange() {
        return (this.action == MessageTypeAction.Delete)
                || (this.action == MessageTypeAction.Save);
    }
}
