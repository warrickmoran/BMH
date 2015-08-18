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

import java.util.List;
import java.util.LinkedList;
import java.util.Deque;

import org.apache.commons.lang.math.Range;
import org.apache.commons.lang.math.IntRange;

/**
 * Text that no transformations can be applied to. It will be used as is.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * Feb 09, 2015 4102       bkowal      Set the lastEndIndex to the endIndex instead of
 *                                     accumulating the indices.
 * Jul 06, 2015 4603       bkowal      Prevent the creation of elements for empty strings.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RulingFreeText extends AbstractTextRuling implements IFreeText {

    private List<Range> claims;

    /**
     * Constructor
     * 
     * @param text
     */
    public RulingFreeText(String text) {
        super(text);
        this.claims = new LinkedList<Range>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.xformer.data.IFreeText#claim(int, int)
     */
    @Override
    public void claim(int start, int end) {
        this.claims.add(new IntRange(start, end));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.xformer.data.IFreeText#eligibleForTransformation
     * ()
     */
    @Override
    public boolean eligibleForTransformation() {
        return (this.claims.isEmpty() == false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.xformer.data.IFreeText#applyRuling(com.raytheon
     * .uf.edex.bmh.xformer.data.ITextTransformation)
     */
    @Override
    public Deque<ITextRuling> applyRuling(ITextTransformation rule) {
        int lastEndIndex = 0;

        Deque<ITextRuling> transformedCandidates = new LinkedList<ITextRuling>();
        for (Range range : this.claims) {
            int beginIndex = range.getMinimumInteger();
            int endIndex = range.getMaximumInteger();

            if (transformedCandidates.isEmpty() == false) {
                // Remove the last element.
                transformedCandidates.removeLast();
            }
            String beforeTarget = this.getText().substring(lastEndIndex,
                    beginIndex);
            if (beforeTarget.trim().isEmpty() == false) {
                ITextRuling freeText = new RulingFreeText(beforeTarget);
                transformedCandidates.add(freeText);
            }

            String target = this.getText().substring(beginIndex, endIndex);
            IBoundText boundText = new RulingBoundText(target);
            boundText.setTransformation(rule);
            transformedCandidates.add(boundText);

            String afterTarget = this.getText().substring(endIndex);
            if (afterTarget.trim().isEmpty() == false) {
                ITextRuling freeText = new RulingFreeText(afterTarget);
                transformedCandidates.add(freeText);
            }

            lastEndIndex = endIndex;
        }

        return transformedCandidates;
    }
}