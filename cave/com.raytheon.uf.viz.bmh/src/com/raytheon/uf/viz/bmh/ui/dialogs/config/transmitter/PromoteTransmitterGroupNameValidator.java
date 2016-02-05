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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;

/**
 * Extension of the {@link TransmitterGroup} name validator used to validate
 * that a standalone Transmitter Group can be promoted to a standard Transmitter
 * Group.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2016  4997       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PromoteTransmitterGroupNameValidator extends
        InputTextTransmitterGroupNameValidator {

    /**
     * @param currentName
     * @param tg
     */
    public PromoteTransmitterGroupNameValidator(String currentName,
            TransmitterGroup tg) {
        super(currentName, tg);
    }

    @Override
    public boolean validateInputText(Shell shell, String text) {
        if (super.validateInputText(shell, text) == false) {
            return false;
        }

        if (this.currentName.equals(text)) {
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Transmitter Promotion Failed",
                    "An updated name has not been entered.");
            return false;
        }

        return true;
    }
}