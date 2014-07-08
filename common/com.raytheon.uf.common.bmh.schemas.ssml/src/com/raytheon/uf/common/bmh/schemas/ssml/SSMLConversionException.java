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
package com.raytheon.uf.common.bmh.schemas.ssml;

/**
 * An exception thrown when SSML generation fails.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SSMLConversionException extends Exception {

    private static final long serialVersionUID = 3516411965227866767L;

    /**
     * @param message the detail message.
     */
    public SSMLConversionException(String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception.
     */
    public SSMLConversionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public SSMLConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message the detail message.
     * @param cause the cause of the exception.
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public SSMLConversionException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}