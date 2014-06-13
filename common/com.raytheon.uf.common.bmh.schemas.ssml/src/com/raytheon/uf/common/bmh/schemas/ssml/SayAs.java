//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.12 at 12:58:15 PM CDT 
//

/**
 * JAXB representation of the SSML say-as tag.
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for say-as complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="say-as">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="interpret-as" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="format" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="detail" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = SayAs.NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = SayAs.NAME, propOrder = { "content" })
public class SayAs implements Serializable {

    public static final String NAME = "say-as";

    private static final long serialVersionUID = 1724786894618795082L;

    @XmlValue
    protected String content;

    @XmlAttribute(name = "interpret-as", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String interpretAs;

    @XmlAttribute(name = "format")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String format;

    @XmlAttribute(name = "detail")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String detail;

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
     * Gets the value of the interpretAs property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getInterpretAs() {
        return interpretAs;
    }

    /**
     * Sets the value of the interpretAs property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setInterpretAs(String value) {
        this.interpretAs = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the detail property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Sets the value of the detail property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDetail(String value) {
        this.detail = value;
    }

}
