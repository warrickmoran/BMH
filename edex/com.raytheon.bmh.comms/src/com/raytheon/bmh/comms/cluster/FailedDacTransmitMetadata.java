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
package com.raytheon.bmh.comms.cluster;

/**
 * POJO that tracks the number of times a Dac Transmit has failed to start after
 * being requested for load balancing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 30, 2016 5419       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class FailedDacTransmitMetadata {

    private static final int MAX_FAILURES = 3;

    private final String transmitterGroup;

    private int failureCount;

    public FailedDacTransmitMetadata(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public boolean isMaxFailures() {
        ++this.failureCount;
        return (this.failureCount == MAX_FAILURES);
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }
}