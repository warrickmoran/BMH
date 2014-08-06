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
 * Jul 03, 2014    3355    mpduff      Initial creation
 * Jul 27, 2014    3407    mpduff      Added delete action
 * Aug 05, 2014    3175    rjpeter     Added replace, removed dictionaryName.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class WordRequest implements IServerRequest {

    public enum WordAction {
        Save, Query, Delete, Replace;
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
}
