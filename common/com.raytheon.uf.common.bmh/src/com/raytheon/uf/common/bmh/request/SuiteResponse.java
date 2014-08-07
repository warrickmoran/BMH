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

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for {@link Suite} queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2014  #3490      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class SuiteResponse {

    @DynamicSerializeElement
    private List<Suite> suiteList;

    /**
     * Get the list of suites.
     * 
     * @return The list of suites.
     */
    public List<Suite> getSuiteList() {
        return suiteList;
    }

    /**
     * Set the list of suites.
     * 
     * @param suiteList
     *            The list of suites.
     */
    public void setSuiteList(List<Suite> suiteList) {
        this.suiteList = suiteList;
    }
}
