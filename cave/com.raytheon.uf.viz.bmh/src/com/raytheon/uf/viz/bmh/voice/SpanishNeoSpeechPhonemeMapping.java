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
 * A neospeech phoneme mapping specific to the Spanish {@link Language}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2015            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SpanishNeoSpeechPhonemeMapping extends
        AbstractNeoSpeechPhonemeMapping {

    private static final int NUMERIC_STRESS_LIMIT = 1;

    /**
     * Constructor.
     */
    public SpanishNeoSpeechPhonemeMapping() {
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
        bmhConsonantMap.put('p', "P");
        bmhConsonantMap.put('b', "B");
        bmhConsonantMap.put('t', "T");
        bmhConsonantMap.put('d', "D");
        bmhConsonantMap.put('k', "K");
        bmhConsonantMap.put('g', "G");
        bmhConsonantMap.put('C', "CH");
        bmhConsonantMap.put('J', "JH");
        bmhConsonantMap.put('f', "F");
        bmhConsonantMap.put('B', "BH");
        bmhConsonantMap.put('D', "DH");
        bmhConsonantMap.put('s', "S");
        bmhConsonantMap.put('z', "S");
        bmhConsonantMap.put('h', "HH");
        bmhConsonantMap.put('G', "G");
        bmhConsonantMap.put('m', "M");
        bmhConsonantMap.put('n', "N");
        bmhConsonantMap.put('N', "NG");
        bmhConsonantMap.put('y', "NY");
        bmhConsonantMap.put('l', "L");
        bmhConsonantMap.put('J', "JH");
        bmhConsonantMap.put('r', "R");
        bmhConsonantMap.put('R', "RR");
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
        bmhVowelMap.put('e', "EH");
        bmhVowelMap.put('i', "IH");
        bmhVowelMap.put('o', "OH");
        bmhVowelMap.put('u', "UH");
    }
}