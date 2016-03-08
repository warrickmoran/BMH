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

import com.raytheon.uf.common.bmh.dac.DacConfigEvent;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response POJO for DAC Configuration actions.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2015 5113       bkowal      Initial creation
 *
 * </pre>
 *
 * @author bkowal
 * @version 1.0	
 */
@DynamicSerialize
public class DacConfigResponse {
    
    @DynamicSerializeElement
    private boolean success;
    
    @DynamicSerializeElement
    private List<DacConfigEvent> events;
    
    @DynamicSerializeElement
    private Dac dac;

    public DacConfigResponse() {
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the events
     */
    public List<DacConfigEvent> getEvents() {
        return events;
    }

    /**
     * @param events the events to set
     */
    public void setEvents(List<DacConfigEvent> events) {
        this.events = events;
    }

    /**
     * @return the dac
     */
    public Dac getDac() {
        return dac;
    }

    /**
     * @param dac the dac to set
     */
    public void setDac(Dac dac) {
        this.dac = dac;
    }
}