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

import javax.xml.bind.JAXBException;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractTextTransformation implements ITextTransformation {

    private static final String REPLACEMENT_TXT = "REPLACE_TXT";

    private static String ssmlSpeakWrapperText;

    private final Pattern transformationRegex;

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
            throws SSMLConversionException {
        if (ssmlSpeakWrapperText == null) {
            generateSpeakSSMLWrapper();
        }
        this.transformationRegex = Pattern.compile(text);
        this.ssmlReplacement = ssmlReplacement;
    }

    private static synchronized void generateSpeakSSMLWrapper()
            throws SSMLConversionException {
        SSMLDocument ssmlDocument = new SSMLDocument();
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
        Matcher matcher = this.transformationRegex.matcher(candidate.getText());
        while (matcher.find()) {
            final int endIndex = matcher.end();
            final String matchText = matcher.group(0);
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