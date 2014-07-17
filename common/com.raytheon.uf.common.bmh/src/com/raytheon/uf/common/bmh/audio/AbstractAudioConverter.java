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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract representation of an audio converter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014 3383       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractAudioConverter implements IAudioConverter {

    private final BMHAudioFormat outputFormat;

    private Set<BMHAudioFormat> supportedFormats;

    /**
     * Constructor
     * 
     * @param outputFormat
     *            the supported destination format
     * @param supportedFormats
     *            the supported source (input) formats
     */
    protected AbstractAudioConverter(final BMHAudioFormat outputFormat,
            final BMHAudioFormat[] supportedFormats) {
        this.outputFormat = outputFormat;
        this.supportedFormats = new HashSet<>(Arrays.asList(supportedFormats));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#verifySupportedAudioFormat
     * (com.raytheon.uf.common.bmh.audio.BMHAudioFormat)
     */
    @Override
    public void verifySupportedAudioFormat(BMHAudioFormat srcFormat)
            throws UnsupportedAudioFormatException {
        if (this.getSupportedSourceFormats().contains(srcFormat) == false) {
            throw new UnsupportedAudioFormatException(srcFormat.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#getConversionFormat()
     */
    @Override
    public BMHAudioFormat getOutputFormat() {
        return this.outputFormat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#getSupportedSourceFormats
     * ()
     */
    @Override
    public Set<BMHAudioFormat> getSupportedSourceFormats() {
        return this.supportedFormats;
    }
}