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

import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;

/**
 * Describes a generic Text Transformation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * Jul 7, 2014  3302       bkowal      Updated the transformation application method
 *                                     to return a list of Serializable.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface ITextTransformation {
    /**
     * Determines if the transformation can be applied to the specified text
     * candidate.
     * 
     * @param candidate
     *            the specified text candidate.
     * @return true, if the transformation can be applied; false, otherwise
     */
    public boolean determineTransformationApplicability(IFreeText candidate);

    /**
     * <TBD> Applies the transformation. Final implementation will be dependent
     * on how the BMH Word.java is used and defined.
     * 
     * @param text
     *            the raw text to apply the transformation to
     * @return the result of the transformation
     */
    public List<Serializable> applyTransformation(String text)
            throws SSMLConversionException;
}