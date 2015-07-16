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
import java.nio.file.Path;
import java.util.TimeZone;

import com.raytheon.uf.edex.bmh.dactransmit.DAC_MODE;

/**
 * Configuration parameters for a DacSession object. Defines all the necessary
 * parameters to construct a DacSession so it can transmit data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Added transmitter group.
 * Jul 17, 2014  #3399     bsteffen     Add comms manager port argument.
 * Aug 12, 2014  #3486     bsteffen     Remove tranmistter group name
 * Aug 18, 2014  #3532     bkowal       Add transmitter decibel range.
 * Sep 4, 2014   #3532     bkowal       Use a decibel target instead of a range.
 * Oct 2, 2014   #3642     bkowal       Add transmitter timezone.
 * Oct 22, 2014  #3687     bsteffen    keep original dac hostname
 * Nov 7, 2014   #3630     bkowal       Refactor for maintenance mode.
 * Apr 29, 2015  #4394     bkowal       The manager port is now required for all
 *                                      dac modes.
 * Jul 08, 2015  #4636     bkowal       Support same and alert decibel levels.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSessionConfig extends AbstractDacConfig {

    private final Path inputDirectory;

    private final TimeZone timezone;

    private final double sameDbTarget;

    private final double alertDbTarget;

    public DacSessionConfig(DacCommonConfig commonConfig, Path inputDirectory,
            TimeZone timezone, double sameDbTarget, double alertDbTarget) {
        super(DAC_MODE.OPERATIONAL, commonConfig);
        this.inputDirectory = inputDirectory;
        this.timezone = timezone;
        this.sameDbTarget = sameDbTarget;
        this.alertDbTarget = alertDbTarget;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("DacSessionConfig [");
        stringBuilder.append(super.toString());
        stringBuilder.append(", inputDirectory=");
        stringBuilder.append(this.inputDirectory);
        stringBuilder.append(", timezone=");
        stringBuilder.append(this.timezone.getID());
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public Path getInputDirectory() {
        return inputDirectory;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * @return the sameDbTarget
     */
    public double getSameDbTarget() {
        return sameDbTarget;
    }

    /**
     * @return the alertDbTarget
     */
    public double getAlertDbTarget() {
        return alertDbTarget;
    }

    @Override
    public IDacSession buildDacSession() throws IOException {
        return new DacSession(this);
    }
}