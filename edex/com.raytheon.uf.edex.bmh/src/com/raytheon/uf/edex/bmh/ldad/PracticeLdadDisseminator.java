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

import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * Practice version of the ldad disseminator that ensures that all data is only
 * disseminated to localhost. When cp is known, {@link PracticeLdadDisseminator}
 * will override the scp command with a cp command.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 20, 2014 3385       bkowal      Initial creation
 * Jan 05, 2015 3651       bkowal      Use {@link IMessageLogger} to log message errors.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PracticeLdadDisseminator extends LdadDisseminator {

    private static final String PRACTICE_HOST = "localhost";

    private static final String DEFAULT_CP_BIN = "/bin/cp";

    private String ldadCp;

    /*
     * quoted spaced file names are handled correctly on localhost.
     */
    private static final String ESCAPE_CP_SPACE = "\\";

    /**
     * 
     */
    public PracticeLdadDisseminator(final IMessageLogger messageLogger) {
        super(messageLogger);
        if (Files.exists(Paths.get(DEFAULT_CP_BIN))) {
            this.ldadCp = DEFAULT_CP_BIN;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.ldad.LdadDisseminator#buildScpCommandLine(com
     * .raytheon.uf.edex.bmh.ldad.LdadMsg,
     * com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig)
     */
    @Override
    protected String buildScpCommandLine(final LdadMsg message,
            LdadConfig ldadConfig) {
        ldadConfig.setHost(PRACTICE_HOST);
        /*
         * if cp is known create a cp command for practice mode.
         */
        if (this.ldadCp == null) {
            /*
             * we do not know where cp is, we will have to do an scp to
             * localhost.
             */
            return super.buildScpCommandLine(message, ldadConfig);
        }

        StringBuilder stringBuilder = new StringBuilder(this.ldadCp);
        stringBuilder.append(CONSTANT_SPACE);
        stringBuilder.append(message.getOutputName().replaceAll(CONSTANT_SPACE,
                ESCAPE_CP_SPACE));
        stringBuilder.append(CONSTANT_SPACE);
        stringBuilder.append(ldadConfig.getDirectory().replaceAll(
                CONSTANT_SPACE, ESCAPE_CP_SPACE));

        return stringBuilder.toString();
    }
}