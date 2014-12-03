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

import java.util.Set;

/**
 * Defines an audio converter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014 3383       bkowal      Initial creation
 * Dec 3, 2014  3880       bkowal      Added verifyCompatibility.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IAudioConverter {
    /**
     * Performs the audio conversion.
     * 
     * @param src
     *            the audio bits from the source (input)
     * @param srcFormat
     *            the current audio format of the source (input)
     * @return the converted audio in the desired format
     * @throws AudioConversionException
     *             when audio conversion fails
     */
    public byte[] convertAudio(final byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException;

    /**
     * Verifies that a converter is able to handle the specified source audio
     * format. Note: the audio converter manager will also perform this
     * verification prior to using a converter.
     * 
     * @param srcFormat
     *            the specified audio format to verify
     * @throws UnsupportedAudioFormatException
     *             when the specified audio format is not supported.
     */
    public void verifySupportedAudioFormat(BMHAudioFormat srcFormat)
            throws UnsupportedAudioFormatException;

    /**
     * Return the supported output format that this converter supports
     * 
     * @return the supported destination audio format
     */
    public BMHAudioFormat getOutputFormat();

    /**
     * Returns a list of recognized source (input) formats. The converter will
     * be capable of converting source audio in the specified format to the
     * desired destination format provided that the specified format is within
     * the list returned by this method.
     * 
     * @return a list of recognized source (input) formats.
     */
    public Set<BMHAudioFormat> getSupportedSourceFormats();

    /**
     * Allows the {@link AudioConvererterManager} to verify that the associated
     * {@link BMHAudioFormat} conversion is supported on this system. This
     * method was used as opposed to just using a method that returns a Boolean
     * because the {@link ConversionNotSupportedException} that will be thrown
     * if the conversion is not supported can provide additional information
     * about why the conversion is not supported.
     * 
     * @throws ConversionNotSupportedException
     *             if audio conversion is not supported for the associated
     *             {@link BMHAudioFormat} on the current machine.
     */
    public void verifyCompatibility() throws ConversionNotSupportedException;
}