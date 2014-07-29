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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Dictionary word, which is basically a substition.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jul 03, 2014            mpduff      Add dynamic column and unique constraints.
 * Jul 29, 2014  3407      mpduff      Removed HashCode and Equals methods, removed dynamic column
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "word", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "word", "dictionary" }) })
@SequenceGenerator(initialValue = 1, name = Word.GEN, sequenceName = "word_seq")
@DynamicSerialize
public class Word {
    static final String GEN = "Word Generator";

    public static final String DYNAMIC_NUMERIC_CHAR = "#";

    // use surrogate key instead
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 150)
    @DynamicSerializeElement
    private String word;

    @Column(columnDefinition = "TEXT", nullable = false)
    @DynamicSerializeElement
    private String substitute;

    /** An identifier used to link this Word to its Dictionary */
    @ManyToOne
    @JoinColumn(name = "dictionary", nullable = false)
    private Dictionary dictionary;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSubstitute() {
        return substitute;
    }

    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    /**
     * @return the dynamic
     */
    public boolean isDynamic() {
        return word.contains(DYNAMIC_NUMERIC_CHAR);
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Word [id=" + id + ", word=" + word + ", substitute="
                + substitute + ", dictionary=" + dictionary.getName() + "]";
    }
}
