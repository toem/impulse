//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse für FieldType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FieldType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://opcfoundation.org/BinarySchema/}Documentation" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="TypeName" type="{http://www.w3.org/2001/XMLSchema}QName" /&gt;
 *       &lt;attribute name="Length" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="LengthField" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="IsLengthInBytes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="SwitchField" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="SwitchValue" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="SwitchOperand" type="{http://opcfoundation.org/BinarySchema/}SwitchOperand" /&gt;
 *       &lt;attribute name="Terminator" type="{http://www.w3.org/2001/XMLSchema}hexBinary" /&gt;
 *       &lt;anyAttribute processContents='lax'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldType", propOrder = {
    "documentation"
})
public class FieldType {

    @XmlElement(name = "Documentation")
    protected Documentation documentation;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "TypeName")
    protected QName typeName;
    @XmlAttribute(name = "Length")
    @XmlSchemaType(name = "unsignedInt")
    protected Long length;
    @XmlAttribute(name = "LengthField")
    protected String lengthField;
    @XmlAttribute(name = "IsLengthInBytes")
    protected Boolean isLengthInBytes;
    @XmlAttribute(name = "SwitchField")
    protected String switchField;
    @XmlAttribute(name = "SwitchValue")
    @XmlSchemaType(name = "unsignedInt")
    protected Long switchValue;
    @XmlAttribute(name = "SwitchOperand")
    protected SwitchOperand switchOperand;
    @XmlAttribute(name = "Terminator")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] terminator;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Ruft den Wert der documentation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Documentation }
     *     
     */
    public Documentation getDocumentation() {
        return documentation;
    }

    /**
     * Legt den Wert der documentation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Documentation }
     *     
     */
    public void setDocumentation(Documentation value) {
        this.documentation = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der typeName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getTypeName() {
        return typeName;
    }

    /**
     * Legt den Wert der typeName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setTypeName(QName value) {
        this.typeName = value;
    }

    /**
     * Ruft den Wert der length-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLength() {
        return length;
    }

    /**
     * Legt den Wert der length-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLength(Long value) {
        this.length = value;
    }

    /**
     * Ruft den Wert der lengthField-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLengthField() {
        return lengthField;
    }

    /**
     * Legt den Wert der lengthField-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthField(String value) {
        this.lengthField = value;
    }

    /**
     * Ruft den Wert der isLengthInBytes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsLengthInBytes() {
        if (isLengthInBytes == null) {
            return false;
        } else {
            return isLengthInBytes;
        }
    }

    /**
     * Legt den Wert der isLengthInBytes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsLengthInBytes(Boolean value) {
        this.isLengthInBytes = value;
    }

    /**
     * Ruft den Wert der switchField-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitchField() {
        return switchField;
    }

    /**
     * Legt den Wert der switchField-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitchField(String value) {
        this.switchField = value;
    }

    /**
     * Ruft den Wert der switchValue-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSwitchValue() {
        return switchValue;
    }

    /**
     * Legt den Wert der switchValue-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSwitchValue(Long value) {
        this.switchValue = value;
    }

    /**
     * Ruft den Wert der switchOperand-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SwitchOperand }
     *     
     */
    public SwitchOperand getSwitchOperand() {
        return switchOperand;
    }

    /**
     * Legt den Wert der switchOperand-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SwitchOperand }
     *     
     */
    public void setSwitchOperand(SwitchOperand value) {
        this.switchOperand = value;
    }

    /**
     * Ruft den Wert der terminator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getTerminator() {
        return terminator;
    }

    /**
     * Legt den Wert der terminator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminator(byte[] value) {
        this.terminator = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
