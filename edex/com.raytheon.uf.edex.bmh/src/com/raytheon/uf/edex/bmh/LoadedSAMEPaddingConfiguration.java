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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.audio.SAMEPaddingConfiguration;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Container for the {@link SAMEPaddingConfiguration}. Used to access the
 * {@link SAMEPaddingConfiguration}; will load the configuration if it has not
 * been loaded yet.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2016 5912       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 */

public final class LoadedSAMEPaddingConfiguration {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(LoadedSAMEPaddingConfiguration.class);

    private static SAMEPaddingConfiguration configuration;

    protected LoadedSAMEPaddingConfiguration() {
    }

    private static void loadConfiguration() throws Exception {
        /*
         * Prepare the {@link JAXBManager}.
         */
        JAXBManager jaxbManager = null;
        try {
            jaxbManager = new JAXBManager(SAMEPaddingConfiguration.class);
        } catch (JAXBException e) {
            throw new Exception("Failed to create a JAXB Manager for: "
                    + SAMEPaddingConfiguration.class.getName() + ".");
        }

        /*
         * Determine the location of the regulation configuration file.
         */
        Path samePaddingConfigPath = Paths.get(
                BMHConstants.getBmhConfDirectory()).resolve(
                SAMEPaddingConfiguration.XML_NAME);
        if (Files.exists(samePaddingConfigPath) == false) {
            throw new Exception(
                    "Failed to find the audio regulation configuration file: "
                            + samePaddingConfigPath.toString() + ".");
        }

        /*
         * Read the configuration.
         */
        try {
            configuration = jaxbManager.unmarshalFromXmlFile(
                    SAMEPaddingConfiguration.class,
                    samePaddingConfigPath.toFile());
        } catch (Exception e) {
            throw new Exception(
                    "Failed to unmarshal the audio regulation configuration file: "
                            + samePaddingConfigPath.toString() + ".", e);
        }

        statusHandler.info("Successfully loaded configuation: "
                + configuration.toString() + ".");
    }

    public static synchronized SAMEPaddingConfiguration getConfiguration()
            throws Exception {
        if (configuration == null) {
            loadConfiguration();
        }
        return configuration;
    }
}