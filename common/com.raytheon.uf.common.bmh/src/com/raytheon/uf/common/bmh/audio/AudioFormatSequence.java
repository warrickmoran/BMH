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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to a build a chain of 3 or more {@link BMHAudioFormat}s that can be
 * linked by {@link IAudioConverter}s provided that a path exists.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 13, 2016 5177       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioFormatSequence {

    private final BMHAudioFormat currentFormat;

    private final List<BMHAudioFormat> formatSequence;

    public AudioFormatSequence(BMHAudioFormat beginningFormat) {
        this.currentFormat = beginningFormat;
        formatSequence = new LinkedList<>();
    }

    public AudioFormatSequence(AudioFormatSequence audioFormatSequence,
            BMHAudioFormat beginningFormat) {
        this.formatSequence = new LinkedList<>(
                audioFormatSequence.formatSequence);
        this.formatSequence.add(audioFormatSequence.currentFormat);

        this.currentFormat = beginningFormat;
    }

    public List<BMHAudioFormat> getSequence() {
        List<BMHAudioFormat> sequence = new LinkedList<>();
        if (this.formatSequence.isEmpty() == false) {
            sequence.addAll(formatSequence);
        }
        sequence.add(this.currentFormat);

        return sequence;
    }

    public boolean sequenceFinished(BMHAudioFormat finalFormat) {
        return this.currentFormat == finalFormat;
    }

    public List<AudioFormatSequence> continueSequence(
            Collection<IAudioConverter> availableConverters) {
        final List<AudioFormatSequence> sequenceContinuation = new ArrayList<>();

        for (IAudioConverter converter : availableConverters) {
            if (converter.getSupportedSourceFormats().contains(
                    this.currentFormat)
                    && this.formatSequence
                            .contains(converter.getOutputFormat()) == false) {
                sequenceContinuation.add(new AudioFormatSequence(this,
                        converter.getOutputFormat()));
            }
        }

        return sequenceContinuation;
    }
}