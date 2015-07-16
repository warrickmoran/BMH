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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * A common data manager that can be used to handle rejected bmh data. Designed
 * to be injected into any BMH component that may need to reject a file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2015 4136       bkowal      Initial creation
 * Jun 17, 2015 4490       bkowal      Handle the case when a rejected data file may have the
 *                                     same name as a previously rejected data file.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHRejectionDataManager {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(BMHRejectionDataManager.class);

    private static final String BMH_REJECT_DESTINATION = "bmh.data.reject";

    private final int MAX_UNIQUE_INDEX = 99;

    private final Path dataRejectionPath;

    /**
     * Constructor.
     * 
     * @throws BMHConfigurationException
     */
    public BMHRejectionDataManager() throws BMHConfigurationException {
        String specifiedRejectionDirectory = System
                .getProperty(BMH_REJECT_DESTINATION);
        if (specifiedRejectionDirectory == null
                || specifiedRejectionDirectory.trim().isEmpty()) {
            /*
             * the data rejection destination has not been specified. ensure
             * that spring initialization fails.
             */
            throw new BMHConfigurationException(
                    "Failed to retrieve the Data Rejection Destination from the configuration. Please specify the Data Rejection Destination using the "
                            + BMH_REJECT_DESTINATION + " property.");
        }

        this.dataRejectionPath = Paths.get(specifiedRejectionDirectory);
        statusHandler.info("Using data rejection destination "
                + this.dataRejectionPath.toString() + " ...");
        /*
         * verify that the directory exists.
         */
        if (Files.exists(dataRejectionPath)) {
            /*
             * the directory exists.
             */
            return;
        }

        /*
         * the directory does not exist. attempt to create it.
         */
        try {
            Files.createDirectories(this.dataRejectionPath);
        } catch (IOException e) {
            /*
             * failed to create the data rejection destination. ensure that
             * spring initialization fails.
             */
            throw new BMHConfigurationException(
                    "Failed to create the Data Rejection Destination: "
                            + this.dataRejectionPath.toString() + ".", e);
        }
        statusHandler.info("Successfully created data rejection destination: "
                + this.dataRejectionPath.toString());
    }

    /**
     * Rejects the specified file by relocating it to the specified rejection
     * destination.
     * 
     * @param rejectedFilePath
     *            the specified file
     * @param category
     *            identifies why the file needs to be rejected
     * @throws BMHRejectionException
     */
    public void rejectFile(final Path rejectedFilePath,
            final BMH_CATEGORY category) throws BMHRejectionException {
        if (rejectedFilePath == null) {
            throw new IllegalArgumentException(
                    "Required argument rejectedFilePath can not be NULL.");
        }

        Path targetFilePath = this.dataRejectionPath.resolve(rejectedFilePath
                .getFileName());

        /*
         * Verify that a rejected file with the same name does not already
         * exist.
         */
        if (Files.exists(targetFilePath)) {
            /*
             * Attempt to find a unique name for the rejected file.
             */
            int count = 1;
            targetFilePath = this.dataRejectionPath.resolve(rejectedFilePath
                    .getFileName() + "_" + count);
            while (Files.exists(targetFilePath)) {
                ++count;
                if (count > MAX_UNIQUE_INDEX) {
                    /*
                     * there are already 100 rejected files with the same name.
                     * do not attempt to copy another to the rejection
                     * destination.
                     */
                    statusHandler
                            .warn(category,
                                    "Unable to copy reject file: "
                                            + rejectedFilePath.getFileName()
                                            + ". 100 rejected files with the same name already exist; please review the contents of the BMH rejected data directory: "
                                            + this.dataRejectionPath.toString()
                                            + ".");
                    return;
                }
                targetFilePath = this.dataRejectionPath
                        .resolve(rejectedFilePath.getFileName() + "_" + count);
            }
        }

        BMHRejectionException ex = null;
        try {
            Files.move(rejectedFilePath, targetFilePath);
        } catch (IOException e) {
            ex = new BMHRejectionException(rejectedFilePath, e);
        }

        if (ex == null) {
            /*
             * the file has been successfully rejected.
             */
            statusHandler.info("Successfully rejected file "
                    + rejectedFilePath.toString() + ".");
            return;
        }

        /*
         * the file has not been rejected and still exists. attempt to purge the
         * file.
         */
        try {
            Files.delete(rejectedFilePath);
        } catch (IOException e) {
            statusHandler.error(category,
                    "Failed to purge unsuccessfully rejected file: "
                            + rejectedFilePath.toString() + ".", e);
        }

        throw ex;
    }
}