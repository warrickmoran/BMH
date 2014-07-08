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
package com.raytheon.uf.common.bmh.request;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request object for word queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 3, 2014    3355     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class WordRequest implements IServerRequest {

    public enum WordAction {
        Save, Query;
    }

    /**
     * Word to Save.
     */
    @DynamicSerializeElement
    private Word word;

    /**
     * Action to perform
     */
    @DynamicSerializeElement
    private WordAction action;

    /**
     * List of words returned from a Query.
     */
    @DynamicSerializeElement
    private List<Word> wordList;

    /**
     * Status of action, true for success, false for fail
     */
    @DynamicSerializeElement
    private boolean status = false;

    /**
     * @return the word
     */
    public Word getWord() {
        return word;
    }

    /**
     * @param word
     *            the word to set
     */
    public void setWord(Word word) {
        this.word = word;
    }

    /**
     * @return the action
     */
    public WordAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(WordAction action) {
        this.action = action;
    }

    /**
     * @return the wordList
     */
    public List<Word> getWordList() {
        return wordList;
    }

    /**
     * @param wordList
     *            the wordList to set
     */
    public void setWordList(List<Word> wordList) {
        this.wordList = wordList;
    }

    /**
     * @return the status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(boolean status) {
        this.status = status;
    }
}
