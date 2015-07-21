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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;

/**
 * Used to validate a name that has been entered into an Eclipse Text widget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2015 4424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InputTextTransmitterGroupNameValidator implements
        IInputTextValidator {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /*
     * Additional abstraction can occur if this validator is ever more widely
     * used.
     */
    private final String currentName;

    /**
     * 
     */
    public InputTextTransmitterGroupNameValidator() {
        this(null);
    }

    public InputTextTransmitterGroupNameValidator(final String currentName) {
        this.currentName = currentName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
     */
    @Override
    public boolean validateInputText(Shell shell, String text) {
        if (this.currentName != null && this.currentName.equals(text)) {
            return true;
        }

        TransmitterGroupNameValidator validator = new TransmitterGroupNameValidator();
        try {
            if (validator.validate(text) == false) {
                DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                        "Name Validation Failed", validator.getMessage());

                return false;
            }
        } catch (Exception e) {
            statusHandler.error(validator.getMessage(), e);
            return false;
        }

        return true;
    }
}