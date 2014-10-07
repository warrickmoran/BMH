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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

/**
 * Definition of a BMH Audio File Buffer.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2014  3642        bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IAudioFileBuffer {
    /**
     * Indicates whether or not there are any fragments that need to be loaded /
     * reloaded prior to audio transmission
     * 
     * @return a boolean indicating whether or not any audio fragments
     *         will need to be loaded / reloaded prior to transmission
     */
    public boolean isDynamic();

    /**
     * Returns this buffer's capacity.
     * 
     * @param includeTones
     *            Whether or not to include the tones data in this calculation.
     * @return The capacity of this buffer
     */
    public int capacity(boolean includeTones);

    /**
     * Rewinds this buffer.
     */
    public void rewind();
}
