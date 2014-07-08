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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.ParsedPhoneme.ParseType;

/**
 * Parser class for legacy phonemes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2014   3355     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class LegacyPhonemeParser {
    /** Digit pattern */
    private final Pattern pattern = Pattern.compile("\\d");

    private final String PAUSE = "<pause>";

    /**
     * List of parsed phonemes
     */
    private final List<ParsedPhoneme> phonemeList = new ArrayList<ParsedPhoneme>();

    /**
     * Default constructor
     */
    public LegacyPhonemeParser() {

    }

    /**
     * Parse the provided phoneme
     * 
     * @param inputPhoneme
     *            The phoneme string to parse
     */
    public List<ParsedPhoneme> parse(String inputPhoneme) {
        int pause = inputPhoneme.indexOf(PAUSE);

        String[] parts = new String[1];
        if (pause > -1) {
            parts = inputPhoneme.split(PAUSE);
        } else {
            parts[0] = inputPhoneme;
        }

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int start = part.indexOf("[");
            if (start > -1) {
                List<Integer> idxList = new ArrayList<Integer>();
                if (start > 0) {
                    idxList.add(0);
                }
                char[] cha = part.toCharArray();
                for (int j = 0; j < cha.length; j++) {
                    if (cha[j] == '[' || cha[j] == ']') {
                        idxList.add(j);
                    }
                }

                if (!(idxList.get(idxList.size() - 1) == cha.length - 1)) {
                    idxList.add(cha.length);
                }

                int index = 0;
                int index2 = 1;

                while (index2 < idxList.size()) {
                    int useIndex = idxList.get(index);
                    if (part.substring(useIndex, useIndex + 1).equals("[")
                            || part.substring(useIndex, useIndex + 1).equals(
                                    "]")) {
                        useIndex++;
                    }
                    String ph = part.substring(useIndex, idxList.get(index2));
                    if (pattern.matcher(ph).find()) {
                        phonemeList
                                .add(new ParsedPhoneme(ph, ParseType.Phoneme));
                    } else {
                        phonemeList.add(new ParsedPhoneme(ph, ParseType.Text));
                    }

                    index = index2;
                    index2++;
                }
            } else if (part.indexOf("#") > -1) {
                ParsedPhoneme p = new ParsedPhoneme(part, ParseType.Dynamic);
                phonemeList.add(p);
            } else {
                // plain text
                ParsedPhoneme p = new ParsedPhoneme(part, ParseType.Text);
                phonemeList.add(p);
            }

            if (i + 1 < parts.length) {
                // Add the break
                ParsedPhoneme br = new ParsedPhoneme("break", ParseType.Break);
                phonemeList.add(br);
            }
        }

        return phonemeList;
    }

    // For testing
    public static void main(String[] args) {
        // String input = "[G EY1 EH0 L ] warrning";
        // String input = "mean lower <pause> low water";
        String input = "no [K UW1 L IH2 NG ]degree";
        LegacyPhonemeParser lpp = new LegacyPhonemeParser();
        List<ParsedPhoneme> list = lpp.parse(input);

        for (ParsedPhoneme p : list) {
            System.out.println(p.getParsedValue() + "   " + p.getType().name());
        }
    }
}
