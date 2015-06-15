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

/**
 * A neospeech phoneme mapping specific to the English {@link Language}.
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

public class EnglishNeoSpeechPhonemeMapping extends
        AbstractNeoSpeechPhonemeMapping {

    private static final int NUMERIC_STRESS_LIMIT = 2;

    /**
     * Constructor.
     */
    public EnglishNeoSpeechPhonemeMapping() {
        super(NUMERIC_STRESS_LIMIT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.voice.AbstractNeoSpeechPhonemeMapping#
     * constructConsonantPhonemeMapping()
     */
    @Override
    protected void constructConsonantPhonemeMapping() {
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.voice.AbstractNeoSpeechPhonemeMapping#
     * constructVowelPhonemeMapping()
     */
    @Override
    protected void constructVowelPhonemeMapping() {
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
    }
}