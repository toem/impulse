//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EnumeratedType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EnumeratedType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://opcfoundation.org/BinarySchema/}OpaqueType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EnumeratedValue" type="{http://opcfoundation.org/BinarySchema/}EnumeratedValue" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;anyAttribute processContents='lax'/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnumeratedType", propOrder = {
    "enumeratedValue"
})
public class EnumeratedType
    extends OpaqueType
{

    @XmlElement(name = "EnumeratedValue", required = true)
    protected List<EnumeratedValue> enumeratedValue;

    /**
     * Gets the value of the enumeratedValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the enumeratedValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnumeratedValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnumeratedValue }
     * 
     * 
     */
    public List<EnumeratedValue> getEnumeratedValue() {
        if (enumeratedValue == null) {
            enumeratedValue = new ArrayList<EnumeratedValue>();
        }
        return this.enumeratedValue;
    }

}
