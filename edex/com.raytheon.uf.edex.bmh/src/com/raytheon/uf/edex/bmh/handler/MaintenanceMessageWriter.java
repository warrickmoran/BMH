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
package com.raytheon.uf.edex.bmh.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.util.SystemUtil;
import com.raytheon.uf.common.util.file.IOPermissionsHelper;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.BMHMaintenanceException;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Common method to write {@link DacMaintenanceMessage} to an XML file in the
 * BMH maintenance directory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 24, 2015 4394       bkowal      Initial creation
 * Jun 11, 2015 4490       bkowal      Maintenance traceability improvements.
 * Jul 23, 2015 4676       bkowal      Use {@link JAXBManager}.
 * Jun 01, 2017 6259       bkowal      Apply required permissions to maintenance message files.
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class MaintenanceMessageWriter {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MaintenanceMessageWriter.class);

    private static final String MESSAGES_DIRECTORY = "messages";

    private static JAXBManager jaxbManager;

    /**
     * Constructor
     */
    protected MaintenanceMessageWriter() {
    }

    private static synchronized JAXBManager getJaxbManager()
            throws BMHMaintenanceException {
        if (jaxbManager == null) {
            try {
                jaxbManager = new JAXBManager(DacMaintenanceMessage.class);
            } catch (JAXBException e) {
                throw new BMHMaintenanceException(
                        "Failed to instantiate the JAXB Manager.", e);
            }
        }

        return jaxbManager;
    }

    /**
     * Writes a XML representation of a {@link DacMaintenanceMessage} to the
     * maintenance messages directory. Returns the {@link Path} to the XML file
     * that was written as a {@link String}.
     * 
     * @param message
     *            the {@link DacMaintenanceMessage} to write.
     * @param request
     *            the {@link AbstractBMHServerRequest} that triggered the
     *            generation of the {@link DacMaintenanceMessage}.
     * @return the {@link Path} to the XML file that was written as a
     *         {@link String}.
     */
    public static String writeMaintenanceMessage(DacMaintenanceMessage message,
            final AbstractBMHServerRequest request)
            throws BMHMaintenanceException {
        /*
         * Determine where to save the file and the name of the file.
         */
        String hostName = SystemUtil.getHostName();
        int index = hostName.indexOf('.');
        if (index > 0) {
            hostName = hostName.substring(0, index);
        }
        final String fileName = hostName + "_" + System.currentTimeMillis()
                + ".xml";
        statusHandler.info("traceId=" + request.getTraceId()
                + ": Writing maintenace message file " + fileName
                + " for broadcast on Transmitter "
                + message.getTransmitterGroup() + " ...");

        Path maintenanceMessagesPath = BMHConstants
                .getBmhDataDirectory(request.isOperational())
                .resolve(BMHConstants.AUDIO_DATA_DIRECTORY)
                .resolve(BMHConstants.MAINTENANCE_DATA_DIRECTORY)
                .resolve(MESSAGES_DIRECTORY);
        if (!Files.exists(maintenanceMessagesPath)) {
            try {
                com.raytheon.uf.common.util.file.Files.createDirectories(
                        maintenanceMessagesPath,
                        FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
            } catch (IOException e) {
                BMHMaintenanceException ex = new BMHMaintenanceException(
                        "Failed to create the maintenance output directory: "
                                + maintenanceMessagesPath);
                statusHandler.error(BMH_CATEGORY.UNKNOWN,
                        "traceId=" + request.getTraceId()
                                + ": Failed to write the maintenance message file "
                                + fileName + ".",
                        ex);
                throw ex;
            }
        }
        Path output = maintenanceMessagesPath.resolve(fileName);

        try (OutputStream os = IOPermissionsHelper.getOutputStream(output,
                FilePermissionUtils.FILE_PERMISSIONS_SET)) {
            getJaxbManager().marshalToStream(message, os);
        } catch (Exception e) {
            BMHMaintenanceException ex = new BMHMaintenanceException(
                    "Failed to marshal the maintenance message.");
            statusHandler.error(BMH_CATEGORY.UNKNOWN,
                    "traceId=" + request.getTraceId()
                            + ": Failed to write the maintenance message file "
                            + fileName + ".",
                    ex);
            throw ex;
        }
        statusHandler.info("traceId=" + request.getTraceId()
                + ": Successfully wrote maintenance message file " + fileName
                + ".");

        return output.toString();
    }
}