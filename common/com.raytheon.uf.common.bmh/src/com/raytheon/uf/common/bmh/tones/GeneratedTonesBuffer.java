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
package com.raytheon.uf.common.bmh.tones;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link Object} used to store SAME & Alert Tone audio. Created so that they can be extracted
 * and attenuated/amplified independently of one another.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 8, 2015  4636       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public final class GeneratedTonesBuffer {

    @DynamicSerializeElement
    private byte[] sameTones;

    @DynamicSerializeElement
    private byte[] beforeAlertTonePause;

    @DynamicSerializeElement
    private byte[] alertTones;

    @DynamicSerializeElement
    private byte[] beforeMessagePause;

    public GeneratedTonesBuffer() {
    }

    public ByteBuffer combineTonesArray() {
        int totalSize = 0;
        List<byte[]> dataList = new LinkedList<>();
        if (sameTones != null) {
            totalSize += sameTones.length;
            dataList.add(sameTones);
        }
        if (beforeAlertTonePause != null) {
            totalSize += beforeAlertTonePause.length;
            dataList.add(beforeAlertTonePause);
        }
        if (alertTones != null) {
            totalSize += alertTones.length;
            dataList.add(alertTones);
        }
        if (beforeMessagePause != null) {
            totalSize += beforeMessagePause.length;
            dataList.add(beforeMessagePause);
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        for (byte[] data : dataList) {
            buffer.put(data);
        }
        return buffer;
    }

    public int length() {
        int totalSize = 0;
        if (sameTones != null) {
            totalSize += sameTones.length;
        }
        if (beforeAlertTonePause != null) {
            totalSize += beforeAlertTonePause.length;
        }
        if (alertTones != null) {
            totalSize += alertTones.length;
        }
        if (beforeMessagePause != null) {
            totalSize += beforeMessagePause.length;
        }

        return totalSize;
    }

    /**
     * @return the sameTones
     */
    public byte[] getSameTones() {
        return sameTones;
    }

    /**
     * @param sameTones
     *            the sameTones to set
     */
    public void setSameTones(byte[] sameTones) {
        this.sameTones = sameTones;
    }

    /**
     * @return the beforeAlertTonePause
     */
    public byte[] getBeforeAlertTonePause() {
        return beforeAlertTonePause;
    }

    /**
     * @param beforeAlertTonePause
     *            the beforeAlertTonePause to set
     */
    public void setBeforeAlertTonePause(byte[] beforeAlertTonePause) {
        this.beforeAlertTonePause = beforeAlertTonePause;
    }

    /**
     * @return the alertTones
     */
    public byte[] getAlertTones() {
        return alertTones;
    }

    /**
     * @param alertTones
     *            the alertTones to set
     */
    public void setAlertTones(byte[] alertTones) {
        this.alertTones = alertTones;
    }

    /**
     * @return the beforeMessagePause
     */
    public byte[] getBeforeMessagePause() {
        return beforeMessagePause;
    }

    /**
     * @param beforeMessagePause
     *            the beforeMessagePause to set
     */
    public void setBeforeMessagePause(byte[] beforeMessagePause) {
        this.beforeMessagePause = beforeMessagePause;
    }
}