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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractTextTransformation implements ITextTransformation {

    private final Pattern transformationRegex;

    /**
     * Constructor
     * 
     * @param text
     *            the text of interest
     * @param transformationApplication
     *            TBD manifestation of how the transformation will be applied;
     *            dependent on the final state of Word.java
     */
    public AbstractTextTransformation(String text,
            Object transformationApplication) {
        this.transformationRegex = Pattern.compile(text);
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
    public Object applyTransformation(String text) {
        return null;
    }
}