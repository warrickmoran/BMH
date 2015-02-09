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

import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.impl.Mp3AudioConverter;
import com.raytheon.uf.common.bmh.audio.impl.WavAudioConverter;

/**
 * Edex implementation of the {@link AudioConvererterManager}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 9, 2015  4091       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EdexAudioConverterManager extends AudioConvererterManager {
    private static AudioConvererterManager instance = new EdexAudioConverterManager();

    /**
     * Returns the Edex instance of the {@link AudioConvererterManager}.
     * 
     * @return the Edex instance of the {@link AudioConvererterManager}.
     */
    public static AudioConvererterManager getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.AudioConvererterManager#initialize()
     */
    @Override
    protected void initialize() {
        super.initialize();
        /*
         * register additional converters that will only be used by Edex.
         */
        this.registerConverter(new Mp3AudioConverter());
        this.registerConverter(new WavAudioConverter());

    }
}