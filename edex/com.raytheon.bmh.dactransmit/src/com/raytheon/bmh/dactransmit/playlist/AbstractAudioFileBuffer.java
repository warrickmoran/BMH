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
package com.raytheon.bmh.dactransmit.playlist;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;

/**
 * Abstract representation of an audio file buffer.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2014  3642       bkowal      Initial creation
 * Dec 11, 2014 3651       bkowal      Added {@link message} for logging
 *                                     purposes.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractAudioFileBuffer implements IAudioFileBuffer {

    protected final DacPlaylistMessage message;

    protected AbstractAudioFileBuffer(DacPlaylistMessage message) {
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.dactransmit.playlist.IAudioFileBuffer#isDynamic
     * ()
     */
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public void rewind() {
        /* Default is to do nothing. */
    }
}