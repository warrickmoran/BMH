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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * NeoSpeech Phoneme data object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2014   3355     mpduff      Initial creation
 * Jan 28, 2015   4045     bkowal      Made {@link NeoSpeechPhonemeMapping} an instance
 *                                     and added phoneme validation methods.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NeoSpeechPhonemeMapping {

    private static final NeoSpeechPhonemeMapping instance = new NeoSpeechPhonemeMapping();

    /**
     * Consonant map
     */
    private Map<Character, String> bmhConsonantMap = null;

    /**
     * Vowel map
     */
    private Map<Character, String> bmhVowelMap = null;

    /**
     * List of vowels
     */
    private List<String> vowelList;

    /**
     * List of consonants
     */
    private List<String> consonantList;

    private Pattern cmuPhonemePattern;

    public static NeoSpeechPhonemeMapping getInstance() {
        return instance;
    }

    /**
     * Constructor
     */
    protected NeoSpeechPhonemeMapping() {
        bmhConsonantMap = new HashMap<Character, String>();
        bmhVowelMap = new HashMap<Character, String>();
        populatePhonemes();
    }

    public List<String> getConsonantPhonemes() {
        return consonantList;
    }

    public List<String> getVowelPhonemes() {
        return vowelList;
    }

    private void populatePhonemes() {
        bmhConsonantMap.put('b', "B");
        bmhConsonantMap.put('C', "CH");
        bmhConsonantMap.put('d', "D");
        bmhConsonantMap.put('D', "DH");
        bmhConsonantMap.put('f', "F");
        bmhConsonantMap.put('F', "D");
        bmhConsonantMap.put('g', "G");
        bmhConsonantMap.put('h', "HH");
        bmhConsonantMap.put('J', "JH");
        bmhConsonantMap.put('k', "K");
        bmhConsonantMap.put('l', "L");
        bmhConsonantMap.put('m', "M");
        bmhConsonantMap.put('n', "N");
        bmhConsonantMap.put('G', "NG");
        bmhConsonantMap.put('p', "P");
        bmhConsonantMap.put('r', "R");
        bmhConsonantMap.put('s', "S");
        bmhConsonantMap.put('t', "T");
        bmhConsonantMap.put('T', "TH");
        bmhConsonantMap.put('S', "SH");
        bmhConsonantMap.put('v', "V");
        bmhConsonantMap.put('w', "W");
        bmhConsonantMap.put('y', "Y");
        bmhConsonantMap.put('z', "Z");
        bmhConsonantMap.put('Z', "ZH");
        bmhVowelMap.put('a', "AA");
        bmhVowelMap.put('A', "AE");
        bmhVowelMap.put('x', "AH");
        bmhVowelMap.put('H', "AH");
        bmhVowelMap.put('c', "AO");
        bmhVowelMap.put('O', "OY");
        bmhVowelMap.put('e', "EY");
        bmhVowelMap.put('E', "EH");
        bmhVowelMap.put('X', "EH");
        bmhVowelMap.put('i', "IY");
        bmhVowelMap.put('I', "IH");
        bmhVowelMap.put('o', "OW");
        bmhVowelMap.put('R', "ER");
        bmhVowelMap.put('U', "UH");
        bmhVowelMap.put('u', "UW");
        bmhVowelMap.put('W', "AW");
        bmhVowelMap.put('Y', "AY");

        Set<String> set = new HashSet<String>();
        for (String vowel : bmhVowelMap.values()) {
            set.add(vowel);
        }

        this.vowelList = new ArrayList<String>(set);

        set.clear();
        for (String consonant : bmhConsonantMap.values()) {
            set.add(consonant);
        }

        this.consonantList = new ArrayList<String>(set);

        /*
         * Build a regex for the cmu phonemes.
         */
        final String spaceRegex = "[\\s]+";
        final String stressNumericRegex = "[0-2]";
        final String separatorRegex = "|";
        final String lParensRegex = "(";
        final String rParensRegex = ")";

        StringBuilder sb = new StringBuilder("^");

        /*
         * build the vowel regex.
         */
        StringBuilder vowelRegex = new StringBuilder(lParensRegex);
        boolean first = true;
        for (String vowel : this.vowelList) {
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
        for (String consonant : this.consonantList) {
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

    public Map<Character, String> getVowelMap() {
        return bmhVowelMap;
    }

    public Map<Character, String> getConsonantMap() {
        return bmhConsonantMap;
    }
}