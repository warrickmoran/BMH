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

import java.util.Set;
import java.util.UUID;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;

/**
 * Used to track the progress of one or multiple audio retrieval tasks.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 9, 2014  3532       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CachedAudioRetrievalTask {

    private final String identifier = UUID.randomUUID().toString();

    private long start;

    private long end;

    private Set<DacPlaylistMessage> retrievals;

    /**
     * Constructor
     * 
     * @param messages
     *            the audio retrievals of interest that will be monitored and
     *            timed
     */
    public CachedAudioRetrievalTask(Set<DacPlaylistMessage> messages) {
        this.retrievals = messages;
        this.start = System.currentTimeMillis();
    }

    /**
     * 
     * 
     * @param message
     */
    public void retrievalFinished(final DacPlaylistMessage message) {
        this.retrievals.remove(message);
        if (this.isComplete()) {
            this.end = System.currentTimeMillis();
        }
    }

    /**
     * Indicates rather or not all expected audio retrievals have been finished.
     * 
     * @return a {@link Boolean} indicating whether or not all audio retrievals
     *         of interest have been finished.
     */
    public boolean isComplete() {
        return this.retrievals.isEmpty();
    }

    /**
     * Returns the time required to complete the audio retrievals of interest
     * 
     * @return the time required to complete the audio retrievals of interest
     */
    public long getDuration() {
        return this.end - this.start;
    }

    /**
     * Returns the generic identifier associated with the audio retrieval task
     * 
     * @return the generic identifier associated with the audio retrieval task
     */
    public String getIdentifier() {
        return this.identifier;
    }
}