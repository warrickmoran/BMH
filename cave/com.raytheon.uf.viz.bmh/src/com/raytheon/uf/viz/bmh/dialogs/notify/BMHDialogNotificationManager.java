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
package com.raytheon.uf.viz.bmh.dialogs.notify;

import com.google.common.eventbus.EventBus;

/**
 * Allows BMH Dialogs to send notifications to other BMH Dialogs in the form of
 * {@link IDialogNotification}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2014  3904       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHDialogNotificationManager {

    private static final BMHDialogNotificationManager instance = new BMHDialogNotificationManager();

    private final EventBus eventBus;

    /**
     * Constructor
     */
    protected BMHDialogNotificationManager() {
        this.eventBus = new EventBus();
    }

    public static BMHDialogNotificationManager getInstance() {
        return instance;
    }

    /**
     * Registers the specified {@link IDialogNotificationListener} with the
     * notification manager so that it can start receiving
     * {@link IDialogNotification} notifications.
     * 
     * @param dialog
     *            the specified {@link IDialogNotificationListener}
     */
    public void register(final IDialogNotificationListener dialog) {
        this.eventBus.register(dialog);
    }

    /**
     * Allows the specified {@link IDialogNotificationListener} to unsubscribe
     * from the notification manager so that it will no longer receive
     * {@link IDialogNotification} notifications.
     * 
     * @param dialog
     *            the specified {@link IDialogNotificationListener}
     */
    public void unregister(final IDialogNotificationListener dialog) {
        this.eventBus.unregister(dialog);
    }

    /**
     * Publishes the specified {@link IDialogNotification} to all subscribers.
     * 
     * @param notification
     *            the specified {@link IDialogNotification}.
     */
    public void post(final IDialogNotification notification) {
        this.eventBus.post(notification);
    }
}