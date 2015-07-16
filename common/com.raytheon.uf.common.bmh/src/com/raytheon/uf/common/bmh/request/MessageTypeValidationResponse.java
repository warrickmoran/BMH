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

import java.util.HashSet;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for Message Type validation. Returns the {@link Set} of valid
 * {@link MessageType}s associated with valid afos ids. Identifies the invalid
 * afos ids and the afos ids that do not have an associated {@link MessageType}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2015 4572       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageTypeValidationResponse {

    /**
     * All of the {@link MessageType}s that were found and successfully
     * retrieved.
     */
    @DynamicSerializeElement
    private Set<MessageTypeSummary> messageTypes;

    /**
     * A {@link Set} of all afos ids that were determined to be invalid (ex: too
     * long or short, do not match afos id the pattern, etc.)
     */
    @DynamicSerializeElement
    private Set<String> invalidAfosIds;

    /**
     * The afos ids that an associated {@link MessageType} was not found for.
     */
    @DynamicSerializeElement
    private Set<String> missingAfosIds;

    /**
     * Constructor.
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageTypeValidationResponse() {
    }

    public void addInvalidAfosId(String afosId) {
        if (this.invalidAfosIds == null) {
            this.invalidAfosIds = new HashSet<>();
        }
        this.invalidAfosIds.add(afosId);
    }

    /**
     * @return the messageTypes
     */
    public Set<MessageTypeSummary> getMessageTypes() {
        return messageTypes;
    }

    /**
     * @param messageTypes
     *            the messageTypes to set
     */
    public void setMessageTypes(Set<MessageTypeSummary> messageTypes) {
        this.messageTypes = messageTypes;
    }

    /**
     * @return the invalidAfosIds
     */
    public Set<String> getInvalidAfosIds() {
        return invalidAfosIds;
    }

    /**
     * @param invalidAfosIds
     *            the invalidAfosIds to set
     */
    public void setInvalidAfosIds(Set<String> invalidAfosIds) {
        this.invalidAfosIds = invalidAfosIds;
    }

    /**
     * @return the missingAfosIds
     */
    public Set<String> getMissingAfosIds() {
        return missingAfosIds;
    }

    /**
     * @param missingAfosIds
     *            the missingAfosIds to set
     */
    public void setMissingAfosIds(Set<String> missingAfosIds) {
        this.missingAfosIds = missingAfosIds;
    }
}