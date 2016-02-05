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
package com.raytheon.uf.common.bmh.legacy;

import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Legacy Dictionary Import response object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 10, 2015 5112       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ImportLegacyDictionaryResponse {

    @DynamicSerializeElement
    private Set<String> correctWords;

    @DynamicSerializeElement
    private Dictionary nationalDictionary;

    public ImportLegacyDictionaryResponse() {
    }

    /**
     * @return the correctWords
     */
    public Set<String> getCorrectWords() {
        return correctWords;
    }

    /**
     * @param correctWords
     *            the correctWords to set
     */
    public void setCorrectWords(Set<String> correctWords) {
        this.correctWords = correctWords;
    }

    /**
     * @return the nationalDictionary
     */
    public Dictionary getNationalDictionary() {
        return nationalDictionary;
    }

    /**
     * @param nationalDictionary
     *            the nationalDictionary to set
     */
    public void setNationalDictionary(Dictionary nationalDictionary) {
        this.nationalDictionary = nationalDictionary;
    }
}