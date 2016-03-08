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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;

/**
 * Validator to validate that a DAC Address has been specified when requested
 * for DAC auto-configuration.
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

public class ConfigureDacAddressValidator implements IInputTextValidator {

    public ConfigureDacAddressValidator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator#
     * validateInputText(org.eclipse.swt.widgets.Shell, java.lang.String)
     */
    @Override
    public boolean validateInputText(Shell shell, String text) {
        if (text == null || text.isEmpty()) {
            DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                    "DAC Configuration",
                    "The DAC IP Address is a required field.");
            return false;
        }

        return true;
    }
}