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
package com.raytheon.uf.common.bmh.dac.tones;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;

/**
 * Container class for static tone patterns.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 12, 2014  #3286     dgilling     Initial creation
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

final class StaticTones {

    private static final byte[] PREAMBLE_CODE = { (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB };

    private static final int SILENCE_BEWTWEEN_PREAMBLE = 8000;

    private static final int SILENCE_BEFORE_ALERT = 2 * 8000;

    private static final int SILENCE_BEFORE_MESSAGE = 4 * 8000;

    private static final int SILENCE_AFTER_MESSAGE = 2 * 8000;

    private static final double ALERT_TONE_DURATION = 9.0;

    // TODO find right default value for this
    private static final double ALERT_TONE_AMPLITUDE = 8192.0;

    private static final String END_OF_MESSAGE_CODE = "NNNN";

    private final byte[] preambleTones;

    private final byte[] betweenPreambleOrClosingPause;

    private final byte[] beforeAlertTonePause;

    private final byte[] alertTone;

    private final byte[] beforeMessagePause;

    private final ByteBuffer endOfMessageTones;

    StaticTones() throws ToneGenerationException {
        this.preambleTones = TonesManager.generateSAMETone(PREAMBLE_CODE);
        this.betweenPreambleOrClosingPause = generateSilence(SILENCE_BEWTWEEN_PREAMBLE);
        this.beforeAlertTonePause = generateSilence(SILENCE_BEFORE_ALERT);
        this.alertTone = TonesManager.generateAlertTone(ALERT_TONE_AMPLITUDE,
                ALERT_TONE_DURATION);
        this.beforeMessagePause = generateSilence(SILENCE_BEFORE_MESSAGE);

        byte[] eomTone = TonesManager.generateSAMETone(END_OF_MESSAGE_CODE);
        byte[] afterMessagePause = generateSilence(SILENCE_AFTER_MESSAGE);
        int eomBufferSize = afterMessagePause.length
                + (3 * (this.preambleTones.length + eomTone.length))
                + (2 * this.betweenPreambleOrClosingPause.length);
        this.endOfMessageTones = ByteBuffer.allocate(eomBufferSize);
        this.endOfMessageTones.put(afterMessagePause);
        this.endOfMessageTones.put(this.preambleTones).put(eomTone);
        this.endOfMessageTones.put(this.betweenPreambleOrClosingPause);
        this.endOfMessageTones.put(this.preambleTones).put(eomTone);
        this.endOfMessageTones.put(this.betweenPreambleOrClosingPause);
        this.endOfMessageTones.put(this.preambleTones).put(eomTone);
    }

    private static byte[] generateSilence(int numBytes) {
        byte[] silence = new byte[numBytes];
        Arrays.fill(silence, DacSessionConstants.SILENCE);
        return silence;
    }

    public byte[] getPreambleTones() {
        return preambleTones;
    }

    public byte[] getBetweenPreambleOrClosingPause() {
        return betweenPreambleOrClosingPause;
    }

    public byte[] getBeforeAlertTonePause() {
        return beforeAlertTonePause;
    }

    public byte[] getAlertTone() {
        return alertTone;
    }

    public byte[] getBeforeMessagePause() {
        return beforeMessagePause;
    }

    public ByteBuffer getEndOfMessageTones() {
        return endOfMessageTones;
    }
}
