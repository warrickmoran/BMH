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
package com.raytheon.uf.viz.bmh.ui.dialogs.phonemes;

import java.util.Map;
import java.util.HashMap;

import com.raytheon.uf.common.bmh.datamodel.language.Language;

/**
 * {@link Language}-specific mechanism to locally cache phoneme audio generated
 * by the BMH EDEX server on-demand.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2015 4552       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PhonemeAudioCache {

    private final Map<String, byte[]> phonemeAudioMap = new HashMap<>();

    /**
     * Constructor.
     */
    protected PhonemeAudioCache() {
    }

    public void cache(final String phoneme, final byte[] audio) {
        this.phonemeAudioMap.put(phoneme, audio);
    }

    public byte[] getCachedAudio(final String phoneme) {
        return this.phonemeAudioMap.get(phoneme);
    }
}