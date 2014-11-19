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
package com.raytheon.uf.edex.bmh.ldad;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.IContextStateProcessor;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.RunProcess;

/**
 * The Ldad Disseminator will transfer custom generated audio files to a
 * specified host. The disseminator assumes that the necessary ssh keys will be
 * setup to allow the awips user to access the specified host without a
 * password.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014 3385       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadDisseminator implements IContextStateProcessor {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(LdadDisseminator.class);

    private static final String DEFAULT_SCP_BIN = "/usr/bin/scp";

    private static final String BMH_LDAD_SCP_PROPERTY = "bmh.ldad.scp";

    private static final String SCP_USER = "awips";

    private static final String AT_HOST = "@";

    private static final String TO_DIRECTORY = ":";

    private static final String CMD_LINE_ARG_SEPARATOR = " ";

    private static final String DELIMITED_QUOTE = "\"";

    private LdadConfigDao ldadConfigDao;

    private String ldadScp;

    private void initialize() {
        statusHandler.info("Initializing the Ldad Disseminator ...");

        this.validateDaos();

        /*
         * Verify that the location of the scp executable is known.
         */
        this.ldadScp = System.getProperty(BMH_LDAD_SCP_PROPERTY,
                DEFAULT_SCP_BIN);
        if (Files.exists(Paths.get(this.ldadScp)) == false) {
            throw new RuntimeException(
                    "Unable to find the scp executable. Please specify the scp location using the "
                            + BMH_LDAD_SCP_PROPERTY + " property.");
        }

        statusHandler.info("Initialization Successful!");
    }

    private void validateDaos() {
        if (this.ldadConfigDao == null) {
            throw new IllegalStateException(
                    "LdadConfigDao has not been set on the LdadDisseminator");
        }
    }

    /**
     * Disseminates the audio specified by the specified {@LdadMsg}.
     * The audio destination is determined based on the {@link LdadConfig}
     * mapped to {@link LdadMsg#getLdadId()}.
     * 
     * @param message
     *            the specified {@LdadMsg}.
     * @throws Exception
     *             if the specified {@LdadMsg} is NULL
     */
    public void process(LdadMsg message) throws Exception {
        if (message == null) {
            throw new Exception(
                    "Receieved an uninitialized or incomplete LdadMsg to process!");
        }

        statusHandler
                .info("Disseminating ldad content for ldad configuration: "
                        + message.getLdadId() + " ...");

        /*
         * Retrieve the associated ldad configuration.
         */
        LdadConfig ldadConfig = this.ldadConfigDao.getByID(message.getLdadId());
        if (ldadConfig == null) {
            statusHandler.error(
                    BMH_CATEGORY.LDAD_ERROR,
                    "Failed to retrieve/find the ldad configuration with id: "
                            + message.getLdadId()
                            + ". Unable to disseminate ldad msg: "
                            + message.toString() + ".");
            return;
        }

        /*
         * Build the scp command line
         */
        final String scpCommadLine = this.buildScpCommandLine(message,
                ldadConfig);

        /*
         * Run the scp command.
         */
        statusHandler.info("Running scp for ldad configuration: "
                + ldadConfig.getName() + " ...");

        ITimer scpTimer = TimeUtil.getTimer();

        scpTimer.start();
        RunProcess scpProcess = RunProcess.getRunProcess().exec(scpCommadLine);

        /*
         * Wait for the scp operation to finish.
         */
        int exitCode = scpProcess.waitFor();

        /*
         * scp process has finished.
         */
        scpTimer.stop();

        /*
         * Determine if the scp operation was successful or not.
         */
        // 0 indicates success
        if (exitCode == 0) {
            statusHandler.info("The scp process for ldad configuration: "
                    + ldadConfig.getName() + " successfully finished in "
                    + TimeUtil.prettyDuration(scpTimer.getElapsedTime()) + ".");
            return;
        }

        StringBuilder errMsg = new StringBuilder(
                "Failed to complete the scp process for ldad configuration: ");
        errMsg.append(ldadConfig.getName());
        errMsg.append(" in ");
        errMsg.append(TimeUtil.prettyDuration(scpTimer.getElapsedTime()));
        errMsg.append("!");
        // all other exit codes indicate failure
        if (exitCode == RunProcess.INTERRUPTED) {
            errMsg.append(" The scp process did not finish because it was interrupted.");
        } else {
            errMsg.append(" The scp process exited with error code (");
            errMsg.append(exitCode);
            errMsg.append(").");
            // determine if additional details exist in std err
            if (scpProcess.getStderr() != null
                    && scpProcess.getStderr().trim().isEmpty() == false) {
                errMsg.append(". ERROR: ");
                errMsg.append(scpProcess.getStderr().trim());
            }
        }

        // failure notification.
        statusHandler.error(BMH_CATEGORY.LDAD_ERROR, errMsg.toString());
    }

    /**
     * Constructs the scp command line that will be used to transfer the audio
     * file to the destination.
     * 
     * @param message
     *            specifies the file that will be transferred
     * @param ldadConfig
     *            species the host and destination for the transfer
     * @return the scp command line
     */
    private String buildScpCommandLine(final LdadMsg message,
            final LdadConfig ldadConfig) {
        StringBuilder stringBuilder = new StringBuilder(this.ldadScp);
        stringBuilder.append(CMD_LINE_ARG_SEPARATOR);
        // specify the file.
        stringBuilder.append(DELIMITED_QUOTE);
        stringBuilder.append(message.getOutputName());
        stringBuilder.append(DELIMITED_QUOTE);

        stringBuilder.append(CMD_LINE_ARG_SEPARATOR);
        // specify the destination
        stringBuilder.append(SCP_USER);
        stringBuilder.append(AT_HOST);
        stringBuilder.append(ldadConfig.getHost());
        stringBuilder.append(TO_DIRECTORY);
        stringBuilder.append(DELIMITED_QUOTE);
        stringBuilder.append(ldadConfig.getDirectory());
        stringBuilder.append(DELIMITED_QUOTE);

        return stringBuilder.toString();
    }

    /**
     * @return the ldadConfigDao
     */
    public LdadConfigDao getLdadConfigDao() {
        return ldadConfigDao;
    }

    /**
     * @param ldadConfigDao
     *            the ldadConfigDao to set
     */
    public void setLdadConfigDao(LdadConfigDao ldadConfigDao) {
        this.ldadConfigDao = ldadConfigDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#preStart()
     */
    @Override
    public void preStart() {
        this.initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#postStart()
     */
    @Override
    public void postStart() {
        // Do Nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#preStop()
     */
    @Override
    public void preStop() {
        // Do Nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#postStop()
     */
    @Override
    public void postStop() {
        // Do Nothing
    }
}