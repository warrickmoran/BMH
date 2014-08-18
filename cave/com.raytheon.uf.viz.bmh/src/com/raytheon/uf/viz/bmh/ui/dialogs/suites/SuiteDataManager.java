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
package com.raytheon.uf.viz.bmh.ui.dialogs.suites;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteRequest.SuiteAction;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * DataManager for the Suite data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 17, 2014 #3490      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class SuiteDataManager {

    /**
     * Get a list of fully populated suites.
     * 
     * @param comparator
     *            Comparator used for sorting, null for no sorting.
     * @return List of suites.
     * @throws Exception
     */
    public List<Suite> getAllSuites(Comparator<Suite> comparator)
            throws Exception {
        List<Suite> suiteList = null;
        SuiteRequest suiteRequest = new SuiteRequest();
        suiteRequest.setAction(SuiteAction.AllSuites);
        SuiteResponse suiteResponse = null;

        suiteResponse = (SuiteResponse) BmhUtils.sendRequest(suiteRequest);
        suiteList = suiteResponse.getSuiteList();

        if (comparator != null && suiteList.isEmpty() == false) {
            Collections.sort(suiteList, comparator);
        }

        return suiteList;
    }

    /**
     * Get a list of suites and associated message types.
     * 
     * @return List of suites and associated message type.
     * @throws Exception
     */
    public List<Suite> getSuitesMsgTypes() throws Exception {
        List<Suite> suiteList = null;

        SuiteRequest suiteRequest = new SuiteRequest();
        suiteRequest.setAction(SuiteAction.SuitesMsgTypes);
        SuiteResponse suiteResponse = null;

        suiteResponse = (SuiteResponse) BmhUtils.sendRequest(suiteRequest);
        suiteList = suiteResponse.getSuiteList();

        return suiteList;
    }

    /**
     * Delete the selected suite.
     * 
     * @param selectedSuite
     *            The suite to be deleted.
     * @throws Exception
     */
    public void deleteSuite(Suite selectedSuite) throws Exception {
        SuiteRequest sr = new SuiteRequest();
        sr.setAction(SuiteAction.Delete);
        sr.setSuite(selectedSuite);

        BmhUtils.sendRequest(sr);
    }
}
