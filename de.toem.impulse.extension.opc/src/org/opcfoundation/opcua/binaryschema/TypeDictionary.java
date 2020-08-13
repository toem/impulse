//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://opcfoundation.org/BinarySchema/}Documentation" minOccurs="0"/&gt;
 *         &lt;element name="Import" type="{http://opcfoundation.org/BinarySchema/}ImportDirective" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="OpaqueType" type="{http://opcfoundation.org/BinarySchema/}OpaqueType"/&gt;
 *           &lt;element name="EnumeratedType" type="{http://opcfoundation.org/BinarySchema/}EnumeratedType"/&gt;
 *           &lt;element name="StructuredType" type="{http://opcfoundation.org/BinarySchema/}StructuredType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="TargetNamespace" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DefaultByteOrder" type="{http://opcfoundation.org/BinarySchema/}ByteOrder" /&gt;
 *       &lt;anyAttribute processContents='lax'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "documentation",
    "_import",
    "opaqueTypeOrEnumeratedTypeOrStructuredType"
})
@XmlRootElement(name = "TypeDictionary")
public class TypeDictionary {

    @XmlElement(name = "Documentation")
    protected Documentation documentation;
    @XmlElement(name = "Import")
    protected List<ImportDirective> _import;
    @XmlElements({
        @XmlElement(name = "OpaqueType", type = OpaqueType.class),
        @XmlElement(name = "EnumeratedType", type = EnumeratedType.class),
        @XmlElement(name = "StructuredType", type = StructuredType.class)
    })
    protected List<TypeDescription> opaqueTypeOrEnumeratedTypeOrStructuredType;
    @XmlAttribute(name = "TargetNamespace", required = true)
    protected String targetNamespace;
    @XmlAttribute(name = "DefaultByteOrder")
    protected ByteOrder defaultByteOrder;
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
     * Gets the value of the import property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the import property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImport().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImportDirective }
     * 
     * 
     */
    public List<ImportDirective> getImport() {
        if (_import == null) {
            _import = new ArrayList<ImportDirective>();
        }
        return this._import;
    }

    /**
     * Gets the value of the opaqueTypeOrEnumeratedTypeOrStructuredType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the opaqueTypeOrEnumeratedTypeOrStructuredType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOpaqueTypeOrEnumeratedTypeOrStructuredType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpaqueType }
     * {@link EnumeratedType }
     * {@link StructuredType }
     * 
     * 
     */
    public List<TypeDescription> getOpaqueTypeOrEnumeratedTypeOrStructuredType() {
        if (opaqueTypeOrEnumeratedTypeOrStructuredType == null) {
            opaqueTypeOrEnumeratedTypeOrStructuredType = new ArrayList<TypeDescription>();
        }
        return this.opaqueTypeOrEnumeratedTypeOrStructuredType;
    }

    /**
     * Ruft den Wert der targetNamespace-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * Legt den Wert der targetNamespace-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetNamespace(String value) {
        this.targetNamespace = value;
    }

    /**
     * Ruft den Wert der defaultByteOrder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ByteOrder }
     *     
     */
    public ByteOrder getDefaultByteOrder() {
        return defaultByteOrder;
    }

    /**
     * Legt den Wert der defaultByteOrder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ByteOrder }
     *     
     */
    public void setDefaultByteOrder(ByteOrder value) {
        this.defaultByteOrder = value;
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
