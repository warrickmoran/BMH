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

/**
 * An audio converter that uses multiple intermediary {@link IAudioConverter}s
 * to convert audio from one format to another that a single converter does not
 * exist for.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 14, 2016 5177       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ChainedAudioConverter extends AbstractAudioConverter implements
        IAudioConverter {

    private final List<IAudioConverter> audioPath;

    public ChainedAudioConverter(BMHAudioFormat sourceFormat,
            BMHAudioFormat destinationFormat, List<IAudioConverter> audioPath) {
        super(destinationFormat, new BMHAudioFormat[] { sourceFormat });
        this.audioPath = audioPath;
    }

    @Override
    public byte[] convertAudio(byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException {
        /*
         * Iterate over the {@link #audioPath} through all of the intermediary
         * and final {@link IAudioConverter}s.
         */
        BMHAudioFormat currentFormat = srcFormat;
        for (IAudioConverter audioConverter : this.audioPath) {
            src = audioConverter.convertAudio(src, currentFormat);
            currentFormat = audioConverter.getOutputFormat();
        }

        return src;
    }

    @Override
    public void verifyCompatibility() throws ConversionNotSupportedException {
        /*
         * Do nothing. This uses a chain of already verified {@link
         * IAudioConverter}s.
         */
    }
}