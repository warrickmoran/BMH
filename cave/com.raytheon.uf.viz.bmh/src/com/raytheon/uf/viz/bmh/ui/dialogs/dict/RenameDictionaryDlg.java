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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;

/**
 * Rename a {@link Dictionary} dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 7, 2015  3931       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RenameDictionaryDlg extends NewDictionaryDlg {

    private Dictionary dictionary;

    /**
     * Constructor
     * 
     * @param parentShell
     *            the application {@link Shell}
     * @param dictionary
     *            the {@link Dictionary} to rename
     */
    public RenameDictionaryDlg(Shell parentShell, Dictionary dictionary) {
        super(parentShell);
        if (dictionary == null) {
            throw new IllegalArgumentException(
                    "Required argument dictionary can not be NULL.");
        }
        this.dictionary = dictionary;

        this.setText("Rename Dictionary");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);

        /*
         * Only allow the name field to be altered.
         */
        this.nameTxt.setText(this.dictionary.getName());
        this.languageCombo.setEnabled(false);
        this.nationalBtn.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.bmh.ui.dialogs.dict.NewDictionaryDlg#handleOkAction()
     */
    @Override
    protected void handleOkAction() {
        if (this.validate() == false) {
            return;
        }

        /**
         * Update the {@link Dictionary} name.
         */
        this.dictionary.setName(this.nameTxt.getText().trim());
        this.setReturnValue(this.dictionary);
        this.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.dialogs.dict.NewDictionaryDlg#validate()
     */
    @Override
    protected boolean validate() {
        if (super.validate() == false) {
            /*
             * initial validation has failed.
             */
            return false;
        }

        /**
         * We also need to verify that the {@link Dictionary} name is unique.
         */
        boolean unique = false;
        try {
            unique = this.dm
                    .verifyNameUniqueness(this.nameTxt.getText().trim());
        } catch (Exception e) {
            statusHandler
                    .error("The BMH Dictionary Import has failed. Failed to determine if the specified name is unique: "
                            + this.nameTxt.getText().trim(), e);
            return false;
        }

        if (unique == false) {
            // the name is not unique. validation has failed.
            MessageBox mb = new MessageBox(getShell(), SWT.ICON_WARNING
                    | SWT.OK);
            mb.setText("Enter a Unique Name");
            mb.setMessage("A dictionary with name: "
                    + this.nameTxt.getText().trim()
                    + " already exists. Please enter a different name.");
            mb.open();
            return false;
        }

        return true;
    }
}