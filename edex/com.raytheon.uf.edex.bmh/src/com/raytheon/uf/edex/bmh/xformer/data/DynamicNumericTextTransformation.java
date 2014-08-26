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
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.lang.math.NumberUtils;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DynamicNumericTextTransformation extends
        AbstractTextTransformation {

    private static final String NUMBER_REGEX = "([0-9])";

    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);

    /**
     * @param text
     * @param ssmlReplacement
     * @throws SSMLConversionException
     */
    public DynamicNumericTextTransformation(String text, String ssmlReplacement)
            throws SSMLConversionException {
        super(buildNumericTransformationRegex(text), ssmlReplacement);
    }

    private static String buildNumericTransformationRegex(String originalRegex) {
        return originalRegex.replace(Word.DYNAMIC_NUMERIC_CHAR, NUMBER_REGEX);
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
        List<Integer> extractedNumerics = new LinkedList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            /*
             * If it matches the pattern, then there should be no reason to
             * perform additional verifications to verify that it is numeric.
             */
            extractedNumerics.add(NumberUtils.toInt(matcher.group(0)));
        }
        String result = this.ssmlReplacement;
        for (Integer extractedNumeric : extractedNumerics) {
            result = result.replaceFirst(Word.DYNAMIC_NUMERIC_CHAR,
                    extractedNumeric.toString());
        }

        return convertSSML(result);
    }
}