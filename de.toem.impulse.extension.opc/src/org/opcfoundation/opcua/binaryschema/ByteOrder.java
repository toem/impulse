//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ByteOrder.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="ByteOrder"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="BigEndian"/&gt;
 *     &lt;enumeration value="LittleEndian"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ByteOrder")
@XmlEnum
public enum ByteOrder {

    @XmlEnumValue("BigEndian")
    BIG_ENDIAN("BigEndian"),
    @XmlEnumValue("LittleEndian")
    LITTLE_ENDIAN("LittleEndian");
    private final String value;

    ByteOrder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ByteOrder fromValue(String v) {
        for (ByteOrder c: ByteOrder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
