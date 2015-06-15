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
import java.util.EnumMap;

import com.raytheon.uf.common.bmh.datamodel.language.Language;

/**
 * Manages the {@link PhonemeAudioCache} and maps them to a specific
 * {@link Language}. Can be used to retrieve audio from and add audio to the
 * cache associated with a specific {@link Language}.
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

public class PhonemeAudioCacheManager {

    private static final PhonemeAudioCacheManager instance = new PhonemeAudioCacheManager();

    private final Map<Language, PhonemeAudioCache> languageCachedAudioMap = new EnumMap<>(
            Language.class);

    /**
     * Returns the application instance of {@link PhonemeAudioCacheManager}.
     * 
     * @return the application instance of {@link PhonemeAudioCacheManager}.
     */
    public static PhonemeAudioCacheManager getInstance() {
        return instance;
    }

    /**
     * Constructor.
     */
    protected PhonemeAudioCacheManager() {
        for (Language language : Language.values()) {
            this.languageCachedAudioMap.put(language, new PhonemeAudioCache());
        }
    }

    /**
     * Cache the specified audio associated with the specified phoneme for the
     * specified {@link Language}.
     * 
     * @param language
     *            the specified {@link Language}
     * @param phoneme
     *            the specified phoneme
     * @param audio
     *            the specified audio
     */
    public void cache(final Language language, final String phoneme,
            final byte[] audio) {
        this.languageCachedAudioMap.get(language).cache(phoneme, audio);
    }

    /**
     * Retrieved cached phoneme audio (if it exists) for the specified phoneme
     * and the specified {@link Language}.
     * 
     * @param language
     *            the specified {@link Language}
     * @param phoneme
     *            the specified phoneme
     * @return the audio retrieved from the cache
     */
    public byte[] getCachedAudio(final Language language, final String phoneme) {
        return this.languageCachedAudioMap.get(language)
                .getCachedAudio(phoneme);
    }
}