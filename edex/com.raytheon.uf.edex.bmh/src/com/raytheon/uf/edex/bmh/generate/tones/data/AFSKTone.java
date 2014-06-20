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
package com.raytheon.uf.edex.bmh.generate.tones.data;

/**
 * A data structure representing a tone used in Audio Frequency Shifting Key
 * (AFSK) calculations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AFSKTone extends Tone {
    /*
     * The following value is defined on page 6 of the SAME Specification
     * (http://www.nws.noaa.gov/directives/sym/pd01017012curr.pdf)
     */
    private static final double SAME_DATA_RATE = 520.83; // bits / second

    /*
     * Used when calculating the duration of one cycle: The period, usually
     * denoted by T, is the duration of one cycle, and is the reciprocal of the
     * frequency f (http://en.wikipedia.org/wiki/Frequency)
     */
    private static final double DURATION_RECIPROCAL = 1.0;

    private double bitFrequency;

    /**
     * 
     */
    public AFSKTone() {
        this.bitFrequency = SAME_DATA_RATE;
        this.calculateDuration();
    }

    private void calculateDuration() {
        double reciprocal = DURATION_RECIPROCAL / this.bitFrequency;
        this.setDuration(reciprocal);
    }

    public double getBitFrequency() {
        return bitFrequency;
    }

    public void setBitFrequency(double bitFrequency) {
        this.bitFrequency = bitFrequency;
        this.calculateDuration();
    }

    public double getBitDuration() {
        return super.getDuration();
    }
}
