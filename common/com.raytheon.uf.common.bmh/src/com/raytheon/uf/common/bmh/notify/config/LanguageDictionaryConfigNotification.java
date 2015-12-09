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

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification that is used when a {@link Language}-specific {@link Dictionary}
 * is altered. A {@link Language}-specific {@link Dictionary} is defined as a
 * {@link Dictionary} that is either national or that has been assigned to a
 * specific {@link TtsVoice}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 11, 2014 3618       bkowal      Initial creation
 * Dec 15, 2014 3618       bkowal      Added {@link #language}.
 * Dec 16, 2014 3618       bkowal      Added {@link #NationalDictionaryConfigNotification()}.
 * May 28, 2015 4429       rjpeter     Update for ITraceable
 * Dec 03, 2015 5159       bkowal      Extend {@link AbstractDictionaryWordChangeNotification}.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LanguageDictionaryConfigNotification extends
        AbstractDictionaryWordChangeNotification {

    @DynamicSerializeElement
    private Language language;

    @DynamicSerializeElement
    private boolean national;

    /*
     * Default constructor for dynamicserialize.
     */
    public LanguageDictionaryConfigNotification() {
    }

    /**
     * Constructor
     */
    public LanguageDictionaryConfigNotification(ConfigChangeType type,
            ITraceable traceable, boolean national) {
        super(type, traceable);
        this.national = national;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the national
     */
    public boolean isNational() {
        return national;
    }

    /**
     * @param national the national to set
     */
    public void setNational(boolean national) {
        this.national = national;
    }
}