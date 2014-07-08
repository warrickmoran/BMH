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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SimpleTextTransformation extends AbstractTextTransformation {

    private List<Serializable> appliedTransformation;

    /**
     * Constructor
     * 
     * @param text
     *            the text to replace
     * @throws JAXBException
     */
    public SimpleTextTransformation(String text, final String ssmlReplacement)
            throws SSMLConversionException {
        super(text, ssmlReplacement);
        this.determineDefaultReplacement();
    }

    private void determineDefaultReplacement() throws SSMLConversionException {
        appliedTransformation = convertSSML(this.ssmlReplacement);
    }

    @Override
    public List<Serializable> applyTransformation(String text)
            throws SSMLConversionException {
        return this.appliedTransformation;
    }
}