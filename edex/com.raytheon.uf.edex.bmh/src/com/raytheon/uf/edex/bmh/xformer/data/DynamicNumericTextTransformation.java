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
package com.raytheon.uf.edex.bmh.xformer.data;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;

/**
 * A text transformation that will extract numeric values from the source String
 * and place them into specific positions in a destination String.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  3302       bkowal      Initial creation
 * Aug 26, 2014 3559       bkowal      Lower case all text when constructing dictionary
 *                                     regex and determining if rules apply.
 * Mar 24, 2015 4301       bkowal      Implement and override {@link #prepareTransformationRegex(String)}.
 * Jul 16, 2015 4603       bkowal      Fixed dynamic numerical text matching.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DynamicNumericTextTransformation extends
        AbstractTextTransformation {

    private static final String NUMBER_REGEX = "[0-9]";

    private Pattern basePattern;

    /**
     * @param text
     * @param ssmlReplacement
     * @throws SSMLConversionException
     */
    public DynamicNumericTextTransformation(String text, String ssmlReplacement)
            throws SSMLConversionException, TransformationException {
        super(text, ssmlReplacement);
    }

    @Override
    protected String prepareTransformationRegex(String text) {
        StringBuilder regexBuilder = new StringBuilder();
        StringBuilder textBuilder = new StringBuilder();
        int digitCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '#') {
                if (textBuilder.length() > 0) {
                    regexBuilder.append(Pattern.quote(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                ++digitCount;
            } else {
                if (digitCount > 0) {
                    for (int i = 0; i < digitCount; i++) {
                        regexBuilder.append("(").append(NUMBER_REGEX)
                                .append(")");
                    }
                    digitCount = 0;
                }
                textBuilder.append(c);
            }
        }
        if (digitCount > 0) {
            for (int i = 0; i < digitCount; i++) {
                regexBuilder.append("(").append(NUMBER_REGEX).append(")");
            }
        } else if (textBuilder.length() > 0) {
            regexBuilder.append(Pattern.quote(textBuilder.toString()));
        }

        this.basePattern = Pattern.compile(regexBuilder.toString());
        return super.prepareTransformationRegex(regexBuilder.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.xformer.data.AbstractTextTransformation#
     * applyTransformation(java.lang.String)
     */
    @Override
    public List<Serializable> applyTransformation(String text)
            throws SSMLConversionException {
        /*
         * The client will ensure that rules that do not specify an equal number
         * of digits on both sides of the rule are not created. However, there
         * is nothing to stop users from manually adding rules to the database
         * ... so, we will be handling that here.
         */

        String result = this.ssmlReplacement;
        Matcher matcher = this.basePattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result = result.replaceFirst(Word.DYNAMIC_NUMERIC_CHAR,
                        matcher.group(i));
            }
        }

        return convertSSML(result);
    }
}