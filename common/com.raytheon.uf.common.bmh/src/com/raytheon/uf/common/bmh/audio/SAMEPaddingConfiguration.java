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
package com.raytheon.uf.common.bmh.audio;

import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Configurable SAME Padding Configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2016 5912       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 */

@DynamicSerialize
@XmlRootElement(name = SAMEPaddingConfiguration.ROOT_NAME)
public class SAMEPaddingConfiguration {

    protected static final String ROOT_NAME = "samePaddingConfiguration";

    public static final String XML_NAME = ROOT_NAME + ".xml";

    @DynamicSerializeElement
    private int samePadding;

    @DynamicSerializeElement
    private int sameEOMPadding;

    public SAMEPaddingConfiguration() {
    }

    public int getSamePadding() {
        return samePadding;
    }

    public void setSamePadding(int samePadding) {
        this.samePadding = samePadding;
    }

    public int getSameEOMPadding() {
        return sameEOMPadding;
    }

    public void setSameEOMPadding(int sameEoMPadding) {
        this.sameEOMPadding = sameEoMPadding;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SAMEPaddingConfiguration [");
        sb.append("samePadding=").append(samePadding);
        sb.append(", sameEoMPadding=").append(sameEOMPadding);
        sb.append("]");
        return sb.toString();
    }
}