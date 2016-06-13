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
package com.raytheon.uf.common.bmh.notify;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link AbstractAlarmableMessageNotification} indicating that dac transmit
 * failed to load/initialize a specific broadcast message.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 25, 2016 5561       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastMsgInitFailedNotification extends
        AbstractAlarmableMessageNotification {

    /*
     * Metadata is important in this case because it includes information that
     * will make it easier for the user to identify the message. The
     * identification will allow the user to bring up the message in Weather
     * Messages so that it can be re-submitted. But, it is important to note
     * that if BMH had its own notification center (instead of using AlertViz),
     * it would be possible for it to provide an easy-access link directly to
     * the message for the user.
     */
    @DynamicSerializeElement
    private DacPlaylistMessageMetadata metadata;

    /*
     * Flag indicating whether or not this message is associated with an
     * interrupt playlist. Would be final; however, it is not so that
     * getters/setters exist for {@link DynamicSerialize}.
     */
    @DynamicSerializeElement
    private boolean interrupt;

    /**
     * Constructor
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public BroadcastMsgInitFailedNotification() {
    }

    /**
     * Constructor
     * 
     * @param message
     *            the {@link DacPlaylistMessage} that could not be initialized.
     */
    public BroadcastMsgInitFailedNotification(DacPlaylistMessage message,
            boolean interrupt) {
        super(message);
        /*
         * Remember metadata is transient directly within a {@link
         * DacPlaylistMessage}, so it is extracted here.
         */
        this.metadata = message.getMetadata();
        this.interrupt = interrupt;
    }

    public boolean isHighPriority() {
        return this.interrupt || this.metadata.isWarning()
                || this.metadata.isWatch();
    }

    public DacPlaylistMessageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DacPlaylistMessageMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
}