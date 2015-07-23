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
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

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
 * Jul 22, 2015 4424       bkowal      Improved validation.
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

    public boolean validate(final Transmitter trxToValidate, String text)
            throws Exception {
        /*
         * Verify that a value has been specified.
         */
        if (trxToValidate == null) {
            throw new IllegalArgumentException(
                    "Required argument trxToValidate cannot be NULL.");
        }
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
            Transmitter transmitter = this.dataManager
                    .getTransmitterByMnemonic(text);
            if (transmitter != null
                    && transmitter.equals(trxToValidate) == false) {
                this.message = "The Transmitter Mnemonic must be unique.";
                return false;
            }
        } catch (Exception e) {
            this.message = "Failed to validate the uniqueness of the Transmitter Mnemonic.";
            throw e;
        }

        /*
         * Verify there are not any Transmitter Group naming conflicts.
         */
        TransmitterGroup tg = null;
        try {
            tg = this.dataManager.getTransmitterGroupByName(text);
        } catch (Exception e) {
            this.message = "Failed to verify that the transmitter mnemonic does not conflict with existing transmitter group names.";
            throw e;
        }

        if (tg != null) {
            /*
             * A {@link TransmitterGroup} was found.
             */

            /*
             * Is the transmitter part of the group that was found?
             */
            if (tg.getTransmitters().contains(trxToValidate) == false) {
                /*
                 * Transmitter cannot override the group.
                 */
                this.message = "Transmitter Mnemonic conflicts with an existing Transmitter Group Name. The Transmitter Mnemonic must be unique.";
                return false;
            }
            /*
             * Is there more than one transmitter in the group that was found?
             */
            else if (tg.getTransmitters().size() > 1) {
                this.message = "The Transmitter Mnemonic and the name of the containing Transmitter Group can only be the same when there is one transmitter in the group.";
                return false;
            }
        }

        return true;
    }

    public boolean verifyUniqueAcrossGroups(String mnemonic,
            Transmitter transmitter) {
        return false;
    }

    public String getMessage() {
        return this.message;
    }
}