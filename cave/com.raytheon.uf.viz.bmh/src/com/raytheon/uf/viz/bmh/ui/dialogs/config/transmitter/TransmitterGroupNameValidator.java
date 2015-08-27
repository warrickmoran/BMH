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
 * Validates the transmitter group name. Verifies that a name has been specified
 * and that it is unique.
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

public class TransmitterGroupNameValidator {

    private final TransmitterDataManager dataManager = new TransmitterDataManager();

    private String message = StringUtils.EMPTY;

    /**
     * 
     */
    public TransmitterGroupNameValidator() {
    }

    public boolean validate(final TransmitterGroup tgToValidate, String text)
            throws Exception {
        /*
         * Verify that a value has been specified.
         */
        if (text == null || text.trim().isEmpty()) {
            this.message = "A Transmitter Group Name must be specified.";
            return false;
        }
        text = text.trim();

        if (text.length() > TransmitterGroup.NAME_LENGTH) {
            this.message = "The Transmitter Group Name must be 1 to "
                    + TransmitterGroup.NAME_LENGTH + " characters in length.";
            return false;
        }

        try {
            if (dataManager.getTransmitterGroupByName(text) != null) {
                this.message = "The Transmitter Group Name must be unique.";
                return false;
            }
        } catch (Exception e) {
            this.message = "Failed to validate the uniqueness of the Transmitter Group Name.";
            throw e;
        }

        /*
         * Verify that there are not any naming conflicts with Transmitters.
         */
        Transmitter transmitter = null;
        try {
            transmitter = dataManager.getTransmitterByMnemonic(text);
        } catch (Exception e) {
            this.message = "Failed to verify that the transmitter group name does not conflict with existing transmitter mnemonics.";
            throw e;
        }

        if (transmitter != null) {
            /*
             * Do not allow the user to potentially overwrite a standalone
             * transmitter group.
             */
            this.message = "Transmitter Group Name conflicts with an existing Transmitter Mnemonic.";
            return false;
        }

        return true;
    }

    public String getMessage() {
        return this.message;
    }
}