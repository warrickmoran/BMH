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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;

/**
 * Abstract representation of a text transformation definition.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * Jul 7, 2014  3302       bkowal      Implemented the transformation capabilities.
 * Aug 26, 2014 3559       bkowal      Lower case all text when constructing dictionary
 *                                     regex and determining if rules apply.
 * Feb 24, 2015 4157       bkowal      Specify a {@link Language} for the {@link SSMLDocument}.
 * Mar 24, 2015 4301       bkowal      Provide better support for plain text rules.
 * Apr 10, 2015 4356       bkowal      Use the end index of the group of interest.
 * Jul 06, 2015 4603       bkowal      Improved matching text retrieval.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractTextTransformation implements ITextTransformation {

    /*
     * apostrophes allowed before the character.
     */
    protected static final String NON_ALPHA_REGEX_APOSTROPHE = "(?<=[^\\w'])+";

    protected static final String NON_ALPHA_REGEX = "(?=[^\\w])+";

    private static final String REPLACEMENT_TXT = "REPLACE_TXT";

    private static String ssmlSpeakWrapperText;

    protected final Pattern transformationRegex;

    protected String ssmlReplacement;

    /**
     * Constructor
     * 
     * @param text
     *            the text of interest
     * @param ssmlReplacement
     *            the ssml snippet that will be utilized in the application of
     *            the transformation
     * @throws JAXBException
     */
    public AbstractTextTransformation(String text, final String ssmlReplacement)
            throws SSMLConversionException, TransformationException {
        if (ssmlSpeakWrapperText == null) {
            generateSpeakSSMLWrapper();
        }

        try {
            this.transformationRegex = Pattern.compile(this
                    .prepareTransformationRegex(text.toLowerCase()));
        } catch (PatternSyntaxException e) {
            throw new TransformationException(text.toLowerCase(), e);
        }
        this.ssmlReplacement = ssmlReplacement;
    }

    protected String prepareTransformationRegex(String text) {
        StringBuilder sb = new StringBuilder("(?:");

        /*
         * case 1: text is bounded by spaces or punctuation on both sides.
         */
        sb.append(NON_ALPHA_REGEX_APOSTROPHE).append(text)
                .append(NON_ALPHA_REGEX);

        /*
         * case 2: text starts at the beginning of the character sequence and is
         * bounded by spaces or punctuation on the right.
         */
        sb.append(")|(?:");
        sb.append("^").append(text).append(NON_ALPHA_REGEX);

        /*
         * case 3: text is located at the end of the line and is bounded by
         * spaces or punctuation on the left.
         */
        sb.append(")|(?:");
        sb.append(NON_ALPHA_REGEX_APOSTROPHE).append(text).append("$)");

        return sb.toString();
    }

    private static synchronized void generateSpeakSSMLWrapper()
            throws SSMLConversionException {
        /*
         * In this case the language does not matter because we just extract the
         * inner tags from the SSML Document.
         */
        SSMLDocument ssmlDocument = new SSMLDocument(Language.ENGLISH);
        ssmlDocument.getRootTag().getContent().add(REPLACEMENT_TXT);

        ssmlSpeakWrapperText = ssmlDocument.toSSML();
    }

    protected static final List<Serializable> convertSSML(String ssml)
            throws SSMLConversionException {
        String ssmlToConvert = ssmlSpeakWrapperText.replace(REPLACEMENT_TXT,
                ssml);
        SSMLDocument ssmlDocument = SSMLDocument.fromSSML(ssmlToConvert);

        return ssmlDocument.getRootTag().getContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.xformer.data.ITextTransformation#
     * determineTransformationApplicability
     * (com.raytheon.uf.edex.bmh.xformer.data.IFreeText)
     */
    @Override
    public boolean determineTransformationApplicability(IFreeText candidate) {
        Matcher matcher = this.transformationRegex.matcher(candidate.getText()
                .toLowerCase());
        while (matcher.find()) {
            final int endIndex = matcher.end();
            final String matchText = matcher.group();
            final int beginIndex = endIndex - matchText.length();

            candidate.claim(beginIndex, endIndex);
        }

        return candidate.eligibleForTransformation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.xformer.data.ITextTransformation#applyTransformation
     * (java.lang.String)
     */
    @Override
    public abstract List<Serializable> applyTransformation(String text)
            throws SSMLConversionException;
}