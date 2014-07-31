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
package com.raytheon.uf.edex.bmh.audio;

/**
 * A simple POJO used to specify how audio should be regulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 31, 2014 3424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRegulatoryParameters {
    /*
     * A generic id. Multiple parameters can be associated with a single buffer.
     * The id is included in the notification to any listeners.
     */
    private final String id;

    private final double dbMin;

    private final double dbMax;

    /**
     * Constructor
     * 
     * @param id id associated with the parameters; uniqueness is not enforced
     * @param dbMin the minimum decibel range
     * @param dbMax the maximum decibel range
     */
    public AudioRegulatoryParameters(final String id, final double dbMin,
            final double dbMax) {
        this.id = id;
        this.dbMin = dbMin;
        this.dbMax = dbMax;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the dbMin
     */
    public double getDbMin() {
        return dbMin;
    }

    /**
     * @return the dbMax
     */
    public double getDbMax() {
        return dbMax;
    }
}