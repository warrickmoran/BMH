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
package com.raytheon.uf.common.bmh.audio;

import java.util.List;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;

/**
 * BMH Audio Regulator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 23, 2014 3424       bkowal      Initial creation
 * Aug 25, 2014 3552       bkowal      Algorithm updates to ensure that audio
 *                                     samples are not adjusted twice { attenuated the
 *                                     first time and amplified the second or vice
 *                                     verse }.
 * Sep 3, 2014  3532       bkowal      Updated the algorithm to use a target instead of
 *                                     a range.
 * Nov 24, 2014 3863       bkowal      Use {@link BMHAudioConstants}.
 * Feb 09, 2015 4091       bkowal      Use {@link EdexAudioConverterManager}.
 * Apr 09, 2015 4365       bkowal      Eliminated unnecessary byte[] creation. Reuse arrays
 *                                     during conversions and regulation.
 * Jun 29, 2015 4602       bkowal      Support both audio attenuation and amplification.
 * Jul 01, 2015 4602       bkowal      Do not attenuate/amplify extremely quiet audio.
 * Jul 13, 2015 4636       bkowal      Do not alter extremely quiet audio.
 * Jul 14, 2015 4636       rjpeter     Check entire stream for max.
 * Jul 15, 2015 4636       bkowal      Increased visibility of a few methods for abstraction.
 * Aug 17, 2015 4757       bkowal      Relocated to BMH common.
 * Aug 24, 2015 4770       bkowal      The decibel silence limit is now configurable.
 * Aug 25, 2015 4771       bkowal      Re-factored to support additional audio regulators.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LinearAudioRegulator extends AbstractAudioRegulator {

    protected LinearAudioRegulator(
            final AudioRegulationConfiguration configuration) {
        super(configuration);
    }

    protected LinearAudioRegulator(
            final AudioRegulationConfiguration configuration,
            final List<byte[]> audioCollectionToRegulate) {
        super(configuration, audioCollectionToRegulate);
    }

    /**
     * Determines the minimum and maximum amplitudes to calculate the min and
     * max decibels respectively associated with the managed audio data.
     * 
     * @param audio
     *            the managed audio data
     * @param offset
     *            determines the first index in the managed audio array that
     *            should be used when calculating the boundary signals
     * @param length
     *            the number of bytes from the offset to use when calculating
     *            the boundary signals
     * @return the calculated decibel range
     */
    protected Range calculateBoundarySignals(final byte[] audio, int offset,
            int length) {
        double runningMinAmplitude = 0.0;
        double runningMaxAmplitude = 0.0;

        for (int i = offset; i < (offset + length); i += 2) {
            short amplitude = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));
            amplitude = (short) Math.abs(amplitude);

            if (i == 0) {
                runningMinAmplitude = amplitude;
                runningMaxAmplitude = amplitude;
            } else {
                /*
                 * We use the quietest audible signal to calculate the minimum
                 * amplitude rather than the lack of audio. For the lack of
                 * audio, when the amplitude is 0, the associated dB value is
                 * -infinity.
                 */
                if ((runningMinAmplitude == 0 || amplitude < runningMinAmplitude)
                        && amplitude != 0) {
                    runningMinAmplitude = amplitude;
                }
                if (amplitude > runningMaxAmplitude) {
                    runningMaxAmplitude = amplitude;
                }
            }
        }

        return new DoubleRange(calculateDecibels(runningMinAmplitude),
                calculateDecibels(runningMaxAmplitude));
    }
}