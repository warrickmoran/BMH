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
package com.raytheon.uf.edex.bmh.audio;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.audio.AudioRegulationConfiguration;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.BMHConstants;

/**
 * Container for the {@link AudioRegulationConfiguration}. Used to access the
 * {@link AudioRegulationConfiguration}; will load the configuration if it has
 * not been loaded yet.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 24, 2015 4770       bkowal      Initial creation
 * Aug 25, 2015 4771       bkowal      Added logging of the configuration that is loaded.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class LoadedAudioRegulationConfiguration {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(LoadedAudioRegulationConfiguration.class);

    private static AudioRegulationConfiguration configuration;

    protected LoadedAudioRegulationConfiguration() {
    }

    private static void loadConfiguration() throws Exception {
        /*
         * Prepare the {@link JAXBManager}.
         */
        JAXBManager jaxbManager = null;
        try {
            jaxbManager = new JAXBManager(AudioRegulationConfiguration.class);
        } catch (JAXBException e) {
            throw new Exception("Failed to create a JAXB Manager for: "
                    + AudioRegulationConfiguration.class.getName() + ".");
        }

        /*
         * Determine the location of the regulation configuration file.
         */
        Path audioRegulationConfigPath = Paths.get(
                BMHConstants.getBmhConfDirectory()).resolve(
                AudioRegulationConfiguration.XML_NAME);
        if (Files.exists(audioRegulationConfigPath) == false) {
            throw new Exception(
                    "Failed to find the audio regulation configuration file: "
                            + audioRegulationConfigPath.toString() + ".");
        }

        /*
         * Read the configuration.
         */
        try {
            configuration = jaxbManager.unmarshalFromXmlFile(
                    AudioRegulationConfiguration.class,
                    audioRegulationConfigPath.toFile());
        } catch (Exception e) {
            throw new Exception(
                    "Failed to unmarshal the audio regulation configuration file: "
                            + audioRegulationConfigPath.toString() + ".", e);
        }

        statusHandler.info("Successfully loaded configuation: "
                + configuration.toString() + ".");
    }

    public static synchronized AudioRegulationConfiguration getConfiguration()
            throws Exception {
        if (configuration == null) {
            loadConfiguration();
        }
        return configuration;
    }
}