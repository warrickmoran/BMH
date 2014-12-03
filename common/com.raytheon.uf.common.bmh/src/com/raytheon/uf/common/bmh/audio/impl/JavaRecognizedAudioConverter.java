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
package com.raytheon.uf.common.bmh.audio.impl;

import com.raytheon.uf.common.bmh.audio.AbstractAudioConverter;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;

/**
 * Abstraction of all {@link IAudioConverter}s that are associated with a java
 * supported and recognized audio format.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2014  3880       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class JavaRecognizedAudioConverter extends
        AbstractAudioConverter {

    /**
     * Constructor
     * 
     * @param outputFormat
     *            the supported destination format
     * @param supportedFormats
     *            the supported source (input) formats
     */
    public JavaRecognizedAudioConverter(BMHAudioFormat outputFormat,
            BMHAudioFormat[] supportedFormats) {
        super(outputFormat, supportedFormats);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#verifyCompatibility()
     */
    @Override
    public final void verifyCompatibility() {
        /**
         * Do nothing. All formats that Java recognizes will always be
         * compatible.
         */
    }
}