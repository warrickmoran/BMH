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

import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;

/**
 * Adapter class that can be used in place of ISuiteSelection. This will allow
 * only the method(s) needed to be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2014 #3490      lvenable     Initial creation
 * Aug 18, 2014 #3490      lvenable     Added methods that were added to the interface.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public abstract class SuiteActionAdapter implements ISuiteSelection {

    @Override
    public void suiteSelected(Suite suite) {
    }

    @Override
    public void suitesUpdated() {
    }

    @Override
    public void deleteSuite(Suite suite) {
    }

    @Override
    public void renameSuite(Suite suite) {

    }

    @Override
    public void copySuite(Suite suite) {

    }

    @Override
    public Set<String> getSuiteNames() {
        return null;
    }
}
