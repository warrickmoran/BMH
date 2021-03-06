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

import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;

/**
 * Interface used when suites are selected/update in a table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 6, 2014  #3490      lvenable     Initial creation
 * Aug 15, 2014  #3490     lvenable     Added delete method
 * Aug 18, 2014  #3490     lvenable     Added rename, copy, get names methods.
 * Aug 21, 2014  #3490     lvenable     Updated method args and addedSuites().
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public interface ISuiteSelection {

    /**
     * Action when a suite is selected.
     * 
     * @param suite
     *            The suite that is selected.
     */
    public void suiteSelected(Suite suite);

    /**
     * Action when the suites have been updated.
     */
    public void suitesUpdated(Suite suite);

    /**
     * Action when a suite has been deleted.
     */
    public void deleteSuite(Suite suite);

    /**
     * Action when a suite has been renamed.
     */
    public void renameSuite(Suite suite);

    /**
     * Action when a suite has been copied.
     */
    public void copySuite(Suite suite);

    /**
     * Action when a suite has been copied.
     */
    public void addedSuites(List<Suite> suiteList);

    /**
     * Get the existing suite names.
     */
    public Set<String> getSuiteNames();
}
