//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.12 at 12:58:15 PM CDT 
//

/**
 * JAXB Representation of the SSML audio Tag.
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

package com.raytheon.uf.common.bmh.schemas.ssml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for audio complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="audio">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/10/synthesis}audio">
 *       &lt;group ref="{http://www.w3.org/2001/10/synthesis}descAndSentenceAndStructure.class"/>
 *       &lt;attribute name="src" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = Audio.NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = Audio.NAME)
public class Audio extends OriginalAudio {

    public static final String NAME = "audio";

    private static final long serialVersionUID = 3274386136215811249L;

}
