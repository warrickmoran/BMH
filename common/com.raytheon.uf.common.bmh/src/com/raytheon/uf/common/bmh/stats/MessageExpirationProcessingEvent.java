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
package com.raytheon.uf.common.bmh.stats;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Stat used to track message expiration processing time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 20, 2015 4397       bkowal      Initial creation
 * Jun 24, 2015 4397       bkowal      Added an empty constructor.
 * Jul 28, 2015 4686       bkowal      Moved statistics to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageExpirationProcessingEvent extends
        AbstractBMHProcessingTimeEvent {

    private static final long serialVersionUID = -8814178589937754712L;

    /**
     * Constructor.
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageExpirationProcessingEvent() {
    }

    /**
     * @param requestTime
     */
    public MessageExpirationProcessingEvent(long requestTime) {
        super(requestTime);
    }
}