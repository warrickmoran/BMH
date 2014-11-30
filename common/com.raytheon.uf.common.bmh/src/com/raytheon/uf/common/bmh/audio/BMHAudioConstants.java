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
 * Constant values used when completing audio operations for BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 24, 2014 3863       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class BMHAudioConstants {

    /*
     * The maximum amplitude that can be associated with the PCM audio samples
     * recognized and supported by the BMH components.
     */
    public static final double MAX_AMPLITUDE = 32767.0;

    /*
     * Reference: http://www.rapidtables.com/electric/decibel.htm
     * 
     * The "Amplitude ratio to dB conversion" formula.
     */
    public static final double AMPLITUDE_TO_DB_CONSTANT = 20.0;

    public static final double DB_ALTERATION_CONSTANT = 10.0;

    /**
     * 
     */
    protected BMHAudioConstants() {
    }
}