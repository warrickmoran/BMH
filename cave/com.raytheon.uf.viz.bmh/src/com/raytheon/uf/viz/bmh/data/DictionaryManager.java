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
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryRequest.DictionaryAction;
import com.raytheon.uf.common.bmh.request.DictionaryResponse;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.bmh.request.WordRequest.WordAction;
import com.raytheon.uf.common.bmh.request.WordResponse;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.core.exception.VizException;

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
 *                                     changed to use response objects.
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * Aug 05, 2014 3175       rjpeter     Added replaceWord.
 * Nov 13, 2014 3803       bkowal      Added default constructor.
 * Dec 15, 2014    3618    bkowal      Added {@link #getNationalDictionaryForLanguage(Language)} and
 *                                     {@link #getNonNationalDictionariesForLanguage(Language)}.
 * Jan 05, 2015    3618    bkowal      Specify the {@link Dictionary} for deletion.
 * Jan 07, 2015    3931    bkowal      Added {@link #verifyNameUniqueness(String)}.
 * Jan 28, 2015    4045    bkowal      Use a {@link NeoSpeechPhonemeMapping} instance.
 * Jun 11, 2015    4552    bkowal      Removed the English-specific phonemeMapping instance.
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
     * Get all the available dictionaries.
     * 
     * @return Map of Dictionary name to Dictionary object
     * @throws VizException
     *             on error
     */
    public List<String> getAllBMHDictionaryNames() throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.ListNames);
        DictionaryResponse response = (DictionaryResponse) BmhUtils
                .sendRequest(req);

        if ((response != null) && (response.getDictionaryNames() != null)) {
            return response.getDictionaryNames();
        }

        return Collections.emptyList();
    }

    /**
     * Retrieves the national {@link Dictionary} for the specified
     * {@link Language}. Will return {@code null} if a national
     * {@link Dictionary} does not exist.
     * 
     * @param language
     *            the specified {@link Language}.
     * @return
     */
    public Dictionary getNationalDictionaryForLanguage(Language language)
            throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.GetNationalForLanguage);
        req.setLanguage(language);
        DictionaryResponse response = (DictionaryResponse) BmhUtils
                .sendRequest(req);

        if (response == null) {
            return null;
        }

        return response.getDictionary();
    }

    /**
     * Retrieves all {@link Dictionary}(ies) that are not national dictionaries
     * for the specified {@link Language}.
     * 
     * @param language
     *            the specified {@link Language}
     * @return a {@link List} of {@link Dictionary}(ies) that are not national
     *         dictionaries.
     * @throws Exception
     */
    public List<Dictionary> getNonNationalDictionariesForLanguage(
            Language language) throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.GetNonNationalForLanguage);
        req.setLanguage(language);
        DictionaryResponse response = (DictionaryResponse) BmhUtils
                .sendRequest(req);

        if (response == null || response.getDictionaries() == null
                || response.getDictionaries().isEmpty()) {
            return Collections.emptyList();
        }

        return response.getDictionaries();
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
    public Word saveWord(Word word) throws Exception {

        WordRequest req = new WordRequest();
        req.setAction(WordAction.Save);
        req.setWord(word);
        WordResponse response = (WordResponse) BmhUtils.sendRequest(req);
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
    public void deleteWord(Word word) throws Exception {
        WordRequest req = new WordRequest();
        req.setAction(WordAction.Delete);
        req.setWord(word);

        BmhUtils.sendRequest(req);
    }

    /**
     * Replace the {@link Word} from the {@link Dictionary}
     * 
     * @param word
     *            The Word to delete
     * @throws VizException
     */
    public Word replaceWord(Word word) throws Exception {
        WordRequest req = new WordRequest();
        req.setAction(WordAction.Replace);
        req.setWord(word);

        WordResponse response = (WordResponse) BmhUtils.sendRequest(req);
        if (response.getWordList().isEmpty()) {
            throw new VizException("An error occurred saving word "
                    + word.getWord());
        }
        word = response.getWordList().get(0);

        return word;
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
            throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Save);
        req.setDictionary(dictionary);

        return (DictionaryResponse) BmhUtils.sendRequest(req);
    }

    /**
     * Create a {@link Dictionary} entry in the BMH DB.
     * 
     * @param dictionary
     *            Dictionary to create
     * @return
     * @throws VizException
     */
    public void createDictionary(Dictionary dictionary) throws Exception {
        if (this.verifyNameUniqueness(dictionary.getName()) == false) {
            String msg = "A dictionary with that name already exists.\n"
                    + "Loading existing dictionary.";
            DialogUtility.showMessageBox(Display.getCurrent().getActiveShell(),
                    SWT.ERROR | SWT.OK, "Dictionary Name Exists", msg);
        } else {
            DictionaryRequest req = new DictionaryRequest();
            req.setAction(DictionaryAction.Save);
            req.setDictionary(dictionary);

            BmhUtils.sendRequest(req);
        }
    }

    /**
     * Determines if the specified {@link Dictionary} name is unique.
     * 
     * @param name
     *            the specified {@link Dictionary} name
     * @return {@code true} if the name is unique; {@code false}, otherwise
     */
    public boolean verifyNameUniqueness(final String name) throws Exception {
        return getAllBMHDictionaryNames().contains(name) == false;
    }

    /**
     * Get a fully populated {@link Dictionary} object
     * 
     * @param dictionaryName
     *            The name of the Dictionary to get
     * @return Dictionary or null if no dictionary exists
     * @throws VizException
     */
    public Dictionary getDictionary(String dictionaryName) throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.QueryByName);
        req.setDictionaryName(dictionaryName);
        DictionaryResponse response = (DictionaryResponse) BmhUtils
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
    public void deleteDictionary(Dictionary dictionary) throws Exception {
        DictionaryRequest req = new DictionaryRequest();
        req.setAction(DictionaryAction.Delete);
        req.setDictionary(dictionary);
        BmhUtils.sendRequest(req);
    }
}
