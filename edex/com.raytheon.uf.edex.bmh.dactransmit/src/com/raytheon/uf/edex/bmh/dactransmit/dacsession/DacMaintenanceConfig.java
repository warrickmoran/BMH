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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.nio.file.Path;

import com.raytheon.uf.edex.bmh.dactransmit.DAC_MODE;

/**
 * Configuration information required by a dac session when in maintenance mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Apr 09, 2015 4364       bkowal      Added {@link #executionTimeout}.
 * Apr 24, 2015 4394       bkowal      Field renaming based on usage.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceConfig extends AbstractDacConfig {

    private final Path messageFilePath;

    private final Integer testDuration;

    private final int executionTimeout;

    private final short transferAmplitude;

    /**
     * @param mode
     * @param commonConfig
     */
    public DacMaintenanceConfig(DacCommonConfig commonConfig,
            Path messageFilePath, int testDuration, int executionTimeout,
            short transferAmplitude) {
        super(DAC_MODE.MAINTENANCE, commonConfig);
        this.messageFilePath = messageFilePath;
        this.testDuration = testDuration;
        this.executionTimeout = executionTimeout;
        this.transferAmplitude = transferAmplitude;
    }

    /**
     * @return the messageFilePath
     */
    public Path getMessageFilePath() {
        return messageFilePath;
    }

    /**
     * @return the testDuration
     */
    public Integer getTestDuration() {
        return testDuration;
    }

    /**
     * @return the executionTimeout
     */
    public int getExecutionTimeout() {
        return executionTimeout;
    }

    /**
     * @return the transferAmplitude
     */
    public short getTransferAmplitude() {
        return transferAmplitude;
    }

    @Override
    public IDacSession buildDacSession() throws Exception {
        return new DacMaintenanceSession(this);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(
                "DacMaintenanceConfig [");
        stringBuilder.append(super.toString());
        stringBuilder.append(", inputAudio=");
        stringBuilder.append(this.messageFilePath.toString());
        stringBuilder.append(", testDuration=");
        stringBuilder.append(this.testDuration);
        stringBuilder.append(", executionTimeout=");
        stringBuilder.append(this.executionTimeout);
        stringBuilder.append(", transferAmplitude=");
        stringBuilder.append(this.transferAmplitude);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}