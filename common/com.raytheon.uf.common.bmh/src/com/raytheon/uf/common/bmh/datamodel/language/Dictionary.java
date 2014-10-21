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
package com.raytheon.uf.common.bmh.datamodel.language;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * Record for storing a dictionary, which is basically a language and collection
 * of words.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jul 08, 2014 3302       bkowal      Use eager fetching to eliminate session closed
 * Jul 08, 2014 3355       mpduff      Updated mappings between dictionary and words
 * Jul 29, 2014 3407       mpduff      Removed orphanRemoval from words field, added toString()
 * Aug 04, 2014 3175       rjpeter     Added serialization adapter to fix circular reference.
 * Oct 16, 2014 3636       rferrel     Added logging.
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = Dictionary.GET_DICTIONARY_NAMES_QUERY, query = "select dict.name from Dictionary dict") })
@Entity
@Table(name = "dictionary", schema = "bmh")
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = DictionaryAdapter.class)
public class Dictionary {

    public static final String GET_DICTIONARY_NAMES_QUERY = "getDictionaryNames";

    @Id
    @Column(length = 20)
    private String name = null;

    @Enumerated(EnumType.STRING)
    @Column(length = 7, nullable = false)
    private Language language = Language.ENGLISH;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dictionary", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Set<Word> words;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<Word> getWords() {
        if (words == null) {
            words = new HashSet<Word>();
        }
        return words;
    }

    public void setWords(Set<Word> words) {
        this.words = words;
        if ((words != null) && !words.isEmpty()) {
            for (Word word : words) {
                word.setDictionary(this);
            }
        }
    }

    public boolean containsWord(String wordName) {
        if (words != null) {
            for (Word word : words) {
                if (wordName.equalsIgnoreCase(word.getWord())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get a {@link Word} from the dictionary.
     * 
     * @param wordName
     *            The word name
     * @return The Word or null if no word
     */
    public Word getWord(String wordName) {
        if (words != null) {
            for (Word word : words) {
                if (wordName.equalsIgnoreCase(word.getWord())) {
                    return word;
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Dictionary [name=" + name + ", language=" + language + "]";
    }

    /**
     * Get log entry.
     * 
     * @param oldDic
     *            - When null assume this is a new Dictionary.
     * @param user
     *            - Who is making the change
     * @return entry - empty string when no differences.
     */
    public String logEntry(Dictionary oldDic, String user) {
        StringBuilder sb = new StringBuilder();
        sb.append("User ").append(user);
        if (oldDic == null) {
            sb.append(" New ").append(toString());
        } else {
            boolean logChanges = false;
            sb.append(" Updates to Dictionary ").append(getName()).append(" [");
            if (!name.equals(oldDic.getName())) {
                BMHLoggerUtils.logFieldChange(sb, "name", oldDic.getName(),
                        getName());
                logChanges = true;
            }
            if (!getLanguage().equals(oldDic.getLanguage())) {
                BMHLoggerUtils.logFieldChange(sb, "language",
                        oldDic.getLanguage(), getLanguage());
                logChanges = true;
            }

            // No changes made
            if (!logChanges) {
                return "";
            }

            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }
}
