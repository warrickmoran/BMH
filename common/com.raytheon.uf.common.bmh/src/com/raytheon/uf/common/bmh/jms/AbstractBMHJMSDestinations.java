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
package com.raytheon.uf.common.bmh.jms;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification;

/**
 * Abstract JMS destination lookup. Returns the appropriate jms destination for
 * operational and practice mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2014 3807       bkowal      Initial creation
 * Nov 19, 2014 3817       bsteffen    Use status queue for more than just dacs.
 * Nov 21, 2014 3385       bkowal      Added ldad dissemination uris
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBMHJMSDestinations {

    /* Status Notification Destinations */
    private static final String BMH_STATUS = "BMH.Status";

    private static final String BMH_PRACTICE_STATUS = "BMH.Practice.Status";

    /* BMH Configuration Notification Destinations */
    private static final String BMH_CONFIG = "BMH.Config";

    private static final String BMH_PRACTICE_CONFIG = "BMH.Practice.Config";

    /* Message Transformation Destinations */

    private static final String BMH_TRANSFORM = "BMH.Transform";

    private static final String BMH_PRACTICE_TRANSFORM = "BMH.Practice.Transform";

    /* TTS Destinations */

    private static final String BMH_TTS = "BMH.TTS";

    private static final String BMH_PRACTICE_TTS = "BMH.Practice.TTS";

    /* Playlist Scheduling Destinations */

    private static final String BMH_SCHEDULE = "BMH.Schedule";

    private static final String BMH_PRACTICE_SCHEDULE = "BMH.Practice.Schedule";

    /* Ldad Dissemination Destinations */

    private static final String BMH_LDAD = "BMH.LDAD";

    private static final String BMH_PRACTICE_LDAD = "BMH.Practice.LDAD";

    /**
     * 
     */
    protected AbstractBMHJMSDestinations() {
    }

    /**
     * Returns the Status URI. Message of types with a package of
     * com.raytheon.uf.common.bmh.notify.status are written to and can be
     * retrieved from this location.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getStatusURI(final boolean operational) {
        return (operational) ? BMH_STATUS : BMH_PRACTICE_STATUS;
    }

    /**
     * Returns the BMH Config URI. {@link ConfigNotification}s and all
     * derivations thereof are posted to and can be read from this location. A
     * {@link ConfigNotification} indicates that a configuration change has
     * occurred. The posted {@link ConfigNotification} will include the id of
     * the element that has been changed.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getBMHConfigURI(final boolean operational) {
        return (operational) ? BMH_CONFIG : BMH_PRACTICE_CONFIG;
    }

    /**
     * Returns the BMH Text Transformation URI. {@link ValidatedMessage}s are
     * posted to and read from this location.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getBMHTransformURI(final boolean operational) {
        return (operational) ? BMH_TRANSFORM : BMH_PRACTICE_TRANSFORM;
    }

    /**
     * Returns the BMH TTS (Text to Speech) URI. {@link BroadcastMsg}s are
     * posted to and read from this location.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getBMHTTSURI(final boolean operational) {
        return (operational) ? BMH_TTS : BMH_PRACTICE_TTS;
    }

    /**
     * Returns the BMH Playlist Generation / Scheduling URI.
     * {@link BroadcastMsg}s are posted to and read from this location.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getBMHScheduleURI(final boolean operational) {
        return (operational) ? BMH_SCHEDULE : BMH_PRACTICE_SCHEDULE;
    }

    /**
     * Returns the BMH Ldad Dissemination URI. {@link LdadMsg} are posted to and
     * read from this location.
     * 
     * @param operational
     *            true indicates return operational uri, false indicates return
     *            practice uri
     * @return the request jms uri
     */
    protected String getBMHLdadURI(final boolean operational) {
        return (operational) ? BMH_LDAD : BMH_PRACTICE_LDAD;
    }
}