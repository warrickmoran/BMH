//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.12 at 12:58:15 PM CDT 
//

/**
 * JAXB representation of the SSML phoneme tag.
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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for phoneme complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="phoneme">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ph" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="alphabet" type="{http://www.w3.org/2001/10/synthesis}alphabet.datatype" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = Phoneme.NAME, propOrder = { "content" })
public class Phoneme implements Serializable {

    public static final String NAME = "phoneme";
    
    private static final long serialVersionUID = -170468800583551480L;

    @XmlValue
    protected String content;

    @XmlAttribute(name = "ph", required = true)
    protected String ph;

    @XmlAttribute(name = "alphabet")
    protected String alphabet;

    /**
     * Gets the value of the content property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Gets the value of the ph property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPh() {
        return ph;
    }

    /**
     * Sets the value of the ph property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPh(String value) {
        this.ph = value;
    }

    /**
     * Gets the value of the alphabet property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getAlphabet() {
        return alphabet;
    }

    /**
     * Sets the value of the alphabet property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setAlphabet(String value) {
        this.alphabet = value;
    }

}
