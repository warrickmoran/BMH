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
 * Indicates that a BMH file has been unsuccessfully processed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2015 4136       bkowal      Initial creation
 * Nov 16, 2015 5127       rjpeter     Renamed to BMHFileProcessException
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHFileProcessException extends Exception {

    private static final long serialVersionUID = -1768662416923119333L;

    private static final String MESSAGE_PREFIX = "Failed to process file: ";

    private static final String MESSAGE_SUFFIX = ". The file will be purged instead.";

    /**
     * Constructor
     * 
     * @param filePath
     *            the file that was not successfully processed
     * @param cause
     *            the cause of the rejection failure
     */
    public BMHFileProcessException(Path filePath, Throwable cause) {
        super(buildProcessMessage(filePath), cause);
    }

    /**
     * Constructs the exception message.
     * 
     * @param filePath
     *            the file that was not successfully processed
     * @return the constructed exception message.
     */
    private static String buildProcessMessage(final Path filePath) {
        StringBuilder sb = new StringBuilder(MESSAGE_PREFIX);
        sb.append(filePath.toString()).append(MESSAGE_SUFFIX);

        return sb.toString();
    }
}