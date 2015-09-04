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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;

/**
 * Audio Regulation Algorithm that excludes outliers a standard deviation or
 * more away when determining how the audio should be altered.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 25, 2015 4771       bkowal      Initial creation
 * Aug 27, 2015 4771       bkowal      Handle edge cases with no to little variance in
 *                                     which there is no standard deviation.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DeviationAudioRegulator extends AbstractAudioRegulator {

    private static final int DEVIATION_RANGE = 1;

    /**
     * @param configuration
     */
    protected DeviationAudioRegulator(AudioRegulationConfiguration configuration) {
        super(configuration);
    }

    protected DeviationAudioRegulator(
            AudioRegulationConfiguration configuration,
            final List<byte[]> audioCollectionToRegulate) {
        super(configuration, audioCollectionToRegulate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.audio.AbstractAudioRegulator#
     * calculateBoundarySignals(byte[], int, int)
     */
    @Override
    protected Range calculateBoundarySignals(byte[] audio, int offset,
            int length) {
        /*
         * First, determine the decibel levels of every segment of audio.
         */
        List<Double> audioDecibels = new ArrayList<>(audio.length / 2);
        double decibelSampleCount = 0;
        double decibelSum = 0.0;
        for (int i = offset; i < (offset + length); i += 2) {
            short amplitude = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));
            amplitude = (short) Math.abs(amplitude);

            double dbValue = calculateDecibels(amplitude);
            /*
             * Silence indicated by +/- infinity is eliminated to avoid
             * completely corrupting the data.
             */
            if (dbValue != Double.NEGATIVE_INFINITY
                    && dbValue != Double.POSITIVE_INFINITY) {
                audioDecibels.add(dbValue);
                ++decibelSampleCount;
                decibelSum += dbValue;
            }
        }

        /*
         * Now calculate the standard deviation.
         */
        if (decibelSampleCount == 0) {
            /*
             * All samples were negative or positive infinity.
             */
            return new DoubleRange(Double.NEGATIVE_INFINITY,
                    Double.NEGATIVE_INFINITY);
        }
        double mean = decibelSum / decibelSampleCount;
        double varianceSum = 0.0;
        for (double dbLevel : audioDecibels) {
            double square = Math.pow((dbLevel - mean), 2);
            varianceSum += square;
        }
        final double stdDeviation = Math.sqrt(varianceSum / decibelSampleCount);
        final double minimumDb = mean - (stdDeviation * DEVIATION_RANGE);
        final double maximumDb = mean + (stdDeviation * DEVIATION_RANGE);

        if (stdDeviation == 0) {
            /*
             * All values are exactly the same; so there is no variance.
             */
            return new DoubleRange(minimumDb, maximumDb);
        }

        /*
         * Finally, determine the minimum and maximum decibel ranges while
         * including all decibel levels that are within a standard deviation of
         * the mean.
         */
        double rangeMinDb = Double.MAX_VALUE;
        double rangeMaxDb = -Double.MAX_VALUE;
        boolean rangeCalculated = false;
        for (double dbLevel : audioDecibels) {
            if (dbLevel >= minimumDb && dbLevel <= maximumDb) {
                rangeCalculated = true;
                rangeMinDb = Math.min(rangeMinDb, dbLevel);
                rangeMaxDb = Math.max(rangeMaxDb, dbLevel);
            }
        }

        if (rangeCalculated == false) {
            /*
             * There was a slight variance; but, it was extremely small.
             */
            return new DoubleRange(minimumDb, maximumDb);
        }

        if (rangeMaxDb == Double.NaN) {
            rangeMinDb = Double.NEGATIVE_INFINITY;
            rangeMaxDb = Double.NEGATIVE_INFINITY;
        }

        return new DoubleRange(rangeMinDb, rangeMaxDb);
    }
}