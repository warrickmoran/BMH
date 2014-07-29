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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request object for dictionary queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 02, 2014    3355    mpduff      Initial creation
 * Jul 21, 2014    3407    mpduff      Add delete dictionary action
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class DictionaryRequest implements IServerRequest {

    public enum DictionaryAction {
        Save, ListNames, QueryByName, Delete;
    }

    @DynamicSerializeElement
    private List<Dictionary> dictionaryList;

    @DynamicSerializeElement
    private DictionaryAction action;

    /** Name of the dictionary to query for */
    @DynamicSerializeElement
    private String dictionaryName;

    @DynamicSerializeElement
    private Dictionary dictionary;

    @DynamicSerializeElement
    private List<String> dictionaryNames;

    /**
     * @return the dictionaryList
     */
    public List<Dictionary> getDictionaryList() {
        return dictionaryList;
    }

    /**
     * @param dictionaryList
     *            the dictionaryList to set
     */
    public void setDictionaryList(List<Dictionary> dictionaryList) {
        this.dictionaryList = dictionaryList;
    }

    /**
     * @return the action
     */
    public DictionaryAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(DictionaryAction action) {
        this.action = action;
    }

    /**
     * @return the queryName
     */
    public String getDictionaryName() {
        return dictionaryName;
    }

    /**
     * @param queryName
     *            the queryName to set
     */
    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    /**
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * @param dictionary
     *            the dictionary to set
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @param dictionary
     *            the dictionary to add
     */
    public void addDictionary(Dictionary dictionary) {
        if (dictionaryList == null) {
            dictionaryList = new ArrayList<Dictionary>();
        }

        dictionaryList.add(dictionary);
    }

    /**
     * @return the dictionaryNames
     */
    public List<String> getDictionaryNames() {
        return dictionaryNames;
    }

    /**
     * @param dictionaryNames
     *            the dictionaryNames to set
     */
    public void setDictionaryNames(List<String> dictionaryNames) {
        this.dictionaryNames = dictionaryNames;
    }
}
