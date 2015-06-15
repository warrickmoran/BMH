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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert;

import java.util.Arrays;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.viz.bmh.voice.AbstractNeoSpeechPhonemeMapping;
import com.raytheon.uf.viz.bmh.voice.NeoSpeechPhonemeMappingFactory;

/**
 * Class to convert legacy dictionary values into NeoSpeech values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2014    3355    mpduff      Initial creation
 * Jan 28, 2015    4045    bkowal      Use the {@link NeoSpeechPhonemeMapping} instance.
 * Jun 11, 2015    4552    bkowal      Use {@link NeoSpeechPhonemeMappingFactory}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class LegacyDictionaryConverter {
    /**
     * Type of legacy word
     */
    public enum WordType {
        PRONUNCIATION("Pronunciation"), URL("URL"), DYNAMIC("Dynamic");

        private final String value;

        WordType(String value) {
            this.value = value;
        }

        public String getType() {
            return value;
        }

        public static String[] getTypes() {
            WordType[] types = values();
            String[] names = new String[types.length];

            for (int i = 0; i < types.length; i++) {
                names[i] = types[i].getType();
            }

            Arrays.sort(names);
            return names;
        }

        public static WordType getWordType(String type) {
            for (WordType wt : values()) {
                if (wt.getType().equals(type)) {
                    return wt;
                }
            }

            throw new IllegalArgumentException("No word type " + type);
        }
    }

    /**
     * PhonemeMapping to use for conversion
     */
    private final AbstractNeoSpeechPhonemeMapping phonemeMapping;

    /**
     * Brief phoneme prefix
     */
    private final String prefix = "[";

    /**
     * Brief phoneme suffix
     */
    private final String suffix = "]";

    public LegacyDictionaryConverter(final Language language) {
        this.phonemeMapping = NeoSpeechPhonemeMappingFactory.getInstance()
                .getNeoSpeechPhonemesForLanguage(language);
    }

    /**
     * Convert word or phoneme to NeoSpeech format
     * 
     * @param wordToConvert
     *            The word to convert
     * @param wordType
     *            The WordType
     * @param legacyPhoneme
     *            The legacy phoneme
     * @return NeoSpeech formatted phoneme
     */
    public String convertWordOrPhoneme(String wordToConvert, String wordType,
            String legacyPhoneme) {
        String wordCurrentPhonemes = "";
        WordType type = WordType.getWordType(wordType);

        if (type == WordType.URL) {
            wordCurrentPhonemes = processAsUrl(wordToConvert);
        } else if (type == WordType.PRONUNCIATION) {
            wordCurrentPhonemes = processAsPronunciation(legacyPhoneme);
        } else if (type == WordType.DYNAMIC) {
            wordCurrentPhonemes = processAsDynamic(legacyPhoneme);
        } else {
            // A SUBSTITUTION/DYNAMIC/UNKNOWN
            wordCurrentPhonemes = wordToConvert;
        }

        return wordCurrentPhonemes;
    }

    /**
     * Process a dynamic replacement
     * 
     * @param legacy
     *            Legacy replacement
     * @return New replacement
     */
    private String processAsDynamic(String legacy) {
        return legacy.replaceAll("@", "#");
    }

    /**
     * Process a url
     * 
     * @param url
     *            The url to process
     * 
     * @return new URL phoneme
     */
    private String processAsUrl(String url) {
        String[] splitUrl = url.split("\\.");
        StringBuilder urlPhonetized = new StringBuilder();
        for (int i = 0; i < splitUrl.length; i++) {
            if (splitUrl[i].contains("/")) {
                String[] seperateSlashes = splitUrl[i].split("\\/");
                for (int j = 0; j < seperateSlashes.length; j++) {
                    if (j == 0) {
                        urlPhonetized.append(" dot ")
                                .append(seperateSlashes[j]);
                    } else {
                        urlPhonetized.append(" slash ").append(
                                seperateSlashes[j]);
                    }
                }
            } else if (i == 0) {
                urlPhonetized.append(splitUrl[i]);
            } else {
                urlPhonetized.append(" dot ").append(splitUrl[i]);
            }
        }
        return urlPhonetized.toString();
    }

    /**
     * Process a phoneme
     * 
     * @param legacyPhoneme
     *            The legacy phoneme
     * @return NeoSpeech formatted phoneme
     */
    private String processAsPronunciation(String legacyPhoneme) {
        // Step #1: Split Substitution part from Phonetic part
        String strResultingHybrid = "";
        String[] splitSubNPhonetic = legacyPhoneme.split(" ");
        for (String s : splitSubNPhonetic) {
            // Step #2: Concatenate substitution with converted phonetic part
            if (s.contains("\\!")) {
                if (s.contains("\\!p")) {
                    strResultingHybrid = strResultingHybrid + " <pause>";
                } else {
                    strResultingHybrid = strResultingHybrid + prefix
                            + phonemeStringFor(s) + suffix;
                }
            } else {
                if (strResultingHybrid.equalsIgnoreCase("")) {
                    strResultingHybrid = strResultingHybrid + s;
                } else {
                    strResultingHybrid = strResultingHybrid + " " + s;
                }
            }
        }

        strResultingHybrid = strResultingHybrid.replace("?", "");

        return strResultingHybrid;
    }

    private String phonemeStringFor(String legacyPhoneme) {
        char[] legacyPhonemeChars = legacyPhoneme.toCharArray();
        char accentLevel = '0';
        char aChar;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < legacyPhonemeChars.length; i++) {
            aChar = legacyPhonemeChars[i];
            if (Character.isDigit(aChar)) {
                accentLevel = aChar;
            } else if (aChar == '.') {
                accentLevel = '0';
            } else {
                String vowelPhonemes = phonemeMapping.getBmhVowelMap().get(
                        aChar);
                String consonantPhonemes = phonemeMapping.getBmhConsonantMap()
                        .get(aChar);
                if (vowelPhonemes != null) {
                    buffer.append(vowelPhonemes
                            + Character.toString(accentLevel) + " ");
                } else {
                    if (consonantPhonemes != null) {
                        buffer.append(consonantPhonemes + " ");
                    }
                }
            }
        }

        return buffer.toString();
    }
}
