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
package com.raytheon.uf.edex.bmh.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;

/**
 * BMH DAO for {@link TtsVoice}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#    Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2014  3302     bkowal      Initial creation
 * Aug 11, 2014  3490     lvenable    Updated to get Voice information.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Dec 16, 2014  3618     bkowal      Added {@link #getVoiceIdentifiers()}.
 * Jan 15, 2015  3809     bkowal      Added {@link #getVoiceIdentifiersForLanguage(Language)}.
 * Dec 03, 2015  5158     bkowal      Added {@link #getDefaultVoiceForLanguage(Language)}. 
 * Dec 08, 2015  5159     bkowal      Added {@link #isVoiceDictionary(Dictionary)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TtsVoiceDao extends AbstractBMHDao<TtsVoice, Integer> {

    public TtsVoiceDao() {
        super(TtsVoice.class);
    }

    public TtsVoiceDao(boolean operational) {
        super(operational, TtsVoice.class);
    }

    /**
     * Get all of the available voices.
     * 
     * @return List of voices.
     */
    public List<TtsVoice> getVoices() {
        List<TtsVoice> voiceList = this.loadAll();
        if (voiceList == null) {
            return Collections.emptyList();
        }

        return voiceList;
    }

    /**
     * Retrieve the minimum set of information required to identify a
     * {@link TtsVoice}. This information can be used to retrieve full
     * {@link TtsVoice} records as needed.
     * 
     * @return a {@link List} of {@link TtsVoice} ids (voiceNumbers) and names.
     */
    public List<TtsVoice> getVoiceIdentifiers() {
        List<?> returnedObjects = this
                .findByNamedQuery(TtsVoice.GET_VOICE_IDENTIFIERS);
        if (returnedObjects == null || returnedObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return this.buildVoiceIdentifiers(returnedObjects,
                TtsVoice.GET_VOICE_IDENTIFIERS);
    }

    /**
     * Retrieve the minimum set of information required to identify a
     * {@link TtsVoice} associated with the specified {@link Language}. This
     * information can be used to retrieve full {@link TtsVoice} records as
     * needed.
     * 
     * @param language
     *            the specified {@link Language}
     * @return a {@link List} of {@link TtsVoice} ids (voiceNumbers) and names.
     */
    public List<TtsVoice> getVoiceIdentifiersForLanguage(final Language language) {
        List<?> returnedObjects = this.findByNamedQueryAndNamedParam(
                TtsVoice.GET_VOICE_IDENTIFIERS_FOR_LANGUAGE, "language",
                language);

        return this.buildVoiceIdentifiers(returnedObjects,
                TtsVoice.GET_VOICE_IDENTIFIERS_FOR_LANGUAGE);
    }

    /**
     * Constructs a {@link List} of {@link TtsVoice}s consisting of the id and
     * name based on the specified {@link Object}s retrieved from the database.
     * 
     * @param sourceObjects
     *            the specified {@link Object}s retrieved from the database
     * @param query
     *            the name of the query that was used to retrieve the
     *            information; used for logging purposes
     * @return a {@link List} of {@link TtsVoice}s consisting of the id and name
     */
    private List<TtsVoice> buildVoiceIdentifiers(List<?> sourceObjects,
            final String query) {
        if (sourceObjects == null || sourceObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<TtsVoice> voiceIdentifiers = new ArrayList<TtsVoice>(
                sourceObjects.size());
        for (Object object : sourceObjects) {
            if (object instanceof Object[] == false) {
                logger.error("The "
                        + query
                        + " query returned results in the wrong format. Expected an array of Object.");
                return Collections.emptyList();
            }

            Object[] objects = (Object[]) object;
            if (objects.length != 2) {
                logger.error("The "
                        + query
                        + " query returned results in the wrong format. Expected an array of Object with 2 elements.");
                return Collections.emptyList();
            }

            if (objects[0] instanceof Integer == false
                    || objects[1] instanceof String == false) {
                logger.error("The "
                        + query
                        + " query returned results in the wrong format. Expected the object array to have objects of type: [Integer, String].");
                return Collections.emptyList();
            }

            TtsVoice voice = new TtsVoice();
            voice.setVoiceNumber((Integer) objects[0]);
            voice.setVoiceName((String) objects[1]);
            voiceIdentifiers.add(voice);
        }

        return voiceIdentifiers;
    }

    public boolean isVoiceDictionary(final Dictionary dictionary) {
        List<?> returnedObjects = this.findByNamedQueryAndNamedParam(
                TtsVoice.GET_VOICE_FOR_DICTIONARY, "dictionary", dictionary);

        return (returnedObjects != null && returnedObjects.isEmpty() == false);
    }

    /**
     * Returns the {@link TtsVoice} associated with the specified
     * {@link Language} as the "default" voice.
     * 
     * @param language
     *            the specified {@link Language}.
     * @return the associated {@link TtsVoice}
     */
    public TtsVoice getDefaultVoiceForLanguage(final Language language) {
        List<?> returnedObjects = this.findByNamedQueryAndNamedParam(
                TtsVoice.GET_DEFAULT_VOICE_FOR_LANGUAGE, "language", language);
        if (returnedObjects == null || returnedObjects.isEmpty()) {
            return null;
        }

        if (returnedObjects.size() > 1) {
            throw new IllegalStateException(
                    "Found more than once voice for language: "
                            + language.name()
                            + "; unable to determine the default voice!");
        }

        return (TtsVoice) returnedObjects.get(0);
    }
}