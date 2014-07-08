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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NeoSpeechPhonemeMapping {

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

    /**
     * Constructor
     */
    public NeoSpeechPhonemeMapping() {
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
    }

    public Map<Character, String> getVowelMap() {
        return bmhVowelMap;
    }

    public Map<Character, String> getConsonantMap() {
        return bmhConsonantMap;
    }
}