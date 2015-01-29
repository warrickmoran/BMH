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

/**
 * A custom audio format enum specific to BMH has been created because the Java
 * Audio API does not support a diverse enough set of audio formats by default.
 * This enum specifies all of the audio formats that are supported by the BMH
 * Audio Conversion capability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014 3383       bkowal      Initial creation
 * Oct 2, 2014  3642       bkowal      Fix file extension specifiers
 * Dec 3, 2014  3880       bkowal      Added MP3.
 * Jan 29, 2015 4057       bkowal      Added {@link #isValidExtension(String)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum BMHAudioFormat {
    /* The WAV Audio Format. */
    WAV(".wav"),
    /* The ULAW Audio Format. */
    ULAW(".ulaw"),
    /* The PCM Audio Format. */
    PCM(".pcm"),
    /* The proprietary MP3 Audio Format. */
    MP3(".mp3");

    private final String extension;

    /**
     * Constructor
     * 
     * @param extension
     *            the extension associated with the audio format
     */
    private BMHAudioFormat(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

    /**
     * Returns the {@link BMHAudioFormat} associated with the specified file
     * extension.
     * 
     * @param extension
     *            the specified file extension
     * @return the associated {@link BMHAudioFormat}
     * @throws IllegalArgumentException
     *             when an unrecognized file extension is specified
     */
    public static BMHAudioFormat lookupByExtension(final String extension) {
        for (BMHAudioFormat format : BMHAudioFormat.values()) {
            if (extension.equals(format.extension)) {
                return format;
            }
        }

        throw new IllegalArgumentException("The specified file extension: "
                + extension
                + " is not associated with a recognizable bmh audio format!");
    }

    /**
     * Determines if the specified extension is associated with a recognized
     * {@link BMHAudioFormat}.
     * 
     * @param extension
     *            the specified extension
     * @return true, if the extension is recognized; false, othwerwise
     */
    public static boolean isValidExtension(final String extension) {
        for (BMHAudioFormat format : BMHAudioFormat.values()) {
            if (extension.equals(format.extension)) {
                return true;
            }
        }

        return false;
    }
}