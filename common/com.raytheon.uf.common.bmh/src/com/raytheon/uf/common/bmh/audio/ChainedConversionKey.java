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
 * Unique identifier used to identify a {@link ChainedAudioConverter}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 13, 2016 5177       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ChainedConversionKey {

    private final BMHAudioFormat origin;

    private final BMHAudioFormat destination;

    public ChainedConversionKey(BMHAudioFormat origin,
            BMHAudioFormat destination) {
        if (origin == null) {
            throw new IllegalArgumentException(
                    "Required argument origin can not be NULL.");
        }
        if (destination == null) {
            throw new IllegalArgumentException(
                    "Required argument destination can not be NULL.");
        }

        this.origin = origin;
        this.destination = destination;
    }

    /**
     * @return the origin
     */
    public BMHAudioFormat getOrigin() {
        return origin;
    }

    /**
     * @return the destination
     */
    public BMHAudioFormat getDestination() {
        return destination;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChainedConversionKey other = (ChainedConversionKey) obj;
        if (destination != other.destination)
            return false;
        if (origin != other.origin)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ChainedConversionKey [origin=");
        sb.append(this.origin.name()).append(", destination=");
        sb.append(this.destination.name()).append("]");

        return sb.toString();
    }
}