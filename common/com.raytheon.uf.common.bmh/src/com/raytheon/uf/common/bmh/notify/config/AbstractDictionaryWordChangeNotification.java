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
package com.raytheon.uf.common.bmh.notify.config;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to keep track of any words that were affected by a dictionary change. This
 * list is used to ensure that only messages that include the affected words will
 * be regenerated due to a dictionary change.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2015  5159       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public abstract class AbstractDictionaryWordChangeNotification extends
        ConfigNotification {

    @DynamicSerializeElement
    private List<String> updatedWords;

    public AbstractDictionaryWordChangeNotification() {
    }

    public AbstractDictionaryWordChangeNotification(ConfigChangeType type,
            ITraceable traceable) {
        super(type, traceable);
    }

    public void setUpdatedWord(final String updatedWord) {
        this.updatedWords = new ArrayList<>(1);
        this.updatedWords.add(updatedWord);
    }

    /**
     * @return the updatedWords
     */
    public List<String> getUpdatedWords() {
        return updatedWords;
    }

    /**
     * @param updatedWords
     *            the updatedWords to set
     */
    public void setUpdatedWords(List<String> updatedWords) {
        this.updatedWords = updatedWords;
    }
}