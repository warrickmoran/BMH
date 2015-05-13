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

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;

/**
 * A simple find and replace transformation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * Jul 7, 2014  3302       bkowal      Implemented the transformation method.
 * Mar 24, 2015 4301       bkowal      Provide better support for basic text substitutions.
 *                                     Ensure that matches are not found within words.
 * Apr 10, 2015 4356       bkowal      Based on the regex padding that is added to all rules, ensure
 *                                     that the second matching group is used.
 * May 13, 2015 4403       bkowal      Reversed {@link #NON_ALPHA_REGEX} to match any rules that
 *                                     are not followed by alphanumeric characters or an apostrophe.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SimpleTextTransformation extends AbstractTextTransformation {

    /*
     * apostrophes allowed before the character.
     */
    private static final String NON_ALPHA_REGEX_APOSTROPHE = "([^\\w']+)";

    private static final String NON_ALPHA_REGEX = "([^\\w]+)";

    private List<Serializable> appliedTransformation;

    private final String originalText;

    /**
     * Constructor
     * 
     * @param text
     *            the text to replace
     * @throws JAXBException
     */
    public SimpleTextTransformation(String text, final String ssmlReplacement)
            throws SSMLConversionException, TransformationException {
        super(text, ssmlReplacement);
        this.determineDefaultReplacement();
        this.originalText = text.toLowerCase();
        super.setMatchGroup(2);
    }

    private void determineDefaultReplacement() throws SSMLConversionException {
        appliedTransformation = convertSSML(this.ssmlReplacement);
    }

    @Override
    protected String prepareTransformationRegex(String text) {
        return NON_ALPHA_REGEX_APOSTROPHE + "(" + Pattern.quote(text) + ")"
                + NON_ALPHA_REGEX;
    }

    @Override
    public boolean determineTransformationApplicability(IFreeText candidate) {
        /*
         * edge case - determine if the candidate only consists of a single
         * character.
         */
        String text = candidate.getText().toLowerCase();
        if (this.originalText.equals(text)) {
            candidate.claim(0, this.originalText.length());
            return true;
        }

        /*
         * apply standard rules.
         */
        return super.determineTransformationApplicability(candidate);
    }

    @Override
    public List<Serializable> applyTransformation(String text)
            throws SSMLConversionException {
        return this.appliedTransformation;
    }
}