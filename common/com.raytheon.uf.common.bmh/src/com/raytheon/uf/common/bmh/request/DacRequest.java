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

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Dac request object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 27, 2014  3173     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Oct 19, 2014  3699     mpduff      Added Dac object
 * Nov 09, 2015  5113     bkowal      Added {@link DacRequestAction#ValidateUnique}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class DacRequest extends AbstractBMHServerRequest {
    public enum DacRequestAction {
        GetAllDacs, DeleteDac, SaveDac, ValidateUnique
    }

    @DynamicSerializeElement
    private DacRequestAction action;

    @DynamicSerializeElement
    private Dac dac;

    /**
     * @return the action
     */
    public DacRequestAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(DacRequestAction action) {
        this.action = action;
    }

    /**
     * @return the dac
     */
    public Dac getDac() {
        return dac;
    }

    /**
     * @param dac
     *            the dac to set
     */
    public void setDac(Dac dac) {
        this.dac = dac;
    }
}
