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
package com.raytheon.uf.viz.bmh.voice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * A generic neospeech phoneme mapping.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2015 4552       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractNeoSpeechPhonemeMapping {

    protected final int numericStressLimit;

    /**
     * Consonant map
     */
    protected final Map<Character, String> bmhConsonantMap = new HashMap<>();

    /**
     * Vowel map
     */
    protected final Map<Character, String> bmhVowelMap = new HashMap<>();

    /**
     * Phoneme Consonants
     */
    private final List<String> phonemeConsonants;

    /**
     * Phoneme Vowels
     */
    private final List<String> phonemeVowels;

    private Pattern cmuPhonemePattern;

    public AbstractNeoSpeechPhonemeMapping(int numericStressLimit) {
        this.numericStressLimit = numericStressLimit;
        this.constructConsonantPhonemeMapping();
        this.constructVowelPhonemeMapping();

        this.phonemeConsonants = new ArrayList<>(new HashSet<>(
                this.bmhConsonantMap.values()));
        Collections.sort(this.phonemeConsonants);
        this.phonemeVowels = new ArrayList<>(new HashSet<>(
                this.bmhVowelMap.values()));
        Collections.sort(this.phonemeVowels);

        this.constructPhonemePattern();
    }

    protected abstract void constructConsonantPhonemeMapping();

    protected abstract void constructVowelPhonemeMapping();

    private void constructPhonemePattern() {
        /*
         * Build a regex for the cmu phonemes.
         */
        final String spaceRegex = "[\\s]+";
        final String stressNumericRegex = "[0-" + this.numericStressLimit + "]";
        final String separatorRegex = "|";
        final String lParensRegex = "(";
        final String rParensRegex = ")";

        StringBuilder sb = new StringBuilder("^");

        /*
         * build the vowel regex.
         */
        StringBuilder vowelRegex = new StringBuilder(lParensRegex);
        boolean first = true;
        for (String vowel : this.phonemeVowels) {
            if (first) {
                first = false;
            } else {
                vowelRegex.append(separatorRegex);
            }
            vowelRegex.append(vowel);
        }
        vowelRegex.append(rParensRegex);

        /*
         * build the consonant regex.
         */
        StringBuilder consonantRegex = new StringBuilder(lParensRegex);
        first = true;
        for (String consonant : this.phonemeConsonants) {
            if (first) {
                first = false;
            } else {
                consonantRegex.append(separatorRegex);
            }
            consonantRegex.append(consonant);
        }
        consonantRegex.append(rParensRegex);

        /*
         * build the final regex {@link String}.
         */
        sb.append(lParensRegex).append(lParensRegex);
        /*
         * there can be zero or many phonemes at the beginning of the regex that
         * must be separated by a space (each phoneme would end with a space).
         */
        sb.append(vowelRegex.toString()).append(stressNumericRegex);
        sb.append(separatorRegex).append(consonantRegex.toString())
                .append(rParensRegex);
        sb.append(spaceRegex).append(rParensRegex).append("*");
        /*
         * the phoneme at the end of the regex will not be delimited by a space.
         */
        sb.append(lParensRegex).append(lParensRegex);
        sb.append(vowelRegex.toString()).append(stressNumericRegex);
        sb.append(rParensRegex).append(separatorRegex);
        sb.append(consonantRegex.toString());
        sb.append(rParensRegex).append("$");

        this.cmuPhonemePattern = Pattern.compile(sb.toString());
    }

    /**
     * Verifies that the specified phoneme is a valid phoneme.
     * 
     * @param text
     *            the specific phoneme
     * @return true, if the specified phoneme is valid; false, otherwise.
     */
    public boolean isValidPhoneme(String text) {
        /*
         * Do we want to determine which portion of the phoneme text is invalid
         * and include it in the invalid phoneme message?
         */
        return this.cmuPhonemePattern.matcher(text.trim()).matches();
    }

    public int getNumericStressLimit() {
        return numericStressLimit;
    }

    public Map<Character, String> getBmhConsonantMap() {
        return bmhConsonantMap;
    }

    public Map<Character, String> getBmhVowelMap() {
        return bmhVowelMap;
    }

    public List<String> getPhonemeConsonants() {
        return phonemeConsonants;
    }

    public List<String> getPhonemeVowels() {
        return phonemeVowels;
    }
}