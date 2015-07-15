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
package com.raytheon.uf.edex.bmh.audio;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.Range;

import com.raytheon.uf.common.bmh.audio.BMHAudioConstants;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.PCMToUlawAlgorithm;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.UlawToPCMAlgorithm;

/**
 * Extension of {@link AudioRegulator} capable of regulating an entire
 * {@link Collection} of audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2015 4636       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CollectibleAudioRegulator extends AudioRegulator {

    private final List<byte[]> audioCollectionToRegulate;

    /**
     * The peak decibel level across the entire audio {@link Collection}.
     */
    private Double maxDbRange = -Double.MAX_VALUE;

    /**
     * The minimum decibel level across the entire audio {@link Collection}.
     */
    private Double minDbRange = Double.MAX_VALUE;

    /**
     * 
     */
    public CollectibleAudioRegulator(List<byte[]> audioCollectionToRegulate) {
        if (audioCollectionToRegulate == null
                || audioCollectionToRegulate.isEmpty()) {
            throw new IllegalArgumentException(
                    "Required audioCollectionToRegulate parameter cannot be NULL or empty.");
        }
        this.audioCollectionToRegulate = new LinkedList<>(
                audioCollectionToRegulate);
    }

    public List<byte[]> regulateAudioCollection(final double dbTarget)
            throws Exception {
        /*
         * Determine the decibel ranges.
         */
        byte[] pcmAudio = new byte[this.audioCollectionToRegulate.get(0).length * 2];
        for (byte[] ulawAudio : this.audioCollectionToRegulate) {
            /*
             * Have to create the conversion array every time because we cannot
             * be certain that all audio arrays in the {@link Collection} are
             * the same length.
             * 
             * TODO: Worth tracking the maximum length so that a single array
             * can be created for use during the actual conversion process and
             * only a subset of the elements would be touched, when necessary?
             */
            if (pcmAudio.length != ulawAudio.length * 2) {
                pcmAudio = new byte[ulawAudio.length * 2];
            }
            UlawToPCMAlgorithm
                    .convert(ulawAudio, 0, ulawAudio.length, pcmAudio);
            Range dbRange = this.calculateBoundarySignals(pcmAudio, 0,
                    pcmAudio.length);
            if (dbRange.getMaximumDouble() != Double.NEGATIVE_INFINITY) {
                this.maxDbRange = Math.max(dbRange.getMaximumDouble(),
                        this.maxDbRange);
            }
            this.minDbRange = Math.min(dbRange.getMinimumDouble(),
                    this.minDbRange);
        }

        /*
         * TODO: Determine if the audio will need to be regulated based on the
         * calculated decibel range?
         */
        if ((this.maxDbRange == Double.NEGATIVE_INFINITY && this.minDbRange == Double.NEGATIVE_INFINITY)
                || this.maxDbRange == dbTarget
                || this.maxDbRange <= DB_SILENCE_LIMIT) {
            return this.audioCollectionToRegulate;
        }

        /*
         * Calculate the amount of adjustment required.
         */
        double difference = dbTarget - this.maxDbRange;
        double adjustmentRate = Math.pow(
                BMHAudioConstants.DB_ALTERATION_CONSTANT,
                (difference / BMHAudioConstants.AMPLITUDE_TO_DB_CONSTANT));

        /*
         * Alter the audio.
         */
        for (byte[] ulawAudio : this.audioCollectionToRegulate) {
            if (pcmAudio.length != ulawAudio.length * 2) {
                pcmAudio = new byte[ulawAudio.length * 2];
            }
            UlawToPCMAlgorithm
                    .convert(ulawAudio, 0, ulawAudio.length, pcmAudio);
            this.regulateAudioVolume(pcmAudio, adjustmentRate, 0,
                    pcmAudio.length);
            PCMToUlawAlgorithm.convert(pcmAudio, 0, pcmAudio.length, ulawAudio,
                    0);
        }

        return this.audioCollectionToRegulate;
    }
}