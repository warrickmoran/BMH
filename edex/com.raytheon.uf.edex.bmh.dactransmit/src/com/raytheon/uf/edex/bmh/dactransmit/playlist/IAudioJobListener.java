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

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;

/**
 * Definition of a listener that receives notifications that an audio retrieval
 * has been finished.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 8, 2014  3532       bkowal      Initial creation
 * Jun 29, 2015 4602       bkowal      Added buffer as a parameter.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IAudioJobListener {
    /**
     * Used to indicate that an audio retrieval attempt has been completed.
     * 
     * @param taskId
     *            generic identifier used for tracking and grouping purposes
     * @param message
     *            used to identify the audio that was retrieved
     * @param buffer
     *            the {@link IAudioFileBuffer} that was retrieved
     */
    public void audioRetrievalFinished(String taskId,
            DacPlaylistMessage message, IAudioFileBuffer buffer);
}