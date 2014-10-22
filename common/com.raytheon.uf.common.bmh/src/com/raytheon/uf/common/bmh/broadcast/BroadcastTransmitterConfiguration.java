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
package com.raytheon.uf.common.bmh.broadcast;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to store information that will be used to prepare a transmitter for a
 * live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2014 3655       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastTransmitterConfiguration {

    @DynamicSerializeElement
    private Transmitter transmitter;

    @DynamicSerializeElement
    private byte[] toneAudio;

    @DynamicSerializeElement
    private long delayMilliseconds;

    /**
     * 
     */
    public BroadcastTransmitterConfiguration() {
    }

    public Transmitter getTransmitter() {
        return transmitter;
    }

    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    public byte[] getToneAudio() {
        return toneAudio;
    }

    public void setToneAudio(byte[] toneAudio) {
        this.toneAudio = toneAudio;
    }

    public long getDelayMilliseconds() {
        return delayMilliseconds;
    }

    public void setDelayMilliseconds(long delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
    }
}