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

import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.IInputTextValidator;

/**
 * Target Decibel Value validator. Checks the target decibel level to make sure
 * it's in the appropriate range.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2014   3630     mpduff      Initial creation.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DecibelLevelValidator implements IInputTextValidator {

    @Override
    public boolean validateInputText(Shell shell, String text) {
        // Validate that the text is a number between -100 and 100 inclusive
        boolean valid = true;
        if (text == null || text.isEmpty()) {
            valid = false;
        }

        if (text.matches("[-+]?\\d*(\\.\\d+)?")) {
            try {
                float value = Float.parseFloat(text);
                if (value < -100 || value > 100) {
                    valid = false;
                }
            } catch (NumberFormatException e) {
                valid = false;
            }
        } else {
            valid = false;
        }

        if (!valid) {
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Invalid Value",
                    "Please enter a target decibel value between -100 and 100");
        }

        return valid;
    }
}
