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
package com.raytheon.uf.common.bmh.schemas.ssml.jaxb;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.schemas.ssml.ObjectFactory;
import com.raytheon.uf.common.serialization.JAXBManager;

/**
 * Manages the jaxb manager instance that is used to convert the JAXB Java
 * classes to XML.
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

public class SSMLJaxbManager {

    private static SSMLJaxbManager instance;

    private final JAXBManager jaxbManager;

    public static synchronized SSMLJaxbManager getInstance()
            throws JAXBException {
        if (instance == null) {
            instance = new SSMLJaxbManager();
        }
        return instance;
    }

    /**
     * 
     */
    protected SSMLJaxbManager() throws JAXBException {
        this.jaxbManager = new JAXBManager(ObjectFactory.class);
    }

    public JAXBManager getJaxbManager() {
        return jaxbManager;
    }
}