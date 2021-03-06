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
package com.raytheon.uf.edex.bmh;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * BMH EDEX Constants
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 3, 2014  3228       bkowal      Initial creation
 * Sep 29, 2014 3291       bkowal      Added the bmh home directory.
 * Oct 07, 2014 3687       bsteffen    Add method to get practice data directory.
 * Nov 05, 2014 3630       bkowal      Added AUDIO_DATA_DIRECTORY
 * Dec 12, 2014 3603       bsteffen    Added MAINTENANCE_DATA_DIRECTORY
 * Aug 10, 2015 4424       bkowal      Added {@link #PLAYLIST_DIRECTORY}.
 * Aug 24, 2015 4770       bkowal      Added {@link #getBmhConfDirectory()}.
 * Feb 02, 2017 6085       bsteffen    Enable ssl in the JMS connection.
 * May 08, 2017 6259       bkowal      Updated to use {@link com.raytheon.uf.common.util.file.Files}.
 *
 * </pre>
 * 
 * @author bkowal
 */
public final class BMHConstants {
    /*
     * IUFStatusHandler will be used in this class because it is utilized before
     * the BMH Notification Manager has been fully initialized (it is used
     * during the initialization of the BMH Notification Manager).
     * 
     * Additionally, if there is a problem in this class, as it is currently
     * used (06/17/2014), it will be obvious because EDEX will fail to start.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHConstants.class);

    public static final String AUDIO_DATA_DIRECTORY = "audio";

    public static final String PLAYLIST_DIRECTORY = "playlist";

    private static final String CONF_DIRECTORY = "conf";

    private static final String BMH_HOME_ENV_VAR = "BMH_HOME";

    private static final String BMH_DATA_ENV_VAR = "BMH_DATA";

    private static String BMH_HOME_DIRECTORY;

    private static String BMH_DATA_DIRECTORY;

    private static String BMH_CONF_DIRECTORY;

    public static final String MAINTENANCE_DATA_DIRECTORY = "maintenance";

    public static synchronized String getBmhHomeDirectory() {
        if (BMH_HOME_DIRECTORY == null) {
            BMH_HOME_DIRECTORY = getHomeDirectory();
        }
        return BMH_HOME_DIRECTORY;
    }

    public static synchronized String getBmhDataDirectory() {
        if (BMH_DATA_DIRECTORY == null) {
            BMH_DATA_DIRECTORY = getDataDirectory();
        }
        return BMH_DATA_DIRECTORY;
    }

    public static synchronized String getBmhConfDirectory() {
        if (BMH_CONF_DIRECTORY == null) {
            BMH_CONF_DIRECTORY = getBmhHomeDirectory() + File.separatorChar
                    + CONF_DIRECTORY;
        }
        return BMH_CONF_DIRECTORY;
    }

    public static Path getBmhDataDirectory(boolean operational) {
        Path result = Paths.get(getDataDirectory());
        if (!operational) {
            result = result.resolve("practice");
        }
        return result;
    }

    public static String getJmsConnectionString(String id) {
        StringBuilder stringBuilder = new StringBuilder("amqp://guest:guest@");
        stringBuilder.append(id);
        stringBuilder.append("/");
        stringBuilder.append(System.getenv("JMS_VIRTUALHOST"));
        stringBuilder.append("?brokerlist='");
        stringBuilder.append(System.getenv("JMS_SERVER"));
        stringBuilder.append(
                "?connecttimeout='5000'&heartbeat='0''&maxprefetch='10'&sync_publish='all'&failover='nofailover'&sync_ack='true'");
        if (Boolean.parseBoolean(System.getenv("JMS_SSL_ENABLED"))) {
            stringBuilder.append("&ssl='true'");
        }
        return stringBuilder.toString();
    }

    private static String getHomeDirectory() {
        String bmhHomeDirectory = System.getenv(BMH_HOME_ENV_VAR);
        if (bmhHomeDirectory == null || bmhHomeDirectory.isEmpty()) {
            /*
             * This case is unlikely because other aspects of the startup would
             * most likely fail long before this segment of code was reached.
             */
            final String exceptionText = "Unable to determine the location of the BMH HOME directory! Please ensure that the environment variable '"
                    + BMH_HOME_ENV_VAR
                    + "' is set to the location of the directory.";

            statusHandler.handle(Priority.CRITICAL, exceptionText);
            throw new RuntimeException(exceptionText);
        }

        statusHandler.info("BMH HOME is: " + bmhHomeDirectory);
        return bmhHomeDirectory;
    }

    private static String getDataDirectory() {
        String bmhDataDirectory = System.getenv(BMH_DATA_ENV_VAR);
        if (bmhDataDirectory == null || bmhDataDirectory.isEmpty()) {
            final String exceptionText = "Unable to determine the location of the BMH DATA directory! Please ensure that the environment variable '"
                    + BMH_DATA_ENV_VAR
                    + "' is set to the location of the directory.";

            statusHandler.handle(Priority.CRITICAL, exceptionText);
            throw new RuntimeException(exceptionText);
        }

        final Path bmhDataPath = Paths.get(bmhDataDirectory);
        if (!Files.exists(bmhDataPath)) {
            /*
             * Attempt to create the BMH data directory.
             */
            try {
                com.raytheon.uf.common.util.file.Files.createDirectories(
                        bmhDataPath,
                        FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
            } catch (Exception e) {
                final String exceptionText = "Failed to create the BMH DATA directory: "
                        + bmhDataDirectory + "!";

                statusHandler.handle(Priority.CRITICAL, exceptionText, e);
                throw new RuntimeException(exceptionText);
            }
        }

        statusHandler.info("BMH DATA is: " + bmhDataDirectory);
        return bmhDataDirectory;
    }

    /**
     * 
     */
    private BMHConstants() {
    }
}