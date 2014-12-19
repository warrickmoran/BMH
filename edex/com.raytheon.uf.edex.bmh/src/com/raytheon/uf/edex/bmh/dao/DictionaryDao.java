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

/**
 * BMH DAO for {@link Dictionary}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3302       bkowal      Initial creation
 * Jul 08, 2014 3355       mpduff      Added getDictionaryNames()
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Dec 11, 2014 3618       bkowal      Added {@link #getNationalDictionaries()}.
 * Dec 15, 2014 3618       bkowal      Added {@link #getNationalDictionaryForLanguage(Language)}.
 * Dec 16, 2014 3618       bkowal      Added {@link #getNonNationalDictionariesForLanguage(Language)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DictionaryDao extends AbstractBMHDao<Dictionary, String> {

    public DictionaryDao() {
        super(Dictionary.class);
    }

    public DictionaryDao(boolean operational) {
        super(operational, Dictionary.class);
    }

    /**
     * Get a list of all dictionaries in the BMH database.
     * 
     * @return dictList list of dictionaries
     */
    public List<String> getDictionaryNames() {
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) findByNamedQuery(Dictionary.GET_DICTIONARY_NAMES_QUERY);
        if (names == null) {
            names = Collections.emptyList();
        }

        return names;
    }

    /**
     * Retrieves every national {@link Dictionary} across every {@link Language}
     * .
     * 
     * @return a {@link List} of national {@link Dictionary}(ies)
     */
    public List<Dictionary> getNationalDictionaries() {
        List<?> returnedObjects = this
                .findByNamedQuery(Dictionary.GET_NATIONAL_DICTIONARIES);
        if (returnedObjects == null || returnedObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<Dictionary> nationalDictionaries = new ArrayList<>(
                returnedObjects.size());
        for (Object object : returnedObjects) {
            if (object instanceof Dictionary == false) {
                logger.error("The "
                        + Dictionary.GET_NATIONAL_DICTIONARIES
                        + " query returned results in the wrong format. Expected a "
                        + Dictionary.class.getName() + "; received a "
                        + object.getClass().getName() + ".");
                return Collections.emptyList();
            }
            nationalDictionaries.add((Dictionary) object);
        }

        return nationalDictionaries;
    }

    /**
     * Retrieves the national {@link Dictionary} associated with the specified
     * {@link Language}.
     * 
     * @param language
     *            the specified {@link Language}.
     * @return A national {@link Dictionary} associated with the specified
     *         {@link Language}, if it exists; otherwise, {@code null}.
     */
    public Dictionary getNationalDictionaryForLanguage(final Language language) {
        List<?> returnedObjects = this.findByNamedQueryAndNamedParam(
                Dictionary.GET_NATIONAL_DICTIONARY_FOR_LANGUAGE, "language",
                language);
        if (returnedObjects == null || returnedObjects.isEmpty()) {
            return null;
        }

        if (returnedObjects.get(0) instanceof Dictionary == false) {
            logger.error("The "
                    + Dictionary.GET_NATIONAL_DICTIONARIES
                    + " query returned results in the wrong format. Expected a "
                    + Dictionary.class.getName() + "; received a "
                    + returnedObjects.get(0).getClass().getName() + ".");
            return null;
        }

        return (Dictionary) returnedObjects.get(0);
    }

    /**
     * Retrieves a {@link List} of {@link Dictionary}(ies) that are not national
     * dictionaries associated with the specified {@link Language}.
     * 
     * @param language
     *            the specified {@link Language}
     * @return the {@link List} of {@link Dictionary}(ies) that are not national
     *         dictionaries.
     */
    public List<Dictionary> getNonNationalDictionariesForLanguage(
            final Language language) {
        List<?> returnedObjects = this.findByNamedQueryAndNamedParam(
                Dictionary.GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE,
                "language", language);
        if (returnedObjects == null || returnedObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<Dictionary> dictionaries = new ArrayList<>(returnedObjects.size());
        for (Object object : returnedObjects) {
            if (object instanceof Dictionary == false) {
                logger.error("The "
                        + Dictionary.GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE
                        + " query returned results in the wrong format. Expected a "
                        + Dictionary.class.getName() + "; received a "
                        + object.getClass().getName() + ".");
                return Collections.emptyList();
            }
            dictionaries.add((Dictionary) object);
        }

        return dictionaries;
    }
}