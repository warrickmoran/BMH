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
package com.raytheon.uf.edex.bmh.test;

/**
 * TestInputPropertyInvalidException. Thrown when a property has been set
 * incorrectly in the input file that is being processed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TestInputPropertyInvalidException extends
        TestProcessingFailedException {

    private static final long serialVersionUID = 637804890654415106L;

    /**
     * @param message
     * @param cause
     */
    public TestInputPropertyInvalidException(String inputFile,
            String propertyName, Throwable cause) {
        super(buildMessage(inputFile, propertyName), cause);
    }

    private static String buildMessage(String inputFile, String propertyName) {
        StringBuilder stringBuilder = new StringBuilder(
                "Invalid value specified for property: ");
        stringBuilder.append(propertyName);
        stringBuilder.append(" in input file: ");
        stringBuilder.append(inputFile);
        stringBuilder.append("!");

        return stringBuilder.toString();
    }
}