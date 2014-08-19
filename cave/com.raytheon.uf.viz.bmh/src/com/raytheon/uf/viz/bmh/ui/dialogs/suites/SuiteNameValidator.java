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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;

/**
 * Validator to validate the suite name.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014  #3490     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class SuiteNameValidator implements IInputTextValidator {

    /**
     * Set of existing names.
     */
    private Set<String> existingNames = null;

    /**
     * Constructor.
     */
    public SuiteNameValidator() {

    }

    /**
     * Constructor.
     * 
     * @param existingNames
     *            Existing names to check against.
     */
    public SuiteNameValidator(Set<String> existingNames) {
        this.existingNames = existingNames;
    }

    @Override
    public boolean validateInputText(Shell parentShell, String text) {

        if (text.matches("[\\sA-Za-z0-9._-]+") == false) {
            StringBuilder sb = new StringBuilder();

            sb.append("The Suite name must be at least one character, be aplhanumeric, and can ");
            sb.append("contain blank spaces, periods, dashes, or underscores.");

            DialogUtility.showMessageBox(parentShell,
                    SWT.ICON_WARNING | SWT.OK, "Invalid Name", sb.toString());

            return false;
        }

        if (existingNames != null && existingNames.contains(text)) {
            StringBuilder sb = new StringBuilder();

            sb.append("The Suite name already exists.  Please enter another name.");

            DialogUtility.showMessageBox(parentShell,
                    SWT.ICON_WARNING | SWT.OK, "Invalid Name", sb.toString());

            return false;
        }

        return true;
    }
}
