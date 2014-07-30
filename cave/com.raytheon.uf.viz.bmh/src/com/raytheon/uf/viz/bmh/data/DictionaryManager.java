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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryRequest.DictionaryAction;
import com.raytheon.uf.common.bmh.request.DictionaryResponse;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.bmh.request.WordRequest.WordAction;
import com.raytheon.uf.common.bmh.request.WordResponse;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
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
 * Jul 21, 2014    3407    mpduff      Added deleteDictionary() and 
 *                                     changed to use response objects
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
        DictionaryResponse response = (DictionaryResponse) ThriftClient
                .sendRequest(req);

        if (response != null && response.getDictionaryNames() != null) {
            return response.getDictionaryNames();
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
     * @return The saved Word object
     * @throws VizException
     */
    public Word saveWord(Word word) throws VizException {

        WordRequest req = new WordRequest();
        req.setAction(WordAction.Save);
        req.setWord(word);
        req.setDictionaryName(word.getDictionary().getName());
        WordResponse response = (WordResponse) ThriftClient.sendRequest(req);
        if (response.getWordList().isEmpty()) {
            throw new VizException("An error occurred saving word "
                    + word.getWord());
        }
        word = response.getWordList().get(0);

        return word;
    }

    /**
     * Delete the {@link Word} from the {@link Dictionary}
     * 
     * @param word
     *            The Word to delete
     * @throws VizException
     */
    public void deleteWord(Word word) throws VizException {
        WordRequest request = new WordRequest();
        request.setAction(WordAction.Delete);
        request.setDictionaryName(word.getDictionary().getName());
        request.setWord(word);

        ThriftClient.sendRequest(request);
    }

    /**
     * Save the {@link Dictionary} object.
     * 
     * @param dictionary
     *            The Dictionary to save
     * @return The dictionary response
     * @throws VizException
     */
    public DictionaryResponse saveDictionary(Dictionary dictionary)
            throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Save);
        req.setDictionary(dictionary);

        return (DictionaryResponse) ThriftClient.sendRequest(req);
    }

    /**
     * Create a {@link Dictionary} entry in the BMH DB.
     * 
     * @param dictionary
     *            Dictionary to create
     * @return
     * @throws VizException
     */
    public void createDictionary(Dictionary dictionary) throws VizException {
        List<String> names = getAllBMHDictionaryNames();
        if (names.contains(dictionary.getName())) {
            String msg = "A dictionary with that name already exists.\n"
                    + "Loading existing dictionary.";
            DialogUtility.showMessageBox(Display.getCurrent().getActiveShell(),
                    SWT.ERROR | SWT.OK, "Dictionary Name Exists", msg);
        } else {
            DictionaryRequest req = new DictionaryRequest();
            req.setAction(DictionaryAction.Save);
            req.setDictionary(dictionary);

            ThriftClient.sendRequest(req);
        }
    }

    /**
     * Get a fully populated {@link Dictionary} object
     * 
     * @param dictionaryName
     *            The name of the Dictionary to get
     * @return Dictionary or null if no dictionary exists
     * @throws VizException
     */
    public Dictionary getDictionary(String dictionaryName) throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.QueryByName);
        req.setDictionaryName(dictionaryName);
        DictionaryResponse response = (DictionaryResponse) ThriftClient
                .sendRequest(req);
        return response.getDictionary();
    }

    /**
     * Delete a {@link Dictionary} from the BMH db.
     * 
     * @param dictionaryName
     *            Name of the Dictionary to delete
     * @throws VizException
     */
    public void deleteDictionary(String dictionaryName) throws VizException {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Delete);
        req.setDictionaryName(dictionaryName);
        ThriftClient.sendRequest(req);
    }
}
