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
package com.raytheon.bmh.dactransmit.ipc;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message sent from comms manager to dac transmit to indicate the amplitude
 * target has been altered for a transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014 3532       bkowal      Initial creation
 * Sep 4, 2014  3532       bkowal      Change to support a single decibel target
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ChangeAmplitudeTarget {

    /*
     * The new amplitude to use.
     */
    @DynamicSerializeElement
    private short audioAmplitude;

    @DynamicSerializeElement
    private short sameAmplitude;

    @DynamicSerializeElement
    private short alertAmplitude;

    /**
     * Constructor.
     */
    public ChangeAmplitudeTarget() {
    }

    public ChangeAmplitudeTarget(short audioAmplitude, short sameAmplitude,
            short alertAmplitude) {
        this.audioAmplitude = audioAmplitude;
        this.sameAmplitude = sameAmplitude;
        this.alertAmplitude = alertAmplitude;
    }

    /**
     * @return the audioAmplitude
     */
    public short getAudioAmplitude() {
        return audioAmplitude;
    }

    /**
     * @param audioAmplitude
     *            the audioAmplitude to set
     */
    public void setAudioAmplitude(short audioAmplitude) {
        this.audioAmplitude = audioAmplitude;
    }

    /**
     * @return the sameAmplitude
     */
    public short getSameAmplitude() {
        return sameAmplitude;
    }

    /**
     * @param sameAmplitude
     *            the sameAmplitude to set
     */
    public void setSameAmplitude(short sameAmplitude) {
        this.sameAmplitude = sameAmplitude;
    }

    /**
     * @return the alertAmplitude
     */
    public short getAlertAmplitude() {
        return alertAmplitude;
    }

    /**
     * @param alertAmplitude
     *            the alertAmplitude to set
     */
    public void setAlertAmplitude(short alertAmplitude) {
        this.alertAmplitude = alertAmplitude;
    }
}