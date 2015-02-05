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
package com.raytheon.uf.viz.bmh;

import java.io.IOException;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.standalone.BMHComponent;
import com.raytheon.uf.viz.core.VizServers;
import com.raytheon.uf.viz.core.localization.ServerRemembrance;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Constants for the BMH Servers
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 09, 2014 3656       bkowal      Initial creation
 * Oct 17, 2014 3687       bsteffen    Support practice servers.
 * Feb 05, 2015 3743       bsteffen    Add methods needed for running standalone.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class BMHServers {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHServers.class);

    public static final String BMH_SERVER = "bmh.server";

    private static final String BMH_SERVER_OPTIONS = "bmh.server.options";

    private static final String LINETAP_SERVER = "bmh.comms.manager.linetap";
    
    private static final String BROADCAST_SERVER = "bmh.comms.manager.broadcast";

    private static final String BROADCAST_SERVER_OPTIONS = "bmh.comms.manager.broadcast.options";

    private static final String PRACTICE_LINETAP_SERVER = "bmh.practice.comms.manager.linetap";

    private static final String PRACTICE_BROADCAST_SERVER = "bmh.practice.comms.manager.broadcast";

    /* Only used to prompt user when request edex is down */
    private static String DEFAULT_SERVER = "px1";

    /* Only used to prompt user when request edex is down */
    private static String DEFAULT_BMH_SERVER = "http://" + DEFAULT_SERVER
            + ":9583/services";

    /* Only used to prompt user when request edex is down */
    private static String DEFAULT_BROADCAST_SERVER = "tcp://"
            + DEFAULT_SERVER + ":58269/";

    public static String getLineTapServer() {
        String key = LINETAP_SERVER;
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            key = PRACTICE_LINETAP_SERVER;
        }
        return VizServers.getInstance().getServerLocation(key);
    }

    public static String getBroadcastServerKey() {
        String key = BROADCAST_SERVER;
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            key = PRACTICE_BROADCAST_SERVER;
        }
        return key;
    }

    public static String getBroadcastServer() {
        return VizServers.getInstance().getServerLocation(
                getBroadcastServerKey());
    }

    /**
     * Save servers to the plugin preferences, they will be loaded if running
     * the {@link BMHComponent} when request edex is down.
     * 
     */
    public static void saveServers() {
        String bmhServer = VizServers.getInstance().getServerLocation(BMH_SERVER);
        String broadcastServer = VizServers.getInstance()
                .getServerLocation(BROADCAST_SERVER);
        if (bmhServer == null && broadcastServer == null) {
            return;
        }
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        if (bmhServer != null) {
            prefs.setValue(BMH_SERVER, bmhServer);
            String serverOptions = ServerRemembrance.formatServerOptions(
                    bmhServer, prefs, BMH_SERVER_OPTIONS);
            prefs.setValue(BMH_SERVER_OPTIONS, serverOptions);
        }
        if (broadcastServer != null) {
            prefs.setValue(BROADCAST_SERVER, broadcastServer);
            String serverOptions = ServerRemembrance.formatServerOptions(
                    broadcastServer, prefs, BROADCAST_SERVER_OPTIONS);
            prefs.setValue(BROADCAST_SERVER_OPTIONS, serverOptions);
        }
        if (prefs instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) prefs).save();
            } catch (IOException e) {
                statusHandler.error("Unable to save BMH Servers", e);
            }
        }
    }

    /**
     * Get previously saved bmh server, only intended for use during initial
     * configuration.
     */
    public static String getSavedBmhServer() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String bmhEdex = prefs.getString(BMH_SERVER);
        if (bmhEdex == null || bmhEdex.isEmpty()) {
            bmhEdex = DEFAULT_BMH_SERVER;
        }
        return bmhEdex;
    }

    /**
     * Get list of all previously saved bmh servers, only intended for use
     * during initial configuration.
     */
    public static String[] getSavedBmhServerOptions() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        return ServerRemembrance.getServerOptions(prefs, BMH_SERVER_OPTIONS);
    }

    /**
     * Get previously saved broadcast server, only intended for use during
     * initial configuration.
     */
    public static String getSavedBroadcastServer() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String bmhEdex = prefs.getString(BROADCAST_SERVER);
        if (bmhEdex == null || bmhEdex.isEmpty()) {
            bmhEdex = DEFAULT_BROADCAST_SERVER;
        }
        return bmhEdex;
    }

    /**
     * Get list of all previously saved broadcast servers, only intended for use
     * during initial configuration.
     */
    public static String[] getSavedBroadcastServerOptions() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        return ServerRemembrance.getServerOptions(prefs,
                BROADCAST_SERVER_OPTIONS);
    }

    /**
     * 
     */
    protected BMHServers() {
    }
}