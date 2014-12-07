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
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object for Suite queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 05, 2014  3490     lvenable    Initial creation
 * Aug 12, 2014  3490     lvenable    Added SuitesMsgTypes action.
 * Aug 17, 2014  3490     lvenable    Added suite and getter/setters.
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Dec 07, 2014  3752     mpduff      Add getSuiteByName
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class SuiteRequest extends AbstractBMHServerRequest {

    public enum SuiteAction {
        Save, ListSuitesCats, AllSuites, SuitesMsgTypes, Delete, GetSuiteByName;
    }

    @DynamicSerializeElement
    private SuiteAction action;

    @DynamicSerializeElement
    private Suite suite;

    @DynamicSerializeElement
    private String suiteName;

    /**
     * Get the suite action.
     * 
     * @return The suite action.
     */
    public SuiteAction getAction() {
        return action;
    }

    /**
     * Set the suite action.
     * 
     * @param action
     *            The suite action.
     */
    public void setAction(SuiteAction action) {
        this.action = action;
    }

    public Suite getSuite() {
        return suite;
    }

    public void setSuite(Suite suite) {
        this.suite = suite;
    }

    /**
     * @return the suiteName
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * @param suiteName
     *            the suiteName to set
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }
}
