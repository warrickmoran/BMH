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
package com.raytheon.uf.common.bmh.dac;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Event message to track the status of the DAC configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2015  5113       bkowal      Initial creation
 * Nov 12, 2015 5113       bkowal      Added "Reboot Only" message text.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class DacConfigEvent {

    public static final String DEFAULT_ACTION = "Verify that the DAC has been powered on and that is has been connected to the network. Verify that the DAC is accessible on the network.";

    public static final String MSG_VERIFY = "Verifying DAC availability ...";

    public static final String MSG_VERIFY_SUCCESS = "Successfully connected to the DAC.";

    public static final String MSG_VERIFY_FAILURE = "Failed to connect to the DAC!";

    public static final String MSG_CONFIGURE = "Configuring the DAC ...";

    public static final String MSG_CONFIGURE_SUCCESS = "DAC configured successfully.";

    public static final String MSG_CONFIGURE_FAILURE = "Failed to configure the DAC!";
    
    public static final String MSG_REBOOT_TRIGGER_FAILURE = "Failed to initiate a DAC reboot!";

    public static final String MSG_REBOOT = "Rebooting the DAC ...";

    public static final String MSG_REBOOT_WAIT = "Waiting for DAC to restart ...";

    public static final String MSG_REBOOT_SUCCESS = "DAC successfully restarted.";

    public static final String MSG_REBOOT_FAILURE = "DAC failed to restart!";

    public static final String MSG_FAIL = "Terminating DAC configuration process ...";

    public static final String MSG_SUCCESS = "DAC configuration complete.";

    private static final String ACTION_NONE = "NONE";

    @DynamicSerializeElement
    private Calendar eventDate;

    @DynamicSerializeElement
    private String message;

    @DynamicSerializeElement
    private boolean error;

    @DynamicSerializeElement
    private String action;

    public DacConfigEvent() {
    }

    public DacConfigEvent(String message) {
        this.eventDate = TimeUtil.newGmtCalendar();
        this.message = message;
        this.action = ACTION_NONE;
    }

    public DacConfigEvent(String message, String action) {
        this(message);
        this.error = true;
        this.action = action;
    }

    /**
     * @return the eventDate
     */
    public Calendar getEventDate() {
        return eventDate;
    }

    /**
     * @param eventDate
     *            the eventDate to set
     */
    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }
}