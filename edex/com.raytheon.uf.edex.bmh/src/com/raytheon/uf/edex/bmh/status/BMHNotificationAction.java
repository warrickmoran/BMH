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
package com.raytheon.uf.edex.bmh.status;

import com.raytheon.uf.edex.bmh.status.BMH_ACTION;

/**
 * Used to store information about what actions should occur for a particular
 * notification type. Generally will be mapped to a category and/or a priority.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 17, 2014 3291       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHNotificationAction {

    private final String audioFile;

    private final BMH_ACTION[] actions;

    /**
     * 
     */
    public BMHNotificationAction(final String audioFile,
            final BMH_ACTION[] actions) {
        this.audioFile = audioFile;
        this.actions = actions;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public BMH_ACTION[] getActions() {
        return actions;
    }
}