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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to request the location(s) of the maintenance audio files - will also
 * verify that the maintenance audio actually exists.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2014  3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MaintenanceMessageRequest extends AbstractBMHServerRequest {

    /* simplified name for dynamicserialize */
    public static enum AUDIOTYPE {
        TEXT, ALERT, SAME
    }

    @DynamicSerializeElement
    private AUDIOTYPE type;

    /**
     * Empty constructor for dynamicserialize.
     */
    public MaintenanceMessageRequest() {
    }

    public AUDIOTYPE getType() {
        return type;
    }

    public void setType(AUDIOTYPE type) {
        this.type = type;
    }
}