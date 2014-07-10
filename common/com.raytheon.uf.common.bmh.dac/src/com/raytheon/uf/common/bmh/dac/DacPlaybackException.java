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
package com.raytheon.uf.common.bmh.dac;

/**
 * The DAC Playback Exception. This exception is primarily thrown when the
 * construction of the DAC Live Streamer Thread fails.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2014 3374       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacPlaybackException extends Exception {

    private static final long serialVersionUID = -1366228624740413767L;

    private static final String EXCEPTION_TEXT = "Failed to initialize the DAC Streaming Capability!";

    /**
     * @param cause
     *            the cause of the exception
     */
    public DacPlaybackException(Throwable cause) {
        super(EXCEPTION_TEXT, cause);
    }
}