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

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;

/**
 * Used to validate a mnemonic that has been entered into an Eclipse Text
 * widget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2015 4424       bkowal      Initial creation
 * Jul 22, 2015 4424       bkowal      Improved validation.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InputTextTransmitterMnemonicValidator implements
        IInputTextValidator {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /*
     * Additional abstraction can occur if this validator is ever more widely
     * used.
     */
    private final String currentName;

    /*
     * The {@link Transmitter} that is being renamed. Used to determine if there
     * are any naming conflicts with {@link TransmitterGroup}s.
     */
    private final Transmitter transmitter;

    public InputTextTransmitterMnemonicValidator(final String currentName,
            final Transmitter transmitter) {
        this.currentName = currentName;
        this.transmitter = transmitter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator#
     * validateInputText(org.eclipse.swt.widgets.Shell, java.lang.String)
     */
    @Override
    public boolean validateInputText(Shell shell, String text) {
        /*
         * Do not validate the text if it has not changed.
         */
        if (this.currentName != null && this.currentName.equals(text)) {
            return true;
        }

        TransmitterMnemonicValidator validator = new TransmitterMnemonicValidator();
        try {
            if (validator.validate(this.transmitter, text) == false) {
                DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                        "Mnemonic Validation Failed", validator.getMessage());

                return false;
            }
        } catch (Exception e) {
            statusHandler.error(validator.getMessage(), e);
            return false;
        }

        return true;
    }
}