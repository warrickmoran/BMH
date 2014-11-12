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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

import com.raytheon.uf.edex.bmh.dactransmit.DAC_MODE;

/**
 * Configuration parameters that will be used by a dac session regardless of the
 * specified {@link DAC_MODE}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractDacConfig {

    private final DAC_MODE mode;

    private final DacCommonConfig commonConfig;

    /**
     * 
     */
    public AbstractDacConfig(final DAC_MODE mode,
            final DacCommonConfig commonConfig) {
        this.mode = mode;
        this.commonConfig = commonConfig;
    }

    public abstract IDacSession buildDacSession() throws IOException;

    /**
     * @return the mode
     */
    public DAC_MODE getMode() {
        return mode;
    }

    /**
     * The original hostname for the dac as it was received from the command
     * line. This may be an actual hostname or a string representation of the IP
     * address. Depending on the format this may be equivelant to
     * {@link #getDacAddress()}.getHostName() but this is not guaranteed.
     * 
     * @return
     */
    public String getDacHostname() {
        return this.commonConfig.getDacHostname();
    }

    /**
     * The resolved address of the {@link #dacHostname}.
     * 
     * @return
     */
    public InetAddress getDacAddress() {
        return this.commonConfig.getDacAddress();
    }

    public int getDataPort() {
        return this.commonConfig.getDataPort();
    }

    public int getControlPort() {
        return this.commonConfig.getControlPort();
    }

    public Collection<Integer> getTransmitters() {
        return this.commonConfig.getTransmitters();
    }

    /**
     * @return the dbTarget
     */
    public double getDbTarget() {
        return this.commonConfig.getDbTarget();
    }

    @Override
    public String toString() {
        return this.commonConfig.toString();
    }
}