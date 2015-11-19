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
package com.raytheon.uf.edex.bmh.dac;

/**
 * Exception indicating that a DAC HTTP connection was not successfully closed.
 * Generally, when this unlikely {@link Exception} is encountered, the suggested
 * course of action is to refrain from reconnecting to the DAC (especially when
 * there was previously a successful connection) because if the DAC were to
 * become unavailable (ex: during reboot), any connection attempts would just
 * hang even if a timeout had been set.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2015 5113           bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacHttpCloseException extends Exception {

    private static final long serialVersionUID = 532160019110052038L;

    public DacHttpCloseException(final String message, Exception e) {
        super(message, e);
    }
}