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

import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object used to validate a {@link Set} of afos ids and to retrieve the
 * {@link MessageType}s associated with the valid afos ids.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 22, 2015 4572       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageTypeValidationRequest extends AbstractBMHServerRequest {

    @DynamicSerializeElement
    private Set<String> afosIds;

    /**
     * Constructor.
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageTypeValidationRequest() {
    }

    public MessageTypeValidationRequest(boolean operational) {
        super(operational);
    }

    /**
     * @return the afosIds
     */
    public Set<String> getAfosIds() {
        return afosIds;
    }

    /**
     * @param afosIds
     *            the afosIds to set
     */
    public void setAfosIds(Set<String> afosIds) {
        this.afosIds = afosIds;
    }
}