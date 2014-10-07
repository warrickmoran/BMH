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

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object to administratively override the currently playing Suite for
 * the selected Transmitter Group.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 25, 2014  3589     dgilling    Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public final class ForceSuiteChangeRequest extends AbstractBMHServerRequest {

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @DynamicSerializeElement
    private Suite selectedSuite;

    public ForceSuiteChangeRequest() {
        // for serialization only
    }

    public ForceSuiteChangeRequest(TransmitterGroup group, Suite suite) {
        this.transmitterGroup = group;
        this.selectedSuite = suite;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ForceSuiteChangeRequest [transmitterGroup=");
        builder.append(transmitterGroup.getName());
        builder.append(", selectedSuite=");
        builder.append(selectedSuite.getName());
        builder.append("]");
        return builder.toString();
    }

    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public Suite getSelectedSuite() {
        return selectedSuite;
    }

    public void setSelectedSuite(Suite selectedSuite) {
        this.selectedSuite = selectedSuite;
    }
}
