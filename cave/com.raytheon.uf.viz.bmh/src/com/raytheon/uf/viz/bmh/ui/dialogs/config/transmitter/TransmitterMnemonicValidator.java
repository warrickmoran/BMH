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

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;

/**
 * Validates the transmitter mnemonic. Verifies that a mnemonic has been
 * specified and that it is unique.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2015 4424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterMnemonicValidator {

    private final TransmitterDataManager dataManager = new TransmitterDataManager();

    private String message = StringUtils.EMPTY;

    /**
     * 
     */
    public TransmitterMnemonicValidator() {
    }

    public boolean validate(String text) throws Exception {
        /*
         * Verify that a value has been specified.
         */
        if (text == null || text.trim().isEmpty()) {
            this.message = "A Transmitter Mnemonic must be specified.";
            return false;
        }
        text = text.trim();

        /*
         * Verify field length.
         */
        if (text.length() > Transmitter.MNEMONIC_LENGTH) {
            this.message = "The Transmitter Mnemonic must be 1 to "
                    + Transmitter.MNEMONIC_LENGTH + " characters in length.";
            return false;
        }

        /*
         * Verify field uniqueness.
         */
        try {
            if (this.dataManager.getTransmitterByMnemonic(text) != null) {
                this.message = "The Transmitter Mnemonic must be unique.";
                return false;
            }
        } catch (Exception e) {
            this.message = "Failed to validate the uniqueness of the Transmitter Mnemonic.";
            throw e;
        }

        return true;
    }

    public String getMessage() {
        return this.message;
    }
}