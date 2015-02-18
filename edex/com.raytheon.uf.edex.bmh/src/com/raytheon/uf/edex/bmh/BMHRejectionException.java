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
package com.raytheon.uf.edex.bmh;

import java.nio.file.Path;

/**
 * Indicates that a BMH file has been unsuccessfully rejected.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2015 4136       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHRejectionException extends Exception {

    private static final long serialVersionUID = -1768662416923119333L;

    private static final String REJECTION_MESSAGE_PREFIX = "Failed to reject file: ";

    private static final String REJECTION_MESSAGE_SUFFIX = ". The file will be purged instead.";

    /**
     * Constructor
     * 
     * @param rejectedFilePath
     *            the file that was not successfully rejected
     * @param cause
     *            the cause of the rejection failure
     */
    public BMHRejectionException(Path rejectedFilePath, Throwable cause) {
        super(buildRejectionMessage(rejectedFilePath), cause);
    }

    /**
     * Constructs the exception message.
     * 
     * @param rejectedFilePath
     *            the file that was not successfully rejected
     * @return the constructed exception message.
     */
    private static String buildRejectionMessage(final Path rejectedFilePath) {
        StringBuilder sb = new StringBuilder(REJECTION_MESSAGE_PREFIX);
        sb.append(rejectedFilePath.toString()).append(REJECTION_MESSAGE_SUFFIX);

        return sb.toString();
    }
}