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

import java.nio.charset.StandardCharsets;

/**
 * This is a port of the Legacy CRS MP fskwave.c algorithm that is maintained by
 * CommPower. The interaction of VM_AOsameMsg.c and fskwave.c were also taken
 * into account when developing this port.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2015 4299       bkowal      Initial creation
 * Jul 01, 2015 4206       rjpeter     BitSet ignores trailing 0 bits,
 *                                     updated to use bit masking.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class FskTonesEncoder {

    public static final byte[] PREAMBLE_CODE = { (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB,
            (byte) 0xAB, (byte) 0xAB, (byte) 0xAB, (byte) 0xAB };

    private static final byte[] MASK = { (byte) 0x01, (byte) 0x02, (byte) 0x04,
            (byte) 0x08, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80, };

    /*
     * Symbol definitions
     */
    private static final int TABLE_LENGTH = 1024;

    private static final double BMH_SAMPLE_RATE = 8000; // was 10,000 in CRS

    private static final double ONES_CONSTANT = 1.0;

    /*
     * for some reason that was not a constant in CRS.
     */
    private static final int AMPLITUDE = 8192;

    /*
     * includes additional decimal places when compared to the Java constant.
     */
    private static final double LEGACY_TWO_PI = 6.283185307179586476;

    // from VM_AO.h
    /* FSK low frequency tone */
    private static final double FSK_LOW_FREQ = 1562.5;

    /* FSK high frequency tone */
    private static final double FSK_HIGH_FREQ = 2083.333333333333333;

    /* FSK bit rate */
    private static final double FSK_BIT_RATE = 520.833333333333333;

    /*
     * Member variables
     */
    // includes an extra index due to Java rounding.
    private final short[] sSineTable = new short[TABLE_LENGTH + 1];

    private double samplePeriod;

    private double bitPeriod;

    private double dPhase;

    private double dLowPhaseIncrement;

    private double dHighPhaseIncrement;

    private double dTableLength;

    /**
     * Constructor
     */
    public FskTonesEncoder() {
        this.initialize();
    }

    /*
     * Port InitFsk in fskwave.c
     */
    private void initialize() {
        // Set the global rate parameters.
        this.bitPeriod = ONES_CONSTANT / FSK_BIT_RATE;
        this.samplePeriod = ONES_CONSTANT / BMH_SAMPLE_RATE;
        this.dTableLength = TABLE_LENGTH;

        // Calculate the low and high tone phase increments.
        this.dLowPhaseIncrement = FSK_LOW_FREQ * this.samplePeriod
                * this.dTableLength;
        this.dHighPhaseIncrement = FSK_HIGH_FREQ * this.samplePeriod
                * this.dTableLength;

        // Build the sine table: amplitudes for 0..2Pi spread across Table
        double mult = LEGACY_TWO_PI / this.dTableLength;

        for (int i = 0; i <= TABLE_LENGTH; i++) {
            double A = AMPLITUDE * Math.sin(mult * i);
            // round away from zero
            if (A >= 0.0) {
                A += 0.5;
            } else {
                A -= 0.5;
            }
            this.sSineTable[i] = (short) A;
        }
    }

    /*
     * Note in the crs version of VM_AOsameMsg.c (), the preamble and padding
     * were added to both the SAME tones and the End of Message Tones. The SAME
     * tones had four (4) bytes of padding and the End of Message Tones had two
     * (2) bytes of padding. For SAME tones, reference lines: 433 - 555 in
     * VM_AOsameMsg.c. For End of Message Tones, reference lines: 292 - 319 in
     * VM_AOsameMsg.c.
     */

    public short[] execute(final String SAMEMessage)
            throws ToneGenerationException {
        return this.execute(SAMEMessage.trim().getBytes(
                StandardCharsets.US_ASCII));
    }

    public short[] execute(final byte[] sameMessage)
            throws ToneGenerationException {
        return this.encode(sameMessage);
    }

    /*
     * Port FskSignal in fskwave.c (excluding wav file management)
     */
    private short[] encode(final byte[] sameMessage) {
        double dPhaseIncrement;
        this.dPhase = 0.0f;
        double lastTime = 0.0;

        int sameIndex = 0;
        int maskIndex = 0;
        int numSamples = (int) (bitPeriod * sameMessage.length * 8 / samplePeriod);
        short[] samples = new short[numSamples];
        int sampleIndex = 0;

        while (sameIndex < sameMessage.length) {
            if ((sameMessage[sameIndex] & MASK[maskIndex]) == 0) {
                dPhaseIncrement = this.dLowPhaseIncrement;
            } else {
                dPhaseIncrement = this.dHighPhaseIncrement;
            }

            /*
             * at the start of a Bit, offset the first sample's phase: add
             * residual phase from the last sample of the previous Bit, but use
             * the "new" Bit's phase increment, not the "old"'s
             */
            dPhase += (lastTime / this.samplePeriod) * dPhaseIncrement;

            if (dPhase >= this.dTableLength) {
                dPhase -= this.dTableLength;
            }

            /*
             * Calculate all samples for this bit.
             */
            double bitTime;
            for (bitTime = lastTime; bitTime <= this.bitPeriod; bitTime += this.samplePeriod) {
                /*
                 * rather than just use sSineTable[dPhase], interpolate for
                 * better accuracy (e.g. when doing coarse sampling for NWS)
                 */
                int iTableIndex = (int) dPhase;
                short sSample1 = this.sSineTable[iTableIndex];
                short sSample2 = this.sSineTable[iTableIndex + 1];
                short sInterpolate = (short) ((sSample2 - sSample1) * (dPhase - iTableIndex));

                /*
                 * store new value
                 */
                samples[sampleIndex++] = (short) (sSample1 + sInterpolate);

                /*
                 * increment to next phase, for next sample
                 */
                dPhase += dPhaseIncrement;
                if (dPhase >= this.dTableLength) {
                    dPhase -= this.dTableLength;
                }
            }

            /*
             * time of first sample of next Bit = the amount of "overshoot"
             */
            lastTime = bitTime - this.bitPeriod;
            /*
             * compensate for the excess increment above at end of Bit... back
             * up to zero crossing, by subtracting proportional amount of the
             * "old" Bit's phase increment
             */
            dPhase -= (lastTime / this.samplePeriod) * dPhaseIncrement;
            if (dPhase < 0) {
                dPhase += this.dTableLength;
            }

            // advance to next bit
            maskIndex++;
            if (maskIndex >= MASK.length) {
                maskIndex = 0;
                sameIndex++;
            }
        }

        return samples;
    }
}