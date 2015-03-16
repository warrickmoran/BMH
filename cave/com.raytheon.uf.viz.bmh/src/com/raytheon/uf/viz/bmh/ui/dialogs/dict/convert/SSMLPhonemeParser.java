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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import javax.xml.bind.JAXBElement;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.schemas.ssml.Break;
import com.raytheon.uf.common.bmh.schemas.ssml.Phoneme;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.common.bmh.schemas.ssml.Speak;
import com.raytheon.uf.common.bmh.schemas.ssml.jaxb.SSMLJaxbManager;

/**
 * Converts a {@link String} to a {@link List} of {@link Serializable}s
 * representing the contents of a SSML document.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 16, 2015 4283       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SSMLPhonemeParser {

    /**
     * JAXB manager for SSML
     */
    private static SSMLJaxbManager jaxb;

    private static String ssmlSpeakWrapper;

    private static final String REPLACE = "REPLACEME";

    /**
     * Constructor - protected so that this class cannot be constructed
     * directly.
     */
    protected SSMLPhonemeParser() {
    }

    /**
     * Converts the specified {@link String} to a {@link List} of
     * {@link Serializable}s representing the contents of a SSML document.
     * 
     * @param text
     *            the specified {@link String}
     * @return a {@link List} of {@link Serializable}s
     * @throws Exception
     */
    public static List<Serializable> parse(String text) throws Exception {
        if (jaxb == null) {
            jaxb = SSMLJaxbManager.getInstance();
        }

        if (ssmlSpeakWrapper == null) {
            /*
             * In this case the language does not matter because we just extract
             * the inner tags from the SSML Document.
             */
            SSMLDocument ssmlDocument = new SSMLDocument(Language.ENGLISH);
            // Need to replace contents to get correct string representation
            ssmlDocument.getRootTag().getContent().add(REPLACE);
            ssmlSpeakWrapper = jaxb.getJaxbManager().marshalToXml(
                    ssmlDocument.getRootTag());
        }

        String updatedXML = ssmlSpeakWrapper.replaceAll(REPLACE, text);
        SSMLDocument ssmlDocument = SSMLDocument.fromSSML(updatedXML);
        Speak speak = ssmlDocument.getRootTag();
        if (speak.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<Serializable> ssmlContents = new LinkedList<>();
        for (Serializable s : speak.getContent()) {
            if (s instanceof JAXBElement<?>) {
                JAXBElement<?> element = (JAXBElement<?>) s;
                if (element.getValue() instanceof Phoneme) {
                    ssmlContents.add((Phoneme) element.getValue());
                } else if (element.getValue() instanceof Break) {
                    ssmlContents.add((Break) element.getValue());
                } else if (element.getValue() instanceof SayAs) {
                    ssmlContents.add((SayAs) element.getValue());
                }
            } else if (s instanceof String) {
                ssmlContents.add(s);
            }
        }
        return ssmlContents;
    }
}