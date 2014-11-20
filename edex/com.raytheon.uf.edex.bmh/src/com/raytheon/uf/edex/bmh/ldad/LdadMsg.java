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
package com.raytheon.uf.edex.bmh.ldad;

/**
 * Temporary Placeholder. Full message is in a separate changeset.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadMsg {
    private long ldadId;

    private String outputName;

    public LdadMsg() {
    }

    /**
     * @return the ldadId
     */
    public long getLdadId() {
        return ldadId;
    }

    /**
     * @param ldadId
     *            the ldadId to set
     */
    public void setLdadId(long ldadId) {
        this.ldadId = ldadId;
    }

    /**
     * @return the outputName
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * @param outputName
     *            the outputName to set
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}