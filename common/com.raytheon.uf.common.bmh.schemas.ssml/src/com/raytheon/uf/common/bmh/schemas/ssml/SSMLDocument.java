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
package com.raytheon.uf.common.bmh.schemas.ssml;

import javax.xml.bind.JAXBException;


import com.raytheon.uf.common.bmh.schemas.ssml.jaxb.SSMLJaxbManager;

/**
 * Represents a SSML Document. Provides a convenient way to initialize the SSML
 * speak root element and generate a SSML String via JAXB.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 12, 2014 3259       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SSMLDocument {

    private static final String SSML_VERSION = "1.0";

    private static final String DEFAULT_LANG = "en-US";

    private final ObjectFactory factory;

    private final Speak rootTag;

    /**
     * 
     */
    public SSMLDocument() {
        this.factory = new ObjectFactory();

        this.rootTag = this.factory.createSpeak();
        /* Set required properties on the root tag. */
        this.rootTag.setVersion(SSML_VERSION);
        this.rootTag.setLang(DEFAULT_LANG);
    }

    public String toSSML() throws JAXBException {
        return SSMLJaxbManager.getInstance().getJaxbManager()
                .marshalToXml(this.rootTag);
    }

    public ObjectFactory getFactory() {
        return factory;
    }

    public Speak getRootTag() {
        return rootTag;
    }
}