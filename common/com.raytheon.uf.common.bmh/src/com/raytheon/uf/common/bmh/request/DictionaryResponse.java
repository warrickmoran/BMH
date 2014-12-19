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

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for {@link Dictionary} queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2014   3407     mpduff      Initial creation
 * Dec 16, 2014   3618     bkowal      Added {@link #dictionaries}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class DictionaryResponse {

    /**
     * Dictionary names
     */
    @DynamicSerializeElement
    private List<String> dictionaryNames;

    /**
     * The requested dictionary
     */
    @DynamicSerializeElement
    private Dictionary dictionary;

    /**
     * Requested dictionaries when more than one dictionary is requested.
     */
    @DynamicSerializeElement
    private List<Dictionary> dictionaries;

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
     * @return the dictionaries
     */
    public List<Dictionary> getDictionaries() {
        return dictionaries;
    }

    /**
     * @param dictionaries
     *            the dictionaries to set
     */
    public void setDictionaries(List<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }
}
