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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Tonal information for a transmitter group.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@DynamicSerialize
@Embeddable
public class Tone {
    @Column
    @DynamicSerializeElement
    private Integer alertToneAmplitude = null;

    @Column
    @DynamicSerializeElement
    private Integer alertToneDuration = null;

    @Column
    @DynamicSerializeElement
    private Integer transferToneAmplitude = null;

    @Column
    @DynamicSerializeElement
    private Integer transferToneDuration = null;

    @Column
    @DynamicSerializeElement
    private Integer sameToneAmplitude = null;

    @Column
    @DynamicSerializeElement
    private Integer sameToneDuration = null;

    @Column
    @DynamicSerializeElement
    private Integer voiceAmplitude = null;

    @Column
    @DynamicSerializeElement
    private Integer amplitude = null;

    public Integer getAlertToneAmplitude() {
        return alertToneAmplitude;
    }

    public void setAlertToneAmplitude(Integer alertToneAmplitude) {
        this.alertToneAmplitude = alertToneAmplitude;
    }

    public Integer getAlertToneDuration() {
        return alertToneDuration;
    }

    public void setAlertToneDuration(Integer alertToneDuration) {
        this.alertToneDuration = alertToneDuration;
    }

    public Integer getTransferToneAmplitude() {
        return transferToneAmplitude;
    }

    public void setTransferToneAmplitude(Integer transferToneAmplitude) {
        this.transferToneAmplitude = transferToneAmplitude;
    }

    public Integer getTransferToneDuration() {
        return transferToneDuration;
    }

    public void setTransferToneDuration(Integer transferToneDuration) {
        this.transferToneDuration = transferToneDuration;
    }

    public Integer getSameToneAmplitude() {
        return sameToneAmplitude;
    }

    public void setSameToneAmplitude(Integer sameToneAmplitude) {
        this.sameToneAmplitude = sameToneAmplitude;
    }

    public Integer getSameToneDuration() {
        return sameToneDuration;
    }

    public void setSameToneDuration(Integer sameToneDuration) {
        this.sameToneDuration = sameToneDuration;
    }

    public Integer getVoiceAmplitude() {
        return voiceAmplitude;
    }

    public void setVoiceAmplitude(Integer voiceAmplitude) {
        this.voiceAmplitude = voiceAmplitude;
    }

    public Integer getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(Integer amplitude) {
        this.amplitude = amplitude;
    }
}
