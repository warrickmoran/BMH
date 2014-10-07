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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Playlist request object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3432     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistRequest extends AbstractBMHServerRequest {
    public enum PlaylistAction {
        GET_PLAYLIST_BY_SUITE_GROUP, GET_PLAYLIST_DATA_FOR_TRANSMITTER
    }

    @DynamicSerializeElement
    private PlaylistAction action;

    @DynamicSerializeElement
    private String suiteName;

    @DynamicSerializeElement
    private String groupName;

    @DynamicSerializeElement
    private String transmitterName;

    /**
     * @return the action
     */
    public PlaylistAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(PlaylistAction action) {
        this.action = action;
    }

    /**
     * @return the suiteName
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * @param suiteName
     *            the suiteName to set
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName
     *            the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the transmitterName
     */
    public String getTransmitterName() {
        return transmitterName;
    }

    /**
     * @param transmitterName
     *            the transmitterName to set
     */
    public void setTransmitterName(String transmitterName) {
        this.transmitterName = transmitterName;
    }
}
