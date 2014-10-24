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
package com.raytheon.uf.common.bmh.audio;

/**
 * Indicates that an audio retrieval has failed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2014 3532       bkowal      Initial creation
 * Oct 23, 2014 3748       bkowal      Move to BMH common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRetrievalException extends Exception {
    private static final long serialVersionUID = 5340111095563724429L;

    /**
     * @param message
     *            the detail message
     */
    public AudioRetrievalException(String message) {
        super(message);
    }

    /**
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public AudioRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}