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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

/*
 * TODO: not sure if BMH_DATA will actually be an environment variable in the
 * final version. However, just adding this as a placeholder because it mimics
 * the way the current EDEX data directory is set.
 */
public final class BMHConstants {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHConstants.class);

    private static final String BMH_DATA_ENV_VAR = "BMH_DATA";

    private static String BMH_DATA_DIRECTORY;

    public static String getBmhDataDirectory() {
        if (BMH_DATA_DIRECTORY == null) {
            BMH_DATA_DIRECTORY = getDataDirectory();
        }
        return BMH_DATA_DIRECTORY;
    }

    private static String getDataDirectory() {
        String bmhDataDirectory = System.getenv(BMH_DATA_ENV_VAR);
        if (bmhDataDirectory == null) {
            final String exceptionText = "Unable to determine the location of the BMH DATA directory! Please ensure that the environment variable '"
                    + BMH_DATA_ENV_VAR
                    + "' is set to the location of the directory.";

            statusHandler.handle(Priority.CRITICAL, exceptionText);
            throw new RuntimeException(exceptionText);
        }

        File verifyExistence = new File(bmhDataDirectory);
        if (verifyExistence.exists() == false
                && verifyExistence.mkdirs() == false) {
            final String exceptionText = "Failed to create the BMH DATA directory: "
                    + bmhDataDirectory + "!";

            statusHandler.handle(Priority.CRITICAL, exceptionText);
            throw new RuntimeException(exceptionText);
        }

        statusHandler.info("BMH DATA is: " + bmhDataDirectory);
        return bmhDataDirectory;
    }

    /**
     * 
     */
    protected BMHConstants() {
    }
}