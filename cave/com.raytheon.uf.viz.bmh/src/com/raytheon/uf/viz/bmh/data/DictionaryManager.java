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
package com.raytheon.uf.viz.bmh.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryRequest.DictionaryAction;
import com.raytheon.uf.viz.bmh.voice.NeoSpeechPhonemeMapping;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

/**
 * Dictionary manager class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 25, 2014    3355    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
public class DictionaryManager {
    /**
     * Gender options for a dictionary
     */
    public enum Gender {
        MALE, FEMALE
    }

    /**
     * Map of Dictionary name -> Dictionary object
     */
    private final Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();

    /**
     * The voice object
     */
    private final NeoSpeechPhonemeMapping phonemeMapping;

    /**
     * Constructor.
     * 
     * @param neoVoice
     *            The voice object
     */
    public DictionaryManager(NeoSpeechPhonemeMapping phonemeMapping) {
        this.phonemeMapping = phonemeMapping;
    }

    /**
     * Get all the available dictionaries.
     * 
     * @return Map of Dictionary name to Dictionary object
     * @throws VizException
     *             on error
     */
    public List<String> getAllBMHDictionaryNames() throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.ListNames);
        DictionaryRequest results = (DictionaryRequest) ThriftClient
                .sendRequest(req);
        if (results != null && results.getDictionaryNames() != null) {
            return results.getDictionaryNames();
        }

        return Collections.emptyList();
    }

    /**
     * Get the voice object
     * 
     * @return The voice object
     */
    public NeoSpeechPhonemeMapping getPhonemeMapping() {
        return phonemeMapping;
    }

    /**
     * Save the {@link Word} to the {@link Dictionary}
     * 
     * @param word
     *            The Word to save
     * @param dictionaryName
     *            The name of the destination dictionary
     * @return true if successfully saved, false otherwise
     * @throws VizException
     */
    public boolean saveWord(Word word, String dictionaryName)
            throws VizException {

        Dictionary dict = dictionaries.get(dictionaryName);
        if (dict == null) {
            throw new IllegalArgumentException("Invalid dictionary name.");
        }
        Set<Word> words = dict.getWords();
        words.add(word);
        dict.setWords(words);

        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Save);
        req.setDictionary(dict);

        DictionaryRequest response;
        try {
            response = (DictionaryRequest) ThriftClient.sendRequest(req);
            dictionaries.put(response.getDictionary().getName(),
                    response.getDictionary());
            return response.isStatus();
        } catch (VizException e) {
            // Remove word on failure to store
            dict.getWords().remove(word);
            throw e;
        }
    }

    /**
     * Create a {@link Dictionary} entry in the BMH DB.
     * 
     * @param dictionary
     *            dictionary to create
     * @return
     * @throws VizException
     */
    public Dictionary createDictionary(Dictionary dictionary)
            throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Save);
        req.setDictionary(dictionary);

        DictionaryRequest response = (DictionaryRequest) ThriftClient
                .sendRequest(req);
        dictionary = response.getDictionary();
        dictionaries.put(dictionary.getName(), dictionary);

        return dictionary;
    }

    /**
     * Get a fully populated {@link Dictionary} object
     * 
     * @param dictionaryName
     * @return Dictionary or null if no dictionary exists
     * @throws VizException
     */
    public Dictionary getDictionary(String dictionaryName) throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.QueryByName);
        req.setQueryName(dictionaryName);
        DictionaryRequest response = (DictionaryRequest) ThriftClient
                .sendRequest(req);
        Dictionary d = response.getDictionary();
        if (d != null) {
            dictionaries.put(d.getName(), d);
        }
        return d;
    }
}
