//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für OpaqueType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="OpaqueType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://opcfoundation.org/BinarySchema/}TypeDescription"&gt;
 *       &lt;attribute name="LengthInBits" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ByteOrderSignificant" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;anyAttribute processContents='lax'/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpaqueType")
@XmlSeeAlso({
    EnumeratedType.class
})
public class OpaqueType
    extends TypeDescription
{

    @XmlAttribute(name = "LengthInBits")
    protected Integer lengthInBits;
    @XmlAttribute(name = "ByteOrderSignificant")
    protected Boolean byteOrderSignificant;

    /**
     * Ruft den Wert der lengthInBits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLengthInBits() {
        return lengthInBits;
    }

    /**
     * Legt den Wert der lengthInBits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLengthInBits(Integer value) {
        this.lengthInBits = value;
    }

    /**
     * Ruft den Wert der byteOrderSignificant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isByteOrderSignificant() {
        if (byteOrderSignificant == null) {
            return false;
        } else {
            return byteOrderSignificant;
        }
    }

    /**
     * Legt den Wert der byteOrderSignificant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setByteOrderSignificant(Boolean value) {
        this.byteOrderSignificant = value;
    }

}
